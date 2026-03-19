package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CsvUtil.sanitizeAndWrapValue;
import static seedu.address.logic.parser.ExportCommandParser.PREFIX_FILENAME;
import static seedu.address.logic.parser.ExportCommandParser.PREFIX_TYPE;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import seedu.address.commons.util.FileUtil;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Event;
import seedu.address.model.person.Person;

/**
 * Exports a list of contacts into a CSV formatted file for future use.
 */
public class ExportCommand extends Command {

    public static final String COMMAND_WORD = "export";

    private static final String FILENAME_SUFFIX = ".csv";
    private static final String NAME_COLUMN_HEADER = "Name";
    private static final String PHONE_COLUMN_HEADER = "Phone";
    private static final String EMAIL_COLUMN_HEADER = "Email";
    private static final String ADDRESS_COLUMN_HEADER = "Address";
    private static final String TAG_COLUMN_HEADER = "Tags";
    private static final String EVENT_COLUMN_HEADER = "Events";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Exports a list of contacts into a CSV formatted file for future use.\n"
            + "Parameters: "
            + PREFIX_TYPE + "EXPORT TYPE "
            + PREFIX_FILENAME + "FILENAME\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_TYPE + "all "
            + PREFIX_FILENAME + "myContacts";

    public static final String MESSAGE_SUCCESS = "Exported list to %1$s";

    public static final String MESSAGE_FAILURE = "Failed to export list to %1$s";

    private final String exportType;
    private final String filename;

    /**
     * Creates an {@code ExportCommand} to export contact data based on the specified {@code exportType}
     * to a CSV file named with the specified {@code filename}.
     *
     * @param exportType The scope of the export. Expected to be "all" for the entire address book,
     * or "current" for the currently filtered list.
     * @param filename The name of the target CSV file (excluding the .csv extension).
     */
    public ExportCommand(String exportType, String filename) {
        this.exportType = exportType;
        this.filename = filename;
    }

    /**
     * Executes the export command, retrieving the appropriate list of contacts based on
     * the {@code exportType} and writing them to a CSV file at the determined path.
     *
     * @param model {@code Model} which the command should operate on.
     * @return A {@code CommandResult} indicating the success of the export operation.
     * @throws CommandException If an error occurs during the file writing process.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        List<Person> exportedList = getExportedList(model);

        Path exportPath = getExportPath(model);

        exportDataToCsv(exportPath, exportedList);

        return new CommandResult(String.format(MESSAGE_SUCCESS,
                filename + FILENAME_SUFFIX));
    }

    /**
     * Writes the specified list of contacts to a CSV file. Creates parent directories if
     * they do not exist and converts the {@code Person} data into a formatted CSV string.
     *
     * @param exportPath The {@code Path} where the file will be saved.
     * @param exportedList The list of {@code Person} objects to be serialized.
     * @throws CommandException If an {@code IOException} occurs during file creation or writing.
     */
    public void exportDataToCsv(Path exportPath, List<Person> exportedList) throws CommandException {
        try {
            FileUtil.createParentDirsOfFile(exportPath);

            String csvData = convertToCsv(exportedList);

            FileUtil.writeToFile(exportPath, csvData);
        } catch (IOException e) {
            throw new CommandException(String.format(MESSAGE_FAILURE, filename + FILENAME_SUFFIX));
        }
    }

    /**
     * Returns the path to the CSV file to be exported, resolved relative to the
     * directory containing the current AddressBook data file.
     *
     * @param model {@code Model} providing the base file path from user preferences.
     * @return The resolved {@code Path} for the export file.
     */
    protected Path getExportPath(Model model) {
        Path userPrefParentDirPath = model.getAddressBookFilePath().getParent();
        return userPrefParentDirPath.resolve(filename + FILENAME_SUFFIX);
    }

    /**
     * Returns the list of contacts to be exported based on the {@code exportType}.
     * Supports exporting all contacts in the address book or only the currently filtered list.
     *
     * @param model {@code Model} containing the address book and filtered list.
     * @return A list of {@code Person} objects to be included in the export.
     */
    private List<Person> getExportedList(Model model) {
        return exportType.equalsIgnoreCase("all")
                ? model.getAddressBook().getPersonList()
                : model.getFilteredPersonList();
    }

    /**
     * Converts the specified list of contacts into a single CSV string, including
     * a header row followed by individual data rows for each person.
     *
     * @param exportedList The list of {@code Person} objects to convert.
     * @return A formatted CSV string representing the entire contact list.
     */
    public String convertToCsv(List<Person> exportedList) {
        StringBuilder csvBuilder = new StringBuilder();

        csvBuilder.append(NAME_COLUMN_HEADER).append(",")
                .append(PHONE_COLUMN_HEADER).append(",")
                .append(EMAIL_COLUMN_HEADER).append(",")
                .append(ADDRESS_COLUMN_HEADER).append(",")
                .append(TAG_COLUMN_HEADER).append(",")
                .append(EVENT_COLUMN_HEADER).append("\n");

        for (Person p : exportedList) {
            csvBuilder.append(formatPersonToRow(p)).append("\n");
        }

        return csvBuilder.toString();
    }

    /**
     * Formats an individual {@code Person} into a single CSV row, ensuring that
     * complex fields like addresses and tags are sanitized to prevent formatting errors.
     *
     * @param p The {@code Person} object to format.
     * @return A comma-separated string representing the person's data.
     */
    public String formatPersonToRow(Person p) {
        return String.format("%s,%s,%s,%s,%s,%s",
                p.getName().fullName,
                p.getPhone().value,
                sanitizeAndWrapValue(getEmailValue(p)),
                sanitizeAndWrapValue(getAddressValue(p)),
                sanitizeAndWrapValue(formatTags(p)),
                sanitizeAndWrapValue(formatEvents(p))
        );
    }

    /**
     * Retrieves the email string from a {@code Person}. Returns an empty string
     * if the email is not present.
     *
     * @param p The {@code Person} object.
     * @return The string representation of the email, or an empty string.
     */
    private String getEmailValue(Person p) {
        return p.getEmail().map(e -> e.value).orElse("");
    }

    /**
     * Retrieves the address string from a {@code Person}. Returns an empty string
     * if the address is not present.
     *
     * @param p The {@code Person} object.
     * @return The string representation of the address, or an empty string.
     */
    private String getAddressValue(Person p) {
        return p.getAddress().map(a -> a.value).orElse("");
    }

    /**
     * Aggregates a person's tags into a single string, sorted alphabetically and
     * separated by semicolons.
     *
     * @param p The {@code Person} object containing tags.
     * @return A semicolon-separated string of tag names.
     */
    private String formatTags(Person p) {
        return p.getTags().stream()
                .map(tag -> tag.tagName)
                .sorted()
                .collect(Collectors.joining(";"));
    }

    /**
     * Formats a single {@code Event} into a pipe-separated string (Description|Start|End).
     * Special characters like pipe and semicolon are removed from the description
     * to maintain CSV integrity.
     *
     * @param e The {@code Event} object to format.
     * @return A pipe-separated string of event details.
     */
    private String formatSingleEvent(Event e) {
        String sanitizedDesc = e.getDescription().replace("|", " ");
        sanitizedDesc = sanitizedDesc.replace(";", " ");

        return String.format("%s|%s|%s",
                sanitizedDesc,
                e.getStartTime(),
                e.getEndTime());
    }

    /**
     * Aggregates all events associated with a {@code Person} into a single string,
     * with individual events separated by semicolons.
     *
     * @param p The {@code Person} object containing events.
     * @return A semicolon-separated string of formatted events.
     */
    private String formatEvents(Person p) {
        return p.getEvents().stream()
                .map(this::formatSingleEvent)
                .collect(Collectors.joining(";"));
    }

    /**
     * Returns true if both {@code ExportCommand} objects have the same export type
     * (e.g., 'all' or 'current') and the same target filename.
     *
     * @param other The reference object with which to compare.
     * @return True if the commands are functionally equivalent.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof ExportCommand)) {
            return false;
        }

        ExportCommand otherExportCommand = (ExportCommand) other;
        return exportType.equals(otherExportCommand.exportType)
                && filename.equals(otherExportCommand.filename);
    }

    /**
     * Returns a string representation of this command, identifying the target file.
     *
     * @return A string summary of the export operation.
     */
    @Override
    public String toString() {
        return String.format("Exporting list to: %s", filename + FILENAME_SUFFIX);
    }
}
