package vn.edu.iuh.fit.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import vn.edu.iuh.fit.dtos.request.ChatMessageRequest;
import vn.edu.iuh.fit.entities.Message;
import vn.edu.iuh.fit.services.MessageService;

@RequiredArgsConstructor
@Controller
public class ChatSocketController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send/{conversationId}")
    public void send(@Payload ChatMessageRequest messageRequest, @DestinationVariable String conversationId) {
        Message savedMessage = messageService.sendMessage(messageRequest);

        // Gửi về người nhận
        messagingTemplate.convertAndSendToUser(
                messageRequest.getReceiverId(),
                "/queue/messages",
                savedMessage
        );

        // Gửi về người gửi luôn (để hiển thị chính họ)
        messagingTemplate.convertAndSendToUser(
                messageRequest.getSenderId(),
                "/queue/messages",
                savedMessage
        );
    }
}
