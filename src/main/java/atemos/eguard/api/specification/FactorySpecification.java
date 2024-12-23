package atemos.eguard.api.specification;

import atemos.eguard.api.entity.Factory;
import atemos.eguard.api.dto.FactoryDto;
import org.springframework.data.jpa.domain.Specification;

/**
 * Factory 엔티티에 대한 동적 쿼리를 생성하는 스펙 클래스입니다.
 * 주어진 조건에 따라 다양한 필터링 옵션을 지원합니다.
 */
public class FactorySpecification {
    /**
     * 주어진 FactoryDto.ReadFactoryRequest를 기반으로 Factory 엔티티에 대한 스펙을 생성합니다.
     *
     * @param readFactoryRequestDto 공장 조회 조건을 포함하는 데이터 전송 객체
     * @return 조건에 맞는 Factory 엔티티를 조회하기 위한 Specification 객체
     */
    public static Specification<Factory> findWith(FactoryDto.ReadFactoryRequest readFactoryRequestDto) {
        return (root, query, criteriaBuilder) -> {
            // 기본 조건 생성
            var predicate = criteriaBuilder.conjunction();
            // 공장 ID
            if (readFactoryRequestDto.getFactoryIds() != null && !readFactoryRequestDto.getFactoryIds().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, root.get("id").in(readFactoryRequestDto.getFactoryIds()));
            }
            // 업체 ID
            if (readFactoryRequestDto.getCompanyIds() != null && !readFactoryRequestDto.getCompanyIds().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, root.get("company").get("id").in(readFactoryRequestDto.getCompanyIds()));
            }
            // 공장명
            if (readFactoryRequestDto.getFactoryName() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(root.get("name"),"%" + readFactoryRequestDto.getFactoryName() + "%"));
            }
            // 주소
            if (readFactoryRequestDto.getFactoryAddress() != null) {
                predicate = criteriaBuilder.and(predicate,criteriaBuilder.like(root.get("address"),"%" + readFactoryRequestDto.getFactoryAddress() + "%"));
            }
            // 업종
            if (readFactoryRequestDto.getFactoryIndustryTypes() != null && !readFactoryRequestDto.getFactoryIndustryTypes().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, root.get("industryType").in(readFactoryRequestDto.getFactoryIndustryTypes()));
            }
            return predicate;
        };
    }
}