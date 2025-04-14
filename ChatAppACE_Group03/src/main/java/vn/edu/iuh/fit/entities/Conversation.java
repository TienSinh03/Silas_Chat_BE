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
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

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
@Document(collection = "conversations")
public class Conversation {
    @Id
    private ObjectId id;
    private String name;
    private String avatar;
    @Field("is_group")
    private boolean isGroup;
    private ObjectId lastMessageId; // Lưu messageId cuối cùng
    private Instant createdAt;

    private Set<ObjectId> memberId; // Lưu danh sách memberId
    private Set<ObjectId> messageIds = new HashSet<>(); // Lưu danh sách messageId
}
