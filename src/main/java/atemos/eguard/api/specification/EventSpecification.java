package atemos.eguard.api.specification;

import atemos.eguard.api.dto.EventDto;
import atemos.eguard.api.entity.Event;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Event 엔티티에 대한 동적 쿼리를 생성하는 스펙 클래스입니다.
 * 주어진 조건에 따라 다양한 필터링 옵션을 지원합니다.
 */
public class EventSpecification {
    /**
     * 주어진 EventDto.ReadEventRequest를 기반으로 Event 엔티티에 대한 스펙을 생성합니다.
     *
     * @param readEventRequest 사건 조회 조건을 포함하는 데이터 전송 객체
     * @return 조건에 맞는 Event 엔티티를 조회하기 위한 Specification 객체
     */
    public static Specification<Event> findWith(EventDto.ReadEventRequest readEventRequest) {
        return (root, query, criteriaBuilder) -> {
            // 기본 조건 생성
            var predicate = criteriaBuilder.conjunction();
            // Employee 엔티티와 Area 엔티티를 LEFT JOIN
            var employeeJoin = root.join("employee", JoinType.LEFT);
            var areaJoin = root.join("area", JoinType.LEFT);
            // 공장 ID 리스트에 포함된 ID
            if (readEventRequest.getFactoryIds() != null && !readEventRequest.getFactoryIds().isEmpty()) {
                var factoryPredicate = criteriaBuilder.disjunction();
                // area.factory.id 필터링 (area가 null일 경우를 고려)
                factoryPredicate = criteriaBuilder.or(
                        factoryPredicate,
                        criteriaBuilder.and(
                                criteriaBuilder.isNotNull(areaJoin),
                                criteriaBuilder.isNotNull(areaJoin.get("factory")),
                                areaJoin.get("factory").get("id").in(readEventRequest.getFactoryIds())
                        )
                );
                // employee.factory.id 필터링 (employee가 null일 경우를 고려)
                factoryPredicate = criteriaBuilder.or(
                        factoryPredicate,
                        criteriaBuilder.and(
                                criteriaBuilder.isNotNull(employeeJoin),
                                criteriaBuilder.isNotNull(employeeJoin.get("factory")),
                                employeeJoin.get("factory").get("id").in(readEventRequest.getFactoryIds())
                        )
                );
                // 최종 조건 결합
                predicate = criteriaBuilder.and(predicate, factoryPredicate);
            }
            // 사건 ID 리스트에 포함된 ID
            if (readEventRequest.getEventIds() != null && !readEventRequest.getEventIds().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, root.get("id").in(readEventRequest.getEventIds()));
            }
            // 근로자 ID 리스트에 포함된 ID
            if (readEventRequest.getEmployeeIds() != null && !readEventRequest.getEmployeeIds().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, employeeJoin.get("id").in(readEventRequest.getEmployeeIds()));
            }
            // 구역 ID 리스트에 포함된 ID
            if (readEventRequest.getAreaIds() != null && !readEventRequest.getAreaIds().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, areaJoin.get("id").in(readEventRequest.getAreaIds()));
            }
            // 근로자에게 발생한 사건
            if (readEventRequest.getEmployeeIncidents() != null && !readEventRequest.getEmployeeIncidents().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, root.get("employeeIncident").in(readEventRequest.getEmployeeIncidents()));
            }
            // 구역에서 발생한 사건
            if (readEventRequest.getAreaIncidents() != null && !readEventRequest.getAreaIncidents().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, root.get("areaIncident").in(readEventRequest.getAreaIncidents()));
            }
            // 사건 해결 여부
            if (readEventRequest.getEventResolved() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("resolved"), readEventRequest.getEventResolved()));
            }
            // 조회 시작일과 종료일
            if (readEventRequest.getSearchStartDate() != null && readEventRequest.getSearchEndDate() != null) {
                var startDateTime = LocalDateTime.of(readEventRequest.getSearchStartDate(), LocalTime.MIN);
                var endDateTime = LocalDateTime.of(readEventRequest.getSearchEndDate(), LocalTime.MAX);
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.between(root.get("createdAt"), startDateTime, endDateTime)
                );
            } else if (readEventRequest.getSearchStartDate() != null) {
                var startDateTime = LocalDateTime.of(readEventRequest.getSearchStartDate(), LocalTime.MIN);
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDateTime));
            } else if (readEventRequest.getSearchEndDate() != null) {
                var endDateTime = LocalDateTime.of(readEventRequest.getSearchEndDate(), LocalTime.MAX);
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDateTime));
            }
            return predicate;
        };
    }
}