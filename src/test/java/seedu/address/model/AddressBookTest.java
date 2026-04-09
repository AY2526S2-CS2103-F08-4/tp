package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.event.Description;
import seedu.address.model.event.Event;
import seedu.address.model.event.TimeRange;
import seedu.address.model.event.Title;
import seedu.address.model.event.exceptions.ClashingEventException;
import seedu.address.model.event.exceptions.DuplicateEventException;
import seedu.address.model.person.Person;
import seedu.address.model.person.exceptions.DuplicatePersonException;
import seedu.address.model.person.exceptions.PersonNotFoundException;
import seedu.address.testutil.PersonBuilder;

public class AddressBookTest {

    private final AddressBook addressBook = new AddressBook();

    @Test
    public void constructor() {
        assertEquals(Collections.emptyList(), addressBook.getPersonList());
        assertEquals(Collections.emptyList(), addressBook.getEventList());
        assertEquals(Collections.emptyList(), addressBook.getPinnedPersonList());
    }

    @Test
    public void resetData_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> addressBook.resetData(null));
    }

    @Test
    public void resetData_withValidReadOnlyAddressBook_replacesData() {
        AddressBook newData = getTypicalAddressBook();
        Event event = newEvent("Meeting", "Discuss", "2026-03-25 0900", "2026-03-25 1000");
        newData.addEvent(event);
        addressBook.resetData(newData);
        assertEquals(newData, addressBook);
    }

    @Test
    public void resetData_withDuplicatePersons_throwsDuplicatePersonException() {
        // Two persons with the same identity fields
        Person editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND)
                .build();
        List<Person> newPersons = Arrays.asList(ALICE, editedAlice);
        AddressBookStub newData = new AddressBookStub(newPersons, Collections.emptyList());

        assertThrows(DuplicatePersonException.class, () -> addressBook.resetData(newData));
    }

    @Test
    public void resetData_withDuplicateEvents_throwsDuplicateEventException() {
        Event event = newEvent("Meeting", "Discuss", "2026-03-25 0900", "2026-03-25 1000");
        Event duplicate = newEvent("Meeting", "Other", "2026-03-25 0900", "2026-03-25 1000");
        List<Event> events = Arrays.asList(event, duplicate);
        AddressBookStub newData = new AddressBookStub(Collections.emptyList(), events);
        assertThrows(DuplicateEventException.class, () -> addressBook.resetData(newData));
    }

    @Test
    public void hasPerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> addressBook.hasPerson(null));
    }

    @Test
    public void hasPerson_personNotInAddressBook_returnsFalse() {
        assertFalse(addressBook.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personInAddressBook_returnsTrue() {
        addressBook.addPerson(ALICE);
        assertTrue(addressBook.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personWithSameIdentityFieldsInAddressBook_returnsTrue() {
        addressBook.addPerson(ALICE);
        Person editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND)
                .build();
        assertTrue(addressBook.hasPerson(editedAlice));
    }

    @Test
    public void getPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> addressBook.getPersonList().remove(0));
    }

    @Test
    public void getEventList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> addressBook.getEventList().remove(0));
    }

    @Test
    public void hasEvent_nullEvent_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> addressBook.hasEvent(null));
    }

    @Test
    public void hasEvent_eventNotInAddressBook_returnsFalse() {
        Event event = newEvent("Meeting", "Discuss", "2026-03-25 0900", "2026-03-25 1000");
        assertFalse(addressBook.hasEvent(event));
    }

    @Test
    public void hasEvent_eventInAddressBook_returnsTrue() {
        Event event = newEvent("Meeting", "Discuss", "2026-03-25 0900", "2026-03-25 1000");
        addressBook.addEvent(event);
        assertTrue(addressBook.hasEvent(event));
    }

    @Test
    public void hasOverlappingEvent_nullEvent_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> addressBook.hasOverlappingEvent(null));
    }

    @Test
    public void hasOverlappingEvent_noOverlap_returnsFalse() {
        Event existing = newEvent("Meeting", "Discuss", "2026-03-25 0900", "2026-03-25 1000");
        addressBook.addEvent(existing);
        Event nonOverlapping = newEvent("Lunch", null, "2026-03-25 1200", "2026-03-25 1300");
        assertFalse(addressBook.hasOverlappingEvent(nonOverlapping));
    }

    @Test
    public void hasOverlappingEvent_withOverlap_returnsTrue() {
        Event existing = newEvent("Meeting", "Discuss", "2026-03-25 0900", "2026-03-25 1100");
        addressBook.addEvent(existing);
        Event overlapping = newEvent("Call", null, "2026-03-25 1000", "2026-03-25 1200");
        assertTrue(addressBook.hasOverlappingEvent(overlapping));
    }

    @Test
    public void getOverlappingEvents_noOverlap_returnsEmptyList() {
        Event e1 = newEvent("CS2106", null, "2026-03-25 0900", "2026-03-25 1000");
        Event e2 = newEvent("CS2109S", null, "2026-03-22 0800", "2026-03-23 1300");
        addressBook.addEvent(e1);

        assertTrue(addressBook.getOverlappingEvent(e2).isEmpty());
    }

    @Test
    public void getOverlappingEvents_withOverlaps_returnsOverlappingEvents() {
        Event e1 = newEvent("CS2106", null, "2026-03-25 0900", "2026-03-25 1000");
        addressBook.addEvent(e1);
        Event overlappingEvent = newEvent("CS2109S", null, "2026-03-25 0800", "2026-03-25 1300");

        List<Event> listOfOverlapEvents = addressBook.getOverlappingEvent(overlappingEvent);
        assertEquals(1, listOfOverlapEvents.size());
        assertTrue(listOfOverlapEvents.contains(e1));
    }

    @Test
    public void addEvent_overlappingEvent_throwsClashingEventException() {
        Event existing = newEvent("Meeting", "Discuss", "2026-03-25 0900", "2026-03-25 1100");
        addressBook.addEvent(existing);
        Event overlapping = newEvent("Call", null, "2026-03-25 1000", "2026-03-25 1200");
        assertThrows(ClashingEventException.class, () -> addressBook.addEvent(overlapping));
    }

    @Test
    public void setEvent_replacesEvent_success() {
        Event original = newEvent("Old", "Desc", "2026-03-25 0900", "2026-03-25 1000");
        Event edited = newEvent("New", "Desc", "2026-03-26 0900", "2026-03-26 1000");
        addressBook.addEvent(original);
        addressBook.setEvent(original, edited);
        assertTrue(addressBook.hasEvent(edited));
        assertFalse(addressBook.hasEvent(original));
    }

    @Test
    public void removeEvent_removesEvent_success() {
        Event event = newEvent("Meeting", "Discuss", "2026-03-25 0900", "2026-03-25 1000");
        addressBook.addEvent(event);
        addressBook.removeEvent(event);
        assertFalse(addressBook.hasEvent(event));
    }

    @Test
    public void linkPersonToEvent_incrementsCountAndReturnsExistingEvent() {
        Event event = newEvent("Meeting", "Discuss", "2026-03-25 0900", "2026-03-25 1000");
        addressBook.addEvent(event);

        Event linkRequest = newEvent("Meeting", "Other", "2026-03-25 0900", "2026-03-25 1000");
        Event linked = addressBook.linkPersonToEvent(linkRequest);

        assertEquals(2, event.getNumberOfPersonLinked());
        assertEquals(2, linked.getNumberOfPersonLinked());
    }

    @Test
    public void unlinkPersonFromEvent_decrementsAndRemovesAtZero() {
        Event event = newEvent("Meeting", "Discuss", "2026-03-25 0900", "2026-03-25 1000");
        addressBook.addEvent(event);

        Event unlinked = addressBook.unlinkPersonFromEvent(event);

        assertEquals(0, unlinked.getNumberOfPersonLinked());
        assertFalse(addressBook.hasEvent(event));
    }

    @Test
    public void pinPerson_unpinnedPerson_success() {
        addressBook.addPerson(ALICE);
        assertFalse(addressBook.isPersonPinned(ALICE));

        addressBook.pinPerson(ALICE);

        assertTrue(addressBook.isPersonPinned(ALICE));
        assertEquals(1, addressBook.getPinnedPersonList().size());
    }

    @Test
    public void pinPerson_alreadyPinned_noChange() {
        addressBook.addPerson(ALICE);
        addressBook.pinPerson(ALICE);
        addressBook.pinPerson(ALICE);

        // Should still be pinned, not added twice
        assertEquals(1, addressBook.getPinnedPersonList().size());
        assertTrue(addressBook.isPersonPinned(ALICE));
    }

    @Test
    public void pinPerson_notInAddressBook_noChange() {
        addressBook.pinPerson(ALICE);

        // Pin should not be added if person not in address book
        assertEquals(0, addressBook.getPinnedPersonList().size());
        assertFalse(addressBook.isPersonPinned(ALICE));
    }

    @Test
    public void unpinPerson_pinnedPerson_success() {
        addressBook.addPerson(ALICE);
        addressBook.pinPerson(ALICE);
        assertTrue(addressBook.isPersonPinned(ALICE));

        addressBook.unpinPerson(ALICE);

        assertFalse(addressBook.isPersonPinned(ALICE));
        assertEquals(0, addressBook.getPinnedPersonList().size());
    }

    @Test
    public void unpinPerson_unpinnedPerson_noChange() {
        addressBook.addPerson(ALICE);
        assertThrows(PersonNotFoundException.class, () -> addressBook.unpinPerson(ALICE));

        assertFalse(addressBook.isPersonPinned(ALICE));
        assertEquals(0, addressBook.getPinnedPersonList().size());
    }

    @Test
    public void isPersonPinned_pinnedPerson_returnsTrue() {
        addressBook.addPerson(ALICE);
        addressBook.pinPerson(ALICE);

        assertTrue(addressBook.isPersonPinned(ALICE));
    }

    @Test
    public void isPersonPinned_unpinnedPerson_returnsFalse() {
        addressBook.addPerson(ALICE);

        assertFalse(addressBook.isPersonPinned(ALICE));
    }

    @Test
    public void setPerson_pinnedPersonEdited_updatesBothLists() {
        addressBook.addPerson(ALICE);
        addressBook.pinPerson(ALICE);

        Person editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND)
                .build();
        addressBook.setPerson(ALICE, editedAlice);

        assertTrue(addressBook.hasPerson(editedAlice));
        assertTrue(addressBook.isPersonPinned(editedAlice));
        assertEquals(1, addressBook.getPinnedPersonList().size());
    }

    @Test
    public void setPerson_unpinnedPersonEdited_onlyUpdatesMainList() {
        addressBook.addPerson(ALICE);
        Person editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND)
                .build();
        addressBook.setPerson(ALICE, editedAlice);

        assertTrue(addressBook.hasPerson(editedAlice));
        assertFalse(addressBook.isPersonPinned(editedAlice));
    }

    @Test
    public void removePerson_pinnedPersonRemoved_removesFromBothLists() {
        addressBook.addPerson(ALICE);
        addressBook.pinPerson(ALICE);

        addressBook.removePerson(ALICE);

        assertFalse(addressBook.hasPerson(ALICE));
        assertEquals(0, addressBook.getPinnedPersonList().size());
    }

    @Test
    public void removePerson_unpinnedPersonRemoved_onlyRemovesFromMainList() {
        addressBook.addPerson(ALICE);
        addressBook.removePerson(ALICE);

        assertFalse(addressBook.hasPerson(ALICE));
        assertEquals(0, addressBook.getPinnedPersonList().size());
    }

    @Test
    public void getPinnedPersonList_modifyList_throwsUnsupportedOperationException() {
        addressBook.addPerson(ALICE);
        addressBook.pinPerson(ALICE);

        assertThrows(UnsupportedOperationException.class, () -> addressBook.getPinnedPersonList().remove(0));
    }

    @Test
    public void toStringMethod() {
        String expected = AddressBook.class.getCanonicalName()
                + "{persons=" + addressBook.getPersonList()
            + ", events=" + addressBook.getEventList()
            + ", pinned=" + addressBook.getPinnedPersonList() + "}";
        assertEquals(expected, addressBook.toString());
    }

    @Test
    public void equals() {
        AddressBook book = new AddressBook();
        AddressBook bookCopy = new AddressBook(book);

        // same values -> returns true
        assertTrue(book.equals(bookCopy));

        // same object -> returns true
        assertTrue(book.equals(book));

        // null -> returns false
        assertFalse(book.equals(null));

        // different type -> returns false
        assertFalse(book.equals(5));

        // different events -> returns false
        Event event = newEvent("Meeting", "Discuss", "2026-03-25 0900", "2026-03-25 1000");
        bookCopy.addEvent(event);
        assertFalse(book.equals(bookCopy));
    }

    @Test
    public void hashCode_sameContent_sameHash() {
        AddressBook book = new AddressBook();
        AddressBook bookCopy = new AddressBook(book);
        assertEquals(book.hashCode(), bookCopy.hashCode());
    }

    private static Event newEvent(String title, String description, String start, String end) {
        return new Event(new Title(title),
                description == null ? Optional.empty() : Optional.of(new Description(description)),
                new TimeRange(start, end));
    }

    /**
     * A stub ReadOnlyAddressBook whose persons list can violate interface constraints.
     */
    private static class AddressBookStub implements ReadOnlyAddressBook {
        private final ObservableList<Person> persons = FXCollections.observableArrayList();
        private final ObservableList<Event> events = FXCollections.observableArrayList();
        private final ObservableList<Person> pinned = FXCollections.observableArrayList();

        AddressBookStub(Collection<Person> persons, Collection<Event> events) {
            this.persons.setAll(persons);
            this.events.setAll(events);
        }

        @Override
        public ObservableList<Person> getPersonList() {
            return persons;
        }

        @Override
        public ObservableList<Event> getEventList() {
            return events;
        }

        @Override
        public ObservableList<Person> getPinnedPersonList() {
            return pinned;
        }
    }

}
