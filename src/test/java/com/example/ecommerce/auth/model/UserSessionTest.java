package com.example.ecommerce.auth.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserSessionTest {

    @Test
    @DisplayName("Should construct UserSession with valid fields and immutable roles")
    void testConstructorAndGetters() {
        Set<String> roles = Set.of("CUSTOMER", "AFFILIATE");
        UserSession session = new UserSession(101, "test@shopkart.com", "John", "Doe", "+1234567890", roles);

        assertEquals(101, session.getUserId());
        assertEquals("test@shopkart.com", session.getEmail());
        assertEquals("John", session.getFirstName());
        assertEquals("Doe", session.getLastName());
        assertEquals("+1234567890", session.getPhone());
        assertEquals("John Doe", session.getFullName());
        assertTrue(session.isCustomer());
        assertFalse(session.isAdmin());
        assertTrue(session.isAuthenticated());
        assertEquals(2, session.getRoles().size());
        assertThrows(UnsupportedOperationException.class, () -> session.getRoles().add("HACKER"));
    }

    @Test
    @DisplayName("Should handle null phone and roles gracefully in constructor")
    void testNullPhoneAndRoles() {
        UserSession session = new UserSession(202, "nulltest@shopkart.com", "Jane", "Smith", null, null);

        assertEquals("", session.getPhone());
        assertNotNull(session.getRoles());
        assertTrue(session.getRoles().isEmpty());
        assertFalse(session.isAdmin());
        assertFalse(session.isCustomer());
        assertFalse(session.hasRole(null));
        assertFalse(session.hasRole("ADMIN"));
    }

    @Test
    @DisplayName("Should format getFullName correctly without leading or trailing spaces")
    void testGetFullNameFormatting() {
        UserSession both = new UserSession(1, "u@test.com", "John", "Doe", Set.of());
        assertEquals("John Doe", both.getFullName());

        UserSession bothPadded = new UserSession(1, "u@test.com", "  John  ", "  Doe  ", Set.of());
        assertEquals("John Doe", bothPadded.getFullName());

        UserSession firstOnly = new UserSession(2, "u@test.com", "John", null, Set.of());
        assertEquals("John", firstOnly.getFullName());

        UserSession firstOnlyEmptyLast = new UserSession(2, "u@test.com", "John", "", Set.of());
        assertEquals("John", firstOnlyEmptyLast.getFullName());

        UserSession lastOnly = new UserSession(3, "u@test.com", null, "Doe", Set.of());
        assertEquals("Doe", lastOnly.getFullName());

        UserSession lastOnlyEmptyFirst = new UserSession(3, "u@test.com", "   ", "Doe", Set.of());
        assertEquals("Doe", lastOnlyEmptyFirst.getFullName());

        UserSession neither = new UserSession(4, "u@test.com", null, null, Set.of());
        assertEquals("", neither.getFullName());

        UserSession bothBlank = new UserSession(4, "u@test.com", "   ", "   ", Set.of());
        assertEquals("", bothBlank.getFullName());
    }

    @Test
    @DisplayName("Should verify role checks including SUPER_ADMIN and ADMIN")
    void testAdminAndRoleChecks() {
        UserSession adminSession = new UserSession(1, "admin@shopkart.com", "Admin", "User", Set.of("admin"));
        assertTrue(adminSession.isAdmin());
        assertFalse(adminSession.isSuperAdmin());
        assertTrue(adminSession.hasRole("ADMIN"));
        assertTrue(adminSession.hasRole(" admin "));

        UserSession superAdminSession = new UserSession(2, "super@shopkart.com", "Super", "Admin", Set.of("super_admin"));
        assertTrue(superAdminSession.isAdmin());
        assertTrue(superAdminSession.isSuperAdmin());
        assertTrue(superAdminSession.hasRole("super_admin"));

        UserSession customerSession = new UserSession(3, "cust@shopkart.com", "Customer", "User", Set.of("customer"));
        assertFalse(customerSession.isAdmin());
        assertFalse(customerSession.isSuperAdmin());
        assertTrue(customerSession.isCustomer());
    }

    @Test
    @DisplayName("Should build UserSession from User entity cleanly")
    void testFromUser() {
        assertNull(UserSession.fromUser(null));

        User user = new User(10, "user@test.com", "hash", "Alice", "Wonder", "9876543210", "ACTIVE");
        Role r1 = new Role(1, "customer", "Customer Role");
        Role r2 = new Role(2, "admin", "Admin Role");
        user.setRoles(List.of(r1, r2));

        UserSession session = UserSession.fromUser(user);
        assertNotNull(session);
        assertEquals(10, session.getUserId());
        assertEquals("user@test.com", session.getEmail());
        assertEquals("Alice", session.getFirstName());
        assertEquals("Wonder", session.getLastName());
        assertEquals("Alice Wonder", session.getFullName());
        assertEquals("9876543210", session.getPhone());
        assertTrue(session.isAdmin());
        assertTrue(session.isCustomer());
    }

    @Test
    @DisplayName("Should correctly implement equals, hashCode, and toString")
    void testEqualsHashCodeAndToString() {
        UserSession s1 = new UserSession(10, "a@b.com", "A", "B", Set.of("CUSTOMER"));
        UserSession s2 = new UserSession(10, "a@b.com", "Different", "Name", Set.of("CUSTOMER"));
        UserSession s3 = new UserSession(20, "c@d.com", "A", "B", Set.of("CUSTOMER"));

        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());
        assertNotEquals(s1, s3);
        assertNotEquals(s1, null);
        assertNotEquals(s1, "someString");

        String str = s1.toString();
        assertTrue(str.contains("userId=10"));
        assertTrue(str.contains("email='a@b.com'"));
        assertTrue(str.contains("fullName='A B'"));
        assertTrue(str.contains("roles=[CUSTOMER]"));
    }

    @Test
    @DisplayName("Should be fully serializable and deserializable across session persistence")
    void testSerializationRoundTrip() throws Exception {
        UserSession original = new UserSession(42, "serial@shopkart.com", "Serial", "User", "+919999999999", Set.of("ADMIN", "CUSTOMER"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(original);
        }

        byte[] serializedBytes = baos.toByteArray();
        assertTrue(serializedBytes.length > 0);

        UserSession deserialized;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(serializedBytes))) {
            deserialized = (UserSession) ois.readObject();
        }

        assertNotNull(deserialized);
        assertEquals(original, deserialized);
        assertEquals(original.getUserId(), deserialized.getUserId());
        assertEquals(original.getEmail(), deserialized.getEmail());
        assertEquals(original.getFirstName(), deserialized.getFirstName());
        assertEquals(original.getLastName(), deserialized.getLastName());
        assertEquals(original.getPhone(), deserialized.getPhone());
        assertEquals(original.getFullName(), deserialized.getFullName());
        assertEquals(original.isAdmin(), deserialized.isAdmin());
        assertEquals(original.isCustomer(), deserialized.isCustomer());
        assertEquals(original.getRoles(), deserialized.getRoles());
    }
}
