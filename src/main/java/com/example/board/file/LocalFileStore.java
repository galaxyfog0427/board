package com.example.board.file;

import com.example.board.file.exception.StoredFileNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
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
        long fileSize = multipartFile.getSize();

        multipartFile.transferTo(new File(getFullPath(storeFileName)));

        return new UploadFile(originalFilename, storeFileName, fileSize);
    }

    private String getFullPath(String storeFileName) {
        return new File(fileDir, storeFileName).getAbsolutePath();
    }

    @Override
    public Resource load(String storeFileName) {
        Resource resource = new FileSystemResource(Path.of(fileDir, storeFileName));
        if (!resource.exists()) {
            throw new StoredFileNotFoundException(storeFileName);
        }
        return resource;
    }

    @Override
    public void delete(String storeFileName) {
        try {
            Files.deleteIfExists(Path.of(fileDir, storeFileName));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void ensureDirectoryExists() throws IOException {
        Files.createDirectories(Path.of(fileDir));
    }
}
