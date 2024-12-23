package atemos.eguard.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * EncryptUtil 클래스는 AES 알고리즘을 사용해 문자열을 암호화 및 복호화하는 유틸리티 클래스입니다.
 * Spring의 @Value 어노테이션을 통해 주입된 키를 사용하여 암호화 및 복호화를 수행합니다.
 */
@Component
public class EncryptUtil {
    private static final String ALGORITHM = "AES";  // 암호화 알고리즘
    private static final String TRANSFORMATION = "AES";  // 암호화 및 복호화 시 사용할 변환
    private final SecretKey secretKey;

    /**
     * 생성자: Spring의 @Value 어노테이션을 통해 주입된 암호화 키를 사용해 SecretKey 객체를 생성합니다.
     *
     * @param key Base64로 인코딩된 암호화 키
     */
    public EncryptUtil(@Value("${aes-256-key}") String key) {
        this.secretKey = new SecretKeySpec(Base64.getDecoder().decode(key), ALGORITHM);
    }

    /**
     * 문자열을 AES 알고리즘을 사용해 암호화한 후, Base64로 인코딩하여 반환합니다.
     *
     * @param data 암호화할 데이터 (평문)
     * @return 암호화된 데이터 (Base64 인코딩된 문자열)
     */
    public String encrypt(String data) {
        try {
            var cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            var encryptedData = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encryptedData);
        } catch (Exception e) {
            throw new RuntimeException("Error during encryption", e);
        }
    }

    /**
     * Base64로 인코딩된 암호화 데이터를 복호화한 후, 원래의 문자열로 반환합니다.
     *
     * @param encryptedData 암호화된 데이터 (Base64 인코딩된 문자열)
     * @return 복호화된 데이터 (평문)
     */
    public String decrypt(String encryptedData) {
        try {
            var cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            var decodedData = Base64.getDecoder().decode(encryptedData);
            return new String(cipher.doFinal(decodedData));
        } catch (Exception e) {
            throw new RuntimeException("Error during decryption", e);
        }
    }
}