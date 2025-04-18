package vn.edu.iuh.fit.services;

import java.util.List;

import org.bson.types.ObjectId;
import vn.edu.iuh.fit.dtos.request.ChatMessageRequest;
import vn.edu.iuh.fit.entities.Message;

public interface MessageService {
    Message sendMessage(ChatMessageRequest request);
    List<Message> getMessages(String conversationId);

    public Message recallMessage(ObjectId messageId, ObjectId senderId, ObjectId conversationId);
    Message deleteMessageForUser(ObjectId messageId, ObjectId userId);
}