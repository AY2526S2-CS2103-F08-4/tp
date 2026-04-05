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
 * Adds an {@link Event} to a person identified by the index in the current filtered person list.
 */
public class AddEventCommand extends Command {

    public static final String COMMAND_WORD = "add";
    public static final String MESSAGE_SUCCESS = "Added event for %1$s: %2$s";
    public static final String MESSAGE_DUPLICATE_EVENT = "This contact is already linked to this event: %1$s";
    public static final String MESSAGE_CLASHING_EVENT = "This event clashes with an existing event in the calendar.";
    public static final String MESSAGE_USAGE = "event " + COMMAND_WORD
            + ": Adds an event and tags it to a contact.\n"
            + "Parameters: event add title/TITLE [desc/DESCRIPTION] start/START end/END to/NAME "
            + "[p/PHONE] [e/EMAIL] [a/ADDRESS]...\n"
            + "Example: event add title/CS2109S Meeting desc/Final discussion on problem set 1 "
            + "start/2026-03-25 0900 end/2026-03-25 1000 to/David Li";

    private static final Logger logger = LogsCenter.getLogger(AddEventCommand.class);
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
        // Step 1: resolve target person
        Person personToEdit = CommandUtil.targetPerson(model, targetInfo);

        // Case 1: Person is already linked to this event
        if (personToEdit.hasEvent(toAdd)) {
            logger.info("AddEvent: linking existing event " + toAdd + " to " + personToEdit.getName());
            throw new CommandException(String.format(MESSAGE_DUPLICATE_EVENT, toAdd));
        }

        // Case 2: Existing global event
        Event eventToLink;
        if (model.hasEvent(toAdd)) {
            logger.info("AddEvent: linking existing event " + toAdd + " to " + personToEdit.getName());
            eventToLink = model.linkPersonToEvent(toAdd);
        } else {
            // Case 3: Overlapping event
            if (model.hasOverlappingEvent(toAdd)) {
                logger.info("AddEvent: event clashes with existing event " + toAdd);
                throw new CommandException(MESSAGE_CLASHING_EVENT);
            }
            // Case 4: New event
            logger.info("AddEvent: creating new event " + toAdd + " for " + personToEdit.getName());
            model.addEvent(toAdd);
            eventToLink = toAdd;
        }

        // Update person's event list
        Person editedPerson = createPersonWithEvent(personToEdit, eventToLink);
        model.setPerson(personToEdit, editedPerson);
        logger.info("AddEvent: person updated " + personToEdit.getName()
                + ", total events=" + editedPerson.getEvents().size());

        model.showEventsForPerson(editedPerson);
        return new CommandResult(String.format(MESSAGE_SUCCESS, personToEdit.getName(), toAdd));
    }

    private static Person createPersonWithEvent(Person personToEdit, Event eventToAdd) {
        Person editedPerson = new Person(personToEdit.getName(), personToEdit.getPhone(), personToEdit.getEmail(),
                personToEdit.getAddress(), personToEdit.getTags(), personToEdit.getPhoto());

        // Adding back the events from old person to new person
        for (Event existingEvent : personToEdit.getEvents()) {
            editedPerson.addEvent(existingEvent);
        }

        // Add the new event and return the NEW Person
        editedPerson.addEvent(eventToAdd);
        return editedPerson;
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
