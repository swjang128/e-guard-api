package atemos.eguard.api.entity;

import atemos.eguard.api.domain.AllowedHttpMethod;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 인증 및 인가 관련 로그를 나타내는 엔티티 클래스입니다.
 */
@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AuthenticationLog {
    /**
     * 인증/인가 로그 ID (기본키)입니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 이벤트가 발생한 근로자를 나타냅니다.
     * - 연관된 근로자를 Lazy로 로딩합니다.
     */
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinColumn(name = "employee_id")
    private Employee employee;
    /**
     * 호출이 발생한 업체를 나타냅니다.
     * - 연관된 업체를 Lazy로 로딩합니다.
     */
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinColumn(name = "company_id")
    private Company company;
    /**
     * API 호출 경로를 나타냅니다.
     */
    @Column(nullable = false)
    private String requestUri;
    /**
     * HTTP 메서드 (GET, POST 등)를 나타냅니다.
     * - 예: "GET", "POST"
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AllowedHttpMethod httpMethod;
    /**
     * 클라이언트의 IP 주소를 나타냅니다.
     * - 예: "127.0.0.1"
     */
    @Pattern(regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$|^([0-9a-fA-F]{1,4}:){7}([0-9a-fA-F]{1,4}|:)$",
            message = "Invalid IP address format")
    @Column(nullable = false, length = 45)
    private String clientIp;
    /**
     * API 응답 상태 코드를 나타냅니다.
     * - 예: 200, 404, 500
     */
    @Column(nullable = false)
    private Integer statusCode;
    /**
     * 추가적인 메타데이터를 저장합니다.
     * - 예: 오류 메시지, 디버그 정보 등
     */
    @Column(columnDefinition = "TEXT")
    private String metaData;
    /**
     * 이벤트 발생 시간을 자동으로 기록합니다.
     * - 업데이트되지 않는 값입니다.
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime requestTime;
}