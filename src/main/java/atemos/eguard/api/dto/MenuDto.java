package atemos.eguard.api.dto;

import atemos.eguard.api.domain.EmployeeRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 메뉴 관련 데이터 전송 객체(DTO)를 정의한 클래스입니다.
 */
public class MenuDto {
    @Schema(description = "새로운 메뉴 생성을 위한 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateMenu {
        @Schema(description = "이름", example = "샘플")
        @Size(min = 1, max = 50)
        private String menuName;

        @Schema(description = "URL", example = "/sample")
        @Size(max = 30)
        private String menuUrl;

        @Schema(description = "설명", example = "샘플 화면입니다")
        @Size(min = 1, max = 255)
        private String menuDescription;

        @Schema(description = "메뉴 사용 여부", example = "true")
        private Boolean menuAvailable;

        @Schema(description = "접근 가능한 역할 목록", example = "[\"ADMIN\", \"MANAGER\"]")
        private List<EmployeeRole> accessibleRoles;

        @Schema(description = "상위 메뉴 ID")
        @Positive
        private Long parentId;
    }

    @Schema(description = "메뉴 수정을 위한 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdateMenu {
        @Schema(description = "이름", example = "샘플")
        @Size(min = 1, max = 50)
        private String menuName;

        @Schema(description = "URL", example = "/sample")
        @Size(max = 30)
        private String menuUrl;

        @Schema(description = "설명", example = "샘플 화면입니다")
        @Size(min = 1, max = 255)
        private String menuDescription;

        @Schema(description = "사용 여부", example = "true")
        private Boolean menuAvailable;

        @Schema(description = "접근 가능한 역할 목록", example = "[\"ADMIN\", \"MANAGER\"]")
        private List<EmployeeRole> accessibleRoles;

        @Schema(description = "상위 메뉴 ID")
        @Positive
        private Long parentId;
    }

    @Schema(description = "메뉴 조회 요청을 위한 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReadMenuRequest {
        @Schema(description = "메뉴 ID 리스트", example = "[1, 2, 3]")
        private List<@Positive Long> menuIds;

        @Schema(description = "이름", example = "sample")
        @Size(max = 50)
        private String menuName;

        @Schema(description = "URL", example = "/sample")
        @Size(max = 30)
        private String menuUrl;

        @Schema(description = "설명", example = "Sample Page로 이동")
        @Size(max = 255)
        private String menuDescription;

        @Schema(description = "사용 여부", example = "true 또는 false")
        private Boolean menuAvailable;

        @Schema(description = "상위 메뉴 ID", example = "[1]")
        private List<@Positive Long> parentIds;

        @Schema(description = "접근 가능한 역할 목록", example = "[\"ADMIN\", \"MANAGER\"]")
        private List<EmployeeRole> accessibleRoles;
    }

    @Schema(description = "메뉴 조회 응답을 위한 DTO")
    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReadMenuResponse {
        @Schema(description = "메뉴 ID")
        private Long menuId;

        @Schema(description = "이름")
        private String menuName;

        @Schema(description = "URL")
        private String menuUrl;

        @Schema(description = "설명")
        private String menuDescription;

        @Schema(description = "사용 여부")
        private Boolean menuAvailable;

        @Schema(description = "데이터 생성일")
        private LocalDateTime createdAt;

        @Schema(description = "데이터 수정일")
        private LocalDateTime updatedAt;

        @Schema(description = "상위 메뉴 ID")
        private Long parentId;

        @Schema(description = "하위 메뉴 목록")
        private List<ReadMenuResponse> children;

        @Schema(description = "깊이(Depth)")
        private Integer menuDepth;

        @Schema(description = "접근 가능한 역할 목록")
        private List<EmployeeRole> accessibleRoles;
    }
}