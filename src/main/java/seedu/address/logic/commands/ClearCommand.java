package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.PhotoStorageUtil;
import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;

/**
 * Clears the address book.
 */
public class ClearCommand extends Command {

    public static final String COMMAND_WORD = "clear";
    public static final String MESSAGE_SUCCESS = "Address book has been cleared!";
    private static final Logger logger = LogsCenter.getLogger(ClearCommand.class);
    private final String targetDirectory;

    /**
     * Creates a ClearCommand using the default image directory.
     */
    public ClearCommand() {
        this.targetDirectory = PhotoStorageUtil.DEFAULT_IMAGE_DIR;
    }

    /**
     * Creates a ClearCommand specifying a target directory (Used for testing).
     */
    public ClearCommand(String targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.setAddressBook(new AddressBook());
        try {
            PhotoStorageUtil.clearDirectory(this.targetDirectory);
        } catch (IOException e) {
            logger.warning(Messages.MESSAGE_CLEAR_USER_IMAGE_FAIL + e.getMessage());
        }
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
