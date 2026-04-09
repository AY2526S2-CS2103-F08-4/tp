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
    public static final String DEFAULT_IMAGE_DIR = "data/images/";

    private static final Logger logger = LogsCenter.getLogger(PhotoStorageUtil.class);
    private static final String MESSAGE_MANAGED_DIRECTORY_SOURCE_NOT_ALLOWED =
            "Direct linking from managed image directory is not allowed."
            + " Please provide a source image outside data/images/.";
    /**
     * Returns true if the photo is already in the directory.
     */
    public static boolean isSavedLocally(Photo photo, String targetDirectory) {
        return photo.getPath().startsWith(targetDirectory);
    }

    /**
     * Returns a formatted path string that is cross-OS compatible.
     */
    public static String formatPath(Path path) {
        return path.toString().replace("\\", "/");
    }

    /**
     * Copies over a file specified by the user, to the data/images/ directory of NAB.
     * @param photo is the photo object specified by the user, it contains the raw file path.
     * @return a photo object that contains the encoded UUID file path for usage by NAB.
     */
    public static Photo copyPhotoToDirectory(Photo photo, String targetDirectory) throws IOException {
        // Define the srcPath to take from, and destDir to move to
        Path srcPath = Paths.get(photo.getPath());
        Path destDir = Paths.get(targetDirectory);

        // Checks relating to srcPath and image file
        validateSourcePath(srcPath, destDir);
        ensureDirectoryExists(destDir);

        String uniqueFileName = generateUniqueUuid(srcPath);
        Path fullDestDir = destDir.resolve(uniqueFileName);
        logger.info("Copying photo from " + srcPath + " to " + fullDestDir);
        Files.copy(srcPath, fullDestDir, StandardCopyOption.REPLACE_EXISTING);

        return createRelativePhoto(uniqueFileName, targetDirectory);
    }

    /**
     * Returns true if srcPath is within managedDirectoryPath
     * @param srcPath is the path of the user image to be copied.
     * @param managedDirectoryPath is the path of where the user image will be copied to, defaulted to data/images/.
     */
    private static boolean isPathWithinManagedDirectory(Path srcPath, Path managedDirectoryPath) {
        Path normalizedManagedDirectory = managedDirectoryPath.toAbsolutePath().normalize();
        Path normalizedSourcePath = srcPath.toAbsolutePath().normalize();
        return normalizedSourcePath.startsWith(normalizedManagedDirectory);
    }

    /**
     * Validates a given srcPath.
     * @param srcPath is the path of the user image to be copied.
     * @param destDir is the path of where the user image will be copied to, defaulted to data/images/.
     */
    private static void validateSourcePath(Path srcPath, Path destDir) throws IOException {
        // Check that the copy to copy from is not a photo within data/images
        if (isPathWithinManagedDirectory(srcPath, destDir)) {
            throw new IOException(MESSAGE_MANAGED_DIRECTORY_SOURCE_NOT_ALLOWED);
        }

        // Check existence of user specified file and is regular file
        if (!Files.exists(srcPath) || !Files.isRegularFile(srcPath)) {
            throw new IOException("The specified image file cannot be found: " + srcPath);
        }
    }

    /**
     * Creates destination directory for images if it does not already exist.
     * @param destDir is the directory to perform this check on.
     */
    private static void ensureDirectoryExists(Path destDir) throws IOException {
        if (!Files.exists(destDir)) {
            Files.createDirectories(destDir);
            logger.info("Created default image directory at: " + destDir.toAbsolutePath());
        }
    }

    /**
     * Generates a unique UUID filename while preserving original file extension.
     * @param srcPath is the path to the object to perform this method on.
     * @return a string that contains the UUID and the original file extension.
     */
    private static String generateUniqueUuid(Path srcPath) {
        // Separate extension to preserve in UUID
        String fileName = srcPath.getFileName().toString();
        String fileExtension = "";
        int i = fileName.lastIndexOf(".");
        if (i > 0) {
            fileExtension = fileName.substring(i);
        }

        // Generate UUID using file name
        return UUID.randomUUID().toString() + fileExtension;
    }

    /**
     * Creates a new Photo object with the relative path required for JSON storage.
     * @param uniqueFileName is the name given to the photo object.
     * @param targetDirectory is the directory to create the photo object in.
     * @return photo object that is tied to a person.
     */
    private static Photo createRelativePhoto(String uniqueFileName, String targetDirectory) {
        Path relativePath = Paths.get(targetDirectory, uniqueFileName);
        return new Photo(formatPath(relativePath));
    }

    /**
     * Deletes a specified photo object from the targetDirectory.
     * @param photo is the photo object to be deleted.
     * @param targetDirectory is the directory to delete the photo from.
     */
    public static void deletePhoto(Photo photo, String targetDirectory) throws IOException {
        // Do not delete photos outside targetDirectory
        if (!isSavedLocally(photo, targetDirectory)) {
            return;
        }

        Path pathToDelete = Paths.get(photo.getPath());

        try {
            Files.deleteIfExists(pathToDelete);
        } catch (IOException e) {
            throw new IOException("The old image file cannot be deleted: " + pathToDelete, e);
        }
    }

    /**
     * Clears the entire data/images directory.
     * @param targetDirectory is the directory to clear all images from.
     */
    public static void clearDirectory(String targetDirectory) throws IOException {
        Path toBeDeleted = Paths.get(targetDirectory);

        if (!Files.exists(toBeDeleted)) {
            return;
        }

        // 1. Collect all paths first to avoid Java/Windows locking the directory stream
        java.util.List<Path> pathsToDelete;
        try (java.util.stream.Stream<Path> paths = Files.walk(toBeDeleted)) {
            pathsToDelete = paths.sorted(java.util.Comparator.reverseOrder())
                    .filter(p -> !p.equals(toBeDeleted))
                    .collect(java.util.stream.Collectors.toList());
        }

        // 2. Safely delete them now that the stream is closed
        for (Path path : pathsToDelete) {
            try {
                Files.delete(path);
            } catch (IOException e) {
                throw new IOException(Messages.MESSAGE_DELETE_PHOTO_FAIL + e.getMessage(), e);
            }
        }
    }
}
