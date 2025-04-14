package vn.edu.iuh.fit.services;

import vn.edu.iuh.fit.entities.Conservation;

public interface ConservationService {
    Conservation createPrivateConservation(String senderId, String receiverId);
}