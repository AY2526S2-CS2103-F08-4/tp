package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.tag.Tag;

public class PersonInformationTest {

    @Test
    public void constructor_nullName_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new PersonInformation(null, null, null, null, null));
    }

    @Test
    public void constructor_nullOptionalFields_setsEmptyOptionalsAndEmptyTags() {
        Name name = new Name("Alice");
        PersonInformation info = new PersonInformation(name, null, null, null, null);

        assertEquals(name, info.getName());
        assertEquals(Optional.empty(), info.getPhone());
        assertEquals(Optional.empty(), info.getEmail());
        assertEquals(Optional.empty(), info.getAddress());
        assertTrue(info.getTags().isEmpty());
    }

    @Test
    public void constructor_nonNullFields_setsOptionalsAndCopiesTags() {
        Name name = new Name("Bob");
        Phone phone = new Phone("12345678");
        Email email = new Email("bob@example.com");
        Address address = new Address("123, Clementi Ave 3");

        Set<Tag> tags = new HashSet<>();
        Tag friends = new Tag("friends");
        tags.add(friends);

        PersonInformation info = new PersonInformation(name, phone, email, address, tags);

        assertEquals(name, info.getName());
        assertEquals(Optional.of(phone), info.getPhone());
        assertEquals(Optional.of(email), info.getEmail());
        assertEquals(Optional.of(address), info.getAddress());
        assertTrue(info.getTags().contains(friends));

        tags.add(new Tag("colleagues"));
        assertFalse(info.getTags().contains(new Tag("colleagues")));
    }

    @Test
    public void getTags_modifyReturnedSet_throwsUnsupportedOperationException() {
        PersonInformation info = buildInfo("Bob", "12345678", "bob@example.com",
                "123, Clementi Ave 3", Set.of("friends"));

        assertThrows(UnsupportedOperationException.class, () -> info.getTags().add(new Tag("colleagues")));
    }

    @Test
    public void equals_sameValues_returnsTrue() {
        PersonInformation first = buildInfo("Delwyn", "91234567", "delwyn@example.com",
                "1, Clementi Ave 3", Set.of("CS2103"));
        PersonInformation second = buildInfo("Delwyn", "91234567", "delwyn@example.com",
                "1, Clementi Ave 3", Set.of("CS2103"));

        assertTrue(first.equals(second));
    }

    @Test
    public void equals_differentPhone_returnsFalse() {
        PersonInformation first = buildInfo("Delwyn", "91234567", "delwyn@example.com",
                "1, Clementi Ave 3", Set.of("CS2103"));
        PersonInformation second = buildInfo("Delwyn", "97654321", "delwyn@example.com",
                "1, Clementi Ave 3", Set.of("CS2103"));

        assertFalse(first.equals(second));
    }

    @Test
    public void equals_differentName_returnsFalse() {
        PersonInformation first = buildInfo("Delwyn", "91234567", "delwyn@example.com",
                "1, Clementi Ave 3", Set.of("CS2103"));
        PersonInformation second = buildInfo("Del", "91234567", "delwyn@example.com",
                "1, Clementi Ave 3", Set.of("CS2103"));

        assertFalse(first.equals(second));
    }

    @Test
    public void equals_differentEmail_returnsFalse() {
        PersonInformation first = buildInfo("Delwyn", "91234567", "delwyn@example.com",
                "1, Clementi Ave 3", Set.of("CS2103"));
        PersonInformation second = buildInfo("Delwyn", "91234567", "other@example.com",
                "1, Clementi Ave 3", Set.of("CS2103"));

        assertFalse(first.equals(second));
    }

    @Test
    public void equals_differentAddress_returnsFalse() {
        PersonInformation first = buildInfo("Delwyn", "91234567", "delwyn@example.com",
                "1, Clementi Ave 3", Set.of("CS2103"));
        PersonInformation second = buildInfo("Delwyn", "91234567", "delwyn@example.com",
                "2, Clementi Ave 3", Set.of("CS2103"));

        assertFalse(first.equals(second));
    }

    @Test
    public void equals_differentTags_returnsFalse() {
        PersonInformation first = buildInfo("Delwyn", "91234567", "delwyn@example.com",
                "1, Clementi Ave 3", Set.of("CS2103"));
        PersonInformation second = buildInfo("Delwyn", "91234567", "delwyn@example.com",
                "1, Clementi Ave 3", Set.of("CS2103", "EG1311"));

        assertFalse(first.equals(second));
    }

    @Test
    public void equals_differentTypeOrNull_returnsFalse() {
        PersonInformation info = buildInfo("Delwyn", "91234567", "delwyn@example.com",
                "1, Clementi Ave 3", Set.of("CS2103"));

        assertFalse(info.equals(1));
        assertFalse(info.equals(null));
    }

    @Test
    public void equals_sameObject_returnsTrue() {
        PersonInformation info = buildInfo("Delwyn", "91234567", "delwyn@example.com",
                "1, Clementi Ave 3", Set.of("CS2103"));

        assertTrue(info.equals(info));
    }

    @Test
    public void toString_returnsName() {
        PersonInformation info = buildInfo("Delwyn", "91234567", "delwyn@example.com",
                "1, Clementi Ave 3", Set.of("CS2103"));
        assertEquals("Delwyn", info.toString());
    }

    private static PersonInformation buildInfo(String name, String phone, String email, String address,
                                               Set<String> tags) {
        Set<Tag> tagSet = tags.stream().map(Tag::new).collect(java.util.stream.Collectors.toSet());
        return new PersonInformation(
                new Name(name),
                phone == null ? null : new Phone(phone),
                email == null ? null : new Email(email),
                address == null ? null : new Address(address),
                tagSet);
    }
}
