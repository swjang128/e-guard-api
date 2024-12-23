package atemos.eguard.api.service;

import atemos.eguard.api.config.EncryptUtil;
import atemos.eguard.api.config.EntityValidator;
import atemos.eguard.api.domain.IncidentPriority;
import atemos.eguard.api.domain.SafetyGrade;
import atemos.eguard.api.dto.EventDto;
import atemos.eguard.api.entity.Event;
import atemos.eguard.api.repository.AreaRepository;
import atemos.eguard.api.repository.EventRepository;
import atemos.eguard.api.repository.FactoryRepository;
import atemos.eguard.api.specification.EventSpecification;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * EventServiceImpl는 사건과 관련된 서비스 로직을 구현한 클래스입니다.
 * 사건 등록, 수정, 삭제 및 조회와 같은 기능을 제공합니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final FactoryRepository factoryRepository;
    private final AreaRepository areaRepository;
    private final EncryptUtil encryptUtil;
    private final EntityValidator entityValidator;

    /**
     * 사건을 등록합니다.
     *
     * @param createEventDto 등록할 사건 정보를 담고 있는 DTO
     * @return 등록된 사건 정보를 담고 있는 DTO 응답
     */
    @Override
    @Transactional
    public EventDto.ReadEventResponse create(EventDto.CreateEvent createEventDto) {
        // 파라미터에 근로자 ID가 존재하면 현재 접속한 근로자가 신규 사건에 등록할 근로자로 접근 가능한지 검증 및 조회
        var employee = Optional.ofNullable(createEventDto.getEmployeeId())
                .map(employeeId -> entityValidator.validateEmployeeIds(List.of(employeeId))
                        .stream().findFirst()
                        .orElseThrow(() -> new AccessDeniedException("근로자를 찾을 수 없거나 등록 권한이 없습니다.")))
                .orElse(null);
        // 파라미터에 구역 ID가 존재하면 현재 접속한 근로자가 신규 사건에 등록할 구역으로 접근 가능한지 검증 및 조회
        var area = Optional.ofNullable(createEventDto.getAreaId())
                .map(areaId -> entityValidator.validateAreaIds(List.of(areaId))
                        .stream().findFirst()
                        .orElseThrow(() -> new AccessDeniedException("구역을 찾을 수 없거나 등록 권한이 없습니다.")))
                .orElse(null);
        // 사건 정보를 빌드하고 저장
        var event = Event.builder()
                .employee(employee)
                .area(area)
                .employeeIncident(createEventDto.getEmployeeIncident())
                .areaIncident(createEventDto.getAreaIncident())
                .resolved(createEventDto.getEventResolved())
                .build();
        event = eventRepository.save(event);
        // 저장된 사건 정보를 반환
        return EventDto.ReadEventResponse.builder()
                .eventId(event.getId())
                .employeeId(employee != null ? employee.getId() : null)
                .employeeName(employee != null ? encryptUtil.decrypt(employee.getName()) : null)
                .employeeNumber(employee != null ? employee.getEmployeeNumber() : null)
                .areaId(area != null ? area.getId() : null)
                .areaName(area != null ? area.getName() : null)
                .areaLocation(area != null ? area.getLocation() : null)
                .employeeIncident(event.getEmployeeIncident())
                .areaIncident(event.getAreaIncident())
                .eventResolved(event.getResolved())
                .incidentName(event.getEmployeeIncident() != null
                        ? event.getEmployeeIncident().getName()
                        : event.getAreaIncident().getName())
                .incidentPriority(event.getEmployeeIncident() != null
                        ? event.getEmployeeIncident().getPriority()
                        : event.getAreaIncident().getPriority())
                .incidentMessage(event.getEmployeeIncident() != null
                        ? event.getEmployeeIncident().getMessage()
                        : event.getAreaIncident().getMessage())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }

    /**
     * 조건에 맞는 사건 목록을 조회합니다.
     *
     * @param readEventRequestDto 사건 조회 조건을 담고 있는 DTO
     * @param pageable 페이징 정보를 담고 있는 객체
     * @return 조회된 사건 목록과 페이지 정보를 포함한 응답 객체
     */
    @Override
    @Transactional(readOnly = true)
    public EventDto.ReadEventResponseList read(EventDto.ReadEventRequest readEventRequestDto, Pageable pageable) {
        // readAreaRequestDto의 사건 ID 리스트가 존재하면 이 정보에 현재 접속한 근로자가 접근할 수 있는지 검증
        Optional.ofNullable(readEventRequestDto.getEventIds())
                .filter(ids -> !ids.isEmpty())
                .ifPresent(eventIds -> {
                    if (entityValidator.validateEventIds(eventIds).isEmpty()) {
                        throw new AccessDeniedException("사건이 존재하지 않거나 조회 권한이 없습니다.");
                    }
                });
        // readAreaRequestDto의 근로자 ID 리스트가 존재하면 이 정보에 현재 접속한 근로자가 접근할 수 있는지 검증
        Optional.ofNullable(readEventRequestDto.getEmployeeIds())
                .filter(ids -> !ids.isEmpty())
                .ifPresent(employeeIds -> {
                    if (entityValidator.validateEmployeeIds(employeeIds).isEmpty()) {
                        throw new AccessDeniedException("근로자가 존재하지 않거나 조회 권한이 없습니다.");
                    }
                });
        // readAreaRequestDto의 구역 ID 리스트가 존재하면 이 정보에 현재 접속한 근로자가 접근할 수 있는지 검증
        Optional.ofNullable(readEventRequestDto.getAreaIds())
                .filter(ids -> !ids.isEmpty())
                .ifPresent(areaIds -> {
                    if (entityValidator.validateAreaIds(areaIds).isEmpty()) {
                        throw new AccessDeniedException("구역이 존재하지 않거나 조회 권한이 없습니다.");
                    }
                });
        // readAreaRequestDto의 공장 ID 리스트가 존재하면 이 정보에 현재 접속한 근로자가 접근할 수 있는지 검증
        Optional.ofNullable(readEventRequestDto.getFactoryIds())
                .filter(ids -> !ids.isEmpty())
                .ifPresent(factoryIds -> {
                    if (entityValidator.validateFactoryIds(factoryIds).isEmpty()) {
                        throw new AccessDeniedException("공장이 존재하지 않거나 조회 권한이 없습니다.");
                    }
                });
        // 조건에 맞는 사건 목록을 페이지 단위로 조회
        var eventPage = eventRepository.findAll(
                EventSpecification.findWith(readEventRequestDto),
                pageable);
        // 조회된 사건 목록을 응답 객체로 변환하여 반환
        var eventList = eventPage.getContent().stream()
                .map(event -> EventDto.ReadEventResponse.builder()
                        .eventId(event.getId())
                        .employeeId(event.getEmployee() != null ? event.getEmployee().getId() : null)
                        .employeeName(event.getEmployee() != null ? encryptUtil.decrypt(event.getEmployee().getName()) : null)
                        .employeeNumber(event.getEmployee() != null ? event.getEmployee().getEmployeeNumber() : null)
                        .areaId(event.getArea() != null ? event.getArea().getId() : null)
                        .areaName(event.getArea() != null ? event.getArea().getName() : null)
                        .areaLocation(event.getArea() != null ? event.getArea().getLocation() : null)
                        .factoryId(event.getArea() != null ? event.getArea().getFactory().getId() : event.getEmployee().getFactory().getId())
                        .factoryName(event.getArea() != null ? event.getArea().getFactory().getName() : event.getEmployee().getFactory().getName())
                        .factoryAddress(event.getArea() != null ? event.getArea().getFactory().getAddress() : event.getEmployee().getFactory().getAddress())
                        .employeeIncident(event.getEmployeeIncident())
                        .areaIncident(event.getAreaIncident())
                        .incidentName(event.getEmployeeIncident() != null
                                ? event.getEmployeeIncident().getName()
                                : event.getAreaIncident().getName())
                        .incidentPriority(event.getEmployeeIncident() != null
                                ? event.getEmployeeIncident().getPriority()
                                : event.getAreaIncident().getPriority())
                        .incidentMessage(event.getEmployeeIncident() != null
                                ? event.getEmployeeIncident().getMessage()
                                : event.getAreaIncident().getMessage())
                        .eventResolved(event.getResolved())
                        .createdAt(event.getCreatedAt())
                        .updatedAt(event.getUpdatedAt())
                        .build())
                .toList();
        return EventDto.ReadEventResponseList.builder()
                .eventList(eventList)
                .totalElements(eventPage.getTotalElements())
                .totalPages(eventPage.getTotalPages())
                .build();
    }

    /**
     * 기존 사건 정보를 수정합니다.
     *
     * @param eventId 수정할 사건의 ID
     * @param updateEventDto 수정할 사건 정보를 담고 있는 DTO
     * @return 수정된 사건 정보를 담은 응답 객체
     */
    @Override
    @Transactional
    public EventDto.ReadEventResponse update(Long eventId, EventDto.UpdateEvent updateEventDto) {
        // 기존 사건을 현재 접속한 근로자가 수정할 수 있는 권한이 있는지 검증 후 조회
        var event = entityValidator.validateEventIds(List.of(eventId))
                .stream().findFirst()
                .orElseThrow(() -> new AccessDeniedException("사건을 찾을 수 없거나 수정 권한이 없습니다."));
        // 기존 사건에 등록된 근로자의 정보를 현재 접속한 근로자가 수정할 수 있는 권한이 있는지 검증
        Optional.ofNullable(event.getEmployee())
                .ifPresent(employee -> {
                    entityValidator.validateEmployeeIds(List.of(employee.getId()))
                            .stream().findFirst()
                            .orElseThrow(() -> new AccessDeniedException("해당 사건의 근로자에 대한 수정 권한이 없습니다."));
                });
        // 수정할 사건의 원인이 되는 근로자 ID가 존재하면 현재 접속한 근로자가 해당 근로자 정보에 접근할 수 있는지 검증 및 조회
        Optional.ofNullable(updateEventDto.getEmployeeId()).ifPresent(employeeId -> {
            var employee = entityValidator.validateEmployeeIds(List.of(employeeId))
                            .stream().findFirst()
                            .orElseThrow(() -> new AccessDeniedException("근로자를 찾을 수 없거나 수정 권한이 없습니다."));
                    event.setEmployee(employee);
       });
        // 수정할 사건이 발생한 구역 ID가 존재하면 현재 접속한 근로자가 해당 구역 정보에 접근할 수 있는지 검증 및 조회
        Optional.ofNullable(updateEventDto.getAreaId()).ifPresent(areaId -> {
            var area = entityValidator.validateAreaIds(List.of(areaId))
                    .stream().findFirst()
                    .orElseThrow(() -> new AccessDeniedException("구역을 찾을 수 없거나 수정 권한이 없습니다."));
                    event.setArea(area);
        });
        // 사건의 필드들을 Optional로 처리하여 업데이트
        Optional.ofNullable(updateEventDto.getEmployeeIncident()).ifPresent(event::setEmployeeIncident);
        Optional.ofNullable(updateEventDto.getAreaIncident()).ifPresent(event::setAreaIncident);
        Optional.ofNullable(updateEventDto.getEventResolved()).ifPresent(event::setResolved);
        // 수정된 사건 정보를 저장하고 반환
        var updatedEvent = eventRepository.save(event);
        // ReadEventResponse에 수정된 사건 정보를 반환 (근로자의 암호화된 정보 복호화 포함)
        return EventDto.ReadEventResponse.builder()
                .eventId(updatedEvent.getId())
                .employeeId(updatedEvent.getEmployee() != null ? updatedEvent.getEmployee().getId() : null)
                .employeeName(updatedEvent.getEmployee() != null ? encryptUtil.decrypt(updatedEvent.getEmployee().getName()) : null)
                .employeeNumber(updatedEvent.getEmployee() != null ? updatedEvent.getEmployee().getEmployeeNumber() : null)
                .areaId(updatedEvent.getArea() != null ? updatedEvent.getArea().getId() : null)
                .areaName(updatedEvent.getArea() != null ? updatedEvent.getArea().getName() : null)
                .areaLocation(updatedEvent.getArea() != null ? updatedEvent.getArea().getLocation() : null)
                .employeeIncident(updatedEvent.getEmployeeIncident())
                .areaIncident(updatedEvent.getAreaIncident())
                .incidentName(updatedEvent.getEmployeeIncident() != null
                        ? updatedEvent.getEmployeeIncident().getName()
                        : updatedEvent.getAreaIncident().getName())
                .incidentPriority(updatedEvent.getEmployeeIncident() != null
                        ? updatedEvent.getEmployeeIncident().getPriority()
                        : updatedEvent.getAreaIncident().getPriority())
                .incidentMessage(updatedEvent.getEmployeeIncident() != null
                        ? updatedEvent.getEmployeeIncident().getMessage()
                        : updatedEvent.getAreaIncident().getMessage())
                .eventResolved(updatedEvent.getResolved())
                .createdAt(updatedEvent.getCreatedAt())
                .updatedAt(updatedEvent.getUpdatedAt())
                .build();
    }

    /**
     * 사건을 삭제합니다.
     *
     * @param eventId 삭제할 사건의 ID
     */
    @Override
    @Transactional
    public void delete(Long eventId) {
        // 삭제할 사건이 존재하는지 확인하고 없으면 예외 처리
        var event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 사건입니다. ID: " + eventId));
        // 사건을 삭제
        eventRepository.delete(event);
    }

    /**
     * 특정 업체의 공장에 대한 이번 달 안전 점수를 계산합니다.
     *
     * @param factoryId 안전 점수를 계산할 공장 ID입니다.
     * @return 해당 업체의 공장 및 구역에 대한 이번 달 안전 점수 정보를 포함하는 EventDto.SafetyScore 객체입니다.
     */
    @Override
    public EventDto.SafetyScore readSafetyScore(Long factoryId) {
        var factory = entityValidator.validateFactoryIds(List.of(factoryId))
                .stream().findFirst()
                .orElseThrow(() -> new AccessDeniedException("공장을 찾을 수 없거나 조회 권한이 없습니다."));
        entityValidator.validateCompanyIds(List.of(factory.getCompany().getId()))
                .stream().findFirst()
                .orElseThrow(() -> new AccessDeniedException("업체를 찾을 수 없거나 조회 권한이 없습니다."));
        // 이번 달의 시작일과 종료일 계산
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).atTime(23, 59, 59);
        // 공장 내 구역들의 사건 조회 및 안전 점수 계산
        List<EventDto.AreaSafetyScore> areaSafetyScores = areaRepository.findByFactory(factory).stream()
                .map(area -> {
                    List<Event> areaEvents = eventRepository.findByAreaAndCreatedAtBetween(area, startOfMonth, endOfMonth);
                    long areaCriticalCount = areaEvents.stream()
                            .filter(event -> event.getAreaIncident().getPriority() == IncidentPriority.CRITICAL)
                            .count();
                    long areaAlertCount = areaEvents.stream()
                            .filter(event -> event.getAreaIncident().getPriority() == IncidentPriority.ALERT)
                            .count();
                    long areaWarningCount = areaEvents.stream()
                            .filter(event -> event.getAreaIncident().getPriority() == IncidentPriority.WARNING)
                            .count();
                    // 구역별 안전 점수 및 등급 계산
                    int areaSafetyScore = calculateSafetyScore(areaCriticalCount, areaAlertCount, areaWarningCount);
                    String areaSafetyGrade = calculateSafetyGrade(areaSafetyScore);
                    return EventDto.AreaSafetyScore.builder()
                            .areaId(area.getId())
                            .areaName(area.getName())
                            .areaLocation(area.getLocation())
                            .safetyScore(areaSafetyScore)
                            .safetyGrade(areaSafetyGrade)
                            .criticalIncidentCount(areaCriticalCount)
                            .alertIncidentCount(areaAlertCount)
                            .warningIncidentCount(areaWarningCount)
                            .build();
                }).collect(Collectors.toList());
        // 공장 안전 점수 계산
        int factorySafetyScore = (int) areaSafetyScores.stream()
                .mapToInt(EventDto.AreaSafetyScore::getSafetyScore)
                .average().orElse(100); // 구역이 없으면 기본 점수 100
        String factorySafetyGrade = calculateSafetyGrade(factorySafetyScore);
        // SafetyGrade Enum에서 등급 리스트 생성
        List<EventDto.SafetyGrade> safetyGrades = Arrays.stream(SafetyGrade.values())
                .map(sg -> new EventDto.SafetyGrade(sg.getGrade(), sg.getMessage(), sg.getMin(), sg.getMax()))
                .toList();
        // 안전 점수 및 등급 반환
        return EventDto.SafetyScore.builder()
                .factorySafetyScore(
                        EventDto.FactorySafetyScore.builder()
                                .factoryId(factory.getId())
                                .factoryName(factory.getName())
                                .factoryAddress(factory.getAddress())
                                .safetyScore(factorySafetyScore)
                                .safetyGrade(factorySafetyGrade)
                                .criticalIncidentCount(areaSafetyScores.stream().mapToLong(EventDto.AreaSafetyScore::getCriticalIncidentCount).sum())
                                .alertIncidentCount(areaSafetyScores.stream().mapToLong(EventDto.AreaSafetyScore::getAlertIncidentCount).sum())
                                .warningIncidentCount(areaSafetyScores.stream().mapToLong(EventDto.AreaSafetyScore::getWarningIncidentCount).sum())
                                .areaSafetyScores(areaSafetyScores)
                                .build()
                )
                .safetyGrades(safetyGrades)
                .build();
    }

    /**
     * 모든 사건을 해결 상태로 일괄 처리합니다.
     *
     * @implNote 이 메서드는 모든 사건의 상태를 해결 상태로 일괄 수정합니다.
     *           특정 조건이 없는 모든 사건을 대상으로 합니다.
     */
    @Override
    public void resolveAllEvents() {
        // 모든 사건을 resolve = true로 변경 후 저장합니다.
        var allEvents = eventRepository.findAll();
        allEvents.forEach(event -> {
            event.setResolved(true);
            eventRepository.save(event);
        });
    }

    /**
     * 사건의 유형별 개수를 바탕으로 안전 점수를 계산하는 메서드입니다.
     * 안전 점수는 아래와 같은 기준으로 계산됩니다:
     * - CRITICAL: 10점 차감
     * - ALERT: 5점 차감
     * - WARNING: 1점 차감
     * 총점 100점에서 차감하여 안전 점수를 계산하고, 점수에 따라 '심각', '주의', '양호' 등급을 부여합니다.
     *
     * @param criticalCount CRITICAL 사건의 개수
     * @param alertCount ALERT 사건의 개수
     * @param warningCount WARNING 사건의 개수
     * @return 계산된 안전 점수와 안전 등급을 담은 객체
     */
    private int calculateSafetyScore(long criticalCount, long alertCount, long warningCount) {
        // 사건별 차감 점수 계산
        int totalDeduction = (int) (criticalCount * 10 + alertCount * 5 + warningCount);
        // 기본 점수에서 차감하여 안전 점수 계산
        return Math.max(100 - totalDeduction, 0);
    }

    /**
     * 안전 점수에 따라 안전 등급을 반환하는 메서드입니다.
     *
     * @param safetyScore 계산된 안전 점수
     * @return "심각", "주의", "양호" 중 하나의 문자열을 반환
     */
    private String calculateSafetyGrade(int safetyScore) {
        if (safetyScore < 60) {
            return "심각";
        } else if (safetyScore < 80) {
            return "주의";
        } else {
            return "양호";
        }
    }
}