package atemos.eguard.api.specification;

import atemos.eguard.api.dto.WorkDto;
import atemos.eguard.api.entity.Work;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

/**
 * Work 엔티티에 대한 동적 쿼리를 생성하는 스펙 클래스입니다.
 * 주어진 조건에 따라 다양한 필터링 옵션을 지원합니다.
 */
public class WorkSpecification {
    /**
     * 주어진 WorkDto.ReadWorkRequest를 기반으로 Work 엔티티에 대한 스펙을 생성합니다.
     *
     * @param readWorkRequest 작업 조회 조건을 포함하는 데이터 전송 객체
     * @return 조건에 맞는 Work 엔티티를 조회하기 위한 Specification 객체
     */
    public static Specification<Work> findWith(WorkDto.ReadWorkRequest readWorkRequest) {
        return (root, query, criteriaBuilder) -> {
            // 기본 조건 생성
            var predicate = criteriaBuilder.conjunction();
            // 작업 ID 목록 필터링
            if (readWorkRequest.getWorkIds() != null && !readWorkRequest.getWorkIds().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, root.get("id").in(readWorkRequest.getWorkIds()));
            }
            // 공장 ID 목록 필터링
            if (readWorkRequest.getFactoryIds() != null && !readWorkRequest.getFactoryIds().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, root.get("area").get("factory").get("id").in(readWorkRequest.getFactoryIds()));
            }
            // 구역 ID 목록 필터링
            if (readWorkRequest.getAreaIds() != null && !readWorkRequest.getAreaIds().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, root.get("area").get("id").in(readWorkRequest.getAreaIds()));
            }
            // 근로자 ID 목록 필터링 (다대다 관계 Join)
            if (readWorkRequest.getEmployeeIds() != null && !readWorkRequest.getEmployeeIds().isEmpty()) {
                var employeesJoin = root.join("employees", JoinType.INNER);
                predicate = criteriaBuilder.and(predicate, employeesJoin.get("id").in(readWorkRequest.getEmployeeIds()));
            }
            // 작업명 필터링 (부분 검색)
            if (readWorkRequest.getWorkName() != null && !readWorkRequest.getWorkName().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(root.get("name"), "%" + readWorkRequest.getWorkName() + "%"));
            }
            // 작업 상태 필터링
            if (readWorkRequest.getWorkStatuses() != null && !readWorkRequest.getWorkStatuses().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, root.get("status").in(readWorkRequest.getWorkStatuses()));
            }
            return predicate;
        };
    }
}