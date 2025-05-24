/**
 * @ (#) PostService.java      5/24/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.services;

import org.bson.types.ObjectId;
import vn.edu.iuh.fit.entities.Post;

import java.util.List;

/*
 * @description:
 * @author: Tien Minh Tran
 * @date: 5/24/2025
 */
public interface PostService {
    Post savePost(Post post);

    // Lấy tất cả bài viết của một người dùng
    List<Post> findByUserId(ObjectId userId);

}