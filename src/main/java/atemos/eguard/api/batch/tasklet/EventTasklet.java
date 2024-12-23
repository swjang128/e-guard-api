package atemos.eguard.api.batch.tasklet;

import atemos.eguard.api.config.EncryptUtil;
import atemos.eguard.api.domain.AreaIncident;
import atemos.eguard.api.domain.EmployeeIncident;
import atemos.eguard.api.entity.Event;
import atemos.eguard.api.repository.AreaRepository;
import atemos.eguard.api.repository.EmployeeRepository;
import atemos.eguard.api.repository.EventRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * EventTasklet는 특정 근로자와 구역에 발생한 사건을 생성합니다.
 * 이 Tasklet은 Spring Batch에서 사용되어 주기적으로 실행됩니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventTasklet implements Tasklet {
    private final AreaRepository areaRepository;
    private final EmployeeRepository employeeRepository;
    private final EventRepository eventRepository;
    private final EncryptUtil encryptUtil;

    /**
     * 모든 구역과 근로자들의 상태를 확인하고 사건이 발생한 경우 사건을 생성합니다.
     */
    @Override
    @Transactional
    public RepeatStatus execute(@NonNull StepContribution contribution, @NonNull ChunkContext chunkContext) {
        var random = new Random();
        // 모든 Area 조회
        var areas = areaRepository.findAll();
        // 해당 구역에 가장 최근 해결되지 않은 사건이 없다면 새로운 무작위 사건 생성
        areas.forEach(area -> eventRepository.findTopByAreaAndResolvedOrderByCreatedAtDesc(area, false)
                .filter(event -> event.getAreaIncident() != null)
                .orElseGet(() -> {
                    if (random.nextInt(100) >= 95) {
                        var randomAreaIncident = getRandomAreaIncident();
                        eventRepository.save(Event.builder()
                                .area(area)
                                .areaIncident(randomAreaIncident)
                                .resolved(false)
                                .build());
                        log.info("Area [{}]에서 새로운 사건 발생: {}", area.getName(), randomAreaIncident.getName());
                    }
                    return null;
                }));
        // 모든 Employee 조회
        var employees = employeeRepository.findAll();
        // 해당 근로자에게 가장 최근 해결되지 않은 사건이 없다면 새로운 무작위 사건 생성
        employees.forEach(employee -> eventRepository.findTopByEmployeeAndResolvedOrderByCreatedAtDesc(employee, false)
                .filter(event -> event.getEmployeeIncident() != null)
                .orElseGet(() -> {
                    if (random.nextInt(100) >= 95) {
                        var randomEmployeeIncident = getRandomEmployeeIncident();
                        eventRepository.save(Event.builder()
                                .employee(employee)
                                .employeeIncident(randomEmployeeIncident)
                                .resolved(false)
                                .build());
                        log.info("Employee [{}]에게 새로운 사건 발생: {}", encryptUtil.decrypt(employee.getName()), randomEmployeeIncident.getName());
                    }
                    return null;
                }));
        return RepeatStatus.FINISHED;
    }

    /**
     * 무작위로 구역 사건 유형을 선택합니다.
     * NORMAL 유형은 제외하고 무작위 사건 유형을 반환합니다.
     */
    private AreaIncident getRandomAreaIncident() {
        List<AreaIncident> areaIncidents = Arrays.stream(AreaIncident.values())
                .filter(incident -> incident != AreaIncident.NORMAL)
                .toList();
        return areaIncidents.get(new Random().nextInt(areaIncidents.size()));
    }

    /**
     * 무작위로 근로자 사건 유형을 선택합니다.
     * NORMAL 유형은 제외하고 무작위 사건 유형을 반환합니다.
     */
    private EmployeeIncident getRandomEmployeeIncident() {
        List<EmployeeIncident> employeeIncidents = Arrays.stream(EmployeeIncident.values())
                .filter(incident -> incident != EmployeeIncident.NORMAL)
                .toList();
        return employeeIncidents.get(new Random().nextInt(employeeIncidents.size()));
    }
}