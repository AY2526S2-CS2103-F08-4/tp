package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Event;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonInformation;
import seedu.address.model.person.UniqueEventList;

/**
 * Deletes an event from a contact identified by name, start datetime, and end datetime.
 */
public class DeleteEventCommand extends Command {

    public static final String COMMAND_WORD = "delete";

    public static final String MESSAGE_USAGE = "event " + COMMAND_WORD
            + ": Deletes an event linked to a specific contact, identified by name and event datetime.\n"
            + "Command Format: event delete n/NAME s/START e/END\n"
            + "Example: event delete n/Delwyn s/21-02-26 1100 e/21-02-26 1500";

    public static final String MESSAGE_SUCCESS = "Event deleted for %1$s: %2$s";
    public static final String MESSAGE_EVENT_NOT_FOUND = "No matching event found!";

    private final PersonInformation targetInfo;
    private final String startTime;
    private final String endTime;

    /**
     * Creates a DeleteEventCommand to delete the event matching {@code startTime} and
     * {@code endTime} from the contact matching {@code targetInfo}.
     */
    public DeleteEventCommand(PersonInformation targetInfo, String startTime, String endTime) {
        requireNonNull(targetInfo);
        requireNonNull(startTime);
        requireNonNull(endTime);
        this.targetInfo = targetInfo;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        List<Person> matches = model.findPersons(targetInfo);

        // Case 1: No matching contact
        if (matches.isEmpty()) {
            model.updateFilteredPersonList(p -> false);
            model.updateFilteredEventList(event -> false);
            throw new CommandException(Messages.MESSAGE_NO_MATCH);
        }

        // Case 2: Multiple contacts match — surface them and prompt user to refine
        if (matches.size() > 1) {
            Set<Person> matchingPersons = Set.copyOf(matches);
            model.updateFilteredPersonList(matchingPersons::contains);
            model.updateFilteredEventList(event -> false);
            throw new CommandException(Messages.MESSAGE_MULTIPLE_MATCH);
        }

        // Case 3: Exactly one contact matched — find and delete the event
        Person personToEdit = matches.get(0);
        Optional<Event> eventToDelete = personToEdit.getEvents().stream()
                .filter(this::matchesDatetime)
                .findFirst(); // There should never be 2 events with the same time slot

        if (eventToDelete.isEmpty()) {
            throw new CommandException(MESSAGE_EVENT_NOT_FOUND);
        }

        Person editedPerson = createPersonWithoutEvent(personToEdit, eventToDelete.get());
        model.setPerson(personToEdit, editedPerson);

        model.updateFilteredPersonList(p -> p.equals(editedPerson));
        model.updateFilteredEventList(event -> editedPerson.getEvents().contains(event));

        return new CommandResult(
                String.format(MESSAGE_SUCCESS, targetInfo.name, eventToDelete.get()));
    }

    /**
     * Creates a new {@code Person} with all fields from {@code person} except {@code eventToRemove}.
     */
    private Person createPersonWithoutEvent(Person person, Event eventToRemove) {
        UniqueEventList updatedEvents = new UniqueEventList();
        for (Event existingEvent : person.getEvents()) {
            if (!existingEvent.equals(eventToRemove)) {
                updatedEvents.add(existingEvent);
            }
        }

        Person editedPerson = new Person(person.getName(), person.getPhone(),
                person.getEmail(), person.getAddress(), person.getTags());

        for (Event existingEvent : person.getEvents()) {
            if (!existingEvent.equals(eventToRemove)) {
                editedPerson.addEvent(existingEvent);
            }
        }

        return editedPerson;
    }

    /**
     * Returns true if the given event matches the target start and end datetime.
     * Events are uniquely identified by start + end datetime per the NAB spec.
     */
    private boolean matchesDatetime(Event event) {
        return event.getStartTime().equals(startTime) && event.getEndTime().equals(endTime);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof DeleteEventCommand)) {
            return false;
        }

        DeleteEventCommand otherCommand = (DeleteEventCommand) other;
        return targetInfo.equals(otherCommand.targetInfo)
                && startTime.equals(otherCommand.startTime)
                && endTime.equals(otherCommand.endTime);
    }

    @Override
    public String toString() {
        return String.format("Deleting event for %s from %s to %s",
                targetInfo.name, startTime, endTime);
    }
}
