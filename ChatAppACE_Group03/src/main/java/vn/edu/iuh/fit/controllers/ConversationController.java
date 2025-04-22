/*
 * @ {#} ConversationController.java   1.0     14/04/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.controllers;

import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.dtos.ConversationDTO;
import vn.edu.iuh.fit.dtos.response.MemberResponse;
import vn.edu.iuh.fit.dtos.response.UserResponse;
import vn.edu.iuh.fit.services.ConversationService;
import vn.edu.iuh.fit.services.UserService;

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
}
