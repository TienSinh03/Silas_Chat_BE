/*
 * @ {#} AuthController.java   1.0     16/03/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */
      
package vn.edu.iuh.fit.controllers;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.dtos.request.SignInRequest;
import vn.edu.iuh.fit.dtos.request.SignUpRequest;
import vn.edu.iuh.fit.dtos.response.ApiResponse;
import vn.edu.iuh.fit.dtos.response.RefreshTokenResponse;
import vn.edu.iuh.fit.dtos.response.SignInResponse;
import vn.edu.iuh.fit.services.AuthService;
import vn.edu.iuh.fit.services.UserService;

import java.util.Map;

/*
 * @description: 
 * @author: Tran Hien Vinh
 * @date:   16/03/2025
 * @version:    1.0
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @Autowired
    private AuthService authService;
    @Autowired
    private UserService userService;

//    Request body:
/*
    {
            "firstName":"Anh Dat",
            "lastName":"Le",
            "phone":"+840862435435",
            "password":"12345678",
            "roles":[
             ]
    }
*/
    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponse<?>> signUp(@RequestBody @Valid SignUpRequest signUpRequest) {
        try {
            if (authService.signUp(signUpRequest)) {
                return ResponseEntity.ok(ApiResponse.builder()
                        .status("SUCCESS")
                        .response(null)
                        .message("Sign up successfully!")
                        .build());
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder()
                    .status("ERROR")
                    .response(null)
                    .message(e.getMessage())
                    .build());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.builder()
                .status("ERROR")
                .response(null)
                .message("Sign up failed!")
                .build());
    }

//    Request body:
/*
    {
        "phone":"phoneNumber",
        "password":"password"
    }
*/
    @PostMapping("/sign-in")
    public ResponseEntity<ApiResponse<?>> signIn(@RequestBody @Valid SignInRequest signInRequest) {
        try {
            SignInResponse signInResponse = authService.signIn(signInRequest);
            return ResponseEntity.ok(ApiResponse.builder()
                    .status("SUCCESS")
                    .response(signInResponse)
                    .message("Sign in successfully!")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.builder()
                    .status("ERROR")
                    .response(null)
                    .message(e.getMessage())
                    .build());
        }
    }

// Authorization: Bearer Token <access_token>
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout(@RequestHeader("Authorization") String accessToken) {
        try {
            authService.logout(accessToken);
            return ResponseEntity.ok(ApiResponse.builder()
                    .status("SUCCESS")
                    .response(null)
                    .message("Logout successfully!")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.builder()
                    .status("ERROR")
                    .response(null)
                    .message(e.getMessage())
                    .build());
        }
    }

// Request body:
/*
        {
            "refreshToken": <refresh_token>
        }
*/
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<?>> refreshToken(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");
            if (refreshToken == null || refreshToken.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.builder()
                                .status("ERROR")
                                .response(null)
                                .message("Missing Refresh Token in request!")
                                .build());
            }
            RefreshTokenResponse response = authService.refreshToken(refreshToken);
            return ResponseEntity.ok(ApiResponse.builder()
                    .status("SUCCESS")
                    .response(response)
                    .message("Token refreshed successfully!")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.builder()
                    .status("ERROR")
                    .response(null)
                    .message(e.getMessage())
                    .build());
        }
    }

// Request body:
/*
        {
            "idToken": <id_token>
        }
 */
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<?>> verifyOtp(@RequestBody Map<String, String> request) {
        try {
            String idToken = request.get("idToken");

            if (idToken == null || idToken.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.builder()
                        .status("ERROR")
                        .message("Thiếu ID Token trong request!")
                        .build());
            }

            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);

            String phoneNumber = decodedToken.getClaims().get("phone_number").toString();

            // Kiểm tra xem số điện thoại đã tồn tại chưa
            if (userService.existsByPhone(phoneNumber)) {
                return ResponseEntity.badRequest().body(ApiResponse.builder()
                        .status("ERROR")
                        .message("Số điện thoại đã được sử dụng!")
                        .build());
            }

            return ResponseEntity.ok(ApiResponse.builder()
                    .status("SUCCESS")
                    .message("Xác thực thành công! Số điện thoại: " + phoneNumber)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .status("ERROR")
                    .message("Xác thực OTP thất bại: " + e.getMessage())
                    .build());
        }
    }

}
