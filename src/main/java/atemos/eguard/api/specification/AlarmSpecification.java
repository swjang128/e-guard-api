package atemos.eguard.api.specification;

import atemos.eguard.api.entity.Alarm;
import atemos.eguard.api.dto.AlarmDto;
import org.springframework.data.jpa.domain.Specification;

/**
 * AlarmSpecification 클래스는 Alarm 엔티티에 대한 동적 쿼리를 생성하기 위한 스펙 클래스입니다.
 */
public class AlarmSpecification {
    /**
     * 주어진 AlarmDto.ReadAlarmRequest 객체를 기반으로 Alarm 엔티티에 대한 Specification을 생성합니다.
     *
     * @param readAlarmRequest 알람 조회 조건을 포함하는 데이터 전송 객체
     * @return 주어진 조건에 맞는 Alarm 엔티티를 조회하기 위한 Specification 객체
     */
    public static Specification<Alarm> findWith(AlarmDto.ReadAlarmRequest readAlarmRequest) {
        return (root, query, criteriaBuilder) -> {
            // 기본 조건을 위한 Predicate 초기화
            var predicate = criteriaBuilder.conjunction();
            // 알람 ID 리스트
            if (readAlarmRequest.getAlarmIds() != null && !readAlarmRequest.getAlarmIds().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, root.get("id").in(readAlarmRequest.getAlarmIds()));
            }
            // 근로자 ID 리스트
            if (readAlarmRequest.getEmployeeIds() != null && !readAlarmRequest.getEmployeeIds().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, root.get("employee").get("id").in(readAlarmRequest.getEmployeeIds()));
            }
            // 사건 ID 리스트
            if (readAlarmRequest.getEventIds() != null && !readAlarmRequest.getEventIds().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, root.get("event").get("id").in(readAlarmRequest.getEventIds()));
            }
            // 공장 ID 리스트
            if (readAlarmRequest.getFactoryIds() != null && !readAlarmRequest.getFactoryIds().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, root.get("employee").get("factory").get("id").in(readAlarmRequest.getFactoryIds()));
            }
            // 업체 ID 리스트
            if (readAlarmRequest.getCompanyIds() != null && !readAlarmRequest.getCompanyIds().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, root.get("employee").get("factory").get("company").get("id").in(readAlarmRequest.getCompanyIds()));
            }
            // 알람 메시지
            if (readAlarmRequest.getAlarmMessage() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(root.get("message"), "%" + (readAlarmRequest.getAlarmMessage()) + "%"));
            }
            // 알람 읽음 여부
            if (readAlarmRequest.getAlarmRead() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("isRead"), readAlarmRequest.getAlarmRead()));
            }
            // 근로자에게 일어난 사건 목록
            if (readAlarmRequest.getEmployeeIncidents() != null && !readAlarmRequest.getEmployeeIncidents().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, root.get("event").get("type").in(readAlarmRequest.getEmployeeIncidents()));
            }
            // 구역에서 일어난 사건 목록
            if (readAlarmRequest.getAreaIncidents() != null && !readAlarmRequest.getAreaIncidents().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, root.get("event").get("type").in(readAlarmRequest.getAreaIncidents()));
            }
            // 조회 시작일시
            if (readAlarmRequest.getSearchStartTime() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), readAlarmRequest.getSearchStartTime()));
            }
            // 조회 종료일시
            if (readAlarmRequest.getSearchEndTime() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), readAlarmRequest.getSearchEndTime()));
            }
            // 최종적으로 생성된 조건을 반환
            return predicate;
        };
    }
}
