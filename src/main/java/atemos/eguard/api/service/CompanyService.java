package atemos.eguard.api.service;

import atemos.eguard.api.dto.CompanyDto;
import org.springframework.data.domain.Pageable;

/**
 * CompanyService는 업체와 관련된 비즈니스 로직을 처리하는 서비스 인터페이스입니다.
 *
 * 이 인터페이스는 업체 등록, 조회, 수정, 삭제와 관련된 기능을 제공하며,
 * 회원 가입 화면에서 노출되는 업체 목록도 조회할 수 있습니다.
 */
public interface CompanyService {
    /**
     * 새로운 업체를 등록합니다.
     *
     * @param createCompanyDto 업체 등록을 위한 데이터 전송 객체입니다.
     *                         이 객체에는 업체의 이름, 주소, 연락처 등 등록에 필요한 정보가 포함됩니다.
     * @return 생성된 업체 객체입니다. 등록된 업체의 상세 정보를 포함합니다.
     */
    CompanyDto.ReadCompanyResponse create(CompanyDto.CreateCompany createCompanyDto);
    /**
     * 조건에 맞는 업체 목록을 조회합니다.
     *
     * @param readCompanyRequestDto 업체 목록 조회를 위한 요청 데이터 전송 객체입니다.
     *                              이 객체에는 검색 조건과 필터링 정보가 포함됩니다.
     * @param pageable 페이지 정보입니다. 결과를 페이지별로 나누어 조회할 때 사용됩니다.
     * @return 조건에 맞는 업체 목록과 페이지 정보를 포함하는 응답 객체입니다.
     *         조회된 업체 목록과 총 페이지 수, 현재 페이지 번호 등의 정보가 포함됩니다.
     */
    CompanyDto.ReadCompanyResponseList read(CompanyDto.ReadCompanyRequest readCompanyRequestDto, Pageable pageable);
    /**
     * 기존 업체 정보를 수정합니다.
     *
     * @param companyId 수정할 업체의 ID입니다.
     * @param updateCompanyDto 업체 수정을 위한 데이터 전송 객체입니다.
     *                         이 객체에는 수정할 업체의 이름, 주소, 연락처 등 수정 정보가 포함됩니다.
     * @return 수정된 업체 객체입니다.
     */
    CompanyDto.ReadCompanyResponse update(Long companyId, CompanyDto.UpdateCompany updateCompanyDto);
    /**
     * 기존 업체를 삭제합니다.
     *
     * @param companyId 삭제할 업체의 ID입니다.
     */
    void delete(Long companyId);
    /**
     * JWT 토큰을 사용하여 현재 로그인된 근로자의 업체 정보를 조회합니다.
     *
     * @return 업체 정보 객체입니다. 근로자가 속한 업체 정보가 포함됩니다.
     */
    CompanyDto.ReadCompanyResponse readCompanyInfo();
}