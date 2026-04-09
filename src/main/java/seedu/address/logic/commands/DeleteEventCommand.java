package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.LocalDateTime;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.CommandUtil;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.event.Event;
import seedu.address.model.event.TimeRange;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonInformation;

/**
 * Deletes an event from a contact identified by name and event start datetime.
 */
public class DeleteEventCommand extends Command {

    public static final String COMMAND_WORD = "delete";

    public static final String MESSAGE_USAGE = "event " + COMMAND_WORD
            + ": Deletes an event linked to a contact.\n"
            + "Parameters: event delete start/START n/NAME "
            + "[p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]...\n"
            + "Example: event delete start/2026-03-25 0900 n/Alex Yeoh";

    public static final String MESSAGE_SUCCESS = "Deleted event for %1$s: %2$s";
    public static final String MESSAGE_EVENT_NOT_FOUND = "This contact does not have this event: %1$s";
    private static final Logger logger = LogsCenter.getLogger(DeleteEventCommand.class);

    private final LocalDateTime startTime;
    private final PersonInformation targetInfo;

    /**
     * Creates a DeleteEventCommand to delete the event matching {@code startTime}
     * from the contact matching {@code targetInfo}.
     */
    public DeleteEventCommand(PersonInformation targetInfo, LocalDateTime startTime) {
        requireNonNull(startTime);
        requireNonNull(targetInfo);
        this.startTime = startTime;
        this.targetInfo = targetInfo;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        Person personToEdit = CommandUtil.targetPerson(model, targetInfo);

        Event eventToDelete = personToEdit.findEventByStartTime(startTime);

        if (eventToDelete == null) {
            String formattedStart = startTime.format(TimeRange.DATE_TIME_FORMATTER);
            logger.info("DeleteEvent: event not found for " + personToEdit.getName()
                    + " with a start time " + formattedStart);
            throw new CommandException(String.format(MESSAGE_EVENT_NOT_FOUND, formattedStart));
        }

        Event eventToUnlink = model.unlinkPersonFromEvent(eventToDelete);

        logger.info("DeleteEvent: unlinking event " + eventToDelete + " from " + personToEdit.getName()
                + ", remaining links=" + eventToUnlink.getNumberOfPersonLinked());

        Person editedPerson = personToEdit.withRemovedEvent(eventToUnlink);
        model.setPerson(personToEdit, editedPerson);
        logger.info("DeleteEvent: person updated " + personToEdit.getName()
                + ", total events=" + editedPerson.getEvents().size());

        model.showEventsForPerson(editedPerson);
        return new CommandResult(String.format(MESSAGE_SUCCESS, personToEdit.getName(), eventToDelete));
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof DeleteEventCommand otherCommand) {
            return startTime.equals(otherCommand.startTime)
                    && targetInfo.equals(otherCommand.targetInfo);
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("Deleting Event for %s at start time %s",
                targetInfo, startTime.format(TimeRange.DATE_TIME_FORMATTER));
    }
}
