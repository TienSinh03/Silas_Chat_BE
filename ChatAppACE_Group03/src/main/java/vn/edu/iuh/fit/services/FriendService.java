/**
 * @ (#) FriendService.java      4/14/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.services;

import org.bson.types.ObjectId;
import vn.edu.iuh.fit.dtos.response.FriendResponse;

import java.util.List;

/*
 * @description:
 * @author: Sinh Phan Tien
 * @date: 4/14/2025
 */
public interface FriendService {
    List<FriendResponse> getFriends(ObjectId userId);
    public boolean unfriend(String token,ObjectId friendId);
}
