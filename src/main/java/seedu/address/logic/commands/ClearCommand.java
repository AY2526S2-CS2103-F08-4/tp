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

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.setAddressBook(new AddressBook());
        try {
            PhotoStorageUtil.clearDirectory();
        } catch (IOException e) {
            logger.warning(Messages.MESSAGE_CLEAR_USER_IMAGE_FAIL + e.getMessage());
        }
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
