package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class PersonCardTest {

    @BeforeAll
    public static void initJavaFxToolkit() {
        try {
            Platform.startup(() -> {
                // No-op. This initializes the JavaFX runtime for control creation in tests.
            });
        } catch (IllegalStateException e) {
            // Toolkit already initialized by another test.
        }
    }

     @Test
    public void personCard_constructor_displaysPersonDetailsAndIndex() {
        Person person = new PersonBuilder()
                .withName("Alex Yeoh")
                .withPhone("98765432")
                .withAddress("311, Clementi Ave 2, #02-25")
                .withEmail("alexyeoh@example.com")
                .build();
        PersonCard personCard = new PersonCard(person, 7);

        assertEquals("7. ", getLabelText(personCard, "id"));
        assertEquals("Alex Yeoh", getLabelText(personCard, "name"));
        assertEquals("98765432", getLabelText(personCard, "phone"));
        assertEquals("311, Clementi Ave 2, #02-25", getLabelText(personCard, "address"));
        assertEquals("alexyeoh@example.com", getLabelText(personCard, "email"));
        assertEquals(person, personCard.person);
    }

    @Test
    public void personCard_withoutOptionalFields_displaysEmptyAddressAndEmail() {
        Person person = new PersonBuilder()
                .withoutAddress()
                .withoutEmail()
                .build();
        PersonCard personCard = new PersonCard(person, 1);

        assertEquals("", getLabelText(personCard, "address"));
        assertEquals("", getLabelText(personCard, "email"));
    }

    @Test
    public void personCard_tagsDisplayedInAlphabeticalOrder() {
        Person person = new PersonBuilder().withTags("family", "friend", "classmate").build();
        PersonCard personCard = new PersonCard(person, 1);

        assertEquals(List.of("classmate", "family", "friend"), getDisplayedTagTexts(personCard));
    }
    
    @Test
    public void personCard_sameTagDifferentCase_hasSameStyle() {
        Person person = new PersonBuilder().withTags("friends", "FRIENDS").build();
        PersonCard personCard = new PersonCard(person, 1);
        String lowerCaseStyle = getStyleForTag(personCard, "friends");
        String upperCaseStyle = getStyleForTag(personCard, "FRIENDS");

        assertEquals(lowerCaseStyle, upperCaseStyle);
    }

    @Test
    public void personCard_differentTags_haveDifferentStyle() {
        Person person = new PersonBuilder().withTags("friends", "family").build();
        PersonCard personCard = new PersonCard(person, 1);
        String firstTagStyle = getStyleForTag(personCard, "friends");
        String secondTagStyle = getStyleForTag(personCard, "family");

        assertNotEquals(firstTagStyle, secondTagStyle);
    }

    @Test
    public void personCard_tagStyle_containsExpectedCssAttributes() {
        Person person = new PersonBuilder().withTags("classmates").build();
        PersonCard personCard = new PersonCard(person, 1);
        String style = getStyleForTag(personCard, "classmates");

        assertTrue(style.matches(".*-fx-text-fill: #[A-F0-9]{6};.*"));
        assertTrue(style.matches(".*-fx-background-color: #[A-F0-9]{6};.*"));
        assertTrue(style.matches(".*-fx-border-color: black;.*"));
        assertTrue(style.matches(".*-fx-border-width: 0.5;.*"));
    }

    private static String getStyleForTag(PersonCard personCard, String tagText) {
        FlowPane tagsPane = getTagsPane(personCard);

        for (Node node : tagsPane.getChildren()) {
            if (node instanceof Label) {
                Label label = (Label) node;
                if (label.getText().equals(tagText)) {
                    return label.getStyle();
                }
            }
        }

        fail("Tag not found in PersonCard: " + tagText);
        return "";
    }

    private static String getLabelText(PersonCard personCard, String fieldName) {
        try {
            Field field = PersonCard.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return ((Label) field.get(personCard)).getText();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new AssertionError("Unable to access label from PersonCard: " + fieldName, e);
        }
    }

    private static List<String> getDisplayedTagTexts(PersonCard personCard) {
        FlowPane tagsPane = getTagsPane(personCard);
        List<String> displayedTags = new ArrayList<>();
        for (Node node : tagsPane.getChildren()) {
            if (node instanceof Label) {
                displayedTags.add(((Label) node).getText());
            }
        }
        return displayedTags;
    }

    private static FlowPane getTagsPane(PersonCard personCard) {
        try {
            Field tagsField = PersonCard.class.getDeclaredField("tags");
            tagsField.setAccessible(true);
            return (FlowPane) tagsField.get(personCard);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new AssertionError("Unable to access tags pane from PersonCard", e);
        }
    }
}
