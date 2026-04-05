package seedu.address.model.util;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.event.Description;
import seedu.address.model.event.Event;
import seedu.address.model.event.TimeRange;
import seedu.address.model.event.Title;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;

/**
 * Contains utility methods for populating {@code AddressBook} with sample data.
 */
public class SampleDataUtil {
    public static Person[] getSamplePersons() {
        return new Person[] {
            createPerson("Alex Yeoh", "87438807", "alexyeoh@example.com",
                    "Blk 30 Geylang Street 29, #06-40", "friends"),
            createPerson("Bernice Yu", "99272758", "berniceyu@example.com",
                    "Blk 30 Lorong 3 Serangoon Gardens, #07-18", "colleagues", "friends"),
            createPerson("Charlotte Oliveiro", "93210283", "charlotte@example.com",
                    "Blk 11 Ang Mo Kio Street 74, #11-04", "neighbours"),
            createPerson("David Li", "91031282", "lidavid@example.com",
                    "Blk 436 Serangoon Gardens Street 26, #16-43", "family"),
            createPerson("Irfan Ibrahim", "92492021", "irfan@example.com",
                    "Blk 47 Tampines Street 20, #17-35", "classmates"),
            createPerson("Roy Balakrishnan", "92624417", "royb@example.com",
                    "Blk 45 Aljunied Street 85, #11-31", "colleagues")
        };
    }

    public static ReadOnlyAddressBook getSampleAddressBook() {
        AddressBook sampleAb = new AddressBook();
        Person[] samplePersons = getSamplePersons();
        Event[] sampleEvents = getSampleEvents();

        linkSampleEvents(samplePersons, sampleEvents);
        addSampleEvents(sampleAb, sampleEvents);
        addSamplePersons(sampleAb, samplePersons);
        return sampleAb;
    }

    private static Person createPerson(String name, String phone, String email, String address, String... tags) {
        return new Person(new Name(name), new Phone(phone),
                Optional.of(new Email(email)),
                Optional.of(new Address(address)),
                getTagSet(tags),
                Optional.empty());
    }

    private static Event createEvent(String title, String description, String start, String end, int linkedCount) {
        return new Event(new Title(title), Optional.of(new Description(description)),
                new TimeRange(start, end), linkedCount);
    }

    private static void linkEvent(Person person, Event event) {
        person.addEvent(event);
    }

    private static Event[] getSampleEvents() {
        return new Event[] {
            createEvent("CS2103 Meeting", "TP UML design discussion",
                    "2026-08-19 1400", "2026-08-19 1530", 3),
            createEvent("CS2103 Meeting", "Discuss team project feature workload distributions",
                    "2026-08-26 1600", "2026-08-26 1730", 3),
            createEvent("Volleyball Practice", "Practice drills",
                    "2026-08-21 1800", "2026-08-21 1930", 2),
            createEvent("Internship Interview Practice", "Dry run for interview",
                    "2026-09-02 1900", "2026-09-02 2000", 2),
            createEvent("Study Group", "Revise for midterms and quiz",
                    "2026-09-04 1500", "2026-09-04 1630", 3),
            createEvent("CCA Planning", "Coordinate school club showcase",
                    "2026-09-09 1700", "2026-09-09 1800", 2)
        };
    }

    private static void linkSampleEvents(Person[] samplePersons, Event[] sampleEvents) {
        Person alex = samplePersons[0];
        Person bernice = samplePersons[1];
        Person charlotte = samplePersons[2];
        Person david = samplePersons[3];
        Person irfan = samplePersons[4];
        Person roy = samplePersons[5];

        Event cs2103Design = sampleEvents[0];
        Event cs2103Workload = sampleEvents[1];
        Event volleyball = sampleEvents[2];
        Event interviewPrep = sampleEvents[3];
        Event studyGroup = sampleEvents[4];
        Event clubPlanning = sampleEvents[5];

        linkEvent(alex, cs2103Design);
        linkEvent(bernice, cs2103Design);
        linkEvent(david, cs2103Design);

        linkEvent(alex, cs2103Workload);
        linkEvent(bernice, cs2103Workload);
        linkEvent(david, cs2103Workload);

        linkEvent(charlotte, volleyball);
        linkEvent(irfan, volleyball);

        linkEvent(alex, interviewPrep);
        linkEvent(roy, interviewPrep);

        linkEvent(bernice, studyGroup);
        linkEvent(charlotte, studyGroup);
        linkEvent(irfan, studyGroup);

        linkEvent(david, clubPlanning);
        linkEvent(roy, clubPlanning);
    }

    private static void addSampleEvents(AddressBook addressBook, Event[] sampleEvents) {
        for (Event sampleEvent : sampleEvents) {
            addressBook.addEvent(sampleEvent);
        }
    }

    private static void addSamplePersons(AddressBook addressBook, Person[] samplePersons) {
        for (Person samplePerson : samplePersons) {
            addressBook.addPerson(samplePerson);
        }
    }

    /**
     * Returns a tag set containing the list of strings given.
     */
    public static Set<Tag> getTagSet(String... strings) {
        return Arrays.stream(strings)
                .map(Tag::new)
                .collect(Collectors.toSet());
    }

}
