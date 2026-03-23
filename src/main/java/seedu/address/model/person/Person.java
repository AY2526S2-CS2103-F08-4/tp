package seedu.address.model.person;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javafx.collections.ObservableList;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.tag.Tag;



/**
 * Represents a Person in the address book.
 * Guarantees: name, phone and tags are present and not null, field values are validated, immutable.
 */
public class Person {

    // Identity fields
    private final Name name;
    private final Phone phone;

    // Optional Data fields
    private final Optional<Email> email;
    private final Optional<Address> address;
    private final Optional<Photo> photo;
    private final Set<Tag> tags = new HashSet<>();


    // Event fields
    private final UniqueEventList events;

    /**
     * Name and phone are compulsory. Email and address are optional.
     */
    public Person(Name name, Phone phone, Optional<Email> email, Optional<Address> address, Set<Tag> tags,
                  Optional<Photo> photo) {
        requireAllNonNull(name, phone, email, address, tags, photo);
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.tags.addAll(tags);
        this.events = new UniqueEventList();
        this.photo = Optional.of(photo.orElse(new Photo("")));
    }

    /**
     * Overloaded constructor: Name and phone are compulsory. Email and address are optional.
     * UniqueEventList is compulsory
     */
    public Person(Name name, Phone phone, Optional<Email> email, Optional<Address> address,
        Set<Tag> tags, UniqueEventList events, Optional<Photo> photo) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.tags.addAll(tags);
        this.events = events;
        this.photo = Optional.of(photo.orElse(new Photo("")));
    }

    public Name getName() {
        return name;
    }

    public Phone getPhone() {
        return phone;
    }

    public Optional<Email> getEmail() {
        return email;
    }

    public Optional<Address> getAddress() {
        return address;
    }

    public Optional<Photo> getPhoto() {return photo;}

    /**
     * Returns an immutable tag set, which throws {@code UnsupportedOperationException}
     * if modification is attempted.
     */
    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    /**
     * Returns an immutable Event List
     */
    public ObservableList<Event> getEvents() {
        return this.events.asUnmodifiableObservableList();
    }

    /**
     * Adds an event to the person's list of events.
     * @param event
     * @return true if the event was added successfully, false otherwise.
     */
    public boolean addEvent(Event event) {
        events.add(event);
        return true;
    }


    /**
     * Returns true if both persons have the same name.
     * We define a contact/person to be uniquely identified by phone numbers.
     * This defines a weaker notion of equality between two persons.
     */
    public boolean isSamePerson(Person otherPerson) {
        if (otherPerson == this) {
            return true;
        }

        return otherPerson != null
                && otherPerson.getPhone().equals(getPhone());
    }

    /**
     * Returns true if both persons have the same identity and data fields.
     * This defines a stronger notion of equality between two persons.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Person)) {
            return false;
        }

        Person otherPerson = (Person) other;
        return name.equals(otherPerson.name)
                && phone.equals(otherPerson.phone)
                && email.equals(otherPerson.email)
                && address.equals(otherPerson.address)
                && tags.equals(otherPerson.tags)
                && events.equals(otherPerson.events);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, phone, email, address, tags);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("phone", phone)
                .add("email", email.map(Email::toString).orElse(""))
                .add("address", address.map(Address::toString).orElse(""))
                .add("tags", tags)
                .add("events", events)
                .toString();
    }

}
