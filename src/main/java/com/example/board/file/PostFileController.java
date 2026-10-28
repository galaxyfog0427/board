package com.example.board.file;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/posts/files")
public class PostFileController {

    private final PostFileRepository postFileRepository;
    private final FileStore fileStore;

    public PostFileController(PostFileRepository postFileRepository, FileStore fileStore) {
        this.postFileRepository = postFileRepository;
        this.fileStore = fileStore;
    }

    @GetMapping("/{fileId}/view")
    public ResponseEntity<Resource> viewFile(@PathVariable Long fileId) {
        PostFile postFile = postFileRepository.findById(fileId);
        Resource resource = fileStore.load(postFile.getStoreFileName());
        return ResponseEntity.ok()
                .contentType(resolveMediaType(postFile.getStoreFileName()))
                .body(resource);
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
        PostFile postFile = postFileRepository.findById(fileId);
        Resource resource = fileStore.load(postFile.getStoreFileName());
        String encodedUploadFileName = UriUtils.encode(postFile.getUploadFileName(), StandardCharsets.UTF_8);
        String contentDisposition = "attachment; filename=\"" + encodedUploadFileName + "\"";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .contentType(resolveMediaType(postFile.getStoreFileName()))
                .body(resource);
    }

    private MediaType resolveMediaType(String storeFileName) {
        return MediaTypeFactory.getMediaType(storeFileName)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
    }

}
