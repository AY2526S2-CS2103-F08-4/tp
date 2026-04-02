package seedu.address.ui;

import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.Person;

/**
 * A UI component that displays information of a {@code Person}.
 */
public class PersonCard extends UiPart<Region> {

    private static final String FXML = "PersonListCard.fxml";
    private static final Map<String, String> TAG_STYLE_BY_NAME = new HashMap<>();

    private static final String TAG_BORDER_COLOR = "black";
    private static final String TAG_BORDER_WIDTH = "0.5";
    private static final String DEFAULT_IMAGE = "/images/pepe-default.png";

    private static final Logger logger = LogsCenter.getLogger(PersonCard.class);

    /**
     * Note: Certain keywords such as "location" and "resources" are reserved keywords in JavaFX.
     * As a consequence, UI elements' variable names cannot be set to such keywords
     * or an exception will be thrown by JavaFX during runtime.
     *
     * @see <a href="https://github.com/se-edu/addressbook-level4/issues/336">The issue on AddressBook level 4</a>
     */

    public final Person person;

    @FXML
    private HBox cardPane;
    @FXML
    private Label name;
    @FXML
    private Label id;
    @FXML
    private Label phone;
    @FXML
    private Label address;
    @FXML
    private Label email;
    @FXML
    private FlowPane tags;
    @FXML
    private Circle photo;
    @FXML
    private Label altText;
    @FXML
    private Rectangle personCardSlidingAccent;

    /**
     * Creates a {@code PersonCode} with the given {@code Person} and index to display.
     */
    public PersonCard(Person person, int displayedIndex) {
        super(FXML);
        this.person = person;
        id.setText(displayedIndex + ". ");
        name.setText(person.getName().fullName);
        personCardSlidingAccent.visibleProperty().bind(
                this.getRoot().focusedProperty().or(this.getRoot().hoverProperty())
        );
        phone.setText(person.getPhone().value);
        address.setText(person.getAddress().map(addr -> addr.value).orElse(""));
        email.setText(person.getEmail().map(email -> email.value).orElse(""));
        person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .forEach(tag -> tags.getChildren().add(createTagLabel(tag.tagName)));
        handlePhoto(person);
    }

    /**
     * Creates a tag label and applies the generated style for the given tag name.
     *
     * @param tagName Name of the tag to display.
     * @return A styled JavaFX {@code Label} representing the tag.
     */
    private static Label createTagLabel(String tagName) {
        Label label = new Label(tagName);
        label.setStyle(getTagStyle(tagName));
        return label;
    }

    /**
     * Returns the style for a tag if it does not exist yet.
     *
     * @param tagName Name of the tag.
     * @return Inline CSS style string for the tag.
     */
    private static String getTagStyle(String tagName) {
        String lowerCaseTagName = tagName.toLowerCase();
        return TAG_STYLE_BY_NAME.computeIfAbsent(lowerCaseTagName, PersonCard::generateTagStyle);
    }

    /**
     * Generates a deterministic random-looking style for a tag name.
     *
     * @param tagName Lowercase tag name used as the random seed source.
     * @return Inline CSS style containing text and background colors.
     */
    private static String generateTagStyle(String tagName) {
        Random random = new Random(tagName.hashCode());
        Color backgroundColor = Color.hsb(
                random.nextDouble() * 360,
                0.5 + random.nextDouble() * 0.35,
                0.55 + random.nextDouble() * 0.3);
        Color textColor = backgroundColor.getBrightness() < 0.65 ? Color.WHITE : Color.BLACK;

        return String.format("-fx-text-fill: %s; -fx-background-color: %s; -fx-border-color: %s; -fx-border-width: %s;",
                toHex(textColor), toHex(backgroundColor), TAG_BORDER_COLOR, TAG_BORDER_WIDTH);
    }

    /**
     * Converts a JavaFX {@code Color} into a {@code #RRGGBB} hex string.
     *
     * @param color JavaFX color to convert.
     * @return Hex color string in the form {@code #RRGGBB}.
     */
    private static String toHex(Color color) {
        return String.format("#%02X%02X%02X",
                (int) Math.round(color.getRed() * 255),
                (int) Math.round(color.getGreen() * 255),
                (int) Math.round(color.getBlue() * 255));
    }

    /**
     * Handles the type of image that should be displayed.
     * @param person is the Person we need to extract the Photo object from to display on the card.
     */
    public void handlePhoto(Person person) {
        Image profilePicture = null;

        try {
            if (person.getPhoto().isEmpty()) {
                java.io.InputStream stream = this.getClass().getResourceAsStream(DEFAULT_IMAGE);
                if (stream != null) {
                    profilePicture = new Image(stream);
                }
            } else {
                String fileUri = Paths.get(person.getPhoto().get().getPath()).toUri().toString();
                profilePicture = new Image(fileUri);
            }
        } catch (Exception e) {
            // Handle silently
        }

        if (profilePicture != null && !profilePicture.isError()) {
            photo.setFill(new javafx.scene.paint.ImagePattern(profilePicture));
            altText.setVisible(false);
        } else {
            photo.getStyleClass().add("empty");
            altText.setVisible(true);
        }
    }

    /**
     * Retrieves the accent bar associated with the person card UI component.
     *
     * @return A {@code Rectangle} representing the accent bar for visual styling.
     */
    public Rectangle getAccentBar() {
        return personCardSlidingAccent;
    }

    /**
     * Copies information contained within a Person Card to the clipboard.
     */
    @FXML
    private void copyPersonCard() {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent personInformation = new ClipboardContent();
        // Mandatory Fields
        String name = person.getNameString();
        String phoneNumber = person.getPhoneString();

        // Optional Fields
        String tag = person.getTagsString();
        String address = person.getAddressString().orElse("");
        String email = person.getEmailString().orElse("");

        // Copy Operation
        String textToCopy = Stream.of(name, phoneNumber, tag, address, email)
                .filter(field -> !field.isEmpty())
                .collect(Collectors.joining("\n"));
        personInformation.putString(textToCopy);
        clipboard.setContent(personInformation);
    }
}
