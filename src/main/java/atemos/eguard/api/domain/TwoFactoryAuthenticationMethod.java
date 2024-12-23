package atemos.eguard.api.domain;

/**
 * 이 열거형은 시스템에서 사용되는 2차 인증(2FA) 방법을 나타냅니다.
 * 두 가지 2차 인증 방법을 정의합니다:
 * - EMAIL: 이메일을 통해 2차 인증 코드를 전송
 * - SMS: SMS를 통해 2차 인증 코드를 전송
 * - KAKAO: 알림톡을 통해 2차 인증 코드를 전송
 * 2차 인증이 활성화된 경우, 이 열거형을 사용하여 시스템이 2FA 인증 코드를 어떤 방식으로
 * 전송할지 결정합니다.
 */
public enum TwoFactoryAuthenticationMethod {
    EMAIL,
    SMS,
    KAKAO
}