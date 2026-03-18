package com.parkify.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Random;

@Service
public class SmsService {

    @Value("${fast2sms.api.key}")
    private String apiKey;

    // Generate 6 digit OTP
    public String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        System.out.println(">>> generateOtp() called, OTP = " + otp);
        return String.valueOf(otp);
    }

    // Send OTP via Fast2SMS
    public boolean sendOtp(String phone, String otp) {
        try {
            String urlStr = "https://www.fast2sms.com/dev/bulkV2"
                + "?authorization=" + apiKey
                + "&route=v"
                + "&variables_values=" + otp
                + "&flash=0"
                + "&numbers=" + phone;
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("cache-control", "no-cache");
            int responseCode = conn.getResponseCode();
            System.out.println("Fast2SMS response: " + responseCode);
            return responseCode == 200;
        } catch (Exception e) {
            System.out.println("SMS error: " + e.getMessage());
            return false;
        }
    }
}