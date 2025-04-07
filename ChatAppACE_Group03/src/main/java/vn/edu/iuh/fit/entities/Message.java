/*
 * @ {#} Friend.java   1.0     18/03/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.entities;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import vn.edu.iuh.fit.enums.MessageType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   18/03/2025
 * @version:    1.0
 */
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "messages")
public class Message {
    @Id
    private ObjectId id;
    private ObjectId senderId;                  // Người gửi
    private ObjectId conversationId;            // ID cuộc trò chuyện

    private String content;                     // Nội dung tin nhắn (text)
    private MessageType messageType;

    private String fileUrl;               // Link file nếu là ảnh/video/file

    private Instant timestamp;                  // Thời gian gửi
    private boolean isSeen;

    private ObjectId replyToMessageId;          // Phản hồi tin nhắn nào (nếu có)

    private Map<String, List<ObjectId>> reactions; // Reaction voi tin nhan

    private List<ObjectId> deletedBy;

    // Quan hệ với FIle
    private List<ObjectId> fileIds; // Danh sách fileId nếu là ảnh/video/file

}

