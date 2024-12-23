package atemos.eguard.api.dto;

import atemos.eguard.api.domain.AuthenticationStatus;
import atemos.eguard.api.domain.EmployeeIncident;
import atemos.eguard.api.domain.EmployeeRole;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 근로자 관련 데이터 전송 객체(DTO)를 정의한 클래스입니다.
 */
@Schema(description = "근로자 관련 데이터 전송 객체(DTO)")
@RequiredArgsConstructor
public class EmployeeDto {
    @Schema(description = "새로운 근로자 생성을 위한 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    public static class CreateEmployee {
        @Schema(description = "소속된 공장 ID", example = "1")
        @Positive
        private Long factoryId;

        @Schema(description = "이름", example = "아테모스")
        @Size(min = 1, max = 50)
        private String employeeName;

        @Schema(description = "이메일", example = "atemos@atemos.co.kr")
        @Pattern(regexp = "^[a-zA-Z0-9.%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Must be a valid email format.")
        private String employeeEmail;

        @Schema(description = "연락처", example = "01012349876")
        @Pattern(regexp = "^\\d{9,11}$", message = "Must be a valid 9 to 11 digit phone number.")
        private String employeePhoneNumber;

        @Schema(description = "비밀번호", example = "Atemos1234!", nullable = true)
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&?]).{8,32}$", message = "Password must be 8-16 characters long, and include letters, numbers, and special characters.")
        private String password;

        @Schema(description = "권한", example = "ADMIN")
        @Enumerated(EnumType.STRING)
        private EmployeeRole role;

        @Schema(description = "건강 상태", example = "NORMAL")
        @Enumerated(EnumType.STRING)
        private EmployeeIncident healthStatus;

        @Schema(description = "계정의 상태", example = "ACTIVE")
        @Enumerated(EnumType.STRING)
        private AuthenticationStatus authenticationStatus;

        @Schema(description = "사원번호", example = "COMPANY123")
        @Size(max = 40)
        private String employeeNumber;
    }

    @Schema(description = "기존 근로자 정보를 업데이트하기 위한 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    public static class UpdateEmployee {
        @Schema(description = "소속된 공장 ID", example = "1")
        @Positive
        private Long factoryId;

        @Schema(description = "이름", example = "아테모스")
        @Size(min = 1, max = 50)
        private String employeeName;

        @Schema(description = "이메일", example = "atemos@atemos.co.kr")
        @Pattern(regexp = "^[a-zA-Z0-9.%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Must be a valid email format.")
        private String employeeEmail;

        @Schema(description = "연락처", example = "01012349876")
        @Pattern(regexp = "^\\d{9,11}$", message = "Must be a valid 9 to 11 digit phone number.")
        private String employeePhoneNumber;

        @Schema(description = "비밀번호", example = "Atemos1234!")
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&?]).{8,32}$", message = "Password must be 8-16 characters long, and include letters, numbers, and special characters.")
        private String password;

        @Schema(description = "권한", example = "ADMIN")
        @Enumerated(EnumType.STRING)
        private EmployeeRole role;

        @Schema(description = "건강 상태", example = "NORMAL")
        @Enumerated(EnumType.STRING)
        private EmployeeIncident healthStatus;

        @Schema(description = "계정의 상태", example = "ACTIVE")
        @Enumerated(EnumType.STRING)
        private AuthenticationStatus authenticationStatus;

        @Schema(description = "사원번호", example = "COMPANY123")
        @Size(max = 40)
        private String employeeNumber;
    }

    @Schema(description = "근로자 조회 요청을 위한 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReadEmployeeRequest {
        @Schema(description = "근로자의 ID 리스트")
        private List<@Positive Long> employeeIds;

        @Schema(description = "소속된 공장 ID 리스트")
        private List<@Positive Long> factoryIds;

        @Schema(description = "이름(암호화 되어있으므로 Like 검색 안됨)")
        @Size(max = 30)
        private String employeeName;

        @Schema(description = "이메일(암호화 되어있으므로 Like 검색 안됨)")
        @Pattern(regexp = "^[a-zA-Z0-9.%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Must be a valid email format.")
        private String employeeEmail;

        @Schema(description = "연락처(암호화 되어있으므로 Like 검색 안됨)")
        @Pattern(regexp = "^\\d{9,11}$", message = "Must be a valid 9 to 11 digit phone number.")
        private String employeePhoneNumber;

        @Schema(description = "권한 리스트")
        private List<EmployeeRole> roles;

        @Schema(description = "건강 상태 리스트")
        private List<EmployeeIncident> healthStatuses;

        @Schema(description = "계정 상태 리스트")
        private List<AuthenticationStatus> authenticationStatuses;

        @Schema(description = "사원번호")
        @Size(max = 40)
        private String employeeNumber;

        @Schema(description = "페이지 번호")
        @PositiveOrZero
        private Integer page;

        @Schema(description = "페이지당 row 개수")
        @Positive
        private Integer size;

        @Schema(description = "이름, 연락처, 이메일 마스킹 여부")
        private Boolean masking;
    }

    @Schema(description = "근로자 조회 응답을 위한 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReadEmployeeResponse {
        @Schema(description = "근로자 ID")
        private Long employeeId;

        @Schema(description = "소속된 업체 ID")
        private Long companyId;

        @Schema(description = "소속된 업체명")
        private String companyName;
        
        @Schema(description = "소속된 업체의 주소")
        private String companyAddress;

        @Schema(description = "소속된 업체의 상세주소")
        private String companyAddressDetail;

        @Schema(description = "소속된 업체의 사업자 등록번호")
        private String companyBusinessNumber;
        
        @Schema(description = "근무 중인 공장 ID")
        private Long factoryId;

        @Schema(description = "근무 중인 공장명")
        private String factoryName;

        @Schema(description = "근무 중인 공장의 주소")
        private String factoryAddress;

        @Schema(description = "근무 중인 공장의 상세주소")
        private String factoryAddressDetail;

        @Schema(description = "이름")
        private String employeeName;

        @Schema(description = "이메일")
        private String employeeEmail;

        @Schema(description = "연락처")
        private String employeePhoneNumber;

        @Schema(description = "비밀번호를 틀린 횟수")
        private Integer failedLoginAttempts;

        @Schema(description = "권한")
        private EmployeeRole role;

        @Schema(description = "건강 상태")
        private EmployeeIncident healthStatus;

        @Schema(description = "계정 상태")
        private AuthenticationStatus authenticationStatus;

        @Schema(description = "사원번호")
        private String employeeNumber;

        @Schema(description = "접근 가능한 메뉴 ID 목록")
        private List<Long> accessibleMenuIds;

        @Schema(description = "투입된 작업의 ID")
        private Long workId;

        @Schema(description = "투입된 작업의 이름")
        private String workName;

        @Schema(description = "생성일")
        private LocalDateTime createdAt;

        @Schema(description = "수정일")
        private LocalDateTime updatedAt;
    }

    @Schema(description = "근로자 목록과 페이지 정보를 포함하는 응답 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReadEmployeeResponseList {
        @Schema(description = "근로자 목록")
        private List<EmployeeDto.ReadEmployeeResponse> employeeList;

        @Schema(description = "전체 Row 개수")
        private Long totalElements;

        @Schema(description = "전체 페이지 수")
        private Integer totalPages;
    }

    @Schema(description = "2차 인증 번호를 발송하기 위한 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AuthCodeRequest {
        @Schema(description = "로그인할 이메일", example = "admin@atemos.co.kr")
        @Pattern(regexp = "^[a-zA-Z0-9.%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Must be a valid email format.")
        private String employeeEmail;
    }

    @Schema(description = "로그인 요청을 위한 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class LoginRequest {
        @Schema(description = "로그인할 이메일", example = "admin@atemos.co.kr")
        @Pattern(regexp = "^[a-zA-Z0-9.%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Must be a valid email format.")
        private String employeeEmail;

        @Schema(description = "비밀번호", example = "Atemos1234!")
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&?]).{8,32}$", message = "Password must be 8-16 characters long, and include letters, numbers, and special characters.")
        private String password;

        @Schema(description = "2차 인증 번호")
        @Pattern(regexp = "^\\d{6}$", message = "The authentication code must be exactly 6 digits.")
        private String authCode;
    }

    @Schema(description = "로그인 응답을 위한 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class LoginResponse {
        @Schema(description = "Access Token")
        private String accessToken;

        @Schema(description = "Refresh Token")
        private String refreshToken;
    }

    @Schema(description = "비밀번호 초기화 요청을 위한 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ResetPassword {
        @Schema(description = "비밀번호를 초기화할 계정", example = "atemos@atemos.co.kr")
        @NotBlank
        @Pattern(regexp = "^[a-zA-Z0-9.%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Must be a valid email format.")
        private String employeeEmail;
    }

    @Schema(description = "비밀번호 변경 요청을 위한 DTO")
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdatePassword {
        @Schema(description = "비밀번호를 변경할 계정", example = "atemos@atemos.co.kr")
        @NotBlank
        @Pattern(regexp = "^[a-zA-Z0-9.%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Must be a valid email format.")
        private String employeeEmail;

        @Schema(description = "기존 비밀번호", example = "Atemos1234!")
        @NotBlank
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&?]).{8,32}$", message = "Password must be 8-16 characters long, and include letters, numbers, and special characters.")
        private String password;

        @Schema(description = "신규 비밀번호", example = "Atemos1234!@")
        @NotBlank
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&?]).{8,32}$", message = "Password must be 8-16 characters long, and include letters, numbers, and special characters.")
        private String newPassword;
    }
}