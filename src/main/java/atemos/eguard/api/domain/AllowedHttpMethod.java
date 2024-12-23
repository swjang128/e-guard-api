package atemos.eguard.api.domain;

/**
 * 열거형을 사용하여 허용된 HTTP 메서드를 정의하는 클래스입니다.
 * 이 열거형은 API 호출 시 사용할 수 있는 HTTP 메서드를 제한적으로 제공합니다.
 * GET, POST, PUT, PATCH, DELETE, OPTIONS 메서드만 허용됩니다.
 * 이 열거형은 API 요청 파라미터에서 HTTP 메서드 값을 제한된 값으로 변환하는 데 사용됩니다.
 */
public enum AllowedHttpMethod {
    /**
     * GET 메서드.
     * 주로 리소스를 조회할 때 사용됩니다.
     */
    GET,
    /**
     * POST 메서드.
     * 주로 리소스를 생성하거나 데이터를 전송할 때 사용됩니다.
     */
    POST,
    /**
     * PUT 메서드.
     * 기존 리소스를 대체하거나 수정할 때 사용됩니다.
     */
    PUT,
    /**
     * PATCH 메서드.
     * 기존 리소스의 일부만 수정할 때 사용됩니다.
     */
    PATCH,
    /**
     * DELETE 메서드.
     * 리소스를 삭제할 때 사용됩니다.
     */
    DELETE,
    /**
     * OPTIONS 메서드.
     * 특정 리소스에 대해 사용할 수 있는 메서드를 확인할 때 사용됩니다.
     */
    OPTIONS
}