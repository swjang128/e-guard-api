package atemos.eguard.api.service;

import atemos.eguard.api.config.EncryptUtil;
import atemos.eguard.api.config.EntityValidator;
import atemos.eguard.api.domain.EmployeeIncident;
import atemos.eguard.api.domain.EmployeeRole;
import atemos.eguard.api.domain.SampleData;
import atemos.eguard.api.domain.WorkStatus;
import atemos.eguard.api.dto.EmployeeDto;
import atemos.eguard.api.dto.EventDto;
import atemos.eguard.api.dto.SettingDto;
import atemos.eguard.api.entity.Employee;
import atemos.eguard.api.entity.Menu;
import atemos.eguard.api.repository.EmployeeRepository;
import atemos.eguard.api.repository.FactoryRepository;
import atemos.eguard.api.repository.MenuRepository;
import atemos.eguard.api.repository.WorkRepository;
import atemos.eguard.api.specification.EmployeeSpecification;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * EmployeeServiceImpl 클래스는 근로자(Employee) 관련된 기능을 구현하는 서비스 클래스입니다.
 * 근로자의 생성, 조회, 수정, 삭제 기능을 제공하며, 근로자 정보의 암호화 및 복호화,
 * 접근 가능한 메뉴 설정 등의 기능을 포함합니다.
 */
@Service
@Slf4j
@AllArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final FactoryRepository factoryRepository;
    private final WorkRepository workRepository;
    private final MenuRepository menuRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final EventService eventService;
    private final EncryptUtil encryptUtil;
    private final EntityValidator entityValidator;
    private final SettingService settingService;

    /**
     * 근로자를 등록하는 메서드입니다.
     * @param createEmployeeDto 근로자 생성 정보
     * @return 등록된 근로자 정보 응답 객체
     */
    @Override
    @Transactional
    public EmployeeDto.ReadEmployeeResponse create(EmployeeDto.CreateEmployee createEmployeeDto) {
        // 현재 접속한 근로자가 신규 알람에 등록할 공장으로 접근 가능한지 검증 및 조회
        var factory = entityValidator.validateFactoryIds(List.of(createEmployeeDto.getFactoryId()))
                .stream().findFirst().orElseThrow(() -> new AccessDeniedException("공장을 찾을 수 없거나 등록 권한이 없습니다."));
        // 해당 업체의 시스템 설정에서 공장에 등록할 수 있는 최대 근로자 생성량을 초과했는지 검증
        var setting = settingService.read(SettingDto.ReadSettingRequest.builder()
                .companyIds(List.of(factory.getCompany().getId()))
                .build());
        if (employeeRepository.findByFactory(factory).size() > setting.getSettingList().getFirst().getMaxEmployeesPerFactory()) {
            throw new IllegalArgumentException("이 공장에 등록할 수 있는 근로자 수가 최대치에 도달하였습니다. 추가 등록하려면 시스템 설정을 변경해주세요.");
        }
        // 이메일 및 전화번호 중복 확인
        checkDuplicateEmployee(createEmployeeDto.getEmployeeEmail(), createEmployeeDto.getEmployeePhoneNumber());
        // 비밀번호가 null이고 Role이 WORKER 경우 기본 패스워드로 저장(그 외에는 비밀번호가 없다고 400 에러 리턴)
        var password = Optional.ofNullable(createEmployeeDto.getPassword())
                .orElseGet (() -> {
                    if (createEmployeeDto.getRole() == EmployeeRole.WORKER) {
                        return SampleData.Employee.ATEMOS_WORKER.getPassword();
                    }
                    throw new IllegalArgumentException("비밀번호를 입력해주세요.");
        });
        // Employee 엔티티 생성 및 저장 (이름, 이메일, 전화번호 암호화)
        var employee = Employee.builder()
                .factory(factory)
                .name(encryptUtil.encrypt(createEmployeeDto.getEmployeeName()))
                .email(encryptUtil.encrypt(createEmployeeDto.getEmployeeEmail()))
                .phoneNumber(encryptUtil.encrypt(createEmployeeDto.getEmployeePhoneNumber()))
                .password(passwordEncoder.encode(password))
                .role(createEmployeeDto.getRole())
                .employeeNumber(createEmployeeDto.getEmployeeNumber())
                .build();
        // 접근 가능한 메뉴 조회
        var accessibleMenuIds = menuRepository.findAllByAccessibleRolesContains(createEmployeeDto.getRole()).stream()
                .map(Menu::getId)
                .collect(Collectors.toList());
        // 엔티티 저장
        employeeRepository.save(employee);
        // 웰컴 메일 발송
        var subject = "[ATEGuard] 환영합니다!";
        var message = String.format("""
                <html><body>
                <p>%s 님께,</p>
                <br>
                <p>ATEMoS의 ATEGuard 플랫폼에 오신 것을 진심으로 환영합니다!</p>
                <p>ATEGuard는 밀폐 공간에서 작업하는 근로자들의 안전을 최우선으로 생각하는 첨단 관리 도구를 제공합니다.</p>
                <br>
                <p>저희의 혁신적인 시스템을 통해 작업 현황을 실시간으로 모니터링하고, 긴급 상황 발생 시 즉각적인 알림을 받을 수 있습니다. 이를 통해 안전한 작업 환경을 조성하고 사고를 예방할 수 있습니다.</p>
                <br>
                <p>지금 바로 대시보드에 로그인하여 다양한 기능을 확인하고, ATEGuard와 함께 안전한 작업 환경을 만들어보세요.</p>
                <br>
                <p>저희와 함께 작업자의 안전을 지켜나가길 기대합니다!</p>
                <p>감사합니다.</p>
                <p>ATEMoS 팀 드림</p>
                </body></html>
            """, encryptUtil.decrypt(employee.getName()));
        emailService.sendEmail(encryptUtil.decrypt(employee.getEmail()), subject, message);
        // 기본적으로 EventType.NORMAL을 할당하여 EventDto.ReadEventResponse 객체 생성
        var healthStatus = createEmployeeDto.getHealthStatus() != null ? createEmployeeDto.getHealthStatus() : EmployeeIncident.NORMAL;
        // 생성한 근로자 정보를 응답 객체에 담아서 리턴
        return EmployeeDto.ReadEmployeeResponse.builder()
                .employeeId(employee.getId())
                .companyId(factory.getCompany().getId())
                .companyBusinessNumber(factory.getCompany().getBusinessNumber())
                .companyName(factory.getCompany().getName())
                .companyAddress(factory.getCompany().getAddress())
                .companyAddressDetail(factory.getCompany().getAddressDetail())
                .factoryId(factory.getId())
                .factoryName(factory.getName())
                .factoryAddress(factory.getAddress())
                .factoryAddressDetail(factory.getAddressDetail())
                .employeeName(encryptUtil.decrypt(employee.getName()))
                .employeeEmail(encryptUtil.decrypt(employee.getEmail()))
                .employeePhoneNumber(encryptUtil.decrypt(employee.getPhoneNumber()))
                .role(employee.getRole())
                .healthStatus(healthStatus)
                .authenticationStatus(employee.getAuthenticationStatus())
                .accessibleMenuIds(accessibleMenuIds)
                .employeeNumber(employee.getEmployeeNumber())
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .build();
    }

    /**
     * 조건에 맞는 근로자 목록을 조회하는 메서드입니다.
     * @param readEmployeeRequestDto 근로자 조회 조건
     * @param pageable 페이징 정보
     * @return 조건에 맞는 근로자 목록과 페이징 정보
     */
    @Override
    @Transactional(readOnly = true)
    public EmployeeDto.ReadEmployeeResponseList read(EmployeeDto.ReadEmployeeRequest readEmployeeRequestDto, Pageable pageable) {
        // readEmployeeRequestDto에 근로자 ID 리스트가 존재하면 이 정보에 현재 접속한 근로자가 접근할 수 있는지 검증
        Optional.ofNullable(readEmployeeRequestDto.getEmployeeIds())
                .filter(ids -> !ids.isEmpty())
                .ifPresent(employeeIds -> {
                    if (entityValidator.validateEmployeeIds(employeeIds).isEmpty()) {
                        throw new AccessDeniedException("근로자가 존재하지 않거나 조회 권한이 없습니다.");
                    }
                });
        // readEmployeeRequestDto에 공장 ID 리스트가 존재하면 이 정보에 현재 접속한 근로자가 접근할 수 있는지 검증
        Optional.ofNullable(readEmployeeRequestDto.getFactoryIds())
                .filter(ids -> !ids.isEmpty())
                .ifPresent(factoryIds -> {
                    if (entityValidator.validateFactoryIds(factoryIds).isEmpty()) {
                        throw new AccessDeniedException("공장이 존재하지 않거나 조회 권한이 없습니다.");
                    }
                });
        // 조건에 맞는 근로자 목록 조회
        var employeePage = employeeRepository.findAll(EmployeeSpecification.findWith(readEmployeeRequestDto, encryptUtil), pageable);
        // 엔티티 목록을 DTO로 변환하여 리턴
        var employeeList = employeePage.getContent().stream()
                .map(employee -> {
                    // Employee에게 일어났던 해결되지 않은 가장 최근 사건 조회
                    var latestUnresolvedEvent = eventService.read(EventDto.ReadEventRequest.builder()
                                    .employeeIds(List.of(employee.getId()))
                                    .eventResolved(false)  // 해결되지 않은 사건만 조회
                                    .page(0)           // 가장 최신의 사건을 가져오기 위해 페이지 번호를 0으로 설정
                                    .size(1)           // 1개의 사건만 필요
                                    .build(),
                            Pageable.ofSize(1)).getEventList().stream().findFirst();
                    // 최근 해결되지 않은 사건이 있을 경우 해당 사건의 healthStatus를 사용, 없을 경우 기본값
                    var healthStatus = latestUnresolvedEvent.map(EventDto.ReadEventResponse::getEmployeeIncident)
                            .orElse(EmployeeIncident.NORMAL);
                    // healthStatuses 조건에 맞게 필터링
                    if (readEmployeeRequestDto.getHealthStatuses() != null && !readEmployeeRequestDto.getHealthStatuses().isEmpty()) {
                        if (!readEmployeeRequestDto.getHealthStatuses().contains(healthStatus)) {
                            return null; // healthStatuses에 맞지 않는 상태인 경우 해당 Employee는 제외
                        }
                    }
                    // 이 근로자가 투입된 작업이 있는지 조회
                    var hasAssignedWork = workRepository.existsByEmployeesContainingAndStatusIn(employee, List.of(WorkStatus.PENDING, WorkStatus.IN_PROGRESS));
                    // 근로자가 작업에 투입된 경우 작업 ID와 이름 조회
                    var workId = hasAssignedWork && !employee.getWorks().isEmpty() ? employee.getWorks().getFirst().getId() : null;
                    var workName = hasAssignedWork && !employee.getWorks().isEmpty() ? employee.getWorks().getFirst().getName() : null;
                    // 복호화 및 마스킹 처리
                    var applyMasking = Boolean.TRUE.equals(readEmployeeRequestDto.getMasking());
                    var name = decryptAndMask(employee.getName(), applyMasking, this::maskName);
                    var email = decryptAndMask(employee.getEmail(), applyMasking, this::maskEmail);
                    var phoneNumber = decryptAndMask(employee.getPhoneNumber(), applyMasking, this::maskPhoneNumber);
                    // EmployeeDTO 응답 객체로 Build(name, email, phone은 마스킹 처리)
                    return EmployeeDto.ReadEmployeeResponse.builder()
                            .employeeId(employee.getId())
                            .employeeNumber(employee.getEmployeeNumber())
                            .companyId(employee.getFactory().getCompany().getId())
                            .companyBusinessNumber(employee.getFactory().getCompany().getBusinessNumber())
                            .companyName(employee.getFactory().getCompany().getName())
                            .companyAddress(employee.getFactory().getCompany().getAddress())
                            .companyAddressDetail(employee.getFactory().getCompany().getAddressDetail())
                            .factoryId(employee.getFactory().getId())
                            .factoryName(employee.getFactory().getName())
                            .factoryAddress(employee.getFactory().getAddress())
                            .factoryAddressDetail(employee.getFactory().getAddressDetail())
                            .healthStatus(healthStatus)
                            .employeeName(name)
                            .employeeEmail(email)
                            .employeePhoneNumber(phoneNumber)
                            .role(employee.getRole())
                            .workId(workId)
                            .workName(workName)
                            .createdAt(employee.getCreatedAt())
                            .updatedAt(employee.getUpdatedAt())
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();
        // 필터링된 employeeList로 totalElements 계산
        long filteredTotalElements = employeeList.size();
        // 총 페이지 수 계산
        int totalPages = pageable.isPaged()
                ? (int) Math.ceil((double) filteredTotalElements / pageable.getPageSize())
                : 1;
        // 응답 객체 반환
        return new EmployeeDto.ReadEmployeeResponseList(
                employeeList,
                filteredTotalElements,
                totalPages);
    }

    /**
     * 근로자의 정보를 수정하는 메서드입니다.
     * @param employeeId 근로자 ID
     * @param updateEmployeeDto 근로자 정보 수정 데이터
     * @return 수정된 근로자 정보 응답 객체
     */
    @Override
    @Transactional
    public EmployeeDto.ReadEmployeeResponse update(Long employeeId, EmployeeDto.UpdateEmployee updateEmployeeDto) {
        // 기존 근로자를 현재 접속한 근로자가 수정할 수 있는 권한이 있는지 검증 후 조회
        var employee = entityValidator.validateEmployeeIds(List.of(employeeId))
                .stream().findFirst().orElseThrow(() -> new AccessDeniedException("근로자를 찾을 수 없거나 수정 권한이 없습니다."));
        // 기존 근로자에 등록된 공장에 현재 접속한 근로자가 수정 가능한지 검증
        entityValidator.validateFactoryIds(List.of(employee.getFactory().getId()))
                .stream().findFirst().orElseThrow(() -> new AccessDeniedException("공장을 찾을 수 없거나 수정 권한이 없습니다."));
        // 수정할 근로자 정보에 공장 ID가 존재하면 이 공장에 현재 접속한 근로자가 접근할 수 있는지 검증 후 조회하여 employee 엔티티에 Set
        Optional.ofNullable(updateEmployeeDto.getFactoryId()).ifPresent(factoryId -> {
            var factory = entityValidator.validateFactoryIds(List.of(factoryId))
                    .stream().findFirst().orElseThrow(() -> new AccessDeniedException("공장을 찾을 수 없거나 수정 권한이 없습니다."));
            employee.setFactory(factory);
        });
        // 해당 공장이 존재하는지 확인하고 업데이트
        var factory = Optional.ofNullable(updateEmployeeDto.getFactoryId())
                .map(factoryId -> factoryRepository.findById(factoryId)
                        .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 공장입니다. ID: " + factoryId)))
                .orElse(employee.getFactory());
        employee.setFactory(factory);
        // 근로자 정보 업데이트 (Optional로 필드 업데이트)
        Optional.ofNullable(updateEmployeeDto.getEmployeeName())
                .ifPresent(name -> employee.setName(encryptUtil.encrypt(name)));
        Optional.ofNullable(updateEmployeeDto.getEmployeeEmail())
                .ifPresent(email -> employee.setEmail(encryptUtil.encrypt(email)));
        Optional.ofNullable(updateEmployeeDto.getEmployeePhoneNumber())
                .ifPresent(phone -> employee.setPhoneNumber(encryptUtil.encrypt(phone)));
        Optional.ofNullable(updateEmployeeDto.getPassword())
                .ifPresent(password -> employee.setPassword(passwordEncoder.encode(password)));
        Optional.ofNullable(updateEmployeeDto.getAuthenticationStatus())
                .ifPresent(employee::setAuthenticationStatus);
        Optional.ofNullable(updateEmployeeDto.getRole())
                .ifPresent(employee::setRole);
        Optional.ofNullable(updateEmployeeDto.getEmployeeNumber())
                .ifPresent(employee::setEmployeeNumber);
        // 접근 가능한 메뉴 조회
        var accessibleMenuIds = menuRepository.findAllByAccessibleRolesContains(
                        Optional.ofNullable(updateEmployeeDto.getRole()).orElse(employee.getRole())
                ).stream()
                .map(Menu::getId)
                .collect(Collectors.toList());
        // 가장 최근 해결되지 않은 사건의 상태 또는 기본값 설정
        var event = eventService.read(EventDto.ReadEventRequest.builder()
                        .employeeIds(List.of(employee.getId()))
                        .eventResolved(false)
                        .page(0)
                        .size(1)
                        .build(), Pageable.ofSize(1))
                .getEventList()
                .stream()
                .findFirst()
                .map(EventDto.ReadEventResponse::getEmployeeIncident)
                .orElse(EmployeeIncident.NORMAL);
        // 엔티티 저장
        employeeRepository.save(employee);
        // 응답 객체 생성 및 반환
        return EmployeeDto.ReadEmployeeResponse.builder()
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
                .employeeName(encryptUtil.decrypt(employee.getName()))
                .employeeEmail(encryptUtil.decrypt(employee.getEmail()))
                .employeePhoneNumber(encryptUtil.decrypt(employee.getPhoneNumber()))
                .failedLoginAttempts(employee.getFailedLoginAttempts())
                .role(employee.getRole())
                .healthStatus(event)
                .authenticationStatus(employee.getAuthenticationStatus())
                .accessibleMenuIds(accessibleMenuIds)
                .employeeNumber(employee.getEmployeeNumber())
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .build();
    }

    /**
     * 근로자를 삭제하는 메서드입니다.
     * @param employeeId 근로자 ID
     */
    @Override
    @Transactional
    public void delete(Long employeeId) {
        // 근로자 정보가 있는지 확인
        var employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 근로자입니다."));
        // 근로자 정보 삭제
        employeeRepository.delete(employee);
    }

    /**
     * 이메일 또는 전화번호가 중복되는지 확인하는 메서드입니다.
     * @param email 근로자 이메일
     * @param phoneNumber 근로자 전화번호
     */
    @Override
    @Transactional(readOnly = true)
    public void checkDuplicateEmployee(String email, String phoneNumber) {
        var encryptedEmail = encryptUtil.encrypt(email);
        var encryptedPhoneNumber = encryptUtil.encrypt(phoneNumber);
        if (employeeRepository.existsByEmailOrPhoneNumber(encryptedEmail, encryptedPhoneNumber)) {
            throw new EntityExistsException("이미 동일한 이메일이나 연락처를 보유한 근로자가 있습니다.");
        }
    }

    /**
     * 근로자 이메일로 근로자 정보를 로드합니다.
     *
     * @param email 근로자 이메일
     * @return 근로자 엔티티 객체
     */
    @Override
    public Employee readEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(encryptUtil.encrypt(email))
                .orElseThrow(() -> new EntityNotFoundException("근로자가 존재하지 않습니다."));
    }

    /**
     * 암호화된 값을 복호화하고, 필요한 경우 마스킹 처리하는 메서드입니다.
     * @param encryptedValue 암호화된 값
     * @param applyMasking 마스킹 처리 여부
     * @param maskFunction 마스킹 함수
     * @return 복호화된 값 (마스킹 적용 시 마스킹된 값)
     */
    private String decryptAndMask(String encryptedValue, boolean applyMasking, Function<String, String> maskFunction) {
        var decryptedValue = encryptUtil.decrypt(encryptedValue);
        return applyMasking ? maskFunction.apply(decryptedValue) : decryptedValue;
    }

    /**
     * 이름 마스킹: 이름의 첫 글자만 남기고 나머지는 별표(*) 처리
     * @param name 이름
     * @return 마스킹된 이름
     */
    private String maskName(String name) {
        return (name == null || name.length() < 2) ? name : name.charAt(0) + "*".repeat(name.length() - 1);
    }

    /**
     * 이메일 마스킹: '@' 이전의 절반을 별표(*) 처리
     * @param email 이메일
     * @return 마스킹된 이메일
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        var parts = email.split("@");
        return parts[0].charAt(0) + "*".repeat(parts[0].length() - 1) + "@" + parts[1];
    }

    /**
     * 전화번호 마스킹: 중간 4자리를 별표(*) 처리
     * @param phoneNumber 전화번호
     * @return 마스킹된 전화번호
     */
    private String maskPhoneNumber(String phoneNumber) {
        return (phoneNumber == null || phoneNumber.length() < 4) ? phoneNumber : phoneNumber.substring(0, phoneNumber.length() - 4) + "****";
    }
}