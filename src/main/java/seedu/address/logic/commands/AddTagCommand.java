package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG_ASSIGN;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.commons.util.CommandUtil;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.event.Event;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonInformation;
import seedu.address.model.tag.Tag;

/**
 * Assigns one or more tags to one or more target persons.
 * <p>
 * Each target is resolved using {@link PersonInformation}. If any target is ambiguous
 * or has no match, command execution fails with the corresponding error.
 */
public class AddTagCommand extends Command {
    public static final String COMMAND_WORD = "tag";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Assigns tags to one or more persons.\n"
            + "Parameters: "
            + PREFIX_TAG_ASSIGN + "TAG_TO_ASSIGN ["
            + PREFIX_TAG_ASSIGN + "TAG_TO_ASSIGN]... "
            + PREFIX_NAME + "NAME "
            + "[" + PREFIX_PHONE + "PHONE_NUMBER] "
            + "[" + PREFIX_EMAIL + "EMAIL] "
            + "[" + PREFIX_ADDRESS + "ADDRESS] "
            + "[" + PREFIX_TAG + "TAG]... "
            + "[more " + PREFIX_NAME + "NAME ...]...\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_TAG_ASSIGN + "CS2103 "
            + PREFIX_TAG_ASSIGN + "CS2030S "
            + PREFIX_NAME + "Alice "
            + PREFIX_NAME + "Joe";

    public static final String MESSAGE_TAG_SUCCESS = "Tagged %1$d person(s) with [%2$s]: %3$s";

    private final List<PersonInformation> targets;
    private final Set<Tag> tagsToAssign;

    /**
     * Creates a TagCommand to assign {@code tagsToAssign} to all persons matching {@code targets}.
     */
    public AddTagCommand(List<PersonInformation> targets, Set<Tag> tagsToAssign) {
        requireNonNull(targets);
        requireNonNull(tagsToAssign);
        this.targets = List.copyOf(targets);
        this.tagsToAssign = Set.copyOf(tagsToAssign);
    }

    /**
     * Resolves all targets, applies the new tags to each resolved person, and refreshes the person list.
     *
     * @param model {@code Model} which the command should operate on
     * @return feedback containing number of persons tagged, assigned tags and person names
     * @throws CommandException if any target cannot be uniquely resolved
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        // Resolve all persons first
        List<Person> uniquePersons = resolvePersons(model);
        // Apply tags for each person
        applyTags(model, uniquePersons);
        // refresh the listing
        model.showAllPersons();

        String tagNames = tagsToAssign.stream()
                .map(t -> t.tagName)
                .sorted()
                .collect(Collectors.joining(", "));

        String personNames = uniquePersons.stream()
                .map(p -> p.getNameString())
                .collect(Collectors.joining(", "));

        return new CommandResult(String.format(MESSAGE_TAG_SUCCESS,
                uniquePersons.size(), tagNames, personNames));
    }

    /**
     * Resolves all configured target descriptors into concrete {@link Person} objects.
     * Duplicate resolved persons are removed while preserving encounter order.
     */
    private List<Person> resolvePersons(Model model) throws CommandException {
        List<Person> resolvedPersons = new ArrayList<>();
        for (PersonInformation targetInfo : targets) {
            Person person = CommandUtil.targetPerson(model, targetInfo);
            resolvedPersons.add(person);
        }

        // De-duplicate in case the same person was specified more than once
        return resolvedPersons.stream()
                .filter(new HashSet<>()::add)
                .collect(Collectors.toList());
    }

    /**
     * Applies {@link #tagsToAssign} to each resolved person and updates the model.
     * Existing tags and linked events are preserved.
     */
    private void applyTags(Model model, List<Person> uniquePersons) {
        for (Person person : uniquePersons) {
            Set<Tag> mergedTags = new HashSet<>(person.getTags());
            mergedTags.addAll(tagsToAssign);

            Person updatedPerson = new Person(
                    person.getName(), person.getPhone(),
                    person.getEmail(), person.getAddress(),
                    mergedTags, person.getPhoto());
            for (Event event : person.getEvents()) {
                updatedPerson.addEvent(event);
            }

            model.setPerson(person, updatedPerson);
        }
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AddTagCommand other) {
            return targets.equals(other.targets) && tagsToAssign.equals(other.tagsToAssign);
        }
        return false;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targets", targets)
                .add("tagsToAssign", tagsToAssign)
                .toString();
    }

}
