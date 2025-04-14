/**
 * @ (#) FriendRequestServiceImpl.java      4/14/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.services.impl;

import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.dtos.request.FriendRequestReq;
import vn.edu.iuh.fit.dtos.response.FriendRequestResponse;
import vn.edu.iuh.fit.dtos.response.FriendResponse;
import vn.edu.iuh.fit.dtos.response.UserResponse;
import vn.edu.iuh.fit.entities.Friend;
import vn.edu.iuh.fit.entities.FriendRequest;
import vn.edu.iuh.fit.entities.User;
import vn.edu.iuh.fit.enums.FriendStatus;
import vn.edu.iuh.fit.enums.RequestFriendStatus;
import vn.edu.iuh.fit.exceptions.FriendRequestException;
import vn.edu.iuh.fit.exceptions.UserNotFoundException;
import vn.edu.iuh.fit.repositories.FriendRepository;
import vn.edu.iuh.fit.repositories.FriendRequestRepository;
import vn.edu.iuh.fit.repositories.UserRepository;
import vn.edu.iuh.fit.services.FriendRequestService;
import vn.edu.iuh.fit.services.FriendService;
import vn.edu.iuh.fit.services.UserService;

import java.time.Instant;
import java.time.LocalDateTime;

/*
 * @description:
 * @author: Sinh Phan Tien
 * @date: 4/14/2025
 */
@Service
public class FriendRequestServiceImpl implements FriendRequestService
{
    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FriendRepository friendRepository;

    //entity to dto
    private FriendRequestResponse convertToDto(FriendRequest friendRequest) {
        return modelMapper.map(friendRequest, FriendRequestResponse.class);
    }

    //dto to entity
    private FriendRequest convertToEntity(FriendRequestResponse friendRequestReq) {
        return modelMapper.map(friendRequestReq, FriendRequest.class);
    }

    /**
     * Gửi lời mời kết bạn
     * @param friendRequestReq
     * @return
     */
    @Override
    public FriendRequestResponse sendFriendRequest(FriendRequestReq friendRequestReq) {

        // Validate the request
        ObjectId senderId = friendRequestReq.getSenderId();
        ObjectId receiverId = friendRequestReq.getReceiverId();

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new UserNotFoundException("Người gửi không tồn tại."));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new UserNotFoundException("Người nhận không tồn tại."));

        if(friendRequestRepository.existsBySenderAndReceiver(sender.getId(), receiver.getId())) {
            throw new IllegalArgumentException("Lời mời đã được gửi trước đó.");
        }

        // tao 1 friend request
        FriendRequest newFriendRequest = new FriendRequest();
        newFriendRequest.setSender(sender.getId());
        newFriendRequest.setReceiver(receiver.getId());
        newFriendRequest.setStatus(RequestFriendStatus.PENDING);
        newFriendRequest.setSendAt(Instant.now());

        // Lưu vào cơ sở dữ liệu
        newFriendRequest = friendRequestRepository.save(newFriendRequest);

        return this.convertToDto(newFriendRequest);

    }

    @Override
    public boolean acceptFriendRequest(String token, ObjectId requestId) {
        UserResponse user = userService.getCurrentUser(token);

        FriendRequest friendRequest = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new FriendRequestException("Lời mời không tồn tại."));

        // Kiểm tra trạng thái của lời mời
        if(friendRequest.getStatus() != RequestFriendStatus.PENDING) {
            throw new FriendRequestException("Lời mời đã được chấp nhận hoặc từ chối trước đó.");
        }

        friendRequest.setStatus(RequestFriendStatus.ACCEPTED);
        friendRequest.setSendAt(Instant.now());

        // Lưu vào cơ sở dữ liệu
        friendRequestRepository.save(friendRequest);

        Friend friend = Friend.builder()
                .userId(friendRequest.getReceiver())
                .friendId(friendRequest.getSender())
                .status(FriendStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();

        friendRepository.save(friend);
        // Lưu vào cơ sở dữ liệu
        return true;
    }
}
