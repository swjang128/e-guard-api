package atemos.eguard.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 블랙리스트에 추가된 토큰을 나타내는 엔티티 클래스입니다.
 * 이 엔티티는 특정 토큰을 블랙리스트에 추가하여 더 이상 사용되지 않도록 관리합니다.
 */
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class BlacklistedToken {
    /**
     * 엔티티의 고유 ID를 저장합니다.
     * - 이 필드는 엔티티의 기본키로 사용됩니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 블랙리스트에 추가된 토큰을 저장합니다.
     * - 이 필드는 고유해야 하며, NULL 값을 허용하지 않습니다.
     */
    @Column(length = 1024, unique = true, nullable = false)
    private String token;
    /**
     * 블랙리스트 토큰이 생성된 날짜와 시간입니다.
     * - 데이터베이스에 처음 저장될 때 자동으로 설정됩니다.
     * - 이후에는 수정할 수 없습니다.
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    /**
     * 블랙리스트 토큰이 마지막으로 수정된 날짜와 시간입니다.
     * - 데이터가 변경될 때마다 자동으로 업데이트됩니다.
     */
    @LastModifiedDate
    private LocalDateTime updatedAt;
}