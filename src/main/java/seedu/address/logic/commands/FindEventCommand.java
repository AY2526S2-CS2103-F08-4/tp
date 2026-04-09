package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.CommandUtil;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonInformation;

/**
 * Finds and displays all events for the specified contact.
 */
public class FindEventCommand extends Command {
    public static final String COMMAND_WORD = "view";

    public static final String MESSAGE_USAGE = "event " + COMMAND_WORD
            + ": Views all events for a specified person.\n"
            + "Parameters: event view n/NAME [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]...\n"
            + "Example: event " + COMMAND_WORD + " n/yikleong";

    private static final Logger logger = LogsCenter.getLogger(FindEventCommand.class);
    private final PersonInformation targetInfo;

    /**
     * Creates a command that finds events for the contact matching the provided person-identification info.
     *
     * @param targetInfo required matching criteria with name and optional refinements
     */
    public FindEventCommand(PersonInformation targetInfo) {
        requireNonNull(targetInfo);
        this.targetInfo = targetInfo;
    }

    /**
     * Finds the person matching the provided info and updates person/event lists based on match count:
     * zero matches will clear both lists,
     * a match shows that person's details and the corresponding person's events.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Person matchedPerson = CommandUtil.targetPerson(model, targetInfo);

        model.showEventsForPerson(matchedPerson);
        logger.info("FindEvent: matched " + matchedPerson.getName()
                + ", events=" + model.getFilteredEventList().size());
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
                .add("targetName", targetInfo.getName())
                .toString();
    }
}
