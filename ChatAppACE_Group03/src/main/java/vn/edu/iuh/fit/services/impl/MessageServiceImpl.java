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
                .recalled(false)
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

    @Override
    public Message recallMessage(ObjectId messageId, ObjectId senderId, ObjectId conversationId) {
        if(messageId == null || senderId == null || conversationId == null) {
            throw new IllegalArgumentException("Message ID, Sender ID, and Conversation ID không thể null.");
        }
        // Tìm kiếm tin nhắn theo ID
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Tin nhắn không tồn tại."));
        // Kiểm tra xem người gửi có phải là người đã gửi tin nhắn không
        if(!message.getSenderId().equals(senderId)) {
            throw new IllegalArgumentException("Bạn không phai người gửi tin nhắn này.");
        }

        // kiểm tra message co thuọc về cuộc trò chuyện không
        if(!message.getConversationId().equals(conversationId)) {
            throw new IllegalArgumentException("Tin nhắn không thuộc về cuộc trò chuyện này.");
        }

        //Kieểm tra xem tin nhắn đã được thu hồi chưa và nếu chưa thì thu hồi
        if(!message.isRecalled()) {
            message.setRecalled(true);
            message.setContent("Tin nhắn đã được thu hồi.");

            return messageRepository.save(message);
        }

        // Nếu tin nhắn đã được thu hồi thì không làm gì cả
        return message = Message.builder()
                .id(message.getId())
                .senderId(message.getSenderId())
                .conversationId(message.getConversationId())
                .content(message.getContent())
                .messageType(message.getMessageType())
                .fileUrl(message.getFileUrl())
                .timestamp(message.getTimestamp())
                .isSeen(message.isSeen())
                .recalled(true)
                .replyToMessageId(message.getReplyToMessageId())
                .build();
    }

    @Override
    public Message getMessageById(ObjectId messageId) {
        return messageRepository.findById(messageId).orElse(null);
    }

}

