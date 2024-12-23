package atemos.eguard.api.service;

import atemos.eguard.api.config.EntityValidator;
import atemos.eguard.api.dto.AlarmDto;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Optional;

/**
 * 업체의 에너지 사용량과 요금 등의 데이터를 조회하고 엑셀로 제공하는 기능을 제공하는 서비스 구현 클래스입니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
    private final AlarmService alarmService;
    private final EntityValidator entityValidator;

    /**
     * 특정 기간 내 업체의 알람 상태 이력을 엑셀 파일로 제공합니다. (조회 결과는 시간별로 집계됩니다)
     *
     * @param readAlarmRequestDto 알람 상태 이력을 조회하기 위한 요청 DTO입니다.
     * @param response  HTTP 응답 객체입니다. 엑셀 파일을 클라이언트로 전송하기 위해 사용됩니다.
     */
    @Override
    @Transactional(readOnly = true)
    public void reportAlarm(AlarmDto.ReadAlarmRequest readAlarmRequestDto, HttpServletResponse response) {
        // readAlarmRequestDto에 알람 ID 리스트가 존재하면 이 정보에 현재 접속한 근로자가 접근할 수 있는지 검증
        Optional.ofNullable(readAlarmRequestDto.getAlarmIds())
                .filter(ids -> !ids.isEmpty())
                .ifPresent(alarmIds -> {
                    if (entityValidator.validateAlarmIds(alarmIds).isEmpty()) {
                        throw new AccessDeniedException("알람이 존재하지 않거나 조회 권한이 없습니다.");
                    }
                });
        // readAlarmRequestDto에 근로자 ID 리스트가 존재하면 이 정보에 현재 접속한 근로자가 접근할 수 있는지 검증
        Optional.ofNullable(readAlarmRequestDto.getEmployeeIds())
                .filter(ids -> !ids.isEmpty())
                .ifPresent(employeeIds -> {
                    if (entityValidator.validateEmployeeIds(employeeIds).isEmpty()) {
                        throw new AccessDeniedException("근로자가 존재하지 않거나 조회 권한이 없습니다.");
                    }
                });
        // readAlarmRequestDto에 사건 ID 리스트가 존재하면 이 정보에 현재 접속한 근로자가 접근할 수 있는지 검증
        Optional.ofNullable(readAlarmRequestDto.getEventIds())
                .filter(ids -> !ids.isEmpty())
                .ifPresent(eventIds -> {
                    if (entityValidator.validateEventIds(eventIds).isEmpty()) {
                        throw new AccessDeniedException("사건이 존재하지 않거나 조회 권한이 없습니다.");
                    }
                });
        // readAlarmRequestDto에 공장 ID 리스트가 존재하면 이 정보에 현재 접속한 근로자가 접근할 수 있는지 검증
        Optional.ofNullable(readAlarmRequestDto.getFactoryIds())
                .filter(ids -> !ids.isEmpty())
                .ifPresent(factoryIds -> {
                    if (entityValidator.validateFactoryIds(factoryIds).isEmpty()) {
                        throw new AccessDeniedException("공장이 존재하지 않거나 조회 권한이 없습니다.");
                    }
                });
        // readAlarmRequestDto에 업체 ID 리스트가 존재하면 이 정보에 현재 접속한 근로자가 접근할 수 있는지 검증
        Optional.ofNullable(readAlarmRequestDto.getCompanyIds())
                .filter(ids -> !ids.isEmpty())
                .ifPresent(companyIds -> {
                    if (entityValidator.validateCompanyIds(companyIds).isEmpty()) {
                        throw new AccessDeniedException("업체가 존재하지 않거나 조회 권한이 없습니다.");
                    }
                });
        // 종료일이 null인 경우 시작일과 동일하게 설정
        if (readAlarmRequestDto.getSearchEndTime() == null) {
            readAlarmRequestDto.setSearchEndTime(readAlarmRequestDto.getSearchStartTime());
        }
        // 알람 상태 이력 조회 (페이징 없이 전체 조회를 위해 Pageable.unpaged() 사용)
        var alarmHistory = alarmService.read(readAlarmRequestDto, Pageable.unpaged());
        // 데이터 시간순 정렬 (내림차순)
        var sortedAlarmHistoryList = alarmHistory.getAlarmList().stream()
                .sorted(Comparator.comparing(AlarmDto.ReadAlarmResponse::getCreatedAt).reversed())
                .toList();
        try (var workbook = new XSSFWorkbook()) {
            // 엑셀 시트 생성
            var sheet = workbook.createSheet(String.format("%s ~ %s",
                    readAlarmRequestDto.getSearchStartTime().toLocalDate(), readAlarmRequestDto.getSearchEndTime().toLocalDate()));
            sheet.setColumnWidth(0, 2 * 256);
            // 엑셀 타이틀 생성
            var titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(33);
            var titleCell = titleRow.createCell(1);
            var title = String.format("Alarm Report %s ~ %s",
                    readAlarmRequestDto.getSearchStartTime().toLocalDate(),
                    readAlarmRequestDto.getSearchEndTime().toLocalDate().equals(readAlarmRequestDto.getSearchStartTime().toLocalDate())
                            ? readAlarmRequestDto.getSearchStartTime().toLocalDate()
                            : readAlarmRequestDto.getSearchEndTime().toLocalDate());
            titleCell.setCellValue(title);
            var titleStyle = workbook.createCellStyle();
            var titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setColor(IndexedColors.WHITE.getIndex());
            titleFont.setFontHeightInPoints((short) 22);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            titleStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
            titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 1, 6));
            // 엑셀 헤더 생성
            var headerRow = sheet.createRow(1);
            var headers = new String[]{"수신자", "사건 발생 근로자", "사건 발생 구역", "알람 발생 시각", "메시지", "읽음 여부"};
            var headerStyle = workbook.createCellStyle();
            var headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            for (int i = 0; i < headers.length; i++) {
                var cell = headerRow.createCell(i + 1);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            // 셀 스타일 설정
            var dataStyle = workbook.createCellStyle();
            dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            dataStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
            // 엑셀 데이터 채우기
            var rowNum = 2;
            for (var alarm : sortedAlarmHistoryList) {
                var row = sheet.createRow(rowNum++);
                row.createCell(1).setCellValue(alarm.getEmployeeName());
                row.createCell(2).setCellValue(alarm.getEventEmployeeName());
                row.createCell(3).setCellValue(alarm.getEventAreaName());
                row.createCell(4).setCellValue(alarm.getCreatedAt().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                row.createCell(5).setCellValue(alarm.getAlarmMessage());
                row.createCell(6).setCellValue(alarm.getAlarmRead());
            }
            // 파일명 설정 및 인코딩
            var fileName = String.format("Alarm Report %s%s.xlsx",
                    readAlarmRequestDto.getSearchStartTime().toLocalDate(),
                    readAlarmRequestDto.getSearchStartTime().toLocalDate().equals(readAlarmRequestDto.getSearchEndTime().toLocalDate())
                            ? ""
                            : " ~ " + readAlarmRequestDto.getSearchEndTime().toLocalDate());
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
            workbook.write(response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            log.error("Error creating reportIotStatusHistory Excel file: {}", e.getMessage());
            throw new RuntimeException("Error creating Excel file", e);
        }
    }
}