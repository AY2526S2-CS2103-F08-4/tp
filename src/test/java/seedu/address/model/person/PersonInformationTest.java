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

        assertEquals(name, info.name);
        assertEquals(Optional.empty(), info.phone);
        assertEquals(Optional.empty(), info.email);
        assertEquals(Optional.empty(), info.address);
        assertTrue(info.tags.isEmpty());
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

        assertEquals(name, info.name);
        assertEquals(Optional.of(phone), info.phone);
        assertEquals(Optional.of(email), info.email);
        assertEquals(Optional.of(address), info.address);
        assertTrue(info.tags.contains(friends));

        tags.add(new Tag("colleagues"));
        assertFalse(info.tags.contains(new Tag("colleagues")));
    }
}
