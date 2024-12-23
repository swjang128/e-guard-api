package atemos.eguard.api.service;

import atemos.eguard.api.dto.FactoryDto;
import org.springframework.data.domain.Pageable;

/**
 * FactoryService는 공장과 관련된 비즈니스 로직을 처리하는 서비스 인터페이스입니다.
 *
 * 이 인터페이스는 공장 등록, 조회, 수정, 삭제와 관련된 기능을 제공하며,
 * 회원 가입 화면에서 노출되는 공장 목록도 조회할 수 있습니다.
 */
public interface FactoryService {
    /**
     * 새로운 공장을 등록합니다.
     *
     * @param createFactoryDto 공장 등록을 위한 데이터 전송 객체입니다.
     *                         이 객체에는 공장의 이름, 주소 등 등록에 필요한 정보가 포함됩니다.
     * @return 생성된 공장 객체입니다. 등록된 공장의 상세 정보를 포함합니다.
     */
    FactoryDto.ReadFactoryResponse create(FactoryDto.CreateFactory createFactoryDto);
    /**
     * 조건에 맞는 공장 목록을 조회합니다.
     *
     * @param readFactoryRequestDto 공장 목록 조회를 위한 요청 데이터 전송 객체입니다.
     *                              이 객체에는 검색 조건과 필터링 정보가 포함됩니다.
     * @param pageable 페이지 정보입니다. 결과를 페이지별로 나누어 조회할 때 사용됩니다.
     * @return 조건에 맞는 공장 목록과 페이지 정보를 포함하는 응답 객체입니다.
     *         조회된 공장 목록과 총 페이지 수, 현재 페이지 번호 등의 정보가 포함됩니다.
     */
    FactoryDto.ReadFactoryResponseList read(FactoryDto.ReadFactoryRequest readFactoryRequestDto, Pageable pageable);
    /**
     * 기존 공장 정보를 수정합니다.
     *
     * @param factoryId 수정할 공장의 ID입니다.
     * @param updateFactoryDto 공장 수정을 위한 데이터 전송 객체입니다.
     *                         이 객체에는 수정할 공장의 이름, 주소, 연락처 등 수정 정보가 포함됩니다.
     * @return 수정된 공장 객체입니다.
     */
    FactoryDto.ReadFactoryResponse update(Long factoryId, FactoryDto.UpdateFactory updateFactoryDto);
    /**
     * 기존 공장을 삭제합니다.
     *
     * @param factoryId 삭제할 공장의 ID입니다.
     */
    void delete(Long factoryId);
    /**
     * JWT 토큰을 사용하여 현재 로그인된 근로자의 공장 정보를 조회합니다.
     *
     * @return 공장 정보 객체입니다. 근로자가 속한 공장 정보가 포함됩니다.
     */
    FactoryDto.ReadFactoryResponse readFactoryInfo();
    /**
     * 근로자를 등록할 때 해당 업체의 공장 목록을 조회합니다.
     * @param readFactoryRequestDto 공장 목록 조회를 위한 요청 데이터 전송 객체입니다.
     * @return 해당 업체의 공장 목록 응답 객체입니다.
     */
    FactoryDto.ReadFactoryResponseList readSignUpFactoryList(FactoryDto.ReadFactoryRequest readFactoryRequestDto);
    /**
     * 공장 요약 정보를 조회합니다.
     * 지정된 공장의 전체 근로자 수와 상태별 근로자 수 정보를 제공합니다.
     *
     * @param factorySummaryRequestDto 공장 요약 정보 조회를 위한 요청 객체입니다.
     * @return 공장 요약 정보 객체입니다. 전체 근로자 수와 상태별 근로자 수가 포함됩니다.
     */
    FactoryDto.FactorySummaryResponse getFactorySummary(FactoryDto.FactorySummaryRequest factorySummaryRequestDto);
}