package com.klef.loanflowbackend.service;

import com.klef.loanflowbackend.entity.Otp;
import com.klef.loanflowbackend.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * Generate and send OTP
     */
    public void sendOtp(String email) {
        try {
            String otp = generateOtp();

            saveOtpRecord(email, otp);

            sendOtpViaGmail(email, otp);

            log.info("OTP sent successfully to {}", email);

        } catch (Exception e) {
            log.error("Failed to send OTP to {} : ", email, e);
            throw new RuntimeException("Failed to send OTP");
        }
    }

    /**
     * Save OTP in DB
     */
    @Transactional
    public void saveOtpRecord(String email, String otp) {
        try {
            Optional<Otp> existing = otpRepository.findByEmail(email);
            existing.ifPresent(otpRepository::delete);
        } catch (Exception e) {
            log.warn("Could not delete old OTP for {}", email);
        }

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
        Optional<Otp> optionalOtp = otpRepository.findByEmail(email);

        if (optionalOtp.isEmpty()) {
            throw new IllegalArgumentException("OTP not found");
        }

        Otp record = optionalOtp.get();

        if (System.currentTimeMillis() > record.getExpiresAt()) {
            otpRepository.delete(record);
            throw new IllegalArgumentException("OTP expired");
        }

        if (!record.getOtp().equals(otp)) {
            throw new IllegalArgumentException("Invalid OTP");
        }

        record.setIsVerified(true);
        otpRepository.save(record);

        return true;
    }

    /**
     * Check verified
     */
    public boolean isOtpVerified(String email) {
        Optional<Otp> optionalOtp = otpRepository.findByEmail(email);
        return optionalOtp.isPresent() && optionalOtp.get().getIsVerified();
    }

    /**
     * Cleanup after register
     */
    @Transactional
    public void cleanupOtp(String email) {
        try {
            Optional<Otp> otp = otpRepository.findByEmail(email);
            otp.ifPresent(otpRepository::delete);
        } catch (Exception e) {
            log.warn("Could not cleanup OTP for {}", email);
        }
    }

    /**
     * Generate 6 digit OTP
     */
    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    /**
     * Send OTP mail
     */
    private void sendOtpViaGmail(String email, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Your LoanFlow OTP for Registration");
            helper.setText(buildOtpEmailHtml(otp), true);

            mailSender.send(message);

            log.info("OTP email sent successfully to {}", email);

        } catch (Exception e) {
            log.error("FULL MAIL ERROR : ", e);
            throw new RuntimeException("Failed to send OTP email");
        }
    }

    /**
     * Email HTML
     */
    private String buildOtpEmailHtml(String otp) {
        return """
            <html>
            <body style='font-family:Arial;padding:20px;background:#f4f4f4'>
                <div style='max-width:600px;margin:auto;background:white;padding:25px;border-radius:10px'>
                    <h2 style='color:#333'>LoanFlow Registration</h2>
                    <p>Your OTP is:</p>

                    <div style='font-size:34px;
                                font-weight:bold;
                                color:#007bff;
                                letter-spacing:6px;
                                padding:20px;
                                text-align:center;
                                border:2px solid #007bff;
                                border-radius:8px'>
                        """ + otp + """
                    </div>

                    <p style='margin-top:20px;color:#666'>
                        Valid for 10 minutes.
                    </p>

                    <p style='color:#999;font-size:12px'>
                        Ignore if not requested.
                    </p>
                </div>
            </body>
            </html>
            """;
    }
}