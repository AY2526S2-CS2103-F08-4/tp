package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_EVENTS;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.GuiSettings;
import seedu.address.model.event.Description;
import seedu.address.model.event.Event;
import seedu.address.model.event.TimeRange;
import seedu.address.model.event.Title;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonInformation;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

public class ModelManagerTest {

    private ModelManager modelManager = new ModelManager();

    private static Event newEvent(String title, String description, String startTime, String endTime) {
        return new Event(new Title(title),
                description == null ? Optional.empty() : Optional.of(new Description(description)),
                new TimeRange(startTime, endTime));
    }

    @Test
    public void constructor() {
        assertEquals(new UserPrefs(), modelManager.getUserPrefs());
        assertEquals(new GuiSettings(), modelManager.getGuiSettings());
        assertEquals(new AddressBook(), new AddressBook(modelManager.getAddressBook()));
    }

    @Test
    public void setUserPrefs_nullUserPrefs_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.setUserPrefs(null));
    }

    @Test
    public void setUserPrefs_validUserPrefs_copiesUserPrefs() {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setAddressBookFilePath(Paths.get("address/book/file/path"));
        userPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        modelManager.setUserPrefs(userPrefs);
        assertEquals(userPrefs, modelManager.getUserPrefs());

        // Modifying userPrefs should not modify modelManager's userPrefs
        UserPrefs oldUserPrefs = new UserPrefs(userPrefs);
        userPrefs.setAddressBookFilePath(Paths.get("new/address/book/file/path"));
        assertEquals(oldUserPrefs, modelManager.getUserPrefs());
    }

    @Test
    public void setGuiSettings_nullGuiSettings_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.setGuiSettings(null));
    }

    @Test
    public void setGuiSettings_validGuiSettings_setsGuiSettings() {
        GuiSettings guiSettings = new GuiSettings(1, 2, 3, 4);
        modelManager.setGuiSettings(guiSettings);
        assertEquals(guiSettings, modelManager.getGuiSettings());
    }

    @Test
    public void setAddressBookFilePath_nullPath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.setAddressBookFilePath(null));
    }

    @Test
    public void setAddressBookFilePath_validPath_setsAddressBookFilePath() {
        Path path = Paths.get("address/book/file/path");
        modelManager.setAddressBookFilePath(path);
        assertEquals(path, modelManager.getAddressBookFilePath());
    }

    @Test
    public void hasPerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.hasPerson(null));
    }

    @Test
    public void hasPerson_personNotInAddressBook_returnsFalse() {
        assertFalse(modelManager.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personInAddressBook_returnsTrue() {
        modelManager.addPerson(ALICE);
        assertTrue(modelManager.hasPerson(ALICE));
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> modelManager.getFilteredPersonList().remove(0));
    }

    @Test
    public void getFilteredEventList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> modelManager.getFilteredEventList().remove(0));
    }

    @Test
    public void updateFilteredEventList_nullPredicate_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.updateFilteredEventList(null));
    }

    @Test
    public void hasEvent_nullEvent_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.hasEvent(null));
    }

    @Test
    public void hasEvent_eventNotInAddressBook_returnsFalse() {
        Event event = newEvent("Project Meeting", "Discuss milestones",
                "2026-03-20 0900", "2026-03-20 1000");
        assertFalse(modelManager.hasEvent(event));
    }

    @Test
    public void addEvent_eventAdded_updatesFilteredEventList() {
        Event event = newEvent("Consultation", "Client sync",
                "2026-03-25 1300", "2026-03-25 1400");
        modelManager.addEvent(event);
        assertTrue(modelManager.hasEvent(event));
        assertEquals(1, modelManager.getFilteredEventList().size());
    }

    @Test
    public void deleteEvent_removesEventFromFilteredEventList() {
        Event event = newEvent("Delete Event", "Cleanup",
                "2026-03-26 0900", "2026-03-26 1000");
        modelManager.addEvent(event);
        assertEquals(1, modelManager.getFilteredEventList().size());

        modelManager.deleteEvent(event);
        assertEquals(0, modelManager.getFilteredEventList().size());
        assertFalse(modelManager.hasEvent(event));
    }

    @Test
    public void setEvent_replacesEvent_success() {
        Event original = newEvent("Old Event", "Old desc",
                "2026-03-23 0900", "2026-03-23 1000");
        Event edited = newEvent("New Event", "New desc",
                "2026-03-24 0900", "2026-03-24 1000");
        modelManager.addEvent(original);
        modelManager.setEvent(original, edited);
        assertFalse(modelManager.hasEvent(original));
        assertTrue(modelManager.hasEvent(edited));
    }

    @Test
    public void linkPersonToEvent_incrementsCount() {
        Event event = newEvent("Team Meeting", "Discuss milestones",
                "2026-03-20 0900", "2026-03-20 1000");
        modelManager.addEvent(event);

        Event linkRequest = newEvent("Team Meeting", "Other",
                "2026-03-20 0900", "2026-03-20 1000");
        Event linked = modelManager.linkPersonToEvent(linkRequest);

        assertEquals(2, linked.getNumberOfPersonLinked());
        assertEquals(2, event.getNumberOfPersonLinked());
    }

    @Test
    public void unlinkPersonFromEvent_decrementsAndRemoves() {
        Event event = newEvent("Cleanup", "Wrap up",
                "2026-03-25 1300", "2026-03-25 1400");
        modelManager.addEvent(event);

        Event unlinked = modelManager.unlinkPersonFromEvent(event);

        assertEquals(0, unlinked.getNumberOfPersonLinked());
        assertFalse(modelManager.hasEvent(event));
    }

    @Test
    public void hasOverlappingEvent_nullEvent_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.hasOverlappingEvent(null));
    }

    @Test
    public void hasOverlappingEvent_noOverlap_returnsFalse() {
        Event event = newEvent("Meeting", "Discuss", "2026-03-25 0900", "2026-03-25 1000");
        modelManager.addEvent(event);
        Event nonOverlapping = newEvent("Lunch", null, "2026-03-25 1200", "2026-03-25 1300");
        assertFalse(modelManager.hasOverlappingEvent(nonOverlapping));
    }

    @Test
    public void hasOverlappingEvent_withOverlap_returnsTrue() {
        Event event = newEvent("Meeting", "Discuss", "2026-03-25 0900", "2026-03-25 1100");
        modelManager.addEvent(event);
        Event overlapping = newEvent("Call", null, "2026-03-25 1000", "2026-03-25 1200");
        assertTrue(modelManager.hasOverlappingEvent(overlapping));
    }

    @Test
    public void updateFilteredEventList_filtersByTitle() {
        Event meeting = newEvent("Team Meeting", "Discuss milestones",
                "2026-03-20 0900", "2026-03-20 1000");
        Event review = newEvent("Code Review", "Quality check",
                "2026-03-21 1400", "2026-03-21 1500");
        modelManager.addEvent(meeting);
        modelManager.addEvent(review);

        modelManager.updateFilteredEventList(event -> event.getTitle().toString().contains("Meeting"));
        assertEquals(1, modelManager.getFilteredEventList().size());
        assertTrue(modelManager.getFilteredEventList().contains(meeting));
    }

    @Test
    public void equals() {
        AddressBook addressBook = new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON).build();
        AddressBook differentAddressBook = new AddressBook();
        UserPrefs userPrefs = new UserPrefs();

        // same values -> returns true
        modelManager = new ModelManager(addressBook, userPrefs);
        ModelManager modelManagerCopy = new ModelManager(addressBook, userPrefs);
        assertTrue(modelManager.equals(modelManagerCopy));

        // same object -> returns true
        assertTrue(modelManager.equals(modelManager));

        // null -> returns false
        assertFalse(modelManager.equals(null));

        // different types -> returns false
        assertFalse(modelManager.equals(5));

        // different addressBook -> returns false
        assertFalse(modelManager.equals(new ModelManager(differentAddressBook, userPrefs)));

        // different filteredList -> returns false
        String[] keywords = ALICE.getName().fullName.split("\\s+");
        modelManager.updateFilteredPersonList(new NameContainsKeywordsPredicate(Arrays.asList(keywords)));
        assertFalse(modelManager.equals(new ModelManager(addressBook, userPrefs)));

        // resets modelManager to initial state for upcoming tests
        modelManager.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        modelManager.updateFilteredEventList(PREDICATE_SHOW_ALL_EVENTS);

        // different userPrefs -> returns false
        UserPrefs differentUserPrefs = new UserPrefs();
        differentUserPrefs.setAddressBookFilePath(Paths.get("differentFilePath"));
        assertFalse(modelManager.equals(new ModelManager(addressBook, differentUserPrefs)));
    }

    @Test
    public void showAllPersons_resetsPersonFilter() {
        modelManager.addPerson(ALICE);
        modelManager.addPerson(BENSON);
        modelManager.updateFilteredPersonList(p -> p.equals(ALICE));
        assertEquals(1, modelManager.getFilteredPersonList().size());

        modelManager.showAllPersons();
        assertEquals(2, modelManager.getFilteredPersonList().size());
    }

    @Test
    public void showPersons_filtersByPredicate() {
        modelManager.addPerson(ALICE);
        modelManager.addPerson(BENSON);

        modelManager.showPersons(p -> p.equals(ALICE));
        assertEquals(1, modelManager.getFilteredPersonList().size());
        assertEquals(ALICE, modelManager.getFilteredPersonList().get(0));
    }

    @Test
    public void showAllPersonsPinnedFirst_ordersPinnedPersonsBeforeUnpinnedPersons() {
        Person first = new PersonBuilder().withName("Alpha One").withPhone("90000001").build();
        Person second = new PersonBuilder().withName("Beta Two").withPhone("90000002").build();
        Person third = new PersonBuilder().withName("Gamma Three").withPhone("90000003").build();

        modelManager.addPerson(first);
        modelManager.addPerson(second);
        modelManager.addPerson(third);

        modelManager.pinPerson(second);
        modelManager.pinPerson(first);

        modelManager.showAllPersonsPinnedFirst();

        assertEquals(second, modelManager.getFilteredPersonList().get(0));
        assertEquals(first, modelManager.getFilteredPersonList().get(1));
        assertEquals(third, modelManager.getFilteredPersonList().get(2));
    }

    @Test
    public void showMatchingPersons_showsOnlyMatchingPersonsAndClearsEvents() {
        modelManager.addPerson(ALICE);
        modelManager.addPerson(BENSON);
        Event event = newEvent("Meeting", null, "2026-03-25 0900", "2026-03-25 1000");
        modelManager.addEvent(event);

        modelManager.showMatchingPersons(Set.of(ALICE));

        assertEquals(1, modelManager.getFilteredPersonList().size());
        assertEquals(ALICE, modelManager.getFilteredPersonList().get(0));
        assertEquals(0, modelManager.getFilteredEventList().size());
    }

    @Test
    public void showEventsForPerson_filtersPersonAndTheirEvents() {
        Person personWithEvent = new PersonBuilder().withName("Eve Tan").withPhone("91110000").build();
        modelManager.addPerson(personWithEvent);
        modelManager.addPerson(BENSON);
        Event event = newEvent("Consult", null, "2026-03-27 0900", "2026-03-27 1000");
        modelManager.addEvent(event);
        personWithEvent.addEvent(event);

        modelManager.showEventsForPerson(personWithEvent);

        assertEquals(1, modelManager.getFilteredPersonList().size());
        assertEquals(personWithEvent, modelManager.getFilteredPersonList().get(0));
        assertEquals(1, modelManager.getFilteredEventList().size());
        assertTrue(modelManager.getFilteredEventList().contains(event));
    }

    @Test
    public void showEventsForPerson_sameIdentityDifferentInstance_filtersStoredPersonAndEvents() {
        Person storedPerson = new PersonBuilder().withName("Eve Tan").withPhone("91110000").build();
        modelManager.addPerson(storedPerson);
        modelManager.addPerson(BENSON);

        Event existingEvent = newEvent("Consult", null, "2026-03-27 0900", "2026-03-27 1000");
        modelManager.addEvent(existingEvent);
        storedPerson.addEvent(existingEvent);

        Event newEvent = newEvent("Review", null, "2026-03-28 0900", "2026-03-28 1000");
        Person editedCopy = new PersonBuilder().withName("Eve Tan").withPhone("91110000").build();
        editedCopy.addEvent(existingEvent);
        editedCopy.addEvent(newEvent);

        assertTrue(storedPerson.isSamePerson(editedCopy));
        assertFalse(storedPerson.equals(editedCopy));

        modelManager.showEventsForPerson(editedCopy);

        assertEquals(1, modelManager.getFilteredPersonList().size());
        assertEquals(storedPerson, modelManager.getFilteredPersonList().get(0));
        assertEquals(1, modelManager.getFilteredEventList().size());
        assertTrue(modelManager.getFilteredEventList().contains(existingEvent));
    }

    @Test
    public void findPersons_matchesByNameOnly_returnsMatchingPersons() {
        modelManager.addPerson(ALICE);
        modelManager.addPerson(BENSON);

        PersonInformation info = new PersonInformation(new Name("Alice Pauline"), null, null, null, Set.of());

        List<Person> matches = modelManager.findPersons(info);
        assertEquals(1, matches.size());
        assertEquals(ALICE, matches.get(0));
    }

    @Test
    public void findPersons_matchesWithOptionalFields_returnsMatchingPersons() {
        Person custom = new PersonBuilder()
                .withName("Alex Tan")
                .withPhone("91234567")
                .withEmail("alex@example.com")
                .withAddress("123, Clementi Ave 3")
                .withTags("friends")
                .build();

        modelManager.addPerson(custom);
        modelManager.addPerson(ALICE);

        PersonInformation info = new PersonInformation(
                new Name("Alex Tan"),
                new Phone("91234567"),
                null,
                null,
                Set.of(new Tag("friends")));

        List<Person> matches = modelManager.findPersons(info);
        assertEquals(1, matches.size());
        assertEquals(custom, matches.get(0));
    }

    @Test
    public void findPersons_optionalEmailProvided_notMatched() {
        Person noEmail = new PersonBuilder()
                .withName("No Email")
                .withPhone("90000000")
                .withoutEmail()
                .build();

        modelManager.addPerson(noEmail);

        PersonInformation info = new PersonInformation(
                new Name("No Email"),
                null,
                new Email("noemail@example.com"),
                null,
                Set.of());

        List<Person> matches = modelManager.findPersons(info);
        assertEquals(0, matches.size());
    }

    @Test
    public void findPersons_tagsRequireAllTags_returnsMatchingPersons() {
        Person tagged = new PersonBuilder()
                .withName("Tagged Person")
                .withPhone("98887766")
                .withTags("friends", "colleagues")
                .build();

        modelManager.addPerson(tagged);

        PersonInformation info = new PersonInformation(
                new Name("Tagged Person"),
                null,
                null,
                null,
                Set.of(new Tag("friends"), new Tag("colleagues")));

        List<Person> matches = modelManager.findPersons(info);
        assertEquals(1, matches.size());
        assertEquals(tagged, matches.get(0));
    }

    @Test
    public void findPersons_optionalFieldNarrowsResults_returnsSingleMatch() {
        Person personOne = new PersonBuilder()
                .withName("Alex Tan")
                .withPhone("90001111")
                .withEmail("alex1@example.com")
                .build();
        Person personTwo = new PersonBuilder()
                .withName("Alex Tan")
                .withPhone("90002222")
                .withEmail("alex2@example.com")
                .build();

        modelManager.addPerson(personOne);
        modelManager.addPerson(personTwo);

        PersonInformation nameOnly = new PersonInformation(
                new Name("Alex Tan"),
                null,
                null,
                null,
                Set.of());
        List<Person> nameOnlyMatches = modelManager.findPersons(nameOnly);
        assertEquals(2, nameOnlyMatches.size());

        PersonInformation withPhone = new PersonInformation(
                new Name("Alex Tan"),
                new Phone("90001111"),
                null,
                null,
                Set.of());
        List<Person> narrowedMatches = modelManager.findPersons(withPhone);
        assertEquals(1, narrowedMatches.size());
        assertEquals(personOne, narrowedMatches.get(0));
    }

    @Test
    public void findPersons_emailExactMatch_returnsMatchingPerson() {
        Person person = new PersonBuilder()
                .withName("Email Match")
                .withPhone("93334444")
                .withEmail("emailmatch@example.com")
                .build();
        Person other = new PersonBuilder()
                .withName("Email Match")
                .withPhone("93335555")
                .withEmail("other@example.com")
                .build();

        modelManager.addPerson(person);
        modelManager.addPerson(other);

        PersonInformation info = new PersonInformation(
                new Name("Email Match"),
                null,
                new Email("emailmatch@example.com"),
                null,
                Set.of());

        List<Person> matches = modelManager.findPersons(info);
        assertEquals(1, matches.size());
        assertEquals(person, matches.get(0));
    }

    @Test
    public void findPersons_addressExactMatch_returnsMatchingPerson() {
        Person person = new PersonBuilder()
                .withName("Address Match")
                .withPhone("94445555")
                .withAddress("Block 123, Clementi Ave 3")
                .build();
        Person other = new PersonBuilder()
                .withName("Address Match")
                .withPhone("94446666")
                .withAddress("Block 456, Clementi Ave 3")
                .build();

        modelManager.addPerson(person);
        modelManager.addPerson(other);

        PersonInformation info = new PersonInformation(
                new Name("Address Match"),
                null,
                null,
                new seedu.address.model.person.Address("Block 123, Clementi Ave 3"),
                Set.of());

        List<Person> matches = modelManager.findPersons(info);
        assertEquals(1, matches.size());
        assertEquals(person, matches.get(0));
    }

    @Test
    public void findPersons_tagsAllMatch_expectedResults() {
        Person personA = new PersonBuilder()
                .withName("Delwyn")
                .withPhone("91112222")
                .withEmail("delwyn1@example.com")
                .withAddress("1, Clementi Ave 3")
                .withTags("CS2103")
                .build();
        Person personB = new PersonBuilder()
                .withName("Delwyn")
                .withPhone("91113333")
                .withEmail("delwyn2@example.com")
                .withAddress("1, Clementi Ave 3")
                .withTags("CS2103", "EG1131")
                .build();

        modelManager.addPerson(personA);
        modelManager.addPerson(personB);

        PersonInformation nameOnly = new PersonInformation(
                new Name("Delwyn"),
                null,
                null,
                null,
                Set.of());
        List<Person> nameOnlyMatches = modelManager.findPersons(nameOnly);
        assertEquals(2, nameOnlyMatches.size());

        PersonInformation withCs2103 = new PersonInformation(
                new Name("Delwyn"),
                null,
                null,
                null,
                Set.of(new Tag("CS2103")));
        List<Person> cs2103Matches = modelManager.findPersons(withCs2103);
        assertEquals(2, cs2103Matches.size());

        PersonInformation withBothTags = new PersonInformation(
                new Name("Delwyn"),
                null,
                null,
                null,
                Set.of(new Tag("CS2103"), new Tag("EG1131")));
        List<Person> bothTagsMatches = modelManager.findPersons(withBothTags);
        assertEquals(1, bothTagsMatches.size());
        assertEquals(personB, bothTagsMatches.get(0));

        PersonInformation withEg1131 = new PersonInformation(
                new Name("Delwyn"),
                null,
                null,
                null,
                Set.of(new Tag("EG1131")));
        List<Person> eg1131Matches = modelManager.findPersons(withEg1131);
        assertEquals(1, eg1131Matches.size());
        assertEquals(personB, eg1131Matches.get(0));

        PersonInformation withWrongPhone = new PersonInformation(
                new Name("Delwyn"),
                new Phone("67676767"),
                null,
                null,
                Set.of(new Tag("EG1131")));
        List<Person> withWrongPhoneMatches = modelManager.findPersons(withWrongPhone);
        assertEquals(0, withWrongPhoneMatches.size());
    }
}
