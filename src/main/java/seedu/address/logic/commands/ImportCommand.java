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

    public static final String MESSAGE_SUCCESS = "Successfully imported list from %1$s";
    public static final String MESSAGE_INVALID_COLUMNS_CSV = "Number of columns in CSV file "
            + " do not match the expected format.";
    public static final String MESSAGE_ERROR_READING_FILE = "Error reading data from %1$s";
    public static final String MESSAGE_EMPTY_FILE = "The file %1$s is empty.";
    public static final String MESSAGE_SUCCESS_ROWS_ADDED_SKIPPED = "Successfully imported list from %1$s with "
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
     * Executes the import command, reading data from the specified CSV file and
     * updating the model's address book.
     *
     * @param model {@code Model} which the command should operate on.
     * @return A {@code CommandResult} containing the summary of added and skipped rows.
     * @throws CommandException If the file cannot be read, is malformed, or contains invalid data.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        Path importPath = getImportPath(model);
        List<String> allLines = readLinesFromCsv(importPath);

        if (hasHeaderOnly(allLines)) {
            return new CommandResult(String.format(MESSAGE_EMPTY_FILE, filename + FILENAME_SUFFIX));
        }

        AddressBook tempAddressBook;
        Model tempModel = new ModelManager();
        if (importType.equalsIgnoreCase("overwrite")) {
            tempAddressBook = new AddressBook();
        } else {
            tempAddressBook = new AddressBook(model.getAddressBook());
        }
        tempModel.setAddressBook(tempAddressBook);

        int addedRows = processImportedLinesFromCsv(tempModel, allLines);
        int totalRows = allLines.size() - 1;
        int skippedRows = totalRows - addedRows;

        // Reaches here if successful, copies over what was performed to the current model
        model.setAddressBook(tempModel.getAddressBook());
        return new CommandResult(String.format(MESSAGE_SUCCESS_ROWS_ADDED_SKIPPED,
                filename + FILENAME_SUFFIX,
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
                throw new CommandException(String.format(MESSAGE_EMPTY_FILE, filename + FILENAME_SUFFIX));
            }
            return lines;

        } catch (IOException e) {
            throw new CommandException(String.format(MESSAGE_ERROR_READING_FILE,
                    filename + FILENAME_SUFFIX));
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
     * Parses all data rows of the CSV into {@code Person} objects, registers their
     * events with the global model, then adds each person if they do not already exist.
     * @param model The {@code Model} to be updated with new contacts and events.
     * @param lines The list of all lines (including header) from the CSV.
     * @return The count of successfully added rows.
     * @throws CommandException If an error is encountered while parsing or adding a {@code Person}.
     */
    private int processImportedLinesFromCsv(Model model, List<String> lines) throws CommandException {
        int added = 0;
        Map<Integer, Event> eventMap = new HashMap<>();

        // Pass 1: parse all rows. Events are collected into eventMap.
        List<ParsedPerson> parsedPersons = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            Optional<ParsedPerson> person = parseLineToPerson(lines.get(i), eventMap);
            person.ifPresent(parsedPersons::add);
        }

        // Pass 2: register every unique parsed event with the global model.
        for (Event event : eventMap.values()) {
            if (!model.hasEvent(event) && !model.hasOverlappingEvent(event)) {
                model.addEvent(event);
            }
        }

        // Pass 3: add persons. Their internal event lists already reference the
        // same objects registered globally in pass 2.
        for (ParsedPerson parsedPerson : parsedPersons) {
            Person person = parsedPerson.person;
            if (!model.hasPerson(person)) {
                model.addPerson(person);
                if (parsedPerson.isPinned) {
                    model.pinPerson(person);
                }
                added++;
            }
        }

        return added;
    }



    /**
     * Attempts to parse a CSV line into a {@code Person} object.
     * Captures parsing errors to allow the import process to continue with other rows.
     * @param line A single data row from the CSV file.
     * @return An {@code Optional} containing the {@code Person} if parsing was successful,
     *         otherwise an empty {@code Optional}.
     */
    private Optional<ParsedPerson> parseLineToPerson(String line, Map<Integer, Event> eventMap) {
        try {
            return Optional.of(createPersonFromCsvRow(line, eventMap));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * Helps convert a raw CSV row into a {@code Person} object by
     * splitting the line, validating the structure, and populating contact and event data.
     *
     * @param row A single comma-separated string from the CSV file.
     * @return A {@code Person} object populated with the data from the row.
     */
    private ParsedPerson createPersonFromCsvRow(String row, Map<Integer, Event> eventMap) {
        String[] columns = CsvUtil.splitCsvLine(row);
        validateColumnCount(columns);

        Person person = populatePersonInfo(columns);
        populateEventInfo(person, unwrapValue(columns[5]), eventMap);

        boolean isPinned = columns.length >= 8 && Boolean.parseBoolean(unwrapValue(columns[7]));

        return new ParsedPerson(person, isPinned);
    }

    /**
     * Validates that the split CSV row contains at least the minimum number of required columns.
     *
     * @param columns An array of strings representing the split CSV fields.
     * @throws IllegalArgumentException If the column count is less than the expected format (6).
     */
    private void validateColumnCount(String[] columns) {
        if (columns.length < 7) {
            throw new IllegalArgumentException(MESSAGE_INVALID_COLUMNS_CSV);
        }
    }

    /**
     * Extracts and initializes the {@code Person} fields except Events (Name, Phone, Email, Address, Tags)
     * from the split CSV columns to create a new {@code Person} instance.
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
     * Parses the CSV event string into a list of {@code Event} objects and
     * associates them with the specified {@code Person}.
     * @param p The {@code Person} object to receive the events.
     * @param eventString The raw, semicolon-separated event string from the CSV.
     */
    private void populateEventInfo(Person p, String eventString, Map<Integer, Event> eventMap) {
        List<Event> events = parseEvents(eventString, eventMap);
        events.forEach(p::addEvent);
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
     * Returns the path to the CSV file to be imported, resolved relative to the
     * directory containing the current AddressBook data file.
     *
     * @param model {@code Model} used to get the base file path from user preferences.
     * @return The resolved {@code Path} pointing to the CSV file.
     */
    protected Path getImportPath(Model model) {
        Path userPrefParentDirPath = model.getAddressBookFilePath().getParent();
        return userPrefParentDirPath.resolve(filename + FILENAME_SUFFIX);
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
     * Parses a semicolon-separated string of events, where each event is then
     * split by pipes (|) into title, description, start time and end time.
     *
     * @param eventString The raw string containing events.
     * @return A {@code List} of {@code Event} objects. Returns an empty list if the input is empty or malformed.
     */
    List<Event> parseEvents(String eventString, Map<Integer, Event> eventMap) {
        List<Event> events = new ArrayList<>();

        if (eventString == null || eventString.trim().isEmpty()) {
            return events;
        }

        String[] eventEntries = eventString.split(";");

        for (String entry : eventEntries) {
            String[] details = entry.trim().split("\\|", -1);
            if (details.length != 6) {
                logger.info(String.format(
                        "ImportCommand: Skipping event entry with invalid column count: %s", entry)
                );
                continue;
            }

            try {
                String titleStr = details[0].trim();
                String descStr = details[1].trim();
                String startStr = details[2].trim();
                String endStr = details[3].trim();
                String linkedCountStr = details[4].trim();
                String eventIdStr = details[5].trim();

                if (titleStr.isEmpty() || startStr.isEmpty() || endStr.isEmpty() || eventIdStr.isEmpty()) {
                    logger.info(String.format(
                            "ImportCommand: Skipping event entry with missing required fields: %s", entry)
                    );
                    continue;
                }

                int linkedCount = Integer.parseInt(linkedCountStr);
                int eventId = Integer.parseInt(eventIdStr);
                if (linkedCount <= 0) {
                    logger.info(String.format(
                            "ImportCommand: Skipping event entry with non-positive linked count for eventId %d",
                            eventId)
                    );
                    continue;
                }

                Title title = new Title(titleStr);
                Optional<Description> desc = descStr.isEmpty()
                        ? Optional.empty()
                        : Optional.of(new Description(descStr));
                TimeRange timeRange = new TimeRange(startStr, endStr);

                Event parsedEvent = new Event(title, desc, timeRange, linkedCount);
                Event existingEvent = eventMap.get(eventId);

                if (existingEvent == null) {
                    eventMap.put(eventId, parsedEvent);
                    events.add(parsedEvent);
                } else if (existingEvent.isSameEvent(parsedEvent)) {
                    events.add(existingEvent);
                }
            } catch (IllegalArgumentException e) {
                // Skip malformed event entries, log the event and continue with the rest of the row
                logger.info(
                        String.format("ImportCommand: Skipping event entry due to error: %s", e.getMessage())
                );
            }
        }

        return events;
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
     * Returns a string representation of this command, including the target filename.
     *
     * @return A string identifying the import operation and target file.
     */
    @Override
    public String toString() {
        return String.format("Importing list from: %s", filename + FILENAME_SUFFIX);
    }
}
