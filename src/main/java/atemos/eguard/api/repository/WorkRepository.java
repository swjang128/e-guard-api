package atemos.eguard.api.repository;

import atemos.eguard.api.domain.WorkStatus;
import atemos.eguard.api.entity.Area;
import atemos.eguard.api.entity.Employee;
import atemos.eguard.api.entity.Work;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * Work 엔티티에 대한 데이터 접근을 제공하는 리포지토리 인터페이스입니다.
 * 이 인터페이스는 JPA의 기본 CRUD 기능을 제공하며, 공장 이름 및 회사 ID로 공장을 조회하는 기능을 추가로 지원합니다.
 */
public interface WorkRepository extends JpaRepository<Work, Long>, JpaSpecificationExecutor<Work> {
    /**
     * 주어진 구역에 속한 모든 작업을 조회합니다.
     *
     * @param area 구역 엔티티
     * @return 해당 구역에 속한 작업 목록
     */
    List<Work> findByArea(Area area);
    /**
     * 특정 근로자가 지정된 상태 목록에 해당하는 작업에 속하고 있는지 확인합니다.
     *
     * @param employee 확인하려는 근로자 엔티티
     * @param statuses 확인할 작업 상태 목록 (예: PENDING, IN_PROGRESS)
     * @return 해당 근로자가 지정된 상태의 작업을 보유하고 있는 경우 true, 그렇지 않으면 false
     */
    boolean existsByEmployeesContainingAndStatusIn(Employee employee, List<WorkStatus> statuses);
}