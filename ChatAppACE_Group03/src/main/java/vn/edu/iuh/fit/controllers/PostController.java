/**
 * @ (#) PostController.java      5/24/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.controllers;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.entities.Post;
import vn.edu.iuh.fit.services.PostService;

/*
 * @description:
 * @author: Tien Minh Tran
 * @date: 5/24/2025
 */
@RestController
@RequestMapping("/api/v1/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @PostMapping("/save")
    public ResponseEntity<?> savePost(@RequestBody Post post) {
        if (post == null) {
            return ResponseEntity.badRequest().body("Post cannot be null");
        }

        try {
            Post savedPost = postService.savePost(post);  // gửi nguyên object
            return ResponseEntity.ok(savedPost);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error saving post: " + e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getPostsByUser(@PathVariable String userId) {
        try {
            ObjectId objectId = new ObjectId(userId);
            return ResponseEntity.ok(postService.findByUserId(objectId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid userId format");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving posts: " + e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllPosts() {
        try {
            return ResponseEntity.ok(postService.findAll());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving posts: " + e.getMessage());
        }
    }

    @GetMapping("/users-with-posts")
    public ResponseEntity<?> getUsersWithPosts() {
        try {
            return ResponseEntity.ok(postService.findUsersWithPosts());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving users with posts: " + e.getMessage());
        }
    }

}