/**
 * @ (#) UserController.java      4/9/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.dtos.request.ChangePasswordRequest;
import vn.edu.iuh.fit.dtos.request.SignUpRequest;
import vn.edu.iuh.fit.dtos.request.UpdateUserRequest;
import vn.edu.iuh.fit.dtos.response.ApiResponse;
import vn.edu.iuh.fit.dtos.response.UserResponse;
import vn.edu.iuh.fit.services.CloudinaryService;
import vn.edu.iuh.fit.services.UserService;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private Validator validator;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<?>> getCurrentUser(@RequestHeader("Authorization") String token) {
        try {
            UserResponse response = userService.getCurrentUser(token);

            simpMessagingTemplate.convertAndSend("/user/profile/" + response.getId(), response);

            return ResponseEntity.ok(ApiResponse.builder().status("SUCCESS").message("Get current user").response(response).build());
        } catch (Exception e) {
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .status("FAILED")
                            .message(e.getMessage())
                            .build());
        }
    }

    @PutMapping(value = "/me/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> updateUser(
            @RequestPart("request") String reqJson,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar ,
            @RequestHeader("Authorization") String token)
    {
        System.out.println("Request update user: " + reqJson);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        UpdateUserRequest requestUpdate = null;
        try {
            requestUpdate = objectMapper.readValue(reqJson, UpdateUserRequest.class);
            String avatarUrl = null;
            if(avatar != null && !avatar.isEmpty()) {
                avatarUrl = cloudinaryService.uploadImage(avatar);
                requestUpdate.setAvatar(avatarUrl);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder()
                            .status("FAILED")
                            .message("Invalid request format")
                            .build());
        }

        // Validate the request
        Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(requestUpdate);
        if(!violations.isEmpty()) {
            Map<String, Object> errors = new HashMap<>();
            violations.forEach(violation -> {
                errors.put(violation.getPropertyPath().toString(), violation.getMessage());
            });
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .status("ERROR")
                    .message("Validation failed")
                    .response(errors)
                    .build());
        }

        try {
            System.out.println("Request update: " + requestUpdate.toString() + " token: " + token);
            UserResponse user = userService.getCurrentUser(token);
            UserResponse userUpdate = userService.updateUser(user.getId(), requestUpdate);

            // Gui tin nhan den WebSocket
            System.out.println("Sending to destination: /user/profile/" + user.getId());
            System.out.println("Data: " + userUpdate);
            simpMessagingTemplate.convertAndSend("/user/profile/" + user.getId(), userUpdate);

            return ResponseEntity.ok(ApiResponse.builder().status("SUCCESS").message("Update user successfully").response(userUpdate).build());
        } catch (Exception e) {
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .status("FAILED")
                            .message(e.getMessage())
                            .build());
        }
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<?>> changePassword(@RequestBody ChangePasswordRequest request, @RequestHeader("Authorization") String token) {
        try {
            UserResponse currentUser = userService.getCurrentUser(token);
            UserResponse updated = userService.changePassword(currentUser.getId(), request.getOldPassword(), request.getNewPassword());
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .status("SUCCESS")
                            .message("Change password successfully")
                            .response(updated)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder()
                            .status("FAILED")
                            .message(e.getMessage())
                            .build()
                    );
        }
    }

}
