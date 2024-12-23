package atemos.eguard.api.repository;

import atemos.eguard.api.entity.Company;
import atemos.eguard.api.entity.Factory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

/**
 * Factory 엔티티에 대한 데이터 접근을 제공하는 리포지토리 인터페이스입니다.
 * 이 인터페이스는 JPA의 기본 CRUD 기능을 제공하며, 공장 이름 및 회사 ID로 공장을 조회하는 기능을 추가로 지원합니다.
 */
public interface FactoryRepository extends JpaRepository<Factory, Long>, JpaSpecificationExecutor<Factory> {
    /**
     * 공장의 이름으로 공장 정보를 조회하는 메서드입니다.
     * @param name 공장 이름
     * @return 공장 정보
     */
    Optional<Factory> findByName(String name);
    /**
     * 특정 업체에 속한 공장 목록을 조회하는 메서드입니다.
     *
     * @param company 조회할 회사
     * @return 해당 업체에 속한 공장 목록
     */
    List<Factory> findByCompany(Company company);
}