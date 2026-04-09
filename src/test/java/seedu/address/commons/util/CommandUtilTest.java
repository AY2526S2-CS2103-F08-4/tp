package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonInformation;
import seedu.address.model.person.Photo;
import seedu.address.testutil.PersonBuilder;

public class CommandUtilTest {

    @Test
    public void constructor_isPrivate_cannotBeInvoked() throws Exception {
        Constructor<CommandUtil> constructor = CommandUtil.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);

        try {
            constructor.newInstance();
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            assertTrue(cause instanceof AssertionError);
            assertEquals("This class should not be instantiated.", cause.getMessage());
        }
    }

    @Test
    public void targetPerson_singleMatch_returnsPerson() throws Exception {
        Model model = new ModelManager();
        model.addPerson(ALICE);

        PersonInformation info = new PersonInformation(new Name("Alice Pauline"), null, null, null, Set.of());
        Person matched = CommandUtil.targetPerson(model, info);

        assertEquals(ALICE, matched);
    }

    @Test
    public void targetPerson_noMatch_throwsCommandException() {
        Model model = new ModelManager();
        model.addPerson(ALICE);

        PersonInformation info = new PersonInformation(new Name("Nonexistent"), null, null, null, Set.of());

        assertThrows(CommandException.class, Messages.MESSAGE_NO_MATCH, () ->
                CommandUtil.targetPerson(model, info));
    }

    @Test
    public void targetPerson_multipleMatches_throwsCommandExceptionAndShowsMatchingPersons() {
        Model model = new ModelManager();
        Person first = new PersonBuilder().withName("Alex Tan").withPhone("90001111").build();
        Person second = new PersonBuilder().withName("Alex Tan").withPhone("90002222").build();
        model.addPerson(first);
        model.addPerson(second);

        PersonInformation info = new PersonInformation(new Name("Alex Tan"), null, null, null, Set.of());

        assertThrows(CommandException.class, Messages.MESSAGE_MULTIPLE_MATCH, () ->
                CommandUtil.targetPerson(model, info));

        assertEquals(2, model.getFilteredPersonList().size());
        assertTrue(model.getFilteredPersonList().contains(first));
        assertTrue(model.getFilteredPersonList().contains(second));
    }

    @Test
    public void safelyDeletePhoto_photoIsNotShared_deletesImageFile(@TempDir Path tempDir) throws Exception {
        // Setup tempDir
        Path dummyImagePath = tempDir.resolve("test_image.png");
        String tempDirString = PhotoStorageUtil.formatPath(tempDir);

        // Setup dummyFolder
        Files.createFile(dummyImagePath);
        Photo dummyPhoto = new Photo(dummyImagePath.toString());


        // Setup model
        Model model = new ModelManager();
        Person personA = new PersonBuilder(ALICE).withPhoto(dummyPhoto.getPath()).build();
        model.addPerson(personA);

        // Test
        CommandUtil.safelyDeletePhoto(model, personA, dummyPhoto, tempDirString);
        assertTrue(Files.notExists(dummyImagePath));
    }

    @Test
    public void safelyDeletePhoto_photoIsShared_doesNotDeleteImageFile(@TempDir Path tempDir) throws Exception {
        // Setup tempDir
        Path dummyImagePath = tempDir.resolve("test_image.png");
        String tempDirString = PhotoStorageUtil.formatPath(tempDir);

        // Setup dummyFolder
        Files.createFile(dummyImagePath);
        Photo dummyPhoto = new Photo(dummyImagePath.toString());

        // Setup model
        Model model = new ModelManager();
        Person personA = new PersonBuilder(ALICE).withPhoto(dummyPhoto.getPath()).build();
        Person personB = new PersonBuilder(BOB).withPhoto(dummyPhoto.getPath()).build();
        model.addPerson(personA);
        model.addPerson(personB);

        // Test
        CommandUtil.safelyDeletePhoto(model, personA, dummyPhoto, tempDirString);
        assertTrue(Files.exists(dummyImagePath));

    }

}
