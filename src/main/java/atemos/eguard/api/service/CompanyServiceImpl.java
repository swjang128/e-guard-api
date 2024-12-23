package atemos.eguard.api.service;

import atemos.eguard.api.config.EntityValidator;
import atemos.eguard.api.domain.SampleData;
import atemos.eguard.api.dto.CompanyDto;
import atemos.eguard.api.entity.Company;
import atemos.eguard.api.entity.Setting;
import atemos.eguard.api.repository.CompanyRepository;
import atemos.eguard.api.repository.SettingRepository;
import atemos.eguard.api.specification.CompanySpecification;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * CompanyServiceImpl는 업체와 관련된 서비스 로직을 구현한 클래스입니다.
 * 업체 등록, 수정, 삭제 및 조회와 같은 기능을 제공합니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {
    private final CompanyRepository companyRepository;
    private final SettingRepository settingRepository;
    private final AuthenticationServiceImpl authenticationService;
    private final EntityValidator entityValidator;

    /**
     * 업체를 등록합니다.
     *
     * @param createCompanyDto 등록할 업체 정보를 담고 있는 DTO
     * @return 등록된 업체 정보를 담고 있는 DTO 응답
     */
    @Override
    @Transactional
    public CompanyDto.ReadCompanyResponse create(CompanyDto.CreateCompany createCompanyDto) {
        // 업체 정보를 빌드하고 저장
        var company = Company.builder()
                .businessNumber(createCompanyDto.getCompanyBusinessNumber())
                .name(createCompanyDto.getCompanyName())
                .email(createCompanyDto.getCompanyEmail())
                .phoneNumber(createCompanyDto.getCompanyPhoneNumber())
                .address(createCompanyDto.getCompanyAddress())
                .addressDetail(createCompanyDto.getCompanyAddressDetail())
                .build();
        company = companyRepository.save(company);
        // 시스템 설정을 저장
        var setting = Setting.builder()
                .company(company)
                .maxFactoriesPerCompany(SampleData.Setting.DEFAULT.getMaxFactoriesPerCompany())
                .maxAreasPerFactory(SampleData.Setting.DEFAULT.getMaxAreasPerFactory())
                .maxEmployeesPerFactory(SampleData.Setting.DEFAULT.getMaxEmployeesPerFactory())
                .maxWorksPerArea(SampleData.Setting.DEFAULT.getMaxWorksPerArea())
                .maxEmployeesPerWork(SampleData.Setting.DEFAULT.getMaxEmployeesPerWork())
                .twoFactorAuthenticationEnabled(SampleData.Setting.DEFAULT.getTwoFactorAuthenticationEnabled())
                .twoFactorAuthenticationMethod(SampleData.Setting.DEFAULT.getTwoFactorAuthenticationMethod())
                .build();
        settingRepository.save(setting);
        // 저장된 업체 정보를 ReadCompanyResponse DTO로 변환하여 반환
        return CompanyDto.ReadCompanyResponse.builder()
                .companyId(company.getId())
                .companyName(company.getName())
                .companyEmail(company.getEmail())
                .companyPhoneNumber(company.getPhoneNumber())
                .companyAddress(company.getAddress())
                .companyAddressDetail(company.getAddressDetail())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();
    }

    /**
     * 조건에 맞는 업체 목록을 조회합니다.
     *
     * @param readCompanyRequestDto 업체 조회 조건을 담고 있는 DTO
     * @param pageable 페이징 정보를 담고 있는 객체
     * @return 조회된 업체 목록과 페이지 정보를 포함한 응답 객체
     */
    @Override
    @Transactional(readOnly = true)
    public CompanyDto.ReadCompanyResponseList read(CompanyDto.ReadCompanyRequest readCompanyRequestDto, Pageable pageable) {
        // 업체 목록 조회
        var companyPage = companyRepository.findAll(
                CompanySpecification.findWith(readCompanyRequestDto),
                pageable);
        // 조회한 업체 목록을 응답 객체로 변환하여 반환
        var companyList = companyPage.getContent().stream()
                .map(company -> CompanyDto.ReadCompanyResponse.builder()
                        .companyId(company.getId())
                        .companyBusinessNumber(company.getBusinessNumber())
                        .companyName(company.getName())
                        .companyEmail(company.getEmail())
                        .companyPhoneNumber(company.getPhoneNumber())
                        .companyAddress(company.getAddress())
                        .companyAddressDetail(company.getAddressDetail())
                        .createdAt(company.getCreatedAt())
                        .updatedAt(company.getUpdatedAt())
                        .build())
                .toList();
        return CompanyDto.ReadCompanyResponseList.builder()
                .companyList(companyList)
                .totalElements(companyPage.getTotalElements())
                .totalPages(companyPage.getTotalPages())
                .build();
    }

    /**
     * 기존 업체 정보를 수정합니다.
     *
     * @param companyId 수정할 업체의 ID
     * @param updateCompanyDto 수정할 업체 정보를 담고 있는 DTO
     * @return 수정된 업체 정보를 담은 응답 객체
     */
    @Override
    @Transactional
    public CompanyDto.ReadCompanyResponse update(Long companyId, CompanyDto.UpdateCompany updateCompanyDto) {
        // 기존 업체를 현재 접속한 근로자가 수정할 수 있는 권한이 있는지 검증 후 조회
        var company = entityValidator.validateCompanyIds(List.of(companyId))
                .stream().findFirst().orElseThrow(() -> new AccessDeniedException("업체를 찾을 수 없거나 수정 권한이 없습니다."));
        // 업체의 나머지 필드들을 업데이트
        Optional.ofNullable(updateCompanyDto.getCompanyBusinessNumber()).ifPresent(company::setBusinessNumber);
        Optional.ofNullable(updateCompanyDto.getCompanyName()).ifPresent(company::setName);
        Optional.ofNullable(updateCompanyDto.getCompanyEmail()).ifPresent(company::setEmail);
        Optional.ofNullable(updateCompanyDto.getCompanyPhoneNumber()).ifPresent(company::setPhoneNumber);
        Optional.ofNullable(updateCompanyDto.getCompanyAddress()).ifPresent(company::setAddress);
        Optional.ofNullable(updateCompanyDto.getCompanyAddressDetail()).ifPresent(company::setAddressDetail);
        // 수정된 업체 정보를 저장하고 DTO로 변환하여 반환
        return CompanyDto.ReadCompanyResponse.builder()
                .companyId(company.getId())
                .companyBusinessNumber(company.getBusinessNumber())
                .companyName(company.getName())
                .companyEmail(company.getEmail())
                .companyPhoneNumber(company.getPhoneNumber())
                .companyAddress(company.getAddress())
                .companyAddressDetail(company.getAddressDetail())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();
    }

    /**
     * 업체를 삭제합니다.
     *
     * @param companyId 삭제할 업체의 ID
     */
    @Override
    @Transactional
    public void delete(Long companyId) {
        // 삭제할 업체가 존재하는지 확인하고 없으면 예외 처리
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 업체입니다."));
        // 업체를 삭제
        companyRepository.delete(company);
    }

    /**
     * JWT 토큰을 사용하여 현재 로그인된 근로자의 업체 정보를 조회합니다.
     *
     * @return 업체 정보 객체입니다. 근로자가 속한 업체 정보가 포함됩니다.
     */
    @Override
    @Transactional(readOnly = true)
    public CompanyDto.ReadCompanyResponse readCompanyInfo() {
        // 현재 근로자의 정보 조회
        var employeeInfo = authenticationService.getCurrentEmployeeInfo();
        // 근로자가 속한 업체를 조건에 맞게 조회
        var readCompanyRequestDto = CompanyDto.ReadCompanyRequest.builder()
                .companyIds(List.of(employeeInfo.getCompanyId()))
                .build();
        var companyPageResponse = this.read(readCompanyRequestDto, Pageable.ofSize(1));
        // 조회된 업체가 없을 경우 예외 처리
        return companyPageResponse.getCompanyList().stream()
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 업체입니다."));
    }
}