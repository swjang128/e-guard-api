package atemos.eguard.api.repository;

import atemos.eguard.api.entity.Area;
import atemos.eguard.api.entity.Employee;
import atemos.eguard.api.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Event 엔티티에 대한 데이터 접근을 제공하는 리포지토리 인터페이스입니다.
 * 이 인터페이스는 JPA의 기본 CRUD 기능을 제공하며, 공장 이름 및 회사 ID로 공장을 조회하는 기능을 추가로 지원합니다.
 */
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {
    /**
     * 주어진 구역에서 발생한 미해결 사건 중 가장 최근에 생성된 사건을 조회합니다.
     *
     * @param area 조회할 구역
     * @param resolved 사건이 해결되었는지 여부 (false이면 미해결 사건)
     * @return 주어진 조건에 해당하는 가장 최근 사건
     */
    Optional<Event> findTopByAreaAndResolvedOrderByCreatedAtDesc(Area area, boolean resolved);
    /**
     * 주어진 근로자에 대한 미해결 사건 중 가장 최근에 생성된 사건을 조회합니다.
     *
     * @param employee 조회할 근로자
     * @param resolved 사건이 해결되었는지 여부 (false이면 미해결 사건)
     * @return 주어진 조건에 해당하는 가장 최근 사건
     */
    Optional<Event> findTopByEmployeeAndResolvedOrderByCreatedAtDesc(Employee employee, boolean resolved);
    /**
     * 특정 구역에서 주어진 기간 내에 발생한 사건 목록을 조회하는 메서드입니다.
     *
     * @param area 조회할 구역
     * @param start 시작 시간 (포함)
     * @param end 종료 시간 (포함)
     * @return 해당 구역에서 주어진 기간 동안 발생한 사건 목록
     */
    List<Event> findByAreaAndCreatedAtBetween(Area area, LocalDateTime start, LocalDateTime end);
}