package seedu.address.model.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.model.event.exceptions.ClashingEventException;
import seedu.address.model.event.exceptions.DuplicateEventException;
import seedu.address.model.event.exceptions.EventNotFoundException;

public class UniqueEventListTest {

    private static Event newEvent(String title, String start, String end) {
        return new Event(new Title(title), Optional.empty(), new TimeRange(start, end));
    }

    @Test
    public void add_contains_success() {
        UniqueEventList list = new UniqueEventList();
        Event event = newEvent("Meeting", "2026-03-25 0900", "2026-03-25 1000");
        list.add(event);
        assertTrue(list.contains(event));
    }

    @Test
    public void add_duplicate_throwsDuplicateEventException() {
        UniqueEventList list = new UniqueEventList();
        Event event = newEvent("Meeting", "2026-03-25 0900", "2026-03-25 1000");
        list.add(event);
        Event duplicate = newEvent("Meeting", "2026-03-25 0900", "2026-03-25 1000");
        assertThrows(DuplicateEventException.class, () -> list.add(duplicate));
    }

    @Test
    public void add_clashingEvent_throwsClashingEventException() {
        UniqueEventList list = new UniqueEventList();
        Event event = newEvent("Meeting", "2026-03-25 0900", "2026-03-25 1100");
        list.add(event);
        Event clashing = newEvent("Call", "2026-03-25 1000", "2026-03-25 1200");
        assertThrows(ClashingEventException.class, () -> list.add(clashing));
    }

    @Test
    public void hasOverlappingEvent_noOverlap_returnsFalse() {
        UniqueEventList list = new UniqueEventList();
        list.add(newEvent("Meeting", "2026-03-25 0900", "2026-03-25 1000"));
        Event nonOverlapping = newEvent("Lunch", "2026-03-25 1200", "2026-03-25 1300");
        assertFalse(list.hasOverlappingEvent(nonOverlapping));
    }

    @Test
    public void hasOverlappingEvent_withOverlap_returnsTrue() {
        UniqueEventList list = new UniqueEventList();
        list.add(newEvent("Meeting", "2026-03-25 0900", "2026-03-25 1100"));
        Event overlapping = newEvent("Call", "2026-03-25 1000", "2026-03-25 1200");
        assertTrue(list.hasOverlappingEvent(overlapping));
    }

    @Test
    public void setEvent_targetMissing_throwsEventNotFoundException() {
        UniqueEventList list = new UniqueEventList();
        Event target = newEvent("Meeting", "2026-03-25 0900", "2026-03-25 1000");
        Event edited = newEvent("Review", "2026-03-26 0900", "2026-03-26 1000");
        assertThrows(EventNotFoundException.class, () -> list.setEvent(target, edited));
    }

    @Test
    public void setEvent_replacesEvent_success() {
        UniqueEventList list = new UniqueEventList();
        Event target = newEvent("Meeting", "2026-03-25 0900", "2026-03-25 1000");
        Event edited = newEvent("Review", "2026-03-26 0900", "2026-03-26 1000");
        list.add(target);
        list.setEvent(target, edited);
        assertTrue(list.contains(edited));
    }

    @Test
    public void setEvent_duplicate_throwsDuplicateEventException() {
        UniqueEventList list = new UniqueEventList();
        Event target = newEvent("Meeting", "2026-03-25 0900", "2026-03-25 1000");
        Event edited = newEvent("Review", "2026-03-26 0900", "2026-03-26 1000");
        Event duplicate = newEvent("Review", "2026-03-26 0900", "2026-03-26 1000");
        list.add(target);
        list.add(edited);
        assertThrows(DuplicateEventException.class, () -> list.setEvent(target, duplicate));
    }

    @Test
    public void remove_missing_throwsEventNotFoundException() {
        UniqueEventList list = new UniqueEventList();
        Event event = newEvent("Meeting", "2026-03-25 0900", "2026-03-25 1000");
        assertThrows(EventNotFoundException.class, () -> list.remove(event));
    }

    @Test
    public void remove_existing_success() {
        UniqueEventList list = new UniqueEventList();
        Event event = newEvent("Meeting", "2026-03-25 0900", "2026-03-25 1000");
        list.add(event);
        list.remove(event);
        assertFalse(list.contains(event));
    }

    @Test
    public void setEvents_list_success() {
        UniqueEventList list = new UniqueEventList();
        Event e1 = newEvent("Meeting", "2026-03-25 0900", "2026-03-25 1000");
        Event e2 = newEvent("Review", "2026-03-26 0900", "2026-03-26 1000");
        list.setEvents(Arrays.asList(e1, e2));
        assertEquals(2, list.asUnmodifiableObservableList().size());
    }

    @Test
    public void setEvents_listWithDuplicates_throwsDuplicateEventException() {
        UniqueEventList list = new UniqueEventList();
        Event e1 = newEvent("Meeting", "2026-03-25 0900", "2026-03-25 1000");
        Event duplicate = newEvent("Meeting", "2026-03-25 0900", "2026-03-25 1000");
        assertThrows(DuplicateEventException.class, () -> list.setEvents(Arrays.asList(e1, duplicate)));
    }

    @Test
    public void setEvents_uniqueEventList_success() {
        UniqueEventList source = new UniqueEventList();
        Event e1 = newEvent("Meeting", "2026-03-25 0900", "2026-03-25 1000");
        source.add(e1);
        UniqueEventList target = new UniqueEventList();
        target.setEvents(source);
        assertTrue(target.contains(e1));
    }

    @Test
    public void getOverlappingEvents_noOverlap_returnsEmptyList() {
        UniqueEventList list = new UniqueEventList();
        list.add(newEvent("CS2106", "2026-03-25 0900", "2026-03-25 1000"));
        Event nonOverlapping = newEvent("CS2109S", "2026-03-25 1200", "2026-03-25 1300");
        assertTrue(list.getOverlappingEvent(nonOverlapping).isEmpty());
    }

    @Test
    public void getOverlappingEvents_withOverlaps_returnsOverlappingEvents() {
        UniqueEventList list = new UniqueEventList();
        Event e1 = newEvent("CS2106", "2026-03-25 0900", "2026-03-25 1000");
        list.add(e1);
        Event overlappingEvent = newEvent("CS2109S", "2026-03-25 0800", "2026-03-25 1300");

        List<Event> listOfOverlapEvents = list.getOverlappingEvent(overlappingEvent);
        assertEquals(1, listOfOverlapEvents.size());
        assertTrue(listOfOverlapEvents.contains(e1));
    }


    @Test
    public void equals_sameContent_returnsTrue() {
        UniqueEventList a = new UniqueEventList();
        UniqueEventList b = new UniqueEventList();
        Event e1 = newEvent("Meeting", "2026-03-25 0900", "2026-03-25 1000");
        a.add(e1);
        b.add(e1);
        assertTrue(a.equals(b));
    }

    @Test
    public void equals_otherType_returnsFalse() {
        UniqueEventList list = new UniqueEventList();
        assertFalse(list.equals(5));
    }

    @Test
    public void toString_nonEmpty() {
        UniqueEventList list = new UniqueEventList();
        list.add(newEvent("Meeting", "2026-03-25 0900", "2026-03-25 1000"));
        assertTrue(list.toString().contains("Meeting"));
    }

    @Test
    public void iterator_iteratesAllEvents() {
        UniqueEventList list = new UniqueEventList();
        Event e1 = newEvent("Meeting", "2026-03-25 0900", "2026-03-25 1000");
        Event e2 = newEvent("Review", "2026-03-26 0900", "2026-03-26 1000");
        list.add(e1);
        list.add(e2);

        int count = 0;
        for (Event ignored : list) {
            count += 1;
        }
        assertEquals(2, count);
    }

    @Test
    public void hashCode_invokesInternalListHashCode() {
        UniqueEventList list = new UniqueEventList();
        list.add(newEvent("Meeting", "2026-03-25 0900", "2026-03-25 1000"));
        assertTrue(list.hashCode() != 0);
    }
}
