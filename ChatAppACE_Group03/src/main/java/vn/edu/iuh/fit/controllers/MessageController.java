package vn.edu.iuh.fit.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.dtos.MessageDTO;
import vn.edu.iuh.fit.dtos.request.ChatMessageRequest;
import vn.edu.iuh.fit.dtos.request.UpdateUserRequest;
import vn.edu.iuh.fit.dtos.response.ApiResponse;
import vn.edu.iuh.fit.entities.Message;
import vn.edu.iuh.fit.services.CloudinaryService;
import vn.edu.iuh.fit.services.MessageService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    @Autowired
    private CloudinaryService cloudinaryService;


    @Autowired
    private final MessageService messageService;


    @PostMapping
    public ResponseEntity<ApiResponse<?>> sendMessage(@RequestBody ChatMessageRequest request) {
        try {
            Message message = messageService.sendMessage(request);
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

    @GetMapping("/{conversationId}")
    public ResponseEntity<ApiResponse<?>> getMessages(@PathVariable String conversationId) {
        try {
            List<Message> messages = messageService.getMessages(conversationId);
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


//    @PostMapping("/upload-file")
//    public ResponseEntity<ApiResponse<?>> uploadFile(
//            @RequestPart("request") String reqJson,
//            @RequestPart(value = "file", required = false) MultipartFile file ,
//            @RequestHeader("Authorization") String token) {
//        System.out.println("Request upload file: " + reqJson);
//        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.registerModule(new JavaTimeModule());
//        ChatMessageRequest chatMessageRequest = null;
//
//        try {
//            chatMessageRequest = objectMapper.readValue(reqJson, ChatMessageRequest.class);
//            String fileUrl = null;
//            if (file != null && !file.isEmpty()) {
//                fileUrl = cloudinaryService.uploadImage(file);
//                chatMessageRequest.setFileUrl(fileUrl);
//                System.out.println("File name: " + file.getOriginalFilename());
//                System.out.println("File size: " + file.getSize());
//            }
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body(ApiResponse.builder()
//                            .status("FAILED")
//                            .message("Invalid request format")
//                            .build());
//        }
//        System.out.println("Authorization token: " + token);
//        // Validate the request
//        if (chatMessageRequest.getSenderId() == null || chatMessageRequest.getConversationId() == null) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body(ApiResponse.builder()
//                            .status("FAILED")
//                            .message("Invalid request format")
//                            .build());
//        }
//
//        // Process the request
//        try {
//            Message message = messageService.sendMessage(chatMessageRequest);
//            return ResponseEntity.ok(ApiResponse.builder()
//                    .status("SUCCESS")
//                    .message("Upload file successfully")
//                    .response(message)
//                    .build());
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(ApiResponse.builder()
//                    .status("FAILED")
//                    .message(e.getMessage())
//                    .build());
//        }
//    }
    // GỞI ẢNH
    //send image (update ->userController)
@PostMapping("/upload-img")
public ResponseEntity<ApiResponse<?>> uploadImage(
        @RequestPart("request") String reqJson,
        @RequestPart(value = "anh", required = false) MultipartFile anh,
        @RequestHeader("Authorization") String token) {
    System.out.println("Request upload img: " + reqJson);
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    ChatMessageRequest chatMessageRequest;

    try {
        chatMessageRequest = objectMapper.readValue(reqJson, ChatMessageRequest.class);

        if (anh != null && !anh.isEmpty()) {
            String imgUrl = cloudinaryService.uploadImage(anh);
            chatMessageRequest.setFileUrl(imgUrl);
            System.out.println("File name: " + anh.getOriginalFilename());

        }
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.builder()
                        .status("FAILED")
                        .message("Invalid request format")
                        .build());
    }

    if (chatMessageRequest.getSenderId() == null || chatMessageRequest.getConversationId() == null) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.builder()
                        .status("FAILED")
                        .message("Invalid request format")
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

    //send file (update ->userController)
}