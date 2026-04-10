package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CsvUtil.sanitizeAndWrapValue;
import static seedu.address.logic.parser.ExportCommandParser.PREFIX_FILENAME;
import static seedu.address.logic.parser.ExportCommandParser.PREFIX_TYPE;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.commons.util.FileUtil;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.event.Event;
import seedu.address.model.person.Person;
import seedu.address.model.person.Photo;

/**
 * Exports a list of contacts into two separate CSV formatted files for future use:
 * - {filename}_persons.csv containing contact information with event ID references
 * - {filename}_events.csv containing all unique events
 */
public class ExportCommand extends Command {

    public static final String COMMAND_WORD = "export";

    public static final String MESSAGE_SUCCESS = "Exported list to %1$s_persons.csv and %1$s_events.csv";

    public static final String MESSAGE_FAILURE = "Failed to export list to %1$s";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Exports a list of contacts into two CSV formatted files for future use.\n"
            + "Parameters: "
            + PREFIX_TYPE + "EXPORT_TYPE "
            + PREFIX_FILENAME + "FILENAME\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_TYPE + "all "
            + PREFIX_FILENAME + "myContacts";

    private static final String PERSONS_FILENAME_SUFFIX = "_persons.csv";
    private static final String EVENTS_FILENAME_SUFFIX = "_events.csv";

    // Person CSV headers
    private static final String NAME_COLUMN_HEADER = "Name";
    private static final String PHONE_COLUMN_HEADER = "Phone";
    private static final String EMAIL_COLUMN_HEADER = "Email";
    private static final String ADDRESS_COLUMN_HEADER = "Address";
    private static final String TAG_COLUMN_HEADER = "Tags";
    private static final String EVENT_IDS_COLUMN_HEADER = "EventIds";
    private static final String PHOTO_COLUMN_HEADER = "Photo";
    private static final String PINNED_COLUMN_HEADER = "Pinned";

    // Event CSV headers
    private static final String EVENT_ID_COLUMN_HEADER = "EventId";
    private static final String TITLE_COLUMN_HEADER = "Title";
    private static final String DESCRIPTION_COLUMN_HEADER = "Description";
    private static final String START_TIME_COLUMN_HEADER = "Start";
    private static final String END_TIME_COLUMN_HEADER = "End";

    private final String exportType;
    private final String filename;

    /**
     * Creates an {@code ExportCommand} to export contact data based on the specified {@code exportType}
     * to a CSV file named with the specified {@code filename}.
     *
     * @param exportType The scope of the export. Expected to be "all" for the entire address book,
     *                   or "current" for the currently filtered list.
     * @param filename The name of the target CSV file (excluding the .csv extension).
     */
    public ExportCommand(String exportType, String filename) {
        this.exportType = exportType;
        this.filename = filename;
    }

    /**
     * Executes the export command, retrieving the appropriate list of contacts based on
     * the {@code exportType} and writing them to two CSV files:
     * one for persons and one for events.
     *
     * @param model {@code Model} which the command should operate on.
     * @return A {@code CommandResult} indicating the success of the export operation.
     * @throws CommandException If an error occurs during the file writing process.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        List<Person> exportedList = getExportedList(model);

        // Collect all unique events referenced by exported persons
        Set<Event> uniqueEvents = collectUniqueEvents(exportedList);

        Path personsExportPath = getPersonsExportPath(model);
        Path eventsExportPath = getEventsExportPath(model);

        exportDataToTwoCsv(personsExportPath, eventsExportPath, exportedList, uniqueEvents, model);

        return new CommandResult(String.format(MESSAGE_SUCCESS, filename));
    }

    /**
     * Writes the specified lists of contacts and events to two separate CSV files.
     * Creates parent directories if they do not exist.
     *
     * @param personsPath The {@code Path} where the persons file will be saved.
     * @param eventsPath The {@code Path} where the events file will be saved.
     * @param exportedList The list of {@code Person} objects to be serialized.
     * @param uniqueEvents The set of {@code Event} objects to be serialized.
     * @throws CommandException If an {@code IOException} occurs during file creation or writing.
     */
    public void exportDataToTwoCsv(Path personsPath, Path eventsPath, List<Person> exportedList,
                                   Set<Event> uniqueEvents, Model model) throws CommandException {
        try {
            FileUtil.createParentDirsOfFile(personsPath);
            FileUtil.createParentDirsOfFile(eventsPath);

            String personsCsvData = convertPersonsToCsv(exportedList, model);
            String eventsCsvData = convertEventsToCsv(uniqueEvents);

            FileUtil.writeToFile(personsPath, personsCsvData);
            FileUtil.writeToFile(eventsPath, eventsCsvData);
        } catch (IOException e) {
            throw new CommandException(String.format(MESSAGE_FAILURE, filename));
        }
    }

    /**
     * Returns the path to the persons CSV file to be exported, resolved relative to the
     * directory containing the current AddressBook data file.
     *
     * @param model {@code Model} providing the base file path from user preferences.
     * @return The resolved {@code Path} for the persons export file.
     */
    protected Path getPersonsExportPath(Model model) {
        Path userPrefParentDirPath = model.getAddressBookFilePath().getParent();
        return userPrefParentDirPath.resolve(filename + PERSONS_FILENAME_SUFFIX);
    }

    /**
     * Returns the path to the events CSV file to be exported, resolved relative to the
     * directory containing the current AddressBook data file.
     *
     * @param model {@code Model} providing the base file path from user preferences.
     * @return The resolved {@code Path} for the events export file.
     */
    protected Path getEventsExportPath(Model model) {
        Path userPrefParentDirPath = model.getAddressBookFilePath().getParent();
        return userPrefParentDirPath.resolve(filename + EVENTS_FILENAME_SUFFIX);
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
     * Converts the specified list of contacts into a CSV string for persons,
     * including a header row followed by individual data rows for each person.
     * Each person row contains references to event IDs instead of full event data.
     *
     * @param exportedList The list of {@code Person} objects to convert.
     * @return A formatted CSV string representing the persons list.
     */
    public String convertPersonsToCsv(List<Person> exportedList, Model model) {
        StringBuilder csvBuilder = new StringBuilder();

        csvBuilder.append(NAME_COLUMN_HEADER).append(",")
                .append(PHONE_COLUMN_HEADER).append(",")
                .append(EMAIL_COLUMN_HEADER).append(",")
                .append(ADDRESS_COLUMN_HEADER).append(",")
                .append(TAG_COLUMN_HEADER).append(",")
                .append(EVENT_IDS_COLUMN_HEADER).append(",")
                .append(PHOTO_COLUMN_HEADER).append(",")
                .append(PINNED_COLUMN_HEADER).append("\n");

        for (Person p : exportedList) {
            csvBuilder.append(formatPersonToRow(p, model)).append("\n");
        }

        return csvBuilder.toString();
    }

    /**
     * Converts the specified set of events into a CSV string,
     * including a header row followed by individual data rows for each event.
     *
     * @param uniqueEvents The set of {@code Event} objects to convert.
     * @return A formatted CSV string representing the events list.
     */
    public String convertEventsToCsv(Set<Event> uniqueEvents) {
        StringBuilder csvBuilder = new StringBuilder();

        csvBuilder.append(EVENT_ID_COLUMN_HEADER).append(",")
                .append(TITLE_COLUMN_HEADER).append(",")
                .append(DESCRIPTION_COLUMN_HEADER).append(",")
                .append(START_TIME_COLUMN_HEADER).append(",")
                .append(END_TIME_COLUMN_HEADER).append("\n");

        for (Event e : uniqueEvents) {
            csvBuilder.append(formatEventToRow(e)).append("\n");
        }

        return csvBuilder.toString();
    }

    /**
     * Formats an individual {@code Person} into a single CSV row, with event references
     * as comma-separated event IDs instead of full event data.
     *
     * @param p The {@code Person} object to format.
     * @return A comma-separated string representing the person's data.
     */
    public String formatPersonToRow(Person p, Model model) {
        return String.format("%s,%s,%s,%s,%s,%s,%s,%s",
                p.getName().fullName,
                p.getPhone().value,
                sanitizeAndWrapValue(getEmailValue(p)),
                sanitizeAndWrapValue(getAddressValue(p)),
                sanitizeAndWrapValue(formatTags(p)),
                sanitizeAndWrapValue(formatEventIds(p)),
                sanitizeAndWrapValue(getPhotoValue(p)),
                sanitizeAndWrapValue(String.valueOf(model.isPersonPinned(p)))
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
     * Formats a single {@code Event} into a CSV row.
     * Special characters like pipe and semicolon are removed to maintain CSV integrity.
     *
     * @param e The {@code Event} object to format.
     * @return A comma-separated string of event details.
     */
    private String formatEventToRow(Event e) {
        String title = e.getTitle().fullTitle;
        String desc = e.getDescription().map(d -> d.fullDescription).orElse("");
        String sanitizedTitle = title.replace(",", " ").replace("\"", " ");
        String sanitizedDesc = desc.replace(",", " ").replace("\"", " ");

        return String.format("%d,%s,%s,%s,%s",
                e.getEventId(),
                sanitizeAndWrapValue(sanitizedTitle),
                sanitizeAndWrapValue(sanitizedDesc),
                e.getStartTimeFormatted(),
                e.getEndTimeFormatted());
    }

    /**
     * Formats a person's event IDs as a semicolon-separated string.
     *
     * @param p The {@code Person} object containing events.
     * @return A semicolon-separated string of event IDs.
     */
    private String formatEventIds(Person p) {
        return p.getEvents().stream()
                .map(event -> String.valueOf(event.getEventId()))
                .collect(Collectors.joining(";"));
    }

    /**
     * Collects all unique events referenced by the exported persons.
     *
     * @param exportedList The list of persons to extract events from.
     * @return A set of unique {@code Event} objects.
     */
    private Set<Event> collectUniqueEvents(List<Person> exportedList) {
        Set<Event> uniqueEvents = new HashSet<>();
        for (Person person : exportedList) {
            uniqueEvents.addAll(person.getEvents());
        }
        return uniqueEvents;
    }

    /**
     * Returns the stored photo path for export, or an empty string when no photo exists.
     */
    private String getPhotoValue(Person p) {
        return p.getPhoto().map(Photo::getPath).orElse("");
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
     * Returns a string representation of this command, including the export type and target files.
     *
     * @return A string identifying the export operation, export type, and target CSV files.
     */
    @Override
    public String toString() {
        return String.format("ExportCommand{type=%s, filename=%s_persons.csv, %s_events.csv}",
                exportType, filename, filename);
    }
}
