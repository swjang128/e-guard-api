package atemos.eguard.api.service;

import atemos.eguard.api.config.EncryptUtil;
import atemos.eguard.api.config.EntityValidator;
import atemos.eguard.api.domain.AreaIncident;
import atemos.eguard.api.domain.EmployeeIncident;
import atemos.eguard.api.domain.WorkStatus;
import atemos.eguard.api.dto.EmployeeDto;
import atemos.eguard.api.dto.EventDto;
import atemos.eguard.api.dto.SettingDto;
import atemos.eguard.api.dto.WorkDto;
import atemos.eguard.api.entity.Employee;
import atemos.eguard.api.entity.Event;
import atemos.eguard.api.entity.Work;
import atemos.eguard.api.repository.AreaRepository;
import atemos.eguard.api.repository.EmployeeRepository;
import atemos.eguard.api.repository.EventRepository;
import atemos.eguard.api.repository.WorkRepository;
import atemos.eguard.api.specification.WorkSpecification;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * WorkServiceImpl는 작업과 관련된 서비스 로직을 구현한 클래스입니다.
 * 작업 등록, 수정, 삭제 및 조회와 같은 기능을 제공합니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WorkServiceImpl implements WorkService {
    private final WorkRepository workRepository;
    private final EmployeeRepository employeeRepository;
    private final AreaRepository areaRepository;
    private final EventRepository eventRepository;
    private final SettingService settingService;
    private final EncryptUtil encryptUtil;
    private final EventService eventService;
    private final EntityValidator entityValidator;

    /**
     * 작업을 등록합니다.
     *
     * @param createWorkDto 등록할 작업 정보를 담고 있는 DTO
     * @return 등록된 작업 정보를 담고 있는 DTO 응답
     */
    @Override
    @Transactional
    public WorkDto.ReadWorkResponse create(WorkDto.CreateWork createWorkDto) {
        // 현재 접속한 근로자가 신규 작업에 등록하려는 구역으로 접근 가능한지 검증 및 조회
        var area = entityValidator.validateAreaIds(List.of(createWorkDto.getAreaId()))
                .stream().findFirst().orElseThrow(() -> new AccessDeniedException("구역을 찾을 수 없거나 등록 권한이 없습니다."));
        // 해당 업체의 시스템 설정에서 구역에 등록할 수 있는 최대 작업 생성량을 초과했는지 검증
        var setting = settingService.read(SettingDto.ReadSettingRequest.builder()
                        .companyIds(List.of(area.getFactory().getCompany().getId()))
                        .build());
        if (workRepository.findByArea(area).size() > setting.getSettingList().getFirst().getMaxWorksPerArea()) {
            throw new IllegalArgumentException("이 구역에 등록할 수 있는 작업 수가 최대치에 도달하였습니다. 추가 등록하려면 시스템 설정을 변경해주세요.");
        }
        // 작업에 투입할 근로자 목록 조회 (근로자 목록이 null 또는 비어있을 경우 빈 리스트로 설정)
        List<Employee> employees = createWorkDto.getEmployeeIds() != null && !createWorkDto.getEmployeeIds().isEmpty()
                ? employeeRepository.findAllById(createWorkDto.getEmployeeIds())
                : Collections.emptyList();
        // 해당 업체의 시스템 설정에서 한 작업에 등록할 수 있는 최대 근로자 수를 초과했는지 검증
        if (employees.size() > setting.getSettingList().getFirst().getMaxEmployeesPerWork()) {
            throw new IllegalArgumentException("이 작업에 등록할 수 있는 근로자 수가 최대치에 도달하였습니다. 추가 등록하려면 시스템 설정을 변경해주세요.");
        }
        // 근로자별로 미해결된 특정 사건 유형을 검사하며 로그를 출력
        employees.forEach(employee -> {
            eventRepository.findTopByEmployeeAndResolvedOrderByCreatedAtDesc(employee, false).ifPresent(event -> {
                if (List.of(EmployeeIncident.INJURY, EmployeeIncident.CRITICAL_HEALTH_ISSUE,
                                EmployeeIncident.MINOR_HEALTH_ISSUE, EmployeeIncident.ON_LEAVE)
                        .contains(event.getEmployeeIncident())) {
                    log.warn("근로자 {}에게 미해결된 사건 {}이(가) 있습니다. 작업에 투입할 수 없습니다.",
                            employee.getId(), event.getEmployeeIncident().getName());
                    throw new IllegalArgumentException("해결되지 않은 사건이 있는 근로자는 작업에 투입할 수 없습니다.");
                }
            });
        });
        // 근로자가 다른 작업에 참여 중인지 확인
        List<WorkDto.EmployeeWorkStatus> inProgressWork = findInProgressWorkForEmployees(createWorkDto.getEmployeeIds());
        if (!inProgressWork.isEmpty()) {
            throw new IllegalArgumentException("근로자들이 이미 다른 작업에 참여 중입니다.");
        }
        // 작업 정보 생성
        var work = Work.builder()
                .name(createWorkDto.getWorkName())
                .area(area)
                .employees(employees)
                .status(createWorkDto.getWorkStatus())
                .build();
        // 작업을 저장
        work = workRepository.save(work);
        // 작업에 참여한 근로자들의 상세 정보를 생성하여 리스트에 담기
        List<EmployeeDto.ReadEmployeeResponse> employeeResponses = employees.stream()
                .map(employee -> EmployeeDto.ReadEmployeeResponse.builder()
                        .employeeId(employee.getId())
                        .employeeName(encryptUtil.decrypt(employee.getName()))
                        .employeeEmail(encryptUtil.decrypt(employee.getEmail()))
                        .employeePhoneNumber(encryptUtil.decrypt(employee.getPhoneNumber()))
                        .build())
                .collect(Collectors.toList());
        // 저장된 작업 정보를 반환
        return WorkDto.ReadWorkResponse.builder()
                .workId(work.getId())
                .employees(employeeResponses)
                .workName(work.getName())
                .workStatus(work.getStatus())
                .createdAt(work.getCreatedAt())
                .updatedAt(work.getUpdatedAt())
                .build();
    }

    /**
     * 조건에 맞는 작업 목록을 조회합니다.
     *
     * @param readWorkRequestDto 작업 조회 조건을 담고 있는 DTO
     * @param pageable 페이징 정보를 담고 있는 객체
     * @return 조회된 작업 목록과 페이지 정보를 포함한 응답 객체
     */
    @Override
    @Transactional(readOnly = true)
    public WorkDto.ReadWorkResponseList read(WorkDto.ReadWorkRequest readWorkRequestDto, Pageable pageable) {
        // readWorkRequestDto의 작업 ID 리스트에 해당하는 작업을 조회할 수 있는 권한이 있는지 검증
        Optional.ofNullable(readWorkRequestDto.getWorkIds())
                .filter(ids -> !ids.isEmpty())
                .ifPresent(workIds -> {
                    if (entityValidator.validateWorkIds(workIds).isEmpty()) {
                        throw new AccessDeniedException("작업이 존재하지 않거나 조회 권한이 없습니다.");
                    }
                });
        // readWorkRequestDto의 공장 ID 리스트에 해당하는 공장을 조회할 수 있는 권한이 있는지 검증
        Optional.ofNullable(readWorkRequestDto.getFactoryIds())
                .filter(ids -> !ids.isEmpty())
                .ifPresent(factoryIds -> {
                    if (entityValidator.validateFactoryIds(factoryIds).isEmpty()) {
                        throw new AccessDeniedException("공장이 존재하지 않거나 조회 권한이 없습니다.");
                    }
                });
        // readWorkRequestDto의 구역 ID 리스트에 해당하는 구역을 조회할 수 있는 권한이 있는지 검증
        Optional.ofNullable(readWorkRequestDto.getAreaIds())
                .filter(ids -> !ids.isEmpty())
                .ifPresent(areaIds -> {
                    if (entityValidator.validateAreaIds(areaIds).isEmpty()) {
                        throw new AccessDeniedException("구역이 존재하지 않거나 조회 권한이 없습니다.");
                    }
                });
        // readWorkRequestDto의 근로자 ID 리스트에 해당하는 근로자를 조회할 수 있는 권한이 있는지 검증
        Optional.ofNullable(readWorkRequestDto.getEmployeeIds())
                .filter(ids -> !ids.isEmpty())
                .ifPresent(employeeIds -> {
                    if (entityValidator.validateEmployeeIds(employeeIds).isEmpty()) {
                        throw new AccessDeniedException("근로자가 존재하지 않거나 조회 권한이 없습니다.");
                    }
                });
        // 조건에 맞는 작업 목록을 페이지 단위로 조회
        var workPage = workRepository.findAll(
                WorkSpecification.findWith(readWorkRequestDto),
                pageable);
        // 조회된 작업 목록을 응답 DTO로 변환하여 반환
        var workList = workPage.getContent().stream()
                .map(work -> {
                    // 각 작업에 포함된 근로자 목록을 DTO로 변환
                    List<EmployeeDto.ReadEmployeeResponse> employeeResponses = work.getEmployees().stream()
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
                                return EmployeeDto.ReadEmployeeResponse.builder()
                                        .employeeId(employee.getId())
                                        .employeeNumber(employee.getEmployeeNumber())
                                        .healthStatus(healthStatus)
                                        .build();
                            })
                            .collect(Collectors.toList());
                    // 현재 구역에서 해결되지 않은 가장 최근 사건 조회
                    var latestUnresolvedAreaEvent = eventRepository.findTopByAreaAndResolvedOrderByCreatedAtDesc(work.getArea(), false);
                    var areaIncident = latestUnresolvedAreaEvent.map(Event::getAreaIncident).orElse(AreaIncident.NORMAL);
                    // 작업 정보를 DTO로 변환하여 반환
                    return WorkDto.ReadWorkResponse.builder()
                            .workId(work.getId())
                            .areaId(work.getArea().getId())
                            .areaName(work.getArea().getName())
                            .areaIncident(areaIncident)
                            .employees(employeeResponses)
                            .workName(work.getName())
                            .workStatus(work.getStatus())
                            .createdAt(work.getCreatedAt())
                            .updatedAt(work.getUpdatedAt())
                            .build();
                })
                .toList();
        // 작업 목록과 페이지 정보를 담아 반환
        return WorkDto.ReadWorkResponseList.builder()
                .workList(workList)
                .totalElements(workPage.getTotalElements())
                .totalPages(workPage.getTotalPages())
                .build();
    }

    /**
     * 기존 작업 정보를 수정합니다.
     *
     * @param workId 수정할 작업의 ID
     * @param updateWorkDto 수정할 작업 정보를 담고 있는 DTO
     * @return 수정된 작업 정보를 담은 응답 객체
     */
    @Override
    @Transactional
    public WorkDto.ReadWorkResponse update(Long workId, WorkDto.UpdateWork updateWorkDto) {
        // 기존 작업을 현재 접속한 근로자가 수정할 수 있는 권한이 있는지 검증 후 조회
        var work = entityValidator.validateWorkIds(List.of(workId))
                .stream().findFirst()
                .orElseThrow(() -> new AccessDeniedException("작업을 찾을 수 없거나 수정 권한이 없습니다."));
        // 기존 작업이 속한 구역에 현재 접속한 근로자가 수정할 수 있는 권한이 있는지 검증
        entityValidator.validateAreaIds(List.of(work.getArea().getId()))
                .stream().findFirst()
                .orElseThrow(() -> new AccessDeniedException("구역을 찾을 수 없거나 수정 권한이 없습니다."));
        // 기존 작업에 투입된 근로자들의 정보를 현재 접속한 근로자가 수정할 수 있는 권한이 있는지 검증
        var existingEmployeeIds = work.getEmployees().stream()
                .map(Employee::getId)
                .toList();
        // 기존 근로자와 새롭게 추가된 근로자를 모두 포함한 검증
        var allEmployeeIds = new ArrayList<>(existingEmployeeIds);
        Optional.ofNullable(updateWorkDto.getEmployeeIds())
                .ifPresent(allEmployeeIds::addAll);
        // allEmployeeIds가 비어있지 않으면 검증
        if (!allEmployeeIds.isEmpty()) {
            entityValidator.validateEmployeeIds(allEmployeeIds)  // 빈 목록이 아니면 권한을 검증
                    .stream().findFirst()
                    .orElseThrow(() -> new AccessDeniedException("근로자에 대한 수정 권한이 없습니다."));
        }
        // 수정하려는 새로운 구역에 현재 접속한 근로자가 접근 가능한지 검증 후 work 엔티티에 Set
        Optional.ofNullable(updateWorkDto.getAreaId()).ifPresent(areaId -> {
            var area = entityValidator.validateAreaIds(List.of(areaId))
                    .stream().findFirst()
                    .orElseThrow(() -> new AccessDeniedException("구역을 찾을 수 없거나 수정 권한이 없습니다."));
            work.setArea(area);
        });
        // 해당 업체의 시스템 설정에서 한 작업에 등록할 수 있는 최대 근로자 수를 초과했는지 검증
        var setting = settingService.read(SettingDto.ReadSettingRequest.builder()
                .companyIds(List.of(work.getArea().getFactory().getCompany().getId()))
                .build());
        if (existingEmployeeIds.size() > setting.getSettingList().getFirst().getMaxEmployeesPerWork()) {
            throw new IllegalArgumentException("이 작업에 등록할 수 있는 근로자 수가 최대치에 도달하였습니다. 추가 등록하려면 시스템 설정을 변경해주세요.");
        }
        // Event 테이블에 해당 근로자의 해결되지 않은 employeeIncident가 있거나 근로자가 다른 작업에 참여 중인지 확인
        if (updateWorkDto.getEmployeeIds() != null && !updateWorkDto.getEmployeeIds().isEmpty()) {
            updateWorkDto.getEmployeeIds().forEach(employeeId -> {
                // 해결되지 않은 사건이 있는지 확인
                eventRepository.findTopByEmployeeAndResolvedOrderByCreatedAtDesc(
                        employeeRepository.findById(employeeId).orElseThrow(() -> new EntityNotFoundException("근로자가 존재하지 않습니다.")),
                        false).ifPresent(event -> {
                    if (List.of(EmployeeIncident.INJURY, EmployeeIncident.CRITICAL_HEALTH_ISSUE,
                                    EmployeeIncident.MINOR_HEALTH_ISSUE, EmployeeIncident.ON_LEAVE)
                            .contains(event.getEmployeeIncident())) {
                        log.warn("근로자 {}에게 미해결 사건 {}이(가) 있습니다. 작업에 투입할 수 없습니다.",
                                employeeId, event.getEmployeeIncident().getName());
                        throw new IllegalStateException("해결되지 않은 사건이 있는 근로자는 작업에 투입할 수 없습니다.");
                    }
                });
            });
            // 근로자가 다른 작업에 참여 중인지 확인
            List<WorkDto.EmployeeWorkStatus> inProgressWork = findInProgressWorkForEmployees(updateWorkDto.getEmployeeIds());
            if (!inProgressWork.isEmpty()) {
                throw new IllegalStateException("근로자들이 이미 다른 작업에 참여 중입니다.");
            }
        }
        // employeeIds가 비어있으면 빈 목록으로 설정
        Optional.ofNullable(updateWorkDto.getEmployeeIds())
                .filter(employeeIds -> !employeeIds.isEmpty())
                .map(employeeRepository::findAllById)
                .ifPresent(work::setEmployees);
        Optional.ofNullable(updateWorkDto.getEmployeeIds())
                .filter(List::isEmpty)
                .ifPresent(employeeIds -> work.setEmployees(Collections.emptyList()));
        // 작업의 나머지 필드들을 업데이트
        Optional.ofNullable(updateWorkDto.getWorkName()).ifPresent(work::setName);
        Optional.ofNullable(updateWorkDto.getAreaId())
                .map(areaId -> areaRepository.findById(areaId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 구역입니다.")))
                .ifPresent(work::setArea);
        Optional.ofNullable(updateWorkDto.getWorkStatus()).ifPresent(work::setStatus);
        // 업데이트된 작업 정보를 저장
        var updateWork = workRepository.save(work);
        // 수정된 작업에 포함된 근로자 정보를 DTO로 변환
        List<EmployeeDto.ReadEmployeeResponse> employeeResponses = work.getEmployees().stream()
                .map(employee -> EmployeeDto.ReadEmployeeResponse.builder()
                        .employeeId(employee.getId())
                        .employeeName(encryptUtil.decrypt(employee.getName()))
                        .employeeEmail(encryptUtil.decrypt(employee.getEmail()))
                        .employeePhoneNumber(encryptUtil.decrypt(employee.getPhoneNumber()))
                        .build())
                .collect(Collectors.toList());
        // 수정된 작업 정보를 담은 DTO 반환
        return WorkDto.ReadWorkResponse.builder()
                .workId(updateWork.getId())
                .employees(employeeResponses)
                .workName(updateWork.getName())
                .workStatus(updateWork.getStatus())
                .createdAt(updateWork.getCreatedAt())
                .updatedAt(updateWork.getUpdatedAt())
                .build();
    }

    /**
     * 작업을 삭제합니다.
     *
     * @param workId 삭제할 작업의 ID
     */
    @Override
    @Transactional
    public void delete(Long workId) {
        // 삭제할 작업이 존재하는지 확인하고 없으면 예외 처리
        var work = workRepository.findById(workId)
                .orElseThrow(() -> new EntityNotFoundException("No such work."));
        // 작업을 삭제
        workRepository.delete(work);
    }

    /**
     * 근로자들이 현재 진행 중인 작업에 참여하고 있는지 확인하는 메서드.
     * 주어진 근로자 ID 목록을 기반으로, 그 근로자들이 기존에 배정된 작업이 아닌 다른 'IN_PROGRESS' 상태의 작업에 참여 중인 경우
     * 해당 근로자들의 작업 상태를 반환합니다.
     *
     * @param employeeIds 확인할 근로자 ID 목록
     * @return 참여 중인 작업 정보를 담고 있는 DTO 리스트
     */
    private List<WorkDto.EmployeeWorkStatus> findInProgressWorkForEmployees(List<Long> employeeIds) {
        // WorkDto.ReadWorkRequest를 생성하여 진행 중인 작업을 조회
        var request = WorkDto.ReadWorkRequest.builder()
                .employeeIds(employeeIds)
                .workStatuses(List.of(WorkStatus.IN_PROGRESS)) // 'IN_PROGRESS' 상태의 작업만 필터링
                .build();
        // 조건에 맞는 작업 목록을 조회
        var workPage = workRepository.findAll(
                WorkSpecification.findWith(request), // 조건에 맞는 작업을 찾는 쿼리
                Pageable.unpaged());
        // 각 근로자가 참여 중인 작업 정보를 EmployeeWorkStatus DTO로 변환하여 반환
        return workPage.getContent().stream()
                .flatMap(work -> work.getEmployees().stream()
                        .filter(employee -> employeeIds.contains(employee.getId())) // 근로자 ID가 요청 목록에 포함된 경우
                        .filter(employee -> !work.getEmployees().contains(employee)) // 기존에 배정된 작업이 아닌 경우
                        .map(employee -> {
                            log.warn("근로자 ID: {}가 작업 ID: {}에 참여 중입니다.", employee.getId(), work.getId());
                            return WorkDto.EmployeeWorkStatus.builder()
                                    .employeeId(employee.getId())
                                    .workId(work.getId())
                                    .workName(work.getName())
                                    .workStatus(work.getStatus())
                                    .build();
                        })
                )
                .collect(Collectors.toList());
    }
}