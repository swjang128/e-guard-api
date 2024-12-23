package atemos.eguard.api.service;

import atemos.eguard.api.config.EncryptUtil;
import atemos.eguard.api.config.JwtUtil;
import atemos.eguard.api.domain.AuthenticationStatus;
import atemos.eguard.api.domain.EmployeeIncident;
import atemos.eguard.api.dto.EmployeeDto;
import atemos.eguard.api.dto.MenuDto;
import atemos.eguard.api.entity.*;
import atemos.eguard.api.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * AuthenticationServiceImpl는 근로자 인증 및 권한 관련 로직을 처리하는 서비스 클래스입니다.
 * 근로자 로그인, 비밀번호 재설정, 토큰 발급 등의 기능을 제공합니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final JwtUtil jwtUtil;
    private final EmployeeService employeeService;
    private final MenuService menuService;
    private final EmployeeRepository employeeRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TwoFactorAuthRepository twoFactorAuthRepository;
    private final SettingRepository settingRepository;
    private final EventRepository eventRepository;
    private final MenuRepository menuRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final EncryptUtil encryptUtil;

    /**
     * 2차 인증을 위한 인증 번호를 발송합니다.
     *
     * @param authCodeRequest 2차 인증 번호를 발송할 이메일이 포함된 DTO 객체
     */
    @Override
    @Transactional
    public void sendTwoFactorAuthCode(EmployeeDto.AuthCodeRequest authCodeRequest) {
        // 근로자가 존재하는지 조회
        var employee = employeeRepository.findByEmail(encryptUtil.encrypt(authCodeRequest.getEmployeeEmail().trim()))
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 근로자입니다."));
        // 최근 3분 내에 발송된 건을 조회(3분 내에 기존 발송한 건이 있다면 429 Exception 처리)
        Optional<TwoFactorAuth> latestRequest = twoFactorAuthRepository
                .findFirstByEmployeeAndCreatedAtAfterOrderByCreatedAtDesc(employee, LocalDateTime.now().minus(Duration.ofMinutes(3)));
        // 인증 번호를 3분 이내에 다시 호출했을 때 429 응답
        if (latestRequest.isPresent()) {
            LocalDateTime blockedUntil = latestRequest.get().getCreatedAt().plus(Duration.ofMinutes(3));
            if (LocalDateTime.now().isBefore(blockedUntil)) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "인증 번호 요청이 너무 잦습니다. 잠시 후 다시 시도해 주세요.");
            }
        }
        // 인증 번호 생성 (6자리 숫자)
        var authCode = generateRandomNumericCode(6);
        // TwoFactorAuth 엔티티 저장
        var twoFactorAuth = TwoFactorAuth.builder()
                .employee(employee)
                .authCode(authCode)
                .build();
        twoFactorAuthRepository.save(twoFactorAuth);
        // 이메일로 인증 번호 발송
        var subject = "[ATEGuard] 2차 인증 번호 안내";
        var message = String.format("""
                <html><body>
                <p>안녕하세요, %s 님. ATEMoS의 ATEGuard 서비스 운영팀입니다.</p>
                <br>
                <p>2차 인증을 위한 인증 번호는 다음과 같습니다:</p>
                <br>
                <p style="font-size: 1.5em; color: blue; font-weight: bold;">%s</p>
                <br>
                <p>감사합니다.</p>
                <p>ATEMoS 팀 드림</p>
                </body></html>
                """, encryptUtil.decrypt(employee.getName()), authCode);
        emailService.sendEmail(encryptUtil.decrypt(employee.getEmail()), subject, message);
    }

    /**
     * 근로자 로그인 처리
     *
     * @param loginRequest 로그인 요청 정보를 포함하는 데이터 전송 객체
     * @return Access Token 및 Refresh Token이 담긴 Map 객체
     */
    @Override
    @Transactional
    public EmployeeDto.LoginResponse login(EmployeeDto.LoginRequest loginRequest) {
        // 근로자 검증 및 2차 인증이 필요한 경우라면 2차 인증까지 검증
        var employee = authenticateAndValidateEmployee(loginRequest);
        // 해결되지 않은 가장 최근 사건의 건강 상태 조회
        var latestUnresolvedEvent = eventRepository.findTopByEmployeeAndResolvedOrderByCreatedAtDesc(employee, false);
        // 건강 상태 설정
        var healthStatus = latestUnresolvedEvent.map(Event::getEmployeeIncident)
                .orElse(EmployeeIncident.NORMAL);
        // 접근 가능한 메뉴 ID 조회 (MenuRepository를 사용하여 역할 기반으로 메뉴 조회)
        var accessibleMenus = menuRepository.findAllByAccessibleRolesContains(employee.getRole());
        var accessibleMenuIds = accessibleMenus.stream()
                .map(Menu::getId)
                .collect(Collectors.toList());
        // EmployeeDto.ReadEmployeeResponse 생성
        var employeeResponse = EmployeeDto.ReadEmployeeResponse.builder()
                .employeeId(employee.getId())
                .companyId(employee.getFactory().getCompany().getId())
                .companyName(employee.getFactory().getCompany().getName())
                .companyAddress(employee.getFactory().getCompany().getAddress())
                .companyAddressDetail(employee.getFactory().getCompany().getAddressDetail())
                .companyBusinessNumber(employee.getFactory().getCompany().getBusinessNumber())
                .factoryId(employee.getFactory().getId())
                .factoryName(employee.getFactory().getName())
                .factoryAddress(employee.getFactory().getAddress())
                .factoryAddressDetail(employee.getFactory().getAddressDetail())
                .employeeEmail(encryptUtil.decrypt(employee.getEmail()))
                .employeeName(encryptUtil.decrypt(employee.getName()))
                .employeePhoneNumber(encryptUtil.decrypt(employee.getPhoneNumber()))
                .healthStatus(healthStatus)
                .authenticationStatus(employee.getAuthenticationStatus())
                .role(employee.getRole())
                .accessibleMenuIds(accessibleMenuIds)
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .build();
        // Access Token 생성(클레임으로 employeeResponse 사용)
        var accessToken = jwtUtil.generateAccessToken(employeeResponse);
        // Refresh Token 생성
        var refreshToken = jwtUtil.generateRefreshToken(encryptUtil.decrypt(employee.getEmail()));
        // Refresh Token을 저장
        refreshTokenRepository.save(RefreshToken.builder()
                .token(refreshToken)
                .employee(employee)
                .expiresAt(LocalDateTime.now().plus(Duration.ofMillis(jwtUtil.getRefreshTokenExpirationInMillis())))
                .build());
        // Access Token과 Refresh Token을 함께 반환
        return EmployeeDto.LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * JWT 토큰을 사용하여 현재 로그인한 근로자의 정보를 조회합니다.
     *
     * @return 근로자 정보 객체입니다. 근로자의 아이디, 이름, 권한 등의 정보가 포함됩니다.
     */
    @Override
    @Transactional(readOnly = true)
    public EmployeeDto.ReadEmployeeResponse getCurrentEmployeeInfo() {
        // JWT 토큰을 확인
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "권한이 없습니다."); // 한글화
        }
        var token = (String) authentication.getCredentials();
        if (token == null || token.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT 토큰이 없거나 비어 있습니다."); // 한글화
        }
        // 토큰에서 근로자 이메일 추출
        String email = jwtUtil.extractEmailFromToken(token);
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 JWT 토큰입니다."); // 한글화
        }
        // EmployeeService.read 메서드를 사용하여 근로자 정보 조회
        var readRequest = EmployeeDto.ReadEmployeeRequest.builder()
                .employeeEmail(email)
                .size(1) // 한 명만 조회하도록 설정
                .build();
        var employeePageResponse = employeeService.read(readRequest, Pageable.ofSize(1));
        var employeeList = employeePageResponse.getEmployeeList();
        if (employeeList.isEmpty()) {
            throw new EntityNotFoundException("존재하지 않는 근로자입니다.");
        }
        // 근로자 정보를 반환
        return employeeList.getFirst();
    }

    /**
     * JWT 토큰을 사용하여 현재 로그인한 근로자의 접근 권한, 접근 가능한 메뉴를 조회합니다.
     *
     * @return 근로자 권한 정보 객체입니다. 근로자의 접근 권한, 접근 가능한 메뉴를 포함합니다.
     */
    @Override
    @Transactional(readOnly = true)
    public EmployeeDto.ReadEmployeeResponse getCurrentEmployeeAuthority() {
        // JWT 토큰을 확인하고 인증된 근로자 정보 조회
        var employeeInfo = getCurrentEmployeeInfo();
        // 근로자의 권한과 접근 가능한 메뉴 정보를 반환
        return EmployeeDto.ReadEmployeeResponse.builder()
                .role(employeeInfo.getRole())
                .accessibleMenuIds(employeeInfo.getAccessibleMenuIds())
                .build();
    }

    /**
     * Refresh Token을 사용하여 새로운 Access Token을 발급
     * 만료된 Access Token이라도 새로운 Access Token을 발급하도록 처리
     *
     * @param refreshToken 리프레시 토큰
     * @return 새로운 Access Token이 담긴 Map 객체
     */
    @Override
    @Transactional
    public EmployeeDto.LoginResponse renewAccessToken(String refreshToken, HttpServletRequest request) {
        // 만료되거나 잘못된 Refresh Token인지 확인
        var validToken = refreshTokenRepository.findByToken(refreshToken)
                .filter(token -> !token.isExpired())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않거나 만료된 리프레시 토큰입니다."));
        // 해당 토큰으로 근로자 정보 조회
        var employee = employeeRepository.findByEmail(validToken.getEmployee().getEmail())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 근로자입니다."));
        // 접근 가능한 메뉴 정보 가져오기 (MenuService를 사용하여 역할 기반으로 메뉴 조회)
        var readMenuRequest = MenuDto.ReadMenuRequest.builder()
                .accessibleRoles(List.of(employee.getRole())) // 근로자의 역할에 맞는 메뉴 조회
                .build();
        var accessibleMenuIds = menuService.read(readMenuRequest).stream()
                .map(MenuDto.ReadMenuResponse::getMenuId)
                .toList();
        // 근로자 정보를 EmployeeDto.ReadEmployeeResponse로 변환
        var employeeInfo = EmployeeDto.ReadEmployeeResponse.builder()
                .employeeId(employee.getId())
                .companyId(employee.getFactory().getCompany().getId())
                .companyBusinessNumber(employee.getFactory().getCompany().getBusinessNumber())
                .companyName(employee.getFactory().getCompany().getName())
                .companyAddress(employee.getFactory().getCompany().getAddress())
                .companyAddressDetail(employee.getFactory().getCompany().getAddressDetail())
                .factoryId(employee.getFactory().getId())
                .factoryName(employee.getFactory().getName())
                .factoryAddress(employee.getFactory().getAddress())
                .factoryAddressDetail(employee.getFactory().getAddressDetail())
                .employeeEmail(encryptUtil.decrypt(employee.getEmail()))
                .employeeName(encryptUtil.decrypt(employee.getName()))
                .employeePhoneNumber(encryptUtil.decrypt(employee.getPhoneNumber()))
                .healthStatus(EmployeeIncident.NORMAL)  // 기본값으로 설정
                .authenticationStatus(employee.getAuthenticationStatus())
                .role(employee.getRole())
                .accessibleMenuIds(accessibleMenuIds) // 접근 가능한 메뉴 설정
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .build();
        // 새로운 Access Token 발급
        return EmployeeDto.LoginResponse.builder()
                .accessToken(jwtUtil.generateAccessToken(employeeInfo))
                .build();
    }

    /**
     * 비밀번호 초기화 처리
     *
     * @param resetPassword 비밀번호 초기화를 위한 DTO
     */
    @Override
    @Transactional
    public void resetPassword(EmployeeDto.ResetPassword resetPassword) {
        // 근로자가 존재하는지 확인
        var employee = employeeRepository.findByEmail(encryptUtil.encrypt(resetPassword.getEmployeeEmail().trim()))
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 근로자입니다."));
        // 계정 상태 확인
        validateAccountStatus(employee);
        // 숫자로 구성된 무작위 8자리 비밀번호를 생성
        var newPasswordLength = 8;
        var newPassword = generateRandomNumericCode(newPasswordLength);
        // 초기화한 비밀번호로 변경
        updatePasswordAndStatus(employee, newPassword, AuthenticationStatus.PASSWORD_RESET);
        // 비밀번호 초기화 안내 이메일 발송
        var subject = "[ATEGuard] 비밀번호 초기화 안내";
        var message = String.format("""
                <html><body>
                <p>안녕하세요, ATEMoS의 ATEGuard 서비스 운영팀입니다.</p>
                <br>
                <p>%s님의 비밀번호를 초기화하였습니다. 새로운 비밀번호는 아래와 같습니다:</p>
                <br>
                <p style="font-size: 1.5em; color: blue; font-weight: bold;">%s</p>
                <br>
                <p>보안을 위해 즉시 로그인하여 비밀번호를 변경해 주시기 바랍니다.</p>
                <br>
                <p>감사합니다.</p>
                <p>ATEMoS 팀 드림</p>
                </body></html>
            """, encryptUtil.decrypt(employee.getName()), newPassword);
        emailService.sendEmail(encryptUtil.decrypt(employee.getEmail()), subject, message);
    }

    /**
     * 비밀번호 변경 처리
     *
     * @param updatePassword 비밀번호 변경을 위한 DTO
     */
    @Override
    @Transactional
    public void updatePassword(EmployeeDto.UpdatePassword updatePassword) {
        // 근로자가 존재하는지 확인
        var employee = employeeRepository.findByEmail(encryptUtil.encrypt(updatePassword.getEmployeeEmail()))
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 근로자입니다."));
        // 비밀번호 변경이 가능한 계정 상태인지 확인
        if (employee.getAuthenticationStatus() != AuthenticationStatus.PASSWORD_RESET) {
            // 계정이 비밀번호 초기화 상태가 아닌 경우, 계정 상태를 확인하여 문제가 있으면 예외 처리
            validateAccountStatus(employee);
        }
        // 입력된 기존 비밀번호가 일치하지 않는 경우 예외 처리
        if (!passwordEncoder.matches(updatePassword.getPassword(), employee.getPassword())) {
            throw new BadCredentialsException("로그인하는 이메일 또는 암호가 일치하지 않습니다.");
        }
        // 비밀번호를 새 비밀번호로 변경하고 계정 상태를 ACTIVE로 업데이트
        updatePasswordAndStatus(employee, updatePassword.getNewPassword(), AuthenticationStatus.ACTIVE);
    }

    /**
     * 근로자 계정 상태를 검증합니다.
     *
     * @param employee 검증할 근로자 객체
     */
    public void validateAccountStatus(Employee employee) {
        switch (employee.getAuthenticationStatus()) {
            case INACTIVE, SUSPENDED, DELETED -> throw new ResponseStatusException(HttpStatus.FORBIDDEN, "활성화된 계정이 아닙니다. 관리자에게 문의해주세요.");
            case LOCKED -> throw new ResponseStatusException(HttpStatus.LOCKED, "계정이 잠겨있습니다. 비밀번호를 재설정해주세요.");
            case AuthenticationStatus.PASSWORD_RESET -> throw new ResponseStatusException(HttpStatus.CONFLICT, "비밀번호가 재설정되었습니다. 로그인 전에 비밀번호를 변경해주세요.");
            case WITHDRAWN -> throw new ResponseStatusException(HttpStatus.NOT_FOUND, "탈퇴한 계정입니다.");
            case AuthenticationStatus.ACTIVE -> {}
            default -> throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "계정 조회 중 서버 오류가 발생하였습니다..");
        }
    }

    /**
     * 근로자의 인증 및 검증을 처리합니다.
     *
     * @param loginRequest 근로자 인증에 필요한 데이터
     * @return 인증된 근로자 객체
     */
    private Employee authenticateAndValidateEmployee(EmployeeDto.LoginRequest loginRequest) {
        // 근로자가 존재하는지 검증
        var employee = employeeRepository.findByEmail(encryptUtil.encrypt(loginRequest.getEmployeeEmail().trim()))
                .orElseThrow(() -> new BadCredentialsException("이메일 또는 비밀번호를 확인해 주세요."));
        // 근로자 계정 상태 검증
        validateAccountStatus(employee);
        // 비밀번호가 일치하지 않을 때
        if (!passwordEncoder.matches(loginRequest.getPassword(), employee.getPassword())) {
            // 비밀번호를 틀린 횟수 1회 추가
            employee.setFailedLoginAttempts(employee.getFailedLoginAttempts() + 1);
            // 비밀번호를 틀린 횟수가 5회 이상일 경우 계정 잠금 처리
            if (employee.getFailedLoginAttempts() >= 5) {
                employee.setAuthenticationStatus(AuthenticationStatus.LOCKED);
                employeeRepository.save(employee);
                throw new ResponseStatusException(HttpStatus.LOCKED, "비밀번호 입력 실패가 너무 많아 계정이 잠겼습니다. 비밀번호를 재설정해 주세요.");
            }
            employeeRepository.saveAndFlush(employee);
            throw new BadCredentialsException("이메일 또는 비밀번호를 확인해 주세요.");
        }
        // 해당 근로자가 속한 업체의 시스템 설정에서 2차 인증 사용 여부 확인
        var setting = settingRepository.findByCompanyId(employee.getFactory().getCompany().getId())
                .orElseThrow(() -> new EntityNotFoundException("시스템 설정이 존재하지 않습니다."));
        // 2차 인증이 필요한 경우에만 2차 인증 로직 수행
        if (setting.getTwoFactorAuthenticationEnabled()) {
            // 인증 코드 유효 기간 설정 (분 단위)
            final int CODE_VALIDITY_MINUTES = 5;
            // 가장 최근에 발송된 검증되지 않은 인증 정보 가져오기
            var twoFactorAuth = twoFactorAuthRepository
                    .findFirstByEmployeeAndIsVerifiedFalseOrderByCreatedAtDesc(employee)
                    .orElseThrow(() -> new EntityNotFoundException("인증 코드가 없습니다. 새 인증 코드를 요청해 주세요."));
            // 인증 코드의 유효 기간 확인 및 예외 처리
            Optional.of(twoFactorAuth)
                    .filter(auth -> Duration.between(auth.getCreatedAt(), LocalDateTime.now()).toMinutes() < CODE_VALIDITY_MINUTES)
                    .orElseThrow(() -> new BadCredentialsException("인증 코드의 유효 기간이 만료되었습니다. 새 인증 코드를 요청해 주세요."));
            // 인증 코드 검증 및 처리
            if (twoFactorAuth.getAuthCode().equals(loginRequest.getAuthCode().trim())) {
                twoFactorAuth.setVerified(true);
            } else {
                twoFactorAuth.setFailedAttempts(twoFactorAuth.getFailedAttempts() + 1);
                throw new BadCredentialsException("%s로 전송된 인증 코드를 확인해 주세요.".formatted(encryptUtil.decrypt(employee.getEmail())));
            }
            // 변경된 TwoFactorAuth 저장
            twoFactorAuthRepository.save(twoFactorAuth);
        }
        // 로그인에 성공하면 비밀번호 틀린 횟수를 0으로 초기화
        employee.setFailedLoginAttempts(0);
        employeeRepository.saveAndFlush(employee);
        return employee;
    }

    /**
     * 근로자 비밀번호 및 상태 업데이트
     *
     * @param employee 근로자 객체
     * @param newPassword 새 비밀번호
     * @param newStatus 새 상태
     */
    private void updatePasswordAndStatus(Employee employee, String newPassword, AuthenticationStatus newStatus) {
        employee.setPassword(passwordEncoder.encode(newPassword));
        employee.setAuthenticationStatus(newStatus);
        employee.setFailedLoginAttempts(0);
        employeeRepository.save(employee);
    }

    /**
     * 랜덤 숫자 인증 코드 생성
     *
     * @param length 생성할 코드의 길이
     * @return 랜덤 생성된 숫자 문자열
     */
    private String generateRandomNumericCode(int length) {
        var digits = "0123456789";
        var random = new SecureRandom();
        var code = new char[length];
        for (int i = 0; i < length; i++) {
            code[i] = digits.charAt(random.nextInt(digits.length()));
        }
        return new String(code);
    }
}