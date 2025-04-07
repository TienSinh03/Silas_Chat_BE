/**
 * @ (#) GlobalExceptionHandler.java      2/16/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import vn.edu.iuh.fit.dtos.response.ApiResponse;

import java.util.LinkedHashMap;
import java.util.Map;

/*
 * @description:
 * @author: Sinh Phan Tien
 * @date: 2/16/2025
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> globalExceptionHandler(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        ApiResponse.builder()
                                .status("FAILED")
                                .message(ex.getMessage())
                                .build()
                );
    }



}
