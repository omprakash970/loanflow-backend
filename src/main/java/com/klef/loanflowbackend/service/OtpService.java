package com.klef.loanflowbackend.service;

import com.klef.loanflowbackend.entity.Otp;
import com.klef.loanflowbackend.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final OtpRepository otpRepository;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final int OTP_LENGTH = 6;
    private static final long OTP_EXPIRY_MINUTES = 10;

    /**
     * Generate and send OTP to email using Gmail SMTP
     */
    public void sendOtp(String email) {
        try {
            // Generate OTP
            String otp = generateOtp();

            // Save OTP to database (separate transaction)
            saveOtpRecord(email, otp);

            // Send OTP via Gmail SMTP (outside transaction)
            sendOtpViaGmail(email, otp);

            log.info("OTP sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send OTP to {}: {}", email, e.getMessage());
            throw new RuntimeException("Failed to send OTP: " + e.getMessage());
        }
    }

    /**
     * Save OTP record to database (transactional)
     */
    @Transactional
    private void saveOtpRecord(String email, String otp) {
        try {
            // Delete old OTP if exists
            Optional<Otp> existingOtp = otpRepository.findByEmail(email);
            if (existingOtp.isPresent()) {
                otpRepository.delete(existingOtp.get());
            }
        } catch (Exception e) {
            log.warn("Could not delete old OTP for {}: {}", email, e.getMessage());
        }

        // Save new OTP to database
        Otp otpRecord = Otp.builder()
                .email(email)
                .otp(otp)
                .isVerified(false)
                .build();
        otpRepository.save(otpRecord);
    }

    /**
     * Verify OTP
     */
    @Transactional
    public boolean verifyOtp(String email, String otp) {
        Optional<Otp> otpRecord = otpRepository.findByEmail(email);

        if (otpRecord.isEmpty()) {
            log.warn("OTP not found for email: {}", email);
            throw new IllegalArgumentException("OTP not found. Please request a new OTP.");
        }

        Otp record = otpRecord.get();

        // Check if OTP is expired
        if (System.currentTimeMillis() > record.getExpiresAt()) {
            log.warn("OTP expired for email: {}", email);
            otpRepository.delete(record);
            throw new IllegalArgumentException("OTP has expired. Please request a new OTP.");
        }

        // Check if OTP matches
        if (!record.getOtp().equals(otp)) {
            log.warn("Invalid OTP for email: {}", email);
            throw new IllegalArgumentException("Invalid OTP. Please try again.");
        }

        // Mark as verified
        record.setIsVerified(true);
        otpRepository.save(record);

        log.info("OTP verified successfully for: {}", email);
        return true;
    }

    /**
     * Check if OTP is verified for email
     */
    public boolean isOtpVerified(String email) {
        Optional<Otp> otpRecord = otpRepository.findByEmail(email);
        return otpRecord.isPresent() && otpRecord.get().getIsVerified();
    }

    /**
     * Clean up OTP record after successful registration
     */
    @Transactional
    public void cleanupOtp(String email) {
        try {
            Optional<Otp> otpRecord = otpRepository.findByEmail(email);
            if (otpRecord.isPresent()) {
                otpRepository.delete(otpRecord.get());
            }
        } catch (Exception e) {
            log.warn("Could not cleanup OTP for {}: {}", email, e.getMessage());
        }
        log.info("OTP cleaned up for: {}", email);
    }

    /**
     * Generate random 6-digit OTP
     */
    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    /**
     * Send OTP via Gmail SMTP
     */
    private void sendOtpViaGmail(String email, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Your LoanFlow OTP for Registration");
            helper.setText(buildOtpEmailHtml(otp), true); // true = HTML content

            mailSender.send(message);
            log.info("OTP email sent successfully to: {} via Gmail SMTP", email);
        } catch (MessagingException e) {
            log.error("Error sending OTP via Gmail SMTP: {}", e.getMessage());
            throw new RuntimeException("Failed to send OTP email: " + e.getMessage());
        }
    }

    /**
     * Build HTML email template for OTP
     */
    private String buildOtpEmailHtml(String otp) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; background-color: #f4f4f4; }\n" +
                "        .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }\n" +
                "        .header { text-align: center; color: #333; }\n" +
                "        .otp-box { background-color: #f9f9f9; border: 2px solid #007bff; padding: 20px; margin: 20px 0; text-align: center; border-radius: 8px; }\n" +
                "        .otp-code { font-size: 32px; font-weight: bold; color: #007bff; letter-spacing: 5px; }\n" +
                "        .footer { text-align: center; color: #666; font-size: 12px; margin-top: 20px; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <div class=\"header\">\n" +
                "            <h2>LoanFlow Registration</h2>\n" +
                "            <p>Your OTP for registration</p>\n" +
                "        </div>\n" +
                "        <p>Hello,</p>\n" +
                "        <p>Thank you for registering with LoanFlow. Use the OTP below to complete your registration:</p>\n" +
                "        <div class=\"otp-box\">\n" +
                "            <div class=\"otp-code\">" + otp + "</div>\n" +
                "        </div>\n" +
                "        <p style=\"color: #666; font-size: 14px;\">This OTP is valid for 10 minutes. Please do not share this OTP with anyone.</p>\n" +
                "        <div class=\"footer\">\n" +
                "            <p>If you did not request this email, please ignore it.</p>\n" +
                "            <p>&copy; 2026 LoanFlow. All rights reserved.</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }
}

