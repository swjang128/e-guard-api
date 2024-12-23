package atemos.eguard.api.service;

import atemos.eguard.api.dto.WorkDto;
import org.springframework.data.domain.Pageable;

/**
 * WorkService는 작업과 관련된 비즈니스 로직을 처리하는 서비스 인터페이스입니다.
 * 이 인터페이스는 작업 등록, 조회, 수정, 삭제와 관련된 기능을 제공하며,
 * 회원 가입 화면에서 노출되는 작업 목록도 조회할 수 있습니다.
 */
public interface WorkService {
    /**
     * 새로운 작업을 등록합니다.
     *
     * @param createWork 작업 등록을 위한 데이터 전송 객체입니다.
     * @return 생성된 작업 객체입니다. 등록된 작업의 상세 정보를 포함합니다.
     */
    WorkDto.ReadWorkResponse create(WorkDto.CreateWork createWork);
    /**
     * 조건에 맞는 작업 목록을 조회합니다.
     *
     * @param readWorkRequest 작업 목록 조회를 위한 요청 데이터 전송 객체입니다.
     * @param pageable 페이지 정보입니다. 결과를 페이지별로 나누어 조회할 때 사용됩니다.
     * @return 조건에 맞는 작업 목록과 페이지 정보를 포함하는 응답 객체입니다.
     *         조회된 작업 목록과 총 페이지 수, 현재 페이지 번호 등의 정보가 포함됩니다.
     */
    WorkDto.ReadWorkResponseList read(WorkDto.ReadWorkRequest readWorkRequest, Pageable pageable);
    /**
    /**
     * 기존 작업 정보를 수정합니다.
     *
     * @param workId 수정할 작업의 ID입니다.
     * @param updateWork 작업 수정을 위한 데이터 전송 객체입니다.
     * @return 수정된 작업 객체입니다.
     */
    WorkDto.ReadWorkResponse update(Long workId, WorkDto.UpdateWork updateWork);
    /**
     * 기존 작업을 삭제합니다.
     *
     * @param workId 삭제할 작업의 ID입니다.
     */
    void delete(Long workId);
}