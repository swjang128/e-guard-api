package atemos.eguard.api.specification;

import atemos.eguard.api.dto.AreaDto;
import atemos.eguard.api.entity.Area;
import org.springframework.data.jpa.domain.Specification;

/**
 * Area 엔티티에 대한 동적 쿼리를 생성하는 스펙 클래스입니다.
 * 주어진 조건에 따라 다양한 필터링 옵션을 지원합니다.
 */
public class AreaSpecification {
    /**
     * 주어진 AreaDto.ReadAreaRequest를 기반으로 Area 엔티티에 대한 스펙을 생성합니다.
     *
     * @param readAreaRequest 구역 조회 조건을 포함하는 데이터 전송 객체
     * @return 조건에 맞는 Area 엔티티를 조회하기 위한 Specification 객체
     */
    public static Specification<Area> findWith(AreaDto.ReadAreaRequest readAreaRequest) {
        return (root, query, criteriaBuilder) -> {
            // 기본 조건 생성
            var predicate = criteriaBuilder.conjunction();
            // 구역 ID 리스트
            if (readAreaRequest.getAreaIds() != null && !readAreaRequest.getAreaIds().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, root.get("id").in(readAreaRequest.getAreaIds()));
            }
            // 공장 ID 리스트
            if (readAreaRequest.getFactoryIds() != null && !readAreaRequest.getFactoryIds().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, root.get("factory").get("id").in(readAreaRequest.getFactoryIds()));
            }
            // 구역명
            if (readAreaRequest.getAreaName() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(root.get("name"), "%" + readAreaRequest.getAreaName() + "%"));
            }
            // 주소
            if (readAreaRequest.getAreaLocation() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(root.get("location"), "%" + readAreaRequest.getAreaLocation() + "%"));
            }
            // 검색 최소/최대 위도
            if (readAreaRequest.getAreaMinLatitude() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("latitude"), readAreaRequest.getAreaMinLatitude()));
            }
            if (readAreaRequest.getAreaMaxLatitude() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get("latitude"), readAreaRequest.getAreaMaxLatitude()));
            }
            // 검색 최소/최대 경도
            if (readAreaRequest.getAreaMinLongitude() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("longitude"), readAreaRequest.getAreaMinLongitude()));
            }
            if (readAreaRequest.getAreaMaxLongitude() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get("longitude"), readAreaRequest.getAreaMaxLongitude()));
            }
            return predicate;
        };
    }
}