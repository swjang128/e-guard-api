package atemos.eguard.api.repository;

import atemos.eguard.api.entity.Employee;
import atemos.eguard.api.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Refresh Token을 관리하기 위한 리포지토리 인터페이스.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    /**
     * Token을 이용해 Refresh Token을 조회합니다.
     *
     * @param token 저장된 Refresh Token
     * @return 해당 Refresh Token의 Optional 객체
     */
    Optional<RefreshToken> findByToken(String token);
    /**
     * 특정 근로자의 모든 Refresh Token을 삭제합니다.
     *
     * @param employee 근로자
     */
    void deleteByEmployee(Employee employee);
}
