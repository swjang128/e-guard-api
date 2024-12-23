package atemos.eguard.api.specification;

import atemos.eguard.api.entity.Company;
import atemos.eguard.api.dto.CompanyDto;
import org.springframework.data.jpa.domain.Specification;

/**
 * Company 엔티티에 대한 동적 쿼리를 생성하는 스펙 클래스입니다.
 * 주어진 조건에 따라 다양한 필터링 옵션을 지원합니다.
 */
public class CompanySpecification {
    /**
     * 주어진 CompanyDto.ReadCompanyRequest를 기반으로 Company 엔티티에 대한 스펙을 생성합니다.
     *
     * @param readCompanyRequest 업체 조회 조건을 포함하는 데이터 전송 객체
     * @return 조건에 맞는 Company 엔티티를 조회하기 위한 Specification 객체
     */
    public static Specification<Company> findWith(CompanyDto.ReadCompanyRequest readCompanyRequest) {
        return (root, query, criteriaBuilder) -> {
            // 기본 조건 생성
            var predicate = criteriaBuilder.conjunction();
            // 업체 ID
            if (readCompanyRequest.getCompanyIds() != null && !readCompanyRequest.getCompanyIds().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, root.get("id").in(readCompanyRequest.getCompanyIds()));
            }
            // 사업자 등록번호
            if (readCompanyRequest.getCompanyBusinessNumber() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(root.get("businessNumber"), "%" + readCompanyRequest.getCompanyBusinessNumber() + "%"));
            }
            // 업체명
            if (readCompanyRequest.getCompanyName() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(root.get("name"),"%" + readCompanyRequest.getCompanyName() + "%"));
            }
            // 이메일
            if (readCompanyRequest.getCompanyEmail() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(root.get("email"),"%" + readCompanyRequest.getCompanyEmail() + "%"));
            }
            // 연락처
            if (readCompanyRequest.getCompanyPhoneNumber() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(root.get("phoneNumber"),"%" + readCompanyRequest.getCompanyPhoneNumber() + "%"));
            }
            // 주소
            if (readCompanyRequest.getCompanyAddress() != null) {
                predicate = criteriaBuilder.and(predicate,criteriaBuilder.like(root.get("address"),"%" + readCompanyRequest.getCompanyAddress() + "%"));
            }
            return predicate;
        };
    }
}