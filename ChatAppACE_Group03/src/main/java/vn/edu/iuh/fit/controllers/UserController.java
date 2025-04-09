/**
 * @ (#) UserController.java      4/9/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.dtos.request.UpdateUserRequest;
import vn.edu.iuh.fit.dtos.response.ApiResponse;
import vn.edu.iuh.fit.dtos.response.UserResponse;
import vn.edu.iuh.fit.services.UserService;

import java.util.Map;

/*
 * @description:
 * @author: Sinh Phan Tien
 * @date: 4/9/2025
 */
@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<?>> getCurrentUser(@RequestHeader("Authorization") String token) {
        try {
            UserResponse response = userService.getCurrentUser(token);
            return ResponseEntity.ok(ApiResponse.builder().status("SUCCESS").message("Get current user").response(response).build());
        } catch (Exception e) {
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .status("FAILED")
                            .message(e.getMessage())
                            .build());
        }
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<?>> updateUser(@RequestBody UpdateUserRequest req, @RequestHeader("Authorization") String token) {
        try {
            UserResponse user = userService.getCurrentUser(token);
            UserResponse userUpdate = userService.updateUser(user.getId(), req);
            return ResponseEntity.ok(ApiResponse.builder().status("SUCCESS").message("Update user successfully").response(userUpdate).build());
        } catch (Exception e) {
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .status("FAILED")
                            .message(e.getMessage())
                            .build());
        }

    }
}
