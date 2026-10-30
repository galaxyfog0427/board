package com.example.board.file.exception;

public class StoredFileNotFoundException extends RuntimeException {
    public StoredFileNotFoundException(String storeFileName) {
        super("저장소에 파일이 없습니다: " + storeFileName);
    }

    public StoredFileNotFoundException(String storeFileName, Throwable cause) {
        super("저장소에 파일이 없습니다: " + storeFileName, cause);
    }
}
