package com.example.board.file;

import com.example.board.file.exception.StoredFileNotFoundException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.io.IOException;

@Component
@ConditionalOnProperty(name = "board.file-store", havingValue = "s3")
public class S3FileStore implements FileStore {

    private final S3Client s3Client;
    private final String bucket;

    public S3FileStore(S3Client s3Client, @Value("${board.s3.bucket}") String bucket) {
        this.s3Client = s3Client;
        this.bucket = bucket;
    }

    @PostConstruct
    void verifyBucketExists() {
        s3Client.headBucket(b -> b.bucket(bucket));
    }

    @Override
    public UploadFile storeFile(MultipartFile multipartFile) throws IOException {
        if (multipartFile.isEmpty()) {
            return null;
        }

        String originalFilename = multipartFile.getOriginalFilename();
        String storeFilename = StoreFileNames.create(originalFilename);
        long fileSize = multipartFile.getSize();

        s3Client.putObject(
                b -> b.bucket(bucket).key(storeFilename),
                RequestBody.fromInputStream(multipartFile.getInputStream(), multipartFile.getSize()));

        return new UploadFile(originalFilename, storeFilename, fileSize);
    }

    @Override
    public void delete(String storeFileName) {
        s3Client.deleteObject(
                b -> b.bucket(bucket).key(storeFileName)
        );
    }

    @Override
    public Resource load(String storeFileName) {
        try {
            return new InputStreamResource(
                    s3Client.getObject(b -> b.bucket(bucket).key(storeFileName))
            );
        } catch (NoSuchKeyException e) {
            throw new StoredFileNotFoundException(storeFileName, e);
        }
    }
}
