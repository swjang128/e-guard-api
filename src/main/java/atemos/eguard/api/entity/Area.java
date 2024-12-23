package atemos.eguard.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 구역 정보를 나타내는 엔티티 클래스입니다.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Area {
    /**
     * 구역의 고유 식별자입니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 이 구역이 속한 공장입니다.
     * - 구역과 공장은 다대일 관계입니다.
     * - 공장 정보는 지연 로딩 방식으로 불러옵니다.
     */
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinColumn(name = "factory_id")
    private Factory factory;
    /**
     * 구역명입니다.
     * - 최대 50자까지 허용됩니다.
     */
    @Column(nullable = false, length = 50)
    private String name;
    /**
     * 구역의 위치입니다.
     * - 최대 255자까지 허용됩니다.
     */
    @Column(nullable = false)
    private String location;
    /**
     * 구역의 면적입니다.(단위: ㎡)
     */
    @Column(precision = 12, scale = 6)
    private BigDecimal usableSize;
    /**
     * 위도입니다.
     * - 소수점 처리 가능을 위해 BigDecimal 사용
     */
    @Column(precision = 10, scale = 6)
    private BigDecimal latitude;
    /**
     * 경도입니다.
     * - 소수점 처리 가능을 위해 BigDecimal 사용
     */
    @Column(precision = 10, scale = 6)
    private BigDecimal longitude;
    /**
     * 구역의 2D 도면 파일명입니다.
     * - 최대 255자까지 허용됩니다.
     */
    @Column
    private String plan2DFilePath;
    /**
     * 구역의 3D 도면 파일명입니다.
     * - 최대 255자까지 허용됩니다.
     */
    @Column
    private String plan3DFilePath;
    /**
     * 해당 구역에 대한 메모
     */
    @Lob
    private String memo;
    /**
     * 구역 정보가 생성된 일시입니다.
     * - 수정할 수 없습니다.
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    /**
     * 구역 정보가 마지막으로 수정된 일시입니다.
     */
    @LastModifiedDate
    private LocalDateTime updatedAt;
}