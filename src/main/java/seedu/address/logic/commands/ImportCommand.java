package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CsvUtil.unwrapValue;
import static seedu.address.logic.parser.ExportCommandParser.PREFIX_TYPE;
import static seedu.address.logic.parser.ImportCommandParser.PREFIX_FILENAME;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.CsvUtil;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.event.Description;
import seedu.address.model.event.Event;
import seedu.address.model.event.TimeRange;
import seedu.address.model.event.Title;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Photo;
import seedu.address.model.tag.Tag;

/**
 * Imports a list of contacts from a CSV-formatted file.
 */
public class ImportCommand extends Command {

    public static final String COMMAND_WORD = "import";

    public static final String FILENAME_SUFFIX = ".csv";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Imports a list of contacts from a CSV formatted file.\n"
            + "Parameters: "
            + PREFIX_TYPE + "IMPORT_TYPE "
            + PREFIX_FILENAME + "FILENAME\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_TYPE + "add "
            + PREFIX_FILENAME + "myContacts";

    public static final String MESSAGE_INVALID_COLUMNS_CSV = "Number of columns in CSV file "
            + " do not match the expected format.";
    public static final String MESSAGE_ERROR_READING_FILE = "Error reading data from %1$s";
    public static final String MESSAGE_EMPTY_FILE = "The file %1$s is empty.";
    public static final String MESSAGE_SUCCESS_ROWS_ADDED_SKIPPED = "Successfully imported list from %1$s.csv with "
            + "%2$d row(s) added, %3$d row(s) skipped.";

    private static final Logger logger = LogsCenter.getLogger(ImportCommand.class);

    private final String importType;
    private final String filename;

    /**
     * Creates an {@code ImportCommand} object to import contact data from a CSV file
     * identified by the specified {@code filename}, using a specific {@code importType}.
     *
     * @param importType The mode of import. Expected to be "overwrite" to replace
     *                   current data, or "add" to append to existing contacts.
     * @param filename The name of the source CSV file (excluding the .csv extension).
     */
    public ImportCommand(String importType, String filename) {
        this.importType = importType;
        this.filename = filename;
    }

    /**
     * Executes the import command, reading data from two separate CSV files:
     * {filename}_events.csv and {filename}_persons.csv, and updating the model's address book.
     *
     * @param model {@code Model} which the command should operate on.
     * @return A {@code CommandResult} containing the summary of added and skipped rows.
     * @throws CommandException If the files cannot be read, are malformed, or contain invalid data.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        Path eventsPath = getEventsImportPath(model);
        Path personsPath = getPersonsImportPath(model);

        List<String> eventsLines = readLinesFromCsv(eventsPath);
        List<String> personLines = readLinesFromCsv(personsPath);


        if (hasHeaderOnly(personLines)) {
            return new CommandResult(String.format(MESSAGE_EMPTY_FILE, filename + "_persons.csv"));
        }

        AddressBook tempAddressBook;
        Model tempModel = new ModelManager();
        if (importType.equalsIgnoreCase("overwrite")) {
            tempAddressBook = new AddressBook();
        } else {
            tempAddressBook = new AddressBook(model.getAddressBook());
        }
        tempModel.setAddressBook(tempAddressBook);

        Map<Integer, Event> eventMap = new HashMap<>();
        processImportedEventsFromCsv(tempModel, eventsLines, eventMap);
        int addedRows = processImportedPersonsFromCsv(tempModel, personLines, eventMap);
        updateEventCounters(tempModel, eventMap);

        int totalRows = personLines.size() - 1;
        int skippedRows = totalRows - addedRows;

        // Reaches here if successful, copies over what was performed to the current model
        model.setAddressBook(tempModel.getAddressBook());
        model.showAllPersonsPinnedFirst();
        model.showNoEvents();
        return new CommandResult(String.format(MESSAGE_SUCCESS_ROWS_ADDED_SKIPPED,
                filename,
                addedRows,
                skippedRows));
    }

    /**
     * Reads all lines from the CSV file at the specified path using UTF-8 encoding.
     * @param importPath The {@code Path} of the file to be read.
     * @return A list of strings, where each string is a line from the file.
     * @throws CommandException If the file cannot be accessed or is empty.
     */
    private List<String> readLinesFromCsv(Path importPath) throws CommandException {
        try {
            List<String> lines = Files.readAllLines(importPath, StandardCharsets.UTF_8);
            if (lines.isEmpty()) {
                throw new CommandException(String.format(MESSAGE_EMPTY_FILE, importPath.getFileName().toString()));
            }
            return lines;

        } catch (IOException e) {
            throw new CommandException(String.format(MESSAGE_ERROR_READING_FILE,
                    importPath.getFileName().toString()));
        }
    }

    /**
     * Checks if the list of lines contains only a header or is effectively empty.
     * @param lines The list of strings read from the CSV file.
     * @return True if there are no data rows to process.
     */
    private boolean hasHeaderOnly(List<String> lines) {
        return lines.size() <= 1;
    }

    /**
     * Parses all event rows from the events CSV and registers them with the model.
     * Detects and rejects duplicate eventIds (computed from the same event content).
     *
     * @param model The {@code Model} to register events with.
     * @param lines The list of all lines (including header) from the events CSV.
     * @param eventMap The map to store parsed events by their computed event IDs.
     * @throws CommandException If a duplicate eventId is detected.
     */
    private void processImportedEventsFromCsv(Model model, List<String> lines,
            Map<Integer, Event> eventMap) throws CommandException {
        for (int i = 1; i < lines.size(); i++) {
            try {
                Optional<Event> eventOpt = parseLineToEvent(lines.get(i));
                if (eventOpt.isPresent()) {
                    Event event = eventOpt.get();
                    int computedEventId = event.getEventId();

                    // Reject duplicate computed IDs (possible tampering / conflicting rows)
                    if (eventMap.containsKey(computedEventId)) {
                        throw new CommandException(
                                String.format("Duplicate event detected in import: EventId %d already exists",
                                        computedEventId));
                    }

                    eventMap.put(computedEventId, event);

                    if (!model.hasEvent(event) && !model.hasOverlappingEvent(event)) {
                        model.addEvent(event);
                    }
                }
            } catch (IllegalArgumentException e) {
                logger.info(String.format("ImportCommand: Skipping malformed event entry: %s",
                        lines.get(i)));
            }
        }
    }

    /**
     * Parses all person rows from the persons CSV, linking them to events via event IDs.
     *
     * @param model The {@code Model} to be updated with new contacts.
     * @param lines The list of all lines (including header) from the persons CSV.
     * @param eventMap The map of available events indexed by computed event ID.
     * @return The count of successfully added rows.
     */
    private int processImportedPersonsFromCsv(Model model, List<String> lines,
            Map<Integer, Event> eventMap) {
        int added = 0;

        for (int i = 1; i < lines.size(); i++) {
            Optional<ParsedPerson> person = parseLineToPerson(lines.get(i), eventMap);
            if (person.isPresent()) {
                ParsedPerson parsedPerson = person.get();
                Person p = parsedPerson.person;
                if (!model.hasPerson(p)) {
                    model.addPerson(p);
                    if (parsedPerson.isPinned) {
                        model.pinPerson(p);
                    }
                    added++;
                }
            }
        }

        return added;
    }



    /**
     * Attempts to parse a CSV line into a {@code Person} object.
     * Captures parsing errors to allow the import process to continue with other rows.
     * @param line A single data row from the persons CSV file.
     * @param eventMap Map of computed event IDs to Event objects for linking.
     * @return An {@code Optional} containing the {@code Person} if parsing was successful,
     *         otherwise an empty {@code Optional}.
     */
    private Optional<ParsedPerson> parseLineToPerson(String line, Map<Integer, Event> eventMap) {
        try {
            return Optional.of(createPersonFromCsvRow(line, eventMap));
        } catch (IllegalArgumentException e) {
            String error = e.getMessage();
            logger.info(String.format(
                    "ImportCommand: Skipping invalid person entry: %s. Reason: %s", line, error));
            return Optional.empty();
        }
    }

    /**
     * Attempts to parse a CSV line into an {@code Event} object.
     * @param line A single data row from the events CSV file.
     * @return An {@code Optional} containing the {@code Event} if parsing was successful,
     *         otherwise an empty {@code Optional}.
     */
    Optional<Event> parseLineToEvent(String line) {
        // Blank event rows are treated as empty entries.
        if (line == null || line.trim().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(createEventFromCsvRow(line));
    }

    /**
     * Helps convert a raw CSV row from the events file into an {@code Event} object.
     * The EventId column in events CSV is ignored; the constructor computes the actual EventId
     * from event content.
     *
     * @param row A single comma-separated string from the events CSV file.
     * @return An {@code Event} object populated with the data from the row.
     */
    Event createEventFromCsvRow(String row) {
        String[] columns = CsvUtil.splitCsvLine(row);
        if (columns.length < 5) {
            throw new IllegalArgumentException("Event row does not have required columns");
        }

        String titleStr = unwrapValue(columns[1]).trim();
        String descStr = unwrapValue(columns[2]).trim();
        String startStr = columns[3].trim();
        String endStr = columns[4].trim();

        if (titleStr.isEmpty() || startStr.isEmpty() || endStr.isEmpty()) {
            throw new IllegalArgumentException("Event has missing required fields");
        }

        Title title = new Title(titleStr);
        Optional<Description> desc = descStr.isEmpty()
                ? Optional.empty()
                : Optional.of(new Description(descStr));
        TimeRange timeRange = new TimeRange(startStr, endStr);

        // Imported events start unlinked; links are recomputed from imported persons.
        return new Event(title, desc, timeRange, 0);
    }

    /**
     * Helps convert a raw CSV row from the persons file into a {@code Person} object by
     * splitting the line, validating the structure, and populating contact and event data.
     * Events are linked via event IDs from the provided eventMap, translated from CSV IDs.
     *
     * @param row A single comma-separated string from the persons CSV file.
     * @param eventMap Map of computed event IDs to Event objects for linking.
     * @return A {@code Person} object populated with the data from the row.
     */
    private ParsedPerson createPersonFromCsvRow(String row, Map<Integer, Event> eventMap) {
        String[] columns = CsvUtil.splitCsvLine(row);
        validatePersonColumnCount(columns);

        Person person = populatePersonInfo(columns);
        populateEventInfo(person, unwrapValue(columns[5]), eventMap);

        boolean isPinned = parsePinnedValue(columns);

        return new ParsedPerson(person, isPinned);
    }

    /**
     * Validates that the split CSV row from the persons file contains the required columns.
     *
     * @param columns An array of strings representing the split CSV fields.
     * @throws IllegalArgumentException If the column count is less than expected (8).
     */
    private void validatePersonColumnCount(String[] columns) {
        if (columns.length < 8) {
            throw new IllegalArgumentException(MESSAGE_INVALID_COLUMNS_CSV);
        }
    }

    /**
     * Parses pinned value from supported person CSV schema variants.
     */
    private boolean parsePinnedValue(String[] columns) {
        return Boolean.parseBoolean(unwrapValue(columns[7]));
    }

    /**
     * Extracts and initializes the {@code Person} fields except Events (Name, Phone, Email, Address, Tags, Photo)
     * from the split CSV columns to create a new {@code Person} instance.
     * Events are not populated here; they are linked separately via event IDs.
     *
     * @param columns An array of strings containing the split CSV fields.
     * @return A {@code Person} object initialized with the extracted information.
     */
    private Person populatePersonInfo(String[] columns) {
        Name name = new Name(unwrapValue(columns[0]));
        Phone phone = new Phone(unwrapValue(columns[1]));

        String emailStr = unwrapValue(columns[2]);
        Optional<Email> email = emailStr.isEmpty() ? Optional.empty() : Optional.of(new Email(emailStr));

        String addressStr = unwrapValue(columns[3]);
        Optional<Address> address = addressStr.isEmpty() ? Optional.empty() : Optional.of(new Address(addressStr));

        Set<Tag> tags = parseTags(unwrapValue(columns[4]));

        String photoStr = unwrapValue(columns[6]);
        Optional<Photo> photo = photoStr.isEmpty() ? Optional.empty() : Optional.of(new Photo(photoStr));

        return new Person(name, phone, email, address, tags, photo);
    }

    /**
     * Parses the CSV event ID string (semicolon-separated event IDs) and
     * links the corresponding events to the specified {@code Person}.
     * Event IDs are interpreted as computed EventIds.
     *
     * @param p The {@code Person} object to receive the events.
     * @param eventIdString The raw, semicolon-separated event ID string from the CSV.
     * @param eventMap Map of computed event IDs to Event objects for linking.
     */
    private void populateEventInfo(Person p, String eventIdString, Map<Integer, Event> eventMap) {
        List<Event> events = parseEventIds(eventIdString, eventMap);
        events.forEach(p::addEvent);
    }

    /**
     * Parses a semicolon-separated string of event IDs and retrieves the corresponding
     * Event objects from the eventMap using computed event IDs.
     *
     * CRITICAL: The eventIds in the CSV refer to computed event IDs (based on event content hash).
     * This method:
     * 1. Parses each event ID from the semicolon-separated string
     * 2. Looks up the event in the eventMap using the computed event ID as the key
     * 3. Returns the actual Event object with the correct numberOfPersonLinked value
     *
     * This ensures that events imported from CSV are correctly linked to their respective
     * Event objects in memory, with proper linkage counts maintained.
     *
     * @param eventIdString The raw string containing event IDs (e.g., "101;102;103").
     * @param eventMap Map of computed event IDs to Event objects, populated during event import.
     * @return A {@code List} of {@code Event} objects.
     *         Returns an empty list if input is empty or if event IDs are not found.
     */
    List<Event> parseEventIds(String eventIdString, Map<Integer, Event> eventMap) {
        List<Event> events = new ArrayList<>();

        if (eventIdString == null || eventIdString.trim().isEmpty()) {
            return events;
        }

        String[] eventIds = eventIdString.split(";");
        for (String idStr : eventIds) {
            try {
                int eventId = Integer.parseInt(idStr.trim());
                Event event = eventMap.get(eventId);
                if (event != null) {
                    events.add(event);
                } else {
                    logger.info(String.format(
                            "ImportCommand: Event with computed ID %d not found in events map", eventId));
                }
            } catch (NumberFormatException e) {
                logger.info(String.format(
                        "ImportCommand: Invalid event ID format in persons CSV: %s", idStr));
            }
        }

        return events;
    }

    /**
     * Helper container for a parsed person and their pinned status.
     */
    private static class ParsedPerson {
        private final Person person;
        private final boolean isPinned;

        ParsedPerson(Person person, boolean isPinned) {
            this.person = person;
            this.isPinned = isPinned;
        }
    }


    /**
     * Returns the path to the events CSV file to be imported, resolved relative to the
     * directory containing the current AddressBook data file.
     *
     * @param model {@code Model} used to get the base file path from user preferences.
     * @return The resolved {@code Path} pointing to the events CSV file.
     */
    protected Path getEventsImportPath(Model model) {
        Path userPrefParentDirPath = model.getAddressBookFilePath().getParent();
        return userPrefParentDirPath.resolve(filename + "_events.csv");
    }

    /**
     * Returns the path to the persons CSV file to be imported, resolved relative to the
     * directory containing the current AddressBook data file.
     *
     * @param model {@code Model} used to get the base file path from user preferences.
     * @return The resolved {@code Path} pointing to the persons CSV file.
     */
    protected Path getPersonsImportPath(Model model) {
        Path userPrefParentDirPath = model.getAddressBookFilePath().getParent();
        return userPrefParentDirPath.resolve(filename + "_persons.csv");
    }

    /**
     * Parses a semicolon-separated string into a set of unique {@code Tag} objects.
     * Leading and trailing whitespaces are removed from each tag.
     *
     * @param tagString The raw string containing tags (e.g. "friends; colleagues").
     * @return A {@code Set} of {@code Tag} objects. Returns an empty set if input is empty.
     */
    Set<Tag> parseTags(String tagString) {
        if (tagString == null || tagString.trim().isEmpty()) {
            return new HashSet<>();
        }

        return Arrays.stream(tagString.split(";"))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .map(Tag::new)
                .collect(Collectors.toSet());
    }


    /**
     * Returns true if both {@code ImportCommand} objects target the same filename
     * and have the same import configuration.
     *
     * @param other The reference object with which to compare.
     * @return True if the commands are functionally equivalent.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof ImportCommand)) {
            return false;
        }

        ImportCommand otherImportCommand = (ImportCommand) other;
        return importType.equals(otherImportCommand.importType)
                && filename.equals(otherImportCommand.filename);
    }

    /**
     * Updates event counters based on actual person-event links and removes events with 0 linked persons.
     * Traverses all persons in the model and increments each event's numberOfPersonLinked counter.
     * Events with 0 linked persons are removed from the model.
     *
     * @param model The {@code Model} containing the imported persons and events.
     * @param eventMap The map of all events by their regenerated event IDs.
     */
    private void updateEventCounters(Model model, Map<Integer, Event> eventMap) {

        // Traverse all persons and increment event counters for each person-event link.
        // Since events start with numberOfPersonLinked = 0, this builds up the correct count.
        for (Person person : model.getAddressBook().getPersonList()) {
            for (Event event : person.getEvents()) {
                event.incrementNumberOfPersonLinked();
            }
        }

        // Remove only imported events that were actually added to the model and remain unlinked.
        // These are events that appeared in the events CSV but are not referenced by any person.
        List<Event> eventsToRemove = eventMap.values().stream()
                .filter(event -> event.getNumberOfPersonLinked() == 0 && model.hasEvent(event))
                .toList();

        for (Event event : eventsToRemove) {
            model.deleteEvent(event);
        }
    }

    /**
     * Returns a string representation of this command, including the target filename and import type.
     *
     * @return A string identifying the import operation, import type, and target files.
     */
    @Override
    public String toString() {
        return String.format("ImportCommand{type=%s, filename=%s_persons.csv, %s_events.csv}",
                importType, filename, filename);
    }
}
