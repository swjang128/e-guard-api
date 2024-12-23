package atemos.eguard.api.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

public class SampleData {
    @Getter
    @AllArgsConstructor
    public enum ParentMenu {
        MAIN("메인",
                "/",
                "전체적인 상황을 한 눈에 확인할 수 있는 대시보드입니다.",
                List.of(EmployeeRole.ADMIN, EmployeeRole.MANAGER, EmployeeRole.WORKER)),
        PROFILE("마이 페이지",
                "/profile",
                "현재 로그인한 근로자와 소속된 공장, 소속된 업체에 대한 정보를 확인할 수 있습니다.",
                List.of(EmployeeRole.ADMIN, EmployeeRole.MANAGER, EmployeeRole.WORKER)),
        MANAGEMENT("관리",
                "/management",
                "업체, 공장, 구역, 근로자, 사건 등의 정보를 관리합니다.",
                List.of(EmployeeRole.ADMIN, EmployeeRole.MANAGER)),
        EVENT("사건",
                "/event",
                "근로자 또는 구역에서 발생한 사건을 관리합니다.",
                List.of(EmployeeRole.MANAGER));

        private final String name;
        private final String path;
        private final String description;
        private final List<EmployeeRole> accessibleRoles;
    }

    @Getter
    @AllArgsConstructor
    public enum ChildrenMenu {
        SETTING("시스템 설정",
                "/management/setting",
                "시스템 설정을 관리합니다.",
                ParentMenu.MANAGEMENT,
                List.of(EmployeeRole.ADMIN)),
        COMPANY("업체 관리",
                "/management/company",
                "업체 정보를 관리합니다.",
                ParentMenu.MANAGEMENT,
                List.of(EmployeeRole.ADMIN)),
        FACTORY("공장 관리",
                "/management/factory",
                "공장 정보를 확인하고 관리합니다.",
                ParentMenu.MANAGEMENT,
                ParentMenu.MANAGEMENT.getAccessibleRoles()),
        WORK("작업 관리",
                "/management/work",
                "작업 정보를 확인하고 관리합니다.",
                ParentMenu.MANAGEMENT,
                List.of(EmployeeRole.MANAGER)),
        MANAGER("관리자 계정 관리",
                "/management/manager",
                "공장 관리자 계정을 관리합니다.",
                ParentMenu.MANAGEMENT,
                List.of(EmployeeRole.ADMIN)),
        EMPLOYEE("근로자 관리",
                "/management/employee",
                "근로자들의 정보를 관리합니다.",
                ParentMenu.MANAGEMENT,
                List.of(EmployeeRole.MANAGER)),
        EVENT_AREA("구역 사건",
                "/event/area",
                "구역에서 발생한 사건들을 관리합니다.",
                ParentMenu.EVENT,
                List.of(EmployeeRole.MANAGER)),
        EVENT_EMPLOYEE("근로자 사건",
                "/event/employee",
                "근로자에게 발생한 사건들을 관리합니다.",
                ParentMenu.EVENT,
                List.of(EmployeeRole.MANAGER));

        private final String name;
        private final String path;
        private final String description;
        private final ParentMenu parentMenu;
        private final List<EmployeeRole> accessibleRoles;
    }

    @Getter
    @AllArgsConstructor
    public enum Company {
        ATEMOS("4618702257",
                "ATEMoS",
                "atemos@atemos.co.kr",
                "01012345678",
                "ATEMoS 주소",
                "ATEMoS 상세주소"),
        DTAAS("8048701061",
                "DTAAS",
                "dtaas@dtaas.com",
                "01098761234",
                "DTAAS 주소",
                "DTAAS 상세주소");

        private final String businessNumber;
        private final String name;
        private final String email;
        private final String phoneNumber;
        private final String address;
        private final String addressDetail;
    }

    @Getter
    @AllArgsConstructor
    public enum Setting {
        DEFAULT(10,
                20,
                1000,
                10,
                60,
                false,
                TwoFactoryAuthenticationMethod.EMAIL);

        private final Integer maxFactoriesPerCompany;
        private final Integer maxAreasPerFactory;
        private final Integer maxEmployeesPerFactory;
        private final Integer maxWorksPerArea;
        private final Integer maxEmployeesPerWork;
        private final Boolean twoFactorAuthenticationEnabled;
        private final TwoFactoryAuthenticationMethod twoFactorAuthenticationMethod;
    }

    @Getter
    @AllArgsConstructor
    public enum Factory {
        ATEMOS_FACTORY("ATEMoS 공장",
                "ATEMoS 공장 주소",
                "ATEMoS 공장 상세주소",
                BigDecimal.valueOf(512.2568),
                BigDecimal.valueOf(365.1234),
                IndustryType.ENERGY,
                Company.ATEMOS),
        DTAAS_FACTORY("DTAAS 공장",
                "DTAAS 공장 주소",
                "DTAAS 공장 상세주소",
                BigDecimal.valueOf(256.1284),
                BigDecimal.valueOf(128.7898),
                IndustryType.MANUFACTURING,
                Company.DTAAS);

        private final String name;
        private final String address;
        private final String addressDetail;
        private final BigDecimal totalSize;
        private final BigDecimal structureSize;
        private final IndustryType industryType;
        private final Company company;
    }

    @Getter
    @AllArgsConstructor
    public enum Area {
        ATEMOS_WASTEWATER_TREATMENT("오염수 처리 장치",
                "ATEMoS 공장 오염수 처리 장치 구역",
                BigDecimal.valueOf(32.9876),
                BigDecimal.valueOf(37.49440),
                BigDecimal.valueOf(127.1102),
                "/1/2DPlan/atemos_area1_2d.png",
                "/1/3DPlan/atemos_area1_3d.glb",
                Factory.ATEMOS_FACTORY),
        ATEMOS_CONTAINER("화물 컨테이너",
                "ATEMoS 공장 화물 컨테이너 구역",
                BigDecimal.valueOf(49.1234),
                BigDecimal.valueOf(37.49277),
                BigDecimal.valueOf(127.1141),
                "/2/2DPlan/atemos_area2_2d.png",
                "/2/3DPlan/atemos_area2_3d.glb",
                Factory.ATEMOS_FACTORY),
        ATEMOS_FUEL_TANK("장비 연료탱크",
                "ATEMoS 공장 장비 연료탱크 구역",
                BigDecimal.valueOf(64.1129),
                BigDecimal.valueOf(37.49306),
                BigDecimal.valueOf(127.1151),
                "/3/2DPlan/atemos_area3_2d.png",
                "/3/3DPlan/atemos_area3_3d.glb",
                Factory.ATEMOS_FACTORY),
        DTAAS_WASTEWATER_TREATMENT("오염수 처리 장치",
                "DTAAS 공장 오염수 처리 장치 구역",
                BigDecimal.valueOf(16.4812),
                BigDecimal.valueOf(37.49440),
                BigDecimal.valueOf(127.1102),
                "/4/2DPlan/dtaas_area1_2d.png",
                "/4/3DPlan/dtaas_area1_3d.glb",
                Factory.DTAAS_FACTORY),
        DTAAS_CONTAINER("화물 컨테이너",
                "DTAAS 공장 화물 컨테이너 구역",
                BigDecimal.valueOf(24.5564),
                BigDecimal.valueOf(37.49277),
                BigDecimal.valueOf(127.1141),
                "/5/2DPlan/dtaas_area2_2d.png",
                "/5/3DPlan/dtaas_area2_3d.glb",
                Factory.DTAAS_FACTORY),
        DTAAS_FUEL_TANK("장비 연료탱크",
                "DTAAS 공장 장비 연료탱크 구역",
                BigDecimal.valueOf(32.2468),
                BigDecimal.valueOf(37.49306),
                BigDecimal.valueOf(127.1151),
                "/6/2DPlan/dtaas_area3_2d.png",
                "/6/3DPlan/dtaas_area3_3d.glb",
                Factory.DTAAS_FACTORY);

        private final String name;
        private final String location;
        private final BigDecimal usableSize;
        private final BigDecimal latitude;
        private final BigDecimal longitude;
        private final String plan2DFilePath;
        private final String plan3DFilePath;
        private final Factory factory;
    }

    @Getter
    @AllArgsConstructor
    public enum Employee {
        ATEMOS_ADMIN("ATEMoS 운영자",
                "ATEMoS1",
                "admin@atemos.co.kr",
                "01012342424",
                "Atemos1234!",
                EmployeeRole.ADMIN,
                Factory.ATEMOS_FACTORY),
        ATEMOS_MANAGER("ATEMoS 관리자",
                "ATEMoS2",
                "manager@atemos.co.kr",
                "01012343535",
                "Atemos1234!",
                EmployeeRole.MANAGER,
                Factory.ATEMOS_FACTORY),
        ATEMOS_WORKER("ATEMoS 근로자",
                "ATEMoS3",
                "worker@atemos.co.kr",
                "01012344646",
                "Atemos1234!",
                EmployeeRole.WORKER,
                Factory.ATEMOS_FACTORY),
        DTAAS_ADMIN("DTAAS 운영자",
                "DTAAS1",
                "admin@dtaas.com",
                "01098765151",
                "Atemos1234!",
                EmployeeRole.ADMIN,
                Factory.DTAAS_FACTORY),
        DTAAS_MANAGER("DTAAS 관리자",
                "DTAAS2",
                "manager@dtaas.com",
                "01098764242",
                "Atemos1234!",
                EmployeeRole.MANAGER,
                Factory.DTAAS_FACTORY),
        DTAAS_WORKER("DTAAS 근로자",
                "DTAAS3",
                "worker@dtaas.com",
                "01098769797",
                "Atemos1234!",
                EmployeeRole.WORKER,
                Factory.DTAAS_FACTORY);

        private final String name;
        private final String employeeNumber;
        private final String email;
        private final String phoneNumber;
        private final String password;
        private final EmployeeRole role;
        private final Factory factory;
    }
}