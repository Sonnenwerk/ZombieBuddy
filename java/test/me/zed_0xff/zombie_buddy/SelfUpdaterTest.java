package me.zed_0xff.zombie_buddy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.CALLS_REAL_METHODS;

public class SelfUpdaterTest {

    @Test
    void performUpdate_nullCurrentJarPath_doesNothing() {
        // Just verify it doesn't throw
        assertDoesNotThrow(() -> SelfUpdater.performUpdate(null, Path.of("dummy"), "1.0"));
        assertEquals("1.0", SelfUpdater.getNewVersion());
    }

    @Test
    void performUpdate_success(@TempDir Path tempDir) throws Exception {
        Path currentJar = tempDir.resolve("current.jar");
        Path newJar = tempDir.resolve("new.jar");

        Files.writeString(currentJar, "current content");
        Files.writeString(newJar, "new content");

        SelfUpdater.performUpdate(currentJar, newJar, "2.0");

        assertEquals("2.0", SelfUpdater.getNewVersion());

        // Assert backup was created and has original content
        Path backupPath = currentJar.resolveSibling("current.jar.bak");
        assertTrue(Files.exists(backupPath), "Backup file should exist");
        assertEquals("current content", Files.readString(backupPath));

        // Assert currentJar was replaced
        assertTrue(Files.exists(currentJar), "Current jar should exist");
        assertEquals("new content", Files.readString(currentJar));

        // Assert no .new file was created
        Path deferredPath = currentJar.resolveSibling("current.jar.new");
        assertFalse(Files.exists(deferredPath), "Deferred update file should not exist on success");
    }

    @Test
    void performUpdate_lockedJarCreatesNewFile(@TempDir Path tempDir) throws Exception {
        Path currentJar = tempDir.resolve("locked.jar");
        Path newJar = tempDir.resolve("new.jar");

        Files.writeString(currentJar, "current content");
        Files.writeString(newJar, "new content");

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class, CALLS_REAL_METHODS)) {
            // Mock Files.move to throw IOException to simulate locked file for backup
            mockedFiles.when(() -> Files.move(eq(currentJar), any(Path.class), eq(StandardCopyOption.REPLACE_EXISTING)))
                    .thenThrow(new IOException("Simulated lock during move"));

            // Mock Files.copy from newJar to currentJar to throw IOException to simulate locked file
            mockedFiles.when(() -> Files.copy(eq(newJar), eq(currentJar), eq(StandardCopyOption.REPLACE_EXISTING)))
                    .thenThrow(new IOException("Simulated lock during copy"));

            // Real copy for the .new file
            mockedFiles.when(() -> Files.copy(eq(newJar), any(Path.class), eq(StandardCopyOption.REPLACE_EXISTING)))
                    .thenCallRealMethod();

            SelfUpdater.performUpdate(currentJar, newJar, "3.0");

            assertEquals("3.0", SelfUpdater.getNewVersion());

            Path deferredPath = currentJar.resolveSibling("locked.jar.new");
            assertTrue(Files.exists(deferredPath), "Deferred update file should be created if JAR is locked");
            assertEquals("new content", Files.readString(deferredPath));

            Path backupPath = currentJar.resolveSibling("locked.jar.bak");
            assertFalse(Files.exists(backupPath), "Backup should not be created if move fails");
        }
    }
}
