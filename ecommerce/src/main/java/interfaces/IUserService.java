package com.example.ecommerce.service.interfaces;

import com.example.ecommerce.dto.UserResponse;
import java.util.List;

public interface IUserService {
    UserResponse getUserById(Long id);
    List<UserResponse> getAllUsers();
    void deleteUser(Long id);
}