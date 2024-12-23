package atemos.eguard.api.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 발생한 사건의 유형을 나타내는 열거형입니다.
 */
@Getter
@AllArgsConstructor
public enum AreaIncident {
    GAS_LEAK("가스 누출", IncidentPriority.CRITICAL, "가스 누출이 발생했습니다. 즉각적인 조치가 필요합니다."),
    FIRE("화재", IncidentPriority.CRITICAL, "화재가 발생했습니다. 빠른 대응이 필요합니다."),
    ELECTRICAL_HAZARD("전기 위험", IncidentPriority.CRITICAL, "전기 위험이 있습니다. 근로자의 안전을 위해 즉각적인 조치가 필요합니다."),
    FLOOD("침수", IncidentPriority.CRITICAL, "침수가 발생했습니다. 작업 구역을 즉시 대피해야 합니다."),
    EVACUATION("대피", IncidentPriority.CRITICAL, "대피가 필요합니다. 긴급 상황입니다."),
    EQUIPMENT_FAILURE("장비 고장", IncidentPriority.ALERT, "장비가 고장났습니다. 수리가 필요합니다."),
    STRUCTURAL_DAMAGE("구조물 손상", IncidentPriority.ALERT, "구조물이 손상되었습니다. 보수 작업이 필요합니다."),
    AIR_QUALITY_ISSUE("공기 질 문제", IncidentPriority.ALERT, "공기 질이 나쁩니다. 근로자의 건강에 위험이 있을 수 있습니다."),
    MINOR_EQUIPMENT_ISSUE("경미한 장비 이상", IncidentPriority.WARNING, "경미한 장비 이상이 발생했습니다."),
    MINOR_WORKSPACE_INTRUSION("경미한 작업 공간 침해", IncidentPriority.WARNING, "작업 공간에 경미한 침해가 발생했습니다."),
    MINOR_ENERGY_CONSUMPTION_ISSUE("경미한 에너지 소비 문제", IncidentPriority.WARNING, "에너지가 비정상적으로 소비되고 있습니다."),
    NOISE_ISSUE("소음 문제", IncidentPriority.WARNING, "소음 문제가 발생했습니다."),
    MINOR_ENVIRONMENTAL_ISSUE("경미한 환경 문제", IncidentPriority.WARNING, "경미한 환경 문제가 발생했습니다."),
    NORMAL("정상", IncidentPriority.NORMAL, "정상 상태입니다.");

    private final String name;
    private final IncidentPriority priority;
    private final String message;
}