package com.example.word.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.word.entity.User;
import com.example.word.mapper.UserMapper;
import com.example.word.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User login(User user) {
        if (user == null || user.getUsername() == null || user.getUsername().trim().isEmpty()
                || user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new RuntimeException("请输入用户名和密码");
        }
        String password = user.getPassword();
        String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, user.getUsername());
        User one = getOne(queryWrapper);

        if (one == null) {
            throw new RuntimeException("用户名不存在");
        }
        if (!one.getPassword().equals(md5Password)) {
            throw new RuntimeException("密码错误");
        }
        return one;
    }

    @Override
    public void register(User user) {
        if (user == null || user.getUsername() == null || user.getUsername().trim().isEmpty()
                || user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new RuntimeException("请填写注册信息");
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, user.getUsername());
        User one = getOne(queryWrapper);
        if (one != null) {
            throw new RuntimeException("用户名已存在");
        }
        user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        // 注册时设置默认空头像
        user.setAvatar("");
        save(user);
    }

    @Override
    public boolean updatePassword(Long userId, String oldPassword, String newPassword) {
        if (oldPassword == null || oldPassword.isEmpty() || newPassword == null || newPassword.isEmpty()) {
            throw new RuntimeException("密码不能为空");
        }
        User user = getById(userId);
        if (user == null) return false;

        String oldMd5 = DigestUtils.md5DigestAsHex(oldPassword.getBytes());
        if (!user.getPassword().equals(oldMd5)) {
            throw new RuntimeException("原密码错误");
        }

        String newMd5 = DigestUtils.md5DigestAsHex(newPassword.getBytes());
        user.setPassword(newMd5);
        return updateById(user);
    }

    @Override
    public boolean updateUsername(Long userId, String newUsername, String password) {
        if (password == null || password.isEmpty()) {
            throw new RuntimeException("密码不能为空");
        }
        User user = getById(userId);
        if (user == null) return false;

        String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!user.getPassword().equals(md5Password)) {
            throw new RuntimeException("密码错误，无法修改用户名");
        }

        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query.eq(User::getUsername, newUsername);
        User existUser = getOne(query);

        if (existUser != null && !existUser.getId().equals(userId)) {
            throw new RuntimeException("该用户名已存在，请更换");
        }

        user.setUsername(newUsername);
        return updateById(user);
    }

    // 【新增】更新头像实现
    @Override
    public boolean updateAvatar(Long userId, String avatarBase64) {
        // 使用 LambdaUpdateWrapper 精准更新，避免 null 被忽略的问题
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, userId)
                     .set(User::getAvatar, avatarBase64 == null ? "" : avatarBase64);
        return update(updateWrapper);
    }
}
