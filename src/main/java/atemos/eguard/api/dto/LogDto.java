package atemos.eguard.api.dto;

import atemos.eguard.api.domain.AllowedHttpMethod;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 로그와 관련된 데이터 전송 객체(DTO)를 정의하는 클래스입니다.
 */
public class LogDto {
    @Schema(description = "API 호출 로그 조회 요청 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReadApiCallLogRequest {
        @Schema(description = "로그 ID 리스트", example = "[1, 2, 3]")
        private List<@Positive Long> apiCallLogIds;

        @Schema(description = "업체 ID 리스트", example = "[1, 2]")
        private List<@Positive Long> companyIds;

        @Schema(description = "근로자 ID 리스트", example = "[1, 2]")
        private List<@Positive Long> employeeIds;

        @Schema(description = "API 호출 경로", example = "/eguard/log")
        private String requestUri;

        @Schema(description = "HTTP 메서드", example = "[GET, POST]")
        private List<AllowedHttpMethod> httpMethods;

        @Schema(description = "클라이언트 IP 주소", example = "192.168.0.1")
        @Pattern(regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$|^([0-9a-fA-F]{1,4}:){7}([0-9a-fA-F]{1,4}|:)$",
                message = "Invalid IP address format")
        private String clientIp;

        @Schema(description = "HTTP 상태 코드", example = "200")
        @Positive
        private Integer statusCode;

        @Schema(description = "조회 시작일시", example = "2023-10-16T10:00:00")
        private LocalDateTime searchStartTime;

        @Schema(description = "조회 종료일시", example = "2023-10-16T18:00:00")
        private LocalDateTime searchEndTime;

        @Schema(description = "페이지 번호", example = "0")
        @PositiveOrZero
        private Integer page;

        @Schema(description = "페이지당 row의 개수", example = "10")
        @Positive
        private Integer size;
    }

    @Schema(description = "조회된 API 호출 로그 응답 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReadApiCallLogResponse {
        @Schema(description = "로그 ID")
        private Long apiCallLogId;

        @Schema(description = "업체 ID")
        private Long companyId;

        @Schema(description = "근로자 ID")
        private Long employeeId;

        @Schema(description = "근로자 이메일")
        private String employeeEmail;

        @Schema(description = "업체 이름")
        private String companyName;

        @Schema(description = "API 경로")
        private String requestUri;

        @Schema(description = "HTTP 메서드")
        private AllowedHttpMethod httpMethod;

        @Schema(description = "클라이언트 IP 주소")
        private String clientIp;

        @Schema(description = "HTTP 상태 코드")
        private Integer statusCode;

        @Schema(description = "추가 메타데이터")
        private String metaData;

        @Schema(description = "요청 시각")
        private LocalDateTime requestTime;
    }

    @Schema(description = "API 호출 로그 목록 응답 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReadApiCallLogResponseList {
        @Schema(description = "API 호출 로그 목록")
        private List<ReadApiCallLogResponse> apiCallLogList;

        @Schema(description = "전체 row 개수")
        private Long apiCallLogTotalElements;

        @Schema(description = "전체 페이지 수")
        private Integer apiCallLogTotalPages;
    }

    @Schema(description = "인증 로그 조회 요청 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReadAuthenticationLogRequest {
        @Schema(description = "인증 로그 ID 리스트", example = "[1, 2, 3]")
        private List<@Positive Long> authenticationLogIds;

        @Schema(description = "업체 ID 리스트", example = "[1, 2]")
        private List<@Positive Long> companyIds;

        @Schema(description = "근로자 ID 리스트", example = "[1, 2]")
        private List<@Positive Long> employeeIds;

        @Schema(description = "API 호출 경로", example = "/eguard/auth")
        private String requestUri;

        @Schema(description = "HTTP 메서드", example = "[GET, POST]")
        private List<AllowedHttpMethod> httpMethods;

        @Schema(description = "클라이언트 IP 주소", example = "192.168.0.1")
        @Pattern(regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$|^([0-9a-fA-F]{1,4}:){7}([0-9a-fA-F]{1,4}|:)$",
                message = "Invalid IP address format")
        private String clientIp;

        @Schema(description = "HTTP 상태 코드", example = "200")
        @Positive
        private Integer statusCode;

        @Schema(description = "조회 시작일시", example = "2023-10-16T10:00:00")
        private LocalDateTime searchStartTime;

        @Schema(description = "조회 종료일시", example = "2023-10-16T18:00:00")
        private LocalDateTime searchEndTime;

        @Schema(description = "페이지 번호", example = "0")
        @PositiveOrZero
        private Integer page;

        @Schema(description = "페이지당 row의 개수", example = "10")
        @Positive
        private Integer size;
    }

    @Schema(description = "조회된 인증 로그 응답 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReadAuthenticationLogResponse {
        @Schema(description = "인증 로그 ID")
        private Long authenticationLogId;

        @Schema(description = "근로자 ID")
        private Long employeeId;

        @Schema(description = "근로자 이메일")
        private String employeeEmail;

        @Schema(description = "업체 ID")
        private Long companyId;

        @Schema(description = "업체 이름")
        private String companyName;

        @Schema(description = "API 경로")
        private String requestUri;

        @Schema(description = "HTTP 메서드")
        private AllowedHttpMethod httpMethod;

        @Schema(description = "클라이언트 IP 주소")
        private String clientIp;

        @Schema(description = "HTTP 상태 코드")
        private Integer statusCode;

        @Schema(description = "추가 메타데이터")
        private String metaData;

        @Schema(description = "요청 시각")
        private LocalDateTime requestTime;
    }

    @Schema(description = "인증 로그 목록 응답 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReadAuthenticationLogResponseList {
        @Schema(description = "인증 로그 목록")
        private List<ReadAuthenticationLogResponse> authenticationLogList;

        @Schema(description = "전체 row 개수")
        private Long authenticationLogTotalElements;

        @Schema(description = "전체 페이지 수")
        private Integer authenticationLogTotalPages;
    }
}