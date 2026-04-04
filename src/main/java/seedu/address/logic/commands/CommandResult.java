package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Represents the result of a command execution.
 */
public class CommandResult {

    private final String feedbackToUser;

    /** Help information should be shown to the user. */
    private final boolean isHelpRequested;

    /** The application should exit. */
    private final boolean shouldExit;

    /**
     * Constructs a {@code CommandResult} with the specified fields.
     */
    public CommandResult(String feedbackToUser, boolean isHelpRequested, boolean shouldExit) {
        this.feedbackToUser = requireNonNull(feedbackToUser);
        this.isHelpRequested = isHelpRequested;
        this.shouldExit = shouldExit;
    }

    /**
     * Constructs a {@code CommandResult} with the specified {@code feedbackToUser},
     * and other fields set to their default value.
     */
    public CommandResult(String feedbackToUser) {
        this(feedbackToUser, false, false);
    }

    public String getFeedbackToUser() {
        return feedbackToUser;
    }

    public boolean isShowHelp() {
        return isHelpRequested;
    }

    public boolean isExit() {
        return shouldExit;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof CommandResult)) {
            return false;
        }

        CommandResult otherCommandResult = (CommandResult) other;
        return feedbackToUser.equals(otherCommandResult.feedbackToUser)
                && isHelpRequested == otherCommandResult.isHelpRequested
                && shouldExit == otherCommandResult.shouldExit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(feedbackToUser, isHelpRequested, shouldExit);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("feedbackToUser", feedbackToUser)
                .add("isHelpRequested", isHelpRequested)
                .add("shouldExit", shouldExit)
                .toString();
    }

}
