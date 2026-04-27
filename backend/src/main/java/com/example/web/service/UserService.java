package com.example.web.service;

import com.example.web.entity.User;

import java.util.List;

public interface UserService {
    User register(String username, String password, String nickname, String email);
    String login(String username, String password);
    User getUserById(Long id);
    User updateUser(User user);

    void changePassword(Long userId, String oldPassword, String newPassword);

    // Admin methods
    List<User> getAllUsers();
    void deleteUserById(Long id);
    void initAdminAccount();
}
