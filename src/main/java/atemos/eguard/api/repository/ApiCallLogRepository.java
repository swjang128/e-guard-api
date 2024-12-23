package atemos.eguard.api.repository;

import atemos.eguard.api.entity.ApiCallLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * ApiCallLog 엔티티에 대한 데이터 접근을 제공하는 리포지토리 인터페이스입니다.
 * 이 인터페이스는 JPA의 기본 CRUD 기능과 스펙을 통한 쿼리 실행을 지원합니다.
 */
public interface ApiCallLogRepository extends JpaRepository<ApiCallLog, Long>, JpaSpecificationExecutor<ApiCallLog> {
}