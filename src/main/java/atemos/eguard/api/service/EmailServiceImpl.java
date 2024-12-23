package atemos.eguard.api.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

/**
 * EmailServiceImpl는 이메일 발송 서비스를 구현한 클래스입니다.
 * JavaMailSender를 이용하여 이메일을 발송하며, 예외 처리를 포함하고 있습니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender javaMailSender;

    /**
     * 이메일을 발송하는 메서드입니다.
     * 수신자의 이메일 주소와 제목, 본문 내용을 받아 이메일을 전송합니다.
     *
     * @param email   수신자 이메일 주소
     * @param subject 이메일 제목
     * @param message 이메일 본문
     */
    @Override
    public void sendEmail(String email, String subject, String message) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            var helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            // 수신자 설정
            helper.setTo(email);
            // 제목 설정 (MIME 인코딩)
            helper.setSubject(MimeUtility.encodeText(subject, "UTF-8", "B"));
            // 본문 설정 (HTML 형식 지원)
            helper.setText(message, true);
            // 이메일 전송
            javaMailSender.send(mimeMessage);
        } catch (MessagingException | UnsupportedEncodingException e) {
            // 예외 처리: 발생한 예외에 따라 적절한 메시지를 로그로 출력하고 RuntimeException으로 래핑하여 던짐
            var errorMsg = switch (e) {
                case MessagingException me -> "An error occurred while sending the email.";
                case UnsupportedEncodingException uee -> "Unsupported encoding was used.";
                default -> "An unknown error occurred.";
            };
            log.error("**** Error occurred while sending email: {}", e.getMessage());
            throw new RuntimeException(errorMsg, e);
        }
    }
}