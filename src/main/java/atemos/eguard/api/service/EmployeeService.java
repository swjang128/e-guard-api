package atemos.eguard.api.service;

import atemos.eguard.api.dto.EmployeeDto;
import atemos.eguard.api.entity.Employee;
import org.springframework.data.domain.Pageable;

/**
 * EmployeeService는 작업자 등록, 조회, 수정, 삭제 기능을 제공하는 서비스 인터페이스입니다.
 *
 * 이 인터페이스는 작업자의 데이터베이스에 대한 CRUD 작업을 정의합니다.
 */
public interface EmployeeService {
    /**
     * 작업자를 등록합니다.
     *
     * @param createEmployeeDto 작업자를 생성하기 위한 정보가 담긴 데이터 전송 객체입니다.
     * @return 등록된 작업자 정보를 담고 있는 응답 객체입니다.
     */
    EmployeeDto.ReadEmployeeResponse create(EmployeeDto.CreateEmployee createEmployeeDto);
    /**
     * 조건에 맞는 작업자 목록을 조회합니다.
     *
     * @param readEmployeeRequestDto 작업자 조회 조건을 포함하는 데이터 전송 객체입니다.
     * @param pageable 페이징 정보를 포함하는 객체입니다.
     * @return 조회된 작업자 목록과 페이지 정보를 포함하는 맵 객체입니다.
     */
    EmployeeDto.ReadEmployeeResponseList read(EmployeeDto.ReadEmployeeRequest readEmployeeRequestDto, Pageable pageable);
    /**
     * 기존의 작업자를 수정합니다.
     *
     * @param employeeId 수정할 작업자의 ID입니다.
     * @param updateEmployeeDto 작업자 수정에 필요한 정보가 담긴 데이터 전송 객체입니다.
     * @return 등록된 작업자 정보를 담고 있는 응답 객체입니다.
     */
    EmployeeDto.ReadEmployeeResponse update(Long employeeId, EmployeeDto.UpdateEmployee updateEmployeeDto);
    /**
     * 작업자를 삭제합니다.
     *
     * @param employeeId 삭제할 작업자의 ID입니다.
     */
    void delete(Long employeeId);
    /**
     * 이메일 또는 전화번호가 중복되는지 확인하는 메서드입니다.
     * @param email 작업자 이메일
     * @param phone 작업자 전화번호
     */
    void checkDuplicateEmployee(String email, String phone);
    /**
     * 작업자 이메일로 작업자 정보를 로드합니다.
     *
     * @param email 작업자 이메일
     * @return 작업자 엔티티 객체
     */
    Employee readEmployeeByEmail(String email);
}