package atemos.eguard.api.controller;

import atemos.eguard.api.config.ApiResponseManager;
import atemos.eguard.api.domain.AreaIncident;
import atemos.eguard.api.domain.EmployeeIncident;
import atemos.eguard.api.dto.ApiResponseDto;
import atemos.eguard.api.dto.EventDto;
import atemos.eguard.api.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 사건 관리 API 컨트롤러.
 * 이 클래스는 사건 관리와 관련된 API 엔드포인트를 정의합니다.
 * 사건 등록, 조회, 수정, 삭제 기능을 제공합니다.
 */
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/event")
@Tag(name = "사건 관리 API", description = "사건 관리 API 모음")
public class EventController {
    private final ApiResponseManager apiResponseManager;
    private final EventService eventService;

    /**
     * 사건 등록 API.
     * 새로운 사건 정보를 등록합니다.
     *
     * @param createEvent 사건 등록 요청 데이터
     * @return 등록된 사건 정보
     */
    @Operation(summary = "사건 등록", description = "사건 정보를 등록하는 API")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @PostMapping
    public ResponseEntity<ApiResponseDto> create(@Valid @RequestBody EventDto.CreateEvent createEvent) {
        return apiResponseManager.success(eventService.create(createEvent));
    }

    /**
     * 조건에 맞는 사건 조회 API.
     * 여러 조건을 기반으로 사건을 조회합니다.
     *
     * @param eventId 사건 ID 리스트
     * @param employeeId 근로자 ID 리스트
     * @param areaId 구역 ID 리스트
     * @param factoryId 공장 ID 리스트
     * @param employeeIncident 근로자에게 발생한 사건 유형 리스트
     * @param areaIncident 구역에서 발생한 사건 유형 리스트
     * @param eventResolved 사건 해결 여부
     * @param searchStartDate 조회 시작일(createdAt 기준)
     * @param searchEndDate 조회 종료일(createdAt 기준)
     * @param page 페이지 번호
     * @param size 페이지 당 데이터 개수
     * @return 조건에 맞는 사건 정보
     */
    @Operation(summary = "조건에 맞는 사건 조회", description = "조건에 맞는 사건을 조회하는 API")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @GetMapping
    public ResponseEntity<ApiResponseDto> read(
            @Parameter(description = "사건 ID 리스트") @RequestParam(required = false) List<Long> eventId,
            @Parameter(description = "근로자 ID 리스트") @RequestParam(required = false) List<Long> employeeId,
            @Parameter(description = "구역 ID 리스트") @RequestParam(required = false) List<Long> areaId,
            @Parameter(description = "공장 ID 리스트") @RequestParam(required = false) List<Long> factoryId,
            @Parameter(description = "근로자에게 발생한 사건 유형 리스트") @RequestParam(required = false) List<EmployeeIncident> employeeIncident,
            @Parameter(description = "구역에서 발생한 사건 유형 리스트") @RequestParam(required = false) List<AreaIncident> areaIncident,
            @Parameter(description = "사건 해결 여부") @RequestParam(required = false) Boolean eventResolved,
            @Parameter(description = "조회 시작일(createdAt 기준)") @RequestParam(required = false) LocalDate searchStartDate,
            @Parameter(description = "조회 종료일(createdAt 기준)") @RequestParam(required = false) LocalDate searchEndDate,
            @Parameter(description = "페이지 번호", example = "0") @RequestParam(required = false) Integer page,
            @Parameter(description = "페이지 당 데이터 개수", example = "10") @RequestParam(required = false) Integer size
    ) {
        return apiResponseManager.success(eventService.read(
                EventDto.ReadEventRequest.builder()
                        .eventIds(eventId)
                        .employeeIds(employeeId)
                        .areaIds(areaId)
                        .factoryIds(factoryId)
                        .employeeIncidents(employeeIncident)
                        .areaIncidents(areaIncident)
                        .eventResolved(eventResolved)
                        .searchStartDate(searchStartDate)
                        .searchEndDate(searchEndDate)
                        .page(page)
                        .size(size)
                        .build(),
                (page != null && size != null) ? PageRequest.of(page, size) : Pageable.unpaged()));
    }

    /**
     * 사건 정보 수정 API.
     * 기존 사건 정보를 수정합니다.
     *
     * @param eventId 사건 ID
     * @param updateEvent 사건 수정 요청 데이터
     * @return 수정된 사건 정보
     */
    @Operation(summary = "사건 정보 수정", description = "사건 정보를 수정하는 API")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @PatchMapping("/{eventId}")
    public ResponseEntity<ApiResponseDto> update(
            @Parameter(description = "사건 ID", example = "1", required = true) @PathVariable() Long eventId,
            @Valid @RequestBody EventDto.UpdateEvent updateEvent
    ) {
        return apiResponseManager.success(eventService.update(eventId, updateEvent));
    }

    /**
     * 사건 삭제 API.
     * 지정된 ID의 사건을 삭제합니다.
     *
     * @param eventId 사건 ID
     * @return 삭제 결과
     */
    @Operation(summary = "사건 삭제", description = "사건을 삭제하는 API")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{eventId}")
    public ResponseEntity<ApiResponseDto> delete(
            @Parameter(description = "사건 ID", example = "1", required = true) @PathVariable() Long eventId
    ) {
        eventService.delete(eventId);
        return apiResponseManager.ok();
    }

    /**
     * 공장의 안전 점수 및 안전 등급을 산출하는 API.
     * 특정 업체 내의 공장을 대상으로 안전 점수 및 안전 등급을 산출합니다.
     *
     * @param factoryId 공장 ID
     * @return 업체 내 공장의 안전 점수 및 안전 등급 정보
     */
    @Operation(summary = "업체별 공장의 안전 점수 조회", description = "업체 내 공장에서 발생한 사건을 바탕으로 안전 점수를 산출하여 응답합니다.")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @GetMapping("/score")
    public ResponseEntity<ApiResponseDto> readSafetyScore(
            @Parameter(description = "공장 ID", required = true) @RequestParam Long factoryId
    ) {
        return apiResponseManager.success(eventService.readSafetyScore(factoryId));
    }

    /**
     * 모든 사건을 해결하여 일괄 수정하는 API.
     * 모든 사건을 해결 상태로 일괄 수정합니다.
     */
    @Operation(summary = "모든 사건 일괄 해결", description = "모든 사건을 해결하여 일괄 수정하는 API")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/resolve")
    public ResponseEntity<ApiResponseDto> resolveAllEvents() {
        eventService.resolveAllEvents();
        return apiResponseManager.ok();
    }
}