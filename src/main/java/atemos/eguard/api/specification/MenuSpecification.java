package atemos.eguard.api.specification;

import atemos.eguard.api.dto.MenuDto;
import atemos.eguard.api.entity.Menu;
import org.springframework.data.jpa.domain.Specification;

/**
 * Menu 엔티티에 대한 동적 쿼리를 생성하는 스펙 클래스입니다.
 * 주어진 조건에 따라 다양한 필터링 옵션을 지원합니다.
 */
public class MenuSpecification {
    /**
     * 주어진 MenuDto.ReadMenuRequest를 기반으로 Menu 엔티티에 대한 스펙을 생성합니다.
     *
     * @param readMenuRequest 메뉴 조회 조건을 포함하는 데이터 전송 객체
     * @return 조건에 맞는 Menu 엔티티를 조회하기 위한 Specification 객체
     */
    public static Specification<Menu> findWith(MenuDto.ReadMenuRequest readMenuRequest) {
        return (root, query, criteriaBuilder) -> {
            // 기본 조건 생성
            var predicate=criteriaBuilder.conjunction();
            // 메뉴 ID
            if (readMenuRequest.getMenuIds() != null && !readMenuRequest.getMenuIds().isEmpty()) {
                predicate=criteriaBuilder.and(predicate, root.get("id").in(readMenuRequest.getMenuIds()));
            }
            // 메뉴 이름
            if (readMenuRequest.getMenuName() != null) {
                predicate=criteriaBuilder.and(predicate, criteriaBuilder.like(root.get("name"), "%" + readMenuRequest.getMenuName() + "%"));
            }
            // URL
            if (readMenuRequest.getMenuUrl() != null) {
                predicate=criteriaBuilder.and(predicate, criteriaBuilder.like(root.get("url"), "%" + readMenuRequest.getMenuUrl() + "%"));
            }
            // 설명
            if (readMenuRequest.getMenuDescription() != null) {
                predicate=criteriaBuilder.and(predicate, criteriaBuilder.like(root.get("description"), "%" + readMenuRequest.getMenuDescription() + "%"));
            }
            // 사용 여부
            if (readMenuRequest.getMenuAvailable() != null) {
                predicate=criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("available"), readMenuRequest.getMenuAvailable()));
            }
            // 상위 메뉴 ID
            if (readMenuRequest.getParentIds() != null && !readMenuRequest.getParentIds().isEmpty()) {
                predicate=criteriaBuilder.and(predicate, root.get("parent").get("id").in(readMenuRequest.getParentIds()));
            }
            // 접근 권한 조건 추가
            if (readMenuRequest.getAccessibleRoles() != null && !readMenuRequest.getAccessibleRoles().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, root.join("accessibleRoles").in(readMenuRequest.getAccessibleRoles()));
            }
            return predicate;
        };
    }
}