package atemos.eguard.api.service;

import atemos.eguard.api.dto.EmployeeDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

/**
 * AuthenticationService는 근로자 인증 및 권한 부여와 관련된 비즈니스 로직을 처리하는 서비스 인터페이스입니다.
 *
 * 이 인터페이스는 로그인, 근로자 정보 조회, JWT 토큰 블랙리스트 추가, 비밀번호 초기화 및 변경 기능을 제공합니다.
 */
public interface AuthenticationService {
    /**
     * 2차 인증을 위한 인증 번호를 발송합니다.
     *
     * @param authCodeRequestDto 2차 인증 번호를 발송할 이메일을 포함한 DTO 객체
     */
    void sendTwoFactorAuthCode(EmployeeDto.AuthCodeRequest authCodeRequestDto);
    /**
     * 근로자가 제공한 로그인 정보를 기반으로 로그인을 수행합니다.
     *
     * @param loginRequestDto 로그인 요청 정보를 포함하는 데이터 전송 객체입니다. 이 객체에는 근로자 아이디와 비밀번호 등이 포함됩니다.
     * @return 로그인 성공 시 반환되는 응답 객체입니다. Access & Refresh Token이 반환됩니다.
     */
    EmployeeDto.LoginResponse login(EmployeeDto.LoginRequest loginRequestDto);
    /**
     * JWT 토큰을 사용하여 현재 로그인된 근로자의 정보를 조회합니다.
     *
     * @return 근로자 정보 객체입니다. 근로자의 아이디, 이름, 권한 등의 정보가 포함됩니다.
     */
    EmployeeDto.ReadEmployeeResponse getCurrentEmployeeInfo();
    /**
     * JWT 토큰을 사용하여 현재 로그인한 근로자의 접근 권한, 접근 가능한 메뉴를 조회합니다.
     *
     * @return 근로자 권한 정보 객체입니다. 근로자의 접근 권한, 접근 가능한 메뉴를 포함합니다.
     */
    EmployeeDto.ReadEmployeeResponse getCurrentEmployeeAuthority();
    /**
     * Refresh Token을 사용하여 새로운 Access Token을 발급합니다.
     * 이 메서드는 클라이언트가 제공한 Refresh Token의 유효성을 확인한 후,
     * 해당 근로자의 새로운 Access Token을 생성하여 반환합니다.
     * Refresh Token이 유효하지 않거나 만료된 경우 예외를 발생시킵니다.
     *
     * @param refreshToken 클라이언트에서 제공된 Refresh Token
     * @param request HttpServletRequest 객체
     * @return 새로운 Access Token을 포함한 응답 객체
     * @throws ResponseStatusException Refresh Token이 유효하지 않거나 만료된 경우 발생
     */
    EmployeeDto.LoginResponse renewAccessToken(String refreshToken, HttpServletRequest request);
    /**
     * 근로자의 비밀번호를 초기화합니다.
     *
     * @param resetPasswordDto 비밀번호 초기화를 위한 데이터 전송 객체입니다. 이 객체에는 초기화할 비밀번호와 관련된 정보가 포함됩니다.
     */
    void resetPassword(EmployeeDto.ResetPassword resetPasswordDto);
    /**
     * 근로자의 비밀번호를 변경합니다.
     *
     * @param updatePasswordDto 비밀번호 변경을 위한 데이터 전송 객체입니다. 이 객체에는 기존 비밀번호와 새 비밀번호가 포함됩니다.
     */
    void updatePassword(EmployeeDto.UpdatePassword updatePasswordDto);
}