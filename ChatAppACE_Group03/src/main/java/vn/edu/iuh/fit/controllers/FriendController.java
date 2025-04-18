/**
 * @ (#) FriendController.java      4/14/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.controllers;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.dtos.request.FriendRequestReq;
import vn.edu.iuh.fit.dtos.response.*;
import vn.edu.iuh.fit.services.FriendRequestService;
import vn.edu.iuh.fit.services.FriendService;
import vn.edu.iuh.fit.services.UserService;

import java.util.List;

/*
 * @description:
 * @author: Sinh Phan Tien
 * @date: 4/14/2025
 */
@RestController
@RequestMapping("/api/v1/friend")
public class FriendController {

    @Autowired
    private FriendService friendService;

    @Autowired
    private FriendRequestService friendRequestService;
    @Autowired
    private UserService userService;

    @GetMapping("/friend-requests/received")
    public ResponseEntity<ApiResponse<?>> getFriendsRequest(@RequestHeader("Authorization") String token) {
        try {
            System.out.println("Token: " + token);
            List<FriendRequestResponse>  friendRequests = friendRequestService.getFriendRequests(token);
            if(friendRequests.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(ApiResponse.builder().status("SUCCESS").message("Get list Friend Request  is empty").build());
            }
            return ResponseEntity.ok(ApiResponse.builder().status("SUCCESS").message("Get list Friend Request  successfully").response(friendRequests).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder().status("FAILED").message(e.getMessage()).build());
        }
    }

    @GetMapping("/friend-requests/sent")
    public ResponseEntity<ApiResponse<?>> getSentFriendsRequest(@RequestHeader("Authorization") String token) {
        try {
            System.out.println("Token: " + token);
            List<FriendRequestResponse>  friendRequests = friendRequestService.getSentFriendRequests(token);

            if(friendRequests.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(ApiResponse.builder().status("SUCCESS").message("Get list Friend Request Sent is empty").build());
            }

            return ResponseEntity.ok(ApiResponse.builder().status("SUCCESS").message("Get list Friend Request Sent successfully").response(friendRequests).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder().status("FAILED").message(e.getMessage()).build());
        }
    }

    @PostMapping("/send-request")
    public ResponseEntity<ApiResponse<?>> sendFriendRequest(@RequestHeader("Authorization") String token, @RequestBody FriendRequestReq friendRequestReq) {
        try {
            FriendRequestDto response = friendRequestService.sendFriendRequest(token,friendRequestReq);
            return ResponseEntity.ok(ApiResponse.builder().status("SUCCESS").message("Request Friend Successfully").response(response).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder().status("FAILED").message(e.getMessage()).build());
        }
    }

    @PostMapping("/accept-request/{requestId}")
    public ResponseEntity<ApiResponse<?>> acceptFriendRequest(@RequestHeader("Authorization") String token,
                                                               @PathVariable("requestId") ObjectId requestId) {
        try {
            System.out.println("Token: " + token);
            System.out.println("Request ID: " + requestId);
            boolean isAccepted = friendRequestService.acceptFriendRequest(token, requestId);
            if(!isAccepted) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.builder().status("FAILED").message("Failed to accept friend request").build());
            }
            return ResponseEntity.ok(ApiResponse.builder().status("SUCCESS").message("Accept Friend Request Successfully").response("").build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder().status("FAILED").message(e.getMessage()).build());
        }
    }

    @PostMapping("/reject-request/{requestId}")
    public ResponseEntity<ApiResponse<?>> rejectFriendRequest(@RequestHeader("Authorization") String token,
                                                              @PathVariable("requestId") ObjectId requestId) {
        try {
            System.out.println("Token: " + token);
            System.out.println("Request ID: " + requestId);
            boolean isAccepted = friendRequestService.rejectFriendRequest(token, requestId);
            if(!isAccepted) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.builder().status("FAILED").message("Failed to reject friend request").build());
            }
            return ResponseEntity.ok(ApiResponse.builder().status("SUCCESS").message("Reject Friend Request Successfully").response("").build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder().status("FAILED").message(e.getMessage()).build());
        }
    }

    @PostMapping("/recall-request/{requestId}")
    public ResponseEntity<ApiResponse<?>> recallFriendRequest(@RequestHeader("Authorization") String token,
                                                              @PathVariable("requestId") ObjectId requestId) {
        try {
            System.out.println("Token: " + token);
            System.out.println("Request ID: " + requestId);
            boolean isAccepted = friendRequestService.recallFriendRequestSent(token, requestId);
            if(!isAccepted) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.builder().status("FAILED").message("Failed to recall friend request").build());
            }
            return ResponseEntity.ok(ApiResponse.builder().status("SUCCESS").message("Recall Friend Request Successfully").response("").build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder().status("FAILED").message(e.getMessage()).build());
        }
    }

    @PostMapping("/unfriend/{friendId}")
    public ResponseEntity<ApiResponse<?>> unfriend(@RequestHeader("Authorization") String token,
                                                   @PathVariable("friendId") ObjectId friendId) {
        try {
            System.out.println("Token: " + token);
            System.out.println("Friend ID: " + friendId);
            boolean isUnfriend = friendService.unfriend(token, friendId);
            if(!isUnfriend) {
                return ResponseEntity.status((HttpStatus.BAD_REQUEST))
                        .body(ApiResponse.builder().status("FAILED").message("Failed to unfriend").build());
            }
            return ResponseEntity.ok(ApiResponse.builder().status("SUCCESS").message("Unfriend Successfully").response("").build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder().status("FAILED").message(e.getMessage()).build());
        }
    }

    @GetMapping("/check-friend")
    public ResponseEntity<ApiResponse<?>> checkFriend(@RequestHeader("Authorization") String token,
                                                   @RequestParam ObjectId friendId) {
        try {
            System.out.println("Token: " + token);
            System.out.println("Friend ID: " + friendId);
            UserResponse user = userService.getCurrentUser(token);
            if(user == null) {
                return ResponseEntity.noContent().build();
            }
            boolean isFriend = friendService.isFriend(user.getId(), friendId);
            return ResponseEntity.ok(ApiResponse.builder().status("SUCCESS").message("Check friend Successfully").response(isFriend).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder().status("FAILED").message(e.getMessage()).build());
        }
    }
}
