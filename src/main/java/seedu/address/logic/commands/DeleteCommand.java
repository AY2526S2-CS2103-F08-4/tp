package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import seedu.address.commons.util.CommandUtil;
import seedu.address.commons.util.PhotoStorageUtil;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonInformation;

/**
 * Deletes a person identified using it's displayed name from the address book.
 */
public class DeleteCommand extends Command {

    public static final String COMMAND_WORD = "delete";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes the person identified by their name.\n"
            + "Parameters: "
            + PREFIX_NAME + "NAME "
            + "[" + PREFIX_PHONE + "PHONE_NUMBER] "
            + "[" + PREFIX_EMAIL + "EMAIL] "
            + "[" + PREFIX_ADDRESS + "ADDRESS] "
            + "[" + PREFIX_TAG + "TAG]...\n"
            + "Example 1 (Unique name): " + COMMAND_WORD + " " + PREFIX_NAME + "John Doe\n"
            + "Example 2 (Multiple matches): " + COMMAND_WORD + " "
            + PREFIX_NAME + "John Doe "
            + PREFIX_TAG + "CS2103";

    public static final String MESSAGE_DELETE_PERSON_SUCCESS = "Deleted Person: %1$s";


    private final PersonInformation targetInfo;
    private final String targetDirectory;

    /**
     * Creates a {@code DeleteCommand} that targets contacts matching the provided information.
     *
     * @param targetInfo matching criteria with required name and optional fields
     */
    public DeleteCommand(PersonInformation targetInfo) {
        requireNonNull(targetInfo);
        this.targetInfo = targetInfo;
        this.targetDirectory = PhotoStorageUtil.DEFAULT_IMAGE_DIR;
    }

    /**
     * Creates a DeleteCommand specifying a target directory (Used for testing).
     */
    public DeleteCommand(PersonInformation targetInfo, String targetDirectory) {
        requireNonNull(targetInfo);
        this.targetInfo = targetInfo;
        this.targetDirectory = targetDirectory;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Person personToDelete = CommandUtil.targetPerson(model, this.targetInfo);
        if (personToDelete.getPhoto().isPresent()) {
            CommandUtil.safelyDeletePhoto(model, personToDelete, personToDelete.getPhoto().get(),
                    this.targetDirectory);
        }

        model.deletePerson(personToDelete);
        model.showNoEvents();
        return new CommandResult(String.format(MESSAGE_DELETE_PERSON_SUCCESS, Messages.format(personToDelete)));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DeleteCommand otherDeleteCommand) {
            return targetInfo.equals(otherDeleteCommand.targetInfo);
        }
        return false;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetName", targetInfo.name)
                .toString();
    }
}
