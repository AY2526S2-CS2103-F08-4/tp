package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.CommandUtil;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.event.Event;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonInformation;

/**
 * Deletes an event from a contact identified by name, start datetime, and end datetime.
 */
public class DeleteEventCommand extends Command {

    public static final String COMMAND_WORD = "delete";

    public static final String MESSAGE_USAGE = "event " + COMMAND_WORD
            + ": Deletes an event linked to a contact.\n"
            + "Parameters: event delete title/TITLE start/START end/END n/NAME "
            + "[p/PHONE] [e/EMAIL] [a/ADDRESS]...\n"
            + "Example: event delete title/Meeting start/2026-03-25 0900 end/2026-03-25 1000 n/David Li";

    public static final String MESSAGE_SUCCESS = "Deleted event for %1$s: %2$s";
    public static final String MESSAGE_EVENT_NOT_FOUND = "This contact does not have this event: %1$s";
    private static final Logger logger = LogsCenter.getLogger(DeleteEventCommand.class);

    private final Event toDelete;
    private final PersonInformation targetInfo;

    /**
     * Creates a DeleteEventCommand to delete the event matching {@code startTime} and
     * {@code endTime} from the contact matching {@code targetInfo}.
     */
    public DeleteEventCommand(PersonInformation targetInfo, Event event) {
        requireNonNull(event);
        requireNonNull(targetInfo);
        this.toDelete = event;
        this.targetInfo = targetInfo;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        Person personToEdit = CommandUtil.targetPerson(model, targetInfo);

        // Checking if the event is in the Person's List<Event>
        if (!personToEdit.hasEvent(toDelete)) {
            logger.info("DeleteEvent: event not found for " + personToEdit.getName() + ": " + toDelete);
            throw new CommandException(String.format(MESSAGE_EVENT_NOT_FOUND, toDelete));
        }

        // unlink globally (decrement count, remove from global list if count reaches 0)
        Event eventToUnlink = model.unlinkPersonFromEvent(toDelete);
        logger.info("DeleteEvent: unlinking event " + toDelete + " from " + personToEdit.getName()
                + ", remaining links=" + eventToUnlink.getNumberOfPersonLinked());

        Person editedPerson = createPersonWithoutEvent(personToEdit, eventToUnlink);
        model.setPerson(personToEdit, editedPerson);
        logger.info("DeleteEvent: person updated " + personToEdit.getName()
                + ", total events=" + editedPerson.getEvents().size());

        model.showEventsForPerson(editedPerson);
        return new CommandResult(String.format(MESSAGE_SUCCESS, personToEdit.getName(), toDelete));
    }

    /**
     * Creates a new {@code Person} with all fields from {@code person} except {@code eventToRemove}.
     */
    private static Person createPersonWithoutEvent(Person personToEdit, Event eventToRemove) {
        Person editedPerson = new Person(personToEdit.getName(), personToEdit.getPhone(), personToEdit.getEmail(),
                personToEdit.getAddress(), personToEdit.getTags(), personToEdit.getPhoto());

        for (Event existingEvent : personToEdit.getEvents()) {
            if (!existingEvent.equals(eventToRemove)) {
                editedPerson.addEvent(existingEvent);
            }
        }
        return editedPerson;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof DeleteEventCommand otherCommand) {
            return toDelete.equals(otherCommand.toDelete)
                    && targetInfo.equals(otherCommand.targetInfo);
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("Deleting Event: %s", toDelete.toString());
    }
}
