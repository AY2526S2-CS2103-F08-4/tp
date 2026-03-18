package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Set;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonInformation;

/**
 * Finds and displays all events associated with persons whose names contain any of the argument keywords.
 * Keyword matching is case insensitive.
 */
public class FindEventCommand extends Command {

    public static final String COMMAND_WORD = "view";

    public static final String MESSAGE_USAGE = "event " + COMMAND_WORD
            + ": Finds all events from persons whose names contain any of "
            + "the specified keywords (case-insensitive) and displays them as a list.\n"
            + "Parameters: event view n/NAME [p/PHONE] [e/EMAIL] [a/ADDRESS]...\n"
            + "Example: event " + COMMAND_WORD + " n/yikleong";

    private final PersonInformation targetInfo;

    /**
     * Creates a command that finds events for contacts matching the provided person-identification info.
     *
     * @param targetInfo required matching criteria with name and optional refinements
     */
    public FindEventCommand(PersonInformation targetInfo) {
        requireNonNull(targetInfo);
        this.targetInfo = targetInfo;
    }

    /**
     * Finds persons matching the provided info and updates person/event lists based on match count:
     * zero matches clears both lists,
     * multiple matches shows matching persons only,
     * one match shows that person details and the corresponding person's events.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        List<Person> matches = model.findPersons(targetInfo);

        if (matches.isEmpty()) {
            model.updateFilteredPersonList(person -> false);
            model.updateFilteredEventList(event -> false);
            throw new CommandException(Messages.MESSAGE_NO_MATCH);
        }

        if (matches.size() > 1) {
            Set<Person> matchingPersons = Set.copyOf(matches);
            model.updateFilteredPersonList(matchingPersons::contains);
            model.updateFilteredEventList(event -> false);
            throw new CommandException(Messages.MESSAGE_MULTIPLE_MATCH);
        }

        Person matchedPerson = matches.get(0);
        model.updateFilteredPersonList(person -> person.equals(matchedPerson));
        model.updateFilteredEventList(event -> matchedPerson.getEvents().contains(event));
        return new CommandResult(
                String.format(Messages.MESSAGE_EVENTS_LISTED_OVERVIEW, model.getFilteredEventList().size()));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FindEventCommand otherFindEventCommand) {
            return targetInfo.equals(otherFindEventCommand.targetInfo);
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
