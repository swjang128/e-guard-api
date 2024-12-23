package atemos.eguard.api.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 파일과 관련된 서비스 인터페이스입니다.
 * 이 인터페이스는 파일 업로드, 데이터 추출, 파일 해싱 등의 기능을 제공합니다.
 */
public interface FileService {
    /**
     * 업로드할 파일을 서버에 저장합니다.
     *
     * @param file 업로드할 MultipartFile 객체.
     *             이 파일은 서버에 저장되며, 이후 데이터 추출 또는 해싱에 사용될 수 있습니다.
     */
    void upload(MultipartFile file) throws IOException;
}