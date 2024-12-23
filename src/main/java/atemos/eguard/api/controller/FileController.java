package atemos.eguard.api.controller;

import atemos.eguard.api.config.ApiResponseManager;
import atemos.eguard.api.dto.ApiResponseDto;
import atemos.eguard.api.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 파일을 업로드하고 업로드한 파일의 데이터에서 필요한 데이터만 추출하여 다운로드할 수 있는 API 컨트롤러.
 * 이 클래스는 파일 업로드와 업로드한 파일의 데이터에서 필요한 데이터를 추출하는 기능을 제공합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "파일 업로드 및 데이터 추출 API", description = "파일 업로드 및 데이터 추출 API 모음")
public class FileController {
    private final ApiResponseManager apiResponseManager;
    private final FileService fileService;

    /**
     * 업로드된 파일을 서버에 저장하는 API.
     *
     * @param file 업로드할 MultipartFile 객체. 파일의 크기 및 형식은 서비스에서 검증됩니다.
     * @return ResponseEntity<ApiResponseDto> 성공적으로 파일이 업로드된 경우 200 OK 상태와 함께 빈 응답을 반환합니다.
     */
    @Operation(summary = "파일 업로드", description = "서버에 파일을 업로드합니다.")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDto> uploadFile(
            @Parameter(description = "업로드할 파일", required = true) @RequestParam MultipartFile file
    ) throws IOException {
        fileService.upload(file);
        return apiResponseManager.ok();
    }
}