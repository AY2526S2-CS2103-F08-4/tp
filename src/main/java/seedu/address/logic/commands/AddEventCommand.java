package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Event;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;

/**
 * Adds an {@link Event} to a person identified by the index in the current filtered person list.
 */
public class AddEventCommand extends Command {

    public static final String COMMAND_WORD = "add";

    public static final String MESSAGE_USAGE = "event " + COMMAND_WORD
            + ": Allows the user to add an event and tag it to a specific contact\n"
            + "Command Format: event add <l/LABEL> [d/DESCRIPTION] <s/START> <e/END> <to/NAME>\n"
            + "Example Command: event " + COMMAND_WORD + " CS2103 Meeting d/Complete feature list "
            + "s/21-02-26 1100 e/21-02-26 1500 to/yikleong";

    public static final String MESSAGE_SUCCESS = "Added event for %1$s: %2$s";

    public static final String MESSAGE_CONTACT_NOT_FOUND =
            "Contact with name %1$s cannot be found in the address book.";

    private final Event toAdd;
    private final Name contact;

    /**
     * Creates an AddEventCommand to add the specified {@code Event} to a person at {@code index}.
     */
    public AddEventCommand(String contact, Event event) {
        requireNonNull(event);
        this.toAdd = event;
        this.contact = new Name(contact);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        // Assumptions: Unique names
        Person personToEdit = model.findPersonByName(this.contact);
        if (personToEdit == null) {
            throw new CommandException(String.format(MESSAGE_CONTACT_NOT_FOUND, this.contact.fullName));
        }
        Person editedPerson = createPersonWithEvent(personToEdit, toAdd);

        model.setPerson(personToEdit, editedPerson);
        return new CommandResult(String.format(MESSAGE_SUCCESS,
                this.contact.fullName, toAdd.toString()));
    }


    private static Person createPersonWithEvent(Person personToEdit, Event eventToAdd) throws CommandException {
        Person editedPerson = new Person(personToEdit.getName(), personToEdit.getPhone(), personToEdit.getEmail(),
                personToEdit.getAddress(), personToEdit.getTags());

        // Adding back the events from old person to new person
        for (Event existingEvent : personToEdit.getEvents()) {
            editedPerson.addEvent(existingEvent);
        }

        // Add the new event and return the NEW Person
        editedPerson.addEvent(eventToAdd);
        return editedPerson;
    }

    // TODO: Implementation the equals() by checking the tagged person and the newly added event
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof AddEventCommand)) {
            return false;
        }

        AddEventCommand otherCommand = (AddEventCommand) other;
        return toAdd.equals(otherCommand.toAdd);
    }

    @Override
    public String toString() {
        return String.format("Adding Event: %s", toAdd.toString());
    }
}
