package atemos.eguard.api.service;

import atemos.eguard.api.dto.LogDto;
import org.springframework.data.domain.Pageable;

/**
 * ApiCallLogService는 API 호출 로그와 관련된 비즈니스 로직을 처리하는 서비스 인터페이스입니다.
 *
 * 이 인터페이스는 API 호출 로그의 조회, 유료 API 호출 횟수 통계 조회 및 로그 삭제 기능을 제공합니다.
 */
public interface LogService {
    /**
     * 주어진 조건에 맞는 API 호출 로그를 조회합니다.
     *
     * @param readApiCallLogRequest API 호출 로그 조회 조건을 포함하는 데이터 전송 객체입니다.
     * @param pageable 페이징 정보를 포함하는 객체로, 결과 목록의 페이지 번호와 크기를 지정합니다.
     * @return 조회된 API 호출 로그 목록과 관련된 추가 정보를 포함하는 맵입니다. 이 맵에는 조회된 로그 목록과 총 페이지 수 등이 포함될 수 있습니다.
     */
    LogDto.ReadApiCallLogResponseList readApiCallLog(LogDto.ReadApiCallLogRequest readApiCallLogRequest, Pageable pageable);
    /**
     * 주어진 조건에 맞는 API 호출 로그를 조회합니다.
     *
     * @param readAuthenticationLogRequest API 호출 로그 조회 조건을 포함하는 데이터 전송 객체입니다.
     * @param pageable 페이징 정보를 포함하는 객체로, 결과 목록의 페이지 번호와 크기를 지정합니다.
     * @return 조회된 API 호출 로그 목록과 관련된 추가 정보를 포함하는 맵입니다. 이 맵에는 조회된 로그 목록과 총 페이지 수 등이 포함될 수 있습니다.
     */
    LogDto.ReadAuthenticationLogResponseList readAuthenticationLog(LogDto.ReadAuthenticationLogRequest readAuthenticationLogRequest, Pageable pageable);
}