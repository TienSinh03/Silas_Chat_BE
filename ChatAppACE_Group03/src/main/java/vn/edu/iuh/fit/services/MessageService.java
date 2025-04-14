/*
 * @ {#} MessageService.java   1.0     14/04/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services;

import org.bson.types.ObjectId;
import vn.edu.iuh.fit.dtos.MessageDTO;

import java.util.List;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   14/04/2025
 * @version:    1.0
 */
public interface MessageService {
     MessageDTO sendMessage(MessageDTO req);
     List<MessageDTO> getMessagesByConversationId(ObjectId conversationId);
     MessageDTO findMessageById(ObjectId messageId);
}
