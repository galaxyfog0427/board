package com.example.board.file;

import com.example.board.file.exception.StoredFileNotFoundException;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStore {

    UploadFile storeFile(MultipartFile multipartFile) throws IOException;

    /**
     * @throws StoredFileNotFoundException 저장소에 파일이 없을 때
     */
    Resource load(String storeFileName);

    void delete(String storeFileName);

}
