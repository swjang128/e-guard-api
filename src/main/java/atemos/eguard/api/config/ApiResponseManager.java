package atemos.eguard.api.config;

import atemos.eguard.api.dto.ApiResponseDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * ApiResponseManager는 API 호출의 응답을 관리하는 클래스입니다.
 * 각 메서드는 다양한 상황에 맞는 응답을 생성하고, 로그를 기록합니다.
 */
@Slf4j
@Component
@AllArgsConstructor
public class ApiResponseManager {
    private final LogComponent logComponent;

    /**
     * API를 정상 호출하였고, 리턴할 데이터가 없는 경우의 응답을 생성합니다.
     *
     * @return 상태 코드와 메시지를 포함한 응답 객체
     */
    public ResponseEntity<ApiResponseDto> ok() {
        logResponse(HttpStatus.OK);
        return buildResponse(HttpStatus.OK, HttpStatus.OK.getReasonPhrase(), null);
    }

    /**
     * API를 정상 호출하였고, 리턴할 데이터가 있는 경우의 응답을 생성합니다.
     *
     * @param data 리턴할 데이터
     * @return 상태 코드와 메시지, 데이터를 포함한 응답 객체
     */
    public ResponseEntity<ApiResponseDto> success(Object data) {
        logResponse(HttpStatus.OK);
        return buildResponse(HttpStatus.OK, HttpStatus.OK.getReasonPhrase(), data);
    }

    /**
     * 인증/인가 API를 정상 호출하였고, 근로자 데이터를 함께 받습니다.
     *
     * @param data 근로자 데이터
     * @return 상태 코드와 메시지, 추가 정보를 포함한 응답 객체
     */
    public ResponseEntity<ApiResponseDto> login(Object data, String requestUri) {
        logAuthentication(requestUri, data);
        return buildResponse(HttpStatus.OK, HttpStatus.OK.getReasonPhrase(), data);
    }

    /**
     * API 호출 후 기능 작동 중 에러가 발생한 경우의 응답을 생성합니다.
     *
     * @param status HTTP 상태 코드
     * @param message 에러 메시지
     * @return 상태 코드와 메시지를 포함한 응답 객체
     */
    public ResponseEntity<ApiResponseDto> error(HttpStatus status, String message) {
        logResponse(status);
        return buildResponse(status, message, null);
    }

    /**
     * 공통 로깅 로직을 처리하는 메서드.
     *
     * @param status HTTP 상태 코드
     */
    private void logResponse(HttpStatus status) {
        logComponent.logRequest(status.value());
    }

    /**
     * 인증/인가 로깅 로직을 처리하는 메서드.
     *
     * @param requestUri 인증 경로
     * @param data 근로자 데이터
     */
    private void logAuthentication(String requestUri, Object data) {
        logComponent.saveAuthenticationLog(HttpStatus.OK.value(), requestUri.replaceFirst("/eguard", ""), data);
    }

    /**
     * 공통 응답 빌드를 처리하는 메서드.
     *
     * @param status HTTP 상태 코드
     * @param data   응답에 포함할 데이터
     * @return 상태 코드와 메시지를 포함한 응답 객체
     */
    private ResponseEntity<ApiResponseDto> buildResponse(HttpStatus status, String message, Object data) {
        return ResponseEntity.status(status)
                .body(ApiResponseDto.builder()
                        .status(status.value())
                        .message(message)
                        .data(data)
                        .build());
    }
}