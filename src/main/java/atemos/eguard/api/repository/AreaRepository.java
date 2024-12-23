package atemos.eguard.api.repository;

import atemos.eguard.api.entity.Area;
import atemos.eguard.api.entity.Factory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * Area 엔티티에 대한 데이터 접근을 제공하는 리포지토리 인터페이스입니다.
 * 이 인터페이스는 JPA의 기본 CRUD 기능을 제공하며, 공장 이름 및 회사 ID로 공장을 조회하는 기능을 추가로 지원합니다.
 */
public interface AreaRepository extends JpaRepository<Area, Long>, JpaSpecificationExecutor<Area> {
    /**
     * 특정 공장에 속한 구역 목록을 조회하는 메서드입니다.
     *
     * @param factory 조회할 공장
     * @return 해당 공장에 속한 구역 목록
     */
    List<Area> findByFactory(Factory factory);
}