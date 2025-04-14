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
import vn.edu.iuh.fit.services.ConversationService;

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

     @GetMapping("/{id}")
     public ResponseEntity<ConversationDTO> getConversationById(@PathVariable ObjectId id) {
         return ResponseEntity.ok(conversationService.findConversationById(id));
     }

     @PostMapping("/createConversationOneToOne")
        public ResponseEntity<ConversationDTO> createConversationOneToOne(@RequestBody ConversationDTO conversationDTO) {
            return ResponseEntity.ok(conversationService.createConversationOneToOne(conversationDTO));
     }

     @GetMapping("/getAllConversationsByUserId")
        public ResponseEntity<?> getAllConversationsByUserId(@RequestParam ObjectId userId) {
            return ResponseEntity.ok(conversationService.findAllConversationsByUserId(userId));
     }

        @PostMapping("/createConversationGroup")
            public ResponseEntity<ConversationDTO> createConversationGroup(@RequestParam ObjectId creatorId, @RequestBody ConversationDTO conversationDTO) {
                return ResponseEntity.ok(conversationService.createConversationGroup(creatorId, conversationDTO));
        }
}
