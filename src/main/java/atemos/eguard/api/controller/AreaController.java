package atemos.eguard.api.controller;

import atemos.eguard.api.config.ApiResponseManager;
import atemos.eguard.api.dto.ApiResponseDto;
import atemos.eguard.api.dto.AreaDto;
import atemos.eguard.api.service.AreaService;
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

import java.math.BigDecimal;
import java.util.List;

/**
 * 구역 관리 API 컨트롤러.
 * 이 클래스는 구역 관리와 관련된 API 엔드포인트를 정의합니다.
 * 구역 등록, 조회, 수정, 삭제 기능을 제공합니다.
 */
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/area")
@Tag(name = "구역 관리 API", description = "구역 관리 API 모음")
public class AreaController {
    private final ApiResponseManager apiResponseManager;
    private final AreaService areaService;

    /**
     * 구역 등록 API.
     * 새로운 구역 정보를 등록합니다.
     *
     * @param createArea 구역 등록 요청 데이터
     * @return 등록된 구역 정보
     */
    @Operation(summary = "구역 등록", description = "구역 정보를 등록하는 API")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @PostMapping
    public ResponseEntity<ApiResponseDto> create(@Valid @RequestBody AreaDto.CreateArea createArea) {
        return apiResponseManager.success(areaService.create(createArea));
    }

    /**
     * 조건에 맞는 구역 조회 API.
     * 여러 조건을 기반으로 구역을 조회합니다.
     *
     * @param areaId 구역 ID 리스트
     * @param factoryId 공장 ID 리스트
     * @param areaName 구역명
     * @param areaLocation 주소
     * @param areaMinLatitude 최소 위도
     * @param areaMaxLatitude 최대 위도
     * @param areaMinLongitude 최소 경도
     * @param areaMaxLongitude 최대 경도
     * @param page 페이지 번호
     * @param size 페이지 당 데이터 개수
     * @return 조건에 맞는 구역 정보
     */
    @Operation(summary = "조건에 맞는 구역 조회", description = "조건에 맞는 구역을 조회하는 API")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @GetMapping
    public ResponseEntity<ApiResponseDto> read(
            @Parameter(description = "구역 ID") @RequestParam(required = false) List<Long> areaId,
            @Parameter(description = "공장 ID") @RequestParam(required = false) List<Long> factoryId,
            @Parameter(description = "구역명") @RequestParam(required = false) String areaName,
            @Parameter(description = "주소") @RequestParam(required = false) String areaLocation,
            @Parameter(description = "최소 위도") @RequestParam(required = false) BigDecimal areaMinLatitude,
            @Parameter(description = "최대 위도") @RequestParam(required = false) BigDecimal areaMaxLatitude,
            @Parameter(description = "최소 경도") @RequestParam(required = false) BigDecimal areaMinLongitude,
            @Parameter(description = "최대 경도") @RequestParam(required = false) BigDecimal areaMaxLongitude,
            @Parameter(description = "페이지 번호", example = "0") @RequestParam(required = false) Integer page,
            @Parameter(description = "페이지 당 데이터 개수", example = "10") @RequestParam(required = false) Integer size
    ) {
        return apiResponseManager.success(areaService.read(
                AreaDto.ReadAreaRequest.builder()
                        .areaIds(areaId)
                        .factoryIds(factoryId)
                        .areaName(areaName)
                        .areaLocation(areaLocation)
                        .areaMinLatitude(areaMinLatitude)
                        .areaMaxLatitude(areaMaxLatitude)
                        .areaMinLongitude(areaMinLongitude)
                        .areaMaxLongitude(areaMaxLongitude)
                        .page(page)
                        .size(size)
                        .build(),
                (page != null && size != null) ? PageRequest.of(page, size) : Pageable.unpaged()));
    }

    /**
     * 구역 정보 수정 API.
     * 기존 구역 정보를 수정합니다.
     *
     * @param areaId 구역 ID
     * @param updateArea 구역 수정 요청 데이터
     * @return 수정된 구역 정보
     */
    @Operation(summary = "구역 정보 수정", description = "구역 정보를 수정하는 API")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @PatchMapping("/{areaId}")
    public ResponseEntity<ApiResponseDto> update(
            @Parameter(description = "구역 ID", example = "1", required = true) @PathVariable() Long areaId,
            @Valid @RequestBody AreaDto.UpdateArea updateArea
    ) {
        return apiResponseManager.success(areaService.update(areaId, updateArea));
    }

    /**
     * 구역 삭제 API.
     * 지정된 ID의 구역을 삭제합니다.
     *
     * @param areaId 구역 ID
     * @return 삭제 결과
     */
    @Operation(summary = "구역 삭제", description = "구역을 삭제하는 API")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{areaId}")
    public ResponseEntity<ApiResponseDto> delete(
            @Parameter(description = "구역 ID", example = "1", required = true) @PathVariable() Long areaId
    ) {
        areaService.delete(areaId);
        return apiResponseManager.ok();
    }
}