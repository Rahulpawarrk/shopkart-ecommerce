package com.example.ecommerce.customer.service;

import com.example.ecommerce.customer.dao.AddressDAO;
import com.example.ecommerce.customer.model.Address;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.exception.ValidationException;
import com.example.ecommerce.util.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service Layer orchestrating customer shipping and billing addresses,
 * validation, ownership enforcement, and default address promotion.
 */
public class AddressService {

    private static final Logger logger = LoggerFactory.getLogger(AddressService.class);
    private final AddressDAO addressDAO;

    public AddressService() {
        this.addressDAO = new AddressDAO();
    }

    public AddressService(AddressDAO addressDAO) {
        this.addressDAO = addressDAO;
    }

    public List<Address> getUserAddresses(int userId) {
        return addressDAO.findByUserId(userId);
    }

    public Address getAddressById(int addressId, int userId) {
        Address address = addressDAO.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + addressId));
        
        if (address.getUserId() != userId) {
            logger.warn("Security Alert: User {} attempted to access address {} belonging to User {}", userId, addressId, address.getUserId());
            throw new ValidationException("You are not authorized to view this address.");
        }
        return address;
    }

    public Optional<Address> getDefaultAddress(int userId) {
        return addressDAO.findDefaultByUserId(userId);
    }

    /**
     * Saves a new address. If it's the customer's first address, sets it as default automatically.
     */
    public Address saveAddress(Address address) {
        validateAddress(address);

        int count = addressDAO.countByUserId(address.getUserId());
        if (count == 0) {
            address.setDefaultAddress(true);
        }

        int addressId = addressDAO.createAddress(address);
        address.setAddressId(addressId);

        if (address.isDefaultAddress()) {
            addressDAO.setDefaultAddress(addressId, address.getUserId());
        }

        logger.info("Saved new address [id={}] for userId [{}]", addressId, address.getUserId());
        return address;
    }

    /**
     * Updates an existing address.
     */
    public void updateAddress(Address address) {
        if (address.getAddressId() <= 0) {
            throw new ValidationException("Valid Address ID is required for update.");
        }
        validateAddress(address);

        // Verify ownership
        getAddressById(address.getAddressId(), address.getUserId());

        addressDAO.updateAddress(address);

        if (address.isDefaultAddress()) {
            addressDAO.setDefaultAddress(address.getAddressId(), address.getUserId());
        }

        logger.info("Updated address [id={}] for userId [{}]", address.getAddressId(), address.getUserId());
    }

    /**
     * Deletes an address. If the deleted address was the default, promotes the most recent remaining address to default.
     */
    public void deleteAddress(int addressId, int userId) {
        Address address = getAddressById(addressId, userId);
        boolean wasDefault = address.isDefaultAddress();

        addressDAO.deleteAddress(addressId, userId);
        logger.info("Deleted address [id={}] for userId [{}]", addressId, userId);

        if (wasDefault) {
            List<Address> remaining = addressDAO.findByUserId(userId);
            if (!remaining.isEmpty()) {
                Address promote = remaining.get(0);
                addressDAO.setDefaultAddress(promote.getAddressId(), userId);
                logger.info("Auto-promoted address [id={}] to default for userId [{}]", promote.getAddressId(), userId);
            }
        }
    }

    /**
     * Sets a specific address as the customer's default address.
     */
    public void setDefaultAddress(int addressId, int userId) {
        getAddressById(addressId, userId); // verify ownership
        addressDAO.setDefaultAddress(addressId, userId);
        logger.info("Set address [id={}] as default for userId [{}]", addressId, userId);
    }

    private void validateAddress(Address address) {
        List<String> errors = new ArrayList<>();

        if (address.getFullName() == null || address.getFullName().trim().isEmpty()) {
            errors.add("Full recipient name is required.");
        } else if (!ValidationUtils.isValidPersonName(address.getFullName())) {
            errors.add("Please enter a valid recipient name (letters and spaces only, 2 to 50 characters).");
        } else {
            address.setFullName(address.getFullName().trim());
        }

        if (address.getPhone() == null || address.getPhone().trim().isEmpty()) {
            errors.add("Contact phone number is required.");
        } else if (!ValidationUtils.isValidPhone(address.getPhone())) {
            errors.add("Please enter a valid 10-digit mobile number (e.g. 9876543210).");
        } else {
            address.setPhone(ValidationUtils.normalizePhone(address.getPhone()));
        }

        if (address.getAddressLine1() == null || address.getAddressLine1().trim().isEmpty()) {
            errors.add("Street address line 1 is required.");
        } else if (!ValidationUtils.isValidAddressLine1(address.getAddressLine1())) {
            errors.add("Address Line 1 must be between 5 and 255 characters with valid street/building details.");
        } else {
            address.setAddressLine1(address.getAddressLine1().trim());
        }

        if (address.getAddressLine2() != null) {
            address.setAddressLine2(address.getAddressLine2().trim());
            if (address.getAddressLine2().length() > 255) {
                errors.add("Address Line 2 cannot exceed 255 characters.");
            }
        }

        if (address.getCity() == null || address.getCity().trim().isEmpty()) {
            errors.add("City is required.");
        } else if (!ValidationUtils.isValidCityOrState(address.getCity())) {
            errors.add("Please enter a valid City name (letters and spaces only, no numbers).");
        } else {
            address.setCity(address.getCity().trim());
        }

        if (address.getState() == null || address.getState().trim().isEmpty()) {
            errors.add("State / Province is required.");
        } else if (!ValidationUtils.isValidCityOrState(address.getState())) {
            errors.add("Please enter a valid State name (letters and spaces only, no numbers).");
        } else {
            address.setState(address.getState().trim());
        }

        if (address.getPostalCode() == null || address.getPostalCode().trim().isEmpty()) {
            errors.add("Postal PIN code is required.");
        } else if (!ValidationUtils.isValidPincode(address.getPostalCode())) {
            errors.add("Postal PIN code must be exactly 6 digits.");
        } else {
            address.setPostalCode(address.getPostalCode().trim());
        }

        if (address.getCountry() != null && !address.getCountry().trim().isEmpty()) {
            if (!ValidationUtils.isValidCityOrState(address.getCountry())) {
                errors.add("Please enter a valid Country name.");
            } else {
                address.setCountry(address.getCountry().trim());
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}
