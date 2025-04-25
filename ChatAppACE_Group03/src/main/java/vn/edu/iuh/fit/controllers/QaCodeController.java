package vn.edu.iuh.fit.controllers;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.entities.QaCode;
import vn.edu.iuh.fit.entities.User;
import vn.edu.iuh.fit.services.QaCodeService;

@RestController
@RequestMapping("/api/v1/qacode")
public class QaCodeController {

    @Autowired
    private QaCodeService qaCodeService;

    @PostMapping("/save")
    public ResponseEntity<QaCode> saveQaCode(@RequestParam("sessionId") String sessionId,
                                           @RequestParam("userId") ObjectId userId, @RequestParam("token") String token) {
        Boolean status = true; // Trạng thái mặc định là true
        QaCode qa = qaCodeService.saveQaCode(sessionId, status, userId, token);
        if (qa == null) {
            return ResponseEntity.badRequest().build(); // Trả về lỗi nếu không thể lưu
        }
        return ResponseEntity.ok(qa); // Trả về đối tượng QaCode đã lưu

    }


    @GetMapping("/status/{sessionId}")
    public ResponseEntity<Boolean> checkStatus(@PathVariable("sessionId") String sessionId) {
        Boolean status = qaCodeService.checkStatus(sessionId);
        if (status == null) {
            return ResponseEntity.notFound().build(); // mã QR không tồn tại
        }
        return ResponseEntity.ok(status);
    }



    // TIM KIEM IDUSER THEO SESSIONID
    @GetMapping("/{sessionId}")
    public ResponseEntity<User> findUserBySessionId(@PathVariable("sessionId") String sessionId) {
        User user = qaCodeService.findUserIdBySessionId(sessionId);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }




}

