package atemos.eguard.api.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 특정 메서드의 로깅을 비활성화하는 애노테이션입니다.
 * 이 애노테이션은 메서드에 적용하여 해당 메서드의 실행을
 * api_call_log 테이블에 남기지 않도록 할 수 있습니다. 이는 로그에 민감한
 * 정보를 노출하는 것을 방지하거나, 로깅이 불필요한 경우에
 * 유용하게 사용될 수 있습니다.
 * 사용 예:
 * {@code
 * @NoLogging
 * public void 메서드() {
 *     // 메서드 구현
 * }
 * }
 *
 * 이 애노테이션은 런타임 동안 유지되며, 메서드에만 적용할 수 있습니다.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface NoLogging {
}