package atemos.eguard.api.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger 설정 클래스.
 * 이 클래스는 Swagger를 이용한 API 문서화를 위한 설정을 정의합니다.
 * JWT를 이용한 인증 방식을 지원하며, API 서버의 기본 URL을 설정합니다.
 */
@Configuration
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer",	bearerFormat = "JWT")
public class SwaggerConfig {
	/**
	 * OpenAPI 빈을 생성하는 메서드.
	 * 이 메서드는 API 문서의 기본 정보(title, version, description)와
	 * 인증 방식(JWT)을 설정합니다.
	 *
	 * @return OpenAPI 객체
	 */
	@Bean
	public OpenAPI EGuardAPI() {
		// API 정보 설정
		Info info = new Info()
				.title("E-Guard API")
				.version("v1")
				.description("E-Guard API Collection");
		// OpenAPI 객체 생성 및 설정
		return new OpenAPI()
				.addServersItem(new Server().url("/eguard"))
				.components(new Components().addSecuritySchemes("bearerAuth",
						new io.swagger.v3.oas.models.security.SecurityScheme()
								.type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")))
				.info(info)
				.addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
	}

	/**
	 * 전역 응답 코드를 추가하는 OpenApiCustomizer 빈 생성.
	 * 이 메서드는 모든 API 엔드포인트에 공통적인 응답 코드를 추가합니다.
	 *
	 * @return OpenApiCustomizer 객체
	 */
	@Bean
	public OpenApiCustomizer globalResponseCustomizer() {
		return openApi -> openApi.getPaths().values().forEach(pathItem -> pathItem.readOperations().forEach(operation -> {
			ApiResponses apiResponses = operation.getResponses();
			apiResponses.addApiResponse("200", createApiResponse("OK - 요청이 성공적으로 처리됨"));
			apiResponses.addApiResponse("400", createApiResponse("Bad Request - 잘못된 요청 파라미터"));
			apiResponses.addApiResponse("401", createApiResponse("Unauthorized - 인증이 필요함"));
			apiResponses.addApiResponse("403", createApiResponse("Forbidden - 접근이 거부됨"));
			apiResponses.addApiResponse("404", createApiResponse("Not Found - 요청한 리소스를 찾을 수 없음"));
			apiResponses.addApiResponse("500", createApiResponse("Internal Server Error - 서버 오류 발생"));
		}));
	}

	private ApiResponse createApiResponse(String description) {
		return new ApiResponse().description(description);
	}
}