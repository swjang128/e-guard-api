package atemos.eguard.api.service;

import atemos.eguard.api.config.EntityValidator;
import atemos.eguard.api.domain.AreaIncident;
import atemos.eguard.api.dto.AreaDto;
import atemos.eguard.api.dto.EventDto;
import atemos.eguard.api.dto.SettingDto;
import atemos.eguard.api.entity.Area;
import atemos.eguard.api.entity.Event;
import atemos.eguard.api.repository.AreaRepository;
import atemos.eguard.api.repository.EventRepository;
import atemos.eguard.api.specification.AreaSpecification;
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
 * AreaServiceImpl는 구역과 관련된 서비스 로직을 구현한 클래스입니다.
 * 구역 등록, 수정, 삭제 및 조회와 같은 기능을 제공합니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AreaServiceImpl implements AreaService {
    private final AreaRepository areaRepository;
    private final EventRepository eventRepository;
    private final EventService eventService;
    private final EntityValidator entityValidator;
    private final SettingService settingService;

    /**
     * 구역을 등록합니다.
     *
     * @param createAreaDto 등록할 구역 정보를 담고 있는 DTO
     * @return 등록된 구역 정보를 담고 있는 DTO 응답
     */
    @Override
    @Transactional
    public AreaDto.ReadAreaResponse create(AreaDto.CreateArea createAreaDto) {
        // 현재 접속한 근로자가 신규 구역에 등록할 공장에 접근 가능한지 검증 및 조회
        var factory = entityValidator.validateFactoryIds(List.of(createAreaDto.getFactoryId()))
                .stream().findFirst().orElseThrow(() -> new AccessDeniedException("공장을 찾을 수 없거나 등록 권한이 없습니다."));
        // 해당 업체의 시스템 설정에서 공장에 등록할 수 있는 최대 구역 생성량을 초과했는지 검증
        var setting = settingService.read(SettingDto.ReadSettingRequest.builder()
                .companyIds(List.of(factory.getCompany().getId()))
                .build());
        if (areaRepository.findByFactory(factory).size() > setting.getSettingList().getFirst().getMaxAreasPerFactory()) {
            throw new IllegalArgumentException("이 공장에 등록할 수 있는 구역 수가 최대치에 도달하였습니다. 추가 등록하려면 시스템 설정을 변경해주세요.");
        }
        // 구역 정보를 빌드하고 저장
        var area = Area.builder()
                .factory(factory)
                .name(createAreaDto.getAreaName())
                .location(createAreaDto.getAreaLocation())
                .usableSize(createAreaDto.getAreaUsableSize())
                .latitude(createAreaDto.getAreaLatitude())
                .longitude(createAreaDto.getAreaLongitude())
                .plan2DFilePath(createAreaDto.getAreaPlan2DFilePath())
                .plan3DFilePath(createAreaDto.getAreaPlan3DFilePath())
                .memo(createAreaDto.getAreaMemo())
                .build();
        area = areaRepository.save(area);
        // 저장된 구역 정보를 반환
        return AreaDto.ReadAreaResponse.builder()
                .areaId(area.getId())
                .areaName(area.getName())
                .areaLocation(area.getLocation())
                .areaUsableSize(area.getUsableSize())
                .areaLatitude(area.getLatitude())
                .areaLongitude(area.getLongitude())
                .areaPlan2DFilePath(area.getPlan2DFilePath())
                .areaPlan3DFilePath(area.getPlan3DFilePath())
                .areaMemo(area.getMemo())
                .factoryId(factory.getId())
                .factoryName(factory.getName())
                .eventName(AreaIncident.NORMAL.getName())
                .createdAt(area.getCreatedAt())
                .updatedAt(area.getUpdatedAt())
                .build();
    }

    /**
     * 조건에 맞는 구역 목록을 조회합니다.
     *
     * @param readAreaRequestDto 구역 조회 조건을 담고 있는 DTO
     * @param pageable 페이징 정보를 담고 있는 객체
     * @return 조회된 구역 목록과 페이지 정보를 포함한 응답 객체
     */
    @Override
    @Transactional(readOnly = true)
    public AreaDto.ReadAreaResponseList read(AreaDto.ReadAreaRequest readAreaRequestDto, Pageable pageable) {
        // readAreaRequestDto의 구역 ID 리스트가 존재하면 이 정보에 현재 접속한 근로자가 접근할 수 있는지 검증
        Optional.ofNullable(readAreaRequestDto.getAreaIds())
                .filter(ids -> !ids.isEmpty())
                .ifPresent(areaIds -> {
                    if (entityValidator.validateAreaIds(areaIds).isEmpty()) {
                        throw new AccessDeniedException("구역이 존재하지 않거나 조회 권한이 없습니다.");
                    }
                });
        // readAreaRequestDto의 공장 ID 리스트가 존재하면 이 정보에 현재 접속한 근로자가 접근할 수 있는지 검증
        Optional.ofNullable(readAreaRequestDto.getFactoryIds())
                .filter(ids -> !ids.isEmpty())
                .ifPresent(factoryIds -> {
                    if (entityValidator.validateFactoryIds(factoryIds).isEmpty()) {
                        throw new AccessDeniedException("공장이 존재하지 않거나 조회 권한이 없습니다.");
                    }
                });
        // 조건에 맞는 구역 목록을 페이지 단위로 조회
        var areaPage = areaRepository.findAll(
                AreaSpecification.findWith(readAreaRequestDto),
                pageable);
        // 조회된 구역 목록을 응답 객체로 변환하여 반환(사건 정보를 포함)
        var areaList = areaPage.getContent().stream()
                .map(area -> {
                    // 각 구역의 가장 최근 해결되지 않은 사건을 조회
                    var latestUnresolvedEvent = eventService.read(EventDto.ReadEventRequest.builder()
                                    .areaIds(List.of(area.getId()))  // 구역 ID에 해당하는 사건만 조회
                                    .eventResolved(false)  // 해결되지 않은 사건만 조회
                                    .page(0)          // 가장 최근 사건 조회
                                    .size(1)          // 1개의 사건만 필요
                                    .build(), Pageable.ofSize(1))
                            .getEventList().stream().findFirst();
                    // 최근 해결되지 않은 사건이 있으면 해당 사건의 type을 사용, 없으면 기본값 NORMAL 설정
                    var eventName = latestUnresolvedEvent.map(event -> event.getAreaIncident().getName())
                            .orElse(AreaIncident.NORMAL.getName());
                    // 현재 구역에서 해결되지 않은 가장 최근 사건 조회
                    var latestUnresolvedAreaEvent = eventRepository.findTopByAreaAndResolvedOrderByCreatedAtDesc(area, false);
                    var areaIncident = latestUnresolvedAreaEvent.map(Event::getAreaIncident).orElse(AreaIncident.NORMAL);
                    // AreaDto.ReadAreaResponse로 변환
                    return AreaDto.ReadAreaResponse.builder()
                            .areaId(area.getId())
                            .areaName(area.getName())
                            .areaLocation(area.getLocation())
                            .areaUsableSize(area.getUsableSize())
                            .areaLatitude(area.getLatitude())
                            .areaLongitude(area.getLongitude())
                            .areaPlan2DFilePath(area.getPlan2DFilePath())
                            .areaPlan3DFilePath(area.getPlan3DFilePath())
                            .areaMemo(area.getMemo())
                            .areaIncident(areaIncident)
                            .factoryId(area.getFactory().getId())
                            .factoryName(area.getFactory().getName())
                            .eventName(eventName)
                            .createdAt(area.getCreatedAt())
                            .updatedAt(area.getUpdatedAt())
                            .build();
                })
                .toList();
        return AreaDto.ReadAreaResponseList.builder()
                .areaList(areaList)
                .totalElements(areaPage.getTotalElements())
                .totalPages(areaPage.getTotalPages())
                .build();
    }

    /**
     * 기존 구역 정보를 수정합니다.
     *
     * @param areaId 수정할 구역의 ID
     * @param updateAreaDto 수정할 구역 정보를 담고 있는 DTO
     * @return 수정된 구역 정보를 담은 응답 객체
     */
    @Override
    @Transactional
    public AreaDto.ReadAreaResponse update(Long areaId, AreaDto.UpdateArea updateAreaDto) {
        // 기존 구역을 현재 접속한 근로자가 수정할 수 있는 권한이 있는지 검증 후 조회
        var area = entityValidator.validateAreaIds(List.of(areaId))
                .stream().findFirst()
                .orElseThrow(() -> new AccessDeniedException("구역을 찾을 수 없거나 수정 권한이 없습니다."));
        // 기존 구역에 등록된 공장에 현재 접속한 근로자가 접근 가능한지 검증
        entityValidator.validateFactoryIds(List.of(area.getFactory().getId()))
                .stream().findFirst()
                .orElseThrow(() -> new AccessDeniedException("기존에 등록된 공장을 찾을 수 없거나 수정 권한이 없습니다."));
        // 수정할 구역 정보에 등록된 공장으로 현재 접속한 근로자가 접근 가능한지 검증 후 조회하여 area 엔티티에 Set
        Optional.ofNullable(updateAreaDto.getFactoryId()).ifPresent(factoryId -> {
            var factory = entityValidator.validateFactoryIds(List.of(factoryId))
                    .stream().findFirst()
                    .orElseThrow(() -> new AccessDeniedException("공장을 찾을 수 없거나 수정 권한이 없습니다."));
            area.setFactory(factory);
        });
        // 구역의 나머지 필드들을 업데이트
        Optional.ofNullable(updateAreaDto.getAreaName()).ifPresent(area::setName);
        Optional.ofNullable(updateAreaDto.getAreaLocation()).ifPresent(area::setLocation);
        Optional.ofNullable(updateAreaDto.getAreaUsableSize()).ifPresent(area::setUsableSize);
        Optional.ofNullable(updateAreaDto.getAreaLatitude()).ifPresent(area::setLatitude);
        Optional.ofNullable(updateAreaDto.getAreaLongitude()).ifPresent(area::setLongitude);
        Optional.ofNullable(updateAreaDto.getAreaPlan2DFilePath()).ifPresent(area::setPlan2DFilePath);
        Optional.ofNullable(updateAreaDto.getAreaPlan3DFilePath()).ifPresent(area::setPlan3DFilePath);
        Optional.ofNullable(updateAreaDto.getAreaMemo()).ifPresent(area::setMemo);
        // 수정한 구역의 가장 최근 해결되지 않은 사건 상태 조회(null인 경우 NORMAL로 설정)
        var latestUnresolvedEvent = eventService.read(EventDto.ReadEventRequest.builder()
                .areaIds(List.of(area.getId()))
                .eventResolved(false)  // 해결되지 않은 사건만 조회
                .page(0)          // 가장 최근 사건 조회
                .size(1)          // 1개의 사건만 필요
                .build(), Pageable.ofSize(1)).getEventList().stream().findFirst();
        // 최근 사건이 있으면 사건을 설정하고 없으면 기본값 NORMAL로 설정
        var eventName = latestUnresolvedEvent.map(event -> event.getAreaIncident().getName())
                .orElse(AreaIncident.NORMAL.getName());
        // 구역을 저장하고, 저장된 정보를 기반으로 응답 객체 생성 및 반환
        areaRepository.save(area);
        return AreaDto.ReadAreaResponse.builder()
                .areaId(area.getId())
                .areaName(area.getName())
                .areaLocation(area.getLocation())
                .areaUsableSize(area.getUsableSize())
                .areaLatitude(area.getLatitude())
                .areaLongitude(area.getLongitude())
                .areaPlan2DFilePath(area.getPlan2DFilePath())
                .areaPlan3DFilePath(area.getPlan3DFilePath())
                .areaMemo(area.getMemo())
                .factoryId(area.getFactory().getId())
                .factoryName(area.getFactory().getName())
                .eventName(eventName)
                .createdAt(area.getCreatedAt())
                .updatedAt(area.getUpdatedAt())
                .build();
    }

    /**
     * 구역을 삭제합니다.
     *
     * @param areaId 삭제할 구역의 ID
     */
    @Override
    @Transactional
    public void delete(Long areaId) {
        // 삭제할 구역이 존재하는지 확인하고 없으면 예외 처리
        var area = areaRepository.findById(areaId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 구역입니다."));
        // 구역을 삭제
        areaRepository.delete(area);
    }
}