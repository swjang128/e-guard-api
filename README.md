# E-Guard API

### **규칙**

모든 API 요청을 전송하는 기본 URL은 `${Server IP}:${Port}/eguard`입니다.

E-Guard는 페이지와 데이터베이스 리소스에 대한 `GET`, `POST`, `PUT`, `DELETE` 요청을 통해 대부분 작업을 수행하는 등 가능한 한 RESTful 규칙을 따릅니다. 기능에 따라 RESTful 규칙을 위배할 수 있습니다. 요청과 응답 본문은 JSON으로 인코딩됩니다.

**매핑 규칙**
- Context path 이름은 `kebab-case` 를 사용합니다.

**테이블 규칙**
- 속성 이름은 `snake_case` 를 사용합니다.

**JSON 규칙**
- 속성 이름은 `Camel Case` 를 사용합니다.
- 시간 값(날짜와 일시)은 [ISO 8601](https://ko.wikipedia.org/wiki/ISO_8601) 문자열로 인코딩됩니다. 일시는 시간 값(`2020-08-12T02:12:33.231Z`)을 포함하며, 날짜는 날짜(`2020-08-12`)만 포함합니다.
- E-Guard API는 빈 문자열을 지원하지 않습니다. 예를 들어, `url` [속성값 개체](https://developers.notion.com/reference/property-value-object)와 같은 속성의 문자열 값을 설정 해제하려면 `""` 대신 명시적인 `null`을 사용하세요.

---

## 프로젝트 실행 방법
별도로 첨부된 .env.{environment} 파일의 변수를 참조해주세요.

### 0. GitHub 소스 Clone
https://github.com/atemos01/e-guard-api.git
- *Branch*: `origin/develop`
  - git push는 백엔드 담당자에게 문의주세요.

### 1. 프로그램 설치
- Java 21
  - https://www.oracle.com/kr/java/technologies/downloads/#java21
- MySQL 8.0.37
  - https://dev.mysql.com/downloads/mysql/8.0.html
- IntelliJ Community
  - https://www.jetbrains.com/ko-kr/idea/download/download-thanks.html?platform=windows&code=IIC

### 2. 데이터베이스 생성
create database {DATABASE_NAME};

### 3. 환경설정 파일 세팅
위 설정 파일의 내용을 프로젝트 환경에 맞게 변경해주세요.

### 4. IntelliJ에서 스프링부트 메인 클래스 실행 설정
- 메인 클래스 EVerseApplication의 Edit Configurations.. 로 접근합니다.
- Environment variables 탭에서 파일을 선택합니다.
- 별도로 첨부된 .env.{environment} 파일을 선택합니다.
- 설정 Save 후 스프링부트 프로젝트를 실행합니다.

---

# 사용자 권한 별 API 호출 가능 목록

- **☑**: 권한이 있는 항목을 표시합니다.
- **Create**: 생성 권한
- **Read**: 조회 권한
- **Update**: 수정 권한
- **Delete**: 삭제 권한

## ADMIN 권한

| 기능 | Create | Read | Update | Delete |
| --- | --- | --- | --- | --- |
| 시스템 설정 관리 |  | ☑ | ☑ |  |
| 알람 관리 | ☑ | ☑ | ☑ | ☑ |
| 업체 관리 | ☑ | ☑ | ☑ | ☑ |
| 공장 관리 | ☑ | ☑ | ☑ | ☑ |
| 영역 관리 | ☑ | ☑ | ☑ | ☑ |
| 근로자 관리 | ☑ | ☑ | ☑ | ☑ |
| 사건 관리 | ☑ | ☑ | ☑ | ☑ |
| 로그 조회 |  | ☑ |  |  |
| 메뉴 관리 | ☑ | ☑ | ☑ | ☑ |
| 리포트 조회 |  | ☑ |  |  |
| 작업 관리 | ☑ | ☑ | ☑ | ☑ |

## MANAGER 권한(본인의 업체만)

| 기능 | Create | Read | Update | Delete |
| --- | --- | --- | --- | --- |
| 시스템 설정 관리 |  | ☑ |  |  |
| 알람 관리 | ☑ | ☑ | ☑ |  |
| 업체 관리 |  | ☑ | ☑ |  |
| 공장 관리 | ☑ | ☑ | ☑ |  |
| 영역 관리 | ☑ | ☑ | ☑ |  |
| 근로자 관리 | ☑ | ☑ | ☑ |  |
| 사건 관리 | ☑ | ☑ | ☑ |  |
| 메뉴 조회 |  | ☑ |  |  |
| 리포트 조회 |  | ☑ |  |  |
| 작업 관리 | ☑ | ☑ | ☑ |  |

## WORKER 권한(근로자 정보는 존재하지만 호출 권한은 없음)

| 기능 | Create | Read | Update | Delete |
| --- | --- | --- | --- | --- |
| 시스템 설정 관리 |  |  |  |  |
| 알람 관리 |  |  |  |  |
| 업체 관리 |  |  |  |  |
| 공장 관리 |  |  |  |  |
| 영역 관리 |  |  |  |  |
| 근로자 관리 |  |  |  |  |
| 사건 관리 |  |  |  |  |
| 메뉴 조회 |  |  |  |  |
| 리포트 조회 |  |  |  |  |
| 작업 관리 |  |  |  |  |

# 기능별 엔드포인트 및 기능 설명

## 시스템 설정(Setting)

### **시스템 설정 조회**(`/setting` - `GET`)

- **설명**: 다양한 조건에 따라 시스템 설정 데이터를 조회하는 API입니다.
- **권한**: `ADMIN`, `MANAGER`
- **파라미터**:
  - `settingId`: 시스템 설정 ID 리스트
  - `companyId`: 업체 ID 리스트
  - `twoFactorAuthenticationEnabled`: 2차 인증 여부
  - `twoFactorAuthenticationMethod`: 2차 인증 방법
- **반환**: 조건에 맞는 시스템 설정 목록

---

### **시스템 설정 수정**(`/setting/{settingId}` - `PATCH`)

- **설명**: 시스템 설정 정보를 수정합니다.
- **권한**: `ADMIN`
- **파라미터**:
  - `settingId` (시스템 설정 ID)
  - `AlarmDto.ReadAlarmRequest` (시스템 설정 변경 정보)
- **반환**: 수정한 시스템 설정 정보

## 알람(Alarm)

### **알람 등록 API** (`/alarm` - `POST`)

- **설명**: 특정 구역에서 발생한 사건에 대한 알람을 생성하는 API입니다.
- **권한**: `ADMIN`, `MANAGER`
- **파라미터**:
  - `AlarmDto.CreateAlarm` (알람 생성 요청 데이터)
- **반환**: 생성된 알람 정보

---

### **알람 조회 API** (`/alarm` - `GET`)

- **설명**: 다양한 조건에 따라 알람 데이터를 조회하는 API입니다.
- **권한**: `ADMIN`, `MANAGER`
- **파라미터**:
  - `alarmId`: 알람 ID 리스트
  - `employeeId`: 근로자 ID 리스트
  - `eventId`: 사건 ID 리스트
  - `factoryId`: 공장 ID 리스트
  - `companyId`: 업체 ID 리스트
  - `isRead`: 알람 읽음 여부
  - `employeeIncident`: 근로자에게 일어난 사건 목록
  - `areaIncident`: 구역에서 일어난 사건 목록
  - `searchStartTime`: 조회 시작일시
  - `searchEndTime`: 조회 종료일시
  - `page`: 페이지 번호 (페이징)
  - `size`: 페이지당 데이터 개수 (페이징)
- **반환**: 조건에 맞는 알람 목록과 페이징 정보

---

### **실시간 알람 스트림 API** (`/alarm/stream` - `GET`)

- **설명**: SSE(Server-Sent Events)를 사용하여 실시간 알람 스트리밍을 제공합니다.
- **권한**: `ADMIN`, `MANAGER`
- **파라미터**:
  - `AlarmDto.ReadAlarmRequest` (알람 조회 조건)
- **반환**: `SseEmitter` 객체로 실시간 알람 스트리밍 제공

---

### **알람 수정 API** (`/alarm/{alarmId}` - `PATCH`)

- **설명**: 기존 알람을 수정하는 API입니다.
- **권한**: `ADMIN`, `MANAGER`
- **파라미터**:
  - `alarmId`: 수정할 알람 ID
  - `AlarmDto.UpdateAlarm`: 수정할 알람 정보
- **반환**: 수정된 알람 정보

---

### **알람 삭제 API** (`/alarm/{alarmId}` - `DELETE`)

- **설명**: 특정 알람 ID를 기준으로 알람을 삭제하는 API입니다.
- **권한**: `ADMIN`
- **파라미터**: `alarmId` (삭제할 알람 ID)
- **반환**: 삭제 결과

---

### **모든 알람 읽음 처리 API** (`/alarm/read` - `PATCH`)

- **설명**: 모든 알람을 읽음 처리하는 API입니다.
- **권한**: `ADMIN`
- **반환**: 성공 응답

## 구역(Area)

### 구역 등록 API (`/area` - `POST`)

- **설명**: 새로운 구역 정보를 등록하는 API입니다.
- **권한**: `ADMIN`, `MANAGER`
- **파라미터**:
  - `AreaDto.CreateArea`: 구역 등록 요청 데이터
- **반환**: 등록된 구역 정보

---

### 구역 조회 API (`/area` - `GET`)

- **설명**: 여러 조건을 기반으로 구역 정보를 조회하는 API입니다.
- **권한**: `ADMIN`, `MANAGER`
- **파라미터**:
  - `areaId`: 구역 ID 리스트
  - `factoryId`: 공장 ID 리스트
  - `name`: 구역명
  - `location`: 주소
  - `page`: 페이지 번호 (페이징)
  - `size`: 페이지당 데이터 개수 (페이징)
- **반환**: 조건에 맞는 구역 정보

---

### 구역 수정 API (`/area/{areaId}` - `PATCH`)

- **설명**: 기존 구역 정보를 수정하는 API입니다.
- **권한**: `ADMIN`, `MANAGER`
- **파라미터**:
  - `areaId`: 수정할 구역 ID
  - `AreaDto.UpdateArea`: 수정할 구역 정보
- **반환**: 수정된 구역 정보

---

### 구역 삭제 API (`/area/{areaId}` - `DELETE`)

- **설명**: 지정된 ID의 구역을 삭제하는 API입니다.
- **권한**: `ADMIN`
- **파라미터**:
  - `areaId`: 삭제할 구역 ID
- **반환**: 삭제 결과

## 인증 및 인가(Authentication)

### **2차 인증 번호 발송 API** (`/auth/2fa` - `POST`)

- **설명**: 로그인 후 2차 인증을 위한 인증 번호를 이메일로 발송하는 API입니다.
- **권한**: `PUBLIC`
- **파라미터**:
  - `EmployeeDto.AuthCodeRequest` (2차 인증 번호 발송 요청 데이터)
- **반환**: 인증 번호 발송 결과 메시지

---

### **로그인 API** (`/auth/login` - `POST`)

- **설명**: 근로자 정보와 2차 인증 여부를 확인한 후 로그인을 처리하는 API입니다. 로그인 시 JWT Access Token과 Refresh Token을 제공합니다.
- **권한**: `PUBLIC`
- **파라미터**:
  - `EmployeeDto.LoginRequest` (로그인 요청 데이터)
- **반환**: 인증 성공 시 JWT Access Token과 Refresh Token을 포함한 응답

---

### **Access Token 재발급 API** (`/auth/renew` - `POST`)

- **설명**: 만료된 Access Token을 Refresh Token을 이용해 재발급하는 API입니다.
- **권한**: `PUBLIC` (Refresh Token 소지 시 사용 가능)
- **파라미터**:
  - `refreshToken` (클라이언트에서 제공된 Refresh Token)
- **반환**: 새로운 Access Token

---

### **현재 로그인한 근로자 정보 조회 API** (`/auth/info` - `GET`)

- **설명**: JWT 토큰을 이용하여 현재 로그인한 근로자 정보를 조회하는 API입니다.
- **권한**: `ADMIN`, `MANAGER`, `WORKER`
- **반환**: 현재 로그인한 근로자의 정보

---

### **현재 로그인한 근로자의 접근 권한 및 접근 가능한 메뉴 조회 API** (`/auth/authority` - `GET`)

- **설명**: JWT 토큰을 이용하여 현재 로그인한 근로자의 접근 권한 및 접근 가능한 메뉴를 조회하는 API입니다.
- **권한**: `ADMIN`, `MANAGER`, `WORKER`
- **반환**: 근로자의 접근 권한 및 접근 가능한 메뉴

---

### **로그아웃 API** (`/auth/logout` - `POST`)

- **설명**: 로그아웃 후 JWT 토큰을 블랙리스트에 추가하는 API입니다. 실제 로그아웃 처리는 `SecurityConfig`에 의해 `CustomLogoutHandler`에서 처리됩니다.
- **권한**: `ADMIN`, `MANAGER`, `WORKER`
- **반환**: 로그아웃 결과

---

### **비밀번호 초기화 API** (`/auth/reset-password` - `PATCH`)

- **설명**: 근로자의 비밀번호를 초기화하는 API입니다.
- **권한**: `PUBLIC`
- **파라미터**:
  - `EmployeeDto.ResetPassword` (비밀번호 초기화 요청 데이터)
- **반환**: 비밀번호 초기화 결과

---

### **비밀번호 변경 API** (`/auth/update-password` - `PATCH`)

- **설명**: 기존 계정 정보를 확인한 후 새로운 비밀번호로 변경하는 API입니다.
- **권한**: `PUBLIC`
- **파라미터**:
  - `EmployeeDto.UpdatePassword` (비밀번호 변경 요청 데이터)
- **반환**: 비밀번호 변경 결과

## 업체(Company)

### **업체 등록 API** (`/company` - `POST`)

- **설명**: 새로운 업체 정보를 등록하는 API입니다.
- **권한**: `ADMIN`
- **파라미터**:
  - `CompanyDto.CreateCompany`: 업체 등록 요청 데이터
- **반환**: 등록된 업체 정보

---

### **업체 조회 API** (`/company` - `GET`)

- **설명**: 여러 조건을 기반으로 업체 정보를 조회하는 API입니다.
- **권한**: `ADMIN`, `MANAGER`
- **파라미터**:
  - `companyId`: 업체 ID 리스트
  - `name`: 업체명
  - `phoneNumber`: 연락처
  - `address`: 주소
  - `page`: 페이지 번호 (페이징)
  - `size`: 페이지당 데이터 개수 (페이징)
- **반환**: 조건에 맞는 업체 정보

---

### **업체 수정 API** (`/company/{companyId}` - `PATCH`)

- **설명**: 기존 업체 정보를 수정하는 API입니다. 수정할 업체의 ID를 경로에 포함하고, 수정할 정보를 `CompanyDto.UpdateCompany` DTO를 통해 전달합니다.
- **권한**: `ADMIN`, `MANAGER`
- **파라미터**:
  - `companyId`: 수정할 업체 ID
  - `CompanyDto.UpdateCompany`: 수정할 업체 정보
- **반환**: 수정된 업체 정보

---

### **업체 삭제 API** (`/company/{companyId}` - `DELETE`)

- **설명**: 지정된 ID의 업체를 삭제하는 API입니다.
- **권한**: `ADMIN`
- **파라미터**:
  - `companyId`: 삭제할 업체 ID
- **반환**: 삭제 결과

---

### **현재 로그인한 근로자의 업체 정보 조회 API** (`/company/info` - `GET`)

- **설명**: JWT 토큰을 이용하여 현재 로그인한 근로자의 업체 정보를 조회하는 API입니다.
- **권한**: `ADMIN`, `MANAGER`
- **반환**: 현재 로그인한 근로자의 업체 정보

---

### **회원 가입 화면 노출 업체 목록 조회 API** (`/company/list` - `GET`)

- **설명**: 회원 가입 화면에서 노출되는 업체 목록을 조회하는 API입니다.
- **권한**: `PUBLIC`
- **반환**: 업체 목록

## 근로자(Employee)

### **근로자 등록 API** (`/employee` - `POST`)

- **설명**: 새로운 근로자를 등록하는 API입니다.
- **권한**: `PUBLIC`
- **파라미터**:
  - `EmployeeDto.CreateEmployee`: 근로자 등록 요청 데이터
- **반환**: 등록된 근로자 정보

---

### **근로자 조회 API** (`/employee` - `GET`)

- **설명**: 여러 조건을 기반으로 근로자 목록을 조회하는 API입니다.
- **권한**: `ADMIN`, `MANAGER`
- **파라미터**:
  - `employeeId`: 근로자 ID 리스트
  - `factoryId`: 공장 ID 리스트
  - `name`: 이름 (암호화되어 있어 Like 검색 불가)
  - `email`: 이메일 (암호화되어 있어 Like 검색 불가)
  - `phoneNumber`: 연락처 (암호화되어 있어 Like 검색 불가)
  - `authenticationStatus`: 계정 상태
  - `roles`: 권한 리스트
  - `page`: 페이지 번호 (페이징)
  - `size`: 페이지당 데이터 개수 (페이징)
  - `masking`: 마스킹 여부 (기본값: true)
- **반환**: 조건에 맞는 근로자 목록 및 페이징 정보

---

### **근로자 수정 API** (`/employee/{employeeId}` - `PATCH`)

- **설명**: 특정 근로자의 정보를 수정하는 API입니다. 관리자 또는 해당 근로자 본인이 수정할 수 있습니다.
- **권한**: `ADMIN`, `MANAGER`
- **파라미터**:
  - `employeeId`: 수정할 근로자 ID
  - `EmployeeDto.UpdateEmployee`: 수정할 근로자 정보
- **반환**: 수정된 근로자 정보

---

### **근로자 삭제 API** (`/employee/{employeeId}` - `DELETE`)

- **설명**: 지정된 ID의 근로자를 삭제하는 API입니다.
- **권한**: `ADMIN`
- **파라미터**:
  - `employeeId`: 삭제할 근로자 ID
- **반환**: 삭제 완료 응답

## 사건(Event)

### **사건 등록 API** (`/event` - `POST`)

- **설명**: 새로운 사건 정보를 등록하는 API입니다.
- **권한**: `ADMIN`, `MANAGER`
- **파라미터**:
  - `EventDto.CreateEvent`: 사건 등록 요청 데이터
- **반환**: 등록된 사건 정보

---

### **사건 조회 API** (`/event` - `GET`)

- **설명**: 여러 조건을 기반으로 사건 정보를 조회하는 API입니다.
- **권한**: `ADMIN`, `MANAGER`
- **파라미터**:
  - `eventId`: 사건 ID 리스트
  - `employeeId`: 근로자 ID 리스트
  - `areaId`: 구역 ID 리스트
  - `type`: 발생한 사건 유형 리스트
  - `resolved`: 사건 해결 여부
  - `searchStartDate`: 조회 시작일 (createdAt 기준)
  - `searchEndDate`: 조회 종료일 (createdAt 기준)
  - `page`: 페이지 번호 (페이징)
  - `size`: 페이지당 데이터 개수 (페이징)
- **반환**: 조건에 맞는 사건 정보 및 페이징 정보

---

### **사건 수정 API** (`/event/{eventId}` - `PATCH`)

- **설명**: 기존 사건 정보를 수정하는 API입니다.
- **권한**: `ADMIN`, `MANAGER`
- **파라미터**:
  - `eventId`: 수정할 사건 ID
  - `EventDto.UpdateEvent`: 수정할 사건 정보
- **반환**: 수정된 사건 정보

---

### **사건 삭제 API** (`/event/{eventId}` - `DELETE`)

- **설명**: 지정된 ID의 사건을 삭제하는 API입니다.
- **권한**: `ADMIN`
- **파라미터**:
  - `eventId`: 삭제할 사건 ID
- **반환**: 삭제 결과

---

### **모든 사건 일괄 해결 API** (`/event/resolve` - `PATCH`)

- **설명**: 모든 사건을 해결 상태로 일괄 수정하는 API입니다.
- **권한**: `ADMIN`
- **반환**: 성공 응답

## 공장(Factory)

### **공장 등록 API** (`/factory` - `POST`)

- **설명**: 새로운 공장 정보를 등록하는 API입니다.
- **권한**: `ADMIN`, `MANAGER`
- **파라미터**:
  - `FactoryDto.CreateFactory`: 공장 등록 요청 데이터
- **반환**: 등록된 공장 정보

---

### **공장 조회 API** (`/factory` - `GET`)

- **설명**: 여러 조건을 기반으로 공장 정보를 조회하는 API입니다.
- **권한**: `ADMIN`, `MANAGER`
- **파라미터**:
  - `companyId`: 업체 ID 리스트
  - `factoryId`: 공장 ID 리스트
  - `name`: 공장명
  - `address`: 주소
  - `fileName`: 공장의 도면 파일명
  - `page`: 페이지 번호 (페이징)
  - `size`: 페이지당 데이터 개수 (페이징)
- **반환**: 조건에 맞는 공장 정보

---

### **공장 수정 API** (`/factory/{factoryId}` - `PATCH`)

- **설명**: 기존 공장 정보를 수정하는 API입니다.
- **권한**: `ADMIN`, `MANAGER`
- **파라미터**:
  - `factoryId`: 수정할 공장 ID
  - `FactoryDto.UpdateFactory`: 수정할 공장 정보
- **반환**: 수정된 공장 정보

---

### **공장 삭제 API** (`/factory/{factoryId}` - `DELETE`)

- **설명**: 지정된 ID의 공장을 삭제하는 API입니다.
- **권한**: `ADMIN`
- **파라미터**:
  - `factoryId`: 삭제할 공장 ID
- **반환**: 삭제 결과

---

### **현재 로그인한 근로자의 공장 정보 조회 API** (`/factory/info` - `GET`)

- **설명**: JWT 토큰을 이용하여 현재 로그인한 근로자의 공장 정보를 조회하는 API입니다.
- **권한**: `ADMIN`, `MANAGER`
- **반환**: 현재 로그인한 근로자의 공장 정보

---

### **회원 가입 화면 노출 공장 목록 조회 API** (`/factory/list` - `GET`)

- **설명**: 회원 가입 화면에서 노출되는 공장 목록을 조회하는 API입니다.
- **권한**: `PUBLIC`
- **파라미터**:
  - `companyIds`: 업체 ID 리스트
- **반환**: 업체에 속한 공장 목록

### **특정 공장의 요약 정보 조회 API** (`/factory/summary/{factoryId}` - `GET`)

- **설명**: 공장 ID를 기반으로 근로자의 전체 수, 건강 상태별 인원 수, 휴가 중인 인원 수를 포함한 요약 정보를 반환합니다.
- **권한**: `ADMIN`, `MANAGER`
- **반환**: 공장의 요약 정보

## 로그(Log)

### **API 호출 로그 조회 API** (`/log/api` - `GET`)

- **설명**: 다양한 조건을 기반으로 API 호출 로그 정보를 조회하는 API입니다.
- **권한**: `ADMIN`
- **파라미터**:
  - `apiCPUBLICLogId`: API 호출 로그 ID 리스트
  - `companyId`: 업체 ID 리스트
  - `employeeId`: 근로자 ID 리스트
  - `httpMethod`: HTTP 메서드 (GET, POST 등)
  - `requestUri`: 엔드포인트 URI
  - `clientIp`: 요청 IP
  - `statusCode`: HTTP 상태 코드
  - `searchStartTime`: 조회 시작일시
  - `searchEndTime`: 조회 종료일시
  - `page`: 페이지 번호 (페이징)
  - `size`: 페이지당 데이터 개수 (페이징)
- **반환**: 조건에 맞는 API 호출 로그 정보

---

### **인증/인가 로그 조회 API** (`/log/auth` - `GET`)

- **설명**: 다양한 조건을 기반으로 인증 및 인가 로그 정보를 조회하는 API입니다.
- **권한**: `ADMIN`
- **파라미터**:
  - `authenticationLogId`: 인증/인가 로그 ID 리스트
  - `companyId`: 업체 ID 리스트
  - `employeeId`: 근로자 ID 리스트
  - `httpMethod`: HTTP 메서드 (GET, POST 등)
  - `requestUri`: 엔드포인트 URI
  - `clientIp`: 요청 IP
  - `statusCode`: HTTP 상태 코드
  - `searchStartTime`: 조회 시작일시
  - `searchEndTime`: 조회 종료일시
  - `page`: 페이지 번호 (페이징)
  - `size`: 페이지당 데이터 개수 (페이징)
- **반환**: 조건에 맞는 인증/인가 로그 정보

## 메뉴(Menu)

### **메뉴 등록 API** (`/menu` - `POST`)

- **설명**: 새로운 메뉴를 등록하는 API입니다. 사용자는 `MenuDto.CreateMenu` DTO를 통해 메뉴를 등록할 수 있습니다.
- **권한**: `ADMIN`
- **파라미터**:
  - `MenuDto.CreateMenu`: 메뉴 등록 요청 데이터
- **반환**: 등록된 메뉴 정보

---

### **메뉴 조회 API** (`/menu` - `GET`)

- **설명**: 여러 조건을 기반으로 메뉴 정보를 조회하는 API입니다.
- **권한**: `ADMIN`, `MANAGER`
- **파라미터**:
  - `menuId`: 메뉴 ID 리스트
  - `name`: 메뉴 이름
  - `url`: 메뉴 URL
  - `description`: 메뉴 설명
  - `available`: 메뉴 사용 여부
  - `parentId`: 상위 메뉴 ID 리스트
- **반환**: 조건에 맞는 메뉴 정보

---

### **메뉴 수정 API** (`/menu/{menuId}` - `PATCH`)

- **설명**: 기존 메뉴 정보를 수정하는 API입니다. 수정할 메뉴의 ID를 경로에 포함하고, 수정할 정보를 `MenuDto.UpdateMenu` DTO를 통해 전달합니다.
- **권한**: `ADMIN`
- **파라미터**:
  - `menuId`: 수정할 메뉴 ID
  - `MenuDto.UpdateMenu`: 수정할 메뉴 정보
- **반환**: 수정된 메뉴 정보

---

### **메뉴 삭제 API** (`/menu/{menuId}` - `DELETE`)

- **설명**: 지정된 ID의 메뉴를 삭제하는 API입니다.
- **권한**: `ADMIN`
- **파라미터**:
  - `menuId`: 삭제할 메뉴 ID
- **반환**: 삭제 완료 응답

## 리포트(Report)

### **알람 이력 엑셀 리포트 다운로드 API** (`/report/alarm` - `GET`)

- **설명**: 특정 업체의 특정 기간 내 알람 이력을 엑셀 파일로 다운로드하는 API입니다. 데이터를 시간별로 집계하여 제공합니다.
- **권한**: `ADMIN`, `MANAGER`
- **파라미터**:
  - `companyId`: 업체 ID 리스트 (선택)
  - `searchStartDate`: 조회 시작 날짜 (필수)
  - `searchEndDate`: 조회 종료 날짜 (선택)
- **반환**: 엑셀 파일로 제공되는 알람 이력 데이터 (HTTP 응답 객체를 통해 전송)

## 작업(Work)

### 작업 등록 API (`/work` - `POST`)

- **설명**: 새로운 작업 정보를 등록하는 API입니다.
- **권한**: `ADMIN`, `MANAGER`
- **파라미터**:
  - `WorkDto.CreateWork`: 작업 등록 요청 데이터
- **반환**: 등록된 작업 정보

---

### 작업 조회 API (`/work` - `GET`)

- **설명**: 여러 조건을 기반으로 작업 정보를 조회하는 API입니다.
- **권한**: `ADMIN`, `MANAGER`
- **파라미터**:
  - `workId`: 작업 ID 리스트
  - `areaId`: 작업이 이루어지는 구역 ID 리스트
  - `employeeId`: 작업에 투입된 근로자 ID 리스트
  - `name`: 작업명
  - `status`: 작업 상태 리스트
  - `page`: 페이지 번호 (페이징)
  - `size`: 페이지당 데이터 개수 (페이징)
- **반환**: 조건에 맞는 작업 목록과 페이징 정보

---

### 작업 수정 API (`/work/{workId}` - `PATCH`)

- **설명**: 기존 작업 정보를 수정하는 API입니다.
- **권한**: `ADMIN`, `MANAGER`
- **파라미터**:
  - `workId`: 수정할 작업 ID
  - `WorkDto.UpdateWork`: 수정할 작업 정보
- **반환**: 수정된 작업 정보

---

### 작업 삭제 API (`/work/{workId}` - `DELETE`)

- **설명**: 지정된 ID의 작업을 삭제하는 API입니다.
- **권한**: `ADMIN`
- **파라미터**:
  - `workId`: 삭제할 작업 ID
- **반환**: 삭제 완료 응답

# 인증 및 인가 기능

| 기능 | 설명 |
| --- | --- |
| **로그인(2차 인증 절차 스킵 가능)** | 2차 인증이 필요한 사용자의 경우, 로그인 시 2차 인증을 스킵할 수 있습니다. |
| **2차 인증 번호 발송** | 로그인 후 2차 인증이 필요한 경우, 인증 번호를 이메일로 발송합니다. |
| **Access Token 재발급** | Access Token이 만료된 경우, 유효한 Refresh Token을 통해 재발급합니다. |
| **현재 로그인한 근로자 정보 조회** | 현재 로그인한 근로자의 상세 정보를 조회할 수 있습니다. |
| **현재 로그인한 근로자의 접근 권한 조회** | 근로자가 접근할 수 있는 메뉴와 권한을 조회할 수 있습니다. |
| **로그아웃** | 사용자 세션을 종료하고 로그아웃합니다. |
| **비밀번호 초기화** | 비밀번호를 잊은 경우, 이메일을 통해 초기화할 수 있습니다. |
| **비밀번호 변경** | 사용자가 기존 비밀번호를 새 비밀번호로 변경할 수 있습니다. |

# 배치 기능

## **오래된 데이터 삭제 기능**

- **주기**: 매일 자정에 실행
- **설명**: `BlacklistedToken` 및 `TwoFactorAuth` 테이블에서 오래된 데이터를 삭제합니다.

## **사건 등록**

- **주기**: 5분마다 실행
- **설명**: 특정 근로자 또는 구역에 발생한 사건을 시스템에 등록합니다.

## **알람 등록**

- **주기**: 5분 30초마다 실행
- **설명**: 특정 근로자 또는 구역에서 발생한 사건에 대해 관련된 사람들에게 알람을 발송합니다.