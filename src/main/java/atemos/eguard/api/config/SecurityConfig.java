package atemos.eguard.api.config;

import atemos.eguard.api.domain.AllowedHttpMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 설정 클래스.
 * 이 클래스는 시스템의 보안 설정을 정의합니다. JWT 기반의 인증을 사용하며,
 * 특정 엔드포인트에 대한 접근 권한을 설정합니다.
 * 사용되는 주요 컴포넌트:
 * - JwtRequestFilter: JWT를 이용한 인증 필터
 * - UserDetailsService: 근로자 정보를 로드하는 서비스
 * - LogoutHandler: 커스텀 로그아웃 핸들러
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtRequestFilter jwtRequestFilter;
    private final UserDetailsService userDetailsService;
    private final LogoutHandler customLogoutHandler;
    private final ApiResponseManager apiResponseManager;

    @Value("${front-end-server}")
    private String frontEndServer;

    /**
     * 보안 필터 체인을 설정하는 메서드.
     * 이 메서드는 HTTP 요청에 대한 보안 설정을 정의합니다.
     * - 특정 엔드포인트는 인증 없이 접근할 수 있도록 설정
     * - 나머지 모든 요청은 인증 필요
     * - HTTP 기본 인증 사용
     * - 로그아웃 설정
     * - JWT 인증 필터 추가
     *
     * @param http HttpSecurity 객체
     * @return SecurityFilterChain 객체
     * @throws Exception 설정 중 발생할 수 있는 예외
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/configuration/**",
                                "/auth/login",
                                "/auth/2fa",
                                "/auth/reset-password",
                                "/auth/update-password",
                                "/company/list",
                                "/factory/list",
                                "/setting")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/employee").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/renew").permitAll()
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .addLogoutHandler(customLogoutHandler)
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK)))
                .exceptionHandling(handler -> handler
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));
        // JWT 인증 필터 추가
        jwtRequestFilter.setUserDetailsService(userDetailsService);
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * AuthenticationManager를 빈으로 등록하는 메서드.
     *
     * @param authenticationConfiguration AuthenticationConfiguration 객체
     * @return AuthenticationManager 객체
     * @throws Exception 설정 중 발생할 수 있는 예외
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * CORS 설정을 위한 빈 등록 메서드.(local일 때만 활성화)
     *
     * @return CorsConfigurationSource 객체
     */
    @Bean
    @Profile("local")
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(frontEndServer));
        // 열거형을 사용하여 허용된 HTTP 메서드를 설정
        configuration.setAllowedMethods(Arrays.stream(AllowedHttpMethod.values())
                .map(Enum::name)
                .toList());
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * XSS 필터를 등록하는 메서드.
     */
    @Bean
    public FilterRegistrationBean<XSSFilter> xssFilterRegistrationBean() {
        FilterRegistrationBean<XSSFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new XSSFilter(apiResponseManager));
        registrationBean.addUrlPatterns("/*"); // 모든 URL에 필터 적용
        return registrationBean;
    }
}