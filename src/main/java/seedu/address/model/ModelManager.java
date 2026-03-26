package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.event.Event;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonInformation;

/**
 * Represents the in-memory model of the address book data.
 */
public class ModelManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final AddressBook addressBook;
    private final UserPrefs userPrefs;
    private final FilteredList<Person> filteredPersons;
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
        filteredEvents = new FilteredList<>(this.addressBook.getEventList());
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
        return filteredPersons;
    }

    @Override
    public void updateFilteredPersonList(Predicate<Person> predicate) {
        requireNonNull(predicate);
        filteredPersons.setPredicate(predicate);
    }

    @Override
    public void showAllPersons() {
        updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
    }

    @Override
    public void showPersons(Predicate<Person> predicate) {
        requireNonNull(predicate);
        updateFilteredPersonList(predicate);
    }

    @Override
    public void showMatchingPersons(java.util.Set<Person> persons) {
        requireNonNull(persons);
        updateFilteredPersonList(persons::contains);
        updateFilteredEventList(event -> false);
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

    @Override
    public void updateFilteredEventList(Predicate<Event> predicate) {
        requireNonNull(predicate);
        filteredEvents.setPredicate(predicate);
    }

    @Override
    public void showEventsForPerson(Person person) {
        requireNonNull(person);
        updateFilteredPersonList(p -> p.equals(person));
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

    private static boolean matchesInformation(Person p, PersonInformation info) {
        if (!p.getName().equalsIgnoreCase(info.name)) {
            return false;
        }
        // checking phone number:
        if (!info.phone.map(ph -> ph.equals(p.getPhone())).orElse(true)) {
            return false;
        }
        if (!info.email.map(em -> p.getEmail().map(e -> em.equals(e)).orElse(false))
                .orElse(true)) {
            return false;
        }
        if (!info.address.map(ad -> p.getAddress().map(a -> ad.equals(a)).orElse(false))
                .orElse(true)) {
            return false;
        }
        if (!info.tags.isEmpty() && !p.getTags().containsAll(info.tags)) {
            return false;
        }
        return true;
    }

}

