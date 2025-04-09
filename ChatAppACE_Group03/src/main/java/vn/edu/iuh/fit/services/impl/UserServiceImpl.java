/*
 * @ {#} UserServiceImpl.java   1.0     16/03/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.dtos.response.UserResponse;
import vn.edu.iuh.fit.entities.User;
import vn.edu.iuh.fit.exceptions.InvalidPasswordException;
import vn.edu.iuh.fit.exceptions.UserNotFoundException;
import vn.edu.iuh.fit.repositories.UserRepository;
import vn.edu.iuh.fit.services.UserService;

import java.util.Optional;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   16/03/2025
 * @version:    1.0
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;


    private UserResponse convertToDto(User user) {
        return modelMapper.map(user, UserResponse.class);
    }
    @Override
    public UserResponse getUserByPhone(String phone) {
        User user = userRepository.findByPhone(phone).orElseThrow(() ->new UserNotFoundException("Không tìm thấy người dùng với số điện thoại: " + phone));
        return convertToDto(user);
    }

    @Override
    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }

    @Override
    public void save(User user) {
        userRepository.save(user);
    }

    @Override
    public boolean isPasswordValid(String phone, String password) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng với số điện thoại: " + phone));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidPasswordException("Mật khẩu không chính xác.");
        }

        return true;
    }
}
