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
import vn.edu.iuh.fit.entities.Conversation;
import vn.edu.iuh.fit.entities.Member;
import vn.edu.iuh.fit.enums.MemberRoles;
import vn.edu.iuh.fit.exceptions.ConversationCreationException;
import vn.edu.iuh.fit.repositories.ConversationRepository;
import vn.edu.iuh.fit.repositories.MemberRepository;
import vn.edu.iuh.fit.repositories.UserRepository;
import vn.edu.iuh.fit.services.ConversationService;

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
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final ModelMapper modelMapper;

    private ConversationDTO mapToDTO(Conversation conversation) {
        return modelMapper.map(conversation, ConversationDTO.class);
    }

    private Conversation mapToEntity(ConversationDTO conversationDTO) {
        return modelMapper.map(conversationDTO, Conversation.class);
    }


    @Override
    public ConversationDTO createConversationOneToOne(ConversationDTO conversationDTO) {
        // Chuyển đổi ConversationDTO thành Conversation entity
        Conversation conversation = mapToEntity(conversationDTO);

        // Thiết lập đây là cuộc trò chuyện một-một
        conversation.setGroup(false);

        // Lưu cuộc trò chuyện vào cơ sở dữ liệu
        Conversation savedConversation = conversationRepository.save(conversation);
        log.info("Conversation: " + conversation);
        // Tạo danh sách Member từ danh sách memberId
        // Mỗi memberId sẽ được chuyển thành một đối tượng Member
        Set<ObjectId> memberIds = Optional.ofNullable(conversationDTO.getMemberId())
                .orElseThrow(() -> new ConversationCreationException("memberId không được null"));

        // Kiểm tra xem danh sách memberId có đúng 2 thành viên không
        if (memberIds.size() != 2) {
            throw new ConversationCreationException("Cuộc trò chuyện một-một phải có đúng 2 thành viên.");
        }

        // Kiểm tra xem danh sách memberId có trùng lặp không
        if (memberIds.size() != new HashSet<>(memberIds).size()) {
            throw new ConversationCreationException("Danh sách memberId chứa giá trị trùng lặp.");
        }

        Set<Member> members = memberIds.stream()
                .map(memberId -> Member.builder()
                        .userId(memberId)
                        .conversationId(savedConversation.getId())
                        .role(MemberRoles.MEMBER)
                        .joinedAt(Instant.now())
                        .build())
                .collect(Collectors.toSet());

        // Lưu danh sách member vào cơ sở dữ liệu
        memberRepository.saveAll(members);

        return mapToDTO(savedConversation);
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
        return conversations.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }


}
