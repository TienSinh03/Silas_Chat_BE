package vn.edu.iuh.fit.dtos.request;

import lombok.Data;

@Data
public class ChatMessageRequest {
    private String senderId;
    private String receiverId;
    private String conversationId;
    private String content;
    private String messageType;
    private String fileUrl;
    private String replyToMessageId;

    public ChatMessageRequest(String senderId, String conversationId, String content, String messageType, String s, String replyToMessageId) {
        this.senderId = senderId;
        this.conversationId = conversationId;
        this.content = content;
        this.messageType = messageType;
        this.fileUrl = s;
        this.replyToMessageId = replyToMessageId;
    }
}

