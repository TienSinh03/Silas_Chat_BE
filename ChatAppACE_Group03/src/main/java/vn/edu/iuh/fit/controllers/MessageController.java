package vn.edu.iuh.fit.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import com.cloudinary.Api;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.dtos.ConversationDTO;
import vn.edu.iuh.fit.dtos.MessageDTO;
import vn.edu.iuh.fit.dtos.request.ChatMessageRequest;
import vn.edu.iuh.fit.dtos.request.ImageRequest;
import vn.edu.iuh.fit.dtos.request.UpdateUserRequest;
import vn.edu.iuh.fit.dtos.response.ApiResponse;
import vn.edu.iuh.fit.entities.File;
import vn.edu.iuh.fit.entities.Message;
import vn.edu.iuh.fit.services.CloudinaryService;
import vn.edu.iuh.fit.services.ConversationService;
import vn.edu.iuh.fit.services.ImageService;
import vn.edu.iuh.fit.services.MessageService;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    @Autowired
    private CloudinaryService cloudinaryService;


    @Autowired
    private final MessageService messageService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ImageService imageService;

    @Autowired
    private final ConversationService conversationService;

    @PostMapping
    @MessageMapping("/chat/send")
    public ResponseEntity<ApiResponse<?>> sendMessage(@RequestBody ChatMessageRequest request) {
        try {
            System.out.println("Request: " + request);
            Message message = messageService.sendMessage(request);

            messagingTemplate.convertAndSend("/chat/message/single/" + message.getConversationId(), message);
            return ResponseEntity.ok(ApiResponse.builder()
                    .status("SUCCESS")
                    .message("Send message successfully")
                    .response(message)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .status("FAILED")
                    .message(e.getMessage())
                    .build());
        }
    }

    @PostMapping("/recall")
    @MessageMapping("/chat/recall")
    public ResponseEntity<ApiResponse<?>> recallMessage(@RequestBody Map<String, String> request) {
        try {
            System.out.println("Request: " + request);
            ObjectId messageId = new ObjectId(request.get("messageId"));
            ObjectId senderId = new ObjectId(request.get("senderId"));
            ObjectId conversationId = new ObjectId(request.get("conversationId"));

            Message messageRecall = messageService.recallMessage(messageId, senderId, conversationId);
            if (messageRecall == null) {
                return ResponseEntity.badRequest().body(ApiResponse.builder()
                        .status("FAILED")
                        .message("Message not found or not belong to this user")
                        .build());
            }
            System.out.println("Message recalled: " + messageRecall);
            // Send the recalled message to the client
            messagingTemplate.convertAndSend("/chat/message/single/" + messageRecall.getConversationId(), messageRecall);
            return ResponseEntity.ok(ApiResponse.builder()
                    .status("SUCCESS")
                    .message("Recall message successfully")
                    .response(messageRecall)
                    .build());

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .status("FAILED")
                    .message(e.getMessage())
                    .build());
        }
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<ApiResponse<?>> getMessages(@PathVariable String conversationId) {
        try {
            System.out.println("Conversation ID: " + conversationId);
            List<Message> messages = messageService.getMessages(conversationId);
            messagingTemplate.convertAndSend("/chat/messages/" + conversationId, messages);
            return ResponseEntity.ok(ApiResponse.builder()
                    .status("SUCCESS")
                    .message("Fetch messages successfully")
                    .response(messages)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .status("FAILED")
                    .message(e.getMessage())
                    .build());
        }
    }


    @PostMapping("/delete-for-user")
    @MessageMapping("/chat/delete-for-user")
    public ResponseEntity<ApiResponse<?>> deleteMessageForUser(@RequestBody Map<String, String> request) {
        try {
            ObjectId messageId = new ObjectId(request.get("messageId"));
            ObjectId userId = new ObjectId(request.get("userId"));

            Message updatedMessage = messageService.deleteMessageForUser(messageId, userId);

            if (updatedMessage == null) {
                return ResponseEntity.badRequest().body(ApiResponse.builder()
                        .status("FAILED")
                        .message("Message not found")
                        .build());
            }

            messagingTemplate.convertAndSend("/chat/message/single/" + updatedMessage.getConversationId(), updatedMessage);

            return ResponseEntity.ok(ApiResponse.builder()
                    .status("SUCCESS")
                    .message("Message deleted for user")
                    .response(updatedMessage)
                    .build());

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .status("FAILED")
                    .message(e.getMessage())
                    .build());
        }
    }
    
@PostMapping(value = "/upload-img", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<ApiResponse<?>> uploadImage(
        @RequestPart("request") String reqJson,
        @RequestPart(value = "anh", required = false) MultipartFile anh) {
    System.out.println("Request upload img: " + reqJson);
    ObjectMapper objectMapper = new ObjectMapper();
    ChatMessageRequest chatMessageRequest = null;


        try {
            chatMessageRequest = objectMapper.readValue(reqJson, ChatMessageRequest.class);

            if (anh != null && !anh.isEmpty()) {
                System.out.println("Anh" + anh.getOriginalFilename());

                String imgUrl = cloudinaryService.uploadImage(anh);
                System.out.println("url" + imgUrl);

                chatMessageRequest.setFileUrl(imgUrl);
                System.out.println("File name: " + anh.getOriginalFilename());
                System.out.println("File size: " + anh.getSize());


                ImageRequest imageRequest = ImageRequest.builder()
                        .fileName(anh.getOriginalFilename())
                        .fileType(anh.getContentType())
                        .fileUrl(imgUrl)
                        .uploadedAt(Instant.now())
//                        .sender(chatMessageRequest.getSenderId())
//                        .receiver(chatMessageRequest.getReceiverId())
//                        .messageId(chatMessageRequest.getReplyToMessageId())
                        .build();

                imageService.saveImage(imageRequest);
            } else {

            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder()
                            .status("FAILED")
                            .message("Invalid 1111111 format")
                            .build());
        }

        if (chatMessageRequest.getSenderId() == null || chatMessageRequest.getConversationId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder()
                            .status("FAILED")
                            .message("LOI")
                            .build());
        }

        try {
            Message message = messageService.sendMessage(chatMessageRequest);
            return ResponseEntity.ok(ApiResponse.builder()
                    .status("SUCCESS")
                    .message("Upload image successfully")

                    .response(message)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .status("FAILED")
                    .message(e.getMessage())
                    .build());
        }
    }



    @PostMapping("/forward")
    public ResponseEntity<ApiResponse<?>> forwardMessage(@RequestBody Map<String, String> request) {
        try {
            ObjectId messageId = new ObjectId(request.get("messageId"));
            ObjectId senderId = new ObjectId(request.get("senderId"));
            String receiverId = request.get("receiverId"); // Có thể là userId hoặc groupId
            String content = request.get("content"); // Nội dung tin nhắn gốc

            // Tìm tin nhắn gốc
            Message originalMessage = messageService.getMessageById(messageId);
            if (originalMessage == null || !originalMessage.getSenderId().equals(senderId)) {
                return ResponseEntity.badRequest().body(ApiResponse.builder()
                        .status("FAILED")
                        .message("Message not found or unauthorized")
                        .build());
            }

            // Tìm hoặc tạo cuộc trò chuyện đích
            ConversationDTO conversation = conversationService.findOrCreateConversation(senderId, receiverId);

            // Tạo tin nhắn mới
            ChatMessageRequest forwardRequest = new ChatMessageRequest();
            forwardRequest.setConversationId(conversation.getId().toString());
            forwardRequest.setSenderId(senderId.toString());
            forwardRequest.setReceiverId(receiverId);
            forwardRequest.setContent(content);
            forwardRequest.setMessageType("TEXT"); // Có thể mở rộng cho hình ảnh/file

            Message forwardedMessage = messageService.sendMessage(forwardRequest);

            // Gửi qua WebSocket
            messagingTemplate.convertAndSend("/chat/message/single/" + forwardedMessage.getConversationId(), forwardedMessage);

            return ResponseEntity.ok(ApiResponse.builder()
                    .status("SUCCESS")
                    .message("Message forwarded successfully")
                    .response(forwardedMessage)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .status("FAILED")
                    .message(e.getMessage())
                    .build());
        }
    }
}