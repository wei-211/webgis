package com.example.webgis.service;

import com.example.webgis.entity.User;
import com.example.webgis.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // 加密器
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * 登录
     */
    public User login(String username, String password) {

        User user = userRepository.findByUsername(username);

        // 关键：使用 matches 比对
        if (user != null && encoder.matches(password, user.getPassword())) {
            return user;
        }

        return null;
    }

    /**
     * 注册
     */
    public User register(User user) {

        // 判断是否存在
        User exist = userRepository.findByUsername(user.getUsername());
        if (exist != null) {
            return null;
        }

        // 🔥 核心：密码加密
        String encodedPwd = encoder.encode(user.getPassword());
        user.setPassword(encodedPwd);

        return userRepository.save(user);
    }
}