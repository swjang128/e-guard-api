package atemos.eguard.api.entity;

import atemos.eguard.api.domain.IndustryType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 공장 정보를 나타내는 엔티티 클래스입니다.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Factory {
    /**
     * 공장의 고유 식별자입니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 이 공장이 속한 업체입니다.
     * - 공장과 업체는 다대일 관계입니다.
     * - 업체 정보는 지연 로딩 방식으로 불러옵니다.
     */
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinColumn(name = "company_id")
    private Company company;
    /**
     * 공장 엔티티와의 양방향 매핑입니다.
     */
    @OneToMany(mappedBy = "factory", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Employee> employees;
    /**
     * 공장명입니다.
     * - 최대 50자까지 허용됩니다.
     */
    @Column(nullable = false, length = 50)
    private String name;
    /**
     * 공장의 주소입니다.
     * - 최대 255자까지 허용됩니다.
     */
    @Column(nullable = false)
    private String address;
    /**
     * 공장의 상세주소입니다.
     * - 최대 255자까지 허용됩니다.
     */
    @Column(nullable = false)
    private String addressDetail;
    /**
     * 공장이 차지하는 전체 면적입니다.(단위: ㎡)
     */
    @Column(precision = 12, scale = 6)
    private BigDecimal totalSize;
    /**
     * 공장의 건물이 차지하는 면적입니다.(단위: ㎡)
     */
    @Column(precision = 12, scale = 6)
    private BigDecimal structureSize;
    /**
     * 공장의 업종입니다.
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private IndustryType industryType;
    /**
     * 공장 정보가 생성된 일시입니다.
     * - 수정할 수 없습니다.
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    /**
     * 공장 정보가 마지막으로 수정된 일시입니다.
     */
    @LastModifiedDate
    private LocalDateTime updatedAt;
}