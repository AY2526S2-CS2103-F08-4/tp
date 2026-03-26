package seedu.address.commons.util;

import java.util.List;
import java.util.Set;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonInformation;

/**
 * Utilities for event-related command operations.
 */
public final class EventUtil {
    private EventUtil() {
        // utility class
    }

    /**
     * Resolves the target person from the model based on the provided info.
     *
     * @throws CommandException if no match or multiple matches are found
     */
    public static Person targetPerson(Model model, PersonInformation targetInfo) throws CommandException {
        List<Person> matches = model.findPersons(targetInfo);
        if (matches.isEmpty()) {
            throw new CommandException(Messages.MESSAGE_NO_MATCH);
        }

        if (matches.size() > 1) {
            Set<Person> matchingPersons = Set.copyOf(matches);
            model.showMatchingPersons(matchingPersons);
            throw new CommandException(Messages.MESSAGE_MULTIPLE_MATCH);
        }

        return matches.get(0);
    }
}
