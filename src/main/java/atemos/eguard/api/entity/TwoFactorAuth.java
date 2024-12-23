package atemos.eguard.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * TwoFactorAuth 엔티티는 2단계 인증(2FA)을 위한 인증번호 발송 및 검증 내역을 관리합니다.
 * 근로자가 인증번호를 요청하거나 입력한 인증번호를 검증할 때, 이 엔티티가 사용됩니다.
 * - 인증번호는 근로자가 로그인 시 2단계 인증을 위해 입력해야 하며, 실패 시 실패 횟수가 증가합니다.
 * - 인증이 성공하면 해당 내역이 isVerified 필드에 기록되며, 실패한 경우 시도 횟수가 기록됩니다.
 * - 인증번호가 발송된 시각은 createdDate 필드에 기록되며, 발송 후 인증번호의 유효 기간을 계산할 때 사용됩니다.
 */
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "two_factor_auth")
public class TwoFactorAuth {
    /**
     * 2FA 인증번호 발송 내역의 고유 식별자입니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 인증 번호입니다. 근로자가 입력해야 하는 코드입니다.
     */
    @Column(nullable = false, length = 10)
    private String authCode;
    /**
     * 인증번호를 발송한 대상자의 User 엔티티와의 관계를 나타냅니다.
     * User 엔티티와는 다대일 관계를 가집니다.
     */
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    /**
     * 인증 시도 횟수를 저장합니다. 실패할 때마다 증가하며, 일정 횟수 이상 실패하면 추가 조치를 취합니다.
     */
    @Column(nullable = false)
    @Builder.Default
    private int failedAttempts = 0;
    /**
     * 인증 성공 여부를 저장합니다. 기본값은 false이며, 성공 시 true로 업데이트됩니다.
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean isVerified = false;
    /**
     * 인증번호가 생성된 시간입니다. 엔티티가 생성될 때 자동으로 설정됩니다.
     * 이 값은 발송된 인증번호의 유효 시간을 계산하는 데 사용됩니다.
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}