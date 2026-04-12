package com.example.authservice.service;

import com.example.authservice.dto.AddressRequest;
import com.example.authservice.dto.AddressResponse;
import com.example.authservice.entity.Address;
import com.example.authservice.entity.User;
import com.example.authservice.repository.AddressRepository;
import com.example.authservice.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public AddressService(AddressRepository addressRepository, UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    public AddressResponse addAddress(Long userId, AddressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Address address = new Address();
        address.setUser(user);
        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPincode(request.getPincode());
        address.setCountry(request.getCountry());
        address.setIsDefault(request.getIsDefault());
        address = addressRepository.save(address);
        return mapToResponse(address);
    }

    public List<AddressResponse> getAddresses(Long userId) {
        return addressRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void deleteAddress(Long addressId, Long userId) {
        addressRepository.deleteByIdAndUserId(addressId, userId);
    }

    private AddressResponse mapToResponse(Address a) {
        return new AddressResponse(a.getId(), a.getStreet(), a.getCity(),
                a.getState(), a.getPincode(), a.getCountry(), a.getIsDefault());
    }
}
