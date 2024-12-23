package atemos.eguard.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * API 응답을 표준화하기 위한 데이터 전송 객체(DTO)입니다.
 */
@Builder
@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseDto {
    @Schema(description = "응답 상태 코드")
    private Integer status;

    @Schema(description = "응답 메시지")
    private String message;

    @Schema(description = "응답 데이터")
    private Object data;
}
