package atemos.eguard.api.config;

import atemos.eguard.api.entity.BlacklistedToken;
import atemos.eguard.api.entity.Employee;
import atemos.eguard.api.repository.BlacklistedTokenRepository;
import atemos.eguard.api.repository.EmployeeRepository;
import atemos.eguard.api.repository.RefreshTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 로그아웃 처리를 담당하는 컴포넌트입니다.
 * JWT 토큰을 블랙리스트에 추가하고, 관련된 리프레시 토큰을 삭제합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final EmployeeRepository employeeRepository;
    private final LogComponent logComponent;

    /**
     * 로그아웃 처리를 수행합니다. 요청에서 JWT 토큰을 추출하고, 해당 토큰을 블랙리스트에 추가합니다.
     *
     * @param request        JWT 토큰을 추출할 HttpServletRequest
     * @param response       현재 HTTP 응답 객체
     * @param authentication 현재 인증 정보를 담고 있는 Authentication 객체
     */
    @Override
    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        // HttpServletRequest에서 토큰을 가져온다.
        var token = jwtUtil.extractTokenFromRequest(request);
        if (token != null) {
            // 블랙리스트에 이미 존재하는지 확인
            boolean alreadyBlacklisted = blacklistedTokenRepository.existsByToken(token);
            if (!alreadyBlacklisted) {
                // JWT 토큰을 블랙리스트에 추가
                blacklistedTokenRepository.save(
                        BlacklistedToken.builder()
                                .token(token)
                                .build());
            }
            // JWT 토큰에서 근로자 이메일을 추출
            String email = jwtUtil.extractEmailFromToken(token);
            if (email != null) {
                // Employee 엔티티를 근로자 이메일로 조회
                Optional<Employee> employee = employeeRepository.findByEmail(email);
                // 해당 근로자의 리프레시 토큰 삭제
                employee.ifPresent(refreshTokenRepository::deleteByEmployee);
            }
            // 로그아웃 관련 인증 로그 기록
            logComponent.saveAuthenticationLog(HttpServletResponse.SC_OK, request.getRequestURI().replaceFirst("/eguard", ""), null);
        }
    }
}