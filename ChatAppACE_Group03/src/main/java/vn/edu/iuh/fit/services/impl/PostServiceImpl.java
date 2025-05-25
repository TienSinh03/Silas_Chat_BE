/**
 * @ (#) PostServiceImpl.java      5/24/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.services.impl;

import com.google.monitoring.v3.Aggregation;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.dtos.PostUserDTO;
import vn.edu.iuh.fit.entities.Post;
import vn.edu.iuh.fit.entities.User;
import vn.edu.iuh.fit.repositories.PostRepository;
import vn.edu.iuh.fit.services.PostService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * @description:
 * @author: Tien Minh Tran
 * @date: 5/24/2025
 */
@Service

public class PostServiceImpl implements PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Post savePost(Post post) {
        // Nếu là post mới chưa có ID => sinh ObjectId mới
        if (post.getId() == null) {
            post.setId(new ObjectId());
            post.setCreatedAt(Instant.now());
        } else {
            post.setUpdatedAt(Instant.now());
        }

        return postRepository.save(post);
    }

    @Override
    public List<Post> findByUserId(ObjectId userId) {
        return postRepository.findByUserId(userId);
    }

    @Override
    public List<Post> findAll() {
        // Lấy tất cả bài viết
        // Nếu cần phân trang, có thể sử dụng Pageable

        return postRepository.findAll();
    }

    @Override
    public List<PostUserDTO> findUsersWithPosts() {
        // Sử dụng MongoTemplate để thực hiện truy vấn phức
        List<PostUserDTO> result = new ArrayList<>();
        AggregationResults<Document> aggregationResults = mongoTemplate.aggregate(
                org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation(
                        org.springframework.data.mongodb.core.aggregation.Aggregation.lookup("user", "userId", "_id", "userInfo")
                ),
                "post",
                Document.class
        );
        List<Document> documents = aggregationResults.getMappedResults();
        Map<ObjectId, PostUserDTO> userMap = new HashMap<>();
        for (Document doc : documents) {
            ObjectId userId = doc.getObjectId("userId");
            User user = mongoTemplate.findById(userId, User.class);
            if (user != null) {
                Post post = new Post();
                post.setId(doc.getObjectId("_id"));
                post.setUserId(userId);
                post.setContent(doc.getString("content"));
                post.setImageUrl(doc.getString("imageUrl"));
                post.setCreatedAt(doc.getDate("createdAt").toInstant());
                post.setUpdatedAt(doc.getDate("updatedAt") != null ? doc.getDate("updatedAt").toInstant() : null);
                post.setPublic(doc.getBoolean("isPublic", false));
                post.setLikeCount(doc.getInteger("likeCount", 0));
                post.setComment(doc.getString("comment"));
                post.setFonts(doc.getInteger("fonts", 0));

                if (!userMap.containsKey(userId)) {
                    userMap.put(userId, new PostUserDTO(user, new ArrayList<>()));
                }
                userMap.get(userId).getPosts().add(post);
            }
        }
        result.addAll(userMap.values());
        return result;
    }


}