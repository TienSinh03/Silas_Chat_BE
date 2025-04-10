/**
 * @ (#) SmsServiceImpl.java      4/10/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import vn.edu.iuh.fit.services.SmsService;
import vn.edu.iuh.fit.utils.FormatPhoneNumber;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/*
 * @description:
 * @author: Sinh Phan Tien
 * @date: 4/10/2025
 */
@Service
public class SmsServiceImpl implements SmsService {
    @Autowired
    private SnsClient snsClient;


    @Override
    public String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
    @Override
    public void sendOtp(String phoneNumber) {
        String otp = generateOtp();
        String message = "Mã OTP của bạn là: " + otp + ". Vui lòng không chia sẻ mã này.";

        Map<String, MessageAttributeValue> attribute = new HashMap<>();
        attribute.put("AWS.SNS.SMS.SenderID", MessageAttributeValue.builder()
                .dataType("String")
                .stringValue("Chat")
                .build());

        attribute.put("AWS.SNS.SMS.SMSType", MessageAttributeValue.builder()
                .dataType("String")
                .stringValue("Transactional")
                .build());

        System.out.println("Sending OTP: " + otp);
        System.out.println(FormatPhoneNumber.formatPhoneNumberTo84(phoneNumber));

        PublishRequest request = PublishRequest.builder()
                .message(message)
                .phoneNumber(FormatPhoneNumber.formatPhoneNumberTo84(phoneNumber))
                .messageAttributes(attribute)
                .build();

        snsClient.publish(request);
    }
}
