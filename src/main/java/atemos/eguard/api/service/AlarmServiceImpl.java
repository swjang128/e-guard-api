package atemos.eguard.api.service;

import atemos.eguard.api.config.EncryptUtil;
import atemos.eguard.api.config.EntityValidator;
import atemos.eguard.api.dto.AlarmDto;
import atemos.eguard.api.entity.Alarm;
import atemos.eguard.api.repository.AlarmRepository;
import atemos.eguard.api.specification.AlarmSpecification;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * AlarmServiceImpl는 알람과 관련된 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 알람 조회, 수정, 삭제 기능을 제공합니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AlarmServiceImpl implements AlarmService {
    private final PlatformTransactionManager transactionManager;
    private final AlarmRepository alarmRepository;
    private final EncryptUtil encryptUtil;
    private final EntityValidator entityValidator;

    /**
     * 알람 생성
     * 공장 ID와 사건 정보를 기반으로 알람을 생성합니다.
     * 해당 공장의 모든 근로자들에게 알람을 발송합니다.
     *
     * @param createAlarmDto 알람을 생성하는 데 필요한 정보가 담긴 DTO
     * @return 생성된 알람 정보
     */
    @Override
    @Transactional
    public AlarmDto.ReadAlarmResponse create(AlarmDto.CreateAlarm createAlarmDto) {
        // 현재 접속한 근로자가 신규 알람에 등록할 근로자로 접근 가능한지 검증 및 조회
        var employee = entityValidator.validateEmployeeIds(List.of(createAlarmDto.getEmployeeId()))
                .stream().findFirst().orElseThrow(() -> new AccessDeniedException("근로자를 찾을 수 없거나 등록 권한이 없습니다."));
        // 현재 접속한 근로자가 신규 알람에 등록할 사건으로 접근 가능한지 검증 및 조회
        var event = entityValidator.validateEventIds(List.of(createAlarmDto.getEventId()))
                .stream().findFirst().orElseThrow(() -> new AccessDeniedException("사건을 찾을 수 없거나 등록 권한이 없습니다."));
        // 알람 메시지 만들기 (Incident Priority에 따라 다르게 가공)
        var message = event.getAreaIncident() != null ?
                // AreaIncident가 있는 경우
                event.getAreaIncident().getPriority().getPrefix() + event.getArea().getName() + "에서 " +
                        event.getAreaIncident().getName() + " 사건이 발생했습니다. " + event.getAreaIncident().getMessage()
                :
                // EmployeeIncident가 있는 경우
                event.getEmployeeIncident() != null ?
                        event.getEmployeeIncident().getPriority().getPrefix() + encryptUtil.decrypt(employee.getName()) + " 근로자에게 " +
                                event.getEmployeeIncident().getName() + " 사건이 발생했습니다. " + event.getEmployeeIncident().getMessage()
                        : null;
        // 알람 생성 및 저장
        var alarm = Alarm.builder()
                .employee(employee)
                .event(event)
                .message(message)
                .isRead(false)
                .build();
        alarmRepository.save(alarm);
        // 생성된 알람 정보를 ReadAlarmResponse로 변환하여 반환
        return AlarmDto.ReadAlarmResponse.builder()
                .alarmId(alarm.getId())
                .employeeId(employee.getId())
                .employeeName(encryptUtil.decrypt(employee.getName()))
                .eventId(event.getId())
                .areaIncident(event.getAreaIncident())
                .employeeIncident(event.getEmployeeIncident())
                .eventEmployeeId(event.getEmployee() != null ? event.getEmployee().getId() : null)
                .eventEmployeeName(event.getEmployee() != null ? encryptUtil.decrypt(event.getEmployee().getName()) : null)
                .eventAreaId(event.getArea() != null ? event.getArea().getId() : null)
                .eventAreaName(event.getArea() != null ? event.getArea().getName() : null)
                .eventAreaLocation(event.getArea() != null ? event.getArea().getLocation() : null)
                .eventResolved(event.getResolved())
                .alarmMessage(message)
                .alarmRead(alarm.getIsRead())
                .createdAt(alarm.getCreatedAt())
                .updatedAt(alarm.getUpdatedAt())
                .build();
    }

    /**
     * 조건에 맞는 알람 조회
     *
     * @param readAlarmRequestDto 알람 정보 조회 조건을 포함하는 DTO
     * @param pageable 페이징 정보를 포함하는 객체
     * @return 조회된 알람 목록과 관련된 추가 정보를 포함하는 응답 객체
     */
    @Override
    @Transactional(readOnly = true)
    public AlarmDto.ReadAlarmResponseList read(AlarmDto.ReadAlarmRequest readAlarmRequestDto, Pageable pageable) {
        // readAlarmRequestDto에 알람 ID 리스트가 존재하면 이 정보에 현재 접속한 근로자가 접근할 수 있는지 검증
        Optional.ofNullable(readAlarmRequestDto.getAlarmIds())
                .filter(ids -> !ids.isEmpty())
                .ifPresent(alarmIds -> {
                    if (entityValidator.validateAlarmIds(alarmIds).isEmpty()) {
                        throw new AccessDeniedException("알람이 존재하지 않거나 조회 권한이 없습니다.");
                    }
                });
        // readAlarmRequestDto에 근로자 ID 리스트가 존재하면 이 정보에 현재 접속한 근로자가 접근할 수 있는지 검증
        Optional.ofNullable(readAlarmRequestDto.getEmployeeIds())
                .filter(ids -> !ids.isEmpty())
                .ifPresent(employeeIds -> {
                    if (entityValidator.validateEmployeeIds(employeeIds).isEmpty()) {
                        throw new AccessDeniedException("근로자가 존재하지 않거나 조회 권한이 없습니다.");
                    }
                });
        // readAlarmRequestDto에 사건 ID 리스트가 존재하면 이 정보에 현재 접속한 근로자가 접근할 수 있는지 검증
        Optional.ofNullable(readAlarmRequestDto.getEventIds())
                .filter(ids -> !ids.isEmpty())
                .ifPresent(eventIds -> {
                    if (entityValidator.validateEventIds(eventIds).isEmpty()) {
                        throw new AccessDeniedException("사건이 존재하지 않거나 조회 권한이 없습니다.");
                    }
                });
        // readAlarmRequestDto에 공장 Id 리스트가 존재하면 이 정보에 현재 접속한 근로자가 접근할 수 있는지 검증
        Optional.ofNullable(readAlarmRequestDto.getFactoryIds())
                .filter(ids -> !ids.isEmpty())
                .ifPresent(factoryIds -> {
                    if (entityValidator.validateFactoryIds(factoryIds).isEmpty()) {
                        throw new AccessDeniedException("공장이 존재하지 않거나 조회 권한이 없습니다.");
                    }
                });
        // readAlarmRequestDto의 companyIds가 존재하면 이 정보에 현재 접속한 근로자가 접근할 수 있는지 검증
        Optional.ofNullable(readAlarmRequestDto.getCompanyIds())
                .filter(ids -> !ids.isEmpty())
                .ifPresent(companyIds -> {
                    if (entityValidator.validateCompanyIds(companyIds).isEmpty()) {
                        throw new AccessDeniedException("업체가 존재하지 않거나 접근 권한이 없습니다.");
                    }
                });
        // 알람 조회
        var alarmResponse = alarmRepository.findAll(AlarmSpecification.findWith(readAlarmRequestDto), pageable);
        var alarmList = alarmResponse.getContent().stream()
                .map(alarm -> AlarmDto.ReadAlarmResponse.builder()
                        .alarmId(alarm.getId())
                        .employeeId(alarm.getEmployee().getId())
                        .employeeName(encryptUtil.decrypt(alarm.getEmployee().getName()))
                        .eventId(alarm.getEvent().getId())
                        .employeeIncident(alarm.getEvent().getEmployeeIncident())
                        .areaIncident(alarm.getEvent().getAreaIncident())
                        .eventEmployeeId(alarm.getEvent().getEmployee() != null ? alarm.getEvent().getEmployee().getId() : null)
                        .eventEmployeeName(alarm.getEvent().getEmployee() != null ? encryptUtil.decrypt(alarm.getEvent().getEmployee().getName()) : null)
                        .eventAreaId(alarm.getEvent().getArea() != null ? alarm.getEvent().getArea().getId() : null)
                        .eventAreaName(alarm.getEvent().getArea() != null ? alarm.getEvent().getArea().getName() : null)
                        .eventAreaLocation(alarm.getEvent().getArea() != null ? alarm.getEvent().getArea().getLocation() : null)
                        .eventResolved(alarm.getEvent().getResolved())
                        .alarmMessage(alarm.getMessage())
                        .alarmRead(alarm.getIsRead())
                        .createdAt(alarm.getCreatedAt())
                        .updatedAt(alarm.getUpdatedAt())
                        .build())
                .toList();
        // 조건에 맞는 알람 목록을 리턴
        return AlarmDto.ReadAlarmResponseList.builder()
                .alarmList(alarmList)
                .totalElements(alarmResponse.getTotalElements())
                .totalPages(alarmResponse.getTotalPages())
                .build();
    }

    /**
     * 알람 수정
     *
     * @param alarmId 수정할 알람의 ID
     * @param updateAlarmDto 알람 수정 정보를 포함하는 DTO
     * @return 수정된 알람 정보를 담고 있는 객체
     */
    @Override
    @Transactional
    public AlarmDto.ReadAlarmResponse update(Long alarmId, AlarmDto.UpdateAlarm updateAlarmDto) {
        // 기존 알람을 현재 접속한 근로자가 수정할 수 있는지 검증 및 조회
        var alarm = entityValidator.validateAlarmIds(List.of(alarmId))
                .stream().findFirst()
                .orElseThrow(() -> new AccessDeniedException("알람을 찾을 수 없거나 수정 권한이 없습니다."));
        // 기존 알람을 수신받는 근로자를 현재 접속한 근로자가 수정할 수 있는지 검증
        entityValidator.validateEmployeeIds(List.of(alarm.getEmployee().getId()))
                .stream().findFirst()
                .orElseThrow(() -> new AccessDeniedException("알람에 등록된 근로자를 찾을 수 없거나 수정 권한이 없습니다."));
        // 기존 알람의 원인이 된 사건 정보를 현재 접속한 근로자가 수정할 수 있는지 검증
        entityValidator.validateEventIds(List.of(alarm.getEvent().getId()))
                .stream().findFirst()
                .orElseThrow(() -> new AccessDeniedException("기존 사건을 찾을 수 없거나 수정 권한이 없습니다."));
        // 수정할 알람 정보에 근로자 ID가 존재하면 해당하는 근로자 정보에 현재 접속한 근로자가 접근할 수 있는지 검증 및 조회
        Optional.ofNullable(updateAlarmDto.getEmployeeId()).ifPresent(employeeId -> {
            var employee = entityValidator.validateEmployeeIds(List.of(employeeId))
                    .stream().findFirst()
                    .orElseThrow(() -> new AccessDeniedException("근로자를 찾을 수 없거나 수정 권한이 없습니다."));
            alarm.setEmployee(employee);
        });
        // 수정할 알람 정보에 사건 ID가 존재하면 해당하는 사건 정보에 현재 접속한 근로자가 접근할 수 있는지 검증 및 조회
        Optional.ofNullable(updateAlarmDto.getEventId()).ifPresent(eventId -> {
            var event = entityValidator.validateEventIds(List.of(eventId))
                    .stream().findFirst()
                    .orElseThrow(() -> new AccessDeniedException("사건을 찾을 수 없거나 수정 권한이 없습니다."));
            alarm.setEvent(event);
        });
        // 알람 메시지 수정
        Optional.ofNullable(updateAlarmDto.getAlarmMessage()).ifPresent(alarm::setMessage);
        // 읽음 여부 수정
        Optional.ofNullable(updateAlarmDto.getAlarmRead()).ifPresent(alarm::setIsRead);
        // 수정된 Alarm 저장
        alarmRepository.save(alarm);
        // 저장된 알람을 ReadAlarmResponse로 변환 후 응답 DTO 리턴
        return AlarmDto.ReadAlarmResponse.builder()
                .alarmId(alarm.getId())
                .employeeId(alarm.getEmployee().getId())
                .employeeName(encryptUtil.decrypt(alarm.getEmployee().getName()))
                .eventId(alarm.getEvent().getId())
                .employeeIncident(alarm.getEvent().getEmployeeIncident())
                .areaIncident(alarm.getEvent().getAreaIncident())
                .eventEmployeeId(alarm.getEvent().getEmployee() != null ? alarm.getEvent().getEmployee().getId() : null)
                .eventEmployeeName(alarm.getEvent().getEmployee() != null ? encryptUtil.decrypt(alarm.getEvent().getEmployee().getName()) : null)
                .eventAreaId(alarm.getEvent().getArea() != null ? alarm.getEvent().getArea().getId() : null)
                .eventAreaName(alarm.getEvent().getArea() != null ? alarm.getEvent().getArea().getName() : null)
                .eventAreaLocation(alarm.getEvent().getArea() != null ? alarm.getEvent().getArea().getLocation() : null)
                .eventResolved(alarm.getEvent().getResolved())
                .alarmMessage(alarm.getMessage())
                .alarmRead(alarm.getIsRead())
                .createdAt(alarm.getCreatedAt())
                .updatedAt(alarm.getUpdatedAt())
                .build();
    }

    /**
     * 알람 삭제
     *
     * @param id 삭제할 알람의 ID
     */
    @Override
    @Transactional
    public void delete(Long id) {
        // 알람 ID로 삭제, 없으면 예외 발생
        var alarm = alarmRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("No such alarm."));
        alarmRepository.delete(alarm);
    }

    /**
     * 실시간 알람을 스트리밍
     * 이 메서드는 서버와 클라이언트 간의 SSE 연결을 통해 실시간 알람을 주기적으로 전송합니다.
     * 주기적으로 최신 알람을 조회하여 클라이언트로 전송합니다.
     *
     * @return SSE 연결을 위한 SseEmitter 객체
     */
    @Override
    public SseEmitter streamAlarm(AlarmDto.ReadAlarmRequest readAlarmRequestDto) {
        // 타임아웃을 30분으로 설정
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        // 현재 SecurityContext를 저장
        var context = SecurityContextHolder.getContext();
        // TransactionTemplate 생성
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        // ExecutorService 생성
        var executor = Executors.newScheduledThreadPool(1, Thread.ofVirtual().factory());
        // 0초 후에 시작하여 5초 간격으로 작업 실행
        var scheduledFuture = executor.scheduleAtFixedRate(() -> {
            try {
                // 스레드 내에서 SecurityContext 수동 설정
                SecurityContextHolder.setContext(context);
                // TransactionTemplate을 사용하여 트랜잭션 시작
                transactionTemplate.execute(status -> {
                    try {
                        // 알람을 조회하고, 클라이언트로 전송
                        var alarms = read(readAlarmRequestDto, Pageable.unpaged());
                        emitter.send(SseEmitter.event()
                                .data(alarms)
                                .name("alarm"));
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                        executor.shutdown();
                    }
                    return null;
                });
            } catch (Exception e) {
                // 오류 발생 시 SSE 연결을 종료하고, 스케줄러를 중지
                emitter.completeWithError(e);
                executor.shutdown();
            }
        }, 0, 55, TimeUnit.MINUTES);
        // 클라이언트가 연결을 종료했을 때 스케줄러를 중지
        emitter.onCompletion(() -> {
            scheduledFuture.cancel(true);
            executor.shutdown();
        });
        emitter.onTimeout(() -> {
            scheduledFuture.cancel(true);
            executor.shutdown();
            emitter.complete();
        });
        return emitter;
    }

    /**
     * 모든 알람을 읽음 상태로 일괄 처리합니다.
     *
     * @implNote 이 메서드는 모든 알람의 상태를 읽음 상태로 일괄 수정합니다.
     *           특정 조건이 없는 모든 알람을 대상으로 합니다.
     */
    @Override
    public void readAllAlarms() {
        // 모든 알람을 read = true로 변경 후 저장합니다.
        var allAlarms = alarmRepository.findAll();
        allAlarms.forEach(alarm -> {
            alarm.setIsRead(true);
            alarmRepository.save(alarm);
        });
    }
}