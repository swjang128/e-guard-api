package atemos.eguard.api.entity;

import atemos.eguard.api.domain.AuthenticationStatus;
import atemos.eguard.api.domain.EmployeeRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 근로자 정보를 저장하는 엔티티 클래스입니다.
 * 이 클래스는 데이터베이스의 `employee` 테이블과 매핑됩니다.
 */
@Entity
@Table(name = "employee", indexes = {
        @Index(name = "idx_employee_email", columnList = "email")
})
@DynamicUpdate
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Employee {
    /**
     * 근로자의 고유 식별자입니다.
     * - 데이터베이스에서 자동 생성됩니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 이 근로자가 작업 중인 공장입니다.
     * - 근로자와 공장은 다대일 관계입니다.
     * - 공장 정보는 지연 로딩 방식으로 불러옵니다.
     */
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinColumn(name = "factory_id")
    private Factory factory;
    /**
     * 근로자가 참여한 작업 목록입니다.
     * - 근로자와 작업은 다대다 관계입니다.
     * - 연관된 작업 목록은 지연 로딩 방식으로 불러옵니다.
     * - 이 필드는 `Work` 엔티티의 `employees` 필드와 매핑됩니다.
     */
    @ManyToMany(mappedBy = "employees", fetch = FetchType.LAZY)
    private List<Work> works;
    /**
     * 근로자의 이름입니다.(Base64 암호화되어 저장합니다.)
     */
    @Column(nullable = false)
    private String name;
    /**
     * 근로자의 이메일 주소입니다.(Base64 암호화되어 저장합니다.)
     */
    @Column(nullable = false, unique = true)
    private String email;
    /**
     * 근로자의 전화번호입니다.(Base64 암호화되어 저장합니다.)
     */
    @Column(nullable = false, unique = true)
    private String phoneNumber;
    /**
     * 근로자의 비밀번호입니다.
     * - 반드시 입력해야 하며, 보안상의 이유로 setter 메소드가 제공됩니다.
     */
    @Setter
    @Column(nullable = false)
    private String password;
    /**
     * 비밀번호가 틀린 횟수를 기록합니다.
     * - 기본값은 0으로 설정됩니다.
     * - 5회 이상 틀리면 Status를 LOCKED로 변경합니다.
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer failedLoginAttempts = 0;
    /**
     * 근로자의 역할을 정의합니다.
     * - 역할은 문자열로 저장되며, 예를 들어 ADMIN, MANAGER, WORKER 등이 있습니다.
     * - 필수 항목입니다.
     */
    @Column(nullable = false, length = 7)
    @Enumerated(EnumType.STRING)
    private EmployeeRole role;
    /**
     * 계정의 상태입니다.
     * - 기본적으로 ACTIVE로 저장됩니다.
     */
    @Column(nullable = false, length = 14)
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private AuthenticationStatus authenticationStatus = AuthenticationStatus.ACTIVE;
    /**
     * 근로자의 사원번호입니다.
     * - 고유한 번호로 유니크 제약이 적용됩니다.
     * - 최대 40자까지 허용됩니다.
     * - 예시로는 업체명+Employee.ID로 생성됩니다.
     */
    @Column(nullable = false, unique = true, length = 40)
    private String employeeNumber;
    /**
     * 근로자 정보가 생성된 날짜와 시간입니다.
     * - 데이터베이스에 처음 저장될 때 자동으로 설정됩니다.
     * - 이후에는 수정할 수 없습니다.
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    /**
     * 근로자 정보가 마지막으로 수정된 날짜와 시간입니다.
     * - 데이터가 변경될 때마다 자동으로 업데이트됩니다.
     */
    @LastModifiedDate
    private LocalDateTime updatedAt;
    /**
     * 근로자의 권한 정보를 반환합니다.
     * 스프링 시큐리티에서 사용됩니다.
     *
     * @return 권한 정보 컬렉션
     */
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }
}