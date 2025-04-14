/*
 * @ {#} ConversationController.java   1.0     14/04/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.controllers;

import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.dtos.ConversationDTO;
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
        UserResponse user = userService.getCurrentUser(token);
        return ResponseEntity.ok(conversationService.createConversationGroup(user.getId(), conversationDTO));
    }
}
