/*
 * @ {#} ConversationController.java   1.0     14/04/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.controllers;

import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.dtos.ConversationDTO;
import vn.edu.iuh.fit.dtos.response.ApiResponse;
import vn.edu.iuh.fit.dtos.response.MemberResponse;
import vn.edu.iuh.fit.dtos.response.UserResponse;
import vn.edu.iuh.fit.entities.Member;
import vn.edu.iuh.fit.entities.Message;
import vn.edu.iuh.fit.exceptions.ConversationCreationException;
import vn.edu.iuh.fit.services.ConversationService;
import vn.edu.iuh.fit.services.UserService;

import java.util.List;
import java.util.Map;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   14/04/2025
 * @version:    1.0
 */
@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
public class ConversationController {
    private final ConversationService conversationService;
    private final UserService userService;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @GetMapping("/{id}")
    public ResponseEntity<ConversationDTO> getConversationById(@PathVariable ObjectId id) {
        return ResponseEntity.ok(conversationService.findConversationById(id));
    }

    @PostMapping("/createConversationOneToOne")
    public ResponseEntity<ConversationDTO> createConversationOneToOne(@RequestBody ConversationDTO conversationDTO) {
        return ResponseEntity.ok(conversationService.createConversationOneToOne(conversationDTO));
    }

    @GetMapping("/getAllConversationsByUserId")
    public ResponseEntity<?> getAllConversationsByUserId(@RequestHeader("Authorization") String token) {
        UserResponse user = userService.getCurrentUser(token);
        return ResponseEntity.ok(conversationService.findAllConversationsByUserId(user.getId()));
    }

    @PostMapping("/createConversationGroup")
    public ResponseEntity<ConversationDTO> createConversationGroup(@RequestHeader("Authorization") String token, @RequestBody ConversationDTO conversationDTO) {
        try {
            UserResponse user = userService.getCurrentUser(token);

            ConversationDTO conversation = conversationService.createConversationGroup(user.getId(), conversationDTO);

            for (ObjectId memberId : conversation.getMemberId()) {
                System.out.println("memberId: " + memberId);
                simpMessagingTemplate.convertAndSend("/chat/create/group/" + memberId, conversation);
            }
            return ResponseEntity.ok(conversation);
        } catch (Exception e) {
            System.out.println("Error creating group conversation: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/find-or-create")
    public ResponseEntity<ConversationDTO> findOrCreateConversation(
            @RequestParam("senderId") String senderIdStr,
            @RequestParam("receiverId") String receiverId
    ) {
        try {
            ObjectId senderId = new ObjectId(senderIdStr);
            ConversationDTO conversation = conversationService.findOrCreateConversation(senderId, receiverId);
            System.out.println("Sender ID: " + senderId);
            System.out.println("Receiver ID: " + receiverId);
            System.out.println("Conversation: " + conversation);
            return ResponseEntity.ok(conversation);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid ObjectId format: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/dissolve/{conversationId}")
    public ResponseEntity<?> dissolveConversation( @RequestHeader("Authorization") String token, @PathVariable ObjectId conversationId) {
        try {
            UserResponse user = userService.getCurrentUser(token);

            System.out.println("User ID: " + user.getId());
            System.out.println("Conversation ID: " + conversationId);
            Map<String, Object> result = conversationService.dissolveGroup(conversationId, user.getId()
            );

            if (!(boolean) result.get("success")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", result.get("message")));
            }

            // Lấy thông tin từ kết quả service
            List<Member> members = (List<Member>) result.get("members");
            String conversationName = (String) result.get("conversationName");

            System.out.println("Members to notify: " + members.size());
            // Gửi thông báo WebSocket cho tất cả thành viên
            for (Member member : members) {
                simpMessagingTemplate.convertAndSend(
                        "/chat/dissolve/group/" + member.getUserId(),
                        Map.of(
                                "conversationId", conversationId,
                                "conversationName", conversationName
                        )
                );
            }

            return ResponseEntity.ok(Map.of("success", true, "message", "Nhóm đã được giải tán thành công"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi khi giải tán nhóm: " + e.getMessage()));

        }
    }

    @DeleteMapping("/leave/{conversationId}")
    public ResponseEntity<?> leaveGroup(@PathVariable ObjectId conversationId, @RequestHeader("Authorization") String token) {
        System.out.println("Leave group conversation with ID: " + conversationId);
        try {
            Message message = conversationService.leaveGroup(conversationId, token);

            simpMessagingTemplate.convertAndSend("/chat/message/single/" + message.getConversationId(), message);

            ConversationDTO conversation = conversationService.findConversationById(message.getConversationId());

            for (ObjectId memberId : conversation.getMemberId()) {
                System.out.println("memberId: " + memberId);
                simpMessagingTemplate.convertAndSend("/chat/create/group/" + memberId, conversation);
            }

            return ResponseEntity.ok(message);
        } catch (ConversationCreationException e) {
            System.out.println("Error leaving group conversation: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/leave/{conversationId}/member/{memberId}")
    public ResponseEntity<?> removeMemberFromGroup(@PathVariable ObjectId conversationId, @PathVariable ObjectId memberId,@RequestHeader("Authorization") String token) {
        System.out.println("Leave group conversation with ID: " + conversationId);
        System.out.println("Member ID: " + memberId);
        try {
            Message message = conversationService.removeGroup(conversationId, token, memberId);

            simpMessagingTemplate.convertAndSend("/chat/message/single/" + message.getConversationId(), message);

            ConversationDTO conversation = conversationService.findConversationById(message.getConversationId());

            for (ObjectId member_id : conversation.getMemberId()) {
                System.out.println("memberId: " + member_id);
                simpMessagingTemplate.convertAndSend("/chat/create/group/" + member_id, conversation);
            }

            return ResponseEntity.ok(message);
        } catch (Exception e) {
            System.out.println("Error leaving group conversation: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("/add-member/{conversationId}")
    public ResponseEntity<?> addMemberToGroup(@PathVariable ObjectId conversationId, @RequestParam  ObjectId id) {
        System.out.println("Add member to group conversation with ID: " + conversationId);
        try {
            Message message = conversationService.addMemberGroup(conversationId, id);

            simpMessagingTemplate.convertAndSend("/chat/message/single/" + message.getConversationId(), message);

            return ResponseEntity.ok(message);
        } catch (Exception e) {
            System.out.println("Error adding member to group conversation: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /*
    test api
    http://localhost:8080/api/v1/conversations/add-member/68075bc43ec6ed45491a7c05
    {
        "idUser": "6807a181f727fc5e721618a7"
    }
     */

    @GetMapping("/members/{conversationId}")
    public ResponseEntity<List<UserResponse>> getMembersByConversationId(@PathVariable ObjectId conversationId) {
        try {
            List<UserResponse> members = conversationService.findUserByIDConversation(conversationId);
            return ResponseEntity.ok(members);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
}
