package seedu.address.commons.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.Messages;
import seedu.address.model.person.Photo;

/**
* Utility class to handle the copying and storage of image files.
*/

public class PhotoStorageUtil {
    private static final Logger logger = LogsCenter.getLogger(PhotoStorageUtil.class);
    private static String imageDirectory = "data/images/";

    public static String getImageDirectory() {
        return imageDirectory;
    }

    public static void setImageDirectory(String directory) {
        imageDirectory = directory;
    }

    /**
     * Copies over a file specified by the user, to the data/images/ directory of NAB.
     * @param photo is the photo object specified by the user, it contains the raw file path.
     * @return a photo object that contains the encoded UUID file path for usage by NAB.
     */
    public static Photo copyPhotoToDirectory(Photo photo) throws IOException {
        // Check if the copying operation is needed
        if (photo.isSavedLocally()) {
            return photo;
        }

        // Returns a file object
        Path srcPath = Paths.get(photo.getPath());

        // Check existence of file and is regular file
        if (!Files.exists(srcPath) || !Files.isRegularFile(srcPath)) {
            throw new IOException("The specified image file cannot be found: " + srcPath);
        }

        // Check if data/images directory exists, otherwise create directory
        Path destDir = Paths.get(imageDirectory);
        if (!Files.exists(destDir)) {
            Files.createDirectories(destDir);
            logger.info("Created default image directory at: " + destDir.toAbsolutePath());
        }

        // Separate extension to preserve in UUID
        String fileName = srcPath.getFileName().toString();
        String fileExtension = "";
        int i = fileName.lastIndexOf(".");
        if (i > 0) {
            fileExtension = fileName.substring(i);
        }

        // Generate UUID using file name
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
        Path fullDestDir = destDir.resolve(uniqueFileName);
        logger.info("Copying photo from " + srcPath + " to " + fullDestDir);
        Files.copy(srcPath, fullDestDir, StandardCopyOption.REPLACE_EXISTING);

        // Update the photo saved in JSON
        String relativePath = (imageDirectory + uniqueFileName).replace("\\", "/");
        return new Photo(relativePath);
    }

    /**
     * Deletes a specified photo object from data/images.
     * @param photo is the photo object to be deleted.
     */
    public static void deletePhoto(Photo photo) throws IOException {
        // Do not delete photos outside /data/images
        if (!photo.isSavedLocally()) {
            return;
        }

        Path pathToDelete = Paths.get(photo.getPath());

        try {
            Files.deleteIfExists(pathToDelete);
        } catch (IOException e) {
            throw new IOException("The old image file cannot be deleted: " + pathToDelete);
        }
    }


    /**
     * Clears the entire data/images directory.
     */
    public static void clearDirectory() throws IOException {
        Path toBeDeleted = Paths.get(imageDirectory);

        if (!Files.exists(toBeDeleted)) {
            return;
        }

        try (java.util.stream.Stream<Path> paths = Files.walk(toBeDeleted)) {
            paths.sorted(java.util.Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            throw new java.io.UncheckedIOException(e);
                        }
                    });
        } catch (java.io.UncheckedIOException | IOException e) {
            throw new IOException(Messages.MESSAGE_DELETE_PHOTO_FAIL + e.getMessage());
        }
    }
}
