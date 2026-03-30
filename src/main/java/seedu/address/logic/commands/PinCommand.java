package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.List;
import java.util.Set;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonInformation;

/**
 * Pins a person identified using their name (and optional unique identifiers).
 * Pinned persons are shown first when the list command is used.
 */
public class PinCommand extends Command {

    public static final String COMMAND_WORD = "pin";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Pins the person identified by their name.\n"
            + "Parameters: "
            + PREFIX_NAME + "NAME "
            + "[" + PREFIX_PHONE + "PHONE] "
            + "[" + PREFIX_EMAIL + "EMAIL] "
            + "[" + PREFIX_ADDRESS + "ADDRESS] "
            + "[" + PREFIX_TAG + "TAG]...\n"
            + "Example 1 (Unique name): " + COMMAND_WORD + " " + PREFIX_NAME + "John Doe\n"
            + "Example 2 (Multiple matches): " + COMMAND_WORD + " "
            + PREFIX_NAME + "John Doe "
            + PREFIX_PHONE + "91234567";

    public static final String MESSAGE_PIN_PERSON_SUCCESS = "Pinned Person: %1$s";

    private final PersonInformation targetInfo;

    /**
     * Creates a {@code PinCommand} that targets contacts matching the provided information.
     *
     * @param targetInfo matching criteria with required name and optional fields
     */
    public PinCommand(PersonInformation targetInfo) {
        requireNonNull(targetInfo);
        this.targetInfo = targetInfo;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> personsToPin = model.findPersons(this.targetInfo);

        //Case 1: No matches found
        if (personsToPin.isEmpty()) {
            throw new CommandException(Messages.MESSAGE_NO_MATCH);
        }

        //Case 2: Multiple matches found
        if (personsToPin.size() > 1) {
            Set<Person> matchingPersons = Set.copyOf(personsToPin);
            model.showMatchingPersons(matchingPersons);
            throw new CommandException(Messages.MESSAGE_MULTIPLE_MATCH);
        }

        //Case 3: Single match found
        Person personToPin = personsToPin.get(0);
        model.pinPerson(personToPin);
        model.showAllPersonsPinnedFirst();
        return new CommandResult(String.format(MESSAGE_PIN_PERSON_SUCCESS, Messages.format(personToPin)));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PinCommand otherPinCommand) {
            return targetInfo.equals(otherPinCommand.targetInfo);
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
