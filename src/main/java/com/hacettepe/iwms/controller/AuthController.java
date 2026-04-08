package com.hacettepe.iwms.controller;

import com.hacettepe.iwms.dto.ApiResponse;
import com.hacettepe.iwms.dto.AuthRequest;
import com.hacettepe.iwms.dto.AuthResponse;
import com.hacettepe.iwms.dto.PasswordResetRequest;
import com.hacettepe.iwms.dto.PasswordUpdateRequest;
import com.hacettepe.iwms.dto.RegisterRequest;
import com.hacettepe.iwms.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return new ResponseEntity<>(new ApiResponse<>(true, "User registered successfully!", null), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Login successful!", authResponse));
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<ApiResponse<String>> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        String token = authService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok(new ApiResponse<>(true, "Password reset token generated.", token));
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<ApiResponse<String>> confirmPasswordReset(@Valid @RequestBody PasswordUpdateRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Password updated successfully.", null));
    }
}
