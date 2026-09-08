package com.example.ecommerce.api.controller;

import com.example.ecommerce.api.dto.*;
import com.example.ecommerce.auth.model.User;
import com.example.ecommerce.auth.model.UserSession;
import com.example.ecommerce.auth.service.AuthService;
import com.example.ecommerce.customer.model.Address;
import com.example.ecommerce.customer.service.AddressService;
import com.example.ecommerce.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST API for Customer Profile management and Address Book CRUD.
 */
@RestController
@RequestMapping("/api/customer")
public class CustomerRestController {

    private final AddressService addressService;
    private final AuthService authService;

    @Autowired
    public CustomerRestController(AddressService addressService, AuthService authService) {
        this.addressService = addressService;
        this.authService = authService;
    }

    private UserSession getAuthenticatedUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (session != null) ? (UserSession) session.getAttribute("currentUser") : null;
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserDto>> getProfile(HttpServletRequest request) {
        UserSession userSession = getAuthenticatedUser(request);
        if (userSession == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Please log in", "UNAUTHORIZED"));
        }

        User user = authService.getUserProfile(userSession.getUserId());
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }

        return ResponseEntity.ok(ApiResponse.ok(UserDto.fromSession(UserSession.fromUser(user))));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserDto>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest req,
            HttpServletRequest request
    ) {
        UserSession userSession = getAuthenticatedUser(request);
        if (userSession == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Please log in", "UNAUTHORIZED"));
        }

        authService.updateProfile(
                userSession.getUserId(),
                req.getFirstName().trim(),
                req.getLastName().trim(),
                req.getPhone() != null ? req.getPhone().trim() : ""
        );

        User updatedUser = authService.getUserProfile(userSession.getUserId());
        UserSession updatedSession = UserSession.fromUser(updatedUser);

        HttpSession session = request.getSession(true);
        session.setAttribute("currentUser", updatedSession);

        return ResponseEntity.ok(ApiResponse.ok("Profile updated successfully", UserDto.fromSession(updatedSession)));
    }

    @GetMapping("/addresses")
    public ResponseEntity<ApiResponse<List<AddressDto>>> getAddresses(HttpServletRequest request) {
        UserSession user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Please log in", "UNAUTHORIZED"));
        }

        List<Address> addresses = addressService.getUserAddresses(user.getUserId());
        List<AddressDto> dtos = addresses.stream().map(AddressDto::fromEntity).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(dtos));
    }

    @PostMapping("/addresses")
    public ResponseEntity<ApiResponse<AddressDto>> addAddress(
            @Valid @RequestBody AddressRequest req,
            HttpServletRequest request
    ) {
        UserSession user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Please log in", "UNAUTHORIZED"));
        }

        Address address = new Address();
        address.setUserId(user.getUserId());
        address.setAddressType(req.getAddressType() != null ? req.getAddressType() : "SHIPPING");
        address.setFullName(req.getFullName().trim());
        address.setPhone(req.getPhone().trim());
        address.setAddressLine1(req.getAddressLine1().trim());
        address.setAddressLine2(req.getAddressLine2() != null ? req.getAddressLine2().trim() : "");
        address.setCity(req.getCity().trim());
        address.setState(req.getState().trim());
        address.setPostalCode(req.getPostalCode().trim());
        address.setCountry(req.getCountry() != null ? req.getCountry().trim() : "India");
        address.setDefaultAddress(req.isDefaultAddress());

        Address created = addressService.saveAddress(address);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Address added successfully", AddressDto.fromEntity(created)));
    }

    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<ApiResponse<AddressDto>> updateAddress(
            @PathVariable int addressId,
            @Valid @RequestBody AddressRequest req,
            HttpServletRequest request
    ) {
        UserSession user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Please log in", "UNAUTHORIZED"));
        }

        Address existing = addressService.getAddressById(addressId, user.getUserId());
        if (existing == null) {
            throw new ResourceNotFoundException("Address not found: " + addressId);
        }

        existing.setAddressType(req.getAddressType() != null ? req.getAddressType() : "SHIPPING");
        existing.setFullName(req.getFullName().trim());
        existing.setPhone(req.getPhone().trim());
        existing.setAddressLine1(req.getAddressLine1().trim());
        existing.setAddressLine2(req.getAddressLine2() != null ? req.getAddressLine2().trim() : "");
        existing.setCity(req.getCity().trim());
        existing.setState(req.getState().trim());
        existing.setPostalCode(req.getPostalCode().trim());
        existing.setCountry(req.getCountry() != null ? req.getCountry().trim() : "India");
        existing.setDefaultAddress(req.isDefaultAddress());

        addressService.updateAddress(existing);
        return ResponseEntity.ok(ApiResponse.ok("Address updated successfully", AddressDto.fromEntity(existing)));
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @PathVariable int addressId,
            HttpServletRequest request
    ) {
        UserSession user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Please log in", "UNAUTHORIZED"));
        }

        addressService.deleteAddress(addressId, user.getUserId());
        return ResponseEntity.ok(ApiResponse.ok("Address deleted successfully", null));
    }

    @PutMapping("/addresses/{addressId}/default")
    public ResponseEntity<ApiResponse<Void>> setDefaultAddress(
            @PathVariable int addressId,
            HttpServletRequest request
    ) {
        UserSession user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Please log in", "UNAUTHORIZED"));
        }

        addressService.setDefaultAddress(addressId, user.getUserId());
        return ResponseEntity.ok(ApiResponse.ok("Default address updated", null));
    }
}
