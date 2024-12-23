package atemos.eguard.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
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
 * 업체와 관련된 데이터 전송 객체(DTO)들을 정의한 클래스입니다.
 */
public class CompanyDto {
    @Schema(description = "신규 업체 등록을 위한 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateCompany {
        @Schema(description = "업체의 사업자 등록번호", example = "1234567890", minLength = 1, maxLength = 10)
        @Size(min = 1, max = 10)
        private String companyBusinessNumber;

        @Schema(description = "업체명", example = "아테모스", minLength = 1, maxLength = 50)
        @Size(min = 1, max = 50)
        private String companyName;

        @Schema(description = "이메일", example = "atemos@atemos.co.kr")
        @Pattern(regexp = "^[a-zA-Z0-9.%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Must be a valid email format.")
        private String companyEmail;

        @Schema(description = "연락처", example = "01098765432", minLength = 8, maxLength = 11)
        @Pattern(regexp = "^\\d{9,11}$", message = "Must be a valid 9 to 11 digit phone number.")
        private String companyPhoneNumber;

        @Schema(description = "주소", example = "경기도 하남시 ****")
        private String companyAddress;

        @Schema(description = "상세주소", example = "상세주소")
        private String companyAddressDetail;
    }

    @Schema(description = "업체 정보 수정 요청을 위한 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdateCompany {
        @Schema(description = "업체의 사업자 등록번호", example = "1234567890", minLength = 1, maxLength = 10)
        @Size(min = 1, max = 10)
        private String companyBusinessNumber;

        @Schema(description = "업체명", example = "아테모스", minLength = 1, maxLength = 50)
        @Size(min = 1, max = 50)
        private String companyName;

        @Schema(description = "이메일", example = "atemos@atemos.co.kr")
        @Pattern(regexp = "^[a-zA-Z0-9.%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Must be a valid email format.")
        private String companyEmail;

        @Schema(description = "연락처", example = "01097532468", minLength = 8, maxLength = 11)
        @Pattern(regexp = "^\\d{9,11}$", message = "Must be a valid 9 to 11 digit phone number.")
        private String companyPhoneNumber;

        @Schema(description = "주소", example = "경기도 성남시 ****")
        private String companyAddress;

        @Schema(description = "상세주소", example = "상세주소")
        private String companyAddressDetail;
    }

    @Schema(description = "업체 조회 요청을 위한 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReadCompanyRequest {
        @Schema(description = "업체 ID 목록")
        @Positive
        private List<Long> companyIds;

        @Schema(description = "사업자 등록번호", maxLength = 10)
        @Pattern(regexp = "^\\d{10}$", message = "사업자 등록번호는 숫자 10자리여야 합니다.")
        private String companyBusinessNumber;

        @Schema(description = "이메일", example = "atemos@atemos.co.kr", maxLength = 50)
        @Pattern(regexp = "^[a-zA-Z0-9.%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Must be a valid email format.")
        private String companyEmail;

        @Schema(description = "업체명", example = "아테모스", maxLength = 50)
        @Size(max = 50)
        private String companyName;

        @Schema(description = "연락처", example = "01098765432")
        @Pattern(regexp = "\\d{11}", message = "Must be a valid 11-digit phone number.")
        private String companyPhoneNumber;

        @Schema(description = "주소", maxLength = 255)
        @Size(max = 255)
        private String companyAddress;

        @Schema(description = "페이지 번호", example = "0")
        @PositiveOrZero
        private Integer page;

        @Schema(description = "페이지당 row 개수", example = "10")
        @Positive
        private Integer size;
    }

    @Schema(description = "업체 조회 응답을 위한 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReadCompanyResponse {
        @Schema(description = "업체 ID")
        private Long companyId;

        @Schema(description = "사업자 등록번호")
        private String companyBusinessNumber;

        @Schema(description = "업체명")
        private String companyName;

        @Schema(description = "이메일")
        private String companyEmail;

        @Schema(description = "연락처")
        private String companyPhoneNumber;

        @Schema(description = "주소")
        private String companyAddress;

        @Schema(description = "상세주소")
        private String companyAddressDetail;

        @Schema(description = "업체 생성일")
        private LocalDateTime createdAt;

        @Schema(description = "업체 수정일")
        private LocalDateTime updatedAt;
    }

    @Schema(description = "업체 목록과 페이지 정보를 포함하는 응답 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReadCompanyResponseList {
        @Schema(description = "업체 목록")
        private List<CompanyDto.ReadCompanyResponse> companyList;

        @Schema(description = "전체 row 수")
        private Long totalElements;

        @Schema(description = "전체 페이지 수")
        private Integer totalPages;
    }
}