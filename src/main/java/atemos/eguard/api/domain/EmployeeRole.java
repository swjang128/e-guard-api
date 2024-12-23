package atemos.eguard.api.domain;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * 시스템 내 근로자 역할을 정의하는 열거형입니다.
 */
public enum EmployeeRole {
    /**
     * 일반 근로자 및 작업자입니다.
     */
    WORKER,
    /**
     * 관리자를 나타냅니다. 근로자의 관리 및 특정 작업을 수행할 수 있는 권한을 가진 근로자입니다.
     */
    MANAGER,
    /**
     * 관리자 이상의 권한을 가진 시스템 관리자입니다. 모든 기능을 제어하고 설정할 수 있습니다.
     */
    ADMIN;

    public GrantedAuthority getAuthority() {
        return new SimpleGrantedAuthority("ROLE_" + this.name());
    }
}