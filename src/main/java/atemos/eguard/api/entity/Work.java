package atemos.eguard.api.entity;

import atemos.eguard.api.domain.WorkStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 작업 정보를 나타내는 엔티티 클래스입니다.
 * 작업의 이름, 작업에 투입된 근로자 목록, 작업이 이루어지는 구역, 작업의 상태를 포함합니다.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Work {
    /**
     * 작업의 고유 식별자입니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 작업이 이루어지는 구역입니다.
     * - 작업과 구역은 다대일 관계입니다.
     * - 구역 정보는 지연 로딩 방식으로 불러옵니다.
     */
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinColumn(name = "area_id", nullable = false)
    private Area area;
    /**
     * 작업에 투입된 근로자 목록입니다.
     * - 작업과 근로자는 다대다 관계입니다.
     * - 연관된 근로자 목록은 즉시 로딩됩니다.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "work_employee",
            joinColumns = @JoinColumn(name = "work_id"),
            inverseJoinColumns = @JoinColumn(name = "employee_id")
    )
    private List<Employee> employees;
    /**
     * 작업 이름입니다.
     * 예: "자동차 외장 부품 생산"
     */
    @Column(nullable = false, length = 100)
    private String name;
    /**
     * 현재 작업의 상태를 나타냅니다.
     * - 예: 대기 중, 진행 중, 완료됨, 취소됨
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private WorkStatus status;
    /**
     * 작업 정보가 생성된 일시입니다.
     * - 수정할 수 없습니다.
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    /**
     * 작업 정보가 마지막으로 수정된 일시입니다.
     */
    @LastModifiedDate
    private LocalDateTime updatedAt;
}