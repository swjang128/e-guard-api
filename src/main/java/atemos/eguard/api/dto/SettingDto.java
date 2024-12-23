package atemos.eguard.api.dto;

import atemos.eguard.api.domain.TwoFactoryAuthenticationMethod;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 시스템 설정과 관련된 데이터 전송 객체(DTO)를 정의하는 클래스입니다.
 */
public class SettingDto {
    @Schema(description = "시스템 설정 수정 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdateSetting {
        @Schema(description = "업체 ID", example = "1")
        @Positive
        private Long companyId;

        @Schema(description = "한 업체가 등록 가능한 최대 공장 수", example = "100")
        @Positive
        private Integer maxFactoriesPerCompany;

        @Schema(description = "한 공장에 등록 가능한 최대 구역 수", example = "100")
        @Positive
        private Integer maxAreasPerFactory;

        @Schema(description = "한 공장에 등록 가능한 최대 근로자 수", example = "1000")
        private Integer maxEmployeesPerFactory;

        @Schema(description = "한 구역에 등록 가능한 최대 작업 수", example = "100")
        @Positive
        private Integer maxWorksPerArea;

        @Schema(description = "한 작업에 등록 가능한 최대 근로자 수", example = "100")
        @Positive
        private Integer maxEmployeesPerWork;

        @Schema(description = "2차 인증 여부 (true: 사용, false: 미사용)", example = "true")
        private Boolean twoFactorAuthenticationEnabled;

        @Schema(description = "2차 인증 방법 (이메일, 문자, 알림톡 등)", example = "EMAIL")
        private TwoFactoryAuthenticationMethod twoFactorAuthenticationMethod;
    }

    @Schema(description = "시스템 설정 조회 요청 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReadSettingRequest {
        @Schema(description = "시스템 설정 ID 리스트", example = "[1,2]")
        @Positive
        private List<Long> settingIds;

        @Schema(description = "시스템 설정을 보유한 업체 ID 리스트", example = "[1,2]")
        @Positive
        private List<Long> companyIds;

        @Schema(description = "근로자의 로그인 이메일", example = "manager@atemos.co.kr")
        private String employeeEmail;

        @Schema(description = "2차 인증 여부 (true: 사용, false: 미사용)", example = "true")
        private Boolean twoFactorAuthenticationEnabled;

        @Schema(description = "2차 인증 방법 (이메일, 문자, 알림톡 등)", example = "[EMAIL,KAKAO]")
        private List<TwoFactoryAuthenticationMethod> twoFactorAuthenticationMethods;
    }

    @Schema(description = "시스템 설정 응답 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReadSettingResponse {
        @Schema(description = "시스템 설정 ID")
        private Long settingId;

        @Schema(description = "시스템 설정을 사용 중인 업체 ID")
        private Long companyId;

        @Schema(description = "시스템 설정을 사용 중인 업체명")
        private String companyName;

        @Schema(description = "한 업체가 등록 가능한 최대 공장 수")
        private Integer maxFactoriesPerCompany;

        @Schema(description = "한 공장에 등록 가능한 최대 구역 수")
        private Integer maxAreasPerFactory;

        @Schema(description = "한 공장에 등록 가능한 최대 근로자 수")
        private Integer maxEmployeesPerFactory;

        @Schema(description = "한 구역에 등록 가능한 최대 작업 수")
        private Integer maxWorksPerArea;

        @Schema(description = "한 작업에 등록 가능한 최대 근로자 수")
        private Integer maxEmployeesPerWork;

        @Schema(description = "2차 인증 여부 (true: 사용, false: 미사용)")
        private Boolean twoFactorAuthenticationEnabled;

        @Schema(description = "2차 인증 방법 (이메일, 문자, 알림톡 등)")
        private TwoFactoryAuthenticationMethod twoFactorAuthenticationMethod;

        @Schema(description = "설정 생성 날짜 및 시간")
        private LocalDateTime createdAt;

        @Schema(description = "설정 마지막 수정 날짜 및 시간")
        private LocalDateTime updatedAt;
    }

    @Schema(description = "시스템 설정 목록과 페이지 정보를 포함하는 응답 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReadSettingResponseList {
        @Schema(description = "시스템 설정 목록")
        private List<SettingDto.ReadSettingResponse> settingList;

        @Schema(description = "전체 시스템 설정 수")
        private Long totalElements;

        @Schema(description = "전체 페이지 수")
        private Integer totalPages;
    }
}