package atemos.eguard.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 알람을 나타내는 엔티티 클래스입니다.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Alarm {
    /**
     * 알람 ID (기본키)입니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 이 알람을 받는 근로자입니다.
     * - 연관된 근로자를 Lazy로 로딩합니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;
    /**
     * 이 알람이 발생하게된 원인이 된 사건입니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;
    /**
     * 알람 메시지
     */
    private String message;
    /**
     * 알람 읽음 여부를 나타냅니다.
     * - 기본값은 false입니다.
     */
    @Column(nullable = false)
    private Boolean isRead;
    /**
     * 알람 생성 일시입니다.
     * - 업데이트되지 않는 값입니다.
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    /**
     * 알람 수정 일시입니다.
     */
    @LastModifiedDate
    private LocalDateTime updatedAt;
}