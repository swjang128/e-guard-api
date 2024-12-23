package atemos.eguard.api.config;

import atemos.eguard.api.domain.AuthenticationStatus;
import atemos.eguard.api.domain.SampleData;
import atemos.eguard.api.entity.*;
import atemos.eguard.api.repository.*;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

/**
 * 데이터베이스 초기 설정을 담당하는 클래스입니다.
 * 데이터베이스가 비어 있는 경우 관리자 계정과 샘플 데이터를 생성합니다.
 */
@Component
@Slf4j
@Getter
@RequiredArgsConstructor
public class DatabaseInitializer {
    private final MenuRepository menuRepository;
    private final CompanyRepository companyRepository;
    private final SettingRepository settingRepository;
    private final EmployeeRepository employeeRepository;
    private final FactoryRepository factoryRepository;
    private final AreaRepository areaRepository;
    private final PasswordEncoder passwordEncoder;
    private final EncryptUtil encryptUtil;
    private final RandomGenerator randomGenerator = RandomGenerator.getDefault();

    /**
     * 데이터베이스 초기화를 담당하는 메서드입니다.
     * 샘플 데이터를 생성하여 데이터베이스에 저장합니다.
     */
    @PostConstruct
    @Transactional
    public void initializeDatabase() {
        createSampleParentMenu();
        createSampleChildrenMenu();
        createSampleCompany();
        createSampleFactory();
        createSampleArea();
        createSampleEmployee();
    }

    /**
     * 상위 메뉴 샘플 데이터를 생성하여 데이터베이스에 저장합니다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void createSampleParentMenu() {
        if (menuRepository.count() == 0) {
            log.info("**** 상위 메뉴 데이터가 없습니다. 샘플 상위 메뉴를 생성합니다.");
            var parentMenuList = Arrays.stream(SampleData.ParentMenu.values())
                    .map(parentMenu -> Menu.builder()
                            .name(parentMenu.getName())
                            .url(parentMenu.getPath())
                            .description(parentMenu.getDescription())
                            .accessibleRoles(parentMenu.getAccessibleRoles())
                            .depth(0)
                            .available(true)
                            .build())
                    .toList();
            menuRepository.saveAll(parentMenuList);
        }
    }

    /**
     * 하위 메뉴 샘플 데이터를 생성하여 데이터베이스에 저장합니다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void createSampleChildrenMenu() {
        if (!menuRepository.existsByDepth(1)) {
            log.info("**** Depth 1의 하위 메뉴 데이터가 없습니다. 샘플 하위 메뉴를 생성합니다.");
            // 상위 메뉴를 매핑하여 Map으로 변환
            var parentMenuMap = menuRepository.findAll().stream()
                    .collect(Collectors.toMap(Menu::getName, menu -> menu));
            var childrenMenuList = Arrays.stream(SampleData.ChildrenMenu.values())
                    .map(childrenMenu -> {
                        var parentMenu = parentMenuMap.get(childrenMenu.getParentMenu().getName());
                        if (parentMenu == null) {
                            throw new EntityNotFoundException("상위 메뉴를 찾을 수 없습니다: " + childrenMenu.getParentMenu().getName());
                        }
                        return Menu.builder()
                                .name(childrenMenu.getName())
                                .url(childrenMenu.getPath())
                                .description(childrenMenu.getDescription())
                                .accessibleRoles(childrenMenu.getAccessibleRoles())
                                .depth(parentMenu.getDepth() + 1)
                                .parent(parentMenu)
                                .available(true)
                                .build();
                    })
                    .toList();
            menuRepository.saveAll(childrenMenuList);
        }
    }

    /**
     * 샘플 업체 데이터를 생성하여 데이터베이스에 저장합니다.
     * 추가로 업체를 생성할 때 시스템 설정도 저장합니다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void createSampleCompany() {
        // 업체 정보가 없으면 샘플 업체 정보와 설정 정보를 저장
        if (companyRepository.count() == 0) {
            log.info("**** 업체 데이터가 없습니다. 샘플 업체를 생성합니다.");
            var companies = Arrays.stream(SampleData.Company.values())
                    .map(company -> Company.builder()
                            .businessNumber(company.getBusinessNumber())
                            .name(company.getName())
                            .email(company.getEmail())
                            .phoneNumber(company.getPhoneNumber())
                            .address(company.getAddress())
                            .addressDetail(company.getAddressDetail())
                            .build())
                    .toList();
            companyRepository.saveAll(companies);
            // 업체에 대한 설정 저장
            for (Company company : companies) {
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
            }
        }
    }

    /**
     * 샘플 공장 데이터를 생성하여 데이터베이스에 저장합니다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void createSampleFactory() {
        if (factoryRepository.count() == 0) {
            log.info("**** 공장 데이터가 없습니다. 샘플 공장을 생성합니다.");
            var factories = Arrays.stream(SampleData.Factory.values())
                    .map(factory -> {
                        var company = companyRepository.findByName(factory.getCompany().getName())
                                .orElseThrow(() -> new EntityNotFoundException("업체를 찾을 수 없습니다: " + factory.getCompany().getName()));
                        return Factory.builder()
                                .company(company)
                                .name(factory.getName())
                                .address(factory.getAddress())
                                .addressDetail(factory.getAddressDetail())
                                .totalSize(factory.getTotalSize())
                                .structureSize(factory.getStructureSize())
                                .industryType(factory.getIndustryType())
                                .build();
                    })
                    .toList();
            factoryRepository.saveAll(factories);
        }
    }

    /**
     * 샘플 근로자 데이터를 생성하여 데이터베이스에 저장합니다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void createSampleEmployee() {
        if (employeeRepository.count() == 0) {
            log.info("**** 근로자 데이터가 없습니다. 샘플 근로자를 생성합니다.");
            var employees = Arrays.stream(SampleData.Employee.values())
                    .map(employee -> {
                        var factory = factoryRepository.findByName(employee.getFactory().getName())
                                .orElseThrow(() -> new EntityNotFoundException("공장을 찾을 수 없습니다: " + employee.getFactory().getName()));
                        return Employee.builder()
                                .name(encryptUtil.encrypt(employee.getName()))
                                .employeeNumber(employee.getEmployeeNumber())
                                .email(encryptUtil.encrypt(employee.getEmail()))
                                .phoneNumber(encryptUtil.encrypt(employee.getPhoneNumber()))
                                .factory(factory)
                                .password(passwordEncoder.encode(employee.getPassword()))
                                .role(employee.getRole())
                                .authenticationStatus(AuthenticationStatus.ACTIVE)
                                .build();
                    })
                    .toList();
            employeeRepository.saveAll(employees);
        }
    }

    /**
     * 샘플 구역 데이터를 생성하여 데이터베이스에 저장합니다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void createSampleArea() {
        if (areaRepository.count() == 0) {
            log.info("**** 구역 데이터가 없습니다. 샘플 구역을 생성합니다.");
            var areas = Arrays.stream(SampleData.Area.values())
                    .map(area -> {
                        var factory = factoryRepository.findByName(area.getFactory().getName())
                                .orElseThrow(() -> new EntityNotFoundException("공장을 찾을 수 없습니다: " + area.getFactory().getName()));
                        return Area.builder()
                                .factory(factory)
                                .name(area.getName())
                                .location(area.getLocation())
                                .usableSize(area.getUsableSize())
                                .latitude(area.getLatitude())
                                .longitude(area.getLongitude())
                                .plan2DFilePath("/download/" + factory.getCompany().getId() + "/" + factory.getId() + area.getPlan2DFilePath())
                                .plan3DFilePath("/download/" + factory.getCompany().getId() + "/" + factory.getId() + area.getPlan3DFilePath())
                                .build();
                    })
                    .toList();
            areaRepository.saveAll(areas);
        }
    }
}