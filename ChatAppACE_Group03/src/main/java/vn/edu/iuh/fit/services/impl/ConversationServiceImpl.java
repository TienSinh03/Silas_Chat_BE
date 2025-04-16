/*
 * @ {#} ConservationServiceImpl.java   1.0     14/04/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.dtos.ConversationDTO;
import vn.edu.iuh.fit.dtos.MessageDTO;
import vn.edu.iuh.fit.dtos.response.UserResponse;
import vn.edu.iuh.fit.entities.Conversation;
import vn.edu.iuh.fit.entities.Member;
import vn.edu.iuh.fit.entities.Message;
import vn.edu.iuh.fit.enums.MemberRoles;
import vn.edu.iuh.fit.exceptions.ConversationCreationException;
import vn.edu.iuh.fit.repositories.ConversationRepository;
import vn.edu.iuh.fit.repositories.MemberRepository;
import vn.edu.iuh.fit.repositories.MessageRepository;
import vn.edu.iuh.fit.repositories.UserRepository;
import vn.edu.iuh.fit.services.ConversationService;
import vn.edu.iuh.fit.services.UserService;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   14/04/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationServiceImpl implements ConversationService {
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final MemberRepository memberRepository;
    private final ModelMapper modelMapper;
    private final UserService userService;
    private final UserRepository userRepository;

    private ConversationDTO mapToDTO(Conversation conversation) {
        ConversationDTO dto = ConversationDTO.builder()
                .id(conversation.getId())
                .name(conversation.getName())
                .avatar(conversation.getAvatar())
                .isGroup(conversation.isGroup())
                .lastMessageId(conversation.getLastMessageId())
                .createdAt(conversation.getCreatedAt())
                .memberId(conversation.getMemberId())
                .messageIds(conversation.getMessageIds())
                .build();

        // Populate members
        List<UserResponse> members = conversation.getMemberId().stream()
                .map(userId -> {
                    return userRepository.findById(userId)
                            .map(user -> UserResponse.builder()
                                    .id(user.getId())
                                    .displayName(user.getDisplayName())
                                    .avatar(user.getAvatar())
                                    .build())
                            .orElse(null);
                })
                .filter(userResponse -> userResponse != null)
                .collect(Collectors.toList());
        dto.setMembers(members);

        // Populate lastMessage
        if (conversation.getLastMessageId() != null) {
            messageRepository.findById(conversation.getLastMessageId())
                    .ifPresent(message -> {
                        dto.setLastMessage(MessageDTO.builder()
                                .id(message.getId())
                                .content(message.getContent())
                                .senderId(message.getSenderId())
                                .conversationId(message.getConversationId())
                                .timestamp(message.getTimestamp())
                                .messageType(message.getMessageType())
                                .build());
                    });
        }else {
            dto.setLastMessage(null);
        }

        return dto;
    }

    private Conversation mapToEntity(ConversationDTO conversationDTO) {
        return modelMapper.map(conversationDTO, Conversation.class);
    }




    @Override
    public ConversationDTO createConversationOneToOne(ConversationDTO conversationDTO) {
        Set<ObjectId> memberIds = Optional.ofNullable(conversationDTO.getMemberId())
                .orElseThrow(() -> new ConversationCreationException("memberId không được null"));

        // Validate phải đúng 2 user và không trùng lặp
        if (memberIds.size() != 2) {
            throw new ConversationCreationException("Cuộc trò chuyện một-một phải có đúng 2 thành viên.");
        }

        if (memberIds.size() != new HashSet<>(memberIds).size()) {
            throw new ConversationCreationException("Danh sách memberId chứa giá trị trùng lặp.");
        }

        // Kiểm tra xem đã có cuộc trò chuyện một-một giữa 2 người này chưa
        List<Conversation> existingConversations = conversationRepository.findOneToOneConversationByMemberIds(memberIds, false);
        for (Conversation existing : existingConversations) {
            Set<ObjectId> existingMemberIds = memberRepository.findByConversationId(existing.getId())
                    .stream()
                    .map(Member::getUserId)
                    .collect(Collectors.toSet());

            if (existingMemberIds.equals(memberIds)) {
                // Đã tồn tại cuộc trò chuyện một-một
                ConversationDTO existingDTO = mapToDTO(existing);
                existingDTO.setMemberId(existingMemberIds);
                return existingDTO;
            }
        }

        // Chưa có, tiến hành tạo mới
        Conversation conversation = mapToEntity(conversationDTO);
        conversation.setGroup(false);
        conversation.setCreatedAt(Instant.now());
        Conversation savedConversation = conversationRepository.save(conversation);

        Set<Member> members = memberIds.stream()
                .map(memberId -> Member.builder()
                        .userId(memberId)
                        .conversationId(savedConversation.getId())
                        .role(MemberRoles.MEMBER)
                        .joinedAt(Instant.now())
                        .build())
                .collect(Collectors.toSet());

        memberRepository.saveAll(members);

        ConversationDTO result = mapToDTO(savedConversation);
        result.setMemberId(memberIds);

        // Nếu DTO đầu vào có messageIds -> gán lại và tính lastMessageId
        if (conversationDTO.getMessageIds() != null && !conversationDTO.getMessageIds().isEmpty()) {
            result.setMessageIds(conversationDTO.getMessageIds());
            ObjectId lastMessageId = getLastMessageId(result.getMessageIds());
            result.setLastMessageId(lastMessageId);
            // Populate lastMessage
            if (lastMessageId != null) {
                messageRepository.findById(lastMessageId)
                        .ifPresent(message -> {
                            MessageDTO messageDTO = MessageDTO.builder()
                                    .id(message.getId())
                                    .content(message.getContent())
                                    .senderId(message.getSenderId())
                                    .conversationId(message.getConversationId())
                                    .timestamp(message.getTimestamp())
                                    .messageType(message.getMessageType())
                                    .build();
                            result.setLastMessage(messageDTO);
                            log.debug("Populated lastMessage for new conversation {}: {}", savedConversation.getId(), messageDTO.getId());
                        });
            }
        } else {
            // Mặc định là rỗng khi vừa tạo mới
            result.setMessageIds(new HashSet<>());
            result.setLastMessageId(null);
            result.setLastMessage(null);
            log.debug("No messages for new conversation {}", savedConversation.getId());
        }
        return result;
    }


    @Override
    public ConversationDTO createConversationGroup(ObjectId creatorId, ConversationDTO conversationDTO) {
        // Chuyển đổi ConversationDTO thành Conversation entity
        Conversation conversation = mapToEntity(conversationDTO);

        // Thiết lập đây là cuộc trò chuyện nhóm
        conversation.setGroup(true);

        // Lưu cuộc trò chuyện vào cơ sở dữ liệu
        Conversation savedConversation = conversationRepository.save(conversation);
        log.info("Conversation: " + conversation);

        // Tạo danh sách Member
        Set<Member> members=new HashSet<>();

        // Thêm người tạo group vào danh sách thành viên với vai trò ADMIN
        members.add(Member.builder()
                .conversationId(savedConversation.getId())
                .userId(creatorId)
                .role(MemberRoles.ADMIN)
                .joinedAt(Instant.now())
                .build());

        log.debug("Added creator as ADMIN with ID: {}", creatorId);

        // Lấy danh sách ID của các thành viên từ ConversationDTO
        Set<ObjectId> memberIds = Optional.ofNullable(conversationDTO.getMemberId())
                .orElseThrow(() -> new ConversationCreationException("memberId không được null"));

        // Kiểm tra trùng lặp với creatorId
        if (memberIds.contains(creatorId)) {
            throw new ConversationCreationException("creatorId không được trùng với memberId");
        }

        // Lấy danh sách ID của các thành viên từ ConversationDTO
        members.addAll(memberIds.stream()
                .map(memberId -> Member.builder()
                        .conversationId(savedConversation.getId())
                        .userId(memberId)
                        .role(MemberRoles.MEMBER)
                        .joinedAt(Instant.now())
                        .build())
                .collect(Collectors.toSet()));

        System.out.println("Members: " + members.size());

        // Lưu danh sách member vào cơ sở dữ liệu
        memberRepository.saveAll(members);
        ConversationDTO result = mapToDTO(savedConversation);
        Set<ObjectId> allMemberIds = members.stream()
                .map(Member::getUserId)
                .collect(Collectors.toSet());
        result.setMemberId(allMemberIds);
        return result;
    }

    @Transactional(readOnly = true) // Đánh dấu phương thức này là chỉ đọc để tối ưu hóa hiệu suất
    @Override
    public ConversationDTO findConversationById(ObjectId conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        return mapToDTO(conversation);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ConversationDTO> findAllConversationsByUserId(ObjectId userId) {

        if (userId == null) {
            throw new IllegalArgumentException("userId không được null");
        }

        // Tìm tất cả các thành người dùng trong cơ  viên của sở dữ liệu
        List<Member> members = memberRepository.findByUserId(userId);
        System.out.println("Members: " + members);
        if (members.isEmpty()) {
            log.debug("No conversations found for userId: {}", userId);
            return Collections.emptyList();
        }


        // Lấy danh sách ID của các cuộc trò chuyện mà người dùng tham gia
        List<ObjectId> conversationIds = members.stream()
                .map(Member::getConversationId)
                .collect(Collectors.toList());
        System.out.println("Conversation IDs: " + conversationIds);


        // Tìm tất cả các cuộc trò chuyện dựa trên danh sách ID
        List<Conversation> conversations = conversationRepository.findAllById(conversationIds);
        System.out.println("Conversations: " + conversations);
        return conversations.stream().map(conversation -> {
            ConversationDTO dto = mapToDTO(conversation);

            // Lấy tất cả các Member record thuộc conversation hiện tại
            List<Member> allMembers = memberRepository.findByConversationId(conversation.getId());

            // Lấy userId của từng thành viên
            Set<ObjectId> userIds = allMembers.stream()
                    .map(Member::getUserId)
                    .collect(Collectors.toSet());

            // Lấy thông tin chi tiết các user
            List<UserResponse> userDTOs = userService.getUsersByIds(userIds); // bạn cần tạo hàm này nếu chưa có
            dto.setMembers(userDTOs);

            // Lấy thông tin lastMessage nếu tồn tại
            if (conversation.getLastMessageId() != null) {
                Optional<Message> lastMessageOpt = messageRepository.findById(conversation.getLastMessageId());
                lastMessageOpt.ifPresent(message -> {
                    MessageDTO messageDTO = modelMapper.map(message, MessageDTO.class);

//                    // Lấy thêm thông tin người gửi
//                    UserResponse sender = userService.getUserById(message.getSenderId());
//                    messageDTO.setSender(sender);

                    dto.setLastMessage(messageDTO);
                });
            }

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public ObjectId getLastMessageId(Set<ObjectId> messageIds) {
        return messageRepository.findByIdIn(messageIds).stream()
                .max(Comparator.comparing(Message::getTimestamp))
                .map(Message::getId)
                .orElse(null);
    }
}
