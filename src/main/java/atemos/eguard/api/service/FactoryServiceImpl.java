package atemos.eguard.api.service;

import atemos.eguard.api.config.EntityValidator;
import atemos.eguard.api.domain.WorkStatus;
import atemos.eguard.api.dto.FactoryDto;
import atemos.eguard.api.dto.SettingDto;
import atemos.eguard.api.entity.Employee;
import atemos.eguard.api.entity.Factory;
import atemos.eguard.api.repository.EmployeeRepository;
import atemos.eguard.api.repository.EventRepository;
import atemos.eguard.api.repository.FactoryRepository;
import atemos.eguard.api.repository.WorkRepository;
import atemos.eguard.api.specification.FactorySpecification;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * FactoryServiceImpl는 공장과 관련된 서비스 로직을 구현한 클래스입니다.
 * 공장 등록, 수정, 삭제 및 조회와 같은 기능을 제공합니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FactoryServiceImpl implements FactoryService {
    private final FactoryRepository factoryRepository;
    private final EmployeeRepository employeeRepository;
    private final EventRepository eventRepository;
    private final WorkRepository workRepository;
    private final AuthenticationServiceImpl authenticationService;
    private final EntityValidator entityValidator;
    private final SettingService settingService;

    /**
     * 공장을 등록합니다.
     *
     * @param createFactoryDto 등록할 공장 정보를 담고 있는 DTO
     * @return 등록된 공장 정보를 담고 있는 DTO 응답
     */
    @Override
    @Transactional
    public FactoryDto.ReadFactoryResponse create(FactoryDto.CreateFactory createFactoryDto) {
        // 현재 접속한 근로자가 신규 공장에 등록하려는 업체로 접근 가능한지 검증 및 조회
        var company = entityValidator.validateCompanyIds(List.of(createFactoryDto.getCompanyId()))
                .stream().findFirst().orElseThrow(() -> new AccessDeniedException("업체를 찾을 수 없거나 등록 권한이 없습니다."));
        // 해당 업체의 시스템 설정에서 업체에 등록할 수 있는 최대 공장 생성량을 초과했는지 검증
        var setting = settingService.read(SettingDto.ReadSettingRequest.builder()
                        .companyIds(List.of(company.getId()))
                        .build());
        if (factoryRepository.findByCompany(company).size() > setting.getSettingList().getFirst().getMaxFactoriesPerCompany()) {
            throw new IllegalArgumentException("이 업체에 등록할 수 있는 공장 수가 최대치에 도달하였습니다. 추가 등록하려면 시스템 설정을 변경해주세요.");
        }
        // 공장 정보를 빌드하고 저장
        var factory = Factory.builder()
                .company(company)
                .name(createFactoryDto.getFactoryName())
                .address(createFactoryDto.getFactoryAddress())
                .addressDetail(createFactoryDto.getFactoryAddressDetail())
                .totalSize(createFactoryDto.getFactoryTotalSize())
                .structureSize(createFactoryDto.getFactoryStructureSize())
                .industryType(createFactoryDto.getFactoryIndustryType())
                .build();
        factory = factoryRepository.save(factory);
        // 저장된 공장 정보를 DTO로 변환하여 반환
        return FactoryDto.ReadFactoryResponse.builder()
                .factoryId(factory.getId())
                .factoryName(factory.getName())
                .factoryAddress(factory.getAddress())
                .factoryAddressDetail(factory.getAddressDetail())
                .factoryTotalSize(factory.getTotalSize())
                .factoryStructureSize(factory.getStructureSize())
                .factoryIndustryType(factory.getIndustryType())
                .companyId(factory.getCompany().getId())
                .companyName(factory.getCompany().getName())
                .companyPhoneNumber(factory.getCompany().getPhoneNumber())
                .companyAddress(factory.getCompany().getAddress())
                .createdAt(factory.getCreatedAt())
                .updatedAt(factory.getUpdatedAt())
                .build();
    }

    /**
     * 조건에 맞는 공장 목록을 조회합니다.
     *
     * @param readFactoryRequestDto 공장 조회 조건을 담고 있는 DTO
     * @param pageable 페이징 정보를 담고 있는 객체
     * @return 조회된 공장 목록과 페이지 정보를 포함한 응답 객체
     */
    @Override
    @Transactional(readOnly = true)
    public FactoryDto.ReadFactoryResponseList read(FactoryDto.ReadFactoryRequest readFactoryRequestDto, Pageable pageable) {
        // readFactoryRequestDto의 공장 ID 리스트가 존재하면 이 정보에 현재 접속한 근로자가 접근할 수 있는지 검증
        Optional.ofNullable(readFactoryRequestDto.getFactoryIds())
                .filter(ids -> !ids.isEmpty())
                .ifPresent(factoryIds -> {
                    if (entityValidator.validateFactoryIds(factoryIds).isEmpty()) {
                        throw new AccessDeniedException("공장이 존재하지 않거나 조회 권한이 없습니다.");
                    }
                });
        // readFactoryRequestDto의 업체 ID 리스트가 존재하면 이 정보에 현재 접속한 근로자가 접근할 수 있는지 검증
        Optional.ofNullable(readFactoryRequestDto.getCompanyIds())
                .filter(ids -> !ids.isEmpty())
                .ifPresent(companyIds -> {
                    if (entityValidator.validateCompanyIds(companyIds).isEmpty()) {
                        throw new AccessDeniedException("업체가 존재하지 않거나 조회 권한이 없습니다.");
                    }
                });
        // 조건에 맞는 공장 목록을 페이지 단위로 조회
        var factoryPage = factoryRepository.findAll(
                FactorySpecification.findWith(readFactoryRequestDto),
                pageable);
        // 조회된 공장 목록을 응답 객체로 변환하여 반환
        var factoryList = factoryPage.getContent().stream()
                .map(factory -> FactoryDto.ReadFactoryResponse.builder()
                        .factoryId(factory.getId())
                        .factoryName(factory.getName())
                        .factoryAddress(factory.getAddress())
                        .factoryAddressDetail(factory.getAddressDetail())
                        .factoryTotalSize(factory.getTotalSize())
                        .factoryStructureSize(factory.getStructureSize())
                        .factoryIndustryType(factory.getIndustryType())
                        .companyId(factory.getCompany().getId())
                        .companyName(factory.getCompany().getName())
                        .companyPhoneNumber(factory.getCompany().getPhoneNumber())
                        .companyAddress(factory.getCompany().getAddress())
                        .createdAt(factory.getCreatedAt())
                        .updatedAt(factory.getUpdatedAt())
                        .build())
                .toList();
        return FactoryDto.ReadFactoryResponseList.builder()
                .factoryList(factoryList)
                .totalElements(factoryPage.getTotalElements())
                .totalPages(factoryPage.getTotalPages())
                .build();
    }

    /**
     * 기존 공장 정보를 수정합니다.
     *
     * @param factoryId 수정할 공장의 ID
     * @param updateFactoryDto 수정할 공장 정보를 담고 있는 DTO
     * @return 수정된 공장 정보를 담은 응답 객체
     */
    @Override
    @Transactional
    public FactoryDto.ReadFactoryResponse update(Long factoryId, FactoryDto.UpdateFactory updateFactoryDto) {
        // 기존 공장을 현재 접속한 근로자가 수정할 수 있는 권한이 있는지 검증 후 조회
        var factory = entityValidator.validateFactoryIds(List.of(factoryId))
                .stream().findFirst()
                .orElseThrow(() -> new AccessDeniedException("공장을 찾을 수 없거나 수정 권한이 없습니다."));
        // 기존 공장이 속한 업체에 현재 접속한 근로자가 수정할 수 있는 권한이 있는지 검증
        entityValidator.validateCompanyIds(List.of(factory.getCompany().getId()))
                .stream().findFirst()
                .orElseThrow(() -> new AccessDeniedException("기존에 등록된 업체를 찾을 수 없거나 수정 권한이 없습니다."));
        // 수정하려는 새로운 업체에 현재 접속한 근로자가 접근 가능한지 검증 후 factory 엔티티에 Set
        Optional.ofNullable(updateFactoryDto.getCompanyId()).ifPresent(companyId -> {
            var company = entityValidator.validateCompanyIds(List.of(companyId))
                    .stream().findFirst()
                    .orElseThrow(() -> new AccessDeniedException("업체를 찾을 수 없거나 수정 권한이 없습니다."));
            factory.setCompany(company);
        });
        // 공장의 나머지 필드들을 Optional로 처리하여 업데이트
        Optional.ofNullable(updateFactoryDto.getFactoryTotalSize()).ifPresent(factory::setTotalSize);
        Optional.ofNullable(updateFactoryDto.getFactoryStructureSize()).ifPresent(factory::setStructureSize);
        Optional.ofNullable(updateFactoryDto.getFactoryIndustryType()).ifPresent(factory::setIndustryType);
        Optional.ofNullable(updateFactoryDto.getFactoryName()).ifPresent(factory::setName);
        Optional.ofNullable(updateFactoryDto.getFactoryAddress()).ifPresent(factory::setAddress);
        Optional.ofNullable(updateFactoryDto.getFactoryAddressDetail()).ifPresent(factory::setAddressDetail);
        // 수정된 공장 정보를 저장하고 응답 객체로 반환
        var savedFactory = factoryRepository.save(factory);
        return FactoryDto.ReadFactoryResponse.builder()
                .factoryId(savedFactory.getId())
                .factoryName(savedFactory.getName())
                .factoryAddress(savedFactory.getAddress())
                .factoryAddressDetail(savedFactory.getAddressDetail())
                .factoryTotalSize(factory.getTotalSize())
                .factoryStructureSize(factory.getStructureSize())
                .factoryIndustryType(factory.getIndustryType())
                .companyId(savedFactory.getCompany().getId())
                .companyName(savedFactory.getCompany().getName())
                .companyPhoneNumber(savedFactory.getCompany().getPhoneNumber())
                .companyAddress(savedFactory.getCompany().getAddress())
                .createdAt(savedFactory.getCreatedAt())
                .updatedAt(savedFactory.getUpdatedAt())
                .build();
    }

    /**
     * 공장을 삭제합니다.
     *
     * @param factoryId 삭제할 공장의 ID
     */
    @Override
    @Transactional
    public void delete(Long factoryId) {
        // 삭제할 공장이 존재하는지 확인하고 없으면 예외 처리
        var factory = factoryRepository.findById(factoryId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 공장입니다."));
        // 공장을 삭제
        factoryRepository.delete(factory);
    }

    /**
     * JWT 토큰을 사용하여 현재 로그인된 근로자의 공장 정보를 조회합니다.
     *
     * @return 공장 정보 객체입니다. 근로자가 속한 공장 정보가 포함됩니다.
     */
    @Override
    @Transactional(readOnly = true)
    public FactoryDto.ReadFactoryResponse readFactoryInfo() {
        // 현재 근로자의 정보 조회
        var employeeInfo = authenticationService.getCurrentEmployeeInfo();
        // 근로자가 속한 공장을 조건에 맞게 조회
        var readFactoryRequestDto = FactoryDto.ReadFactoryRequest.builder()
                .factoryIds(List.of(employeeInfo.getFactoryId()))
                .build();
        var factoryPageResponse = this.read(readFactoryRequestDto, Pageable.ofSize(1));
        // 조회된 공장이 없을 경우 예외 처리
        return factoryPageResponse.getFactoryList().stream()
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 공장입니다."));
    }

    /**
     * 근로자를 등록할 때 해당 업체의 공장 목록을 조회합니다.
     * @param readFactoryRequestDto 공장 목록 조회를 위한 요청 데이터 전송 객체입니다.
     * @return 해당 업체의 공장 목록 응답 객체입니다.
     */
    @Override
    public FactoryDto.ReadFactoryResponseList readSignUpFactoryList(FactoryDto.ReadFactoryRequest readFactoryRequestDto) {
        // 조건에 맞는 공장 목록을 페이지 단위로 조회
        var factories = factoryRepository.findAll(
                FactorySpecification.findWith(readFactoryRequestDto));
        // 조회된 공장 목록을 응답 객체로 변환하여 반환
        var factoryList = factories.stream()
                .map(factory -> FactoryDto.ReadFactoryResponse.builder()
                        .factoryId(factory.getId())
                        .factoryName(factory.getName())
                        .factoryAddress(factory.getAddress())
                        .factoryAddressDetail(factory.getAddressDetail())
                        .factoryTotalSize(factory.getTotalSize())
                        .factoryStructureSize(factory.getStructureSize())
                        .factoryIndustryType(factory.getIndustryType())
                        .companyId(factory.getCompany().getId())
                        .companyName(factory.getCompany().getName())
                        .companyPhoneNumber(factory.getCompany().getPhoneNumber())
                        .companyAddress(factory.getCompany().getAddress())
                        .createdAt(factory.getCreatedAt())
                        .updatedAt(factory.getUpdatedAt())
                        .build())
                .toList();
        return FactoryDto.ReadFactoryResponseList.builder()
                .factoryList(factoryList)
                .build();
    }

    /**
     * 공장 요약 정보를 조회합니다.
     * 지정된 공장의 전체 근로자 수와 상태별 근로자 수 정보를 제공합니다.
     *
     * @param factorySummaryRequestDto 공장 요약 정보 조회를 위한 요청 객체입니다.
     * @return 공장 요약 정보 객체입니다. 전체 근로자 수와 상태별 근로자 수가 포함됩니다.
     */
    @Override
    public FactoryDto.FactorySummaryResponse getFactorySummary(FactoryDto.FactorySummaryRequest factorySummaryRequestDto) {
        // factoryId에 해당하는 공장을 조회
        var factory = factoryRepository.findById(factorySummaryRequestDto.getFactoryId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 공장입니다."));
        // factoryId에 대응하는 공장 정보에 현재 접속한 근로자가 접근할 수 있는지 검증(같은 업체의 공장인지)
        if (entityValidator.validateCompanyIds(List.of(factory.getCompany().getId())).isEmpty()) {
            throw new AccessDeniedException("업체가 존재하지 않거나 조회 권한이 없습니다.");
        }
        // 해당 공장에 근무 중인 전체 근로자를 조회
        var employees = employeeRepository.findByFactoryAndRole(factory, factorySummaryRequestDto.getRole());
        var totalEmployees = employees.size();
        var injuryEmployees = 0;
        var criticalHealthIssueEmployees = 0;
        var minorHealthIssueEmployees = 0;
        var onLeaveEmployees = 0;
        var normalEmployees = 0;
        var unassignedEmployees = 0;
        // 조회한 근로자 ID로 Event 엔티티에서 아직 해결되지 않은(resolved = false) 가장 최근 employeeIncident가 있는지 조회(없으면 NORMAL로 간주)
        for (Employee employee : employees) {
            // 가장 최근에 발생한 해결되지 않은 EmployeeIncident 조회
            var recentIncident = eventRepository.findTopByEmployeeAndResolvedOrderByCreatedAtDesc(employee, false)
                    .orElse(null);
            // 사건에 따른 상태별 근로자 수 계산
            if (recentIncident != null) {
                switch (recentIncident.getEmployeeIncident()) {
                    case INJURY:
                        injuryEmployees++;
                        break;
                    case CRITICAL_HEALTH_ISSUE:
                        criticalHealthIssueEmployees++;
                        break;
                    case MINOR_HEALTH_ISSUE:
                        minorHealthIssueEmployees++;
                        break;
                    case ON_LEAVE:
                        onLeaveEmployees++;
                        break;
                    default:
                        break;
                }
            } else {
                // 사건이 없고 현재 근로자에게 PENDING, IN_PROGRESS 상태의 작업이 없는 경우 unassignedEmployees 증가
                var hasActiveWork = workRepository.existsByEmployeesContainingAndStatusIn(
                        employee, List.of(WorkStatus.PENDING, WorkStatus.IN_PROGRESS)
                );
                normalEmployees += hasActiveWork ? 1 : 0;
                unassignedEmployees += hasActiveWork ? 0 : 1;
            }
        }
        // 결과를 DTO에 담아 리턴
        return FactoryDto.FactorySummaryResponse.builder()
                .factoryName(factory.getName())
                .totalEmployees(totalEmployees)
                .injuryEmployees(injuryEmployees)
                .criticalHealthIssueEmployees(criticalHealthIssueEmployees)
                .minorHealthIssueEmployees(minorHealthIssueEmployees)
                .onLeaveEmployees(onLeaveEmployees)
                .normalEmployees(normalEmployees)
                .unassignedEmployees(unassignedEmployees)
                .build();
    }
}