package atemos.eguard.api.repository;

import atemos.eguard.api.entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * Setting 엔티티에 대한 데이터 접근을 제공하는 리포지토리 인터페이스입니다.
 * 이 인터페이스는 JPA의 기본 CRUD 기능을 제공하며, 스펙을 사용하여 복잡한 조건의 쿼리를 작성할 수 있도록 지원합니다.
 * - 기본 CRUD 작업을 위한 메소드 제공 (저장, 조회, 수정, 삭제)
 * - 스펙을 사용하여 복잡한 조건의 쿼리 작성 지원
 */
public interface SettingRepository extends JpaRepository<Setting, Long>, JpaSpecificationExecutor<Setting> {
    /**
     * 해당 업체가 사용하는 시스템 설정을 조회합니다.
     * @param companyId 업체 ID
     * @return 시스템 설정
     */
    Optional<Setting> findByCompanyId(Long companyId);
}