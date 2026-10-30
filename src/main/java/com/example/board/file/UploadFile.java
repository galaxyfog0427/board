package com.example.board.file;

public class UploadFile {

    private final String uploadFileName;
    private final String storeFileName;
    private final long fileSize;

    public UploadFile(String uploadFileName, String storeFileName, long fileSize) {
        this.uploadFileName = uploadFileName;
        this.storeFileName = storeFileName;
        this.fileSize = fileSize;
    }

    public String getUploadFileName() {
        return uploadFileName;
    }

    public String getStoreFileName() {
        return storeFileName;
    }

    public long getFileSize() {
        return fileSize;
    }
}
