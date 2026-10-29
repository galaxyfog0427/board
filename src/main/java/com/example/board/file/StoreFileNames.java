package com.example.board.file;

import java.util.UUID;

public final class StoreFileNames {

    private StoreFileNames() {}

    public static String create(String originalFilename) {
        String ext = extractExt(originalFilename);
        String uuid = UUID.randomUUID().toString();
        if (ext.isBlank()) {
            return uuid;
        }
        return uuid + "." + ext;
    }

    private static String extractExt(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }

        int pos = originalFilename.lastIndexOf(".");

        if (pos == -1) {
            return "";
        }

        return originalFilename.substring(pos + 1);
    }

}
