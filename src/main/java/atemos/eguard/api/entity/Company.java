package atemos.eguard.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 업체 정보를 나타내는 엔티티 클래스입니다.
 * 이 엔티티는 업체의 기본 정보와 관련된 다양한 필드를 포함합니다.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Company {
    /**
     * 업체의 고유 식별자입니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 공장 엔티티와의 양방향 매핑입니다.
     */
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Factory> factories;
    /**
     * 사업자 번호입니다.
     * - 유일성을 보장합니다.
     * - 최대 10자리 숫자 형식입니다.
     */
    @Column(nullable = false, unique = true, length = 10)
    private String businessNumber;
    /**
     * 업체명입니다.
     * - 최대 50자까지 허용됩니다.
     */
    @Column(nullable = false, length = 50)
    private String name;
    /**
     * 업체의 이메일 주소입니다.
     * - 유일성을 보장합니다.
     */
    @Column(nullable = false, unique = true, length = 50)
    private String email;
    /**
     * 업체의 연락처입니다.
     * - 11자리 숫자 형식입니다.
     */
    @Column(nullable = false, unique = true, length = 11)
    private String phoneNumber;
    /**
     * 업체의 주소입니다.
     * - 최대 255자까지 허용됩니다.
     */
    @Column(nullable = false)
    private String address;
    /**
     * 업체의 상세주소입니다.
     * - 최대 255자까지 허용됩니다.
     */
    @Column(nullable = false)
    private String addressDetail;
    /**
     * 업체가 생성된 일시입니다.
     * - 수정할 수 없습니다.
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    /**
     * 업체 정보가 마지막으로 수정된 일시입니다.
     */
    @LastModifiedDate
    private LocalDateTime updatedAt;
}