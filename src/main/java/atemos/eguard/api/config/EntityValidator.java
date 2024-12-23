package atemos.eguard.api.config;

import atemos.eguard.api.domain.EmployeeRole;
import atemos.eguard.api.entity.*;
import atemos.eguard.api.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * EntityValidator 클래스는 현재 인증된 사용자(근로자)에 대한 정보와 관련된 접근 권한을 검증하는 역할을 담당합니다.
 * 사용자가 접근하려는 알람, 사건, 공장, 업체, 근로자, 구역, 작업, 시스템 설정 등의 엔터티 ID 목록을 받아와
 * 해당 엔터티들이 현재 사용자에게 적절한 접근 권한이 있는지 확인합니다.
 * 만약 사용자에게 접근 권한이 없거나 엔터티가 존재하지 않는 경우 예외를 발생시킵니다.
 * ADMIN 권한이 있는 사용자는 모든 엔터티에 접근할 수 있습니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EntityValidator {
    private final CompanyRepository companyRepository;
    private final FactoryRepository factoryRepository;
    private final AlarmRepository alarmRepository;
    private final EventRepository eventRepository;
    private final EmployeeRepository employeeRepository;
    private final AreaRepository areaRepository;
    private final WorkRepository workRepository;
    private final SettingRepository settingRepository;
    private final JwtUtil jwtUtil;

    /**
     * 현재 인증된 사용자의 employeeId를 기반으로 근로자 정보를 조회합니다.
     * @return 현재 인증된 근로자 (Employee)
     * @throws EntityNotFoundException 현재 인증된 근로자가 존재하지 않으면 예외 발생
     */
    @Transactional(readOnly = true)
    public Employee getCurrentEmployee() {
        // JWT 토큰에서 employeeId 추출
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "권한이 없습니다.");
        }
        // 토큰에서 클레임 정보 추출
        var token = (String) authentication.getCredentials();
        if (token == null || token.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT 토큰이 없거나 비어 있습니다.");
        }
        // employeeId를 JWT 클레임에서 안전하게 추출
        Map<String, Object> claims = jwtUtil.extractClaimsFromToken(token);
        Object employeeIdObj = claims.get("employeeId");
        if (employeeIdObj == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.");
        }
        // Integer 또는 Long으로 처리
        long employeeId;
        if (employeeIdObj instanceof Integer) {
            employeeId = ((Integer) employeeIdObj).longValue();
        } else if (employeeIdObj instanceof Long) {
            employeeId = (Long) employeeIdObj;
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 employeeId 데이터 타입입니다.");
        }
        // employeeId로 Employee 조회
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("현재 로그인한 근로자를 찾을 수 없습니다."));
    }

    /**
     * 알람 ID 목록에 대한 검증 및 검증된 결과만 리턴합니다.
     * 주어진 알람 ID 목록이 현재 접속한 근로자가 속한 공장의 알람인지 검증하고, 존재하지 않는 알람에 대한 예외 처리도 수행합니다.
     * 현재 접속한 근로자와 다른 업체의 공장에 속한 구역에서 발생한 사건의 알람이거나 다른 업체의 공장에 속한 근로자에게 발생한 사건의 알람인 경우 해당 알람에 대한 접근을 제한합니다.
     * ADMIN이 아닌 경우에만 검증을 수행합니다.
     *
     * @param alarmIds 검증할 알람 ID 목록
     * @return 검증된 알람 목록
     */
    @Transactional(readOnly = true)
    public List<Alarm> validateAlarmIds(List<Long> alarmIds) {
        // 주어진 알람 ID 목록을 검증하여 알람을 조회하고, 해당 알람에 대한 접근 권한을 확인합니다.
        return alarmIds.stream()
                .map(alarmRepository::findById)
                .map(alarm -> alarm.orElseThrow(() -> new EntityNotFoundException("존재하지 않는 알람입니다."))) // 알람이 없으면 예외 발생
                .filter(alarm -> hasAccessToFactory(alarm.getEmployee().getFactory().getId())) // 공장 접근 권한을 확인
                .peek(alarm -> {
                    // 알람이 호출된 근로자가 속한 공장의 권한과 일치하지 않으면 예외를 발생시킵니다.
                    if (!hasAccessToFactory(alarm.getEmployee().getFactory().getId())) {
                        log.warn("알람 ID {}는 호출한 근로자가 속한 공장과 다릅니다.", alarm.getId());
                        throw new AccessDeniedException("권한이 없습니다. 알람 ID: " + alarm.getId());
                    }
                })
                .toList();
    }

    /**
     * 사건 ID 목록에 대한 검증 및 검증된 결과만 리턴합니다.
     * 주어진 사건 ID 목록이 현재 접속한 근로자가 속한 공장에서 발생한 사건인지 검증하고, 존재하지 않는 사건에 대한 예외 처리도 수행합니다.
     * 현재 접속한 근로자와 다른 업체의 공장에 속한 구역에서 발생한 사건이거나 다른 업체의 공장에 속한 근로자에게 발생한 사건인 경우 해당 사건에 대한 접근을 제한합니다.
     * ADMIN이 아닌 경우에만 검증을 수행합니다.
     *
     * @param eventIds 검증할 사건 ID 목록
     * @return 검증된 사건 목록
     */
    @Transactional(readOnly = true)
    public List<Event> validateEventIds(List<Long> eventIds) {
        return eventIds.stream()
                .map(eventRepository::findById)
                .map(event -> event.orElseThrow(() -> new EntityNotFoundException("존재하지 않는 사건입니다."))) // 사건이 없으면 예외 발생
                .filter(event -> {
                    Long factoryId = Optional.ofNullable(event.getArea())
                            .map(Area::getFactory)
                            .map(Factory::getId)
                            .orElseGet(() -> Optional.ofNullable(event.getEmployee())
                                    .map(Employee::getFactory)
                                    .map(Factory::getId)
                                    .orElse(null)); // Area와 Employee 모두 null일 경우 null 반환
                    if (factoryId == null) {
                        throw new AccessDeniedException("공장 정보가 없는 사건입니다. 사건 ID: " + event.getId());
                    }
                    return hasAccessToFactory(factoryId); // 접근 권한 확인
                })
                .peek(event -> {
                    Long factoryId = Optional.ofNullable(event.getArea())
                            .map(Area::getFactory)
                            .map(Factory::getId)
                            .orElseGet(() -> event.getEmployee().getFactory().getId());
                    if (!hasAccessToFactory(factoryId)) {
                        log.warn("사건 ID {}는 호출한 근로자가 속한 공장과 다릅니다.", event.getId());
                        throw new AccessDeniedException("권한이 없습니다. 사건 ID: " + event.getId());
                    }
                })
                .toList();
    }

    /**
     * 공장 ID 목록에 대한 검증 및 검증된 결과만 리턴합니다.
     * 주어진 공장 ID 목록이 현재 접속한 근로자가 속한 공장인지 검증하고, 존재하지 않는 공장에 대한 예외 처리도 수행합니다.
     * 현재 접속한 근로자와 다른 업체의 공장인 경우 해당 공장에 대한 접근을 막습니다.
     * ADMIN이 아닌 경우에만 검증을 수행합니다.
     *
     * @param factoryIds 검증할 공장 ID 목록
     * @return 검증된 공장 목록
     */
    @Transactional(readOnly = true)
    public List<Factory> validateFactoryIds(List<Long> factoryIds) {
        // 주어진 공장 ID 목록을 검증하여 공장을 조회하고, 해당 공장에 대한 접근 권한을 확인합니다.
        return factoryIds.stream()
                .map(factoryRepository::findById)
                .map(factory -> factory.orElseThrow(() -> new EntityNotFoundException("존재하지 않는 공장입니다."))) // 공장이 없으면 예외 발생
                .filter(factory -> hasAccessToFactory(factory.getId())) // 공장 접근 권한을 확인
                .peek(factory -> {
                    // 접근 권한이 없으면 예외를 발생시킵니다.
                    if (!hasAccessToFactory(factory.getId())) {
                        log.warn("공장 ID {}는 호출한 근로자가 속한 공장과 다릅니다.", factory.getId());
                        throw new AccessDeniedException("권한이 없습니다. 공장 ID: " + factory.getId());
                    }
                })
                .toList();
    }

    /**
     * 업체 ID 목록에 대한 검증 및 검증된 결과만 리턴합니다.
     * 주어진 업체 ID 목록이 현재 접속한 근로자가 속한 업체인지 검증하고, 존재하지 않는 업체에 대한 예외 처리도 수행합니다.
     * 현재 접속한 근로자와 다른 업체인 경우 해당 업체에 대한 접근을 막습니다.
     * ADMIN이 아닌 경우에만 검증을 수행합니다.
     *
     * @param companyIds 검증할 업체 ID 목록
     * @return 검증된 업체 목록
     */
    @Transactional(readOnly = true)
    public List<Company> validateCompanyIds(List<Long> companyIds) {
        // 주어진 업체 ID 목록을 검증하여 업체를 조회하고, 해당 업체에 대한 접근 권한을 확인합니다.
        return companyIds.stream()
                .map(companyRepository::findById)
                .map(company -> company.orElseThrow(() -> new EntityNotFoundException("존재하지 않는 업체입니다."))) // 업체가 없으면 예외 발생
                .filter(company -> hasAccessToCompany(company.getId())) // 업체 접근 권한을 확인
                .peek(company -> {
                    // 접근 권한이 없으면 예외를 발생시킵니다.
                    if (!hasAccessToCompany(company.getId())) {
                        log.warn("업체 ID {}는 호출한 근로자가 속한 업체와 다릅니다.", company.getId());
                        throw new AccessDeniedException("권한이 없습니다. 업체 ID: " + company.getId());
                    }
                })
                .toList();
    }

    /**
     * 근로자 ID 목록에 대한 검증 및 검증된 결과만 리턴합니다.
     * 주어진 근로자 ID 목록이 현재 접속한 근로자가 속한 공장의 근로자인지 검증하고, 존재하지 않는 근로자에 대한 예외 처리도 수행합니다.
     * 현재 접속한 근로자와 다른 공장에 속한 근로자에 접근한 경우 헤당 근로자에 대한 접근을 막습니다.
     * ADMIN이 아닌 경우에만 검증을 수행합니다.
     *
     * @param employeeIds 검증할 근로자 ID 목록
     * @return 검증된 근로자 목록
     */
    @Transactional(readOnly = true)
    public List<Employee> validateEmployeeIds(List<Long> employeeIds) {
        // 주어진 근로자 ID 목록을 검증하여 근로자를 조회하고, 해당 근로자에 대한 접근 권한을 확인합니다.
        return employeeIds.stream()
                .map(employeeRepository::findById)
                .map(employee -> employee.orElseThrow(() -> new EntityNotFoundException("존재하지 않는 근로자입니다."))) // 근로자가 없으면 예외 발생
                .filter(employee -> hasAccessToFactory(employee.getFactory().getId())) // 근로자 접근 권한을 확인
                .peek(employee -> {
                    // 근로자가 호출한 근로자가 속한 공장과 다르면 예외를 발생시킵니다.
                    if (!hasAccessToFactory(employee.getFactory().getId())) {
                        log.warn("근로자 ID {}는 호출한 근로자가 속한 공장과 다릅니다.", employee.getId());
                        throw new AccessDeniedException("권한이 없습니다. 근로자 ID: " + employee.getId());
                    }
                })
                .toList();
    }

    /**
     * 구역 ID 목록에 대한 검증 및 검증된 결과만 리턴합니다.
     * 주어진 구역 ID 목록이 현재 접속한 근로자가 속한 공장의 구역인지 검증하고, 존재하지 않는 구역에 대한 예외 처리도 수행합니다.
     * 현재 접속한 근로자와 다른 업체의 공장에 소속된 구역인 경우 해당 구역에 대한 접근을 막습니다.
     * ADMIN이 아닌 경우에만 검증을 수행합니다.
     *
     * @param areaIds 검증할 구역 ID 목록
     * @return 검증된 구역 목록
     */
    @Transactional(readOnly = true)
    public List<Area> validateAreaIds(List<Long> areaIds) {
        // 주어진 구역 ID 목록을 검증하여 구역을 조회하고, 해당 구역에 대한 접근 권한을 확인합니다.
        return areaIds.stream()
                .map(areaRepository::findById)
                .map(area -> area.orElseThrow(() -> new EntityNotFoundException("존재하지 않는 구역입니다."))) // 구역이 없으면 예외 발생
                .filter(area -> hasAccessToFactory(area.getFactory().getId())) // 구역 접근 권한을 확인
                .peek(area -> {
                    // 구역의 공장에 접근 권한이 없으면 예외를 발생시킵니다.
                    if (!hasAccessToFactory(area.getFactory().getId())) {
                        log.warn("구역 ID {}는 호출한 근로자가 속한 공장과 다릅니다.", area.getId());
                        throw new AccessDeniedException("권한이 없습니다. 구역 ID: " + area.getId());
                    }
                })
                .toList();
    }

    /**
     * 작업 ID 목록에 대한 검증 및 검증된 결과만 리턴합니다.
     * 주어진 작업 ID 목록이 현재 접속한 근로자가 속한 공장의 구역에 할당된 작업인지 검증하고, 존재하지 않는 작업에 대한 예외 처리도 수행합니다.
     * 현재 접속한 근로자와 다른 업체의 공장에 소속된 구역에 할당된 작업인 경우 해당 작업에 대한 접근을 막습니다.
     * ADMIN이 아닌 경우에만 검증을 수행합니다.
     *
     * @param workIds 검증할 작업 ID 목록
     * @return 검증된 작업 목록
     */
    @Transactional(readOnly = true)
    public List<Work> validateWorkIds(List<Long> workIds) {
        // 주어진 작업 ID 목록을 검증하여 작업을 조회하고, 해당 작업에 대한 접근 권한을 확인합니다.
        return workIds.stream()
                .map(workRepository::findById)
                .map(work -> work.orElseThrow(() -> new EntityNotFoundException("존재하지 않는 작업입니다."))) // 작업이 없으면 예외 발생
                .filter(work -> hasAccessToFactory(work.getArea().getFactory().getId())) // 작업의 구역에 대한 접근 권한을 확인
                .peek(work -> {
                    // 작업에 대한 권한이 없으면 예외를 발생시킵니다.
                    if (!hasAccessToFactory(work.getArea().getFactory().getId())) {
                        log.warn("작업 ID {}는 호출한 근로자가 속한 공장과 다릅니다.", work.getId());
                        throw new AccessDeniedException("권한이 없습니다. 작업 ID: " + work.getId());
                    }
                })
                .toList();
    }

    /**
     * 시스템 설정 ID 목록에 대한 검증 및 검증된 결과만 리턴합니다.
     * 주어진 시스템 설정 ID 목록이 현재 접속한 근로자가 속한 업체의 시스템 설정에 접근할 수 있는지 검증하고, 존재하지 않는 시스템 설정에 대한 예외 처리도 수행합니다.
     * 현재 접속한 근로자와 다른 업체의 시스템 설정일 경우 접근을 막습니다.
     * ADMIN이 아닌 경우에만 검증을 수행합니다.
     *
     * @param settingIds 검증할 시스템 설정 ID 목록
     * @return 검증된 시스템 설정 목록
     */
    @Transactional(readOnly = true)
    public List<Setting> validateSettingIds(List<Long> settingIds) {
        // 주어진 시스템 설정 ID 목록을 검증하여 설정을 조회하고, 해당 설정에 대한 접근 권한을 확인합니다.
        return settingIds.stream()
                .map(settingRepository::findById)
                .map(setting -> setting.orElseThrow(() -> new EntityNotFoundException("존재하지 않는 시스템 설정입니다."))) // 설정이 없으면 예외 발생
                .filter(setting -> hasAccessToCompany(setting.getCompany().getId())) // 설정에 대한 업체 접근 권한을 확인
                .peek(setting -> {
                    // 시스템 설정에 대한 권한이 없으면 예외를 발생시킵니다.
                    if (!hasAccessToCompany(setting.getCompany().getId())) {
                        log.warn("시스템 설정 ID {}는 호출한 근로자가 속한 업체와 다릅니다.", setting.getId());
                        throw new AccessDeniedException("권한이 없습니다. 시스템 설정 ID: " + setting.getId());
                    }
                })
                .toList();
    }

    /**
     * 주어진 공장 ID에 대해 접근 권한을 검증합니다.
     * @param factoryId 검증할 공장 ID
     * @return 공장에 대한 접근 권한 여부
     */
    private boolean hasAccessToFactory(Long factoryId) {
        Employee currentEmployee = getCurrentEmployee();
        return currentEmployee.getRole().equals(EmployeeRole.ADMIN) || factoryId.equals(currentEmployee.getFactory().getId());
    }

    /**
     * 주어진 업체 ID에 대해 접근 권한을 검증합니다.
     * @param companyId 검증할 업체 ID
     * @return 업체에 대한 접근 권한 여부
     */
    private boolean hasAccessToCompany(Long companyId) {
        Employee currentEmployee = getCurrentEmployee();
        return currentEmployee.getRole().equals(EmployeeRole.ADMIN) || companyId.equals(currentEmployee.getFactory().getCompany().getId());
    }
}