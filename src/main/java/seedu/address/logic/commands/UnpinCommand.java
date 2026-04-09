package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.List;

import seedu.address.commons.util.CommandUtil;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonInformation;

/**
 * Unpins a person identified using their name (and optional unique identifiers).
 */
public class UnpinCommand extends Command {

    public static final String COMMAND_WORD = "unpin";

    public static final String MESSAGE_ALREADY_UNPINNED = "This contact is already unpinned.";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Unpins the person identified by their name.\n"
            + "Parameters: "
            + PREFIX_NAME + "NAME "
            + "[" + PREFIX_PHONE + "PHONE_NUMBER] "
            + "[" + PREFIX_EMAIL + "EMAIL] "
            + "[" + PREFIX_ADDRESS + "ADDRESS] "
            + "[" + PREFIX_TAG + "TAG]...\n"
            + "Example : " + COMMAND_WORD + " " + PREFIX_NAME + "John Doe "
            + PREFIX_PHONE + "91234567";

    public static final String MESSAGE_UNPIN_PERSON_SUCCESS = "Unpinned Person: %1$s";

    private final PersonInformation targetInfo;

    /**
     * Creates an {@code UnpinCommand} that targets contacts matching the provided information.
     *
     * @param targetInfo matching criteria with required name and optional fields
     */
    public UnpinCommand(PersonInformation targetInfo) {
        requireNonNull(targetInfo);
        this.targetInfo = targetInfo;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        // Resolve the target person first to allow for a specific pinned-state error.
        List<Person> matches = model.findPersons(this.targetInfo);
        Person personToUnpin = CommandUtil.targetPersonFromMatches(model, matches);

        if (!model.isPersonPinned(personToUnpin)) {
            throw new CommandException(MESSAGE_ALREADY_UNPINNED);
        }

        model.unpinPerson(personToUnpin);
        model.showAllPersonsPinnedFirst();
        return new CommandResult(String.format(MESSAGE_UNPIN_PERSON_SUCCESS, Messages.format(personToUnpin)));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UnpinCommand otherUnpinCommand) {
            return targetInfo.equals(otherUnpinCommand.targetInfo);
        }
        return false;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetName", targetInfo.getName())
                .toString();
    }

}
