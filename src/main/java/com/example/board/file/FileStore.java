package com.example.board.file;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStore {

    UploadFile storeFile(MultipartFile multipartFile) throws IOException;

    String getFullPath(String storeFileName);

}
