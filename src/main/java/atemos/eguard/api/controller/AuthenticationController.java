package atemos.eguard.api.controller;

import atemos.eguard.api.config.ApiResponseManager;
import atemos.eguard.api.dto.ApiResponseDto;
import atemos.eguard.api.dto.EmployeeDto;
import atemos.eguard.api.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 근로자 인증/인가 처리 API 컨트롤러.
 * 이 클래스는 근로자 인증 및 인가와 관련된 API 엔드포인트를 정의합니다.
 * 로그인, 근로자 정보 조회, 로그아웃, 비밀번호 초기화 및 변경, 웰컴 이메일 발송 및 2차 인증 관련 기능 기능을 제공합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "근로자 인증/인가 처리 관리 API", description = "근로자 인증/인가 처리 API 모음")
public class AuthenticationController {
    private final ApiResponseManager apiResponseManager;
    private final AuthenticationService authenticationService;

    /**
     * 2차 인증 번호 발송 API.
     * 근로자가 로그인 후 2단계 인증을 위한 인증 번호를 이메일로 발송합니다.
     *
     * @param authCodeRequest 2차 인증 번호를 발송할 이메일을 포함한 DTO 객체
     * @return 인증 번호 발송 결과
     */
    @Operation(summary = "2차 인증 번호 발송", description = "로그인 후 2차 인증을 위한 인증 번호를 이메일로 발송하는 API")
    @PostMapping("/2fa")
    public ResponseEntity<ApiResponseDto> sendTwoFactorAuthCode(
            @Valid @RequestBody EmployeeDto.AuthCodeRequest authCodeRequest
    ) {
        authenticationService.sendTwoFactorAuthCode(authCodeRequest);
        return apiResponseManager.success("Please check the verification code sent to " + authCodeRequest.getEmployeeEmail() + ".");
    }

    /**
     * 근로자 정보와 2차 인증 번호를 확인 후 로그인 처리하는 API.
     * EmployeeDto.LoginRequest.getRequire2FA()값이 true면 2차 인증이 필요하다는 뜻이고, false면 2차 인증을 스킵합니다.
     *
     * @param loginRequest 로그인 요청 데이터
     * @return 인증 성공 시 JWT Access Token과 Refresh Token을 포함한 응답 또는 실패 결과
     */
    @Operation(summary = "로그인", description = "로그인 API")
    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto> login(
            @Valid @RequestBody EmployeeDto.LoginRequest loginRequest,
            HttpServletRequest request
    ) {
        return apiResponseManager.login(authenticationService.login(loginRequest), request.getRequestURI());
    }

    /**
     * Access Token 재발급 API.
     * 만료된 Access Token을 Refresh Token을 이용해 재발급합니다.
     *
     * @param refreshToken 클라이언트에서 제공된 Refresh Token
     * @return 새로운 Access Token
     */
    @Operation(summary = "Access Token 재발급", description = "Refresh Token을 이용해 새로운 Access Token을 발급하는 API")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/renew")
    public ResponseEntity<ApiResponseDto> refreshToken(
            @Parameter(description = "Refresh Token") @RequestParam String refreshToken,
            HttpServletRequest request
    ) {
        return apiResponseManager.success(authenticationService.renewAccessToken(refreshToken, request));
    }

    /**
     * 현재 로그인한 근로자 정보 조회 API.
     * JWT 토큰을 이용하여 현재 로그인한 근로자 정보를 조회합니다.
     *
     * @return 근로자 정보
     */
    @Operation(summary = "현재 로그인한 근로자 정보 조회", description = "JWT 토큰을 이용하여 현재 로그인한 근로자 정보를 조회하는 API")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/info")
    public ResponseEntity<ApiResponseDto> getCurrentEmployeeInfo() {
        return apiResponseManager.success(authenticationService.getCurrentEmployeeInfo());
    }

    /**
     * 현재 로그인한 근로자의 접근 권한, 접근 가능한 메뉴를 조회
     * JWT 토큰을 이용하여 현재 로그인한 근로자의 접근 권한, 접근 가능한 메뉴를 조회합니다.
     *
     * @return 근로자 정보
     */
    @Operation(summary = "현재 로그인한 근로자의 접근 권한, 접근 가능한 메뉴를 조회", description = "JWT 토큰을 이용하여 현재 로그인한 근로자의 접근 권한, 접근 가능한 메뉴를 조회하는 API")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/authority")
    public ResponseEntity<ApiResponseDto> getCurrentEmployeeAuthority() {
        return apiResponseManager.success(authenticationService.getCurrentEmployeeAuthority());
    }

    /**
     * 로그아웃 API.
     * 로그아웃 후 JWT 토큰을 블랙리스트에 추가합니다.
     * 실제 로그아웃 처리는 SecurityConfig에 의해 CustomLogoutHandler에서 처리합니다.
     *
     * @return 로그아웃 결과
     */
    @Operation(summary = "로그아웃", description = "로그아웃 후 JWT 토큰을 블랙리스트에 추가하는 API")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponseDto> logout() {
        return apiResponseManager.ok();
    }

    /**
     * 비밀번호 초기화 API.
     * 근로자의 비밀번호를 초기화합니다.
     *
     * @param resetPassword 비밀번호 초기화 요청 데이터
     * @return 초기화 결과
     */
    @Operation(summary = "비밀번호 초기화", description = "근로자의 비밀번호를 초기화하는 API")
    @PatchMapping("/reset-password")
    public ResponseEntity<ApiResponseDto> resetPassword(
            @Valid @RequestBody EmployeeDto.ResetPassword resetPassword
    ) {
        authenticationService.resetPassword(resetPassword);
        return apiResponseManager.ok();
    }

    /**
     * 비밀번호 변경 API.
     * 기존 계정 정보를 확인 후 새로운 비밀번호로 변경합니다.
     *
     * @param updatePassword 비밀번호 변경 요청 데이터
     * @return 변경 결과
     */
    @Operation(summary = "비밀번호 변경", description = "기존 계정 정보를 확인 후 새로운 비밀번호로 변경하는 API")
    @PatchMapping("/update-password")
    public ResponseEntity<ApiResponseDto> updatePassword(
            @Valid @RequestBody EmployeeDto.UpdatePassword updatePassword
    ) {
        authenticationService.updatePassword(updatePassword);
        return apiResponseManager.ok();
    }
}