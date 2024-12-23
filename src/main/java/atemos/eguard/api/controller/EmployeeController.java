package atemos.eguard.api.controller;

import atemos.eguard.api.config.ApiResponseManager;
import atemos.eguard.api.domain.AuthenticationStatus;
import atemos.eguard.api.domain.EmployeeIncident;
import atemos.eguard.api.domain.EmployeeRole;
import atemos.eguard.api.dto.ApiResponseDto;
import atemos.eguard.api.dto.EmployeeDto;
import atemos.eguard.api.service.EmployeeService;
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
 * 근로자 관리 API 컨트롤러.
 * 이 클래스는 근로자 등록, 조회, 수정 및 삭제를 위한 API를 제공합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/employee")
@Tag(name = "근로자 관리 API", description = "근로자 관리 API 모음")
public class EmployeeController {
    private final ApiResponseManager apiResponseManager;
    private final EmployeeService employeeService;

    /**
     * 근로자 등록 API.
     * 새 근로자를 등록합니다.
     *
     * @param createEmployee 등록할 근로자 정보
     * @return 등록된 근로자 정보
     */
    @Operation(summary = "근로자 등록", description = "근로자 정보를 등록하는 API")
    @PostMapping
    public ResponseEntity<ApiResponseDto> create(
            @Valid @RequestBody EmployeeDto.CreateEmployee createEmployee
    ) {
        return apiResponseManager.success(employeeService.create(createEmployee));
    }

    /**
     * 조건에 맞는 근로자 목록 조회 API.
     * 주어진 조건에 맞는 근로자 목록을 조회합니다.
     *
     * @param employeeId 근로자 ID
     * @param factoryId 공장 ID
     * @param employeeName 이름(암호화 되어있으므로 Like 검색 안됨)
     * @param employeeEmail 이메일(암호화 되어있으므로 Like 검색 안됨)
     * @param employeePhoneNumber 연락처(암호화 되어있으므로 Like 검색 안됨)
     * @param employeeNumber 사원번호(Like 검색 가능)
     * @param healthStatus 근로자의 건강 상태
     * @param authenticationStatus 계정 상태
     * @param roles 권한
     * @param page 페이지 번호
     * @param size 페이지 당 데이터 개수
     * @param masking 마스킹 여부
     * @return 조건에 맞는 근로자 목록
     */
    @Operation(summary = "조건에 맞는 근로자 목록 조회", description = "조건에 맞는 근로자 목록을 조회하는 API")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<ApiResponseDto> read(
            @Parameter(description = "근로자 ID") @RequestParam(required = false) List<Long> employeeId,
            @Parameter(description = "공장 ID") @RequestParam(required = false) List<Long> factoryId,
            @Parameter(description = "이름(암호화 되어있으므로 Like 검색 안됨)") @RequestParam(required = false) String employeeName,
            @Parameter(description = "이메일(암호화 되어있으므로 Like 검색 안됨)") @RequestParam(required = false) String employeeEmail,
            @Parameter(description = "연락처(암호화 되어있으므로 Like 검색 안됨)") @RequestParam(required = false) String employeePhoneNumber,
            @Parameter(description = "사원번호(Like 검색 가능)") @RequestParam(required = false) String employeeNumber,
            @Parameter(description = "건강 상태") @RequestParam(required = false) List<EmployeeIncident> healthStatus,
            @Parameter(description = "계정 상태") @RequestParam(required = false) List<AuthenticationStatus> authenticationStatus,
            @Parameter(description = "권한") @RequestParam(required = false) List<EmployeeRole> roles,
            @Parameter(description = "페이지 번호", example = "0") @RequestParam(required = false) Integer page,
            @Parameter(description = "페이지 당 데이터 개수", example = "10") @RequestParam(required = false) Integer size,
            @Parameter(description = "마스킹 여부", example = "false") @RequestParam(required = false, defaultValue = "true") Boolean masking
    ) {
        // 조건에 맞는 근로자 목록 및 페이징 객체 리턴
        return apiResponseManager.success(employeeService.read(
                EmployeeDto.ReadEmployeeRequest.builder()
                        .employeeIds(employeeId)
                        .factoryIds(factoryId)
                        .employeeName(employeeName)
                        .employeeEmail(employeeEmail)
                        .employeePhoneNumber(employeePhoneNumber)
                        .employeeNumber(employeeNumber)
                        .healthStatuses(healthStatus)
                        .authenticationStatuses(authenticationStatus)
                        .roles(roles)
                        .page(page)
                        .size(size)
                        .masking(masking)
                        .build(),
                (page != null && size != null) ? PageRequest.of(page, size) : Pageable.unpaged()));
    }

    /**
     * 근로자 정보 수정 API.
     * 특정 근로자의 정보를 수정합니다. (관리자 권한 필요)
     *
     * @param employeeId 근로자 ID
     * @param updateEmployee 수정할 근로자 정보
     * @return 수정된 근로자 정보
     */
    @Operation(summary = "근로자 정보 수정", description = "근로자 정보를 수정하는 API")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{employeeId}")
    public ResponseEntity<ApiResponseDto> update(
            @Parameter(description = "근로자 ID", example = "1", required = true) @PathVariable Long employeeId,
            @RequestBody EmployeeDto.UpdateEmployee updateEmployee
    ) {
        return apiResponseManager.success(employeeService.update(employeeId, updateEmployee));
    }

    /**
     * 근로자 삭제 API.
     * 특정 근로자를 삭제합니다.
     *
     * @param employeeId 근로자 ID
     * @return 삭제 완료 응답
     */
    @Operation(summary = "근로자 삭제", description = "근로자를 삭제하는 API")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{employeeId}")
    public ResponseEntity<ApiResponseDto> delete(
            @Parameter(description = "근로자 ID", example = "1", required = true) @PathVariable Long employeeId
    ) {
        employeeService.delete(employeeId);
        return apiResponseManager.ok();
    }
}