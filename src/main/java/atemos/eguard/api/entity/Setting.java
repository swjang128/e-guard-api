package atemos.eguard.api.entity;

import atemos.eguard.api.domain.TwoFactoryAuthenticationMethod;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 시스템 설정을 관리하는 엔티티입니다. ADMIN 권한 사용자가 관리할 수 있는 다양한 시스템 설정을 포함합니다.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Setting {
    /**
     * 설정의 고유 식별자입니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 이 시스템 설정을 사용하는 업체입니다.
     * - 시스템 설정과 업체는 일대일 관계입니다.
     * - 업체 정보는 지연 로딩 방식으로 불러옵니다.
     */
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinColumn(name = "company_id")
    private Company company;
    /**
     * 한 업체가 등록 가능한 최대 공장 수
     */
    @Column(nullable = false)
    @Positive
    private Integer maxFactoriesPerCompany;
    /**
     * 한 공장에 등록 가능한 최대 구역 수
     */
    @Column(nullable = false)
    @Positive
    private Integer maxAreasPerFactory;
    /**
     * 한 공장에 등록 가능한 최대 근로자 수
     */
    @Column(nullable = false)
    @Positive
    private Integer maxEmployeesPerFactory;
    /**
     * 한 구역에 등록 가능한 최대 작업 수
     */
    @Column(nullable = false)
    @Positive
    private Integer maxWorksPerArea;
    /**
     * 한 작업에 등록 가능한 최대 근로자 수
     */
    @Column(nullable = false)
    @Positive
    private Integer maxEmployeesPerWork;
    /**
     * 2차 인증 여부 (true: 사용, false: 미사용)
     */
    @Column(nullable = false)
    private Boolean twoFactorAuthenticationEnabled;
    /**
     * 2차 인증 방법 (EMAIL, SMS 등)
     */
    @Column(length = 50)
    @Enumerated(EnumType.STRING)
    private TwoFactoryAuthenticationMethod twoFactorAuthenticationMethod;
    /**
     * 설정이 생성된 날짜와 시간입니다.
     * - 데이터베이스에 처음 저장될 때 자동으로 설정됩니다.
     * - 이후에는 수정할 수 없습니다.
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    /**
     * 설정이 마지막으로 수정된 날짜와 시간입니다.
     * - 데이터가 변경될 때마다 자동으로 업데이트됩니다.
     */
    @LastModifiedDate
    private LocalDateTime updatedAt;
}