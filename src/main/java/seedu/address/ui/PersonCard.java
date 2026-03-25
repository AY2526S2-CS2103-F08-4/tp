package seedu.address.ui;

import java.nio.file.Paths;
import java.util.Comparator;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.shape.Circle;
import seedu.address.model.person.Person;

/**
 * An UI component that displays information of a {@code Person}.
 */
public class PersonCard extends UiPart<Region> {

    private static final String FXML = "PersonListCard.fxml";
    private static final String DEFAULT_IMAGE = "/images/pepe-default.png";

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

    /**
     * Creates a {@code PersonCode} with the given {@code Person} and index to display.
     */
    public PersonCard(Person person, int displayedIndex) {
        super(FXML);
        this.person = person;
        id.setText(displayedIndex + ". ");
        name.setText(person.getName().fullName);
        phone.setText(person.getPhone().value);
        address.setText(person.getAddress().map(addr -> addr.value).orElse(""));
        email.setText(person.getEmail().map(email -> email.value).orElse(""));
        person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .forEach(tag -> tags.getChildren().add(new Label(tag.tagName)));
        handlePhoto(person);
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
            photo.setFill(javafx.scene.paint.Color.valueOf("#424242"));
            altText.setVisible(true);
        }

        photo.setStroke(javafx.scene.paint.Color.valueOf("#EF7C00")); // NUS Gold color
        photo.setStrokeWidth(2.0); // Thickness of Border
    }
}
