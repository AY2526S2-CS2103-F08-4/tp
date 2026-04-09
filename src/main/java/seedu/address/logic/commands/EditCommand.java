package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHOTO;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import seedu.address.commons.util.CollectionUtil;
import seedu.address.commons.util.CommandUtil;
import seedu.address.commons.util.PhotoStorageUtil;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.event.Event;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonInformation;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Photo;
import seedu.address.model.tag.Tag;

/**
 * Edits the details of an existing person in the address book.
 */
public class EditCommand extends Command {

    public static final String COMMAND_WORD = "edit";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits the details of the person identified "
            + "by name and optional identifying fields. "
            + "Existing values will be overwritten by the input values.\n"
            + "Format: "
            + COMMAND_WORD + " " + PREFIX_NAME + "NAME "
            + "[" + PREFIX_PHONE + "PHONE_NUMBER] "
            + "[" + PREFIX_EMAIL + "EMAIL] "
            + "[" + PREFIX_ADDRESS + "ADDRESS] "
            + "[" + PREFIX_TAG + "TAG]... "
            + "-- "
            + "[" + PREFIX_NAME + "NAME] "
            + "[" + PREFIX_PHONE + "PHONE_NUMBER] "
            + "[" + PREFIX_EMAIL + "EMAIL] "
            + "[" + PREFIX_ADDRESS + "ADDRESS] "
            + "[" + PREFIX_TAG + "TAG]... "
            + "[" + PREFIX_PHOTO + "PHOTO_PATH]\n"
            + "Example: " + COMMAND_WORD + " " + PREFIX_NAME + "John Doe "
            + PREFIX_PHONE + "91234567 -- "
            + PREFIX_EMAIL + "john.new@example.com "
            + PREFIX_TAG + "friends";

    public static final String MESSAGE_EDIT_PERSON_SUCCESS = "Edited Person: %1$s";
    public static final String MESSAGE_NOT_EDITED = "At least one field to edit must be provided.";
    public static final String MESSAGE_NO_CHANGES_DONE = "No changes done.";
    public static final String MESSAGE_DUPLICATE_PERSON = "A contact with the same phone number already exists.";

    private final PersonInformation targetInfo;
    private final EditPersonDescriptor editPersonDescriptor;
    private final String targetDirectory;

    /**
     * Creates an EditCommand that encapsulates the details required to edit a person's information.
     *
     * @param targetInfo The {@code PersonInformation} object representing the person to edit.
     *                   Must not be null and must specify the person's identifying details.
     * @param editPersonDescriptor The {@code EditPersonDescriptor} object containing the details
     *                             to update the person's information with. Must not be null.
     */
    public EditCommand(PersonInformation targetInfo, EditPersonDescriptor editPersonDescriptor) {
        requireNonNull(targetInfo);
        requireNonNull(editPersonDescriptor);

        this.targetInfo = targetInfo;
        this.editPersonDescriptor = new EditPersonDescriptor(editPersonDescriptor);
        this.targetDirectory = PhotoStorageUtil.DEFAULT_IMAGE_DIR;
    }

    /**
     * Creates an EditCommand specifying a target directory (Used for testing).
     */
    public EditCommand(PersonInformation targetInfo, EditPersonDescriptor editPersonDescriptor,
                       String targetDirectory) {
        requireNonNull(targetInfo);
        requireNonNull(editPersonDescriptor);

        this.targetInfo = targetInfo;
        this.editPersonDescriptor = new EditPersonDescriptor(editPersonDescriptor);
        this.targetDirectory = targetDirectory;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Person personToEdit = CommandUtil.targetPerson(model, targetInfo);
        Person previewPerson = createEditedPerson(personToEdit, editPersonDescriptor);

        //If edit is not the same, but phone number already exist to another person
        if (!personToEdit.isSamePerson(previewPerson) && model.hasPerson(previewPerson)) {
            throw new CommandException(MESSAGE_DUPLICATE_PERSON);
        }

        if (editPersonDescriptor.getPhoto().isPresent()) {
            Photo newPhoto;
            try {
                newPhoto = PhotoStorageUtil.copyPhotoToDirectory(
                        editPersonDescriptor.getPhoto().get(),
                        this.targetDirectory);
                editPersonDescriptor.setPhoto(newPhoto);
            } catch (IOException e) {
                throw new CommandException(Messages.MESSAGE_SAVE_PHOTO_FAIL + e.getMessage());
            }

            if (personToEdit.getPhoto().isPresent()) {
                Photo oldPhoto = personToEdit.getPhoto().get();
                if (!oldPhoto.equals(newPhoto)) {
                    CommandUtil.safelyDeletePhoto(model, personToEdit, oldPhoto, this.targetDirectory);
                }
            }
        }

        Person editedPerson = createEditedPerson(personToEdit, editPersonDescriptor);

        //If edit is the same as before, do not proceed with edit and return appropriate message
        if (personToEdit.equals(editedPerson)) {
            return new CommandResult(MESSAGE_NO_CHANGES_DONE);
        }

        model.setPerson(personToEdit, editedPerson);
        model.showPerson(editedPerson);
        return new CommandResult(String.format(MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson)));
    }

    /**
     * Merges and returns a {@code Person} with the details of {@code personToEdit}
     * edited with {@code editPersonDescriptor}.
     *
     * @param personToEdit The original person whose details are to be edited. Must not be null.
     * @param editPersonDescriptor The descriptor containing the details to edit the person with. Must not be null.
     * @return A new {@code Person} object with the combined edited details.
     */
    private static Person createEditedPerson(Person personToEdit, EditPersonDescriptor editPersonDescriptor) {
        assert personToEdit != null;
        assert editPersonDescriptor != null;

        Name updatedName = editPersonDescriptor.getName().isPresent()
            ? editPersonDescriptor.getName().get()
            : personToEdit.getName();
        Phone updatedPhone = editPersonDescriptor.getPhone().isPresent()
            ? editPersonDescriptor.getPhone().get()
            : personToEdit.getPhone();
        Optional<Email> updatedEmail = editPersonDescriptor.getEmail().isPresent()
                ? editPersonDescriptor.getEmail() : personToEdit.getEmail();
        Optional<Address> updatedAddress = editPersonDescriptor.getAddress().isPresent()
                ? editPersonDescriptor.getAddress() : personToEdit.getAddress();
        Set<Tag> updatedTags = createEditedTags(personToEdit.getTags(), editPersonDescriptor.getTags());
        Optional<Photo> updatedPhoto = editPersonDescriptor.getPhoto().isPresent()
                ? editPersonDescriptor.getPhoto()
                : personToEdit.getPhoto();

        Person editedPerson = new Person(updatedName, updatedPhone, updatedEmail, updatedAddress, updatedTags,
                updatedPhoto);
        for (Event existingEvent : personToEdit.getEvents()) {
            editedPerson.addEvent(existingEvent);
        }

        return editedPerson;
    }

    /**
     * Returns updated tags using toggle semantics for edit:
     * empty tag set clears all tags, otherwise each provided tag toggles membership.
     */
    private static Set<Tag> createEditedTags(Set<Tag> existingTags, Optional<Set<Tag>> tagsToEdit) {
        if (tagsToEdit.isEmpty()) {
            return existingTags;
        }

        Set<Tag> newTags = tagsToEdit.get();
        if (newTags.isEmpty()) {
            return Collections.emptySet();
        }

        Set<Tag> toggledTags = new HashSet<>(existingTags);
        for (Tag newTag : newTags) {
            Optional<Tag> existingTagToRemove = toggledTags.stream()
                    .filter(existingTag -> existingTag.tagName.equalsIgnoreCase(newTag.tagName))
                    .findFirst();
            if (existingTagToRemove.isPresent()) {
                toggledTags.remove(existingTagToRemove.get());
            } else {
                toggledTags.add(newTag);
            }
        }
        return toggledTags;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof EditCommand)) {
            return false;
        }

        EditCommand otherEditCommand = (EditCommand) other;
        return targetInfo.equals(otherEditCommand.targetInfo)
                && editPersonDescriptor.equals(otherEditCommand.editPersonDescriptor);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetInfo", targetInfo)
                .add("editPersonDescriptor", editPersonDescriptor)
                .toString();
    }

    /**
     * Stores the details to edit the person with. Each non-empty field value will replace the
     * corresponding field value of the person.
     */
    public static class EditPersonDescriptor {
        private Name name;
        private Phone phone;
        private Email email;
        private Address address;
        private Set<Tag> tags;
        private Photo photo;

        public EditPersonDescriptor() {}

        /**
         * Copy constructor.
         * A defensive copy of {@code tags} is used internally.
         */
        public EditPersonDescriptor(EditPersonDescriptor toCopy) {
            setName(toCopy.name);
            setPhone(toCopy.phone);
            setEmail(toCopy.email);
            setAddress(toCopy.address);
            setTags(toCopy.tags);
            setPhoto(toCopy.photo);
        }

        /**
         * Returns true if at least one field is edited.
         */
        public boolean isAnyFieldEdited() {
            return CollectionUtil.isAnyNonNull(name, phone, email, address, tags, photo);
        }

        public void setName(Name name) {
            this.name = name;
        }

        public Optional<Name> getName() {
            return Optional.ofNullable(name);
        }

        public void setPhone(Phone phone) {
            this.phone = phone;
        }

        public Optional<Phone> getPhone() {
            return Optional.ofNullable(phone);
        }

        public void setEmail(Email email) {
            this.email = email;
        }

        public Optional<Email> getEmail() {
            return Optional.ofNullable(email);
        }

        public void setAddress(Address address) {
            this.address = address;
        }

        public Optional<Address> getAddress() {
            return Optional.ofNullable(address);
        }

        public void setPhoto(Photo photo) {
            this.photo = photo;
        }

        public Optional<Photo> getPhoto() {
            return Optional.ofNullable(photo);
        }

        /**
         * Sets {@code tags} to this object's {@code tags}.
         * A defensive copy of {@code tags} is used internally.
         */
        public void setTags(Set<Tag> tags) {
            this.tags = (tags != null) ? new HashSet<>(tags) : null;
        }

        /**
         * Returns an unmodifiable tag set, which throws {@code UnsupportedOperationException}
         * if modification is attempted.
         * Returns {@code Optional#empty()} if {@code tags} is null.
         */
        public Optional<Set<Tag>> getTags() {
            return (tags != null) ? Optional.of(Collections.unmodifiableSet(tags)) : Optional.empty();
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }

            // instanceof handles nulls
            if (!(other instanceof EditPersonDescriptor)) {
                return false;
            }

            EditPersonDescriptor otherEditPersonDescriptor = (EditPersonDescriptor) other;
            return Objects.equals(name, otherEditPersonDescriptor.name)
                    && Objects.equals(phone, otherEditPersonDescriptor.phone)
                    && Objects.equals(email, otherEditPersonDescriptor.email)
                    && Objects.equals(address, otherEditPersonDescriptor.address)
                    && Objects.equals(tags, otherEditPersonDescriptor.tags)
                    && Objects.equals(photo, otherEditPersonDescriptor.photo);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .add("name", name)
                    .add("phone", phone)
                    .add("email", email)
                    .add("address", address)
                    .add("tags", tags)
                    .add("photo", photo)
                    .toString();
        }
    }
}
