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
public final class CommandUtil {
    private CommandUtil() {
        // utility class
    }

    /**
     * Resolves the target person from the model based on the provided info.
     * @param model the model containing the person data
     * @param targetInfo the information to identify the target person
     * @return the resolved target person
     * @throws CommandException if no match or multiple matches are found
     */
    public static Person targetPerson(Model model, PersonInformation targetInfo) throws CommandException {
        List<Person> matches = model.findPersons(targetInfo);
        return targetPersonFromMatches(model, matches);
    }

    /**
     * Resolves a single target person from the provided list of matches.
     *
     * @param model the model containing the person data
     * @param matches the list of matching persons to resolve from
     * @return the resolved target person
     * @throws CommandException if no match or multiple matches are found
     */
    public static Person targetPersonFromMatches(Model model, List<Person> matches) throws CommandException {
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
