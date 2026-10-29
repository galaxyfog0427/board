package com.example.board.file;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
@ConditionalOnProperty(name = "board.file-store", havingValue = "local", matchIfMissing = true)
public class LocalFileStore implements FileStore {

    @Value("${file.dir}")
    private String fileDir;

    @Override
    public UploadFile storeFile(MultipartFile multipartFile) throws IOException {
        if (multipartFile.isEmpty()) {
            return null;
        }

        ensureDirectoryExists();

        String originalFilename = multipartFile.getOriginalFilename();
        String storeFileName = StoreFileNames.create(originalFilename);

        multipartFile.transferTo(new File(getFullPath(storeFileName)));

        return new UploadFile(originalFilename, storeFileName);
    }

    private String getFullPath(String storeFileName) {
        return new File(fileDir, storeFileName).getAbsolutePath();
    }

    @Override
    public Resource load(String storeFileName) {
        return new FileSystemResource(Path.of(fileDir, storeFileName));
    }

    private void ensureDirectoryExists() throws IOException {
        Files.createDirectories(Path.of(fileDir));
    }
}
