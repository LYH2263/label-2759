package com.library.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.library.backend.entity.User;
import com.library.backend.utils.Result;

public interface UserService extends IService<User> {
    Result<String> login(String username, String password);
    Result<String> changePassword(Long userId, String oldPassword, String newPassword);
}
