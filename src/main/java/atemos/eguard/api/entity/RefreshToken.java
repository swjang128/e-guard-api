package atemos.eguard.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Refresh Token 엔티티 클래스.
 * JWT Refresh Token을 데이터베이스에 저장하고 관리합니다.
 */
@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    /**
     * Refresh Token의 고유 식별자입니다.
     * - 데이터베이스에서 자동 생성됩니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * Refresh Token이 발급된 근로자
     */
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    /**
     * 저장된 Refresh Token.
     */
    @Column(nullable = false, unique = true)
    private String token;
    /**
     * Refresh Token의 만료 날짜와 시간.
     */
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Refresh Token이 만료되었는지 확인합니다.
     *
     * @return 만료되었으면 true, 그렇지 않으면 false
     */
    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }
}