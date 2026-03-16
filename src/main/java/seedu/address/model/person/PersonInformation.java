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
    public final Name name;
    public final Optional<Phone> phone;
    public final Optional<Email> email;
    public final Optional<Address> address;
    public final Set<Tag> tags;

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
}
