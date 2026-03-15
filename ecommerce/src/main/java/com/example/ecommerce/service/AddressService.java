package com.example.ecommerce.service;

import com.example.ecommerce.dto.AddressRequest;
import com.example.ecommerce.dto.AddressResponse;
import com.example.ecommerce.entity.Address;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.AddressRepository;
import com.example.ecommerce.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public AddressService(AddressRepository addressRepository,
                          UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    // ─── Get logged in user ───────────────────────────────────

    private User getLoggedInUser() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    // ─── Add Address ──────────────────────────────────────────

    @Transactional
    public AddressResponse addAddress(AddressRequest request) {
        User user = getLoggedInUser();

        // ── If this is first address set as default ───────────
        boolean isFirst = addressRepository.countByUserId(user.getId()) == 0;

        // ── If new address is default remove old default ──────
        if (request.getIsDefault() || isFirst) {
            addressRepository.findByUserIdAndIsDefaultTrue(user.getId())
                    .ifPresent(existing -> {
                        existing.setIsDefault(false);
                        addressRepository.save(existing);
                    });
        }

        Address address = new Address();
        address.setUser(user);
        address.setFullName(request.getFullName());
        address.setPhone(request.getPhone());
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPincode(request.getPincode());
        address.setCountry(request.getCountry() != null ? request.getCountry() : "India");
        address.setIsDefault(request.getIsDefault() || isFirst);

        return mapToResponse(addressRepository.save(address));
    }

    // ─── Get My Addresses ─────────────────────────────────────

    public List<AddressResponse> getMyAddresses() {
        User user = getLoggedInUser();
        return addressRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── Update Address ───────────────────────────────────────

    @Transactional
    public AddressResponse updateAddress(Long addressId, AddressRequest request) {
        User user = getLoggedInUser();

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Address not found: " + addressId));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You are not authorized to update this address");
        }

        // ── If setting as default remove old default ──────────
        if (request.getIsDefault()) {
            addressRepository.findByUserIdAndIsDefaultTrue(user.getId())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(addressId)) {
                            existing.setIsDefault(false);
                            addressRepository.save(existing);
                        }
                    });
        }

        address.setFullName(request.getFullName());
        address.setPhone(request.getPhone());
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPincode(request.getPincode());
        address.setCountry(request.getCountry() != null ? request.getCountry() : "India");
        address.setIsDefault(request.getIsDefault());

        return mapToResponse(addressRepository.save(address));
    }

    // ─── Delete Address ───────────────────────────────────────

    @Transactional
    public void deleteAddress(Long addressId) {
        User user = getLoggedInUser();

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Address not found: " + addressId));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You are not authorized to delete this address");
        }

        addressRepository.delete(address);
    }

    // ─── Set Default Address ──────────────────────────────────

    @Transactional
    public AddressResponse setDefaultAddress(Long addressId) {
        User user = getLoggedInUser();

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Address not found: " + addressId));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You are not authorized to update this address");
        }

        // ── Remove old default ────────────────────────────────
        addressRepository.findByUserIdAndIsDefaultTrue(user.getId())
                .ifPresent(existing -> {
                    existing.setIsDefault(false);
                    addressRepository.save(existing);
                });

        address.setIsDefault(true);
        return mapToResponse(addressRepository.save(address));
    }

    // ─── Map to Response ──────────────────────────────────────

    public AddressResponse mapToResponse(Address address) {
        AddressResponse response = new AddressResponse();
        response.setId(address.getId());
        response.setFullName(address.getFullName());
        response.setPhone(address.getPhone());
        response.setAddressLine1(address.getAddressLine1());
        response.setAddressLine2(address.getAddressLine2());
        response.setCity(address.getCity());
        response.setState(address.getState());
        response.setPincode(address.getPincode());
        response.setCountry(address.getCountry());
        response.setIsDefault(address.getIsDefault());
        return response;
    }
}