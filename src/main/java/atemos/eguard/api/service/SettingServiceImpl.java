package atemos.eguard.api.service;

import atemos.eguard.api.config.EncryptUtil;
import atemos.eguard.api.config.EntityValidator;
import atemos.eguard.api.dto.SettingDto;
import atemos.eguard.api.repository.SettingRepository;
import atemos.eguard.api.specification.SettingSpecification;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * SettingServiceImpl 클래스는 시스템 설정 관리 기능을 제공하는 서비스 클래스입니다.
 * 특정 업체의 시스템 설정을 조회하거나, 수정할 수 있습니다.
 */
@Service
@Slf4j
@AllArgsConstructor
public class SettingServiceImpl implements SettingService {
    private final EntityValidator entityValidator;
    private final SettingRepository settingRepository;
    private final EncryptUtil encryptUtil;

    /**
     * 조건에 맞는 시스템 설정들을 조회합니다.
     *
     * @param readSettingRequestDto 시스템 설정 조회 조건을 담고 있는 객체입니다.
     * @return 조건에 맞는 시스템 설정들을 포함하는 응답 객체입니다.
     */
    @Override
    @Transactional(readOnly = true)
    public SettingDto.ReadSettingResponseList read(SettingDto.ReadSettingRequest readSettingRequestDto) {
        // 시스템 설정 조회 후 리턴
        var settingResponse = settingRepository.findAll(SettingSpecification.findWith(readSettingRequestDto, encryptUtil));
        var settingList = settingResponse.stream()
                .map(setting -> SettingDto.ReadSettingResponse.builder()
                        .settingId(setting.getId())
                        .companyId(setting.getCompany().getId())
                        .companyName(setting.getCompany().getName())
                        .maxFactoriesPerCompany(setting.getMaxFactoriesPerCompany())
                        .maxAreasPerFactory(setting.getMaxAreasPerFactory())
                        .maxEmployeesPerFactory(setting.getMaxEmployeesPerFactory())
                        .maxWorksPerArea(setting.getMaxWorksPerArea())
                        .maxEmployeesPerWork(setting.getMaxEmployeesPerWork())
                        .twoFactorAuthenticationEnabled(setting.getTwoFactorAuthenticationEnabled())
                        .twoFactorAuthenticationMethod(setting.getTwoFactorAuthenticationMethod())
                        .createdAt(setting.getCreatedAt())
                        .updatedAt(setting.getUpdatedAt())
                        .build())
                .toList();
        return SettingDto.ReadSettingResponseList.builder()
                .settingList(settingList)
                .build();
    }

    /**
     * 특정 업체의 시스템 설정을 수정합니다.
     *
     * @param settingId 수정할 시스템 설정의 ID입니다.
     * @param updateSettingDto 시스템 설정 수정을 위한 데이터 전송 객체입니다.
     * @return 수정한 시스템 설정 정보를 담고 있는 객체입니다.
     */
    @Override
    public SettingDto.ReadSettingResponse update(Long settingId, SettingDto.UpdateSetting updateSettingDto) {
        // 기존 시스템 설정을 현재 접속한 근로자가 수정할 수 있는지 검증 및 조회
        var setting = entityValidator.validateSettingIds(List.of(settingId))
                .stream().findFirst()
                .orElseThrow(() -> new AccessDeniedException("시스템 설정을 찾을 수 없거나 수정 권한이 없습니다."));
        // 기존 시스템 설정을 사용하던 업체를 현재 접속한 근로자가 수정할 수 있는지 검증
        entityValidator.validateCompanyIds(List.of(setting.getCompany().getId()))
                .stream().findFirst()
                .orElseThrow(() -> new AccessDeniedException("시스템 설정을 사용하던 업체를 찾을 수 없거나 수정 권한이 없습니다."));
        // 수정할 시스템 설정에 업체 ID가 존재하면 현재 접속한 근로자가 접근 가능한 업체인지 검증 및 조회
        Optional.ofNullable(updateSettingDto.getCompanyId()).ifPresent(companyId -> {
            var company = entityValidator.validateCompanyIds(List.of(companyId))
                    .stream().findFirst()
                    .orElseThrow(() -> new AccessDeniedException("업체를 찾을 수 없거나 수정 권한이 없습니다."));
            setting.setCompany(company);
        });
        // 다른 수정 객체들을 시스템 엔티티 객체에 Set
        Optional.ofNullable(updateSettingDto.getMaxFactoriesPerCompany()).ifPresent(setting::setMaxFactoriesPerCompany);
        Optional.ofNullable(updateSettingDto.getMaxAreasPerFactory()).ifPresent(setting::setMaxAreasPerFactory);
        Optional.ofNullable(updateSettingDto.getMaxEmployeesPerFactory()).ifPresent(setting::setMaxEmployeesPerFactory);
        Optional.ofNullable(updateSettingDto.getMaxWorksPerArea()).ifPresent(setting::setMaxWorksPerArea);
        Optional.ofNullable(updateSettingDto.getMaxEmployeesPerWork()).ifPresent(setting::setMaxEmployeesPerWork);
        Optional.ofNullable(updateSettingDto.getTwoFactorAuthenticationEnabled()).ifPresent(setting::setTwoFactorAuthenticationEnabled);
        Optional.ofNullable(updateSettingDto.getTwoFactorAuthenticationMethod()).ifPresent(setting::setTwoFactorAuthenticationMethod);
        // 수정한 시스템 설정을 저장
        settingRepository.save(setting);
        // 저장한 시스템 설정을 ReadSettingResponse로 변환 후 응답 DTO 리턴
        return SettingDto.ReadSettingResponse.builder()
                .settingId(setting.getId())
                .companyId(setting.getCompany().getId())
                .companyName(setting.getCompany().getName())
                .maxFactoriesPerCompany(setting.getMaxFactoriesPerCompany())
                .maxAreasPerFactory(setting.getMaxAreasPerFactory())
                .maxEmployeesPerFactory(setting.getMaxEmployeesPerFactory())
                .maxWorksPerArea(setting.getMaxWorksPerArea())
                .maxEmployeesPerWork(setting.getMaxEmployeesPerWork())
                .twoFactorAuthenticationEnabled(setting.getTwoFactorAuthenticationEnabled())
                .twoFactorAuthenticationMethod(setting.getTwoFactorAuthenticationMethod())
                .createdAt(setting.getCreatedAt())
                .updatedAt(setting.getUpdatedAt())
                .build();
    }
}