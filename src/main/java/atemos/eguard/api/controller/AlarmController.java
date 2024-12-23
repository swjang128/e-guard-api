package atemos.eguard.api.controller;

import atemos.eguard.api.config.ApiResponseManager;
import atemos.eguard.api.domain.AreaIncident;
import atemos.eguard.api.domain.EmployeeIncident;
import atemos.eguard.api.dto.AlarmDto;
import atemos.eguard.api.dto.ApiResponseDto;
import atemos.eguard.api.service.AlarmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 알람 API 컨트롤러.
 * 이 클래스는 알람과 관련된 API 엔드포인트를 정의합니다.
 * 알람 조회 및 삭제 기능을 제공합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/alarm")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "알람 API", description = "알람 API 모음")
public class AlarmController {
    private final ApiResponseManager apiResponseManager;
    private final AlarmService alarmService;

    /**
     * 알람을 등록하는 API.
     * 특정 구역에서 발생한 사건에 대해 알람을 생성합니다.
     *
     * @param createAlarmDto 알람을 생성하는 데 필요한 정보를 담고 있는 DTO
     * @return 생성된 알람 정보
     */
    @Operation(summary = "알람 등록", description = "특정 구역에서 발생한 사건에 대한 알람을 생성하는 API")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @PostMapping
    public ResponseEntity<ApiResponseDto> create(@Valid @RequestBody AlarmDto.CreateAlarm createAlarmDto) {
        return apiResponseManager.success(alarmService.create(createAlarmDto));
    }

    /**
     * 알람 조회 API.
     * 주어진 조건에 따라 알람 데이터를 조회하여 반환합니다.
     *
     * @param alarmId 알람 ID 리스트
     * @param employeeId 근로자 ID 리스트
     * @param eventId 사건 ID 리스트
     * @param factoryId 공장 ID 리스트
     * @param companyId 업체 ID 리스트
     * @param alarmMessage 알람 메시지
     * @param alarmRead 알람 읽음 여부
     * @param employeeIncident 근로자에게 일어난 목록
     * @param areaIncident 구역에서 일어난 사건 목록
     * @param searchStartTime 조회 시작일시
     * @param searchEndTime 조회 종료일시
     * @param page 페이지 번호
     * @param size 페이지당 데이터 개수
     * @return 조건에 맞는 알람 목록과 페이징 정보를 포함한 응답 객체를 반환합니다.
     */
    @Operation(summary = "조건에 맞는 알람 조회", description = "조건에 맞는 알람 조회 API")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @GetMapping
    public ResponseEntity<ApiResponseDto> read(
            @Parameter(description = "알람 ID 리스트") @RequestParam(required = false) List<Long> alarmId,
            @Parameter(description = "근로자 ID 리스트") @RequestParam(required = false) List<Long> employeeId,
            @Parameter(description = "사건 ID 리스트") @RequestParam(required = false) List<Long> eventId,
            @Parameter(description = "공장 ID 리스트") @RequestParam(required = false) List<Long> factoryId,
            @Parameter(description = "업체 ID 리스트") @RequestParam(required = false) List<Long> companyId,
            @Parameter(description = "알람 메시지") @RequestParam(required = false) String alarmMessage,
            @Parameter(description = "알람 읽음 여부") @RequestParam(required = false) Boolean alarmRead,
            @Parameter(description = "근로자에게 일어난 사건 목록") @RequestParam(required = false) List<EmployeeIncident> employeeIncident,
            @Parameter(description = "구역에서 일어난 사건 목록") @RequestParam(required = false) List<AreaIncident> areaIncident,
            @Parameter(description = "조회 시작일시") @RequestParam(required = false) LocalDateTime searchStartTime,
            @Parameter(description = "조회 종료일시") @RequestParam(required = false) LocalDateTime searchEndTime,
            @Parameter(description = "페이지 번호", example = "0") @RequestParam(required = false) Integer page,
            @Parameter(description = "페이지당 데이터 개수", example = "10") @RequestParam(required = false) Integer size
    ) {
        return apiResponseManager.success(alarmService.read(
                AlarmDto.ReadAlarmRequest.builder()
                        .alarmIds(alarmId)
                        .employeeIds(employeeId)
                        .eventIds(eventId)
                        .factoryIds(factoryId)
                        .companyIds(companyId)
                        .alarmMessage(alarmMessage)
                        .alarmRead(alarmRead)
                        .employeeIncidents(employeeIncident)
                        .areaIncidents(areaIncident)
                        .searchStartTime(searchStartTime)
                        .searchEndTime(searchEndTime)
                        .build(),
                (page != null && size != null) ? PageRequest.of(page, size) : Pageable.unpaged()));
    }

    /**
     * 알람 수정 API.
     * 기존 알람을 수정합니다.
     *
     * @param alarmId 알람 ID
     * @param updateAlarm 수정할 알람 정보
     * @return 수정된 알람 정보
     */
    @Operation(summary = "알람 수정", description = "기존 알람을 수정하는 API")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @PatchMapping("/{alarmId}")
    public ResponseEntity<ApiResponseDto> update(
            @PathVariable Long alarmId,
            @Valid @RequestBody AlarmDto.UpdateAlarm updateAlarm
    ) {
        return apiResponseManager.success(alarmService.update(alarmId, updateAlarm));
    }
    
    /**
     * 알람 정보를 삭제하는 메서드.
     * 특정 알람 ID를 기반으로 알람을 삭제합니다.
     *
     * @param alarmId 알람 ID
     * @return 삭제 결과
     */
    @Operation(summary = "알람 삭제", description = "알람 정보를 삭제하는 API")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{alarmId}")
    public ResponseEntity<ApiResponseDto> delete(
            @Parameter(description = "알람 ID", example = "2") @PathVariable Long alarmId
    ) {
        alarmService.delete(alarmId);
        return apiResponseManager.ok();
    }

    /**
     * 실시간 알람 스트림을 제공하는 SSE 엔드포인트.
     * 클라이언트와의 SSE 연결을 통해 실시간으로 알람을 전송합니다.
     *
     * @param readAlarmRequest 알람 조회 조건을 포함하는 DTO로, 업체 ID, 읽음 여부 등의 정보를 포함합니다.
     * @return SSE 연결을 위한 SseEmitter 객체로, 클라이언트에게 실시간 알람을 스트리밍합니다.
     */
    @Operation(summary = "실시간 알람 스트림", description = "실시간으로 알람을 스트리밍하는 SSE 엔드포인트")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamAlarm(@Valid @ModelAttribute AlarmDto.ReadAlarmRequest readAlarmRequest) {
        return alarmService.streamAlarm(readAlarmRequest);
    }

    /**
     * 모든 알람을 읽음 상태로 일괄 수정하는 API.
     * 모든 알람을 읽음 상태로 일괄 수정합니다.
     */
    @Operation(summary = "모든 알람 읽음 처리", description = "모든 알람을 읽음 처리하는 API")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/read")
    public ResponseEntity<ApiResponseDto> readAllAlarms() {
        alarmService.readAllAlarms();
        return apiResponseManager.ok();
    }
}