package atemos.eguard.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    @Value("/${file.plan-file-dir}/")
    private String planFileDir;

    /**
     * 업로드할 파일을 서버에 저장합니다.
     *
     * @param file 업로드할 MultipartFile 객체.
     *             이 파일은 서버에 저장되며, 이후 데이터 추출 또는 해싱에 사용될 수 있습니다.
     */
    @Override
    public void upload(MultipartFile file) throws IOException {
        // 파일이 비어있는지 확인
        if (file.isEmpty()) {
            throw new IOException("업로드할 파일이 비어 있습니다.");
        }
        // 업로드 경로 설정 및 디렉토리 생성
        File uploadPath = new File(planFileDir);
        if (!uploadPath.exists() && !uploadPath.mkdirs()) {
            throw new IllegalStateException("업로드 경로를 생성할 수 없습니다: " + planFileDir);
        }
        // 파일 저장 경로 설정 및 파일 저장
        file.transferTo(new File(uploadPath, Objects.requireNonNull(file.getOriginalFilename())));
    }
}