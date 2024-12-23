package atemos.eguard.api.config;

import atemos.eguard.api.domain.EmployeeIncident;
import atemos.eguard.api.domain.SampleData;
import atemos.eguard.api.dto.EmployeeDto;
import atemos.eguard.api.entity.Company;
import atemos.eguard.api.entity.Employee;
import atemos.eguard.api.entity.Factory;
import atemos.eguard.api.service.AuthenticationServiceImpl;
import atemos.eguard.api.service.EmployeeService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 요청 필터 클래스.
 * HTTP 요청에서 JWT 토큰을 추출하고 검증하여 Spring Security의 인증 컨텍스트를 설정합니다.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final EmployeeService employeeService;
    private final AuthenticationServiceImpl authenticationService;
    private final EncryptUtil encryptUtil;

    @Setter
    private UserDetailsService userDetailsService;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    // Swagger UI와 같은 특정 경로는 필터링에서 제외
    private static final List<String> EXCLUDED_PATHS = List.of(
            "/eguard/swagger-ui/**", "/eguard/v3/api-docs/**", "/eguard/swagger-resources/**", "/eguard/webjars/**",
            "/eguard/configuration/**", "/eguard/auth/login", "/eguard/auth/2fa", "/eguard/auth/renew", "/eguard/setting",
            "/eguard/auth/reset-password", "/eguard/auth/update-password","/eguard/company/list", "/eguard/factory/list");

    /**
     * HTTP 요청을 필터링하여 JWT 토큰을 검증하고, 인증 정보를 설정합니다.
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @param chain 필터 체인
     * @throws ServletException 서블릿 예외
     * @throws IOException 입출력 예외
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain)
            throws ServletException, IOException {
        // 예외 처리할 URI는 필터링을 하지 않고 체인으로 넘어감
        if (EXCLUDED_PATHS.stream().anyMatch(request.getRequestURI()::startsWith)) {
            chain.doFilter(request, response);
            return;
        }
        // JWT 토큰을 HTTP 요청 헤더에서 추출
        var accessToken = jwtUtil.extractTokenFromRequest(request);
        // 로컬 프로파일인 경우, 토큰이 없으면 샘플 데이터를 사용하여 토큰 생성
        if ("local".equals(activeProfile) && accessToken == null) {
            log.info("Local profile detected with no token. Generating test token.");
            // 샘플 데이터를 사용하여 엔티티 생성
            var sampleEmployee = SampleData.Employee.ATEMOS_ADMIN;
            var company = Company.builder()
                    .name(sampleEmployee.getFactory().getCompany().getName())
                    .email(sampleEmployee.getFactory().getCompany().getEmail())
                    .phoneNumber(sampleEmployee.getFactory().getCompany().getPhoneNumber())
                    .address(sampleEmployee.getFactory().getCompany().getAddress())
                    .build();
            var factory = Factory.builder()
                    .name(sampleEmployee.getFactory().getName())
                    .address(sampleEmployee.getFactory().getAddress())
                    .company(company)
                    .build();
            var employee = Employee.builder()
                    .name(encryptUtil.encrypt(sampleEmployee.getName()))
                    .email(encryptUtil.encrypt(sampleEmployee.getEmail()))
                    .phoneNumber(encryptUtil.encrypt(sampleEmployee.getPhoneNumber()))
                    .role(sampleEmployee.getRole())
                    .factory(factory)
                    .build();
            // EmployeeDto.ReadEmployeeResponse 생성
            var employeeResponse = EmployeeDto.ReadEmployeeResponse.builder()
                    .employeeId(employee.getId())
                    .companyId(company.getId())
                    .companyName(company.getName())
                    .companyAddress(company.getAddress() + company.getAddressDetail())
                    .companyBusinessNumber(company.getBusinessNumber())
                    .factoryId(factory.getId())
                    .factoryName(factory.getName())
                    .factoryAddress(factory.getAddress() + factory.getAddressDetail())
                    .employeeEmail(encryptUtil.decrypt(employee.getEmail()))
                    .employeeName(encryptUtil.decrypt(employee.getName()))
                    .employeePhoneNumber(encryptUtil.decrypt(employee.getPhoneNumber()))
                    .healthStatus(EmployeeIncident.NORMAL)  // 기본값으로 설정
                    .authenticationStatus(employee.getAuthenticationStatus())
                    .role(employee.getRole())
                    .accessibleMenuIds(List.of())  // 샘플이므로 비워두거나 적절한 값 설정
                    .createdAt(employee.getCreatedAt())
                    .updatedAt(employee.getUpdatedAt())
                    .build();
            // EmployeeDto.ReadEmployeeResponse를 클레임으로 전달하여 Access Token 생성
            accessToken = jwtUtil.generateAccessToken(employeeResponse);
        }
        if (accessToken != null) {
            // 토큰을 검증
            if (jwtUtil.validateToken(accessToken)) {
                // 검증된 토큰에서 이메일 추출
                var email = jwtUtil.extractEmailFromToken(accessToken);
                // 이메일이 유효하고, 현재 인증이 없다면 사용자 인증 수행
                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // 근로자 정보 조회
                    Employee employee = employeeService.readEmployeeByEmail(email);
                    // 계정 상태 검증
                    authenticationService.validateAccountStatus(employee);
                    // 권한 부여 및 인증 객체 생성
                    var authorities = employee.getAuthorities().stream()
                            .map(authority -> new SimpleGrantedAuthority(authority.getAuthority()))
                            .toList();
                    var authenticationToken = new UsernamePasswordAuthenticationToken(email, accessToken, authorities);
                    // SecurityContext에 인증 객체 설정
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
        }
        // 필터 체인 계속 진행
        chain.doFilter(request, response);
    }
}