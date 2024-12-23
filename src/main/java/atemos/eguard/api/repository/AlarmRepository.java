package atemos.eguard.api.repository;

import atemos.eguard.api.entity.Alarm;
import atemos.eguard.api.entity.Employee;
import atemos.eguard.api.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Alarm 엔티티에 대한 데이터 접근을 제공하는 리포지토리 인터페이스입니다.
 * 이 인터페이스는 JPA의 기본 CRUD 기능과 스펙을 통한 쿼리 실행을 지원합니다.
 */
public interface AlarmRepository extends JpaRepository<Alarm, Long>, JpaSpecificationExecutor<Alarm> {
    /**
     * 주어진 근로자와 사건에 대한 알람이 이미 존재하는지 확인합니다.
     *
     * @param employee 알람을 받을 근로자
     * @param event 사건 정보
     * @return 알람이 존재하면 true, 그렇지 않으면 false 반환
     */
    boolean existsByEmployeeAndEvent(Employee employee, Event event);
}