package seedu.address.model.person;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.event.Event;
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
    private final List<Event> events;

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
        this.events = new ArrayList<>();
        this.photo = photo;
    }

    /**
     * Overloaded constructor: Name and phone are compulsory. Email and address are optional.
     */
    public Person(Name name, Phone phone, Optional<Email> email, Optional<Address> address,
        Set<Tag> tags, List<Event> events, Optional<Photo> photo) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.tags.addAll(tags);
        this.events = events;
        this.photo = photo;
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

    public Optional<Photo> getPhoto() {
        return photo;
    }

    public String getNameString() {
        return name.fullName;
    }

    public String getPhoneString() {
        return phone.value;
    }

    public Optional<String> getEmailString() {
        return this.email.map(e -> e.value);
    }

    public Optional<String> getAddressString() {
        return this.address.map(a -> a.value);
    }

    /**
     * Returns an immutable tag set, which throws {@code UnsupportedOperationException}
     * if modification is attempted.
     */
    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    /**
     * Returns a String representation of tags tied to the Person
     */
    public String getTagsString() {
        if (tags.isEmpty()) {
            return "";
        }

        return "Tags: " + tags.stream()
                .map(tag -> tag.tagName)
                .sorted()
                .collect(Collectors.joining(", "));
    }

    /**
     * Returns an immutable Event List
     */
    public List<Event> getEvents() {
        return this.events;
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
     * Returns true if this person is linked to an event that is the same as {@code event}.
     */
    public boolean hasEvent(Event event) {
        return events.stream().anyMatch(e -> e.isSameEvent(event));
    }

    /**
     * Removes the event that is the same as {@code event} from this person's event list.
     */
    public void removeEvent(Event event) {
        events.removeIf(e -> e.isSameEvent(event));
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
        if (other instanceof Person otherPerson) {
            return name.equals(otherPerson.name)
                    && phone.equals(otherPerson.phone)
                    && email.equals(otherPerson.email)
                    && address.equals(otherPerson.address)
                    && tags.equals(otherPerson.tags)
                    && events.equals(otherPerson.events)
                    && photo.equals(otherPerson.photo);
        }
        return false;
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, phone, email, address, tags, photo);
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
                .add("photo", photo.isPresent() ? photo.get().getPath() : "")
                .toString();
    }

}
