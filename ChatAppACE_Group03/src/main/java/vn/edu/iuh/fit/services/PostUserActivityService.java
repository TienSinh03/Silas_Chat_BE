/**
 * @ (#) PostUserActivityService.java      5/25/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.services;

import org.bson.types.ObjectId;
import vn.edu.iuh.fit.entities.PostUserActivity;

import java.util.List;

/*
 * @description:
 * @author: Tien Minh Tran
 * @date: 5/25/2025
 */
public interface PostUserActivityService {
    // THEM
    PostUserActivity savePostUserActivity(PostUserActivity postUserActivity);

    // lấy tat ca theo post id
     List<PostUserActivity> findByPostId(ObjectId postId);
}