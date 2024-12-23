package atemos.eguard.api.controller;

import atemos.eguard.api.config.ApiResponseManager;
import atemos.eguard.api.domain.TwoFactoryAuthenticationMethod;
import atemos.eguard.api.dto.ApiResponseDto;
import atemos.eguard.api.dto.SettingDto;
import atemos.eguard.api.service.SettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * SettingController는
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/setting")
@Tag(name = "시스템 설정 API", description = "시스템 설정 API 모음")
public class SettingController {
    private final ApiResponseManager apiResponseManager;
    private final SettingService settingService;

    /**
     * 특정 업체의 시스템 설정 정보를 조회합니다.
     *
     * @param settingId 시스템 설정 ID 리스트
     * @param companyId 업체 ID 리스트
     * @param employeeEmail 근로자의 로그인 이메일
     * @param twoFactorAuthenticationEnabled 2차 인증 여부
     * @param twoFactorAuthenticationMethod 2차 인증 방법
     */
    @Operation(summary = "특정 업체의 시스템 설정을 조회.", description = "특정 업체의 시스템 설정을 조회하는 API.")
    @GetMapping
    public ResponseEntity<ApiResponseDto> read(
            @Parameter(description = "시스템 설정 ID 리스트") @RequestParam(required = false) List<Long> settingId,
            @Parameter(description = "시스템 설정을 보유한 업체 ID 리스트") @RequestParam(required = false) List<Long> companyId,
            @Parameter(description = "근로자의 로그인 이메일") @RequestParam(required = false) String employeeEmail,
            @Parameter(description = "2차 인증 여부") @RequestParam(required = false) Boolean twoFactorAuthenticationEnabled,
            @Parameter(description = "2차 인증 방법") @RequestParam(required = false) List<TwoFactoryAuthenticationMethod> twoFactorAuthenticationMethod
    ) {
        return apiResponseManager.success(settingService.read(SettingDto.ReadSettingRequest.builder()
                        .settingIds(settingId)
                        .companyIds(companyId)
                        .employeeEmail(employeeEmail)
                        .twoFactorAuthenticationEnabled(twoFactorAuthenticationEnabled)
                        .twoFactorAuthenticationMethods(twoFactorAuthenticationMethod)
                        .build()));
    }

    /**
     * 시스템 설정 정보를 수정합니다.
     *
     * @param settingId 시스템 설정 ID
     *
     */
    @Operation(summary = "시스템 설정을 수정.", description = "시스템 설정을 수정하는 API.")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{settingId}")
    public ResponseEntity<ApiResponseDto> update(
            @PathVariable Long settingId,
            @Valid @RequestBody SettingDto.UpdateSetting updateSettingDto
    ) {
        return apiResponseManager.success(settingService.update(settingId, updateSettingDto));
    }
}