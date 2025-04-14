package vn.edu.iuh.fit.services.impl;

import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.dtos.request.ChatMessageRequest;
import vn.edu.iuh.fit.entities.Message;
import vn.edu.iuh.fit.enums.MessageType;
import vn.edu.iuh.fit.repositories.MessageRepository;
import vn.edu.iuh.fit.services.MessageService;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;

    // Phương thức kiểm tra ObjectId hợp lệ
    private boolean isValidObjectId(String str) {
        return str != null && str.matches("[a-fA-F0-9]{24}"); // Kiểm tra độ dài 24 ký tự và chỉ chứa các ký tự hex
    }

    @Override
    public Message sendMessage(ChatMessageRequest request) {
        // Kiểm tra tính hợp lệ của senderId và conversationId
        if (!isValidObjectId(request.getSenderId()) || !isValidObjectId(request.getConversationId())) {
            throw new IllegalArgumentException("Invalid ObjectId format");
        }

        Message message = Message.builder()
                .senderId(new ObjectId(request.getSenderId()))
                .conversationId(new ObjectId(request.getConversationId()))
                .content(request.getContent())
                .messageType(MessageType.valueOf(request.getMessageType()))
                .fileUrl(request.getFileUrl())
                .timestamp(Instant.now())
                .isSeen(false)
                .replyToMessageId(request.getReplyToMessageId() != null && isValidObjectId(request.getReplyToMessageId()) ? new ObjectId(request.getReplyToMessageId()) : null)
                .build();

        return messageRepository.save(message);
    }

    @Override
    public List<Message> getMessages(String conversationId) {
        // Kiểm tra tính hợp lệ của conversationId trước khi gọi method
        if (!isValidObjectId(conversationId)) {
            throw new IllegalArgumentException("Invalid conversationId format");
        }
        return messageRepository.findByConversationIdOrderByTimestampAsc(new ObjectId(conversationId));
    }

}

