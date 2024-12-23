package atemos.eguard.api.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum IncidentPriority {
    CRITICAL("[긴급] "),
    ALERT("[주의] "),
    WARNING("[경고] "),
    NORMAL("[정상] ");

    private final String prefix;
}
