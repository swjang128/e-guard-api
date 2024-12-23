package atemos.eguard.api.controller;

import atemos.eguard.api.dto.AlarmDto;
import atemos.eguard.api.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * ReportController는 조회한 데이터를 엑셀로 다운로드하는 API를 제공하는 컨트롤러입니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/report")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "엑셀 리포트 API", description = "데이터를 엑셀로 변환하여 다운로드하는 API 모음")
public class ReportController {
    private final ReportService reportService;

    /**
     * 특정 업체의 특정 기간 내 알람 이력을 엑셀 파일로 다운로드하는 API.
     * 데이터는 시간별로 집계되어 엑셀 파일로 제공됩니다.
     *
     * @param companyId 업체 ID 리스트
     * @param searchStartDate 조회 시작일
     * @param searchEndDate   조회 종료일
     * @param response  HTTP 응답 객체 (엑셀 파일 전송에 사용)
     */
    @Operation(summary = "특정 업체의 특정 기간 내 알람 이력을 이력 엑셀 리포트 다운로드",
            description = "특정 업체의 특정 기간 내 알람 이력을 집계하여 엑셀 파일로 다운로드합니다.")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @GetMapping("/alarm")
    public void reportAlarm(
            @Parameter(description = "업체 ID", example = "1") @RequestParam(required = false) List<Long> companyId,
            @Parameter(description = "조회 시작 날짜", example = "2024-06-03") @RequestParam LocalDate searchStartDate,
            @Parameter(description = "조회 종료 날짜", example = "2025-12-31") @RequestParam(required = false) LocalDate searchEndDate,
            HttpServletResponse response
    ) {
        reportService.reportAlarm(AlarmDto.ReadAlarmRequest.builder()
                        .companyIds(companyId)
                        .searchStartTime(searchStartDate.atStartOfDay())
                        .searchEndTime(searchEndDate.atTime(23, 59, 59))
                        .build(),
                response);
    }
}