package seedu.address.logic.commands;

import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.util.PhotoStorageUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;

public class ClearCommandTest {

    @Test
    public void execute_emptyAddressBook_success(@TempDir Path tempDir) throws IOException {
        Model model = new ModelManager();
        Model expectedModel = new ModelManager();

        Path testFolder = tempDir.resolve("data_images");
        Files.createDirectory(testFolder);
        String tempDirPath = PhotoStorageUtil.formatPath(testFolder);

        assertCommandSuccess(new ClearCommand(tempDirPath), model, ClearCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_nonEmptyAddressBook_success(@TempDir Path tempDir) throws IOException {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel.setAddressBook(new AddressBook());


        Path testFolder = tempDir.resolve("data_images");
        Files.createDirectory(testFolder);
        String tempDirPath = PhotoStorageUtil.formatPath(testFolder);

        assertCommandSuccess(new ClearCommand(tempDirPath), model, ClearCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_clearDirectoryFails_throwsIoException(@TempDir Path tempDir) throws IOException {
        Model model = new ModelManager();
        Model expectedModel = new ModelManager();

        // Create an isolated test folder inside the temp directory
        Path testFolder = tempDir.resolve("data_images");
        Files.createDirectory(testFolder);
        String tempDirPath = PhotoStorageUtil.formatPath(testFolder);

        Path dummyFile = testFolder.resolve("cannot_delete_me.jpg");
        Files.createFile(dummyFile);

        // Temporarily open a stream to file, so cannot delete
        try (FileOutputStream fs = new FileOutputStream(dummyFile.toFile())) {
            // Temporarily change permissions
            testFolder.toFile().setReadable(false);
            testFolder.toFile().setWritable(false);
            testFolder.toFile().setExecutable(false);

            try {
                assertCommandSuccess(new ClearCommand(tempDirPath), model, ClearCommand.MESSAGE_SUCCESS, expectedModel);
            } finally {
                testFolder.toFile().setReadable(true);
                testFolder.toFile().setWritable(true);
                testFolder.toFile().setExecutable(true);
            }
        }
    }

}
