package atemos.eguard.api.controller;

import atemos.eguard.api.config.ApiResponseManager;
import atemos.eguard.api.config.NoLogging;
import atemos.eguard.api.domain.AllowedHttpMethod;
import atemos.eguard.api.dto.ApiResponseDto;
import atemos.eguard.api.dto.LogDto;
import atemos.eguard.api.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Log API 컨트롤러.
 * 이 클래스는 Log와 관련된 API 엔드포인트를 정의합니다.
 * Log의 조회, 유료 API 호출 횟수 조회, 삭제 기능을 제공합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/log")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Log API", description = "Log API 모음")
public class LogController {
    private final ApiResponseManager apiResponseManager;
    private final LogService logService;

    /**
     * API 호출 로그 정보를 조회하는 메서드.
     * 다양한 조건에 따라 API 호출 로그를 조회할 수 있습니다.
     *
     * @param apiCallLogId Log ID 리스트
     * @param companyId 업체 ID 리스트
     * @param employeeId 근로자 ID 리스트
     * @param requestUri 엔드포인트
     * @param httpMethod Http 메서드
     * @param clientIp 요청 IP
     * @param statusCode Http 상태 코드
     * @param searchStartTime 조회 시작일시
     * @param searchEndTime 조회 종료일시
     * @param page 페이지 번호
     * @param size 페이지 당 데이터 개수
     * @return 조회된 Log 리스트
     */
    @Operation(summary = "API 호출 로그 조회", description = "API 호출 로그 정보를 조회하는 API")
    @PreAuthorize("hasRole('ADMIN')")
    @NoLogging
    @GetMapping("/api")
    public ResponseEntity<ApiResponseDto> readApiCallLog(
            @Parameter(description = "API 호출 로그 ID 리스트") @RequestParam(required = false) List<@Positive Long> apiCallLogId,
            @Parameter(description = "업체 ID 리스트", example = "1") @RequestParam(required = false) List<@Positive Long> companyId,
            @Parameter(description = "근로자 ID 리스트", example = "1") @RequestParam(required = false) List<@Positive Long> employeeId,
            @Parameter(description = "Http Method") @RequestParam(required = false) List<AllowedHttpMethod> httpMethod,
            @Parameter(description = "엔드포인트") @RequestParam(required = false) String requestUri,
            @Parameter(description = "요청 IP") @RequestParam(required = false) String clientIp,
            @Parameter(description = "Http Status Code") @RequestParam(required = false) Integer statusCode,
            @Parameter(description = "조회 시작일시", example = "2024-06-03T00:00:00") @RequestParam(required = false) LocalDateTime searchStartTime,
            @Parameter(description = "조회 종료일시", example = "2025-12-31T23:59:59") @RequestParam(required = false) LocalDateTime searchEndTime,
            @Parameter(description = "페이지 번호", example = "0") @RequestParam(required = false) Integer page,
            @Parameter(description = "페이지 당 데이터 개수", example = "10") @RequestParam(required = false) Integer size
    ) {
        return apiResponseManager.success(logService.readApiCallLog(
                LogDto.ReadApiCallLogRequest.builder()
                        .apiCallLogIds(apiCallLogId)
                        .companyIds(companyId)
                        .employeeIds(employeeId)
                        .httpMethods(httpMethod)
                        .requestUri(requestUri)
                        .clientIp(clientIp)
                        .statusCode(statusCode)
                        .searchStartTime(searchStartTime)
                        .searchEndTime(searchEndTime)
                        .page(page)
                        .size(size)
                        .build(),
                (page != null && size != null) ? PageRequest.of(page, size) : Pageable.unpaged()));
    }

    /**
     * 인증/인가 로그 정보를 조회하는 메서드.
     * 다양한 조건에 따라 인증 로그를 조회할 수 있습니다.
     *
     * @param authenticationLogId 인증/인가 로그 ID 리스트
     * @param companyId 업체 ID 리스트
     * @param employeeId 근로자 ID 리스트
     * @param requestUri 엔드포인트
     * @param httpMethod Http 메서드
     * @param clientIp 요청 IP
     * @param statusCode Http 상태 코드
     * @param searchStartTime 조회 시작일시
     * @param searchEndTime 조회 종료일시
     * @param page 페이지 번호
     * @param size 페이지 당 데이터 개수
     * @return 조회된 Log 리스트
     */
    @Operation(summary = "인증/인가 로그 조회", description = "인증/인가 로그 정보를 조회하는 API")
    @PreAuthorize("hasRole('ADMIN')")
    @NoLogging
    @GetMapping("/auth")
    public ResponseEntity<ApiResponseDto> readAuthenticationLog(
            @Parameter(description = "인증/인가 로그 ID 리스트") @RequestParam(required = false) List<@Positive Long> authenticationLogId,
            @Parameter(description = "업체 ID 리스트", example = "1") @RequestParam(required = false) List<@Positive Long> companyId,
            @Parameter(description = "근로자 ID 리스트", example = "1") @RequestParam(required = false) List<@Positive Long> employeeId,
            @Parameter(description = "Http Method") @RequestParam(required = false) List<AllowedHttpMethod> httpMethod,
            @Parameter(description = "엔드포인트") @RequestParam(required = false) String requestUri,
            @Parameter(description = "요청 IP") @RequestParam(required = false) String clientIp,
            @Parameter(description = "Http Status Code") @RequestParam(required = false) Integer statusCode,
            @Parameter(description = "조회 시작일시", example = "2024-06-03T00:00:00") @RequestParam(required = false) LocalDateTime searchStartTime,
            @Parameter(description = "조회 종료일시", example = "2025-12-31T23:59:59") @RequestParam(required = false) LocalDateTime searchEndTime,
            @Parameter(description = "페이지 번호", example = "0") @RequestParam(required = false) Integer page,
            @Parameter(description = "페이지 당 데이터 개수", example = "10") @RequestParam(required = false) Integer size
    ) {
        return apiResponseManager.success(logService.readAuthenticationLog(
                LogDto.ReadAuthenticationLogRequest.builder()
                        .authenticationLogIds(authenticationLogId)
                        .companyIds(companyId)
                        .employeeIds(employeeId)
                        .httpMethods(httpMethod)
                        .requestUri(requestUri)
                        .clientIp(clientIp)
                        .statusCode(statusCode)
                        .searchStartTime(searchStartTime)
                        .searchEndTime(searchEndTime)
                        .page(page)
                        .size(size)
                        .build(),
                (page != null && size != null) ? PageRequest.of(page, size) : Pageable.unpaged()));
    }
}