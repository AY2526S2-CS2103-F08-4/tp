package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.CommandUtil;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.event.Event;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonInformation;

/**
 * Adds an {@link Event} to a person identified by the index in the current filtered person list.
 */
public class AddEventCommand extends Command {

    public static final String COMMAND_WORD = "add";
    public static final String MESSAGE_SUCCESS = "Added event for %1$s: %2$s";
    public static final String MESSAGE_DUPLICATE_EVENT = "This contact is already linked to this event: %1$s";
    public static final String MESSAGE_CLASHING_EVENT = "This event clashes with an existing event in the calendar: ";
    public static final String MESSAGE_USAGE = "event " + COMMAND_WORD
            + ": Adds an event and tags it to a contact.\n"
            + "Parameters: event add title/TITLE [desc/DESCRIPTION] start/START end/END n/NAME "
            + "[p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]...\n"
            + "Example: event add title/CS2109S Meeting desc/Final discussion on problem set 1 "
            + "start/2026-03-25 0900 end/2026-03-25 1000 n/David Li";

    private static final Logger LOGGER = LogsCenter.getLogger(AddEventCommand.class);
    private final Event toAdd;
    private final PersonInformation targetInfo;


    /**
     * Creates an AddEventCommand to add the specified {@code Event} to a person at {@code index}.
     */
    public AddEventCommand(PersonInformation targetInfo, Event event) {
        requireNonNull(event);
        requireNonNull(targetInfo);
        this.toAdd = event;
        this.targetInfo = targetInfo;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Person personToEdit = CommandUtil.targetPerson(model, targetInfo);

        if (personToEdit.hasEvent(toAdd)) {
            LOGGER.info("AddEvent: linking existing event " + toAdd + " to " + personToEdit.getName());
            throw new CommandException(String.format(MESSAGE_DUPLICATE_EVENT, toAdd));
        }

        Event eventToLink;
        if (model.hasEvent(toAdd)) {
            LOGGER.info("AddEvent: linking existing event " + toAdd + " to " + personToEdit.getName());
            eventToLink = model.linkPersonToEvent(toAdd);
        } else {
            eventToLink = createNewEvent(model, personToEdit);
        }

        Person editedPerson = personToEdit.copyWithAddedEvent(eventToLink);
        model.setPerson(personToEdit, editedPerson);
        LOGGER.info("AddEvent: person updated " + personToEdit.getName()
                + ", total events=" + editedPerson.getEvents().size());

        model.showEventsForPerson(editedPerson);
        return new CommandResult(String.format(MESSAGE_SUCCESS, personToEdit.getName(), toAdd));
    }

    private Event createNewEvent(Model model, Person personToEdit) throws CommandException {
        Event eventToLink;
        List<Event> clashingEvents = model.getOverlappingEvent(toAdd);
        if (!clashingEvents.isEmpty()) {
            LOGGER.info("AddEvent: event clashes with existing event " + toAdd);
            StringBuilder errorMessage = new StringBuilder(MESSAGE_CLASHING_EVENT + "\n");
            for (Event conflict : clashingEvents) {
                String linkedNames = model.getNamesLinkedToEvent(conflict);
                errorMessage.append(String.format("• %s (Linked to %s)\n",
                        conflict.getClashDisplayString(), linkedNames));
            }
            throw new CommandException(errorMessage.toString().trim());
        }
        LOGGER.info("AddEvent: creating new event " + toAdd + " for " + personToEdit.getName());
        model.addEvent(toAdd);
        eventToLink = toAdd;
        return eventToLink;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof AddEventCommand otherAddEventCommand) {
            return toAdd.equals(otherAddEventCommand.toAdd)
                    && targetInfo.equals(otherAddEventCommand.targetInfo);
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("Adding Event: %s", toAdd.toString());
    }
}
