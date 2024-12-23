package atemos.eguard.api.domain;

/**
 * 작업의 상태를 나타내는 열거형입니다.
 */
public enum WorkStatus {
    /**
     * 작업이 대기 중인 상태입니다.
     * 작업이 아직 시작되지 않았습니다.
     */
    PENDING,
    /**
     * 작업이 진행 중인 상태입니다.
     * 작업이 현재 수행되고 있습니다.
     */
    IN_PROGRESS,
    /**
     * 작업이 완료된 상태입니다.
     * 작업이 성공적으로 끝났습니다.
     */
    COMPLETED,
    /**
     * 작업이 취소된 상태입니다.
     * 작업이 중단되거나 취소되었습니다.
     */
    CANCELLED
}