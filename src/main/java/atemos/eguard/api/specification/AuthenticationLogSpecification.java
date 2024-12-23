package atemos.eguard.api.specification;

import atemos.eguard.api.dto.LogDto;
import atemos.eguard.api.entity.AuthenticationLog;
import org.springframework.data.jpa.domain.Specification;

/**
 * AuthenticationLogSpecification 클래스는 AuthenticationLog 엔티티에 대한 동적 쿼리를 생성하기 위한 스펙 클래스입니다.
 * 다양한 필터링 조건을 지원하며, 주어진 조건에 따라 인증/인가 로그를 조회하는 데 사용됩니다.
 */
public class AuthenticationLogSpecification {
    /**
     * 주어진 LogDto.ReadAuthenticationLogRequest 객체를 기반으로 AuthenticationLog 엔티티에 대한 Specification을 생성합니다.
     *
     * @param readAuthenticationLogRequest 인증/인가 로그 조회 조건을 포함하는 데이터 전송 객체
     * @return 주어진 조건에 맞는 AuthenticationLog 엔티티를 조회하기 위한 Specification 객체
     */
    public static Specification<AuthenticationLog> findWith(LogDto.ReadAuthenticationLogRequest readAuthenticationLogRequest) {
        return (root, query, criteriaBuilder) -> {
            // 기본 조건 초기화
            var predicate = criteriaBuilder.conjunction();
            // AuthenticationLog ID
            if (readAuthenticationLogRequest.getAuthenticationLogIds() != null && !readAuthenticationLogRequest.getAuthenticationLogIds().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, root.get("id").in(readAuthenticationLogRequest.getAuthenticationLogIds()));
            }
            // 근로자 ID
            if (readAuthenticationLogRequest.getEmployeeIds() != null && !readAuthenticationLogRequest.getEmployeeIds().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, root.get("employee").get("id").in(readAuthenticationLogRequest.getEmployeeIds()));
            }
            // 업체 ID
            if (readAuthenticationLogRequest.getCompanyIds() != null && !readAuthenticationLogRequest.getCompanyIds().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, root.get("company").get("id").in(readAuthenticationLogRequest.getCompanyIds()));
            }
            // 엔드포인트
            if (readAuthenticationLogRequest.getRequestUri() != null && !readAuthenticationLogRequest.getRequestUri().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(root.get("requestUri"), "%" + readAuthenticationLogRequest.getRequestUri() + "%"));
            }
            // HTTP 메서드
            if (readAuthenticationLogRequest.getHttpMethods() != null && !readAuthenticationLogRequest.getHttpMethods().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, root.get("httpMethod").in(readAuthenticationLogRequest.getHttpMethods()));
            }
            // 응답 코드
            if (readAuthenticationLogRequest.getStatusCode() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("statusCode"), readAuthenticationLogRequest.getStatusCode()));
            }
            // 클라이언트 IP
            if (readAuthenticationLogRequest.getClientIp() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("clientIp"), readAuthenticationLogRequest.getClientIp()));
            }
            // 조회 시작일시
            if (readAuthenticationLogRequest.getSearchStartTime() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("requestTime"), readAuthenticationLogRequest.getSearchStartTime()));
            }
            // 조회 종료일시
            if (readAuthenticationLogRequest.getSearchEndTime() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get("requestTime"), readAuthenticationLogRequest.getSearchEndTime()));
            }
            return predicate;
        };
    }
}