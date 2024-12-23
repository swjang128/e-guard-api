package atemos.eguard.api.specification;

import atemos.eguard.api.config.EncryptUtil;
import atemos.eguard.api.dto.EmployeeDto;
import atemos.eguard.api.entity.Employee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

/**
 * Employee 엔티티에 대한 동적 쿼리를 생성하는 스펙 클래스입니다.
 * 주어진 조건에 따라 다양한 필터링 옵션을 지원합니다.
 */
@Slf4j
public class EmployeeSpecification {
    /**
     * 주어진 EmployeeDto.ReadEmployeeRequest를 기반으로 Employee 엔티티에 대한 스펙을 생성합니다.
     *
     * @param readEmployeeRequestDto 근로자 조회 조건을 포함하는 데이터 전송 객체
     * @param encryptUtil 암호화 유틸리티
     * @return 조건에 맞는 Employee 엔티티를 조회하기 위한 Specification 객체
     */
    public static Specification<Employee> findWith(EmployeeDto.ReadEmployeeRequest readEmployeeRequestDto, EncryptUtil encryptUtil) {
        return (root, query, criteriaBuilder) -> {
            // 기본 조건 생성
            var predicate = criteriaBuilder.conjunction();
            // 근로자 ID
            if (readEmployeeRequestDto.getEmployeeIds() != null && !readEmployeeRequestDto.getEmployeeIds().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, root.get("id").in(readEmployeeRequestDto.getEmployeeIds()));
            }
            // 공장 ID
            if (readEmployeeRequestDto.getFactoryIds() != null && !readEmployeeRequestDto.getFactoryIds().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, root.get("factory").get("id").in(readEmployeeRequestDto.getFactoryIds()));
            }
            // 이름(암호화된 값으로 정확히 검색)
            if (readEmployeeRequestDto.getEmployeeName() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("name"), encryptUtil.encrypt(readEmployeeRequestDto.getEmployeeName())));
            }
            // 이메일(암호화된 값으로 정확히 검색)
            if (readEmployeeRequestDto.getEmployeeEmail() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("email"), encryptUtil.encrypt(readEmployeeRequestDto.getEmployeeEmail())));
            }
            // 연락처(암호화된 값으로 정확히 검색)
            if (readEmployeeRequestDto.getEmployeePhoneNumber() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("phoneNumber"), encryptUtil.encrypt(readEmployeeRequestDto.getEmployeePhoneNumber())));
            }
            // 권한
            if (readEmployeeRequestDto.getRoles() != null && !readEmployeeRequestDto.getRoles().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, root.get("role").in(readEmployeeRequestDto.getRoles()));
            }
            // 계정 상태
            if (readEmployeeRequestDto.getAuthenticationStatuses() != null && !readEmployeeRequestDto.getAuthenticationStatuses().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, root.get("authenticationStatus").in(readEmployeeRequestDto.getAuthenticationStatuses()));
            }
            // 사원번호(부분 일치 검색)
            if (readEmployeeRequestDto.getEmployeeNumber() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.like(root.get("employeeNumber"), "%" + readEmployeeRequestDto.getEmployeeNumber() + "%"));
            }
            return predicate;
        };
    }
}