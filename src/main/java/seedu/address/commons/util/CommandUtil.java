package seedu.address.commons.util;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonInformation;
import seedu.address.model.person.Photo;

/**
 * Contains utility methods to assist commands in executing their logic and interacting with the Model.
 */
public final class CommandUtil {
    private CommandUtil() {
        // utility class
        throw new AssertionError("This class should not be instantiated.");
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

    /**
     * Safely deletes a photo from storage only if it is not shared by any other contact in the model.
     *
     * @param model the model containing the person data
     * @param personToExclude the person whose photo is being deleted (to ignore in the sharing check)
     * @param photoToDelete the photo to evaluate and potentially delete
     * @throws CommandException if the file deletion fails
     */
    public static void safelyDeletePhoto(Model model, Person personToExclude, Photo photoToDelete)
            throws CommandException {
        if (!model.isPhotoShared(photoToDelete, personToExclude)) {
            try {
                PhotoStorageUtil.deletePhoto(photoToDelete);
            } catch (IOException e) {
                throw new CommandException(Messages.MESSAGE_DELETE_PHOTO_FAIL + e.getMessage());
            }
        }
    }
}
