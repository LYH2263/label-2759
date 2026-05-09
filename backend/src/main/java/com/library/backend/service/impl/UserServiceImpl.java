package com.library.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.backend.entity.User;
import com.library.backend.mapper.UserMapper;
import com.library.backend.service.UserService;
import com.library.backend.utils.JwtUtils;
import com.library.backend.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public Result<String> login(String username, String password) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        User user = baseMapper.selectOne(queryWrapper);
        
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        String dbPass = user.getPassword();
        boolean match = checkPassword(password, dbPass);

        if (!match) {
            return Result.error("密码错误");
        }
        
        if (user.getStatus() == 0) {
            return Result.error("账号已被禁用");
        }

        String token = jwtUtils.generateToken(user.getUsername(), user.getId());
        return Result.success(token);
    }

    @Override
    public Result<String> changePassword(Long userId, String oldPassword, String newPassword) {
        User user = baseMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        if (!checkPassword(oldPassword, user.getPassword())) {
            return Result.error("旧密码错误");
        }

        // For simplicity, using plain text or {noop} prefix as per existing logic
        // In production, should use BCrypt
        user.setPassword("{noop}" + newPassword);
        baseMapper.updateById(user);
        
        return Result.success("密码修改成功");
    }

    private boolean checkPassword(String inputPass, String dbPass) {
        if (dbPass.startsWith("{noop}")) {
            return dbPass.substring(6).equals(inputPass);
        } else {
            return dbPass.equals(inputPass);
        }
    }
}
