package atemos.eguard.api.dto;

import atemos.eguard.api.domain.AreaIncident;
import atemos.eguard.api.domain.WorkStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 작업과 관련된 데이터 전송 객체(DTO)들을 정의한 클래스입니다.
 */
public class WorkDto {
    @Schema(description = "신규 작업 등록을 위한 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateWork {
        @Schema(description = "구역 ID", defaultValue = "1")
        @Positive
        private Long areaId;

        @Schema(description = "작업명", defaultValue = "자동차 외장 도색 작업")
        @Size(min = 1, max = 100)
        private String workName;

        @Schema(description = "투입할 근로자 ID 목록", defaultValue = "[1,2,3]")
        private List<@Positive Long> employeeIds;

        @Schema(description = "상태", defaultValue = "PENDING")
        private WorkStatus workStatus;
    }

    @Schema(description = "작업 정보 수정을 위한 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdateWork {
        @Schema(description = "구역 ID", defaultValue = "1")
        @Positive
        private Long areaId;

        @Schema(description = "작업명", defaultValue = "자동차 내장 부품 조립 작업")
        @Size(min = 1, max = 100)
        private String workName;

        @Schema(description = "투입할 근로자 ID 목록", defaultValue = "[1,2]")
        private List<@Positive Long> employeeIds;

        @Schema(description = "상태", defaultValue = "IN_PROGRESS")
        private WorkStatus workStatus;
    }

    @Schema(description = "작업 조회 요청을 위한 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReadWorkRequest {
        @Schema(description = "조회할 작업 ID 목록")
        private List<@Positive Long> workIds;

        @Schema(description = "작업이 이루어지는 구역이 속한 공장 ID 목록")
        private List<@Positive Long> factoryIds;

        @Schema(description = "작업이 이루어지는 구역 ID 목록")
        private List<@Positive Long> areaIds;

        @Schema(description = "작업에 투입된 근로자 ID 목록")
        private List<@Positive Long> employeeIds;

        @Schema(description = "작업명")
        @Size(max = 100)
        private String workName;

        @Schema(description = "상태 목록")
        private List<WorkStatus> workStatuses;

        @Schema(description = "페이지 번호")
        @PositiveOrZero
        private Integer page;

        @Schema(description = "페이지당 데이터 개수")
        @Positive
        private Integer size;
    }

    @Schema(description = "작업 조회 응답을 위한 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReadWorkResponse {
        @Schema(description = "작업 ID")
        private Long workId;

        @Schema(description = "작업을 배정한 구역 ID")
        private Long areaId;

        @Schema(description = "작업을 배정한 구역 이름")
        private String areaName;

        @Schema(description = "작업을 배정한 구역에서 발생한 사건")
        private AreaIncident areaIncident;

        @Schema(description = "투입된 근로자 목록")
        private List<EmployeeDto.ReadEmployeeResponse> employees;

        @Schema(description = "작업명")
        private String workName;

        @Schema(description = "상태")
        private WorkStatus workStatus;

        @Schema(description = "생성일")
        private LocalDateTime createdAt;

        @Schema(description = "수정일")
        private LocalDateTime updatedAt;
    }

    @Schema(description = "작업 목록과 페이지 정보를 포함하는 응답 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReadWorkResponseList {
        @Schema(description = "작업 목록")
        private List<WorkDto.ReadWorkResponse> workList;

        @Schema(description = "전체 row 수")
        private Long totalElements;

        @Schema(description = "전체 페이지 수")
        private Integer totalPages;
    }

    @Schema(description = "근로자와 그들이 참여 중인 작업에 대한 정보를 포함하는 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EmployeeWorkStatus {
        @Schema(description = "근로자 ID")
        private Long employeeId;

        @Schema(description = "근로자가 참여 중인 작업 ID")
        private Long workId;

        @Schema(description = "근로자가 참여 중인 작업명")
        private String workName;

        @Schema(description = "근로자가 참여 중인 작업 상태")
        private WorkStatus workStatus;
    }
}