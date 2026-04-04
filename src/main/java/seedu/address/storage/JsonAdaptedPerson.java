package seedu.address.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.event.Event;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Photo;
import seedu.address.model.tag.Tag;

/**
 * Jackson-friendly version of {@link Person}.
 */
class JsonAdaptedPerson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Person's %s field is missing!";
    public static final String MISSING_EVENT_MESSAGE_FORMAT = "Event ID %d referenced by person '%s' not found.";

    private final String name;
    private final String phone;
    private final String email;
    private final String address;
    private final String photo;
    private final List<JsonAdaptedTag> tags = new ArrayList<>();
    private final List<Integer> eventIds = new ArrayList<>();

    /**
     * Constructs a {@code JsonAdaptedPerson} with the given person details.
     */
    @JsonCreator
    public JsonAdaptedPerson(@JsonProperty("name") String name, @JsonProperty("phone") String phone,
            @JsonProperty("email") String email,
            @JsonProperty("address") String address,
            @JsonProperty("photo") String photo,
            @JsonProperty("tags") List<JsonAdaptedTag> tags,
            @JsonProperty("eventIds") List<Integer> eventIds) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.photo = photo;
        if (tags != null) {
            this.tags.addAll(tags);
        }
        if (eventIds != null) {
            this.eventIds.addAll(eventIds);
        }
    }

    /**
     * Converts a given {@code Person} into this class for Jackson use.
     */
    public JsonAdaptedPerson(Person source) {
        name = source.getName().fullName;
        phone = source.getPhone().value;
        email = source.getEmail().map(email -> email.value).orElse(null);
        address = source.getAddress().map(address -> address.value).orElse(null);
        photo = source.getPhoto().map(photo -> photo.getPath()).orElse(null);
        tags.addAll(source.getTags().stream()
                .map(JsonAdaptedTag::new)
                .collect(Collectors.toList()));
        eventIds.addAll(source.getEvents().stream()
                .map(Event::getEventId)
                .collect(Collectors.toList()));
    }

    /**
     * Converts this Jackson-friendly adapted person object into the model's {@code Person} object.
     * Events are resolved from {@code eventMap} using their IDs as foreign keys.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted person.
     */
    public Person toModelType(Map<Integer, Event> eventMap) throws IllegalValueException {
        final List<Tag> personTags = new ArrayList<>();
        for (JsonAdaptedTag tag : tags) {
            personTags.add(tag.toModelType());
        }

        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName()));
        }
        if (!Name.isValidName(name)) {
            throw new IllegalValueException(Name.MESSAGE_CONSTRAINTS);
        }
        final Name modelName = new Name(name);

        if (phone == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName()));
        }
        if (!Phone.isValidPhone(phone)) {
            throw new IllegalValueException(Phone.MESSAGE_CONSTRAINTS);
        }
        final Phone modelPhone = new Phone(phone);

        final Optional<Email> modelEmail;
        if (email == null) { // Optional field
            modelEmail = Optional.empty();
        } else if (!Email.isValidEmail(email)) {
            throw new IllegalValueException(Email.MESSAGE_CONSTRAINTS);
        } else {
            modelEmail = Optional.of(new Email(email));
        }

        final Optional<Address> modelAddress;
        if (address == null) {
            modelAddress = Optional.empty();
        } else if (!Address.isValidAddress(address)) {
            throw new IllegalValueException(Address.MESSAGE_CONSTRAINTS);
        } else {
            modelAddress = Optional.of(new Address(address));
        }

        final Set<Tag> modelTags = new HashSet<>(personTags);

        final Optional<Photo> modelPhoto;
        if (photo == null) {
            modelPhoto = Optional.empty();
        } else if (!Photo.isValidPhoto(photo)) {
            // Scenario: Invalid format,
            modelPhoto = Optional.of(new Photo("data/images/corrupted_data.jpg"));
        } else {
            modelPhoto = Optional.of(new Photo(photo));
        }

        Person person = new Person(modelName, modelPhone, modelEmail, modelAddress, modelTags, modelPhoto);
        for (int eventId : eventIds) {
            Event event = eventMap.get(eventId);
            if (event == null) {
                throw new IllegalValueException(String.format(MISSING_EVENT_MESSAGE_FORMAT, eventId, name));
            }
            person.addEvent(event);
        }

        return person;
    }

}
