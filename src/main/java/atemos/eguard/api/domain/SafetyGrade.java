package atemos.eguard.api.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SafetyGrade {
    GOOD("양호", "작업에 쾌적한 환경입니다.", 80, 100),
    CAUTION("주의", "작업에 주의를 요합니다.", 60, 80),
    SEVERE("심각", "사고가 잦습니다. 점검이 필요합니다.", 0, 60);

    private final String grade;
    private final String message;
    private final int min;
    private final int max;
}