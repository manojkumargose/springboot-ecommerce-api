package com.example.authservice.repository;

import com.example.authservice.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserId(Long userId);
    void deleteByIdAndUserId(Long id, Long userId);
}
