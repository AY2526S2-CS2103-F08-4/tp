package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import javafx.stage.Stage;
import seedu.address.ui.Ui;

public class MainAppTest {

    @Test
    public void start_withDataLoadingError_showsDataLoadingErrorOnUi() {
        TestMainApp app = new TestMainApp();
        RecordingUi ui = new RecordingUi();
        app.ui = ui;
        app.dataLoadingError = "duplicate persons";

        app.start(null);

        assertEquals(1, ui.startCallCount);
        assertEquals("duplicate persons", ui.lastDataLoadingError);
    }

    @Test
    public void start_withoutDataLoadingError_doesNotShowDataLoadingErrorOnUi() {
        TestMainApp app = new TestMainApp();
        RecordingUi ui = new RecordingUi();
        app.ui = ui;

        app.start(null);

        assertEquals(1, ui.startCallCount);
        assertNull(ui.lastDataLoadingError);
    }

    private static class TestMainApp extends MainApp {
        @Override
        public void init() {
            // No-op for unit testing start() behavior in isolation.
        }
    }

    private static class RecordingUi implements Ui {
        private int startCallCount;
        private String lastDataLoadingError;

        @Override
        public void start(Stage primaryStage) {
            startCallCount++;
        }

        @Override
        public void showDataLoadingError(String message) {
            lastDataLoadingError = message;
        }
    }
}
