package atemos.eguard.api.repository;

import atemos.eguard.api.domain.EmployeeRole;
import atemos.eguard.api.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * Menu 엔티티에 대한 데이터 접근을 제공하는 리포지토리 인터페이스입니다.
 * 이 인터페이스는 JPA의 기본 CRUD 기능을 제공하며, 스펙을 사용하여 복잡한 조건의 쿼리를 작성할 수 있도록 지원합니다.
 * - 기본 CRUD 작업을 위한 메소드 제공 (저장, 조회, 수정, 삭제)
 * - 스펙을 사용하여 복잡한 조건의 쿼리 작성 지원
 */
public interface MenuRepository extends JpaRepository<Menu, Long>, JpaSpecificationExecutor<Menu> {
    /**
     * 특정 깊이를 가진 메뉴가 존재하는지 확인합니다.
     *
     * @param depth 메뉴의 깊이
     * @return 존재하면 true, 그렇지 않으면 false
     */
    boolean existsByDepth(int depth);
    /**
     * 주어진 역할에 따라 접근 가능한 메뉴들을 조회합니다.
     *
     * @param role 접근할 수 있는 역할
     * @return 접근 가능한 메뉴 목록
     */
    List<Menu> findAllByAccessibleRolesContains(EmployeeRole role);
}