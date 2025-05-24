/**
 * @ (#) PostServiceImpl.java      5/24/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.services.impl;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.dtos.response.PostReponse;
import vn.edu.iuh.fit.entities.Post;
import vn.edu.iuh.fit.repositories.PostRepository;
import vn.edu.iuh.fit.services.PostService;

import java.time.Instant;
import java.util.List;

/*
 * @description:
 * @author: Tien Minh Tran
 * @date: 5/24/2025
 */
@Service

public class PostServiceImpl implements PostService {

    @Autowired
    private PostRepository postRepository;

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


}