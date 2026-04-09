package seedu.address.model;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.event.Event;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonInformation;
import seedu.address.model.person.Photo;

/**
 * The API of the Model component.
 */
public interface Model {
    /** {@code Predicate} that always evaluate to true */
    Predicate<Person> PREDICATE_SHOW_ALL_PERSONS = unused -> true;
    Predicate<Event> PREDICATE_SHOW_ALL_EVENTS = unused -> true;

    /**
     * Replaces user prefs data with the data in {@code userPrefs}.
     */
    void setUserPrefs(ReadOnlyUserPrefs userPrefs);

    /**
     * Returns the user prefs.
     */
    ReadOnlyUserPrefs getUserPrefs();

    /**
     * Returns the user prefs' GUI settings.
     */
    GuiSettings getGuiSettings();

    /**
     * Sets the user prefs' GUI settings.
     */
    void setGuiSettings(GuiSettings guiSettings);

    /**
     * Returns the user prefs' address book file path.
     */
    Path getAddressBookFilePath();

    /**
     * Sets the user prefs' address book file path.
     */
    void setAddressBookFilePath(Path addressBookFilePath);

    /**
     * Replaces address book data with the data in {@code addressBook}.
     */
    void setAddressBook(ReadOnlyAddressBook addressBook);

    /** Returns the AddressBook */
    ReadOnlyAddressBook getAddressBook();

    /**
     * Returns true if a person with the same identity as {@code person} exists in the address book.
     */
    boolean hasPerson(Person person);

    /**
     * Deletes the given person.
     * The person must exist in the address book.
     */
    void deletePerson(Person target);

    /**
     * Adds the given person.
     * {@code person} must not already exist in the address book.
     */
    void addPerson(Person person);

    /**
     * Replaces the given person {@code target} with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the address book.
     */
    void setPerson(Person target, Person editedPerson);

    /** Returns an unmodifiable view of the filtered person list */
    ObservableList<Person> getFilteredPersonList();

    /**
     * Updates the filter of the filtered person list to filter by the given {@code predicate}.
     * @throws NullPointerException if {@code predicate} is null.
     */
    void updateFilteredPersonList(Predicate<Person> predicate);

    /**
     * Resets the filtered person list to show all persons.
     */
    void showAllPersons();

    /**
     * Resets the filtered person list to show all persons with pinned persons first.
     */
    void showAllPersonsPinnedFirst();

    /**
     * Filters the person list to persons matching {@code predicate}.
     * Does not modify the event list.
     */
    void showPersons(Predicate<Person> predicate);

    /**
     * Filters the person list to show only {@code persons} and clears the event list.
     */
    void showMatchingPersons(Set<Person> persons);

    /**
     * Filters the person list to show only {@code person}.
     * Does not modify the event list.
     */
    void showPerson(Person person);

    /**
     * Pins the given person for this application session.
     */
    void pinPerson(Person person);

    /**
     * Unpins the given person for this application session.
     */
    void unpinPerson(Person person);

    /**
     * Returns true if the given person is currently pinned.
     */
    boolean isPersonPinned(Person person);

    /**
     * Return a list of correct contact(s) based on the optional parameters provided
     */
    List<Person> findPersons(PersonInformation info);

    /**
     * Returns a list of persons whose name contains any of the keywords in {@code info.getName()}
     * (OR, case-insensitive).
     * Optional fields are applied as additional AND filters when present.
     */
    List<Person> searchPersons(PersonInformation info);

    /**
     * Returns true if an event with the same identity as {@code event} exists.
     */
    boolean hasEvent(Event event);

    /**
     * Returns true if any event in the address book overlaps with {@code event}.
     */
    boolean hasOverlappingEvent(Event event);

    /**
     * Returns a list of events that overlaps with {@code event}.
     */
    List<Event> getOverlappingEvent(Event event);

    /**
     * Adds the given event.
     * The event must not already exist in the address book.
     */
    void addEvent(Event event);

    /**
     * Deletes the given event.
     * The event must exist in the address book.
     */
    void deleteEvent(Event target);

    /**
     * Replaces the given event {@code target} with {@code editedEvent}.
     * {@code target} must exist and {@code editedEvent} must not duplicate another event.
     */
    void setEvent(Event target, Event editedEvent);

    /** Returns an unmodifiable view of the filtered event list */
    ObservableList<Event> getFilteredEventList();

    /**
     * Filters the filtered event list to show no events.
     */
    void showNoEvents();

    Event linkPersonToEvent(Event eventToAdd);

    Event unlinkPersonFromEvent(Event eventToUnlink);

    /**
     * Updates the filtered event list to show only events linked to {@code person},
     * and the filtered person list to show only {@code person}.
     */
    void showEventsForPerson(Person person);

    /**
     * Returns true if the given photo is used by any person in the address book,
     * excluding the specified person.
     */
    boolean isPhotoShared(Photo photo, Person personToExclude);
}
