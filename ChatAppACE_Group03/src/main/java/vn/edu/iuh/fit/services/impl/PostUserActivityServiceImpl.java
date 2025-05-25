/**
 * @ (#) PostUserActivityServiceImpl.java      5/25/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.services.impl;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.entities.PostUserActivity;
import vn.edu.iuh.fit.repositories.PostUserActivityRepository;
import vn.edu.iuh.fit.services.PostUserActivityService;

import java.util.List;

/*
 * @description:
 * @author: Tien Minh Tran
 * @date: 5/25/2025
 */
@Service
public class PostUserActivityServiceImpl implements PostUserActivityService {

    @Autowired
    private PostUserActivityRepository postUserActivityRepository;


    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public PostUserActivity savePostUserActivity(PostUserActivity postUserActivity) {
        // Thiết lập thời gian hành động nếu chưa có
        if (postUserActivity.getActivityTime() == null) {
            postUserActivity.setActivityTime(java.time.LocalDateTime.now());
        }

        // Lưu vào MongoDB qua repository
        return postUserActivityRepository.save(postUserActivity);
    }

    @Override
    public List<PostUserActivity> findByPostId(ObjectId postId) {
        // Sử dụng MongoTemplate để tìm tất cả hoạt động của người dùng theo postId
        return mongoTemplate.find(
                new org.springframework.data.mongodb.core.query.Query(
                        org.springframework.data.mongodb.core.query.Criteria.where("postId").is(postId)
                ),
                PostUserActivity.class
        );
    }

}