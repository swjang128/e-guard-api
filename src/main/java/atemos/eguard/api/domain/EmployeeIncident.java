package atemos.eguard.api.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 발생한 사건의 유형을 나타내는 열거형입니다.
 */
@Getter
@AllArgsConstructor
public enum EmployeeIncident {
    INJURY("부상", IncidentPriority.CRITICAL, "근로자가 부상을 입었습니다. 즉각적인 조치가 필요합니다."),
    CRITICAL_HEALTH_ISSUE("중대한 건강 이상", IncidentPriority.CRITICAL, "근로자가 중대한 건강 이상을 겪고 있습니다. 긴급한 의료 조치가 필요합니다."),
    MINOR_HEALTH_ISSUE("경미한 건강 이상", IncidentPriority.ALERT, "근로자가 경미한 건강 이상을 겪고 있습니다."),
    ON_LEAVE("휴가 중", IncidentPriority.NORMAL, "근로자가 휴가 또는 병가 중입니다."),
    NORMAL("정상", IncidentPriority.NORMAL, "근로자가 정상 상태입니다.");

    private final String name;
    private final IncidentPriority priority;
    private final String message;
}