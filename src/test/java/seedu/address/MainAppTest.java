package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Application;
import javafx.stage.Stage;
import seedu.address.commons.core.Config;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.ConfigUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.Storage;
import seedu.address.storage.UserPrefsStorage;
import seedu.address.testutil.PersonBuilder;
import seedu.address.ui.Ui;

public class MainAppTest {

    @AfterEach
    public void resetLogLevel() {
        LogsCenter.init(new Config());
    }

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

    @Test
    public void initLogging_setsConfiguredLogLevel() throws Exception {
        TestMainApp app = new TestMainApp();
        Config config = new Config();
        config.setLogLevel(Level.FINE);

        invokeInitLogging(app, config);

        assertEquals(Level.FINE, LogsCenter.getLogger(MainAppTest.class).getParent().getLevel());
    }

    @Test
    public void init_withConfigParameter_initializesMainComponents(@TempDir Path tempDir) throws Exception {
        MainApp app = new MainApp();
        Path configFile = tempDir.resolve("config.json");
        Path prefsFile = tempDir.resolve("prefs.json");
        Path addressBookFile = tempDir.resolve("addressbook.json");
        Config config = new Config();
        config.setUserPrefsFilePath(prefsFile);
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setAddressBookFilePath(addressBookFile);
        ConfigUtil.saveConfig(config, configFile);
        new JsonUserPrefsStorage(prefsFile).saveUserPrefs(userPrefs);
        registerApplicationParameters(app, List.of("--config=" + configFile));

        app.init();

        assertNotNull(app.config);
        assertNotNull(app.storage);
        assertNotNull(app.model);
        assertNotNull(app.logic);
        assertNotNull(app.ui);
    }

    @Test
    public void initConfig_nullPath_usesDefaultConfigFile(@TempDir Path tempDir) throws Exception {
        TestMainApp app = new TestMainApp();
        Path defaultConfigFile = Config.DEFAULT_CONFIG_FILE;
        byte[] originalContents = Files.exists(defaultConfigFile) ? Files.readAllBytes(defaultConfigFile) : null;

        try {
            Config initializedConfig = app.initConfig(null);

            assertNotNull(initializedConfig);
            assertTrue(Files.exists(defaultConfigFile));
        } finally {
            if (originalContents == null) {
                Files.deleteIfExists(defaultConfigFile);
            } else {
                Files.write(defaultConfigFile, originalContents);
            }
        }
    }

    @Test
    public void initConfig_customMissingPath_createsConfigFile(@TempDir Path tempDir) {
        TestMainApp app = new TestMainApp();
        Path configFile = tempDir.resolve("missing-config.json");

        Config initializedConfig = app.initConfig(configFile);

        assertEquals(new Config(), initializedConfig);
        assertTrue(Files.exists(configFile));
    }

    @Test
    public void initConfig_invalidConfigFile_usesDefaultConfig(@TempDir Path tempDir) throws IOException {
        TestMainApp app = new TestMainApp();
        Path configFile = tempDir.resolve("invalid-config.json");
        Files.writeString(configFile, "not json");

        Config initializedConfig = app.initConfig(configFile);

        assertEquals(new Config(), initializedConfig);
    }

    @Test
    public void initConfig_configCannotBeSaved_returnsConfig(@TempDir Path tempDir) throws IOException {
        TestMainApp app = new TestMainApp();
        Path configDirectory = tempDir.resolve("config-directory");
        Files.createDirectory(configDirectory);

        Config initializedConfig = app.initConfig(configDirectory);

        assertEquals(new Config(), initializedConfig);
    }

    @Test
    public void initPrefs_existingPrefs_returnsAndSavesPrefs(@TempDir Path tempDir) {
        TestMainApp app = new TestMainApp();
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setAddressBookFilePath(tempDir.resolve("addressbook.json"));
        UserPrefsStorageStub storage = new UserPrefsStorageStub(tempDir.resolve("prefs.json"),
                Optional.of(userPrefs), null, false);

        UserPrefs initializedPrefs = app.initPrefs(storage);

        assertEquals(userPrefs, initializedPrefs);
        assertEquals(userPrefs, storage.savedUserPrefs);
    }

    @Test
    public void initPrefs_missingPrefs_usesDefaultPrefs(@TempDir Path tempDir) {
        TestMainApp app = new TestMainApp();
        UserPrefsStorageStub storage = new UserPrefsStorageStub(tempDir.resolve("prefs.json"),
                Optional.empty(), null, false);

        UserPrefs initializedPrefs = app.initPrefs(storage);

        assertEquals(new UserPrefs(), initializedPrefs);
        assertEquals(new UserPrefs(), storage.savedUserPrefs);
    }

    @Test
    public void initPrefs_dataLoadingException_usesDefaultPrefs(@TempDir Path tempDir) {
        TestMainApp app = new TestMainApp();
        UserPrefsStorageStub storage = new UserPrefsStorageStub(tempDir.resolve("prefs.json"),
                Optional.empty(), new DataLoadingException(new IOException("bad prefs")), false);

        UserPrefs initializedPrefs = app.initPrefs(storage);

        assertEquals(new UserPrefs(), initializedPrefs);
        assertEquals(new UserPrefs(), storage.savedUserPrefs);
    }

    @Test
    public void initPrefs_saveFails_returnsPrefs(@TempDir Path tempDir) {
        TestMainApp app = new TestMainApp();
        UserPrefs userPrefs = new UserPrefs();
        UserPrefsStorageStub storage = new UserPrefsStorageStub(tempDir.resolve("prefs.json"),
                Optional.of(userPrefs), null, true);

        UserPrefs initializedPrefs = app.initPrefs(storage);

        assertEquals(userPrefs, initializedPrefs);
    }

    @Test
    public void initModelManager_existingAddressBook_usesStoredData(@TempDir Path tempDir) throws Exception {
        TestMainApp app = new TestMainApp();
        AddressBook addressBook = new AddressBook();
        Person person = new PersonBuilder().withName("Alice Pauline").build();
        addressBook.addPerson(person);
        StorageStub storage = new StorageStub(tempDir.resolve("addressbook.json"),
                Optional.of(addressBook), null);

        Model model = invokeInitModelManager(app, storage, new UserPrefs());

        assertEquals(1, model.getAddressBook().getPersonList().size());
        assertEquals(person, model.getAddressBook().getPersonList().get(0));
        assertNull(app.dataLoadingError);
    }

    @Test
    public void initModelManager_missingAddressBook_usesSampleData(@TempDir Path tempDir) throws Exception {
        TestMainApp app = new TestMainApp();
        StorageStub storage = new StorageStub(tempDir.resolve("addressbook.json"), Optional.empty(), null);

        Model model = invokeInitModelManager(app, storage, new UserPrefs());

        assertFalse(model.getAddressBook().getPersonList().isEmpty());
        assertNull(app.dataLoadingError);
    }

    @Test
    public void initModelManager_dataLoadingException_usesEmptyAddressBook(@TempDir Path tempDir)
            throws Exception {
        TestMainApp app = new TestMainApp();
        StorageStub storage = new StorageStub(tempDir.resolve("addressbook.json"), Optional.empty(),
                new DataLoadingException(new IOException("bad address book")));

        Model model = invokeInitModelManager(app, storage, new UserPrefs());

        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertEquals("bad address book", app.dataLoadingError);
    }

    @Test
    public void initModelManager_dataLoadingExceptionWithoutCause_usesExceptionMessage(@TempDir Path tempDir)
            throws Exception {
        TestMainApp app = new TestMainApp();
        StorageStub storage = new StorageStub(tempDir.resolve("addressbook.json"), Optional.empty(),
                new DataLoadingException(null));

        Model model = invokeInitModelManager(app, storage, new UserPrefs());

        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertNull(app.dataLoadingError);
    }

    @Test
    public void stop_userPrefsSaved_clearsHelpDirectory(@TempDir Path tempDir) {
        TestMainApp app = new TestMainApp();
        StorageStub storage = new StorageStub(tempDir.resolve("addressbook.json"), Optional.empty(), null);
        UserPrefs userPrefs = new UserPrefs();
        app.storage = storage;
        app.model = new ModelManager(new AddressBook(), userPrefs);

        app.stop();

        assertEquals(userPrefs, storage.savedUserPrefs);
    }

    @Test
    public void stop_saveUserPrefsFails_handlesException(@TempDir Path tempDir) {
        TestMainApp app = new TestMainApp();
        StorageStub storage = new StorageStub(tempDir.resolve("addressbook.json"), Optional.empty(), null);
        storage.isSaveUserPrefsFailing = true;
        app.storage = storage;
        app.model = new ModelManager(new AddressBook(), new UserPrefs());

        app.stop();

        assertTrue(storage.isSaveUserPrefsCalled);
    }

    private static Model invokeInitModelManager(MainApp app, Storage storage, ReadOnlyUserPrefs userPrefs)
            throws Exception {
        Method method = MainApp.class.getDeclaredMethod("initModelManager", Storage.class, ReadOnlyUserPrefs.class);
        method.setAccessible(true);
        return (Model) method.invoke(app, storage, userPrefs);
    }

    private static void invokeInitLogging(MainApp app, Config config) throws Exception {
        Method method = MainApp.class.getDeclaredMethod("initLogging", Config.class);
        method.setAccessible(true);
        method.invoke(app, config);
    }

    private static void registerApplicationParameters(MainApp app, List<String> args) throws Exception {
        Class<?> parametersImplClass = Class.forName("com.sun.javafx.application.ParametersImpl");
        Object parameters = parametersImplClass.getDeclaredConstructor(List.class).newInstance(args);
        Method registerParameters = parametersImplClass.getDeclaredMethod("registerParameters",
                Application.class, Application.Parameters.class);
        registerParameters.invoke(null, app, parameters);
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

    private static class UserPrefsStorageStub implements UserPrefsStorage {
        private final Path userPrefsFilePath;
        private final Optional<UserPrefs> userPrefsToRead;
        private final DataLoadingException dataLoadingException;
        private final boolean isSaveFailing;
        private ReadOnlyUserPrefs savedUserPrefs;

        UserPrefsStorageStub(Path userPrefsFilePath, Optional<UserPrefs> userPrefsToRead,
                DataLoadingException dataLoadingException, boolean isSaveFailing) {
            this.userPrefsFilePath = userPrefsFilePath;
            this.userPrefsToRead = userPrefsToRead;
            this.dataLoadingException = dataLoadingException;
            this.isSaveFailing = isSaveFailing;
        }

        @Override
        public Path getUserPrefsFilePath() {
            return userPrefsFilePath;
        }

        @Override
        public Optional<UserPrefs> readUserPrefs() throws DataLoadingException {
            if (dataLoadingException != null) {
                throw dataLoadingException;
            }
            return userPrefsToRead;
        }

        @Override
        public void saveUserPrefs(ReadOnlyUserPrefs userPrefs) throws IOException {
            if (isSaveFailing) {
                throw new IOException("save failed");
            }
            savedUserPrefs = userPrefs;
        }
    }

    private static class StorageStub implements Storage {
        private final Path addressBookFilePath;
        private final Optional<ReadOnlyAddressBook> addressBookToRead;
        private final DataLoadingException dataLoadingException;
        private boolean isSaveUserPrefsFailing;
        private boolean isSaveUserPrefsCalled;
        private ReadOnlyUserPrefs savedUserPrefs;

        StorageStub(Path addressBookFilePath, Optional<ReadOnlyAddressBook> addressBookToRead,
                DataLoadingException dataLoadingException) {
            this.addressBookFilePath = addressBookFilePath;
            this.addressBookToRead = addressBookToRead;
            this.dataLoadingException = dataLoadingException;
        }

        @Override
        public Path getUserPrefsFilePath() {
            return addressBookFilePath.resolveSibling("prefs.json");
        }

        @Override
        public Optional<UserPrefs> readUserPrefs() {
            return Optional.empty();
        }

        @Override
        public void saveUserPrefs(ReadOnlyUserPrefs userPrefs) throws IOException {
            isSaveUserPrefsCalled = true;
            if (isSaveUserPrefsFailing) {
                throw new IOException("save failed");
            }
            savedUserPrefs = userPrefs;
        }

        @Override
        public Path getAddressBookFilePath() {
            return addressBookFilePath;
        }

        @Override
        public Optional<ReadOnlyAddressBook> readAddressBook() throws DataLoadingException {
            if (dataLoadingException != null) {
                throw dataLoadingException;
            }
            return addressBookToRead;
        }

        @Override
        public Optional<ReadOnlyAddressBook> readAddressBook(Path filePath) throws DataLoadingException {
            return readAddressBook();
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook addressBook) {
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook addressBook, Path filePath) {
        }
    }
}
