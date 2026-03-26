package seedu.address.model.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class TimeRangeTest {

    @Test
    public void constructor_validTimeRange_success() {
        TimeRange range = new TimeRange("2026-03-25 0900", "2026-03-25 1000");
        assertEquals("2026-03-25 0900", range.getStartTimeFormatted());
        assertEquals("2026-03-25 1000", range.getEndTimeFormatted());
        assertEquals("2026-03-25T09:00", range.getStartTime().toString());
        assertEquals("2026-03-25T10:00", range.getEndTime().toString());
    }

    @Test
    public void constructor_invalidFormat_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new TimeRange("2026/03/25 0900", "2026-03-25 1000"));
    }

    @Test
    public void constructor_nonExistentDate_throwsIllegalArgumentException() {
        // Feb 30 does not exist — should be rejected, not coerced to Feb 28
        assertThrows(IllegalArgumentException.class, () ->
                new TimeRange("2026-02-30 0900", "2026-02-30 1000"));
    }

    @Test
    public void constructor_endBeforeStart_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new TimeRange("2026-03-25 1000", "2026-03-25 0900"));
    }

    @Test
    public void equals_sameRange_returnsTrue() {
        TimeRange a = new TimeRange("2026-03-25 0900", "2026-03-25 1000");
        TimeRange b = new TimeRange("2026-03-25 0900", "2026-03-25 1000");
        assertTrue(a.equals(b));
    }

    @Test
    public void equals_differentRange_returnsFalse() {
        TimeRange a = new TimeRange("2026-03-25 0900", "2026-03-25 1000");
        TimeRange b = new TimeRange("2026-03-26 0900", "2026-03-26 1000");
        assertFalse(a.equals(b));
    }

    @Test
    public void equals_otherType_returnsFalse() {
        TimeRange a = new TimeRange("2026-03-25 0900", "2026-03-25 1000");
        assertFalse(a.equals(5));
    }

    @Test
    public void hashCode_sameRange_sameHash() {
        TimeRange a = new TimeRange("2026-03-25 0900", "2026-03-25 1000");
        TimeRange b = new TimeRange("2026-03-25 0900", "2026-03-25 1000");
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void isOverlapping_overlappingRanges_returnsTrue() {
        TimeRange a = new TimeRange("2026-03-25 0900", "2026-03-25 1100");
        TimeRange b = new TimeRange("2026-03-25 1000", "2026-03-25 1200");
        assertTrue(a.isOverlapping(b));
        assertTrue(b.isOverlapping(a));
    }

    @Test
    public void isOverlapping_nonOverlappingRanges_returnsFalse() {
        TimeRange a = new TimeRange("2026-03-25 0900", "2026-03-25 1000");
        TimeRange b = new TimeRange("2026-03-25 1000", "2026-03-25 1100");
        assertFalse(a.isOverlapping(b));
    }

    @Test
    public void isValidDateTimeFormat_validFormat_returnsTrue() {
        assertTrue(TimeRange.isValidDateTimeFormat("2026-03-25 0900"));
        assertTrue(TimeRange.isValidDateTimeFormat("2026-12-01 2359"));
    }

    @Test
    public void isValidDateTimeFormat_invalidFormat_returnsFalse() {
        assertFalse(TimeRange.isValidDateTimeFormat("25-03-2026 0900"));
        assertFalse(TimeRange.isValidDateTimeFormat("2026/03/25 0900"));
        assertFalse(TimeRange.isValidDateTimeFormat("not-a-date"));
        assertFalse(TimeRange.isValidDateTimeFormat(""));
        assertFalse(TimeRange.isValidDateTimeFormat("2026-02-30 0900")); // non-existent date
    }

    @Test
    public void toString_formatsRange() {
        TimeRange range = new TimeRange("2026-03-25 0900", "2026-03-25 1000");
        assertEquals("2026-03-25 0900 to 2026-03-25 1000", range.toString());
    }
}
