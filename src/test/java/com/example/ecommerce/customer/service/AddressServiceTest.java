package com.example.ecommerce.customer.service;

import com.example.ecommerce.customer.dao.AddressDAO;
import com.example.ecommerce.customer.model.Address;
import com.example.ecommerce.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AddressService Unit Tests with Mockito")
class AddressServiceTest {

    @Mock
    private AddressDAO addressDAO;

    @InjectMocks
    private AddressService addressService;

    @Test
    @DisplayName("Should automatically set first address created by user as DEFAULT")
    void testSaveAddressFirstBecomesDefault() {
        Address addr = new Address();
        addr.setUserId(1);
        addr.setFullName("John Doe");
        addr.setPhone("+91 9876543210");
        addr.setAddressLine1("123 Tech Park");
        addr.setCity("Bengaluru");
        addr.setState("Karnataka");
        addr.setPostalCode("560001");
        addr.setDefaultAddress(false);

        when(addressDAO.countByUserId(1)).thenReturn(0);
        when(addressDAO.createAddress(any(Address.class))).thenReturn(101);

        Address saved = addressService.saveAddress(addr);

        assertTrue(saved.isDefaultAddress());
        assertEquals("9876543210", saved.getPhone()); // normalized
        verify(addressDAO, times(1)).setDefaultAddress(101, 1);
    }

    @Test
    @DisplayName("Should throw ValidationException when mandatory address fields are missing")
    void testSaveAddressValidationFailures() {
        Address addr = new Address();
        addr.setUserId(1);
        // Missing full name, phone, addressLine1, etc.

        assertThrows(ValidationException.class, () -> addressService.saveAddress(addr));
    }

    @Test
    @DisplayName("Should reject address with digits in recipient name (e.g. Rahul434343434)")
    void testSaveAddressInvalidNameWithDigits() {
        Address addr = new Address();
        addr.setUserId(1);
        addr.setFullName("Rahul434343434");
        addr.setPhone("9876543210");
        addr.setAddressLine1("Flat 402, Sunshine Residency");
        addr.setCity("Bengaluru");
        addr.setState("Karnataka");
        addr.setPostalCode("560001");

        ValidationException ex = assertThrows(ValidationException.class, () -> addressService.saveAddress(addr));
        assertTrue(ex.getErrors().stream().anyMatch(e -> e.contains("valid recipient name")));
    }

    @Test
    @DisplayName("Should reject address with invalid mobile number (e.g. 767676633333232322)")
    void testSaveAddressInvalidPhoneLength() {
        Address addr = new Address();
        addr.setUserId(1);
        addr.setFullName("Rahul Sharma");
        addr.setPhone("767676633333232322");
        addr.setAddressLine1("Flat 402, Sunshine Residency");
        addr.setCity("Bengaluru");
        addr.setState("Karnataka");
        addr.setPostalCode("560001");

        ValidationException ex = assertThrows(ValidationException.class, () -> addressService.saveAddress(addr));
        assertTrue(ex.getErrors().stream().anyMatch(e -> e.contains("valid 10-digit mobile number")));
    }

    @Test
    @DisplayName("Should reject address with digits in city name (e.g. pune4343434)")
    void testSaveAddressInvalidCityWithDigits() {
        Address addr = new Address();
        addr.setUserId(1);
        addr.setFullName("Rahul Sharma");
        addr.setPhone("9876543210");
        addr.setAddressLine1("Flat 402, Sunshine Residency");
        addr.setCity("pune4343434");
        addr.setState("Maharashtra");
        addr.setPostalCode("411001");

        ValidationException ex = assertThrows(ValidationException.class, () -> addressService.saveAddress(addr));
        assertTrue(ex.getErrors().stream().anyMatch(e -> e.contains("valid City name")));
    }

    @Test
    @DisplayName("Should reject address with digits in state name (e.g. Maharashtra3343434)")
    void testSaveAddressInvalidStateWithDigits() {
        Address addr = new Address();
        addr.setUserId(1);
        addr.setFullName("Rahul Sharma");
        addr.setPhone("9876543210");
        addr.setAddressLine1("Flat 402, Sunshine Residency");
        addr.setCity("Pune");
        addr.setState("Maharashtra3343434");
        addr.setPostalCode("411001");

        ValidationException ex = assertThrows(ValidationException.class, () -> addressService.saveAddress(addr));
        assertTrue(ex.getErrors().stream().anyMatch(e -> e.contains("valid State name")));
    }

    @Test
    @DisplayName("Should reject address with too short/invalid address line 1 (e.g. 402)")
    void testSaveAddressShortAddressLine1() {
        Address addr = new Address();
        addr.setUserId(1);
        addr.setFullName("Rahul Sharma");
        addr.setPhone("9876543210");
        addr.setAddressLine1("402");
        addr.setCity("Pune");
        addr.setState("Maharashtra");
        addr.setPostalCode("411001");

        ValidationException ex = assertThrows(ValidationException.class, () -> addressService.saveAddress(addr));
        assertTrue(ex.getErrors().stream().anyMatch(e -> e.contains("Address Line 1 must be between 5 and 255 characters")));
    }

    @Test
    @DisplayName("Should reject address with invalid pincode (e.g. 56010)")
    void testSaveAddressInvalidPincode() {
        Address addr = new Address();
        addr.setUserId(1);
        addr.setFullName("Rahul Sharma");
        addr.setPhone("9876543210");
        addr.setAddressLine1("Flat 402, Sunshine Residency");
        addr.setCity("Pune");
        addr.setState("Maharashtra");
        addr.setPostalCode("56010");

        ValidationException ex = assertThrows(ValidationException.class, () -> addressService.saveAddress(addr));
        assertTrue(ex.getErrors().stream().anyMatch(e -> e.contains("Postal PIN code must be exactly 6 digits")));
    }

    @Test
    @DisplayName("Should reject address deletion if userId does not own the address")
    void testDeleteAddressUnauthorizedUser() {
        Address addr = new Address();
        addr.setAddressId(10);
        addr.setUserId(1); // Belongs to User 1

        when(addressDAO.findById(10)).thenReturn(Optional.of(addr));

        // User 2 attempts to delete User 1's address
        assertThrows(ValidationException.class, () ->
            addressService.deleteAddress(10, 2)
        );

        verify(addressDAO, never()).deleteAddress(anyInt(), anyInt());
    }

    @Test
    @DisplayName("Should successfully set default address when owned by user")
    void testSetDefaultAddressSuccess() {
        Address addr = new Address();
        addr.setAddressId(20);
        addr.setUserId(5);

        when(addressDAO.findById(20)).thenReturn(Optional.of(addr));

        addressService.setDefaultAddress(20, 5);

        verify(addressDAO, times(1)).setDefaultAddress(20, 5);
    }
}
