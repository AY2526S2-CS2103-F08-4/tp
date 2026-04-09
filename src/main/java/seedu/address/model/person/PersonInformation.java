package seedu.address.model.person;

import java.util.Optional;
import java.util.Set;

import seedu.address.model.tag.Tag;

/**
 * Encapsulates parameters for identifying persons.
 * Required parameters: name
 * Optional parameters: phone, email, address, and tags
 */
public class PersonInformation {
    private final Name name;
    private final Optional<Phone> phone;
    private final Optional<Email> email;
    private final Optional<Address> address;
    private final Set<Tag> tags;

    /**
     * Creates a {@code PersonInformation} with the given fields.
     *
     * @param name required name
     * @param phone optional phone
     * @param email optional email
     * @param address optional address
     * @param tags optional tags
     */
    public PersonInformation(Name name, Phone phone,
                             Email email, Address address,
                             Set<Tag> tags) {
        if (name == null) {
            throw new IllegalArgumentException("Name is required");
        }
        this.name = name;
        this.phone = Optional.ofNullable(phone);
        this.email = Optional.ofNullable(email);
        this.address = Optional.ofNullable(address);
        this.tags = (tags == null) ? Set.of() : Set.copyOf(tags);
    }

    public Name getName() {
        return name;
    }

    public Optional<Phone> getPhone() {
        return phone;
    }

    public Optional<Email> getEmail() {
        return email;
    }

    public Optional<Address> getAddress() {
        return address;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PersonInformation otherInfo) {
            return name.equals(otherInfo.getName())
                    && phone.equals(otherInfo.getPhone())
                    && email.equals(otherInfo.getEmail())
                    && address.equals(otherInfo.getAddress())
                    && tags.equals(otherInfo.getTags());
        }
        return false;
    }

    @Override
    public String toString() {
        return this.name.toString();
    }
}
