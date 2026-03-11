package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class EventTest {

    private static final String DESCRIPTION = "Complete feature list";
    private static final String START = "21-02-26 1100";
    private static final String END = "21-02-26 1500";
    private static final String NAME = "Amy Bee";

    @Test
    public void constructor_nullField_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Event(null, START, END));
        assertThrows(NullPointerException.class, () -> new Event(DESCRIPTION, null, END));
        assertThrows(NullPointerException.class, () -> new Event(DESCRIPTION, START, null));
    }

    @Test
    public void getters() {
        Event event = new Event(DESCRIPTION, START, END);
        assertEquals(DESCRIPTION, event.getDescription());
        assertEquals(START, event.getStartTime());
        assertEquals(END, event.getEndTime());
    }

    @Test
    public void equals() {
        Event event = new Event(DESCRIPTION, START, END);

        // same values -> returns true
        assertTrue(event.equals(new Event(DESCRIPTION, START, END)));

        // same object -> returns true
        assertTrue(event.equals(event));

        // null -> returns false
        assertFalse(event.equals(null));

        // different type -> returns false
        assertFalse(event.equals(5));

        // different description -> returns false
        assertFalse(event.equals(new Event("Other", START, END)));

        // different start -> returns false
        assertFalse(event.equals(new Event(DESCRIPTION, "21-02-26 1000", END)));

        // different end -> returns false
        assertFalse(event.equals(new Event(DESCRIPTION, START, "21-02-26 1600")));
    }

    @Test
    public void toStringMethod() {
        Event event = new Event(DESCRIPTION, START, END);
        String expected = String.format("%s from %s to %s.", DESCRIPTION, START, END);
        assertEquals(expected, event.toString());
    }
}
