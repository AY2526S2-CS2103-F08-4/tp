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
import seedu.address.commons.util.PhotoStorageUtil;
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

    public static final String MESSAGE_EVENT_CLASH_IN_IMPORT =
            "Import failed: the import file contains clashing events.";

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
    public static final String MESSAGE_SUCCESS_ROWS_ADDED_SKIPPED = "Successfully imported %1$s_events.csv "
            + "and %1$s_persons.csv with %2$d contact(s) added, %3$d contact(s) skipped.";

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


        if (isHeaderOnly(personLines)) {
            return new CommandResult(String.format(MESSAGE_EMPTY_FILE, filename + "_persons.csv"));
        }

        Model tempModel = createWorkingModel(model);
        Map<Integer, Event> eventMap = importEvents(tempModel, eventsLines);
        int addedRows = processImportedPersonsFromCsv(tempModel, personLines, eventMap);

        int skippedRows = calculateSkippedRows(personLines, addedRows);
        applyImportResult(model, tempModel);
        return new CommandResult(String.format(MESSAGE_SUCCESS_ROWS_ADDED_SKIPPED,
                filename,
                addedRows,
                skippedRows));
    }

    private Model createWorkingModel(Model sourceModel) {
        Model tempModel = new ModelManager();
        tempModel.setAddressBook(createWorkingAddressBook(sourceModel));
        return tempModel;
    }

    private AddressBook createWorkingAddressBook(Model sourceModel) {
        if (importType.equalsIgnoreCase("overwrite")) {
            return new AddressBook();
        }
        return new AddressBook(sourceModel.getAddressBook());
    }

    private Map<Integer, Event> importEvents(Model model, List<String> eventsLines) throws CommandException {
        Map<Integer, Event> eventMap = new HashMap<>();
        processImportedEventsFromCsv(model, eventsLines, eventMap);
        return eventMap;
    }

    private int calculateSkippedRows(List<String> personLines, int addedRows) {
        int totalRows = personLines.size() - 1;
        return totalRows - addedRows;
    }

    private void applyImportResult(Model targetModel, Model importedModel) {
        targetModel.setAddressBook(importedModel.getAddressBook());
        targetModel.showAllPersonsPinnedFirst();
        targetModel.showNoEvents();
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
    private boolean isHeaderOnly(List<String> lines) {
        return lines.size() <= 1;
    }

    /**
     * Parses all event rows from the events CSV and registers them with the model.
     * Detects and rejects duplicate CSV event IDs.
     *
     * @param model The {@code Model} to register events with.
     * @param lines The list of all lines (including header) from the events CSV.
     * @param eventMap The map to store parsed events by their CSV event IDs.
     * @throws CommandException If a duplicate CSV event ID is detected.
     */
    private void processImportedEventsFromCsv(Model model, List<String> lines,
            Map<Integer, Event> eventMap) throws CommandException {
        List<Event> importedEvents = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            ParsedEvent parsedEvent = parseEventOrSkip(lines.get(i));
            if (parsedEvent != null) {
                ensureUniqueCsvEventId(eventMap, parsedEvent.csvEventId);
                Event canonicalEvent = resolveCanonicalEvent(model, parsedEvent.event, importedEvents);
                eventMap.put(parsedEvent.csvEventId, canonicalEvent);
            }
        }
    }

    private ParsedEvent parseEventOrSkip(String line) {
        try {
            return parseLineToEvent(line).orElse(null);
        } catch (IllegalArgumentException e) {
            logger.info(String.format("ImportCommand: Skipping malformed event entry: %s", line));
            return null;
        }
    }

    private void ensureUniqueCsvEventId(Map<Integer, Event> eventMap, int csvEventId) throws CommandException {
        if (!eventMap.containsKey(csvEventId)) {
            return;
        }
        throw new CommandException(
                String.format("Duplicate event detected in import: EventId %d already exists", csvEventId));
    }

    /**
     * Parses all person rows from the persons CSV, linking them to events via event IDs.
     *
     * @param model The {@code Model} to be updated with new contacts.
     * @param lines The list of all lines (including header) from the persons CSV.
     * @param eventMap The map of available events indexed by CSV event ID.
     * @return The count of successfully added rows.
     */
    private int processImportedPersonsFromCsv(Model model, List<String> lines,
            Map<Integer, Event> eventMap) {
        int added = 0;

        for (int i = 1; i < lines.size(); i++) {
            Optional<ParsedPerson> person = parseLineToPerson(lines.get(i), eventMap);
            if (person.isPresent() && isParsedPersonAdded(model, person.get())) {
                added++;
            }
        }

        return added;
    }

    private boolean isParsedPersonAdded(Model model, ParsedPerson parsedPerson) {
        Person person = parsedPerson.person;
        if (model.hasPerson(person)) {
            return false;
        }

        linkEventsToModel(model, person);
        model.addPerson(person);
        if (parsedPerson.isPinned) {
            model.pinPerson(person);
        }
        return true;
    }

    private void linkEventsToModel(Model model, Person person) {
        for (Event event : person.getEvents()) {
            addEventIfMissing(model, event);
            event.incrementNumberOfPersonLinked();
        }
    }

    private void addEventIfMissing(Model model, Event event) {
        if (model.hasEvent(event)) {
            return;
        }
        model.addEvent(event);
    }



    /**
     * Attempts to parse a CSV line into a {@code Person} object.
     * Captures parsing errors to allow the import process to continue with other rows.
     * @param line A single data row from the persons CSV file.
     * @param eventMap Map of CSV event IDs to Event objects for linking.
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
     * @return An {@code Optional} containing the parsed event if parsing was successful,
     *         otherwise an empty {@code Optional}.
     */
    protected Optional<ParsedEvent> parseLineToEvent(String line) {
        // Blank event rows are treated as empty entries.
        if (line == null || line.trim().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(createEventFromCsvRow(line));
    }

    /**
     * Helps convert a raw CSV row from the events file into an {@code Event} object.
     * The CSV EventId column is used only as a temporary file-local reference key.
     *
     * @param row A single comma-separated string from the events CSV file.
     * @return A parsed event containing the CSV event ID and reconstructed event object.
     */
    protected ParsedEvent createEventFromCsvRow(String row) {
        String[] columns = CsvUtil.splitCsvLine(row);
        validateEventColumnCount(columns);

        int csvEventId = parseCsvEventId(columns[0]);
        Event event = parseEventFromColumns(columns);
        return new ParsedEvent(csvEventId, event);
    }

    private void validateEventColumnCount(String[] columns) {
        if (columns.length >= 5) {
            return;
        }
        throw new IllegalArgumentException("Event row does not have required columns");
    }

    private int parseCsvEventId(String csvEventIdText) {
        try {
            return Integer.parseInt(csvEventIdText.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Event ID must be a valid integer");
        }
    }

    private Event parseEventFromColumns(String[] columns) {
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
        return new Event(title, desc, timeRange, 0);
    }

    /**
     * Helps convert a raw CSV row from the persons file into a {@code Person} object by
     * splitting the line, validating the structure, and populating contact and event data.
     * Events are linked via CSV event IDs from the provided eventMap.
     *
     * @param row A single comma-separated string from the persons CSV file.
     * @param eventMap Map of CSV event IDs to Event objects for linking.
     * @return A {@code Person} object populated with the data from the row.
     */
    private ParsedPerson createPersonFromCsvRow(String row, Map<Integer, Event> eventMap) {
        String[] columns = CsvUtil.splitCsvLine(row);
        validatePersonColumnCount(columns);

        Person person = populatePersonInfo(columns);
        populateEventInfo(person, unwrapValue(columns[5]), eventMap);

        boolean isPinned = isPinnedFromColumns(columns);

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
    private boolean isPinnedFromColumns(String[] columns) {
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

        String photoStr = unwrapValue(columns[6]).trim();
        Optional<Photo> photo = parseImportedPhoto(photoStr);

        return new Person(name, phone, email, address, tags, photo);
    }

    private Optional<Photo> parseImportedPhoto(String photoStr) {
        if (photoStr.isEmpty()) {
            return Optional.empty();
        }

        if (!Photo.isValidPhoto(photoStr)) {
            return Optional.empty();
        }

        Path importedPath = Path.of(photoStr);
        if (Files.exists(importedPath) && Files.isRegularFile(importedPath)) {
            return Optional.of(new Photo(photoStr));
        }

        Path localManagedPath = Path.of(PhotoStorageUtil.DEFAULT_IMAGE_DIR).resolve(importedPath.getFileName());
        if (Files.exists(localManagedPath) && Files.isRegularFile(localManagedPath)) {
            return Optional.of(new Photo(localManagedPath.toString()));
        }

        logger.info(String.format("ImportCommand: Photo file not found, using default image instead: %s", photoStr));
        return Optional.empty();
    }


    /**
     * Parses the CSV event ID string (semicolon-separated event IDs) and
     * links the corresponding events to the specified {@code Person}.
     * Event IDs are interpreted as CSV event IDs.
     *
     * @param p The {@code Person} object to receive the events.
     * @param eventIdString The raw, semicolon-separated event ID string from the CSV.
     * @param eventMap Map of CSV event IDs to Event objects for linking.
     */
    private void populateEventInfo(Person p, String eventIdString, Map<Integer, Event> eventMap) {
        List<Event> events = parseEventIds(eventIdString, eventMap);
        events.forEach(p::addEvent);
    }

    /**
     * Parses a semicolon-separated string of event IDs and retrieves the corresponding
     * Event objects from the eventMap using CSV event IDs.
     *
     * @param eventIdString The raw string containing event IDs (e.g., "101;102;103").
     * @param eventMap Map of CSV event IDs to Event objects, populated during event import.
     * @return A {@code List} of {@code Event} objects.
     *         Returns an empty list if input is empty or if event IDs are not found.
     */
    protected List<Event> parseEventIds(String eventIdString, Map<Integer, Event> eventMap) {
        List<Event> events = new ArrayList<>();
        Set<Event> seenEvents = new HashSet<>();

        if (eventIdString == null || eventIdString.trim().isEmpty()) {
            return events;
        }

        String[] eventIds = eventIdString.split(";");
        for (String eventIdText : eventIds) {
            Optional<Integer> eventId = parseEventIdOrSkip(eventIdText);
            eventId.ifPresent(parsedEventId -> addMappedEventIfPresent(eventMap, seenEvents, events, parsedEventId));
        }

        return events;
    }

    private Optional<Integer> parseEventIdOrSkip(String eventIdText) {
        try {
            return Optional.of(Integer.parseInt(eventIdText.trim()));
        } catch (NumberFormatException e) {
            logger.info(String.format(
                    "ImportCommand: Invalid event ID format in persons CSV: %s", eventIdText));
            return Optional.empty();
        }
    }

    private void addMappedEventIfPresent(Map<Integer, Event> eventMap, Set<Event> seenEvents,
            List<Event> events, int eventId) {
        Event event = eventMap.get(eventId);
        if (event == null) {
            logger.info(String.format("ImportCommand: Event with CSV ID %d not found in events map", eventId));
            return;
        }

        if (seenEvents.add(event)) {
            events.add(event);
        }
    }

    /**
     * Helper container for a parsed person and their pinned status.
     */
    private static class ParsedPerson {
        private final Person person;
        private final boolean isPinned;

        private ParsedPerson(Person person, boolean isPinned) {
            this.person = person;
            this.isPinned = isPinned;
        }
    }

    /**
     * Helper container for a parsed event row and its CSV EventId.
     */
    protected static class ParsedEvent {
        private final int csvEventId;
        private final Event event;

        private ParsedEvent(int csvEventId, Event event) {
            this.csvEventId = csvEventId;
            this.event = event;
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
    protected Set<Tag> parseTags(String tagString) {
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
     * Resolves the canonical event instance to use for imported person links.
     * Reuses an existing matching event when present.
     * Throws a {@code CommandException} if the imported event clashes with either
     * an existing event in the address book or another event in the same import file.
     */
    private Event resolveCanonicalEvent(Model model, Event importedEvent,
            List<Event> importedEvents) throws CommandException {
        Event existingEvent = findExistingEvent(model, importedEvent);
        if (existingEvent != null) {
            return existingEvent;
        }

        rejectIfOverlappingExistingEvent(model, importedEvent);
        rejectIfClashingWithinImport(importedEvents, importedEvent);

        importedEvents.add(importedEvent);
        return importedEvent;
    }

    private void rejectIfOverlappingExistingEvent(Model model, Event importedEvent) throws CommandException {
        if (!model.hasOverlappingEvent(importedEvent)) {
            return;
        }
        logger.info(String.format("ImportCommand: Skipping overlapping event entry: %s", importedEvent));
        throw new CommandException(MESSAGE_EVENT_CLASH_IN_IMPORT);
    }

    private void rejectIfClashingWithinImport(List<Event> importedEvents, Event importedEvent)
            throws CommandException {
        boolean isClashWithinImport = importedEvents.stream().anyMatch(importedEvent::isClashingWith);
        if (!isClashWithinImport) {
            return;
        }
        logger.info(String.format("ImportCommand: Event clashes within import file: %s", importedEvent));
        throw new CommandException(MESSAGE_EVENT_CLASH_IN_IMPORT);
    }


    /**
     * Finds the existing event object in the model that matches {@code targetEvent}.
     */
    private Event findExistingEvent(Model model, Event targetEvent) {
        return model.getAddressBook().getEventList().stream()
                .filter(targetEvent::isSameEvent)
                .findFirst()
                .orElse(null);
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
