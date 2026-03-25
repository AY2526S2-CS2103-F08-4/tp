package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;

public class HelpStorageUtilTest {
    private static final Path HELP_DIR = Paths.get("data/help");

    @BeforeEach
    @AfterEach
    public void cleanup() throws IOException {
        HelpStorageUtil.clearDirectory();
    }

    @Test
    public void copyOverOfflineHelp_createsDirectory_success() {
        assertFalse(Files.exists(HELP_DIR));
        assertDoesNotThrow(() -> HelpStorageUtil.copyOverOfflineHelp());

        // Verify the directory was created
        assertTrue(Files.exists(HELP_DIR));
        assertTrue(Files.isDirectory(HELP_DIR));
    }

    @Test
    public void clearDirectory_existingDirectoryWithFiles_deleteSuccess() throws IOException {
        Files.createDirectories(HELP_DIR);
        Path dummyFile = HELP_DIR.resolve("dummy_test_file.txt");
        Files.createFile(dummyFile);
        assertTrue(Files.exists(dummyFile));
        assertTrue(Files.exists(HELP_DIR));

        HelpStorageUtil.clearDirectory();

        assertFalse(Files.exists(dummyFile), "The dummy file should be deleted");
        assertFalse(Files.exists(HELP_DIR), "The help directory should be deleted");
    }

    @Test
    public void clearDirectory_nonExistentDirectory_doesNothing() {
        assertFalse(Files.exists(HELP_DIR));
        assertDoesNotThrow(() -> HelpStorageUtil.clearDirectory());
    }

    @Test
    public void copyOverOfflineHelp_targetFileUnwritable_logsWarning() throws IOException {
        Files.createDirectories(HELP_DIR);
        File targetFile = HELP_DIR.resolve("index.html").toFile();
        targetFile.createNewFile();
        targetFile.setWritable(false);

        try {
            IOException thrown = assertThrows(IOException.class, () -> {
                HelpStorageUtil.copyFiles(List.of(targetFile.getPath()));
            });
            assertTrue(thrown.getMessage().contains(Messages.MESSAGE_FAILED_OFFLINE_GUIDE));
        } finally {
            targetFile.setWritable(true);
        }
    }

    @Test
    public void clearDirectory_directoryUnwritable_throwsAndLogsError() throws IOException {
        Files.createDirectories(HELP_DIR);
        Path dummyFile = HELP_DIR.resolve("test.txt");
        Files.createFile(dummyFile);

        // Remove W perms from directory
        File dirFile = HELP_DIR.toFile();
        File targetFile = dummyFile.toFile();
        dirFile.setWritable(false);
        targetFile.setWritable(false);
        try {
            IOException thrown = assertThrows(IOException.class, () -> {
                HelpStorageUtil.clearDirectory();
            });
            assertTrue(thrown.getMessage().contains(Messages.MESSAGE_FAILED_OFFLINE_GUIDE));
        } finally {
            dirFile.setWritable(true);
            targetFile.setWritable(true);
        }
    }

    @Test
    public void copyFiles_missingInternalFile_throwsIoException() {
        List<String> dummyFileList = List.of("this_is_a_fake_file_for_testing.txt");
        IOException thrown = assertThrows(IOException.class, () -> {
            HelpStorageUtil.copyFiles(dummyFileList);
        });
        assertTrue(thrown.getMessage().contains(Messages.MESSAGE_MISSING_INTERNAL_RESOURCE));
    }
}
