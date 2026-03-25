package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Set;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Event;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonInformation;
import seedu.address.model.person.exceptions.DuplicateEventException;

/**
 * Adds an {@link Event} to a person identified by the index in the current filtered person list.
 */
public class AddEventCommand extends Command {

    public static final String COMMAND_WORD = "add";

    public static final String MESSAGE_USAGE = "event " + COMMAND_WORD
            + ": Allows the user to add an event and tag it to a specific contact\n"
            + "Command Format: event add <d/DESCRIPTION> <start/START> <end/END> <to/NAME> "
            + "[p/PHONE] [e/EMAIL] [a/ADDRESS]...\n"
            + "Example Command: event " + COMMAND_WORD + " d/Complete feature list "
            + "start/21-02-26 1100 end/21-02-26 1500 to/yikleong";

    public static final String MESSAGE_SUCCESS = "Added event for %1$s: %2$s";

    public static final String MESSAGE_CONTACT_NOT_FOUND =
            "Contact with name %1$s cannot be found in the address book.";
    public static final String MESSAGE_DUPLICATE_EVENT =
            "This contact already has this event: %1$s";

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

        List<Person> matches = model.findPersons(targetInfo);

        if (matches.isEmpty()) {
            throw new CommandException(Messages.MESSAGE_NO_MATCH);
        }

        if (matches.size() > 1) {
            Set<Person> matchingPersons = Set.copyOf(matches);
            model.updateFilteredPersonList(matchingPersons::contains);
            model.updateFilteredEventList(event -> false);
            throw new CommandException(Messages.MESSAGE_MULTIPLE_MATCH);
        }

        Person personToEdit = matches.get(0);

        Person editedPerson;
        try {
            editedPerson = createPersonWithEvent(personToEdit, toAdd);
        } catch (DuplicateEventException e) {
            throw new CommandException(String.format(MESSAGE_DUPLICATE_EVENT, toAdd));
        }

        model.setPerson(personToEdit, editedPerson);
        model.updateFilteredPersonList(p -> p.equals(editedPerson));
        model.updateFilteredEventList(event -> editedPerson.getEvents().contains(event));
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
        if (other == this) {
            return true;
        }

        if (!(other instanceof AddEventCommand)) {
            return false;
        }

        AddEventCommand otherCommand = (AddEventCommand) other;
        return toAdd.equals(otherCommand.toAdd) && targetInfo.equals(otherCommand.targetInfo);
    }

    @Override
    public String toString() {
        return String.format("Adding Event: %s", toAdd.toString());
    }
}
