package atemos.eguard.api.service;

import atemos.eguard.api.dto.AlarmDto;
import jakarta.servlet.http.HttpServletResponse;

/**
 * ReportService는 다양한 분석 데이터를 엑셀로 다운로드하는 기능을 제공하는 서비스 인터페이스입니다.
 */
public interface ReportService {
    /**
     * 특정 기간 알람 이력을 엑셀 파일로 제공합니다. (조회 결과는 시간별로 집계됩니다)
     *
     * @param readIotAlarmRequest IoT 상태 이력을 조회하기 위한 요청 DTO입니다.
     * @param response  HTTP 응답 객체입니다. 엑셀 파일을 클라이언트로 전송하기 위해 사용됩니다.
     */
    void reportAlarm(AlarmDto.ReadAlarmRequest readIotAlarmRequest, HttpServletResponse response);
}