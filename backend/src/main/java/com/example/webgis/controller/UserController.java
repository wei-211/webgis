package com.example.webgis.controller;

import com.example.webgis.entity.User;
import com.example.webgis.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody User user) {

        Map<String, Object> result = new HashMap<>();

        User loginUser = userService.login(user.getUsername(), user.getPassword());

        if (loginUser != null) {
            result.put("code", 200);
            result.put("msg", "登录成功");
            result.put("data", loginUser);
        } else {
            result.put("code", 500);
            result.put("msg", "用户名或密码错误");
        }

        return result;
    }
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {

        Map<String, Object> result = new HashMap<>();

        User newUser = userService.register(user);

        if (newUser != null) {
            result.put("code", 200);
            result.put("msg", "注册成功");
        } else {
            result.put("code", 500);
            result.put("msg", "用户名已存在");
        }

        return result;
    }
}
