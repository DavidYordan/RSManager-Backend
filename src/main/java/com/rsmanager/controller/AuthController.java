package com.rsmanager.controller;

import com.rsmanager.dto.api.ApiResponseDTO;
import com.rsmanager.dto.login.LoginRequest;
import com.rsmanager.dto.login.LoginResponse;
import com.rsmanager.service.AuthService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticateUser(@RequestBody LoginRequest loginRequest, HttpSession session) {
        LoginResponse response = authService.authenticateUser(loginRequest, session);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/captcha")
    public ResponseEntity<byte[]> getCaptcha(HttpSession session) {
        byte[] imageBytes = authService.getCaptcha(session);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponseDTO<Void>> logout(HttpSession session) {
        authService.logout(session);
        ApiResponseDTO<Void> response = ApiResponseDTO.<Void>builder()
                .success(true)
                .message("Logout successful")
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
