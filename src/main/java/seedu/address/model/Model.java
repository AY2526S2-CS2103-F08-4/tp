package seedu.address.model;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.event.Event;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonInformation;

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
     * Filters the person list to persons matching {@code predicate}.
     * Does not modify the event list.
     */
    void showPersons(Predicate<Person> predicate);

    /**
     * Filters the person list to show only {@code persons} and clears the event list.
     */
    void showMatchingPersons(java.util.Set<Person> persons);

    /**
     * Return a list of correct contact(s) based on the optional parameters provided
     */
    List<Person> findPersons(PersonInformation info);

    /**
     * Returns true if an event with the same identity as {@code event} exists.
     */
    boolean hasEvent(Event event);

    /**
     * Returns true if any event in the address book overlaps with {@code event}.
     */
    boolean hasOverlappingEvent(Event event);

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
     * Updates the filter of the filtered event list to filter by the given {@code predicate}.
     * @throws NullPointerException if {@code predicate} is null.
     */
    void updateFilteredEventList(Predicate<Event> predicate);

    Event linkPersonToEvent(Event eventToAdd);

    Event unlinkPersonFromEvent(Event eventToUnlink);

    /**
     * Updates the filtered event list to show only events linked to {@code person},
     * and the filtered person list to show only {@code person}.
     */
    void showEventsForPerson(Person person);

}

