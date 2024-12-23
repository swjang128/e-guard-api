package atemos.eguard.api.service;

import atemos.eguard.api.config.EncryptUtil;
import atemos.eguard.api.dto.LogDto;
import atemos.eguard.api.repository.ApiCallLogRepository;
import atemos.eguard.api.repository.AuthenticationLogRepository;
import atemos.eguard.api.specification.ApiCallLogSpecification;
import atemos.eguard.api.specification.AuthenticationLogSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ApiCallLogServiceImpl는 API 호출 로그와 관련된 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * API 호출 로그의 조회, 월별/일별 유료 호출 횟수 조회, 로그 삭제 기능을 제공합니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {
    private final ApiCallLogRepository apiCallLogRepository;
    private final AuthenticationLogRepository authenticationLogRepository;
    private final EncryptUtil encryptUtil;

    /**
     * 조건에 맞는 ApiCallLog를 조회합니다.
     *
     * @param readApiCallLogRequest ApiCallLog 조회 조건을 포함하는 데이터 전송 객체
     * @param pageable 페이징 정보를 포함하는 객체
     * @return 조회된 ApiCallLog 목록과 관련된 추가 정보를 포함하는 응답 객체
     */
    @Override
    @Transactional(readOnly = true)
    public LogDto.ReadApiCallLogResponseList readApiCallLog(LogDto.ReadApiCallLogRequest readApiCallLogRequest, Pageable pageable) {
        // 조건에 맞는 ApiCallLog 목록 조회
        var apiCallLogPage = apiCallLogRepository.findAll(ApiCallLogSpecification.findWith(readApiCallLogRequest), pageable);
        // ApiCallLog 응답 DTO로 변환하여 LocalDateTime으로 변환
        var apiCallLogList = apiCallLogPage.getContent().stream()
                .map(apiCallLog -> LogDto.ReadApiCallLogResponse.builder()
                        .apiCallLogId(apiCallLog.getId())
                        .companyId(apiCallLog.getCompany().getId())
                        .companyName(apiCallLog.getCompany().getName())
                        .employeeId(apiCallLog.getEmployee() != null ? apiCallLog.getEmployee().getId() : null)
                        .employeeEmail(apiCallLog.getEmployee() != null ? encryptUtil.decrypt(apiCallLog.getEmployee().getEmail()) : null)
                        .requestUri(apiCallLog.getRequestUri())
                        .httpMethod(apiCallLog.getHttpMethod())
                        .clientIp(apiCallLog.getClientIp())
                        .statusCode(apiCallLog.getStatusCode())
                        .metaData(apiCallLog.getMetaData())
                        .requestTime(apiCallLog.getRequestTime())
                        .build())
                .toList();
        // 응답 객체 반환
        return LogDto.ReadApiCallLogResponseList.builder()
                .apiCallLogList(apiCallLogList)
                .apiCallLogTotalElements(apiCallLogPage.getTotalElements())
                .apiCallLogTotalPages(apiCallLogPage.getTotalPages())
                .build();
    }

    /**
     * 조건에 맞는 AuthenticationLog를 조회합니다.
     *
     * @param readAuthenticationLogRequest AuthenticationLog 조회 조건을 포함하는 데이터 전송 객체
     * @param pageable 페이징 정보를 포함하는 객체
     * @return 조회된 AuthenticationLog 목록과 관련된 추가 정보를 포함한 응답 객체
     */
    @Override
    @Transactional(readOnly = true)
    public LogDto.ReadAuthenticationLogResponseList readAuthenticationLog(LogDto.ReadAuthenticationLogRequest readAuthenticationLogRequest, Pageable pageable) {
        // 조건에 맞는 AuthenticationLog 목록을 조회
        var authenticationLogPage = authenticationLogRepository.findAll(
                AuthenticationLogSpecification.findWith(readAuthenticationLogRequest),
                pageable);
        // 조회된 AuthenticationLog 목록을 응답 DTO로 변환
        var authenticationLogList = authenticationLogPage.getContent().stream()
                .map(authenticationLog -> LogDto.ReadAuthenticationLogResponse.builder()
                        .authenticationLogId(authenticationLog.getId())
                        .employeeId(authenticationLog.getEmployee() != null ? authenticationLog.getEmployee().getId() : null)
                        .employeeEmail(authenticationLog.getEmployee() != null ? encryptUtil.decrypt(authenticationLog.getEmployee().getEmail()) : null)
                        .companyId(authenticationLog.getCompany().getId())
                        .companyName(authenticationLog.getCompany().getName())
                        .requestUri(authenticationLog.getRequestUri())
                        .httpMethod(authenticationLog.getHttpMethod())
                        .clientIp(authenticationLog.getClientIp())
                        .statusCode(authenticationLog.getStatusCode())
                        .metaData(authenticationLog.getMetaData())
                        .requestTime(authenticationLog.getRequestTime())
                        .build())
                .toList();
        // 응답 객체 반환
        return LogDto.ReadAuthenticationLogResponseList.builder()
                .authenticationLogList(authenticationLogList)
                .authenticationLogTotalElements(authenticationLogPage.getTotalElements())
                .authenticationLogTotalPages(authenticationLogPage.getTotalPages())
                .build();
    }
}