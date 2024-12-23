package atemos.eguard.api.dto;

import atemos.eguard.api.domain.AreaIncident;
import atemos.eguard.api.domain.EmployeeIncident;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 알람과 관련된 데이터 전송 객체(DTO)를 정의하는 클래스입니다.
 */
public class AlarmDto {
    @Schema(description = "알람을 생성할 때 필요한 정보를 담는 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CreateAlarm {
        @Schema(description = "알람을 수신할 근로자 ID", example = "1")
        @Positive
        private Long employeeId;

        @Schema(description = "알람이 발생하게된 사건 ID", example = "1")
        @Positive
        private Long eventId;
    }

    @Schema(description = "알람을 수정할 때 필요한 정보를 담는 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdateAlarm {
        @Schema(description = "알람을 수신할 근로자 ID", example = "1")
        @Positive
        private Long employeeId;

        @Schema(description = "알람이 발생하게된 사건 ID", example = "1")
        @Positive
        private Long eventId;

        @Schema(description = "알람 메시지", example = "알람 메시지가 수정되었습니다.")
        private String alarmMessage;

        @Schema(description = "읽음 여부", example = "false")
        private Boolean alarmRead;
    }

    @Schema(description = "알람 조회 요청을 위한 DTO")
    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReadAlarmRequest {
        @Schema(description = "알람 ID 목록")
        private List<@Positive Long> alarmIds;

        @Schema(description = "알람을 수신한 근로자 ID 목록")
        private List<@Positive Long> employeeIds;

        @Schema(description = "알람이 발생하게된 사건 ID 목록")
        private List<@Positive Long> eventIds;

        @Schema(description = "알람을 수신한 공장 ID 목록")
        private List<@Positive Long> factoryIds;

        @Schema(description = "알람을 수신한 업체 ID 목록")
        private List<@Positive Long> companyIds;

        @Schema(description = "알람 메시지")
        private String alarmMessage;

        @Schema(description = "알람 읽음 여부")
        private Boolean alarmRead;

        @Schema(description = "근로자에게 일어난 사건 유형")
        private List<EmployeeIncident> employeeIncidents;

        @Schema(description = "구역에서 일어난 사건 유형")
        private List<AreaIncident> areaIncidents;

        @Schema(description = "조회 시작일")
        private LocalDateTime searchStartTime;

        @Schema(description = "조회 종료일")
        private LocalDateTime searchEndTime;

        @Schema(description = "페이지 번호")
        @PositiveOrZero
        private Integer page;

        @Schema(description = "페이지당 데이터 개수")
        @Positive
        private Integer size;
    }

    @Schema(description = "알람 조회 결과를 담는 DTO")
    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReadAlarmResponse {
        @Schema(description = "알람 ID")
        private Long alarmId;

        @Schema(description = "알람을 수신한 근로자 ID")
        private Long employeeId;

        @Schema(description = "알람을 수신한 근로자 이름")
        private String employeeName;

        @Schema(description = "알람의 원인이 된 사건 ID")
        private Long eventId;

        @Schema(description = "근로자에게 발생한 사건")
        @Enumerated(EnumType.STRING)
        private EmployeeIncident employeeIncident;

        @Schema(description = "구역에서 발생한 사건")
        @Enumerated(EnumType.STRING)
        private AreaIncident areaIncident;

        @Schema(description = "사건이 발생한 근로자 ID")
        private Long eventEmployeeId;

        @Schema(description = "사건이 발생한 근로자 이름")
        private String eventEmployeeName;

        @Schema(description = "사건이 발생한 구역 ID")
        private Long eventAreaId;

        @Schema(description = "사건이 발생한 구역 이름")
        private String eventAreaName;

        @Schema(description = "사건이 발생한 구역 위치")
        private String eventAreaLocation;

        @Schema(description = "사건 해결 여부")
        private Boolean eventResolved;

        @Schema(description = "알람 메시지")
        private String alarmMessage;

        @Schema(description = "알람 읽음 여부")
        private Boolean alarmRead;

        @Schema(description = "알람 생성일시")
        private LocalDateTime createdAt;

        @Schema(description = "알람 수정일시")
        private LocalDateTime updatedAt;
    }

    @Schema(description = "알람 목록과 페이지 정보를 포함하는 응답 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReadAlarmResponseList {
        @Schema(description = "알람 목록")
        private List<AlarmDto.ReadAlarmResponse> alarmList;

        @Schema(description = "전체 알람 수")
        private Long totalElements;

        @Schema(description = "전체 페이지 수")
        private Integer totalPages;
    }
}