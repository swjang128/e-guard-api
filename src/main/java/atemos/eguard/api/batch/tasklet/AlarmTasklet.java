package atemos.eguard.api.batch.tasklet;

import atemos.eguard.api.config.EncryptUtil;
import atemos.eguard.api.domain.AreaIncident;
import atemos.eguard.api.domain.EmployeeIncident;
import atemos.eguard.api.domain.EmployeeRole;
import atemos.eguard.api.entity.Alarm;
import atemos.eguard.api.repository.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * AlarmTasklet는 특정 근로자에게 발송할 알람을 생성합니다.
 * 이 Tasklet은 Spring Batch에서 사용되어 주기적으로 실행됩니다.
 * 이미 동일한 Event와 동일한 Employee에게 발송을 한 알람이 있다면 알람을 생성하지 않습니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlarmTasklet implements Tasklet {
    private final AreaRepository areaRepository;
    private final AlarmRepository alarmRepository;
    private final EmployeeRepository employeeRepository;
    private final EventRepository eventRepository;
    private final EncryptUtil encryptUtil;

    /**
     * 특정 근로자(Employee)에게 알람을 생성합니다. 알람은 아래와 같은 상황에서 생성됩니다.
     * 이미 동일한 Event와 동일한 Employee에게 발송을 한 알람이 있다면 알람을 생성하지 않습니다.
     */
    @Override
    @Transactional
    public RepeatStatus execute(@NonNull StepContribution contribution, @NonNull ChunkContext chunkContext) {
        // 사고가 발생한 구역이 속한 공장의 모든 근로자에게 알람 생성
        createAreaAlarm();
        // 사고가 발생한 근로자와 그 근로자가 속한 공장의 모든 MANAGER 및 같은 Work에 속한 근로자들에게 알람 생성
        createEmployeeAlarm();
        return RepeatStatus.FINISHED;
    }

    /**
     * Event에서 각 Area의 가장 최근 AreaIncident가 NORMAL이 아니고, resolved가 false인 경우
     * 해당 Area가 속한 Factory의 모든 Employee에게 알람을 생성합니다.
     */
    private void createAreaAlarm() {
        // 모든 구역 조회
        var areas = areaRepository.findAll();
        areas.forEach(area -> {
            // 구역의 최근 미해결 사건 조회
            var recentEventOptional = eventRepository.findTopByAreaAndResolvedOrderByCreatedAtDesc(area, false);
            recentEventOptional.ifPresent(recentEvent -> {
                // 최근 사건의 AreaIncident 확인
                var areaIncident = recentEvent.getAreaIncident();
                // 구역과 관련된 사건이 AREA이고 NORMAL 상태가 아니면 알람 생성
                if (areaIncident != null && areaIncident != AreaIncident.NORMAL) {
                    // 해당 구역의 공장에 속한 근로자 중 MANAGER 역할만 필터링
                    employeeRepository.findByFactory(area.getFactory()).stream()
                            .filter(employee -> employee.getRole() == EmployeeRole.MANAGER)
                            .forEach(employee -> {
                                // 동일한 사건에 대해 이미 알람이 존재하는지 확인
                                boolean alarmExists = alarmRepository.existsByEmployeeAndEvent(employee, recentEvent);
                                if (!alarmExists) {
                                    // 알람 메시지 생성
                                    var message = areaIncident.getPriority().getPrefix() + area.getName() + " 에서 " + areaIncident.getMessage();
                                    // 알람 생성 및 저장
                                    var alarm = Alarm.builder()
                                            .employee(employee)
                                            .event(recentEvent)
                                            .message(message)
                                            .isRead(false)
                                            .build();
                                    alarmRepository.save(alarm);
                                }
                            });
                }
            });
        });
    }

    /**
     * Event에서 각 Employee의 가장 최근 EmployeeIncident가 NORMAL 또는 ON_LEAVE가 아니고,
     * resolved가 false인 경우 그 Employee가 속한 Factory의 Role.MANAGER에게 알람을 생성합니다.
     */
    private void createEmployeeAlarm() {
        // 모든 근로자 조회
        var employees = employeeRepository.findAll();
        employees.forEach(employee -> {
            // 근로자의 최근 미해결 사건 조회
            var recentEventOptional = eventRepository.findTopByEmployeeAndResolvedOrderByCreatedAtDesc(employee, false);
            recentEventOptional.ifPresent(event -> {
                // 사건의 EmployeeIncident 확인
                var employeeIncident = event.getEmployeeIncident();
                // 사건이 EMPLOYEE 관련이고 NORMAL 및 ON_LEAVE 상태가 아니면 알람 생성
                if (employeeIncident != null && employeeIncident != EmployeeIncident.NORMAL && employeeIncident != EmployeeIncident.ON_LEAVE) {
                    // 근로자의 이름을 복호화하고, 알람 메시지 생성
                    String employeeName = encryptUtil.decrypt(employee.getName());
                    var message = employeeIncident.getPriority().getPrefix() + employeeName + employeeIncident.getMessage();
                    // 매니저들에게 알람 전송
                    var managers = employeeRepository.findByFactoryAndRole(employee.getFactory(), EmployeeRole.MANAGER);
                    managers.forEach(manager -> {
                        // 수신 대상자에게 동일한 사건으로 알람이 이미 존재하는지 확인
                        boolean alarmExists = alarmRepository.existsByEmployeeAndEvent(employee, event);
                        if (!alarmExists) {
                            // 알람 생성 및 저장
                            var alarm = Alarm.builder()
                                    .employee(employee)
                                    .event(event)
                                    .message(message)
                                    .isRead(false)
                                    .build();
                            alarmRepository.save(alarm);
                        }
                    });
                }
            });
        });
    }
}