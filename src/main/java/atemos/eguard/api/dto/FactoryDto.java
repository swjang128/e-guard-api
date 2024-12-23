package atemos.eguard.api.dto;

import atemos.eguard.api.domain.EmployeeRole;
import atemos.eguard.api.domain.IndustryType;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 공장과 관련된 데이터 전송 객체(DTO)를 정의하는 클래스입니다.
 */
public class FactoryDto {
    @Schema(description = "신규 공장 등록을 위한 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateFactory {
        @Schema(description = "업체 ID", example = "1")
        @Positive
        private Long companyId;

        @Schema(description = "공장명", example = "아테모스")
        @Size(min = 1, max = 50)
        private String factoryName;

        @Schema(description = "주소", example = "경기도 하남시 ****")
        private String factoryAddress;

        @Schema(description = "상세주소", example = "상세주소")
        private String factoryAddressDetail;

        @Schema(description = "업종")
        private IndustryType factoryIndustryType;

        @Schema(description = "공장이 차지하는 전체 면적(단위: ㎡)", example = "512.2568")
        private BigDecimal factoryTotalSize;

        @Schema(description = "공장의 건물이 차지하는 면적(단위: ㎡)", example = "365.1234")
        private BigDecimal factoryStructureSize;
    }

    @Schema(description = "공장 정보 수정 요청을 위한 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdateFactory {
        @Schema(description = "업체 ID", example = "1")
        @Positive
        private Long companyId;

        @Schema(description = "공장명", example = "아테모스")
        @Size(min = 1, max = 50)
        private String factoryName;

        @Schema(description = "주소", example = "경기도 하남시 ****")
        private String factoryAddress;

        @Schema(description = "상세주소", example = "상세주소")
        private String factoryAddressDetail;

        @Schema(description = "업종")
        private IndustryType factoryIndustryType;

        @Schema(description = "공장이 차지하는 전체 면적(단위: ㎡)", example = "512.2568")
        private BigDecimal factoryTotalSize;

        @Schema(description = "공장의 건물이 차지하는 면적(단위: ㎡)", example = "365.1234")
        private BigDecimal factoryStructureSize;
    }

    @Schema(description = "공장 조회 요청을 위한 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReadFactoryRequest {
        @Schema(description = "공장 ID 리스트", example = "[1,2,3]")
        private List<@Positive Long> factoryIds;

        @Schema(description = "업체 ID 리스트", example = "[1,2]")
        private List<@Positive Long> companyIds;

        @Schema(description = "공장명", example = "ATEMoS Factory")
        @Size(max = 50)
        private String factoryName;

        @Schema(description = "주소", example = "경기도 하남시 ****")
        @Size(max = 255)
        private String factoryAddress;

        @Schema(description = "상세주소", example = "상세주소")
        @Size(max = 255)
        private String factoryAddressDetail;

        @Schema(description = "업종")
        private List<IndustryType> factoryIndustryTypes;

        @Schema(description = "페이지 번호", example = "0")
        @PositiveOrZero
        private Integer page;

        @Schema(description = "페이지당 row의 개수", example = "10")
        @Positive
        private Integer size;
    }

    @Schema(description = "공장 조회 응답을 위한 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReadFactoryResponse {
        @Schema(description = "공장 ID")
        private Long factoryId;

        @Schema(description = "공장명")
        private String factoryName;

        @Schema(description = "공장 주소")
        private String factoryAddress;

        @Schema(description = "공장 상세주소")
        private String factoryAddressDetail;

        @Schema(description = "공장이 차지하는 전체 면적(단위: ㎡)")
        private BigDecimal factoryTotalSize;

        @Schema(description = "공장의 건물이 차지하는 면적(단위: ㎡)")
        private BigDecimal factoryStructureSize;

        @Schema(description = "업종")
        private IndustryType factoryIndustryType;

        @Schema(description = "업체 ID")
        private Long companyId;

        @Schema(description = "업체명")
        private String companyName;

        @Schema(description = "업체 연락처")
        private String companyPhoneNumber;

        @Schema(description = "업체 주소")
        private String companyAddress;

        @Schema(description = "공장 생성일")
        private LocalDateTime createdAt;

        @Schema(description = "공장 수정일")
        private LocalDateTime updatedAt;
    }

    @Schema(description = "공장 목록과 페이지 정보를 포함하는 응답 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReadFactoryResponseList {
        @Schema(description = "공장 목록")
        private List<FactoryDto.ReadFactoryResponse> factoryList;

        @Schema(description = "전체 row 수")
        private Long totalElements;

        @Schema(description = "전체 페이지 수")
        private Integer totalPages;
    }

    @Schema(description = "공장 요약 정보 요청을 위한 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FactorySummaryRequest {
        @Schema(description = "공장 ID")
        private Long factoryId;

        @Schema(description = "근로자의 권한")
        private EmployeeRole role;
    }

    @Schema(description = "공장 요약 정보 응답을 위한 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FactorySummaryResponse {
        @Schema(description = "공장 이름")
        private String factoryName;

        @Schema(description = "전체 근로자 수")
        @PositiveOrZero
        private Integer totalEmployees;

        @Schema(description = "부상인 근로자 수")
        @PositiveOrZero
        private Integer injuryEmployees;

        @Schema(description = "중대한 건강 이상인 근로자 수")
        @PositiveOrZero
        private Integer criticalHealthIssueEmployees;

        @Schema(description = "경미한 건강 이상인 근로자 수")
        @PositiveOrZero
        private Integer minorHealthIssueEmployees;

        @Schema(description = "휴가 중인 근로자 수")
        @PositiveOrZero
        private Integer onLeaveEmployees;

        @Schema(description = "정상 상태인 근로자 수")
        @PositiveOrZero
        private Integer normalEmployees;

        @Schema(description = "작업 미 배정 근로자 수")
        @PositiveOrZero
        private Integer unassignedEmployees;
    }
}