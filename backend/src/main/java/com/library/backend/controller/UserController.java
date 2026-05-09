package com.library.backend.controller;

import com.library.backend.entity.User;
import com.library.backend.mapper.UserMapper;
import com.library.backend.service.UserService;
import com.library.backend.utils.Result;
import com.library.backend.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/profile")
    public Result<User> getProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userService.getById(userId);
        if (user != null) {
            user.setPassword(null); // Don't return password
            user.setRoles(userMapper.getRoleCodesByUserId(userId));
        }
        return Result.success(user);
    }

    @PutMapping("/profile")
    public Result<Boolean> updateProfile(@RequestBody User user) {
        Long userId = SecurityUtils.getCurrentUserId();
        user.setId(userId); // Ensure updating current user
        user.setPassword(null); // Prevent password update via this endpoint
        user.setUsername(null); // Prevent username update
        user.setStatus(null); // Prevent status update
        return Result.success(userService.updateById(user));
    }

    @PostMapping("/password")
    public Result<String> changePassword(@RequestBody Map<String, String> params) {
        Long userId = SecurityUtils.getCurrentUserId();
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        return userService.changePassword(userId, oldPassword, newPassword);
    }
}
