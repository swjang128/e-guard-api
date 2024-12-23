package atemos.eguard.api.specification;

import atemos.eguard.api.config.EncryptUtil;
import atemos.eguard.api.dto.SettingDto;
import atemos.eguard.api.entity.Setting;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

/**
 * Setting 엔티티에 대한 동적 쿼리를 생성하는 스펙 클래스입니다.
 * 주어진 조건에 따라 다양한 필터링 옵션을 지원합니다.
 */
public class SettingSpecification {
    /**
     * 주어진 SettingDto.ReadSettingRequest를 기반으로 Setting 엔티티에 대한 스펙을 생성합니다.
     *
     * @param readSettingRequestDto 시스템 설정 조회 조건을 포함하는 데이터 전송 객체
     * @return 조건에 맞는 Setting 엔티티를 조회하기 위한 Specification 객체
     */
    public static Specification<Setting> findWith(SettingDto.ReadSettingRequest readSettingRequestDto, EncryptUtil encryptUtil) {
        return (root, query, criteriaBuilder) -> {
            // 기본 조건 생성
            var predicate=criteriaBuilder.conjunction();
            // Company, Factory, Employee 엔티티를 LEFT JOIN
            var companyJoin = root.join("company", JoinType.LEFT);
            var factoryJoin = companyJoin.join("factories", JoinType.LEFT);
            var employeeJoin = factoryJoin.join("employees", JoinType.LEFT);
            // 근로자 이메일
            if (readSettingRequestDto.getEmployeeEmail() != null && !readSettingRequestDto.getEmployeeEmail().isEmpty()) {
                var employeePredicate = criteriaBuilder.equal(employeeJoin.get("email"), encryptUtil.encrypt(readSettingRequestDto.getEmployeeEmail()));
                predicate = criteriaBuilder.and(predicate, employeePredicate);
            }
            // 시스템 설정 ID
            if (readSettingRequestDto.getSettingIds() != null && !readSettingRequestDto.getSettingIds().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, root.get("id").in(readSettingRequestDto.getSettingIds()));
            }
            // 업체 ID
            if (readSettingRequestDto.getCompanyIds() != null && !readSettingRequestDto.getCompanyIds().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, root.get("company").get("id").in(readSettingRequestDto.getCompanyIds()));
            }
            // 2차 인증 여부
            if (readSettingRequestDto.getTwoFactorAuthenticationEnabled() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("twoFactorAuthenticationEnabled"), readSettingRequestDto.getTwoFactorAuthenticationEnabled()));
            }
            // 2차 인증 방법
            if (readSettingRequestDto.getTwoFactorAuthenticationMethods() != null && !readSettingRequestDto.getTwoFactorAuthenticationMethods().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, root.get("twoFactorAuthenticationMethod").in(readSettingRequestDto.getTwoFactorAuthenticationMethods()));
            }
            return predicate;
        };
    }
}