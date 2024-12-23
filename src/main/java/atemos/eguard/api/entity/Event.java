package atemos.eguard.api.entity;

import atemos.eguard.api.domain.AreaIncident;
import atemos.eguard.api.domain.EmployeeIncident;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Employee와 Area에 발생한 사건 내역을 저장하는 엔티티 클래스입니다.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Event {
    /**
     * 구역의 고유 식별자입니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 이 사건이 발생한 근로자입니다. null일 수 있습니다.
     * - 사건과 근로자는 다대일 관계입니다.
     * - 근로자 정보는 지연 로딩 방식으로 불러옵니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;
    /**
     * 이 사건이 발생한 구역입니다. null일 수 있습니다.
     * - 사건과 구역은 다대일 관계입니다.
     * - 구역 정보는 지연 로딩 방식으로 불러옵니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id")
    private Area area;
    /**
     * 근로자에게 발생한 사건 유형입니다.
     */
    @Column
    @Enumerated(EnumType.STRING)
    private EmployeeIncident employeeIncident;
    /**
     * 구역에서 발생한 사건 유형입니다.
     */
    @Column
    @Enumerated(EnumType.STRING)
    private AreaIncident areaIncident;
    /**
     * 사건 해결 여부입니다.
     */
    @Column(nullable = false)
    @ColumnDefault("false")
    private Boolean resolved;
    /**
     * 사건 정보가 생성된 일시입니다.
     * - 수정할 수 없습니다.
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    /**
     * 사건 정보가 마지막으로 수정된 일시입니다.
     */
    @LastModifiedDate
    private LocalDateTime updatedAt;
}