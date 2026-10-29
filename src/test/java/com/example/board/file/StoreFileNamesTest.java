package com.example.board.file;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class StoreFileNamesTest {

    @Test
    @DisplayName("filename.png를 넣으면 UUID.png로 반환한다")
    void extensionIsKept() {
        String result = StoreFileNames.create("filename.png");
        String resultPart = result.substring(0, 36);

        assertThat(result).endsWith(".png");
        assertThatCode(() -> UUID.fromString(resultPart)).doesNotThrowAnyException();
    }

    @ParameterizedTest(name = "[{index}] 입력: {0}")
    @NullSource
    @ValueSource(strings = {"filename", "filename."})
    @DisplayName("확장자가 없으면 UUID만 반환한다")
    void noExtensionIsJustUUID(String originalFilename) {
        String result = StoreFileNames.create(originalFilename);

        assertThat(result).hasSize(36);
        assertThatCode(() -> UUID.fromString(result)).doesNotThrowAnyException();
    }


}