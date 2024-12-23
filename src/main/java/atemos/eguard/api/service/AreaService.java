package atemos.eguard.api.service;

import atemos.eguard.api.dto.AreaDto;
import org.springframework.data.domain.Pageable;

/**
 * AreaService는 구역과 관련된 비즈니스 로직을 처리하는 서비스 인터페이스입니다.
 * 이 인터페이스는 구역 등록, 조회, 수정, 삭제와 관련된 기능을 제공하며,
 * 회원 가입 화면에서 노출되는 구역 목록도 조회할 수 있습니다.
 */
public interface AreaService {
    /**
     * 새로운 구역을 등록합니다.
     *
     * @param createAreaDto 구역 등록을 위한 데이터 전송 객체입니다.
     *                         이 객체에는 구역의 이름, 위치 등 등록에 필요한 정보가 포함됩니다.
     * @return 생성된 구역 객체입니다. 등록된 구역의 상세 정보를 포함합니다.
     */
    AreaDto.ReadAreaResponse create(AreaDto.CreateArea createAreaDto);
    /**
     * 조건에 맞는 구역 목록을 조회합니다.
     *
     * @param readAreaRequestDto 구역 목록 조회를 위한 요청 데이터 전송 객체입니다.
     *                              이 객체에는 검색 조건과 필터링 정보가 포함됩니다.
     * @param pageable 페이지 정보입니다. 결과를 페이지별로 나누어 조회할 때 사용됩니다.
     * @return 조건에 맞는 구역 목록과 페이지 정보를 포함하는 응답 객체입니다.
     *         조회된 구역 목록과 총 페이지 수, 현재 페이지 번호 등의 정보가 포함됩니다.
     */
    AreaDto.ReadAreaResponseList read(AreaDto.ReadAreaRequest readAreaRequestDto, Pageable pageable);
    /**
    /**
     * 기존 구역 정보를 수정합니다.
     *
     * @param areaId 수정할 구역의 ID입니다.
     * @param updateAreaDto 구역 수정을 위한 데이터 전송 객체입니다.
     *                         이 객체에는 수정할 구역의 이름, 위치, 연락처 등 수정 정보가 포함됩니다.
     * @return 수정된 구역 객체입니다.
     */
    AreaDto.ReadAreaResponse update(Long areaId, AreaDto.UpdateArea updateAreaDto);
    /**
     * 기존 구역을 삭제합니다.
     *
     * @param areaId 삭제할 구역의 ID입니다.
     */
    void delete(Long areaId);
}