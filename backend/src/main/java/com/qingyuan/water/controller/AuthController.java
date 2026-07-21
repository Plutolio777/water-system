package com.qingyuan.water.controller;

import com.qingyuan.water.common.ApiResponse;
import com.qingyuan.water.dto.LoginRequest;
import com.qingyuan.water.dto.RegisterRequest;
import com.qingyuan.water.service.AuthService;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) { return ApiResponse.ok(authService.login(request)); }

    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) { authService.register(request); return ApiResponse.ok("注册成功", null); }
}
