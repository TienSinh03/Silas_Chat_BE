package vn.edu.iuh.fit.services.impl;

import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.entities.Conservation;
import vn.edu.iuh.fit.repositories.ConservationRepository;
import vn.edu.iuh.fit.services.ConservationService;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ConservationServiceImpl implements ConservationService {

    private final MongoTemplate mongoTemplate;
    private final ConservationRepository conservationRepository;

    @Override
    public Conservation createPrivateConservation(String senderIdStr, String receiverIdStr) {
        ObjectId senderId = new ObjectId(senderIdStr);
        ObjectId receiverId = new ObjectId(receiverIdStr);

        // Tìm cuộc trò chuyện 1-1 đã tồn tại
        Query query = new Query();
        query.addCriteria(
                Criteria.where("isGroup").is(false)
                        .andOperator(
                                Criteria.where("memberId").all(List.of(senderId, receiverId)),
                                Criteria.where("memberId").size(2)
                        )
        );

        Conservation existing = mongoTemplate.findOne(query, Conservation.class);
        if (existing != null) {
            return existing;
        }

        // Nếu không tồn tại thì tạo mới
        Conservation conservation = Conservation.builder()
                .isGroup(false)
                .memberId(Set.of(senderId, receiverId))
                .createdAt(Instant.now())
                .build();

        return conservationRepository.save(conservation);
    }
}