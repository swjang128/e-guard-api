package atemos.eguard.api.dto;

import atemos.eguard.api.domain.AreaIncident;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 구역와 관련된 데이터 전송 객체(DTO)들을 정의한 클래스입니다.
 */
public class AreaDto {
    @Schema(description = "신규 구역 등록을 위한 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateArea {
        @Schema(description = "공장 ID", example = "1")
        @Positive
        private Long factoryId;

        @Schema(description = "구역명", example = "오염수 처리 장치")
        @Size(min = 1, max = 50)
        private String areaName;

        @Schema(description = "위치", example = "ATEMoS 공장 오염수 처리 장치 구역")
        private String areaLocation;

        @Schema(description = "구역의 면적(단위: ㎡)", example = "32.9876")
        private BigDecimal areaUsableSize;

        @Schema(description = "위도", example = "65.193282")
        @DecimalMin(value = "-90.000000", message = "위도는 -90.0 이상이어야 합니다.")
        @DecimalMax(value = "90.000000", message = "위도는 90.0 이하이어야 합니다.")
        private BigDecimal areaLatitude;

        @Schema(description = "경도", example = "124.103829")
        @DecimalMin(value = "-180.000000", message = "경도는 -180.0 이상이어야 합니다.")
        @DecimalMax(value = "180.000000", message = "경도는 180.0 이하이어야 합니다.")
        private BigDecimal areaLongitude;

        @Schema(description = "구역의 2D 도면 파일 경로", example = "example_area_2d_file.png")
        private String areaPlan2DFilePath;

        @Schema(description = "구역의 3D 도면 파일 경로", example = "example_area_3d_file.png")
        private String areaPlan3DFilePath;

        @Schema(description = "메모", example = "메모")
        private String areaMemo;
    }

    @Schema(description = "구역 정보 수정 요청을 위한 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdateArea {
        @Schema(description = "공장 ID", example = "1")
        @Positive
        private Long factoryId;

        @Schema(description = "구역명", example = "오염수 처리 장치")
        @Size(min = 1, max = 50)
        private String areaName;

        @Schema(description = "위치", example = "ATEMoS 공장 오염수 처리 장치 구역")
        private String areaLocation;

        @Schema(description = "구역의 면적(단위: ㎡)", example = "32.9876")
        private BigDecimal areaUsableSize;

        @Schema(description = "위도", example = "88.193282")
        @DecimalMin(value = "-90.000000", message = "위도는 -90.0 이상이어야 합니다.")
        @DecimalMax(value = "90.000000", message = "위도는 90.0 이하이어야 합니다.")
        private BigDecimal areaLatitude;

        @Schema(description = "경도", example = "179.103829")
        @DecimalMin(value = "-180.000000", message = "경도는 -180.0 이상이어야 합니다.")
        @DecimalMax(value = "180.000000", message = "경도는 180.0 이하이어야 합니다.")
        private BigDecimal areaLongitude;

        @Schema(description = "구역의 2D 도면 파일 경로", example = "example_area_2d_file.png")
        private String areaPlan2DFilePath;

        @Schema(description = "구역의 3D 도면 파일 경로", example = "example_area_3d_file.png")
        private String areaPlan3DFilePath;

        @Schema(description = "메모", example = "메모")
        private String areaMemo;
    }

    @Schema(description = "구역 조회 요청을 위한 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReadAreaRequest {
        @Schema(description = "구역 ID 리스트", example = "[1, 2, 3]")
        private List<@Positive Long> areaIds;

        @Schema(description = "공장 ID 리스트", example = "[1, 2]")
        private List<@Positive Long> factoryIds;

        @Schema(description = "구역명", example = "ATEMoS Area")
        @Size(max = 50)
        private String areaName;

        @Schema(description = "위치", example = "경기도 하남시 ****")
        @Size(max = 255)
        private String areaLocation;

        @Schema(description = "검색 최소 위도", example = "76.111111")
        @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다.")
        @DecimalMax(value = "90.0", message = "위도는 90 이하이어야 합니다.")
        private BigDecimal areaMinLatitude;

        @Schema(description = "검색 최대 위도", example = "12.999999")
        @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다.")
        @DecimalMax(value = "90.0", message = "위도는 90 이하이어야 합니다.")
        private BigDecimal areaMaxLatitude;

        @Schema(description = "검색 최소 경도", example = "111.111111")
        @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다.")
        @DecimalMax(value = "180.0", message = "경도는 180 이하이어야 합니다.")
        private BigDecimal areaMinLongitude;

        @Schema(description = "검색 최대 경도", example = "179.999999")
        @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다.")
        @DecimalMax(value = "180.0", message = "경도는 180 이하이어야 합니다.")
        private BigDecimal areaMaxLongitude;

        @Schema(description = "페이지 번호", example = "0")
        @PositiveOrZero
        private Integer page;

        @Schema(description = "페이지당 row 개수", example = "10")
        @Positive
        private Integer size;
    }

    @Schema(description = "구역 조회 응답을 위한 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReadAreaResponse {
        @Schema(description = "구역 ID")
        private Long areaId;

        @Schema(description = "구역명")
        private String areaName;

        @Schema(description = "위치")
        private String areaLocation;

        @Schema(description = "구역의 면적(단위: ㎡)")
        private BigDecimal areaUsableSize;

        @Schema(description = "위도")
        private BigDecimal areaLatitude;

        @Schema(description = "경도")
        private BigDecimal areaLongitude;

        @Schema(description = "구역의 2D 도면 파일 경로")
        private String areaPlan2DFilePath;

        @Schema(description = "구역의 3D 도면 파일 경로")
        private String areaPlan3DFilePath;

        @Schema(description = "메모")
        private String areaMemo;

        @Schema(description = "구역에서 발생 중인 사건")
        private AreaIncident areaIncident;

        @Schema(description = "공장 ID")
        private Long factoryId;

        @Schema(description = "공장명")
        private String factoryName;

        @Schema(description = "발생 중인 사건")
        private String eventName;

        @Schema(description = "구역 생성일")
        private LocalDateTime createdAt;

        @Schema(description = "구역 수정일")
        private LocalDateTime updatedAt;
    }

    @Schema(description = "구역 목록과 페이지 정보를 포함하는 응답 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReadAreaResponseList {
        @Schema(description = "구역 목록")
        private List<AreaDto.ReadAreaResponse> areaList;

        @Schema(description = "전체 row 수")
        private Long totalElements;

        @Schema(description = "전체 페이지 수")
        private Integer totalPages;
    }
}