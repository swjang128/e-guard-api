package atemos.eguard.api.specification;

import atemos.eguard.api.dto.LogDto;
import atemos.eguard.api.entity.ApiCallLog;
import org.springframework.data.jpa.domain.Specification;

/**
 * ApiCallLogSpecification 클래스는 ApiCallLog 엔티티에 대한 동적 쿼리를 생성하기 위한 스펙 클래스입니다.
 * 다양한 필터링 조건을 지원하며, 주어진 조건에 따라 API 호출 로그를 조회하는 데 사용됩니다.
 */
public class ApiCallLogSpecification {
    /**
     * 주어진 LogDto.ReadApiCallLogRequest 객체를 기반으로 ApiCallLog 엔티티에 대한 Specification을 생성합니다.
     *
     * @param readApiCallLogRequest API 호출 로그 조회 조건을 포함하는 데이터 전송 객체
     * @return 주어진 조건에 맞는 ApiCallLog 엔티티를 조회하기 위한 Specification 객체
     */
    public static Specification<ApiCallLog> findWith(LogDto.ReadApiCallLogRequest readApiCallLogRequest) {
        return (root, query, criteriaBuilder) -> {
            // 기본 조건 초기화
            var predicate = criteriaBuilder.conjunction();
            // ApiCallLog ID
            if (readApiCallLogRequest.getApiCallLogIds() != null && !readApiCallLogRequest.getApiCallLogIds().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, root.get("id").in(readApiCallLogRequest.getApiCallLogIds()));
            }
            // 근로자 ID
            if (readApiCallLogRequest.getEmployeeIds() != null && !readApiCallLogRequest.getEmployeeIds().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, root.get("employee").get("id").in(readApiCallLogRequest.getEmployeeIds()));
            }
            // 업체 ID
            if (readApiCallLogRequest.getCompanyIds() != null && !readApiCallLogRequest.getCompanyIds().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, root.get("company").get("id").in(readApiCallLogRequest.getCompanyIds()));
            }
            // 엔드포인트
            if (readApiCallLogRequest.getRequestUri() != null && !readApiCallLogRequest.getRequestUri().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(root.get("requestUri"), "%" + readApiCallLogRequest.getRequestUri() + "%"));
            }
            // HTTP 메서드
            if (readApiCallLogRequest.getHttpMethods() != null && !readApiCallLogRequest.getHttpMethods().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, root.get("httpMethod").in(readApiCallLogRequest.getHttpMethods()));
            }
            // 응답 코드
            if (readApiCallLogRequest.getStatusCode() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("statusCode"), readApiCallLogRequest.getStatusCode()));
            }
            // 클라이언트 IP
            if (readApiCallLogRequest.getClientIp() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("clientIp"), readApiCallLogRequest.getClientIp()));
            }
            // 조회 시작일시
            if (readApiCallLogRequest.getSearchStartTime() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("requestTime"), readApiCallLogRequest.getSearchStartTime()));
            }
            // 조회 종료일시
            if (readApiCallLogRequest.getSearchEndTime() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get("requestTime"), readApiCallLogRequest.getSearchEndTime()));
            }
            return predicate;
        };
    }
}