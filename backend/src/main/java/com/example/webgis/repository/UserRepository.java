package com.example.webgis.repository;

import com.example.webgis.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {

    // 根据用户名查询
    User findByUsername(String username);
}
