package com.jobbed.document;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import static org.assertj.core.api.Assertions.*;

class LocalFileStorageServiceTest {
    @TempDir Path temp;

    @Test void storesReadsAndDeletesInsideConfiguredRoot() throws Exception {
        var storage = new LocalFileStorageService(temp.toString());
        var stored = storage.store("safe".getBytes(), "pdf");
        assertThat(storage.open(stored.relativePath()).readAllBytes()).isEqualTo("safe".getBytes());
        storage.delete(stored.relativePath());
        assertThatThrownBy(() -> storage.open(stored.relativePath())).isInstanceOf(RuntimeException.class);
    }
    @Test void rejectsPathTraversal() {
        var storage = new LocalFileStorageService(temp.toString());
        assertThatThrownBy(() -> storage.open("../../secret.txt")).isInstanceOf(SecurityException.class);
    }
}
