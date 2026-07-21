package com.qingyuan.water.service;

import com.qingyuan.water.config.JwtUtil;
import com.qingyuan.water.dto.LoginRequest;
import com.qingyuan.water.dto.RegisterRequest;
import com.qingyuan.water.entity.User;
import com.qingyuan.water.mapper.UserMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthService(UserMapper userMapper, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    public Map<String, Object> login(LoginRequest request) {
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null || !encoder.matches(request.getPassword(), user.getPassword())) throw new IllegalArgumentException("账号或密码错误");
        Map<String, Object> safeUser = new HashMap<String, Object>();
        safeUser.put("id", user.getId());
        safeUser.put("username", user.getUsername());
        safeUser.put("realName", user.getRealName());
        safeUser.put("role", user.getRole());
        safeUser.put("phone", user.getPhone());
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("token", jwtUtil.createToken(user.getId(), user.getUsername(), user.getRealName(), user.getRole()));
        result.put("user", safeUser);
        return result;
    }

    public void register(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(encoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setRole("OPERATOR");
        userMapper.insert(user);
    }
}
