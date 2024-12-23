package atemos.eguard.api.domain;

/**
 * 시스템 내 근로자 상태를 정의하는 열거형입니다.
 */
public enum AuthenticationStatus {
    /**
     * 계정이 활성화된 상태입니다.
     */
    ACTIVE,
    /**
     * 계정이 비활성화된 상태입니다.
     */
    INACTIVE,
    /**
     * 계정의 보안상 이슈로 관리자에 의해 정지된 상태입니다.
     */
    SUSPENDED,
    /**
     * 비밀번호 실패로 인해 계정이 잠긴 상태입니다.
     */
    LOCKED,
    /**
     * 근로자의 비밀번호가 초기화된 상태입니다.
     */
    PASSWORD_RESET,
    /**
     * 근로자가 탈퇴한 상태입니다.
     */
    WITHDRAWN,
    /**
     * 계정이 삭제된 상태입니다.
     */
    DELETED
}