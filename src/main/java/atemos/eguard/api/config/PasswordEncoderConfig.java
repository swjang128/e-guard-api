package atemos.eguard.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 비밀번호 인코더 설정 클래스.
 * 이 클래스는 Spring Security에서 비밀번호를 암호화하기 위해
 * 사용되는 PasswordEncoder를 빈으로 등록합니다.
 * BCryptPasswordEncoder는 비밀번호를 해시하여 보안을 강화합니다.
 * 이를 통해 비밀번호를 안전하게 저장하고 검증할 수 있습니다.
 * 사용 예:
 * {@code *
 *   private final PasswordEncoder passwordEncoder;
 * <p>
 *   public void someMethod() {
 *     String rawPassword = "plainPassword";
 *     String encodedPassword = passwordEncoder.encode(rawPassword);
 *     // encodedPassword를 저장하거나 비교하는 로직
 *   }
 * }
 */
@Configuration
public class PasswordEncoderConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}