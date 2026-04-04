package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHOTO;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;
import static seedu.address.testutil.TypicalPersons.CARL;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.model.event.Event;
import seedu.address.model.event.TimeRange;
import seedu.address.model.event.Title;
import seedu.address.testutil.PersonBuilder;

public class PersonTest {

    @Test
    public void asObservableList_modifyList_throwsUnsupportedOperationException() {
        Person person = new PersonBuilder().build();
        assertThrows(UnsupportedOperationException.class, () -> person.getTags().remove(0));
    }

    @Test
    public void isSamePerson() {
        // same object -> returns true
        assertTrue(ALICE.isSamePerson(ALICE));

        // null -> returns false
        assertFalse(ALICE.isSamePerson(null));

        // same phone, all other attributes different -> returns true
        Person editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).withEmail(VALID_EMAIL_BOB)
                .withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND).build();
        assertTrue(ALICE.isSamePerson(editedAlice));

        // different phone, all other attributes same -> returns false
        editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).build();
        assertFalse(ALICE.isSamePerson(editedAlice));

    }

    @Test
    public void equals() {
        // same values -> returns true
        Person aliceCopy = new PersonBuilder(ALICE).build();
        assertTrue(ALICE.equals(aliceCopy));

        // same object -> returns true
        assertTrue(ALICE.equals(ALICE));

        // null -> returns false
        assertFalse(ALICE.equals(null));

        // different type -> returns false
        assertFalse(ALICE.equals(5));

        // different person -> returns false
        assertFalse(ALICE.equals(BOB));

        // different name -> returns false
        Person editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different phone -> returns false
        editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different email -> returns false
        editedAlice = new PersonBuilder(ALICE).withEmail(VALID_EMAIL_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different address -> returns false
        editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different tags -> returns false
        editedAlice = new PersonBuilder(ALICE).withTags(VALID_TAG_HUSBAND).build();
        assertFalse(ALICE.equals(editedAlice));

        // different events -> returns false
        editedAlice = new PersonBuilder(ALICE)
                .withEvents("Project meeting,2026-02-21 1100,2026-02-21 1200")
                .build();
        assertFalse(ALICE.equals(editedAlice));

        // different photo -> return false
        editedAlice = new PersonBuilder(ALICE).withPhoto(VALID_PHOTO).build();
        assertFalse(ALICE.equals(editedAlice));
    }

    @Test
    public void constructor_withEventList_setsEventsCorrectly() {
        Event event = new Event(new Title("Meeting"), Optional.empty(),
                new TimeRange("2026-02-21 1100", "2026-02-21 1500"));
        Person person = new Person(ALICE.getName(), ALICE.getPhone(),
                ALICE.getEmail(), ALICE.getAddress(), Collections.emptySet(), List.of(event), ALICE.getPhoto());
        assertEquals(ALICE.getName(), person.getName());
        assertTrue(person.getEvents().contains(event));
    }

    @Test
    public void hasEvent_eventPresent_returnsTrue() {
        Person person = new PersonBuilder().build();
        Event event = new Event(new Title("Meeting"), Optional.empty(),
                new TimeRange("2026-03-25 0900", "2026-03-25 1000"));
        person.addEvent(event);
        assertTrue(person.hasEvent(event));
    }

    @Test
    public void hasEvent_eventAbsent_returnsFalse() {
        Person person = new PersonBuilder().build();
        Event event = new Event(new Title("Meeting"), Optional.empty(),
                new TimeRange("2026-03-25 0900", "2026-03-25 1000"));
        assertFalse(person.hasEvent(event));
    }

    @Test
    public void removeEvent_eventPresent_removesEvent() {
        Person person = new PersonBuilder().build();
        Event event = new Event(new Title("Meeting"), Optional.empty(),
                new TimeRange("2026-03-25 0900", "2026-03-25 1000"));
        person.addEvent(event);
        person.removeEvent(event);
        assertFalse(person.hasEvent(event));
    }

    @Test
    public void removeEvent_eventAbsent_noChange() {
        Person person = new PersonBuilder().build();
        Event event = new Event(new Title("Meeting"), Optional.empty(),
                new TimeRange("2026-03-25 0900", "2026-03-25 1000"));
        person.removeEvent(event); // should not throw
        assertTrue(person.getEvents().isEmpty());
    }

    @Test
    public void toStringMethod() {
        String expected = Person.class.getCanonicalName() + "{name=" + ALICE.getName() + ", phone=" + ALICE.getPhone()
                + ", email=" + ALICE.getEmail().map(email -> email.toString()).orElse("")
                + ", address=" + ALICE.getAddress().map(addr -> addr.toString()).orElse("")
                + ", tags=" + ALICE.getTags() + ", events=" + ALICE.getEvents()
                + ", photo=" + ALICE.getPhoto().map(Object::toString).orElse("") + "}";
        assertEquals(expected, ALICE.toString());
    }

    @Test
    public void delegatorMethod() {
        assertEquals("Alice Pauline", ALICE.getNameString());
        assertEquals("94351253", ALICE.getPhoneString());
        assertEquals(Optional.of("123, Jurong West Ave 6, #08-111"), ALICE.getAddressString());
        assertEquals(Optional.of("alice@example.com"), ALICE.getEmailString());
        assertEquals("Tags: friends", ALICE.getTagsString());
        assertEquals("", CARL.getTagsString());
        assertEquals(Optional.empty(), CARL.getPhotoPath());
        assertEquals(List.of("friends"), ALICE.getTagNames());
    }
}
