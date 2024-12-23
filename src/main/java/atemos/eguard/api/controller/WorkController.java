package atemos.eguard.api.controller;

import atemos.eguard.api.config.ApiResponseManager;
import atemos.eguard.api.domain.WorkStatus;
import atemos.eguard.api.dto.ApiResponseDto;
import atemos.eguard.api.dto.WorkDto;
import atemos.eguard.api.service.WorkService;
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

import java.util.List;

/**
 * 작업 관리 API 컨트롤러.
 * 이 클래스는 작업 관리와 관련된 API 엔드포인트를 정의합니다.
 * 작업 등록, 조회, 수정, 삭제 기능을 제공합니다.
 */
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/work")
@Tag(name = "작업 관리 API", description = "작업 관리 API 모음")
public class WorkController {
    private final ApiResponseManager apiResponseManager;
    private final WorkService workService;

    /**
     * 작업 등록 API.
     * 새로운 작업 정보를 등록합니다.
     *
     * @param createWork 작업 등록 요청 데이터
     * @return 등록된 작업 정보
     */
    @Operation(summary = "작업 등록", description = "작업 정보를 등록하는 API")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @PostMapping
    public ResponseEntity<ApiResponseDto> create(@Valid @RequestBody WorkDto.CreateWork createWork) {
        return apiResponseManager.success(workService.create(createWork));
    }

    /**
     * 조건에 맞는 작업 조회 API.
     * 여러 조건을 기반으로 작업을 조회합니다.
     *
     * @param workId 작업 ID 리스트
     * @param factoryId 작업이 이루어지는 구역이 속한 공장 ID 리스트
     * @param areaId 작업이 이루어지는 구역 ID 리스트
     * @param employeeId 작업에 투입된 근로자 ID 리스트
     * @param workName 작업명
     * @param workStatus 작업 상태 리스트
     * @param page 페이지 번호
     * @param size 페이지당 데이터 개수
     * @return 조건에 맞는 작업 목록과 페이징 정보를 포함한 응답 객체를 반환합니다.
     */
    @Operation(summary = "조건에 맞는 작업 조회", description = "조건에 맞는 작업을 조회하는 API")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @GetMapping
    public ResponseEntity<ApiResponseDto> read(
            @Parameter(description = "작업 ID 리스트") @RequestParam(required = false) List<Long> workId,
            @Parameter(description = "작업 ID 리스트") @RequestParam(required = false) List<Long> factoryId,
            @Parameter(description = "작업 ID 리스트") @RequestParam(required = false) List<Long> areaId,
            @Parameter(description = "작업 ID 리스트") @RequestParam(required = false) List<Long> employeeId,
            @Parameter(description = "작업 ID 리스트") @RequestParam(required = false) String workName,
            @Parameter(description = "작업 ID 리스트") @RequestParam(required = false) List<WorkStatus> workStatus,
            @Parameter(description = "작업 ID 리스트") @RequestParam(required = false) Integer page,
            @Parameter(description = "작업 ID 리스트") @RequestParam(required = false) Integer size
    ) {
        return apiResponseManager.success(workService.read(
                WorkDto.ReadWorkRequest.builder()
                        .workIds(workId)
                        .factoryIds(factoryId)
                        .areaIds(areaId)
                        .employeeIds(employeeId)
                        .workName(workName)
                        .workStatuses(workStatus)
                        .build(),
                (page != null && size != null) ? PageRequest.of(page, size) : Pageable.unpaged()));
    }

    /**
     * 작업 정보 수정 API.
     * 기존 작업 정보를 수정합니다.
     *
     * @param workId 작업 ID
     * @param updateWork 작업 수정 요청 데이터
     * @return 수정된 작업 정보
     */
    @Operation(summary = "작업 정보 수정", description = "작업 정보를 수정하는 API")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @PatchMapping("/{workId}")
    public ResponseEntity<ApiResponseDto> update(
            @Parameter(description = "작업 ID", example = "1", required = true) @PathVariable() Long workId,
            @Valid @RequestBody WorkDto.UpdateWork updateWork
    ) {
        return apiResponseManager.success(workService.update(workId, updateWork));
    }

    /**
     * 작업 삭제 API.
     * 지정된 ID의 작업을 삭제합니다.
     *
     * @param workId 작업 ID
     * @return 삭제 결과
     */
    @Operation(summary = "작업 삭제", description = "작업을 삭제하는 API")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{workId}")
    public ResponseEntity<ApiResponseDto> delete(
            @Parameter(description = "작업 ID", example = "1", required = true) @PathVariable() Long workId
    ) {
        workService.delete(workId);
        return apiResponseManager.ok();
    }
}