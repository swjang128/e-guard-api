package atemos.eguard.api.config;

import atemos.eguard.api.dto.ApiResponseDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.sun.jdi.request.DuplicateRequestException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.json.JSONException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.NotAcceptableStatusException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.webjars.NotFoundException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.TimeoutException;

/**
 * 예외가 발생했을 때 원인을 파악하고 응답하는 모듈.
 * 다양한 예외를 처리하고 적절한 HTTP 상태 코드와 메시지를 반환합니다.
 */
@RestControllerAdvice
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class ExceptionResponseHandler extends ResponseEntityExceptionHandler {
    private final ApiResponseManager apiResponseManager;

    /**
     * 잘못된 요청 예외 처리.
     * @param e 예외
     * @return BAD_REQUEST 상태 코드와 메시지를 담은 응답
     */
    @ExceptionHandler({
            ConstraintViolationException.class,
            BadRequestException.class,
            IllegalArgumentException.class})
    public ResponseEntity<ApiResponseDto> handleBadRequestException(Exception e) {
        return logAndRespond(HttpStatus.BAD_REQUEST, e);
    }

    /**
     * 인증되지 않은 예외 처리.
     * @param e 예외
     * @return UNAUTHORIZED 상태 코드와 메시지를 담은 응답
     */
    @ExceptionHandler({
            UsernameNotFoundException.class,
            BadCredentialsException.class})
    public ResponseEntity<ApiResponseDto> handleUnauthorizedException(Exception e) {
        return logAndRespond(HttpStatus.UNAUTHORIZED, e);
    }

    /**
     * 접근 금지 예외 처리.
     * @param e 예외
     * @return FORBIDDEN 상태 코드와 메시지를 담은 응답
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponseDto> handleForbiddenException(Exception e) {
        return logAndRespond(HttpStatus.FORBIDDEN, e);
    }

    /**
     * 리소스를 찾을 수 없는 예외 처리.
     * @param e 예외
     * @return NOT_FOUND 상태 코드와 메시지를 담은 응답
     */
    @ExceptionHandler({
            NotFoundException.class,
            EntityNotFoundException.class})
    public ResponseEntity<ApiResponseDto> handleNotFoundException(Exception e) {
        return logAndRespond(HttpStatus.NOT_FOUND, e);
    }

    /**
     * 허용되지 않은 HTTP 메서드 예외 처리.
     * @param e 예외
     * @return METHOD_NOT_ALLOWED 상태 코드와 메시지를 담은 응답
     */
    @ExceptionHandler(MethodNotAllowedException.class)
    public ResponseEntity<ApiResponseDto> handleMethodNotAllowedException(Exception e) {
        return logAndRespond(HttpStatus.METHOD_NOT_ALLOWED, e);
    }

    /**
     * 수락할 수 없는 요청 예외 처리.
     * @param e 예외
     * @return NOT_ACCEPTABLE 상태 코드와 메시지를 담은 응답
     */
    @ExceptionHandler(NotAcceptableStatusException.class)
    public ResponseEntity<ApiResponseDto> handleNotAcceptableException(Exception e) {
        return logAndRespond(HttpStatus.NOT_ACCEPTABLE, e);
    }

    /**
     * 요청 시간 초과 예외 처리.
     * @param e 예외
     * @return REQUEST_TIMEOUT 상태 코드와 메시지를 담은 응답
     */
    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<ApiResponseDto> handleRequestTimeoutException(Exception e) {
        return logAndRespond(HttpStatus.REQUEST_TIMEOUT, e);
    }

    /**
     * 충돌 예외 처리.
     * @param e 예외
     * @return CONFLICT 상태 코드와 메시지를 담은 응답
     */
    @ExceptionHandler(DuplicateRequestException.class)
    public ResponseEntity<ApiResponseDto> handleConflictException(Exception e) {
        return logAndRespond(HttpStatus.CONFLICT, e);
    }

    /**
     * `ResponseStatusException`을 처리하는 통합 메서드.
     * @param e 예외
     * @return 적절한 상태 코드와 메시지를 담은 응답
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponseDto> handleResponseStatusException(ResponseStatusException e) {
        log.error("handle ResponseStatusException: {}", e.getReason());
        return apiResponseManager.error((HttpStatus) e.getStatusCode(), e.getReason() != null ? e.getReason() : e.getMessage());
    }

    /**
     * 지원되지 않는 미디어 타입 예외 처리.
     * @param e 예외
     * @return UNSUPPORTED_MEDIA_TYPE 상태 코드와 메시지를 담은 응답
     */
    @ExceptionHandler(UnsupportedMediaTypeStatusException.class)
    public ResponseEntity<ApiResponseDto> handleUnsupportedMediaTypeException(Exception e) {
        return logAndRespond(HttpStatus.UNSUPPORTED_MEDIA_TYPE, e);
    }

    /**
     * 서버 내부 오류 예외 처리.
     * @param e 예외
     * @return INTERNAL_SERVER_ERROR 상태 코드와 메시지를 담은 응답
     */
    @ExceptionHandler({
            RuntimeException.class, InterruptedException.class, InternalError.class,
            JsonProcessingException.class, JSONException.class, IOException.class,
            SQLException.class, NullPointerException.class,
            IllegalStateException.class, ClassNotFoundException.class, NoSuchMethodException.class})
    public ResponseEntity<ApiResponseDto> handleServerException(Exception e) {
        return logAndRespond(HttpStatus.INTERNAL_SERVER_ERROR, e);
    }

    /**
     * 예외를 로그로 기록하고 응답을 생성하는 공통 메서드.
     * @param status HTTP 상태 코드
     * @param e      처리할 예외
     * @return 상태 코드와 메시지를 담은 응답
     */
    private ResponseEntity<ApiResponseDto> logAndRespond(HttpStatus status, Exception e) {
        if (status.is4xxClientError()) {
            log.error("Client error occurred: {}", e.getMessage());  // 400대 에러: 간단한 메시지만 로그
        } else {
            log.error("Exception occurred: {}", e.getMessage(), e);  // 500대 에러: 스택 트레이스 포함
        }
        return apiResponseManager.error(status, e.getMessage());
    }
}