package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.AddEventCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.ExportCommand;
import seedu.address.logic.commands.FilterCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ImportCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.event.Description;
import seedu.address.model.event.Event;
import seedu.address.model.event.TimeRange;
import seedu.address.model.event.Title;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonInformation;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PersonUtil;

public class AddressBookParserTest {

    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parseCommand_add() throws Exception {
        Person person = new PersonBuilder().build();
        AddCommand command = (AddCommand) parser.parseCommand(PersonUtil.getAddCommand(person));
        assertEquals(new AddCommand(person), command);
    }

    @Test
    public void parseCommand_eventAdd() throws Exception {
        Event expectedEvent = new Event(new Title("Complete feature list"),
                Optional.of(new Description("All tasks")),
                new TimeRange("2026-02-21 1100", "2026-02-21 1500"));
        AddEventCommand expectedCommand = new AddEventCommand(
                new PersonInformation(new Name("Lee eejoong"), null, null, null, null), expectedEvent);

        AddEventCommand command = (AddEventCommand) parser.parseCommand(
                "event add title/Complete feature list desc/All tasks start/2026-02-21 1100 "
                        + "end/2026-02-21 1500 to/Lee eejoong");

        assertEquals(expectedCommand, command);
    }

    @Test
    public void parseCommand_clear() throws Exception {
        assertTrue(parser.parseCommand(ClearCommand.COMMAND_WORD) instanceof ClearCommand);
        assertTrue(parser.parseCommand(ClearCommand.COMMAND_WORD + " 3") instanceof ClearCommand);
    }

    @Test
    public void parseCommand_delete() throws Exception {
        Person personToDelete = new PersonBuilder().withName("John Doe").build();
        DeleteCommand command = (DeleteCommand) parser.parseCommand(
                DeleteCommand.COMMAND_WORD + " " + PREFIX_NAME + personToDelete.getName());
        assertEquals(new DeleteCommand(new PersonInformation(new Name("John Doe"), null, null, null, null)), command);
    }

    @Test
    public void parseCommand_edit() throws Exception {
        Person person = new PersonBuilder().build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(person).build();
        EditCommand command = (EditCommand) parser.parseCommand(EditCommand.COMMAND_WORD + " "
            + person.getName().fullName + " -- " + PersonUtil.getEditPersonDescriptorDetails(descriptor));
        assertEquals(new EditCommand(new PersonInformation(person.getName(), null, null, null, null), descriptor),
            command);
    }

    @Test
    public void parseCommand_exit() throws Exception {
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD) instanceof ExitCommand);
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD + " 3") instanceof ExitCommand);
    }

    @Test
    public void parseCommand_find() throws Exception {
        FindCommand command = (FindCommand) parser.parseCommand(
                FindCommand.COMMAND_WORD + " " + PREFIX_NAME + "foo bar baz");
        assertEquals(new FindCommand(new PersonInformation(new Name("foo bar baz"), null, null, null, null)),
                command);
    }

    @Test
    public void parseCommand_help() throws Exception {
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD) instanceof HelpCommand);
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD + " 3") instanceof HelpCommand);
    }

    @Test
    public void parseCommand_list() throws Exception {
        assertTrue(parser.parseCommand(ListCommand.COMMAND_WORD) instanceof ListCommand);
        assertTrue(parser.parseCommand(ListCommand.COMMAND_WORD + " 3") instanceof ListCommand);
    }

    @Test
    public void parseCommand_filter() throws Exception {
        String singleTag = FilterCommand.COMMAND_WORD + " t/friends";
        assertTrue(parser.parseCommand(singleTag) instanceof FilterCommand);

        String doubleTag = FilterCommand.COMMAND_WORD + " t/friends, colleagues";
        assertTrue(parser.parseCommand(doubleTag) instanceof FilterCommand);
    }

    @Test
    public void parseCommand_import() throws Exception {
        String type = "add";
        String file = "testImport";
        ImportCommand expectedCommand = new ImportCommand(type, file);

        ImportCommand command = (ImportCommand) parser.parseCommand(
                ImportCommand.COMMAND_WORD + " t/" + type + " f/" + file);

        assertEquals(expectedCommand, command);
    }

    @Test
    public void parseCommand_export() throws Exception {
        String type = "all";
        String file = "testExport";
        ExportCommand expectedCommand = new ExportCommand(type, file);

        ExportCommand command = (ExportCommand) parser.parseCommand(
                ExportCommand.COMMAND_WORD + " t/" + type + " f/" + file);

        assertEquals(expectedCommand, command);
    }

    @Test
    public void parseCommand_import_missingPrefixThrowsParseException() {
        // Missing 'f/' prefix
        assertThrows(ParseException.class, () ->
                parser.parseCommand(ImportCommand.COMMAND_WORD + " t/add"));
    }

    @Test
    public void parseCommand_export_missingPrefixThrowsParseException() {
        // Missing 't/' prefix
        assertThrows(ParseException.class, () ->
                parser.parseCommand(ExportCommand.COMMAND_WORD + " f/ testExport"));
    }

    @Test
    public void parseCommand_unrecognisedInput_throwsParseException() {
        assertThrows(ParseException.class, String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE), ()
            -> parser.parseCommand(""));
    }

    @Test
    public void parseCommand_unknownCommand_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("unknownCommand"));
    }
}
