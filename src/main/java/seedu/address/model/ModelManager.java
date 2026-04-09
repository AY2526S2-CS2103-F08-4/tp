package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.event.Event;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonInformation;
import seedu.address.model.person.Photo;

/**
 * Represents the in-memory model of the address book data.
 */
public class ModelManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final AddressBook addressBook;
    private final UserPrefs userPrefs;
    private final FilteredList<Person> filteredPersons;
    private final SortedList<Person> sortedPersons;
    private final FilteredList<Event> filteredEvents;

    /**
     * Initializes a ModelManager with the given addressBook and userPrefs.
     */
    public ModelManager(ReadOnlyAddressBook addressBook, ReadOnlyUserPrefs userPrefs) {
        requireAllNonNull(addressBook, userPrefs);

        logger.fine("Initializing with address book: " + addressBook + " and user prefs " + userPrefs);

        this.addressBook = new AddressBook(addressBook);
        this.userPrefs = new UserPrefs(userPrefs);
        filteredPersons = new FilteredList<>(this.addressBook.getPersonList());
        sortedPersons = new SortedList<>(filteredPersons);
        sortedPersons.setComparator(createPinnedComparator());
        filteredEvents = new FilteredList<>(this.addressBook.getEventList());

        showNoEvents();
    }

    public ModelManager() {
        this(new AddressBook(), new UserPrefs());
    }

    //=========== UserPrefs ==================================================================================

    @Override
    public void setUserPrefs(ReadOnlyUserPrefs userPrefs) {
        requireNonNull(userPrefs);
        this.userPrefs.resetData(userPrefs);
    }

    @Override
    public ReadOnlyUserPrefs getUserPrefs() {
        return userPrefs;
    }

    @Override
    public GuiSettings getGuiSettings() {
        return userPrefs.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        requireNonNull(guiSettings);
        userPrefs.setGuiSettings(guiSettings);
    }

    @Override
    public Path getAddressBookFilePath() {
        return userPrefs.getAddressBookFilePath();
    }

    @Override
    public void setAddressBookFilePath(Path addressBookFilePath) {
        requireNonNull(addressBookFilePath);
        userPrefs.setAddressBookFilePath(addressBookFilePath);
    }

    //=========== AddressBook ================================================================================

    @Override
    public void setAddressBook(ReadOnlyAddressBook addressBook) {
        this.addressBook.resetData(addressBook);
    }

    @Override
    public ReadOnlyAddressBook getAddressBook() {
        return addressBook;
    }

    @Override
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return addressBook.hasPerson(person);
    }

    @Override
    public void deletePerson(Person target) {
        addressBook.removePerson(target);
    }

    @Override
    public void addPerson(Person person) {
        addressBook.addPerson(person);
    }

    @Override
    public void setPerson(Person target, Person editedPerson) {
        requireAllNonNull(target, editedPerson);
        addressBook.setPerson(target, editedPerson);
    }

    @Override
    public boolean hasEvent(Event event) {
        requireNonNull(event);
        return addressBook.hasEvent(event);
    }

    @Override
    public boolean hasOverlappingEvent(Event event) {
        requireNonNull(event);
        return addressBook.hasOverlappingEvent(event);
    }

    @Override
    public List<Event> getOverlappingEvent(Event event) {
        requireNonNull(event);
        return addressBook.getOverlappingEvent(event);
    }

    @Override
    public void addEvent(Event event) {
        addressBook.addEvent(event);
    }

    @Override
    public void deleteEvent(Event target) {
        addressBook.removeEvent(target);
    }

    @Override
    public void setEvent(Event target, Event editedEvent) {
        requireAllNonNull(target, editedEvent);
        addressBook.setEvent(target, editedEvent);
    }

    @Override
    public Event linkPersonToEvent(Event toLink) {
        requireAllNonNull(toLink);
        return addressBook.linkPersonToEvent(toLink);
    }

    @Override
    public Event unlinkPersonFromEvent(Event eventToUnlink) {
        requireAllNonNull(eventToUnlink);
        return addressBook.unlinkPersonFromEvent(eventToUnlink);
    }

    //=========== Filtered Person List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Person} backed by the internal list of
     * {@code versionedAddressBook}
     */
    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return sortedPersons;
    }

    @Override
    public void updateFilteredPersonList(Predicate<Person> predicate) {
        requireNonNull(predicate);
        filteredPersons.setPredicate(predicate);
    }

    @Override
    public void showAllPersons() {
        updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        sortedPersons.setComparator(null);
    }

    @Override
    public void showAllPersonsPinnedFirst() {
        updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        sortedPersons.setComparator(createPinnedComparator());
    }

    @Override
    public void showPersons(Predicate<Person> predicate) {
        requireNonNull(predicate);
        updateFilteredPersonList(predicate);
        sortedPersons.setComparator(createPinnedComparator());
    }

    @Override
    public void showMatchingPersons(Set<Person> persons) {
        requireNonNull(persons);
        updateFilteredPersonList(persons::contains);
        sortedPersons.setComparator(null);
        updateFilteredEventList(event -> false);
    }

    @Override
    public void showPerson(Person person) {
        requireNonNull(person);
        updateFilteredPersonList(p -> p.equals(person));
        sortedPersons.setComparator(null);
    }

    @Override
    public void pinPerson(Person person) {
        requireNonNull(person);
        addressBook.pinPerson(person);
    }

    @Override
    public boolean isPersonPinned(Person person) {
        requireNonNull(person);
        return addressBook.isPersonPinned(person);
    }

    @Override
    public void unpinPerson(Person person) {
        requireNonNull(person);
        addressBook.unpinPerson(person);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof ModelManager otherModelManager) {
            return addressBook.equals(otherModelManager.addressBook)
                    && userPrefs.equals(otherModelManager.userPrefs)
                    && filteredPersons.equals(otherModelManager.filteredPersons)
                    && filteredEvents.equals(otherModelManager.filteredEvents);
        }
        return false;
    }

    //=========== Filtered Event List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Event} from all events.
     */
    @Override
    public ObservableList<Event> getFilteredEventList() {
        return filteredEvents;
    }

    private void updateFilteredEventList(Predicate<Event> predicate) {
        requireNonNull(predicate);
        filteredEvents.setPredicate(predicate);
    }

    @Override
    public void showNoEvents() {
        updateFilteredEventList(e -> false);
    }

    @Override
    public void showEventsForPerson(Person person) {
        requireNonNull(person);
        updateFilteredPersonList(p -> p.isSamePerson(person));
        sortedPersons.setComparator(null);
        updateFilteredEventList(person::hasEvent);
    }

    /**
     * Returns a list of persons matching the provided {@link PersonInformation}.
     * Name is required and must match (case-insensitive). Optional fields are applied as additional
     * filters when present. Tags match if any provided tag is present on the person.
     *
     * @param info search criteria with required name and optional fields
     * @return list of persons matching the criteria
     */
    public List<Person> findPersons(PersonInformation info) {
        return addressBook
                .getPersonList()
                .stream()
                .filter(person -> matchesInformation(person, info))
                .toList();
    }

    @Override
    public List<Person> searchPersons(PersonInformation info) {
        String[] keywords = info.getName().fullName.trim().split("\\s+");
        return addressBook
                .getPersonList()
                .stream()
                .filter(person -> matchesKeywords(person, keywords) && matchesOptionalFields(person, info))
                .toList();
    }

    private static boolean matchesKeywords(Person p, String[] keywords) {
        List<String> personWords = List.of(p.getName().fullName.toLowerCase().split("\\s+"));
        for (String keyword : keywords) {
            if (personWords.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesOptionalFields(Person p, PersonInformation info) {
        boolean isPhoneMatching = info.getPhone().map(ph -> ph.equals(p.getPhone())).orElse(true);
        boolean isEmailMatching = info.getEmail().map(em -> p.getEmail().map(em::equals).orElse(false)).orElse(true);
        boolean isAddressMatching = info.getAddress().map(ad -> p.getAddress().map(ad::equals).orElse(false))
                .orElse(true);
        boolean isTagsMatching = info.getTags().isEmpty() || p.getTags().containsAll(info.getTags());
        return isPhoneMatching && isEmailMatching && isAddressMatching && isTagsMatching;
    }

    private static boolean matchesInformation(Person p, PersonInformation info) {
        boolean isNameMatching = p.getName().equalsIgnoreCase(info.getName());
        boolean isPhoneMatching = info.getPhone().map(ph -> ph.equals(p.getPhone())).orElse(true);
        boolean isEmailMatching = info.getEmail().map(em -> p.getEmail().map(em::equals).orElse(false)).orElse(true);
        boolean isAddressMatching = info.getAddress().map(ad -> p.getAddress().map(ad::equals).orElse(false))
                .orElse(true);
        boolean isTagsMatching = info.getTags().isEmpty() || p.getTags().containsAll(info.getTags());
        return isNameMatching && isPhoneMatching && isEmailMatching && isAddressMatching && isTagsMatching;
    }

    /**
     * Creates a comparator that orders pinned persons first.
     * Pinned persons are ordered by pin sequence (list index), while unpinned persons
     * retain their relative order from the address book list.
     */
    private Comparator<Person> createPinnedComparator() {
        return (p1, p2) -> {
            boolean p1Pinned = findPinnedPersonByIdentity(p1) != null;
            boolean p2Pinned = findPinnedPersonByIdentity(p2) != null;

            if (p1Pinned && !p2Pinned) {
                return -1;
            }
            if (!p1Pinned && p2Pinned) {
                return 1;
            }
            if (p1Pinned) {
                return Integer.compare(getPinIndexByIdentity(p1), getPinIndexByIdentity(p2));
            }

            List<Person> personList = addressBook.getPersonList();
            return Integer.compare(personList.indexOf(p1), personList.indexOf(p2));
        };
    }

    /**
     * Returns the pinned person with the same identity as {@code person}, if present.
     */
    private Person findPinnedPersonByIdentity(Person person) {
        return addressBook.getPinnedPersonList().stream()
                .filter(pinnedPerson -> pinnedPerson.isSamePerson(person))
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns the pin index for the person with the same identity as {@code person}.
     * Returns {@code Integer.MAX_VALUE} when the person is not pinned.
     */
    private int getPinIndexByIdentity(Person person) {
        Person pinnedPerson = findPinnedPersonByIdentity(person);
        if (pinnedPerson == null) {
            return Integer.MAX_VALUE;
        }
        return addressBook.getPinnedPersonList().indexOf(pinnedPerson);
    }

    @Override
    public boolean isPhotoShared(Photo photo, Person personToExclude) {
        requireNonNull(photo);
        requireNonNull(personToExclude);

        return addressBook.getPersonList().stream()
                .filter(p -> !p.isSamePerson(personToExclude))
                .anyMatch(p -> p.getPhoto().equals(Optional.of(photo)));
    }

}
