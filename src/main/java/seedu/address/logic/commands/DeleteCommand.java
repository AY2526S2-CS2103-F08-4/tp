package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.List;
import java.util.Set;

import seedu.address.commons.util.CommandUtil;
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
            + "[" + PREFIX_PHONE + "PHONE] "
            + "[" + PREFIX_EMAIL + "EMAIL] "
            + "[" + PREFIX_ADDRESS + "ADDRESS] "
            + "[" + PREFIX_TAG + "TAG]...\n"
            + "Example 1 (Unique name): " + COMMAND_WORD + " " + PREFIX_NAME + "John Doe\n"
            + "Example 2 (Multiple matches): " + COMMAND_WORD + " "
            + PREFIX_NAME + "John Doe "
            + PREFIX_TAG + "CS2103";

    public static final String MESSAGE_DELETE_PERSON_SUCCESS = "Deleted Person: %1$s";


    private final PersonInformation targetInfo;

    /**
     * Creates a {@code DeleteCommand} that targets contacts matching the provided information.
     *
     * @param targetInfo matching criteria with required name and optional fields
     */
    public DeleteCommand(PersonInformation targetInfo) {
        requireNonNull(targetInfo);
        this.targetInfo = targetInfo;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> listOfPersonToDelete = model.findPersons(this.targetInfo);

        // Scenario : No matching name
        if (listOfPersonToDelete.isEmpty()) {
            throw new CommandException(Messages.MESSAGE_NO_MATCH);
        }

        // Scenario : Multiple contact match name, duplicate handling, returns all matched contacts
        if (listOfPersonToDelete.size() > 1) {
            Set<Person> matchingPersons = Set.copyOf(listOfPersonToDelete);
            model.showMatchingPersons(matchingPersons);
            throw new CommandException(Messages.MESSAGE_MULTIPLE_MATCH);
        }

        Person personToDelete = listOfPersonToDelete.get(0);

        // Scenario : Only attempt to delete photo if isPresent and not shared by other contacts
        if (personToDelete.getPhoto().isPresent()) {
            CommandUtil.safelyDeletePhoto(model, personToDelete, personToDelete.getPhoto().get());
        }

        model.deletePerson(personToDelete);
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
