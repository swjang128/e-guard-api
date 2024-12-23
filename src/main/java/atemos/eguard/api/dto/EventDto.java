package atemos.eguard.api.dto;

import atemos.eguard.api.domain.AreaIncident;
import atemos.eguard.api.domain.EmployeeIncident;
import atemos.eguard.api.domain.IncidentPriority;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 사건와 관련된 데이터 전송 객체(DTO)들을 정의한 클래스입니다.
 */
@Schema(description = "사건 관련 데이터 전송 객체(DTO)")
public class EventDto {
    @Schema(description = "신규 사건 등록을 위한 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CreateEvent {
        @Schema(description = "근로자 ID", example = "1")
        @Positive
        private Long employeeId;

        @Schema(description = "구역 ID", example = "1")
        @Positive
        private Long areaId;

        @Schema(description = "근로자에게 발생한 사건", example = "INJURY")
        @Enumerated(EnumType.STRING)
        private EmployeeIncident employeeIncident;

        @Schema(description = "구역에서 발생한 사건", example = "FIRE")
        @Enumerated(EnumType.STRING)
        private AreaIncident areaIncident;

        @Schema(description = "사건 해결 여부", defaultValue = "false")
        private Boolean eventResolved;
    }

    @Schema(description = "사건 정보 수정 요청을 위한 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UpdateEvent {
        @Schema(description = "근로자 ID", example = "1")
        @Positive
        private Long employeeId;

        @Schema(description = "구역 ID", example = "1")
        @Positive
        private Long areaId;

        @Schema(description = "근로자에게 발생한 사건", example = "INJURY")
        @Enumerated(EnumType.STRING)
        private EmployeeIncident employeeIncident;

        @Schema(description = "구역에서 발생한 사건", example = "FIRE")
        @Enumerated(EnumType.STRING)
        private AreaIncident areaIncident;

        @Schema(description = "사건 해결 여부", defaultValue = "false")
        private Boolean eventResolved;
    }

    @Schema(description = "사건 조회 요청을 위한 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReadEventRequest {
        @Schema(description = "사건 ID 리스트", example = "[1, 2, 3]")
        private List<@Positive Long> eventIds;

        @Schema(description = "근로자 ID 리스트", example = "[1, 2]")
        private List<@Positive Long> employeeIds;

        @Schema(description = "구역 ID 리스트", example = "[1, 2]")
        private List<@Positive Long> areaIds;

        @Schema(description = "공장 ID 리스트", example = "[1, 2]")
        private List<@Positive Long> factoryIds;

        @Schema(description = "근로자에게 발생한 사건 유형 리스트", example = "[INJURY, CRITICAL_HEALTH_ISSUE]")
        private List<EmployeeIncident> employeeIncidents;

        @Schema(description = "구역에서 발생한 사건 유형 리스트", example = "[FIRE, ACCIDENT]")
        private List<AreaIncident> areaIncidents;

        @Schema(description = "사건 해결 여부", example = "false")
        private Boolean eventResolved;

        @Schema(description = "조회 시작일", example = "2023-10-01")
        private LocalDate searchStartDate;

        @Schema(description = "조회 종료일", example = "2023-10-31")
        private LocalDate searchEndDate;

        @Schema(description = "페이지 번호", example = "0")
        @PositiveOrZero
        private Integer page;

        @Schema(description = "페이지당 row의 개수", example = "10")
        @Positive
        private Integer size;
    }

    @Schema(description = "사건 조회 응답을 위한 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReadEventResponse {
        @Schema(description = "사건 ID")
        private Long eventId;

        @Schema(description = "근로자 ID")
        private Long employeeId;

        @Schema(description = "근로자 이름")
        private String employeeName;

        @Schema(description = "근로자 번호")
        private String employeeNumber;

        @Schema(description = "구역 ID")
        private Long areaId;

        @Schema(description = "구역명")
        private String areaName;

        @Schema(description = "구역 위치")
        private String areaLocation;

        @Schema(description = "공장 ID")
        private Long factoryId;

        @Schema(description = "공장명")
        private String factoryName;

        @Schema(description = "공장 주소")
        private String factoryAddress;

        @Schema(description = "근로자에게 발생한 사건")
        @Enumerated(EnumType.STRING)
        private EmployeeIncident employeeIncident;

        @Schema(description = "구역에서 발생한 사건")
        @Enumerated(EnumType.STRING)
        private AreaIncident areaIncident;

        @Schema(description = "사건 발생 명칭")
        private String incidentName;

        @Schema(description = "사건의 심각도")
        @Enumerated(EnumType.STRING)
        private IncidentPriority incidentPriority;

        @Schema(description = "사건 메시지")
        private String incidentMessage;

        @Schema(description = "사건 해결 여부")
        private Boolean eventResolved;

        @Schema(description = "사건 생성일")
        private LocalDateTime createdAt;

        @Schema(description = "사건 수정일")
        private LocalDateTime updatedAt;
    }

    @Schema(description = "사건 목록과 페이지 정보를 포함하는 응답 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReadEventResponseList {
        @Schema(description = "사건 목록")
        private List<EventDto.ReadEventResponse> eventList;

        @Schema(description = "전체 row 수")
        private Long totalElements;

        @Schema(description = "전체 페이지 수")
        private Integer totalPages;
    }

    @Schema(description = "안전 점수 응답을 위한 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SafetyScore {
        @Schema(description = "공장에 대한 안전 점수")
        private FactorySafetyScore factorySafetyScore;

        @Schema(description = "안전 등급 리스트")
        private List<SafetyGrade> safetyGrades;
    }

    @Schema(description = "공장별 안전 점수를 위한 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FactorySafetyScore {
        @Schema(description = "공장 ID")
        private Long factoryId;

        @Schema(description = "공장 이름")
        private String factoryName;

        @Schema(description = "공장 주소")
        private String factoryAddress;

        @Schema(description = "공장별 안전 점수 (0~100 사이의 숫자)")
        private Integer safetyScore;

        @Schema(description = "공장별 안전 등급 ('심각', '주의', '양호')")
        private String safetyGrade;

        @Schema(description = "이번 달 공장의 CRITICAL 사건 수")
        private Long criticalIncidentCount;

        @Schema(description = "이번 달 공장의 ALERT 사건 수")
        private Long alertIncidentCount;

        @Schema(description = "이번 달 공장의 WARNING 사건 수")
        private Long warningIncidentCount;

        @Schema(description = "공장 내 구역들의 안전 점수 리스트")
        private List<AreaSafetyScore> areaSafetyScores;
    }

    @Schema(description = "구역별 안전 점수를 위한 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AreaSafetyScore {
        @Schema(description = "구역 ID")
        private Long areaId;

        @Schema(description = "구역 이름")
        private String areaName;

        @Schema(description = "구역 위치")
        private String areaLocation;

        @Schema(description = "구역별 안전 점수 (0~100 사이의 숫자)")
        private Integer safetyScore;

        @Schema(description = "구역별 안전 등급 ('심각', '주의', '양호')")
        private String safetyGrade;

        @Schema(description = "이번 달 구역의 CRITICAL 사건 수")
        private Long criticalIncidentCount;

        @Schema(description = "이번 달 구역의 ALERT 사건 수")
        private Long alertIncidentCount;

        @Schema(description = "이번 달 구역의 WARNING 사건 수")
        private Long warningIncidentCount;
    }

    @Schema(description = "안전 등급 산출 응답 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SafetyGrade {
        @Schema(description = "등급")
        private String grade;

        @Schema(description = "등급에 대한 메시지")
        private String message;

        @Schema(description = "등급 기준 점수 최소치")
        private Integer min;

        @Schema(description = "등급 기준 점수 최대치")
        private Integer max;
    }
}