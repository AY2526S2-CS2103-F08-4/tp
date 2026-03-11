package seedu.address.model.tag;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class TagTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Tag(null));
    }

    @Test
    public void constructor_invalidTagName_throwsIllegalArgumentException() {
        String invalidTagName = "";
        assertThrows(IllegalArgumentException.class, () -> new Tag(invalidTagName));
    }

    @Test
    public void isValidTagName() {
        // null tag name
        assertThrows(NullPointerException.class, () -> Tag.isValidTagName(null));

        // invalid tag names
        assertFalse(Tag.isValidTagName("")); // empty string
        assertFalse(Tag.isValidTagName(" friends")); // leading space (first char must be alphanumeric)
        assertFalse(Tag.isValidTagName("friend!")); // special character not allowed
        assertFalse(Tag.isValidTagName("aVeryLongTagNameThatExceedsLimit")); // exceeds 20 characters

        // valid tag names
        assertTrue(Tag.isValidTagName("friends")); // alphanumeric only
        assertTrue(Tag.isValidTagName("CS2103 Team")); // with space
        assertTrue(Tag.isValidTagName("group_A")); // with underscore
        assertTrue(Tag.isValidTagName("year-1")); // with hyphen
        assertTrue(Tag.isValidTagName("twentycharacterslong")); // exactly 20 characters
    }

}
