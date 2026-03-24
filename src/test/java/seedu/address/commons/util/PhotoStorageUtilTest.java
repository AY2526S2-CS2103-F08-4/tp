package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        String tempDirPath = testFolder.toString().replace("\\", "/") + "/";
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
        Photo imageFile = new Photo(sourceFile.toString().replace("\\", "/"));

        // Simulate copy to testFolder
        Photo result = PhotoStorageUtil.copyPhotoToDirectory(imageFile);

        // Assert
        assertNotEquals(imageFile.getPath(), result.getPath()); // raw path (from user) vs uuid encoded path (to test)
        assertTrue(result.isSavedLocally());
        assertTrue(Files.exists(Paths.get(result.getPath())));

    }

    @Test
    public void copyPhotoToDirectory_nonExistentFile_throwIoException() throws IOException {
        Path sourceFile = userFolder.resolve("does_not_exist.jpg");
        Photo imageFile = new Photo(sourceFile.toString().replace("\\", "/"));
        assertThrows(IOException.class, () -> PhotoStorageUtil.copyPhotoToDirectory(imageFile));
    }

    @Test
    public void deletePhoto_validUserPhoto_success() throws IOException {
        Path sourceFile = testFolder.resolve("mock-uuid-1234.jpg");
        Files.createFile(sourceFile);
        Photo imageFile = new Photo(sourceFile.toString().replace("\\", "/"));

        assertTrue(Files.exists(sourceFile));
        assertTrue(imageFile.isSavedLocally());

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
        Photo dummyPhoto = new Photo(dummyDir.toString().replace("\\", "/"));

        assertTrue(dummyPhoto.isSavedLocally());
        assertTrue(Files.exists(dummyDir));

        assertThrows(IOException.class, () -> PhotoStorageUtil.deletePhoto(dummyPhoto));
    }

    @Test
    public void deletePhoto_userPhotoNotSavedLocally_throwIoException() throws IOException {
        Path dummyFile = userFolder.resolve("do_not_delete_me.jpg");
        Files.createDirectory(dummyFile);
        Photo dummyPhoto = new Photo(dummyFile.toString().replace("\\", "/"));

        assertFalse(dummyPhoto.isSavedLocally());
        assertTrue(Files.exists(dummyFile));

        PhotoStorageUtil.deletePhoto(dummyPhoto);
        assertTrue(Files.exists(dummyFile));
    }

    @Test
    public void copyPhotoToDirectory_alreadySavedPhoto_returnsSamePhoto() throws IOException {
        Photo localPhoto = new Photo(testFolder.resolve("existing-uuid.jpg")
                        .toString().replace("\\", "/"));
        Photo result = PhotoStorageUtil.copyPhotoToDirectory(localPhoto);

        // 3. Assert that it bypassed the copy logic and returned the exact same object
        assertEquals(localPhoto.getPath(), result.getPath());
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
    public void clearDirectory_deletionFail_throwIOException() throws IOException {
        Path dummyFile = testFolder.resolve("cannot_delete_me.jpg");
        Files.createFile(dummyFile);

        // Temporarily change permissions
        testFolder.toFile().setReadable(false);
        testFolder.toFile().setWritable(false);
        testFolder.toFile().setExecutable(false);

        // Temporarily open a stream to file, so cannot delete
        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(dummyFile.toFile())) {
            assertThrows(IOException.class, () -> PhotoStorageUtil.clearDirectory());
        } finally {
            testFolder.toFile().setReadable(true);
            testFolder.toFile().setWritable(true);
            testFolder.toFile().setExecutable(true);
        }
    }
}
