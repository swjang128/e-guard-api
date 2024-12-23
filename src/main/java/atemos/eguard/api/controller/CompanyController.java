package atemos.eguard.api.controller;

import atemos.eguard.api.config.ApiResponseManager;
import atemos.eguard.api.dto.ApiResponseDto;
import atemos.eguard.api.dto.CompanyDto;
import atemos.eguard.api.service.CompanyService;
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
 * 업체 관리 API 컨트롤러.
 * 이 클래스는 업체 관리와 관련된 API 엔드포인트를 정의합니다.
 * 업체 등록, 조회, 수정, 삭제 기능을 제공합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/company")
@Tag(name = "업체 관리 API", description = "업체 관리 API 모음")
public class CompanyController {
    private final ApiResponseManager apiResponseManager;
    private final CompanyService companyService;

    /**
     * 업체 등록 API.
     * 새로운 업체 정보를 등록합니다.
     *
     * @param createCompany 업체 등록 요청 데이터
     * @return 등록된 업체 정보
     */
    @Operation(summary = "업체 등록", description = "업체 정보를 등록하는 API")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<ApiResponseDto> create(@Valid @RequestBody CompanyDto.CreateCompany createCompany) {
        return apiResponseManager.success(companyService.create(createCompany));
    }

    /**
     * 조건에 맞는 업체 조회 API.
     * 여러 조건을 기반으로 업체를 조회합니다.
     *
     * @param companyId 업체 ID 리스트
     * @param companyBusinessNumber 사업자 등록번호
     * @param companyName 업체명
     * @param companyPhoneNumber 연락처
     * @param companyAddress 주소
     * @param page 페이지 번호
     * @param size 페이지 당 데이터 개수
     * @return 조건에 맞는 업체 정보
     */
    @Operation(summary = "조건에 맞는 업체 조회", description = "조건에 맞는 업체를 조회하는 API")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<ApiResponseDto> read(
            @Parameter(description = "업체 ID") @RequestParam(required = false) List<Long> companyId,
            @Parameter(description = "사업자 등록번호") @RequestParam(required = false) String companyBusinessNumber,
            @Parameter(description = "업체명") @RequestParam(required = false) String companyName,
            @Parameter(description = "연락처") @RequestParam(required = false) String companyPhoneNumber,
            @Parameter(description = "주소") @RequestParam(required = false) String companyAddress,
            @Parameter(description = "페이지 번호", example = "0") @RequestParam(required = false) Integer page,
            @Parameter(description = "페이지 당 데이터 개수", example = "10") @RequestParam(required = false) Integer size
    ) {
        return apiResponseManager.success(companyService.read(
                CompanyDto.ReadCompanyRequest.builder()
                        .companyIds(companyId)
                        .companyBusinessNumber(companyBusinessNumber)
                        .companyName(companyName)
                        .companyPhoneNumber(companyPhoneNumber)
                        .companyAddress(companyAddress)
                        .page(page)
                        .size(size)
                        .build(),
                (page != null && size != null) ? PageRequest.of(page, size) : Pageable.unpaged()));
    }

    /**
     * 업체 정보 수정 API.
     * 기존 업체 정보를 수정합니다.
     *
     * @param companyId 업체 ID
     * @param updateCompany 업체 수정 요청 데이터
     * @return 수정된 업체 정보
     */
    @Operation(summary = "업체 정보 수정", description = "업체 정보를 수정하는 API")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{companyId}")
    public ResponseEntity<ApiResponseDto> update(
            @Parameter(description = "업체 ID", example = "1", required = true) @PathVariable() Long companyId,
            @Valid @RequestBody CompanyDto.UpdateCompany updateCompany
    ) {
        return apiResponseManager.success(companyService.update(companyId, updateCompany));
    }

    /**
     * 업체 삭제 API.
     * 지정된 ID의 업체를 삭제합니다.
     *
     * @param companyId 업체 ID
     * @return 삭제 결과
     */
    @Operation(summary = "업체 삭제", description = "업체를 삭제하는 API")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{companyId}")
    public ResponseEntity<ApiResponseDto> delete(
            @Parameter(description = "업체 ID", example = "1", required = true) @PathVariable() Long companyId
    ) {
        companyService.delete(companyId);
        return apiResponseManager.ok();
    }

    /**
     * 현재 로그인한 근로자의 업체 정보 조회
     * JWT 토큰을 이용하여 현재 로그인한 근로자의 업체 정보를 조회합니다.
     *
     * @return 근로자 정보
     */
    @Operation(summary = "현재 로그인한 근로자의 업체 정보 조회", description = "JWT 토큰을 이용하여 현재 로그인한 근로자의 업체 정보를 조회하는 API")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/info")
    public ResponseEntity<ApiResponseDto> readCompanyInfo() {
        return apiResponseManager.success(companyService.readCompanyInfo());
    }

    /**
     * 회원 가입 화면에서 노출되는 업체 목록 조회 API.
     * 회원 가입 화면에 노출되는 업체 목록을 조회합니다.
     *
     * @return 업체 목록
     */
    @Operation(summary = "회원 가입 화면에서 노출되는 업체 목록 조회", description = "회원 가입 화면에서 노출되는 업체 목록 조회 API")
    @GetMapping("/list")
    public ResponseEntity<ApiResponseDto> readSignUpCompanyList() {
        return apiResponseManager.success(companyService.read(
                CompanyDto.ReadCompanyRequest.builder()
                        .build()
                ,Pageable.unpaged()));
    }
}