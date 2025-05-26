/**
 * @ (#) PostUserActivityController.java      5/25/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.controllers;

/*
 * @description:
 * @author: Tien Minh Tran
 * @date: 5/25/2025
 */

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.entities.PostUserActivity;
import vn.edu.iuh.fit.services.PostUserActivityService;

@RestController
@RequestMapping("/api/v1/postactivity")
public class PostUserActivityController {

    @Autowired
    private PostUserActivityService postUserActivityService;

    //save
     @PostMapping("/save")
    public ResponseEntity<?> savePostUserActivity(@RequestBody PostUserActivity postUserActivity) {
         if (postUserActivity == null) {
             return ResponseEntity.badRequest().body("PostUserActivity cannot be null");
         }

         try {
             PostUserActivity savedActivity = postUserActivityService.savePostUserActivity(postUserActivity);
             return ResponseEntity.ok(savedActivity);
         } catch (Exception e) {
             return ResponseEntity.status(500).body("Error saving post user activity: " + e.getMessage());
         }
     }

    // lấy tất cả hoạt động của người dùng theo postId
    @GetMapping("/post/{postId}")
    public ResponseEntity<?> getActivitiesByPostId(@PathVariable ObjectId postId) {
        if (postId == null) {
            return ResponseEntity.badRequest().body("Post ID cannot be null");
        }

        try {
            return ResponseEntity.ok(postUserActivityService.findByPostId(postId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving post user activities: " + e.getMessage());
        }
     }

    // lấy danh sách hoạt động của người dùng theo postId
    @GetMapping("/post/comment/{postId}")
    public ResponseEntity<?> getActivitiesByPostIdAndActivityType(@PathVariable ObjectId postId) {
        if (postId == null) {
            return ResponseEntity.badRequest().body("Post ID cannot be null");
        }

        try {
            return ResponseEntity.ok(postUserActivityService.findByPostIdAndActivityType(postId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving post user activities: " + e.getMessage());
        }
    }

}