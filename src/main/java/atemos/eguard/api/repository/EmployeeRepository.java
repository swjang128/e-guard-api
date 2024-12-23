package atemos.eguard.api.repository;

import atemos.eguard.api.domain.EmployeeRole;
import atemos.eguard.api.entity.Employee;
import atemos.eguard.api.entity.Factory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

/**
 * Employee 엔티티에 대한 데이터 접근을 제공하는 리포지토리 인터페이스입니다.
 *
 * 이 인터페이스는 JPA의 기본 CRUD 기능을 제공하며, 이메일로 근로자를 조회하는 기능을 추가로 지원합니다.
 * - 기본 CRUD 작업을 위한 메소드 제공 (저장, 조회, 수정, 삭제)
 * - 스펙을 사용하여 복잡한 조건의 쿼리 작성 지원
 * - 이메일을 통해 회원을 조회하는 메소드 추가
 */
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {
    /**
     * 주어진 이메일 주소로 회원을 조회합니다.
     *
     * @param email 조회할 근로자의 이메일 주소
     * @return 주어진 이메일 주소를 가진 근로자가 존재하면 {@link Optional}로 반환하며, 존재하지 않으면 빈 {@link Optional} 반환
     */
    Optional<Employee> findByEmail(String email);
    /**
     * 주어진 이메일 또는 전화번호가 존재하는지 여부를 확인합니다.
     *
     * @param encryptedEmail 확인할 근로자의 암호화된 이메일 주소
     * @param encryptedPhoneNumber 확인할 근로자의 암호화된 전화번호
     * @return 이메일 또는 전화번호가 존재하면 true, 그렇지 않으면 false 반환
     */
    boolean existsByEmailOrPhoneNumber(String encryptedEmail, String encryptedPhoneNumber);
    /**
     * 주어진 공장에 속한 모든 근로자를 조회합니다.
     *
     * @param factory 공장 엔티티
     * @return 해당 공장에 속한 근로자 목록
     */
    List<Employee> findByFactory(Factory factory);
    /**
     * 주어진 공장과 역할에 속한 근로자를 조회합니다.
     *
     * @param factory 조회할 공장
     * @param role 조회할 근로자의 역할
     * @return 주어진 공장과 역할에 해당하는 근로자 목록
     */
    List<Employee> findByFactoryAndRole(Factory factory, EmployeeRole role);
}