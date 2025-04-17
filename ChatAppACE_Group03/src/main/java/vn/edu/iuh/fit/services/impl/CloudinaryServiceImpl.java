/**
 * @ (#) CloudinaryServiceImpl.java      4/10/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.services.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.config.CloudinaryConfig;
import vn.edu.iuh.fit.services.CloudinaryService;

import java.util.Map;

/*
 * @description:
 * @author: Sinh Phan Tien
 * @date: 4/10/2025
 */
@Service
public class CloudinaryServiceImpl implements CloudinaryService {
    @Autowired
    private Cloudinary cloudinary;
    @Override
    public String uploadImage(MultipartFile file) {
        try {
            Map<?,?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
//            System.out.println("Cloudinary result: " + result);
            return result.get("secure_url").toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload image", e);
        }
    }

    @Override
    public void deleteImage(String publicId) {
        // Delete the image from Cloudinary
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete image", e);
        }
    }
}
