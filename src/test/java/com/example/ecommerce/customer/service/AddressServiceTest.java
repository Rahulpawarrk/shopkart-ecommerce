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
