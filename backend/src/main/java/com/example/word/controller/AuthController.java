package com.example.word.controller;

import com.example.word.common.BaseContext;
import com.example.word.common.R;
import com.example.word.entity.User;
import com.example.word.service.UserService;
import com.example.word.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody User user) {
        try {
            User loginUser = userService.login(user);
            Map<String, Object> map = new HashMap<>();
            String token = JwtUtil.createToken(loginUser.getId(), loginUser.getUsername());
            map.put("token", token);
            map.put("username", loginUser.getUsername());
            map.put("avatar", loginUser.getAvatar());
            map.put("id", loginUser.getId());
            return R.ok(map);
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @PostMapping("/register")
    public R<String> register(@RequestBody User user) {
        try {
            userService.register(user);
            return R.ok("注册成功");
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @GetMapping("/info")
    public R<User> getUserInfo() {
        Long userId = BaseContext.getCurrentId();
        if (userId == null)
            return R.error("未登录");
        User user = userService.getById(userId);
        if (user != null) {
            user.setPassword(null);
            return R.ok(user);
        }
        return R.error("用户不存在");
    }

    @PutMapping("/avatar")
    public R<String> updateAvatar(@RequestBody Map<String, String> params) {
        String avatar = params.get("avatar");
        if (avatar == null)
            return R.error("头像数据为空");
        try {
            userService.updateAvatar(BaseContext.getCurrentId(), avatar);
            return R.ok("头像更新成功");
        } catch (Exception e) {
            return R.error("更新失败");
        }
    }

    @DeleteMapping("/avatar")
    public R<String> resetAvatar() {
        try {
            userService.updateAvatar(BaseContext.getCurrentId(), null);
            return R.ok("头像已重置");
        } catch (Exception e) {
            return R.error("重置失败");
        }
    }

    @PutMapping("/username")
    public R<String> updateUsername(@RequestBody Map<String, String> params) {
        String newUsername = params.get("username");
        String password = params.get("password");

        if (newUsername == null || newUsername.trim().isEmpty())
            return R.error("用户名不能为空");

        try {
            userService.updateUsername(BaseContext.getCurrentId(), newUsername, password);
            return R.ok("用户名修改成功");
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @PutMapping("/password")
    public R<String> updatePassword(@RequestBody Map<String, String> params) {
        String oldPwd = params.get("oldPassword");
        String newPwd = params.get("newPassword");
        try {
            userService.updatePassword(BaseContext.getCurrentId(), oldPwd, newPwd);
            return R.ok("密码修改成功");
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }
}