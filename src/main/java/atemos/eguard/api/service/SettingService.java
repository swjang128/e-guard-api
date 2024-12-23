package atemos.eguard.api.service;

import atemos.eguard.api.dto.SettingDto;

/**
 * SettingService는 다양한 분석 데이터를 엑셀로 다운로드하는 기능을 제공하는 서비스 인터페이스입니다.
 */
public interface SettingService {
    /**
     * 조건에 맞는 시스템 설정들을 조회합니다.
     *
     * @param readSettingRequestDto 시스템 설정 조회 조건을 담고 있는 객체입니다.
     * @return 조건에 맞는 시스템 설정들을 포함하는 응답 객체입니다.
     */
    SettingDto.ReadSettingResponseList read(SettingDto.ReadSettingRequest readSettingRequestDto);
    /**
     * 특정 업체의 시스템 설정을 수정합니다.
     *
     * @param settingId 수정할 시스템 설정의 ID입니다.
     * @param updateSettingDto 메뉴 수정을 위한 데이터 전송 객체입니다.
     * @return 수정된 메뉴 정보를 담고 있는 객체입니다.
     */
    SettingDto.ReadSettingResponse update(Long settingId, SettingDto.UpdateSetting updateSettingDto);
}