package atemos.eguard.api.config;

import atemos.eguard.api.dto.EmployeeDto;
import atemos.eguard.api.repository.BlacklistedTokenRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;

/**
 * JWT 토큰을 생성하고 검증하는 유틸리티 클래스입니다.
 * Access Token과 Refresh Token을 생성할 수 있으며, 토큰의 유효성을 확인하는 기능을 제공합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final ObjectMapper objectMapper;

    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;
    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private Key key;
    /**
     * Secret Key를 생성하고 HMAC-SHA256 알고리즘을 사용합니다.
     */
    @PostConstruct
    public void initJwtUtil() {
        // secret이 null이거나 길이가 충분하지 않은지 확인
        if (secret == null || secret.length() < 32) { // 32 bytes = 256 bits
            throw new IllegalArgumentException("JWT secret must be at least 32 characters long");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Refresh Token의 만료 기간을 반환합니다.
     *
     * @return Refresh Token 만료 기간 (밀리초)
     */
    public long getRefreshTokenExpirationInMillis() {
        return refreshTokenExpiration;
    }

    /**
     * Access Token을 생성합니다.
     * 현재 로그인한 근로자가 속한 업체의 시스템 설정에서 로그인 유지 만료 시간(분)을 Expiration 시간(밀리초)로 설정합니다.
     *
     * @param employeeInfo 토큰에 포함할 근로자 정보 (ReadEmployeeResponse)
     * @return 생성된 Access Token
     */
    public String generateAccessToken(EmployeeDto.ReadEmployeeResponse employeeInfo) {
        // ObjectMapper를 사용하여 ReadEmployeeResponse를 Map<String, Object>로 변환
        Map<String, Object> claims = objectMapper.convertValue(employeeInfo, new TypeReference<>() {});
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(employeeInfo.getEmployeeEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(key)
                .compact();
    }

    /**
     * Refresh Token을 생성합니다.
     * 현재 로그인한 근로자가 속한 업체의 시스템 설정에서 로그인 갱신 만료 시간(분)을 Expiration 시간(밀리초)로 설정합니다.
     * @param email 토큰을 생성한 근로자(복호화한 이메일)
     * @return 생성된 Refresh Token
     */
    public String generateRefreshToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(key)
                .compact();
    }

    /**
     * HttpServletRequest에서 토큰 정보를 가져옵니다.
     *
     * @param request HttpServletRequest 정보
     * @return JWT 토큰
     */
    public String extractTokenFromRequest(HttpServletRequest request) {
        // 헤더에서 토큰 추출
        var authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        // 쿠키에서 토큰 추출
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Access Token 또는 Refresh Token의 유효성을 검증합니다.
     * 유효성 검증을 수행하고, 토큰이 유효하면 true를 반환하고 그렇지 않으면 false를 반환합니다.
     *
     * @param token JWT 토큰
     * @return 토큰이 유효하면 true, 그렇지 않으면 false
     */
    public Boolean validateToken(String token) {
        // 블랙리스트 토큰에 있는지 확인 후 유효하지 않으면 바로 false 반환
        if (blacklistedTokenRepository.existsByToken(token)) {
            log.warn("This token is blacklisted: {}", token);
            return false;
        }
        // 토큰 검증 및 파싱
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true; // 유효한 토큰
        } catch (JwtException e) {
            // 토큰이 만료되었거나 유효하지 않을 경우 false 반환
            log.warn("JWT token validation failed: {}", e.getMessage());
        } catch (Exception e) {
            // 기타 예외 발생 시 false 반환
            log.error("Unexpected error during token validation: {}", e.getMessage());
        }
        return false; // 검증 실패 시 false 반환
    }

    /**
     * 검증된 토큰에서 계정 이메일을 추출합니다.
     * 만료된 토큰이라도 사용자 이름을 추출할 수 있도록 처리합니다.
     *
     * @param token JWT 토큰
     * @return 토큰에서 추출된 계정 이메일 또는 null
     */
    public String extractEmailFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (ExpiredJwtException e) {
            // 만료된 토큰의 경우에도 클레임에서 사용자 이름 추출
            return e.getClaims().getSubject();
        } catch (JwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * JWT 토큰에서 클레임 정보를 추출합니다.
     * @param token JWT 토큰
     * @return 클레임 정보가 담긴 Map
     */
    public Map<String, Object> extractClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 JWT 토큰입니다.");
        }
    }
}