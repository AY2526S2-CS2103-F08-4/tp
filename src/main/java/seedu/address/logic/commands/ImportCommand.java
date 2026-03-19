package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CsvUtil.unwrapValue;
import static seedu.address.logic.parser.ImportCommandParser.PREFIX_FILENAME;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.commons.util.CsvUtil;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Event;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;

/**
 * Imports a list of contacts from a CSV formatted file.
 */
public class ImportCommand extends Command {

    public static final String COMMAND_WORD = "import";

    public static final String FILENAME_SUFFIX = ".csv";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Imports a list of contacts from a CSV formatted file.\n"
            + "Parameters: "
            + PREFIX_FILENAME + "FILENAME\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_FILENAME + "myContacts";

    public static final String MESSAGE_SUCCESS = "Successfully imported list from %1$s";
    public static final String MESSAGE_INVALID_COLUMNS_CSV = "Number of columns in CSV file do not match the expected format.";
    public static final String MESSAGE_ERROR_READING_FILE = "Error reading data from %1$s";
    public static final String MESSAGE_EMPTY_FILE = "The file %1$s is empty.";
    public static final String MESSAGE_SUCCESS_ROWS_ADDED_SKIPPED = "Successfully imported list from %1$s with "
            + "%2$d row(s) added, %3$d row(s) skipped.";

    private final String importType;
    private final String filename;

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

        if (importType.equalsIgnoreCase("overwrite")) {
            model.setAddressBook(new AddressBook());
        }

        int addedRows = processImportedLinesFromCsv(model, allLines);
        int totalRows = allLines.size() - 1;
        int skippedRows = totalRows - addedRows;

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
            List<String> lines =  Files.readAllLines(importPath, StandardCharsets.UTF_8);
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
     * Iterates through the data rows of the CSV, parses each into a {@code Person},
     * and adds them to the {@code Model} if they do not already exist.
     * @param model The {@code Model} to be updated with new contacts.
     * @param lines The list of all lines (including header) from the CSV.
     * @return The count of successfully added rows.
     * @throws CommandException If an error is encountered while parsing or adding a {@code Person}.
     */
    private int processImportedLinesFromCsv(Model model, List<String> lines) throws CommandException {
        int added = 0;

        for (int i = 1; i < lines.size(); i++) {
            Optional<Person> person = parseLineToPerson(lines.get(i));

            if (person.isPresent() && !model.hasPerson(person.get())) {
                model.addPerson(person.get());
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
     * otherwise an empty {@code Optional}.
     */
    private Optional<Person> parseLineToPerson(String line) {
        try {
            return Optional.of(createPersonFromCsvRow(line));
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
    private Person createPersonFromCsvRow(String row) {
        String[] columns = CsvUtil.splitCsvLine(row);
        validateColumnCount(columns);

        Person person = populatePersonInfo(columns);
        populateEventInfo(person, unwrapValue(columns[5]));

        return person;
    }

    /**
     * Validates that the split CSV row contains at least the minimum number of required columns.
     *
     * @param columns An array of strings representing the split CSV fields.
     * @throws IllegalArgumentException If the column count is less than the expected format (6).
     */
    private void validateColumnCount(String[] columns) {
        if (columns.length < 6) {
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
        Optional<Email> email = Optional.of(new Email(unwrapValue(columns[2])));
        Optional<Address> address = Optional.of(new Address(unwrapValue(columns[3])));
        Set<Tag> tags = parseTags(unwrapValue(columns[4]));

        return new Person(name, phone, email, address, tags);
    }

    /**
     * Parses the CSV event string into a list of {@code Event} objects and
     * associates them with the specified {@code Person}.
     * @param p The {@code Person} object to receive the events.
     * @param eventString The raw, semicolon-separated event string from the CSV.
     */
    private void populateEventInfo(Person p, String eventString) {
        List<Event> events = parseEvents(eventString);
        events.forEach(p::addEvent);
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
    private Set<Tag> parseTags(String tagString) {
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
     * split by pipes (|) into description, start time and end time.
     *
     * @param eventString The raw string containing events.
     * @return A {@code List} of {@code Event} objects. Returns an empty list if the input is empty or malformed.
     */
    private List<Event> parseEvents(String eventString) {
        List<Event> events = new ArrayList<>();

        if (eventString == null || eventString.trim().isEmpty()) {
            return events;
        }

        String[] eventEntries = eventString.split(";");

        for (String entry : eventEntries) {
            String[] details = entry.trim().split("\\|", 3);

            if (details.length == 3) {
                String description = details[0].trim();
                String start = details[1].trim();
                String end = details[2].trim();

                if (!description.isEmpty() && !start.isEmpty() && !end.isEmpty()) {
                    events.add(new Event(description, start, end));
                }
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
        return filename.equals(otherImportCommand.filename);
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
