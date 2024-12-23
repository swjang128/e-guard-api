package atemos.eguard.api.controller;

import atemos.eguard.api.config.ApiResponseManager;
import atemos.eguard.api.domain.EmployeeRole;
import atemos.eguard.api.domain.IndustryType;
import atemos.eguard.api.dto.ApiResponseDto;
import atemos.eguard.api.dto.FactoryDto;
import atemos.eguard.api.service.FactoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 공장 관리 API 컨트롤러.
 * 이 클래스는 공장 관리와 관련된 API 엔드포인트를 정의합니다.
 * 공장 등록, 조회, 수정, 삭제 기능을 제공합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/factory")
@Tag(name = "공장 관리 API", description = "공장 관리 API 모음")
public class FactoryController {
    private final ApiResponseManager apiResponseManager;
    private final FactoryService factoryService;

    /**
     * 공장 등록 API.
     * 새로운 공장 정보를 등록합니다.
     *
     * @param createFactory 공장 등록 요청 데이터
     * @return 등록된 공장 정보
     */
    @Operation(summary = "공장 등록", description = "공장 정보를 등록하는 API")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @PostMapping
    public ResponseEntity<ApiResponseDto> create(@Valid @RequestBody FactoryDto.CreateFactory createFactory) {
        return apiResponseManager.success(factoryService.create(createFactory));
    }

    /**
     * 조건에 맞는 공장 조회 API.
     * 여러 조건을 기반으로 공장을 조회합니다.
     *
     * @param companyId 업체 ID 리스트
     * @param factoryId 공장 ID 리스트
     * @param factoryName 공장명
     * @param factoryAddress 주소
     * @param factoryIndustryType 공장의 업종
     * @param page 페이지 번호
     * @param size 페이지 당 데이터 개수
     * @return 조건에 맞는 공장 정보
     */
    @Operation(summary = "조건에 맞는 공장 조회", description = "조건에 맞는 공장을 조회하는 API")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @GetMapping
    public ResponseEntity<ApiResponseDto> read(
            @Parameter(description = "업체 ID") @RequestParam(required = false) List<@Positive Long> companyId,
            @Parameter(description = "공장 ID") @RequestParam(required = false) List<@Positive Long> factoryId,
            @Parameter(description = "공장명") @RequestParam(required = false) String factoryName,
            @Parameter(description = "주소") @RequestParam(required = false) String factoryAddress,
            @Parameter(description = "공장의 업종") @RequestParam(required = false) List<IndustryType> factoryIndustryType,
            @Parameter(description = "페이지 번호", example = "0") @RequestParam(required = false) Integer page,
            @Parameter(description = "페이지 당 데이터 개수", example = "10") @RequestParam(required = false) Integer size
    ) {
        return apiResponseManager.success(factoryService.read(
                FactoryDto.ReadFactoryRequest.builder()
                        .companyIds(companyId)
                        .factoryIds(factoryId)
                        .factoryName(factoryName)
                        .factoryAddress(factoryAddress)
                        .factoryIndustryTypes(factoryIndustryType)
                        .page(page)
                        .size(size)
                        .build(),
                (page != null && size != null) ? PageRequest.of(page, size) : Pageable.unpaged()));
    }

    /**
     * 공장 정보 수정 API.
     * 기존 공장 정보를 수정합니다.
     *
     * @param factoryId 공장 ID
     * @param updateFactory 공장 수정 요청 데이터
     * @return 수정된 공장 정보
     */
    @Operation(summary = "공장 정보 수정", description = "공장 정보를 수정하는 API")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @PatchMapping("/{factoryId}")
    public ResponseEntity<ApiResponseDto> update(
            @Parameter(description = "공장 ID", example = "1", required = true) @PathVariable() Long factoryId,
            @Valid @RequestBody FactoryDto.UpdateFactory updateFactory
    ) {
        return apiResponseManager.success(factoryService.update(factoryId, updateFactory));
    }

    /**
     * 공장 삭제 API.
     * 지정된 ID의 공장을 삭제합니다.
     *
     * @param factoryId 공장 ID
     * @return 삭제 결과
     */
    @Operation(summary = "공장 삭제", description = "공장을 삭제하는 API")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{factoryId}")
    public ResponseEntity<ApiResponseDto> delete(
            @Parameter(description = "공장 ID", example = "1", required = true) @PathVariable() Long factoryId
    ) {
        factoryService.delete(factoryId);
        return apiResponseManager.ok();
    }

    /**
     * 현재 로그인한 근로자의 공장 정보 조회
     * JWT 토큰을 이용하여 현재 로그인한 근로자의 공장 정보를 조회합니다.
     *
     * @return 근로자 정보
     */
    @Operation(summary = "현재 로그인한 근로자의 공장 정보 조회", description = "JWT 토큰을 이용하여 현재 로그인한 근로자의 공장 정보를 조회하는 API")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/info")
    public ResponseEntity<ApiResponseDto> readFactoryInfo() {
        return apiResponseManager.success(factoryService.readFactoryInfo());
    }

    /**
     * 회원 가입 화면에서 노출되는 공장 목록 조회 API.
     * 회원 가입 화면에 노출되는 공장 목록을 조회합니다.
     * 업체 ID를 파라미터로 받아서 업체가 보유한 공장 목록만 표시합니다.
     *
     * @param companyIds 업체 ID 리스트
     * @return 공장 목록
     */
    @Operation(summary = "회원 가입 화면에서 노출되는 공장 목록 조회", description = "회원 가입 화면에서 노출되는 공장 목록 조회 API")
    @GetMapping("/list")
    public ResponseEntity<ApiResponseDto> readSignUpFactoryList(
            @Parameter(description = "업체 ID", example = "1") @RequestParam(required = false) List<@Positive Long> companyIds
    ) {
        return apiResponseManager.success(factoryService.readSignUpFactoryList(
                FactoryDto.ReadFactoryRequest.builder()
                        .companyIds(companyIds)
                        .build()));
    }

    /**
     * 특정 공장의 요약 정보를 조회하는 API.
     * 공장 ID를 기반으로 근로자의 전체 수, 건강 상태별 인원 수, 휴가 중인 인원 수를 포함한
     * 요약 정보를 반환합니다.
     *
     * @param factoryId 공장 ID
     * @param role 근로자의 권한
     * @return 공장의 요약 정보
     */
    @Operation(summary = "공장 요약 정보 조회", description = "공장 ID를 기반으로 근로자 상태 요약 정보를 조회하는 API")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @GetMapping("/summary/{factoryId}")
    public ResponseEntity<ApiResponseDto> readFactorySummary(
            @Parameter(description = "공장 ID", example = "1", required = true) @PathVariable() Long factoryId,
            @Parameter(description = "권한", example = "WORKER") @RequestParam(required = false) EmployeeRole role
    ) {
        return apiResponseManager.success(factoryService.getFactorySummary(
                FactoryDto.FactorySummaryRequest.builder()
                        .factoryId(factoryId)
                        .role(role)
                        .build()));
    }
}