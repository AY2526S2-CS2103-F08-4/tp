package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.person.TagContainsKeywordsPredicate;

/**
 * Allows the user to filter contact by 1 or more tags.
 * Keyword matching is case insensitive.
 */
public class FilterCommand extends Command {

    public static final String COMMAND_WORD = "filter";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Finds all persons who match any of "
            + "the specified tags (case-insensitive) and displays them as a list with index numbers.\n"
            + "Parameters: t/TAG, [TAG2, ...]\n"
            + "Example: " + COMMAND_WORD + " t/CS2103 Group";

    private final TagContainsKeywordsPredicate predicate;

    public FilterCommand(TagContainsKeywordsPredicate predicate) {
        this.predicate = predicate;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.showPersons(predicate);

        if (model.getFilteredPersonList().isEmpty()) {
            return new CommandResult(String.format(Messages.MESSAGE_NO_PERSONS));
        }

        if (model.getFilteredPersonList().size() == 1) {
            return new CommandResult(
                    String.format(Messages.MESSAGE_ONE_PERSON_LISTED_OVERVIEW));
        }

        model.showNoEvents();
        return new CommandResult(
                String.format(Messages.MESSAGE_PERSONS_LISTED_OVERVIEW, model.getFilteredPersonList().size()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof FilterCommand)) {
            return false;
        }

        FilterCommand otherFilterCommand = (FilterCommand) other;
        return predicate.equals(otherFilterCommand.predicate);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("predicate", predicate)
                .toString();
    }
}
