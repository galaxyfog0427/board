package com.example.board.post;

import com.example.board.file.FileStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

import java.net.MalformedURLException;
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
    public ResponseEntity<Resource> viewFile(@PathVariable Long fileId) throws MalformedURLException {
        PostFile postFile = postFileRepository.findById(fileId);
        UrlResource resource = new UrlResource("file:" + fileStore.getFullPath(postFile.getStoreFileName()));
        return ResponseEntity.ok().body(resource);
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) throws MalformedURLException {
        PostFile postFile = postFileRepository.findById(fileId);
        UrlResource resource = new UrlResource("file:" + fileStore.getFullPath(postFile.getStoreFileName()));

        String encodedUploadFileName = UriUtils.encode(postFile.getUploadFileName(), StandardCharsets.UTF_8);
        String contentDisposition = "attachment; filename=\"" + encodedUploadFileName + "\"";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
    }

}
