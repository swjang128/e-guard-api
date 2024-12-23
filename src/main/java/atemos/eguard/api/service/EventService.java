package atemos.eguard.api.service;

import atemos.eguard.api.dto.EventDto;
import org.springframework.data.domain.Pageable;

/**
 * EventService는 사건과 관련된 비즈니스 로직을 처리하는 서비스 인터페이스입니다.
 * 이 인터페이스는 사건 등록, 조회, 수정, 삭제와 관련된 기능을 제공하며,
 * 회원 가입 화면에서 노출되는 사건 목록도 조회할 수 있습니다.
 */
public interface EventService {
    /**
     * 새로운 사건을 등록합니다.
     *
     * @param createEventDto 사건 등록을 위한 데이터 전송 객체입니다.
     * @return 생성된 사건 객체입니다. 등록된 사건의 상세 정보를 포함합니다.
     */
    EventDto.ReadEventResponse create(EventDto.CreateEvent createEventDto);
    /**
     * 조건에 맞는 사건 목록을 조회합니다.
     *
     * @param readEventRequestDto 사건 목록 조회를 위한 요청 데이터 전송 객체입니다.
     *                              이 객체에는 검색 조건과 필터링 정보가 포함됩니다.
     * @param pageable 페이지 정보입니다. 결과를 페이지별로 나누어 조회할 때 사용됩니다.
     * @return 조건에 맞는 사건 목록과 페이지 정보를 포함하는 응답 객체입니다.
     *         조회된 사건 목록과 총 페이지 수, 현재 페이지 번호 등의 정보가 포함됩니다.
     */
    EventDto.ReadEventResponseList read(EventDto.ReadEventRequest readEventRequestDto, Pageable pageable);
    /**
     * 특정 사건 정보를 수정합니다.
     *
     * @param eventId 수정할 사건의 ID입니다.
     * @param updateEventDto 사건 수정을 위한 데이터 전송 객체입니다.
     * @return 수정된 사건 객체입니다.
     */
    EventDto.ReadEventResponse update(Long eventId, EventDto.UpdateEvent updateEventDto);
    /**
     * 특정 사건을 삭제합니다.
     *
     * @param eventId 삭제할 사건의 ID입니다.
     */
    void delete(Long eventId);
    /**
     * 특정 업체의 공장 및 구역에 대한 안전 점수 및 안전 등급을 계산합니다.
     *
     * @param companyId 안전 점수를 계산할 업체 ID입니다.
     * @return 해당 업체의 공장 및 구역에 대한 안전 점수 및 안전 등급 정보를 포함하는 EventDto.SafetyScore 객체입니다.
     */
    EventDto.SafetyScore readSafetyScore(Long companyId);
    /**
     * 모든 사건을 해결 상태로 일괄 처리합니다.
     *
     * @implNote 이 메서드는 모든 사건의 상태를 해결 상태로 일괄 수정합니다.
     *           특정 조건이 없는 모든 사건을 대상으로 합니다.
     */
    void resolveAllEvents();
}