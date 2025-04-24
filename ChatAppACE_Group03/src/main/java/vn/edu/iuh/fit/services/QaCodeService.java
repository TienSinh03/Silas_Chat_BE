package vn.edu.iuh.fit.services;

import org.bson.types.ObjectId;
import vn.edu.iuh.fit.entities.User;

public interface QaCodeService {
    void saveQaCode(String sessionId, Boolean status, ObjectId userId);

    Boolean checkStatus(String sessionId);

    void updateStatus(String sessionId, Boolean status);

    //    // TIM KIEM IDUSER THEO SESSIONID
    User findUserIdBySessionId(String sessionId);
}
