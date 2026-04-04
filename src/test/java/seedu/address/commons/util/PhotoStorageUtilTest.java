package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.person.Photo;

public class PhotoStorageUtilTest {
    @TempDir
    public Path sharedTempFolder; // Simulate data/

    private Path testFolder; // Simulate data_images
    private Path userFolder; // Simulate user_desktop

    private String originalDirectory;

    @BeforeEach
    public void setUp() throws IOException {
        testFolder = Files.createDirectory(sharedTempFolder.resolve("data_images"));
        userFolder = Files.createDirectory(sharedTempFolder.resolve("user_desktop"));

        originalDirectory = PhotoStorageUtil.getImageDirectory();
        String tempDirPath = PhotoStorageUtil.formatPath(testFolder);
        PhotoStorageUtil.setImageDirectory(tempDirPath);
    }

    @AfterEach
    public void tearDown() {
        PhotoStorageUtil.setImageDirectory(originalDirectory);
    }

    @Test
    public void copyPhotoToDirectory_validUserPhoto_success() throws IOException {
        // Create the image file in the "user_desktop"
        Path sourceFile = userFolder.resolve("test.jpg");
        Files.createFile(sourceFile);
        Photo imageFile = new Photo(PhotoStorageUtil.formatPath(sourceFile));

        // Simulate copy to testFolder
        Photo result = PhotoStorageUtil.copyPhotoToDirectory(imageFile);

        // Assert
        assertNotEquals(imageFile.getPath(), result.getPath()); // raw path (from user) vs uuid encoded path (to test)
        assertTrue(PhotoStorageUtil.isSavedLocally(result));
        assertTrue(Files.exists(Paths.get(result.getPath())));

    }

    @Test
    public void copyPhotoToDirectory_nonExistentFile_throwIoException() throws IOException {
        Path sourceFile = userFolder.resolve("does_not_exist.jpg");
        Photo imageFile = new Photo(PhotoStorageUtil.formatPath(sourceFile));
        assertThrows(IOException.class, () -> PhotoStorageUtil.copyPhotoToDirectory(imageFile));
    }

    @Test
    public void deletePhoto_validUserPhoto_success() throws IOException {
        Path sourceFile = testFolder.resolve("mock-uuid-1234.jpg");
        Files.createFile(sourceFile);
        Photo imageFile = new Photo(PhotoStorageUtil.formatPath(sourceFile));

        assertTrue(Files.exists(sourceFile));
        assertTrue(PhotoStorageUtil.isSavedLocally(imageFile));

        PhotoStorageUtil.deletePhoto(imageFile);
        assertFalse(Files.exists(sourceFile));
    }

    @Test
    public void deletePhoto_validUserPhoto_throwIoException() throws IOException {
        // Create a folder that resembles a .jpg
        Path dummyDir = testFolder.resolve("to_be_deleted.jpg");
        Files.createDirectory(dummyDir);

        // Put a dummy file within this folder, OS will fail to delete this
        Files.createFile(dummyDir.resolve("dummy.jpg"));
        Photo dummyPhoto = new Photo(PhotoStorageUtil.formatPath(dummyDir));

        assertTrue(PhotoStorageUtil.isSavedLocally(dummyPhoto));
        assertTrue(Files.exists(dummyDir));

        assertThrows(IOException.class, () -> PhotoStorageUtil.deletePhoto(dummyPhoto));
    }

    @Test
    public void deletePhoto_userPhotoNotSavedLocally_throwIoException() throws IOException {
        Path dummyFile = userFolder.resolve("do_not_delete_me.jpg");
        Files.createDirectory(dummyFile);
        Photo dummyPhoto = new Photo(PhotoStorageUtil.formatPath(dummyFile));

        assertFalse(PhotoStorageUtil.isSavedLocally(dummyPhoto));
        assertTrue(Files.exists(dummyFile));

        PhotoStorageUtil.deletePhoto(dummyPhoto);
        assertTrue(Files.exists(dummyFile));
    }

    @Test
    public void copyPhotoToDirectory_photoInsideManagedDirectory_throwsIoException() {
        Photo localPhoto = new Photo(PhotoStorageUtil.formatPath(testFolder.resolve("existing-uuid.jpg")));

        assertThrows(IOException.class, () -> PhotoStorageUtil.copyPhotoToDirectory(localPhoto));
    }

    @Test
    public void copyPhotoToDirectory_missingImageDirectory_createsDirectory() throws IOException {
        // Set the image directory to a non-existent directory first
        Path missingDir = sharedTempFolder.resolve("this_folder_does_not_exist");
        String missingDirPath = PhotoStorageUtil.formatPath(missingDir);
        PhotoStorageUtil.setImageDirectory(missingDirPath);

        assertFalse(Files.exists(missingDir));

        Path dummyFile = userFolder.resolve("do_not_delete_me.jpg");
        Files.createFile(dummyFile);
        Photo dummyPhoto = new Photo(PhotoStorageUtil.formatPath(dummyFile));

        PhotoStorageUtil.copyPhotoToDirectory(dummyPhoto);
        assertTrue(Files.exists(missingDir));
    }

    @Test
    public void clearDirectory_success() throws IOException {
        Path photoOne = testFolder.resolve("help.jpg");
        Files.createFile(photoOne);
        Path photoTwo = testFolder.resolve("me.jpg");
        Files.createFile(photoTwo);
        Path photoThree = testFolder.resolve("please.jpg");
        Files.createFile(photoThree);

        assertTrue(Files.exists(photoOne));
        assertTrue(Files.exists(photoTwo));
        assertTrue(Files.exists(photoThree));

        PhotoStorageUtil.clearDirectory();

        assertFalse(Files.exists(photoOne));
        assertFalse(Files.exists(photoTwo));
        assertFalse(Files.exists(photoThree));
    }

    @Test
    public void clearDirectory_deletionFail_throwIoException() throws IOException {
        Path dummyFile = testFolder.resolve("cannot_delete_me.jpg");
        Files.createFile(dummyFile);

        // Temporarily open a stream to file, so cannot delete
        try (FileOutputStream fs = new FileOutputStream(dummyFile.toFile())) {
            // Temporarily change permissions
            testFolder.toFile().setReadable(false);
            testFolder.toFile().setWritable(false);
            testFolder.toFile().setExecutable(false);

            try {
                assertThrows(IOException.class, () -> PhotoStorageUtil.clearDirectory());
            } finally {
                testFolder.toFile().setReadable(true);
                testFolder.toFile().setWritable(true);
                testFolder.toFile().setExecutable(true);
            }
        }
    }
}
