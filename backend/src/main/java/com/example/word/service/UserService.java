package com.example.word.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.word.entity.User;

public interface UserService extends IService<User> {
    User login(User user);
    void register(User user);
    boolean updatePassword(Long userId, String oldPassword, String newPassword);
    boolean updateUsername(Long userId, String newUsername, String password);

    // 【新增】更新头像接口
    boolean updateAvatar(Long userId, String avatarBase64);
}