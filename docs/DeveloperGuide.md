---
layout: default.md
title: "Developer Guide"
pageNav: 3
---

# NAB Developer Guide

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

### **Original Source**
* This project is based on the AddressBook-Level3 project created by the [SE-EDU initiative](https://se-education.org).
* Libraries used: [JavaFX](https://openjfx.io/), [Jackson](https://github.com/FasterXML/jackson), [JUnit5](https://github.com/junit-team/junit5)

### **AI-Assisted Work**
* Google Gemini was used to generate the NAB logo for the application and the GUI window.

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

### Architecture

<puml src="diagrams/ArchitectureDiagram.puml" width="280" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

Given below is a quick overview of main components and how they interact with each other.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](https://github.com/AY2526S2-CS2103-F08-4/tp/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/AY2526S2-CS2103-F08-4/tp/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `delete n/David`.

<puml src="diagrams/ArchitectureSequenceDiagram.puml" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* implements its functionality using a concrete `{Component Name}Manager` class (which follows the corresponding API `interface` mentioned in the previous point.)

For example, the `Logic` component defines its API in the `Logic.java` interface and implements its functionality using the [`LogicManager.java`](https://github.com/AY2526S2-CS2103-F08-4/tp/tree/master/src/main/java/seedu/address/logic/LogicManager.java) class which follows the `Logic` interface. Other components interact with a given component through its interface rather than the concrete class (reason: to prevent outside components being coupled to the implementation of a component), as illustrated in the (partial) class diagram below.

<puml src="diagrams/ComponentManagers.puml" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/AY2526S2-CS2103-F08-4/tp/tree/master/src/main/java/seedu/address/ui/Ui.java)

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` that is made up of parts e.g. `CommandBox`, `ResultDisplay`, `PersonListPanel`, `StatusBarFooter` etc. All these, including the `MainWindow`, inherit from the abstract `UiPart` class which captures the commonalities between classes that represent parts of the visible GUI.

Additionally, the `CommandBox` component will contain a `CommandHistory` component that is used to support the command history feature.

The `UI` component uses the JavaFX UI framework. The layout of these UI parts are defined in matching `.fxml` files that are in the `src/main/resources/view` folder. For example, the layout of the [`MainWindow`](https://github.com/AY2526S2-CS2103-F08-4/tp/tree/master/src/main/java/seedu/address/ui/MainWindow.java) is specified in [`MainWindow.fxml`](https://github.com/AY2526S2-CS2103-F08-4/tp/tree/master/src/main/resources/view/MainWindow.fxml)

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component, as it displays `Person` object residing in the `Model`.

### Logic component

**API** : [`Logic.java`](https://github.com/AY2526S2-CS2103-F08-4/tp/tree/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<puml src="diagrams/LogicClassDiagram.puml" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete n/David")` API call as an example.

<puml src="diagrams/DeleteSequenceDiagram.puml" alt="Interactions Inside the Logic Component for the `delete 1` Command" />

<box type="info" seamless>

**Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X) but due to a limitation of PlantUML, the lifeline continues till the end of diagram.
</box>

How the `Logic` component works:

1. When `Logic` is called upon to execute a command, it is passed to an `AddressBookParser` object which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
2. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
3. The command can communicate with the `Model` when it is executed (e.g., to delete a person).
    - Note that although this is shown as a single step in the diagram above (for simplicity), in the code it can take several interactions (between the command object and the `Model`) to achieve.
4. The result of the command execution is encapsulated as a `CommandResult` object which is returned back from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name e.g., `AddCommandParser`) which uses the other classes shown above to parse the user command and create a `XYZCommand` object (e.g., `AddCommand`) which the `AddressBookParser` returns back as a `Command` object.
* All `XYZCommandParser` classes (e.g., `AddCommandParser`, `DeleteCommandParser`, ...) inherit from the `Parser` interface so that they can be treated similarly where possible e.g, during testing.

### Model component
**API** : [`Model.java`](https://github.com/AY2526S2-CS2103-F08-4/tp/tree/master/src/main/java/seedu/address/model/Model.java)

<div align="center">
<puml src="diagrams/ModelClassDiagram.puml" width="800" />
</div>


The class diagram below illustrates the [`Person`](https://github.com/AY2526S2-CS2103-F08-4/tp/tree/master/src/main/java/seedu/address/model/person/Person.java) and [`Event`](https://github.com/AY2526S2-CS2103-F08-4/tp/tree/master/src/main/java/seedu/address/model/event/Event.java) classes and their related attribute classes:

<div align="center">
<puml src="diagrams/PersonEventClassDiagram.puml" width="700" />
</div>

The `Model` component,

* stores the address book data i.e., all `Person` objects (contained in a `UniquePersonList`) and all pinned `Person` objects (contained in a separate `UniquePersonList`), as well as all `Event` objects (contained in a `UniqueEventList`).
* exposes the currently 'selected' `Person` objects (e.g., results of a search query or a `pin` operation) as a _filtered_ and _sorted_ live view over the address book's person list, exposed to outsiders as an unmodifiable `ObservableList<Person>`
that can be 'observed' e.g. the UI can be bound to this list so that the UI automatically updates when the data in the list changes. The _sorted_ list's comparator is dynamically toggled — it floats pinned contacts to the top only when
showing all persons (`showAllPersonsPinnedFirst()`) and tag-filtered views (`showPersons(...)`), and is disabled during search-result, single-person, or event-view operations.
* exposes the currently 'selected' `Event` objects (e.g., results of an `event view` query) as a _filtered_ live view over the address book's event list, similarly exposed as an unmodifiable `ObservableList<Event>`.
* stores a `UserPrefs` object that represents the user’s preferences. This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components).

<box type="info" seamless>

**Note:** An alternative (arguably, a more OOP) model is given below. It has a `Tag` list in the `AddressBook`, which `Person` references. This allows `AddressBook` to only require one `Tag` object per unique tag, instead of each `Person` needing their own `Tag` objects.<br>

<div align="center">
<puml src="diagrams/BetterModelClassDiagram.puml" width="700" />
</div>

</box>


### Storage component

**API** : [`Storage.java`](https://github.com/AY2526S2-CS2103-F08-4/tp/tree/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="700" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* inherits from both `AddressBookStorage` and `UserPrefsStorage`, which means it can be treated as either one (if only the functionality of only one is needed).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`).
* persists `Event` objects as a top-level list in the address book JSON, identified by a unique integer ID. Each [`JsonAdaptedPerson`](https://github.com/AY2526S2-CS2103-F08-4/tp/tree/master/src/main/java/seedu/address/storage/JsonAdaptedPerson.java) stores a list of event IDs as foreign keys, which are resolved back into `Event` objects during loading.
* persists the pinned persons list separately within the address book JSON. During loading, pinned entries are resolved against the main persons list to ensure a single source of truth.
* **Note:** CSV import/export (`import`/`export` commands) is handled at the `Logic` layer via [`CsvUtil`](https://github.com/AY2526S2-CS2103-F08-4/tp/tree/master/src/main/java/seedu/address/commons/util/CsvUtil.java) in `Commons`, and is not part of the `Storage` component.

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### \[Proposed\] Undo/redo feature

#### Proposed Implementation

The proposed undo/redo mechanism is facilitated by `VersionedAddressBook`. It extends `AddressBook` with an undo/redo history, stored internally as an `addressBookStateList` and `currentStatePointer`. Additionally, it implements the following operations:

* `VersionedAddressBook#commit()` — Saves the current address book state in its history.
* `VersionedAddressBook#undo()` — Restores the previous address book state from its history.
* `VersionedAddressBook#redo()` — Restores a previously undone address book state from its history.

These operations are exposed in the `Model` interface as `Model#commitAddressBook()`, `Model#undoAddressBook()` and `Model#redoAddressBook()` respectively.

Given below is an example usage scenario and how the undo/redo mechanism behaves at each step.

Step 1. The user launches the application for the first time. The `VersionedAddressBook` will be initialized with the initial address book state, and the `currentStatePointer` pointing to that single address book state.

<puml src="diagrams/UndoRedoState0.puml" alt="UndoRedoState0" />

Step 2. The user executes `delete n/Marcus` command to delete a contact named "Marcus" in the address book. The `delete` command calls `Model#commitAddressBook()`, causing the modified state of the address book after the `delete n/Marcus` command executes to be saved in the `addressBookStateList`, and the `currentStatePointer` is shifted to the newly inserted address book state.

<puml src="diagrams/UndoRedoState1.puml" alt="UndoRedoState1" />

Step 3. The user executes `add n/David …​` to add a new person. The `add` command also calls `Model#commitAddressBook()`, causing another modified address book state to be saved into the `addressBookStateList`.

<puml src="diagrams/UndoRedoState2.puml" alt="UndoRedoState2" />

<box type="info" seamless>

**Note:** If a command fails its execution, it will not call `Model#commitAddressBook()`, so the address book state will not be saved into the `addressBookStateList`.

</box>

Step 4. The user now decides that adding the person was a mistake, and decides to undo that action by executing the `undo` command. The `undo` command will call `Model#undoAddressBook()`, which will shift the `currentStatePointer` once to the left, pointing it to the previous address book state, and restores the address book to that state.

<puml src="diagrams/UndoRedoState3.puml" alt="UndoRedoState3" />


<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index 0, pointing to the initial AddressBook state, then there are no previous AddressBook states to restore. The `undo` command uses `Model#canUndoAddressBook()` to check if this is the case. If so, it will return an error to the user rather
than attempting to perform the undo.

</box>

The following sequence diagram shows how an undo operation goes through the `Logic` component:

<puml src="diagrams/UndoSequenceDiagram-Logic.puml" alt="UndoSequenceDiagram-Logic" />

<box type="info" seamless>

**Note:** The lifeline for `UndoCommand` should end at the destroy marker (X) but due to a limitation of PlantUML, the lifeline reaches the end of diagram.

</box>

Similarly, how an undo operation goes through the `Model` component is shown below:

<puml src="diagrams/UndoSequenceDiagram-Model.puml" alt="UndoSequenceDiagram-Model" />

The `redo` command does the opposite — it calls `Model#redoAddressBook()`, which shifts the `currentStatePointer` once to the right, pointing to the previously undone state, and restores the address book to that state.

<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index `addressBookStateList.size() - 1`, pointing to the latest address book state, then there are no undone AddressBook states to restore. The `redo` command uses `Model#canRedoAddressBook()` to check if this is the case. If so, it will return an error to the user rather than attempting to perform the redo.

</box>

Step 5. The user then decides to execute the command `list`. Commands that do not modify the address book, such as `list`, will usually not call `Model#commitAddressBook()`, `Model#undoAddressBook()` or `Model#redoAddressBook()`. Thus, the `addressBookStateList` remains unchanged.

<puml src="diagrams/UndoRedoState4.puml" alt="UndoRedoState4" />

Step 6. The user executes `clear`, which calls `Model#commitAddressBook()`. Since the `currentStatePointer` is not pointing at the end of the `addressBookStateList`, all address book states after the `currentStatePointer` will be purged. Reason: It no longer makes sense to redo the `add n/David …​` command. This is the behavior that most modern desktop applications follow.

<puml src="diagrams/UndoRedoState5.puml" alt="UndoRedoState5" />

The following activity diagram summarizes what happens when a user executes a new command:

<puml src="diagrams/CommitActivityDiagram.puml" width="250" />

#### Design considerations:

**Aspect: How undo & redo executes:**

* **Alternative 1 (current choice):** Saves the entire address book.
  * Pros: Easy to implement.
  * Cons: May have performance issues in terms of memory usage.

* **Alternative 2:** Individual command knows how to undo/redo by
  itself.
  * Pros: Will use less memory (e.g., for `delete`, just save the person being deleted).
  * Cons: We must ensure that the implementation of each individual command are correct.

_{more aspects and alternatives to be added}_

### \[Proposed\] Data archiving

_{Explain here how the data archiving feature will be implemented}_

### Retrieval of past entered commands
#### Implementation
The command retrieval mechanism is implemented as a UI-level feature split between [`CommandBox`](https://github.com/AY2526S2-CS2103-F08-4/tp/tree/master/src/main/java/seedu/address/ui/CommandBox.java) and [`CommandHistory`](https://github.com/AY2526S2-CS2103-F08-4/tp/tree/master/src/main/java/seedu/address/ui/CommandHistory.java).

`CommandBox` owns a single `CommandHistory` instance for the application's UI session. This instance is responsible for
storing previously entered command strings and handling navigation state (e.g., current history pointer and the user's
latest in-progress input).

This design keeps parser and command execution logic unchanged, since retrieval is triggered by key events in the command input box rather than by a typed command word.

History is non-persistent by design. Command history exists only in memory for the current application run, and is cleared when the application closes.

#### Usage scenario
The interaction flow is as follows:

* User enters a command and presses `Enter`.
  1. `CommandBox` records the command into `CommandHistory`.
      * `CommandHistory` stores all previously entered commands in a list
  2. `CommandBox` executes the command through the existing Logic pipeline.

<puml src="diagrams/CommandHistoryEnterSequenceDiagram.puml" width="500"/>

* When user presses `Up`, `CommandBox` requests an older command from `CommandHistory` and updates the text field.
* When user presses `Down`, `CommandBox` requests a newer command (or restored in-progress input) from
  `CommandHistory` and updates the text field.

<div>
  <puml src="diagrams/CommandHistoryUpSequenceDiagram.puml" width="350"/>
  <puml src="diagrams/CommandHistoryDownSequenceDiagram.puml" width="350"/>
</div>

* Before navigating away, any currently input command by the user is saved as the latest command
* When user manually edits a previous command, `CommandBox` syncs the current text to
  `CommandHistory` as the in-progress input.


#### Design Considerations
**Aspect: Behavior of retrieval and navigation**
* **Alternative 1 (current choice):** Preserve the in-progress input while navigating history.

  Behavior: If the user has partially typed a command, navigates to older commands using Up, and then returns using Down,
  the original partially typed command is restored.

  * Pros: Intuitive for users who use Up/Down to inspect previous commands without wanting to lose current work.
  * Pros: Reduces accidental loss of in-progress input.
  * Cons: Requires storing one extra temporary input state (the current in-progress command).
* **Alternative 2:** Replace the current input with history entries and do not restore the original partial input.

  Behavior: If the user has partially typed a command and navigates history, editing a recalled command will replace the
  current line; the original partial command is not restored automatically.

  * Pros: Simpler state management and implementation.
  * Pros: Editing recalled history entries is straightforward.
  * Cons: Higher risk of losing in-progress input when browsing history.
  * Cons: Less forgiving for users who use history only for quick reference.

**Aspect: Usage**
* **Alternative 1 (current choice):** Treat command history as a UI enhancement in `CommandBox`.

  * Pros: Aligns with event-driven Up/Down key handling in the UI layer.
  * Pros: Keeps parser and command execution flow unchanged.
  * Pros: Low implementation complexity and low regression risk.
  * Cons: History is not directly accessible as a typed command (e.g. `history`).
* **Alternative 2:** Implement history as an explicit command in Logic (e.g. `history`, `history 10`).

  * Pros: Can present command history as structured output and potentially support filtering/limits.
  * Pros: Easier to expose to non-UI clients in future.
  * Cons: Adds parser/command/model changes for a feature primarily triggered by keyboard navigation.
  * Cons: Does not by itself provide the same quick Up/Down editing workflow.

Given current goals, Alternative 1 is preferred because the feature focuses on fast command-line input recall rather than
command-output reporting.
### Pin/Unpin contact feature

#### Implementation

The pin/unpin feature is implemented as [`PinCommand`](https://github.com/AY2526S2-CS2103-F08-4/tp/tree/master/src/main/java/seedu/address/logic/commands/PinCommand.java) and [`UnpinCommand`](https://github.com/AY2526S2-CS2103-F08-4/tp/tree/master/src/main/java/seedu/address/logic/commands/UnpinCommand.java), both implementing [`Command`](https://github.com/AY2526S2-CS2103-F08-4/tp/tree/master/src/main/java/seedu/address/logic/commands/Command.java). The feature spans the `Logic`, `Model`, `Storage`, and `UI` components.

The following class diagram shows the main classes involved in the feature:

<puml src="diagrams/PinClassDiagram.puml" alt="PinClassDiagram" width=75% />

[`PinCommandParser`](https://github.com/AY2526S2-CS2103-F08-4/tp/tree/master/src/main/java/seedu/address/logic/parser/PinCommandParser.java) and [`UnpinCommandParser`](https://github.com/AY2526S2-CS2103-F08-4/tp/tree/master/src/main/java/seedu/address/logic/parser/UnpinCommandParser.java) each parse the user's input and construct their respective commands with a `PersonInformation` object as the matching criteria. `PinCommand#execute(Model)` resolves the target by finding all matching contacts and applying shared disambiguation logic. After resolving to a single contact, it checks whether the contact is already pinned — throwing an error if so — before calling `model.pinPerson(...)`. `UnpinCommand#execute(Model)` follows the same flow, but checks whether the resolved contact is not pinned (throwing an error if already unpinned) before calling `model.unpinPerson(...)`. Both commands throw an error if no match or multiple matches remain after disambiguation.

[`AddressBook`](https://github.com/AY2526S2-CS2103-F08-4/tp/tree/master/src/main/java/seedu/address/model/AddressBook.java) maintains two lists: `persons` as the source of truth for all contacts, and `pinnedPersons` as an ordered list of pinned contacts. The insertion order of `pinnedPersons` defines the display order among pinned contacts. The UI reorders the displayed list by sorting against this pinned list, keeping pinned contacts at the top while preserving the relative order of unpinned contacts.

Pinned state is persisted in the JSON save file and reconstructed on load. The UI reflects pin state via a pin indicator shown on each pinned contact's card.

#### Usage scenario

The following sequence diagram shows how a `pin` command flows through the `Logic` component. The `unpin` command follows the same flow, except it checks whether the resolved contact is unpinned before calling `model.unpinPerson(...)`.

<puml src="diagrams/PinSequenceDiagram-Logic.puml" alt="PinSequenceDiagram-Logic" />

<box type="info" seamless>

**Note:** The lifeline for `PinCommand` should end at the destroy marker (X) but due to a limitation of PlantUML, the lifeline reaches the end of the diagram.

</box>

How the `pinPerson` call is handled inside the `Model` component is shown below:

<puml src="diagrams/PinSequenceDiagram-Model.puml" alt="PinSequenceDiagram-Model" />

The following activity diagram summarizes the pin/unpin command flow:

<puml src="diagrams/PinActivityDiagram.puml" width="1000" alt="PinActivityDiagram" />

#### Design considerations

**Aspect: How pinned contacts are represented**

* **Alternative 1 (current choice):** Maintain a separate `pinnedPersons` list in `AddressBook`.
  * Pros: Keeps pin ordering explicit and makes pinned-first sorting straightforward.
  * Pros: Allows pin state to be persisted independently from the main display order.
  * Cons: The model must preserve consistency between `persons` and `pinnedPersons`.

* **Alternative 2:** Store a pinned flag inside each `Person`.
  * Pros: Avoids maintaining a second list for pinned contacts.
  * Cons: Pin order would need extra bookkeeping, and editing a person would mix contact data with UI ordering state.
  * Cons: `isPinned` is not an intrinsic property of a person — it is a feature concern. Placing it on `Person` dilutes the class's responsibility by coupling contact data with application-level behaviour, violating the principle that a `Person` should model a real-world entity rather than a feature's state.

**Aspect: How the displayed list is reordered**

* **Alternative 1 (current choice):** Reorder the UI view using `sortedPersons` and `createPinnedComparator()`.
  * Pros: Keeps the base `persons` list unchanged while still presenting pinned contacts first.
  * Pros: Preserves the relative order of unpinned contacts.
  * Cons: The ordering logic is split between stored pin state and a comparator in `ModelManager`.

* **Alternative 2:** Physically move pinned contacts inside the main person list.
  * Pros: The displayed order would directly match the underlying storage order.
  * Cons: Reordering the main list couples display concerns to storage order and makes non-pin-related operations
    harder to reason about.
  * Cons: Unable to restore order if the pinned contact was unpinned.

### Contact Disambiguating feature

The contact disambiguation feature allows NAB to accurately resolve a target contact when multiple contacts share the same name. This feature is utilized by commands that require precise targeting (e.g., delete, edit) and spans the Logic and Model components.

Most person-targeting commands in NAB are intentionally stricter than `find`. For commands that act on an existing contact, such as `edit`, `delete`, `pin`, `unpin`, `tag`, and event-related commands, the target `n/NAME` is expected to match the contact's full name exactly before any optional disambiguation fields are applied. This reduces the risk of accidentally modifying or acting on the wrong contact. `find` is the main exception, as it is designed for retrieval rather than precise target selection.

#### Implementation

The core of this feature relies on the [`PersonInformation`](https://github.com/AY2526S2-CS2103-F08-4/tp/tree/master/src/main/java/seedu/address/model/person/PersonInformation.java) class. It encapsulates mandatory fields like `Name` and any optional fields (e.g., phone, email, address, or tags) that can be compared against existing contacts.

In NAB, these fields do not all play the same role. Phone number is the only contact field for which uniqueness is enforced. By contrast, email, address, and tags are treated as optional descriptive fields that may be shared across multiple contacts, and are therefore used only for filtering or disambiguation rather than identity enforcement.

The disambiguation logic is driven by [`CommandUtil`](https://github.com/AY2526S2-CS2103-F08-4/tp/tree/master/src/main/java/seedu/address/commons/util/CommandUtil.java) and [`ModelManager`](https://github.com/AY2526S2-CS2103-F08-4/tp/tree/master/src/main/java/seedu/address/model/ModelManager.java): <br>
* `CommandUtil#targetPerson(Model, PersonInformation)` acts as the orchestrator for the resolution process. This delegates the search and evaluation of `Person` to the methods below.<br><br>
* `ModelManager#findPersons(PersonInformation)` performs the filtering of the address book. It first applies a broad case-insensitive match on the name, followed by an "enriched search" that narrows down the contact candidates by strictly matching any optional fields present in the `PersonInformation` object.<br><br>
* `CommandUtil#targetPersonFromMatches(Model, List<Person>)` evaluates the resulting filtered list. It enforces that exactly one target person is isolated. <br>
  * If the search results in zero matches, it throws a `CommandException`. <br>
  * If it results in multiple ambiguous matches, it updates the UI to display only the conflicting contacts and throws a `CommandException` to prompt the user for better criteria.

#### Usage scenario

The following sequence diagram illustrates the functional path taken when a user executes a command that triggers the disambiguation process.

<puml src="diagrams/DisambiguationSequenceDiagram.puml"/>

<box type="info" seamless>

**Note:** Due to a PlantUML rendering limitation, the `:XYZCommand` lifeline is shown to prematurely end at the 1st alt path. The unified destroy marker (X) at the bottom represents the termination of the command's lifecycle for all three alternative paths.

</box>

The following activity diagram summarizes the command's match-resolution flow:

<puml src="diagrams/DisambiguationActivityDiagram.puml"/>

#### Design considerations

**Aspect: How search criteria are passed across architectural boundaries**

* **Alternative 1 (current choice):** Encapsulate all search criteria (Name, Phone, Email, Address, Tag) within a dedicated `PersonInformation` object.
    * Pros: Highly extensible. If a new optional field is added and is considered as a search criterion, the method signatures across `Logic` and `Model` remain unchanged. Only the `PersonInformation` wrapper is updated.
    * Pros: Resolves "Long Parameter List" code smell. Makes method signatures succinct.
    * Cons: Additional overhead from creating and maintaining an additional class.
<br><br>
* **Alternative 2:** Pass individual fields directly as arguments to the utility and model methods.
    * Pros: Does not require creating and maintaining a new class.
    * Cons: Creates method signatures with "Long Parameter List" code smell.
    * Cons: Tight coupling. Any changes to the search criteria (e.g., adding new search criteria,removing search criteria) will require modification to all the method signatures.

**Aspect: Which contact fields should be uniqueness-enforced**

* **Alternative 1 (current choice):** Enforce uniqueness only on phone numbers.
    * Pros: Keeps the identity rule simple and consistent, because phone number is a mandatory field while email and address are optional.
    * Pros: Avoids treating optional fields as identity fields, which would otherwise block contact creation based on information that may be absent or intentionally shared.
    * Pros: Still allows users to narrow down duplicate-name matches using email, address, and tags when needed.
    * Cons: Some real-world cases that look naturally unique, such as student email addresses, are not prevented from appearing more than once.

* **Alternative 2:** Enforce uniqueness on both phone numbers and email addresses.
    * Pros: Would catch more potential duplicate contacts in cases where email is present and expected to be unique.
    * Cons: Makes an optional field behave like an identity field, complicating the contact model and duplicate-handling rules.
    * Cons: Reduces flexibility for contacts that do not have an email recorded yet, or for workflows where email is used mainly as an auxiliary lookup/disambiguation field.

### Event Add feature

The event add feature allows users to create and link a new event to a contact. It spans the `Logic` and `Model` components, and reuses the contact disambiguation mechanism from `CommandUtil`.

NAB uses a shared-event model: events are stored globally and linked to one or more contacts. Two event additions are treated as the same event only when both the title and time range match exactly.

#### Implementation

[`AddEventParser`](https://github.com/AY2526S2-CS2103-F08-4/tp/tree/master/src/main/java/seedu/address/logic/parser/AddEventParser.java) parses the user's input and constructs an [`AddEventCommand`](https://github.com/AY2526S2-CS2103-F08-4/tp/tree/master/src/main/java/seedu/address/logic/commands/AddEventCommand.java) with an `Event` object and a `PersonInformation` target. An `Event` object is composed of the following attributes:

* [`Title`](https://github.com/AY2526S2-CS2103-F08-4/tp/tree/master/src/main/java/seedu/address/model/event/Title.java) — the name of the event (required).
* [`TimeRange`](https://github.com/AY2526S2-CS2103-F08-4/tp/tree/master/src/main/java/seedu/address/model/event/TimeRange.java) — the start and end date-time of the event in `yyyy-MM-dd HHmm` format (required). The end time must be after the start time.
* [`Description`](https://github.com/AY2526S2-CS2103-F08-4/tp/tree/master/src/main/java/seedu/address/model/event/Description.java) — a short description of the event (optional).

The required prefixes are `title/`, `start/`, and `end/` for the event, and `n/` for the target contact. The `desc/` prefix is optional, and additional person fields (`p/`, `e/`, `a/`, `t/`) may be supplied for disambiguation.

The `java.util.Optional<T>` class is utilised to encapsulate any optional attribute of the `Event` object (i.e., `Description`), allowing the absence of a value to be represented explicitly rather than using `null`.

`AddEventCommand#execute(Model)` begins with a prerequisite target resolution step, followed by a five-case resolution flow:

1. **Target resolution** — `CommandUtil#targetPerson(Model, PersonInformation)` is called first to resolve the target contact (see [Contact Disambiguating feature](#contact-disambiguating-feature)).
2. **Duplicate check** — If the resolved person is already linked to the same event, a `CommandException` is thrown.
3. **Shared event** — If the event already exists in the global [`UniqueEventList`](https://github.com/AY2526S2-CS2103-F08-4/tp/tree/master/src/main/java/seedu/address/model/event/UniqueEventList.java) (matched by `Title` and `TimeRange` via `Event#isSameEvent()`), `Model#linkPersonToEvent(Event)` is called instead of adding a new event. This increments the event's `numberOfPersonLinked` counter and returns the existing shared `Event` object to be linked to the person.
4. **Clash check** — If the event is new but its `TimeRange` overlaps with any existing event in `UniqueEventList`, a `CommandException` is thrown.
5. **New event** — If none of the above cases apply, `Model#addEvent(Event)` adds the event to `UniqueEventList`, and it is linked to the resolved person.

In all success cases, a new `Person` object is constructed with the event appended to its event list, replacing the old entry via `Model#setPerson(Person, Person)`. Finally, `Model#showEventsForPerson(Person)` updates the UI to display the linked person and their events.

#### Usage scenario

The following sequence diagram shows how an `event add` command flows through the `Logic` and `Model` components, illustrating all four resolution cases within `AddEventCommand#execute(Model)`.

<puml src="diagrams/AddEventSequenceDiagram.puml" alt="AddEventSequenceDiagram" />

<box type="info" seamless>

**Note:**
* The lifeline for `AddEventCommand` should end at the destroy marker (X) but due to a limitation of PlantUML, the lifeline reaches the end of the diagram.
* For clarity, the first-case duplicate-link validation `personToEdit.hasEvent(toAdd)` is omitted from this sequence diagram. If this check returns `true`, it means the target person is already linked to the event, and `AddEventCommand` immediately throws a `CommandException` without proceeding to the model-level event checks.

</box>

The following activity diagram summarizes the command's match-resolution flow:

<puml src="diagrams/AddEventActivityDiagram.puml" alt="AddEventActivityDiagram" />

#### Design considerations

**Aspect: How events are shared across multiple persons**

* **Alternative 1 (current choice):** Store events once in a global `UniqueEventList` and use a `numberOfPersonLinked` reference counter.
  * Pros: Avoids duplicating event data when multiple persons attend the same event.
  * Pros: Automatically removes the event from `UniqueEventList` when no persons are linked (counter reaches zero), keeping the list clean without explicit deletion logic.
  * Cons: `Event` carries mutable state (`numberOfPersonLinked`) despite the rest of the model favouring immutability.

* **Alternative 2:** Store an independent copy of the event inside each `Person`.
  * Pros: Keeps `Event` fully immutable and simplifies person-level operations.
  * Cons: The same event would be duplicated across persons, causing data inconsistency if the event details were to be updated.

**Aspect: How time clash detection is enforced**

* **Alternative 1 (current choice):** Detect clashes globally at the `AddressBook` level by checking all events in `UniqueEventList` via `TimeRange#isOverlapping()`.
  * Pros: Enforces a global no-overlap constraint — no two events in the address book can overlap in time, regardless of which person they belong to.
  * Cons: Prevents multiple persons from independently attending overlapping events, which may be overly restrictive.

* **Alternative 2:** Detect clashes only at the per-person level.
  * Pros: Allows different persons to have independently overlapping schedules.
  * Cons: It does not make sense for a user to schedule two different events in the same time slot, as that would imply being in two places at once. A global clash check better reflects real-world scheduling constraints.

### Tag handling

NAB treats [`Tag`](https://github.com/AY2526S2-CS2103-F08-4/tp/tree/master/src/main/java/seedu/address/model/tag/Tag.java) objects as case-insensitive labels. To keep the UI and stored data consistent, tags are normalized to lowercase when they are created or imported. This avoids confusing situations where logically identical tags such as `Friends` and `friends` appear with different casing.

NAB also enforces a hard limit of 30 characters for each tag. This is an intentional tradeoff: tags are meant to remain short, scannable labels rather than long free-form descriptions, but 30 characters still provides enough room for realistic module, project, and CCA-related labels.

The `tag` command is intentionally designed as a bulk-assignment operation that ensures the specified tags are present on the matched contacts. Its purpose is not to guarantee that every target contact is modified. If a contact already has a tag, NAB does not create a duplicate tag, and the command still succeeds because the desired postcondition has already been met.

--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, configuration, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [Configuration guide](Configuration.md)
* [DevOps guide](DevOps.md)

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

### Product scope

**Target user profile**:

* NUS student needing to track peer details across modules, tutorials and lab
* has a need to manage a significant number of contacts and multiple commitments
* can type fast
* is reasonably comfortable using CLI apps
* prefers desktop apps to other types
* prefers typing to mouse interactions

**Value proposition**: NAB enables students to quickly organize and find saved contacts across multiple modules
efficiently, while providing event management, tracking, and reminders.

### User stories

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (unlikely to have) - `*`

| Priority | As a …​             | I want to …​                                                                                      | So that I can…​                                                                             |
|----------|---------------------|---------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------|
| `* * *`  | student             | add/save a peer's contact easily                                                                  | easily contact them in the future                                                           |
| `* * *`  | student             | view a peer's contact details                                                                     | quickly access their information when I need to communicate or plan something with them     |
| `* * *`  | student             | delete a peer's contact                                                                           | remove old information                                                                      |
| `* * *`  | student             | search for a specific contact by their name                                                       | quickly find their details without scrolling through the whole list                         |
| `* * *`  | organized student   | categorise my peers according to context (e.g., modules, tutorial class, CCA, orientation group)  | search for contacts in a specific grouping                                                  |
| `* * *`  | organized student   | create an event for a commitment I have (e.g., module/project/CCA) linked to relevant contacts    | keep track of events and remind/contact involved individuals                                |
| `* * *`  | organized student   | delete an existing event for a commitment I have                                                  | remove any old or cancelled events so I don't mix up confirmed arrangements                 |
| `* * *`  | organized student   | view all events related to a specific contact                                                     | easily view my arranged commitments with the specified contact                              |
| `* * *`  | efficient student   | filter my peers by context                                                                        | quickly find someone from a certain grouping (e.g., tutorial class)                         |
| `* *`    | student             | update a peer's contact                                                                           | always keep my contact information up to date                                               |
| `* *`    | student             | avoid contact duplication when adding                                                             | ensure I don't get confused from duplicate contacts                                         |
| `* *`    | organized student   | update an existing event for a commitment I have                                                  | always keep events updated in the case the details are changed                              |
| `* *`    | organized student   | view all upcoming events related to my logical grouping (using tags) in one organised list        | coordinate my group's schedule and make sure everyone knows what's coming next              |
| `* *`    | proactive student   | record a teammate's stated unavailability                                                         | avoid proposing a meeting during times they already told me they are busy                   |
| `* *`    | efficient student   | import my existing contacts from a local file                                                     | easily load my existing contacts into NAB without adding them manually                     |
| `* *`    | efficient student   | group more than 1 contact at once                                                                 | efficiently group contacts all at once instead of 1 at a time                              |
| `* *`    | careful student     | export the application's local data (contacts, details, commitments, etc.)                        | backup my data and transfer it between devices                                              |
| `* *`    | forgetful student   | add/attach notes to a contact when saving them                                                    | remember important details about my peers without relying on memory                        |
| `* *`    | forgetful student   | edit notes that are attached to a contact                                                         | keep that contact's notes updated                                                           |
| `* *`    | forgetful student   | delete notes that are attached to a contact                                                       | remove old or unnecessary notes that may be misleading                                     |
| `* *`    | lazy student        | pin contacts to the top of the list                                                               | easily access favourited contacts                                                           |
| `* *`    | lazy student        | click on emails saved in my contacts                                                              | easily and quickly reach them via email without having to copy and paste their email address|
| `* *`    | new user            | view all available commands                                                                       | use the product immediately without having to consult an external guide                    |
| `* *`    | user                | know what went wrong with my command                                                              | rectify immediately and continue using the product                                         |
| `*`      | organized student   | view my upcoming contact-linked events in chronological order                                     | plan my weekly schedule and quickly see what's next while spotting potential overlaps       |
| `*`      | organized student   | archive contacts from the previous semester                                                       | keep my contact list clean after modules end                                               |
| `*`      | lazy student        | easily copy phone numbers of a chosen contact                                                     | reach them fast during coordinations                                                        |
| `*`      | seasoned student    | create aliases for commands                                                                       | customize my workflow and reduce repetitive command input                                   |
| `*`      | seasoned student    | reuse my previous commands                                                                        | repeat actions quickly without retyping                                                     |
| `*`      | seasoned student    | press Tab to autocomplete command keywords/prefixes                                               | type faster and make fewer syntax mistakes                                                  |

### Use cases (UC)

(For all use cases below, the **System** is the `NAB` and the **Actor** is the `user`, unless specified otherwise)

<box type="info" seamless>

**UC1 - Add Contact**


**Use case:** `UC1` - Add Contact<br>
**Guarantee:** New contact is successfully saved in the system.<br>
**MSS**
1. User requests to add a contact by providing the necessary contact information.
2. NAB saves the contact into the system.
3. NAB updates the display to show the contact list and clears the event panel.
<br> *Use case ends.*

**Extensions**

* 1a. NAB detects that a contact with the provided phone number exists.
  * 1a1. NAB returns an error message stating that a contact with this phone number already exists.
<br> *Use case ends.*<br><br>
* 1b. NAB detects invalid or missing contact information.
  * 1b1. NAB displays an error message specifying the correct command format.
<br> *Use case ends.*<br><br>
* 1c. NAB is unable to read or copy the provided photo file.
  * 1c1. NAB returns an error message indicating the photo save failure.
    <br> *Use case ends.*<br><br>
</box>

<box type="info" seamless>

**UC2 - Find Contact**


**Use case:** `UC2` - Find Contact<br>
**MSS**
1. User requests to find a contact by providing a name and any optional fields.
2. NAB searches for all contacts matching the criteria.
3. NAB displays a list of contacts matching the criteria specified.
   <br> *Use case ends.*

**Extensions**

* 1a. NAB detects invalid characters or formats in the provided fields.
    * 1a1. NAB displays an error message specifying the correct command format.
      <br> *Use case ends.*<br><br>
* 2a. NAB finds exactly one contact matching the criteria.
    * 2a1. NAB displays the single contact.
    * 2a2. NAB automatically retrieves and displays the event list associated with that specific contact.
      <br> *Use case ends.*<br><br>
* 2b. NAB finds no available contacts matching the criteria.
    * 2b1. NAB displays an empty contact list and informs the user that 0 matches were found.
      <br> *Use case ends.*
</box>

<box type="info" seamless>

**UC3 - Delete Contact**


**Use case:** `UC3` - Delete Contact<br>
**MSS**
1. User requests to delete a specific contact by providing their name and any optional fields.
2. NAB identifies the specific contact matching the fields.
3. NAB deletes the contact.
4. NAB updates the display to show the remaining contacts and clears the event panel.
   <br> *Use case ends.*

**Extensions**

* 1a. NAB detects invalid characters or formats in the provided fields.
    * 1a1. NAB displays an error message specifying the correct command format.
      <br> *Use case ends.*<br><br>
* 2a. NAB finds multiple contacts that match the provided fields.
    * 2a1. NAB displays the conflicting contacts and returns an error informing the user to provide more specific details.
      <br> *Use case ends.*<br><br>
* 2b. NAB finds no available contacts that matches the fields provided.
    * 2b1. NAB informs the user that no matches were found.
      <br> *Use case ends.*
</box>

<box type="info" seamless>

**UC4 - Adding an Event for a Contact**


**Use case:** `UC4` - Adding an Event for a Contact<br>
**Preconditions:** Contact that the event will be tagged to already exists in NAB.<br>
**Guarantees:** Event is added to the system and is tagged to the specified contact.<br>
**MSS**
1. User requests to add an event to a contact by providing the event details and the contact's identifying details.
2. NAB identifies the specific contact.
3. NAB saves the event and links it to the contact.
4. NAB displays the updated event list for the contact.
   <br> *Use case ends.*

**Extensions**

* 1a. NAB detects invalid characters, formats, or missing required fields.
  * 1a1. NAB displays an error message specifying the correct command format.
    <br> *Use case ends.*<br><br>
* 2a. NAB finds no contacts matching the provided details.
  * 2a1. NAB displays an error message indicating the contact does not exist.
    <br> *Use case ends.*<br><br>
* 2b. NAB finds multiple contacts that match the provided details.
  * 2b1. NAB displays the conflicting contacts and prompts for more specific details.
    <br> *Use case ends.*<br><br>
* 3a. NAB detects that the contact is already linked to this exact event.
  * 3a1. NAB displays an error message indicating the duplication.
    <br> *Use case ends.*<br><br>
* 3b. NAB detects that the event overlaps with an existing event in the calendar.
  * 3b1. NAB displays an error message listing the clashing events.
    <br> *Use case ends.*
</box>

<box type="info" seamless>

**UC5 - View Event**


**Use case:** `UC5` - View Event<br>
**MSS**
1. User requests to view the event list for a specific contact by providing their name and any optional fields.
2. NAB identifies the specific contact.
3. NAB retrieves the event list associated with the contact.
4. NAB displays the formatted event list to the user.
   <br> *Use case ends.*

**Extensions**

* 1a. NAB detects invalid characters or formats in the provided fields.
    * 1a1. NAB displays an error message specifying the correct command format.
    <br> *Use case ends.*<br><br>
* 2a. NAB is unable to find a contact matching the provided criteria.
    * 2a1. NAB informs the user that contact does not exist.
    <br> *Use case ends.*<br><br>
* 2b. NAB finds multiple contacts that match the provided criteria.
  * 2b1. NAB displays the conflicting contacts and returns an error prompting the user to provide more specific arguments.
    <br> *Use case ends.*<br><br>
* 3a. NAB finds no events associated with the contact.
    * 3a1. NAB informs the user that there are 0 events associated with the contact.
    <br> *Use case ends.*
</box>

<box type="info" seamless>

**UC6 - Filter Contact by Tag**


**Use case:** `UC6` - Filter Contact by Tag<br>
**MSS**
1. User requests to filter contacts by providing one or more tags.
2. NAB filters contact(s) with the tags provided.
3. NAB displays the list of contacts matching the tags and clears the event panel.
   <br> *Use case ends.*

**Extensions**

* 1a. NAB detects invalid characters or formats in the provided fields.
  * 1a1. NAB displays an error message specifying the correct command format.
    <br> *Use case ends.*<br><br>
* 2a. No contacts in the system contain any of the provided tags.
  * 2a1. NAB displays an empty list and a message indicating no contacts were found with those tags.
    <br> *Use case ends.*
</box>

<box type="info" seamless>

**UC7 - Export Contacts**


**Use case:** `UC7` - Export Contacts<br>
**MSS**
1. User requests to export contacts and provides an export type and a filename prefix.
2. NAB determines the set of contacts to export according to the chosen export type.
3. NAB writes the export files.
4. NAB informs the user of a successful export.
   <br> *Use case ends.*

**Extensions**

* 1a. The provided export arguments are invalid.
    * 1a1. NAB informs the user of a command format error and aborts the export.
      <br> *Use case ends.*
* 2a. The selected contact set is empty.
    * 2a1. NAB informs the user that there are no contacts to export and aborts the export.
      <br> *Use case ends.*
* 3a. NAB cannot write the files to the destination.
    * 3a1. NAB informs the user about the write error and aborts the export.
      <br> *Use case ends.*
</box>

<box type="info" seamless>

**UC8 - Import Contacts**


**Use case:** `UC8` - Import Contacts<br>
**Preconditions:** The user supplies a filename prefix identifying the expected import files which exist.<br>
**MSS**
1. User requests to import contacts specifying an import mode and a filename prefix.
2. NAB reads the required import files and parses their contents.
3. NAB adds or replaces contacts according to the selected import mode.
4. NAB informs the user as to how many contacts were added and how many were skipped.
   <br> *Use case ends.*

**Extensions**

* 1a. The provided import arguments are invalid.
    * 1a1. NAB informs the user of a command format error and aborts the import.
      <br> *Use case ends.*
* 2a. NAB cannot read the required files.
    * 2a1. NAB informs the user about the read error and aborts the import.
      <br> *Use case ends.*
* 3a. A data row is malformed or invalid.
    * 3a1. NAB skips the row, records the reason, and continues importing the remaining rows.
      <br> *Use case continues from step 4.*
* 3b. An existing contact is found when importing.
    * 3b1. NAB skips the duplicate row and continues importing the remaining rows.
      <br> *Use case continues from step 4.*
</box>

### Non-Functional Requirements

###### Portability:
1.  Should work on any _mainstream OS_ as long as it has Java `17` or above installed.
2.  Should be packaged as a single JAR file not exceeding a size of 100MB.
3.  Should be fully functional offline and must not depend on any remote server.

###### Scalability:
4.  NAB is intended for single-user use only and does not support multi-user scenarios.

###### Usability:
5.  GUI should work well (i.e. should not cause any resolution-related inconveniences to the user), for standard screen resolutions 1920x1080 and higher, and for screens scaled by 100% to 125%.
6.  GUI should remain usable (i.e. all functions can be used even if the user experience is not optimal) for resolutions 1280x720 and higher, and for screens scaled by 150%.

###### Performance:
7. NAB should be able to hold up to 1000 persons without a noticeable sluggishness in performance for typical usage.

###### Data Persistence:
8.  The data file should be stored locally in a human-editable text file, allowing advanced users to manipulate data directly by editing the file.

###### Data Synchronization:
9.  All modifications to data should be propagated and reflected in local data storage within 3 seconds.

###### Stability:
10.  All exceptions and errors should be handled gracefully by the application (i.e. there should not be any application crashes).

###### Fault Tolerance:
11.  Should handle corrupted storage files gracefully by loading data when recoverable storage issues can be resolved, or otherwise notifying the user of the loading error and starting with an empty address book.

###### Efficiency:
12.  A user with above-average typing speed for regular English text (i.e. not code, not system admin commands) should be able to accomplish most of the tasks faster using commands than using the mouse.

###### Response time:
13.  Should not take more than 1 second to process commands and load data for up to 1000 persons and 30 tags cumulative in storage.

###### Data Integrity:
14.  When a contact is deleted, all events linked to that contact should also be removed to prevent orphaned data.

*{More to be added}*

### Glossary

* **NAB**: NUS Address Book, the name of our desktop application.
* **Contacts**: A person (fellow student, friend, classmate, schoolmate) that a user has saved in NAB. A contact is typically (but not necessarily) associated with an event.
* **Tag**: A logical label attached to a contact for association-oriented lookups and logical groupings for easier management.
* **Event**: A contact-linked commitment or arrangement that the user has with one or more contacts (e.g. a project meeting, training session).
* **Unavailability**: A special type of event in NAB to indicate that a contact is unavailable during a time period, used to avoid scheduling conflicts.
* **CLI**: Command Line Interface is a text-based user interface that primarily uses commands and typed-inputs for user interaction (with the application), as opposed to GUI.
* **GUI**: Graphic User Interface is a graphics-based user interface that primarily uses mouse-clicks for user interaction (with the application), as opposed to CLI.
* **Alias**: An alternate name a user can assign to a command that allows easier command execution while maintaining command functionality.
* **CSV**: Comma Separated Values, a plain-text file format used to store tabular data. Specifically, this is to store the application data including contact names, phone numbers, tags, etc.
* **Mainstream OS**: Windows, Linux, Unix, macOS
* **JavaScript Object Notation (JSON)**: A file format used to store and send data in a human-readable format.
* **Java Archive (JAR)**: A file format used to compress multiple Java-related files into a single file for ease of distribution, deployment and execution.
* **Prefix**: A keyword that is used to identify parameters in a command (e.g. n/, p/, e/, a/).

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Effort**

##### Why NAB required more effort than AB3

AB3 mainly manages one entity type and many operations can rely on direct list selection. NAB extends that baseline into a product with **two closely related entity types**, `Person` and `Event`, while still remaining a CLI-first brownfield evolution of AB3. This increased the effort significantly because many features now involve not just storing data, but preserving relationships and consistency between contacts, events, UI views, and storage.

##### Main difficulty: modelling events realistically

The hardest design problem was the **event model**. In NAB, an event is not just a note attached to one contact, and it is not a free-standing calendar entry either. It represents a commitment that implicitly involves the NAB user together with one or more saved contacts. Because the user is not modelled as an explicit in-app entity, the event could not be implemented as a straightforward association class.

We therefore designed events as **global shared entities** stored in a `UniqueEventList`, while each `Person` keeps references to those events. This decision increased the implementation effort because it required custom handling for:

* linking multiple contacts to the same canonical event
* distinguishing duplicate event linkage from true event clashes
* enforcing a global no-overlap rule
* tracking participant counts and cleaning up orphaned events
* reconstructing shared event references correctly during storage loading

##### Realistic command targeting made the logic harder

Another major difficulty came from replacing AB3's index-based targeting with **name-based resolution and disambiguation**. For a realistic address book, users remember names, not list indices. NAB also allows duplicate names, which meant the team had to build a reusable disambiguation mechanism rather than depending on unique-name assumptions or index selection.

This affected a large number of commands, including `edit`, `delete`, `pin`, `unpin`, `tag`, `event add`, `event view`, and `event delete`. Each of these commands had to support:

* matching by name first
* narrowing by optional fields such as phone, email, address, and tags
* surfacing ambiguous results safely instead of acting on the wrong contact

This added substantial parser, model, and testing complexity compared to AB3.

##### Data handling and storage complexity

NAB also went beyond AB3 in its storage and file-handling requirements. Besides JSON persistence, the product supports **CSV import/export** with both `add` and `overwrite` modes. This created many extra cases to design and test, such as:

* reconstructing persons and events from two related CSV files
* merging imported persons with already existing canonical events
* preserving pinned state through import/export
* skipping malformed or duplicate rows safely
* ensuring that cross-entity links remain valid after import

Profile photo support introduced another layer of effort in file management: path validation, copying external images into managed storage, avoiding invalid reuse, and deleting photos safely only when no other contact still depends on them.

##### Additional implementation effort beyond the core model

On top of the major modelling work, NAB also introduced several non-trivial user-facing features that added meaningful implementation and testing effort:

* pinned contacts with persistent ordering and separate pinned-state storage
* command history implemented at the UI layer for CLI-heavy usage
* copy-to-clipboard support from contact cards
* event-aware list views that keep the person and event panels synchronized

Each feature is individually smaller than the event/disambiguation work, but together they significantly increased the integration surface of the product.

##### Reuse and its impact on effort

NAB reused a meaningful portion of AB3's infrastructure, including the high-level architecture, JavaFX scaffold, command/parser structure, base contact management flow, and JSON framework. This definitely saved effort, well above 5%, because the team did not need to build the application skeleton from scratch.

However, the reuse mainly provided a platform rather than a shortcut for the most difficult work. The most effort-intensive parts of NAB were custom additions built on top of that reused base. This includes the global event model, name-based disambiguation, pinned-state handling, CSV import/export logic, and photo/file lifecycle management.

##### Key achievement

The main achievement of the project was turning AB3 into a more realistic student-facing contact manager while still keeping it usable as a CLI-first product. In particular, the project successfully combined:

* contact management with duplicate-name support
* a shared event model with cross-entity consistency
* realistic import/export and file-handling features
* several usability improvements beyond the original AB3 baseline

Overall, the project required substantially more effort than AB3 because it introduced both deeper domain modelling and more difficult consistency problems, while still operating within the constraints of a portable, single-user, human-editable desktop application.

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<box type="info" seamless>

**Note:** These steps assume the tester starts NAB from a fresh home folder so that the default sample data is loaded.
If the app state has already been modified, restart with a new empty folder before following the sequence below.

</box>

### Suggested route for testing

1. Start with the built-in sample contacts and verify basic launch, shutdown, and persistence behaviour.
2. Use the sample data first for non-destructive features such as `find`, `filter`, `pin`, `unpin`, `event view`, command history, and copy-to-clipboard.
3. Create the additional contacts in the [Reusable test inputs](#reusable-test-inputs) section. These are intended to help test duplicate-name disambiguation, editing, tagging, deletion, profile photos, and event management.
4. After creating those contacts, test commands that require a unique target by intentionally trying both ambiguous and disambiguated variants.
5. Export the current data, then test `clear` and `import` using the exported files.

### Sample data reference

The default sample data includes these useful contacts:

* `Alex Yeoh` tagged with `friends`
* `Bernice Yu` tagged with `colleagues`, `friends`
* `Charlotte Oliveiro` tagged with `neighbours`
* `David Li` tagged with `family`
* `Irfan Ibrahim` tagged with `classmates`
* `Roy Balakrishnan` tagged with `colleagues`

The sample data also contains existing events, so `event view n/Alex Yeoh` and `event view n/David Li` are good starting points for event-related testing.

### Launch, shutdown, and persistence

| Scenario | Command | Manual action | Expected |
| --- | --- | --- | --- |
| Initial launch | `java -jar NAB.jar` | Copy `NAB.jar` into an empty folder and run the command there | The GUI opens with sample contacts and sample events. |
| Saving window preferences | - | Resize and move the window, close the app, then relaunch the jar | The window size and position are retained. |
| Help | `help` | - | NAB shows the help message. |
| Exit | `exit` | - | The application closes cleanly. |

### Quick checks on sample data

| Feature | Command | Manual action | Expected |
| --- | --- | --- | --- |
| Find unique contact | `find n/Alex Yeoh` | - | Only `Alex Yeoh` is shown. His linked events should also be shown because the match is unique. |
| Filter by tag | `filter t/friends` | - | `Alex Yeoh` and `Bernice Yu` are shown. |
| Filter by multiple tags | `filter t/colleagues, family` | - | Contacts tagged with either `colleagues` or `family` are shown. |
| Reset person list | `list` | - | The full contact list is shown again. |
| Pin contact | `pin n/Bernice Yu` | - | `Bernice Yu` becomes pinned and appears above unpinned contacts in the full list. |
| Filter preserves pinned-first ordering | `pin n/Bernice Yu` then `filter t/friends` | Run the commands in sequence | `Alex Yeoh` and `Bernice Yu` are shown, with pinned `Bernice Yu` above `Alex Yeoh`. |
| Pin already pinned contact | `pin n/Bernice Yu` | - | Error indicating that the contact is already pinned. |
| Unpin contact | `unpin n/Bernice Yu` | - | `Bernice Yu` is unpinned. |
| Unpin already unpinned contact | `unpin n/Bernice Yu` | - | Error indicating that the contact is already unpinned. |
| View events for sample contact | `event view n/Alex Yeoh` | - | Alex's events are shown in the event list panel. |
| View events for missing contact | `event view n/Person Not In List` | - | Error indicating that no such contact can be found. |
| Command history | `list` then `find n/Alex Yeoh` then `filter t/friends` | After entering the commands, press the Up and Down arrow keys in the command box | NAB navigates previously entered commands for the current session only. |
| Clipboard copy | - | Double-click any contact card in the person list, then paste elsewhere | The displayed contact details are copied to the system clipboard line-by-line. |

### Add command checks

| Feature | Command | Manual action | Expected |
| --- | --- | --- | --- |
| Add minimal contact | `add n/Test User p/84561234` | - | A new contact named `Test User` is added successfully. |
| Add contact with profile photo | `add n/Photo User p/85672345 pfp/manual_photo.jpg` | Place a valid `.png`, `.jpg`, or `.jpeg` file named `manual_photo.jpg` beside the jar before running the command | The contact is added and the image is copied into NAB's managed image storage. |
| Reject duplicate phone on add | `add n/Duplicate Phone p/84561234` | - | Error indicating that another contact already uses this phone number. |
| Reject missing photo file | `add n/Missing Photo p/86783456 pfp/not_found.jpg` | Ensure `not_found.jpg` does not exist at the given path | Error indicating that the image file cannot be found/read. |

### Reusable test inputs

Use the following commands in order to create a controlled setup for the remaining feature tests:

```text
list
add n/John Tan p/81234567 e/johntan@example.com a/PGP House 12 t/cs2103 t/project
add n/John Tan p/82345678 e/johntan2@example.com a/UTown Residence 3 t/cs2105
add n/Mary Lim p/83456789 e/marylim@example.com a/College Avenue West 15 t/orbital
```

Expected:

* Two contacts named `John Tan` now exist, which is useful for disambiguation testing.
* `Mary Lim` can be used for multi-person tagging and import/export checks.

### Disambiguation, edit, tag, and delete

| Feature | Command | Manual action | Expected |
| --- | --- | --- | --- |
| Find duplicate-name contacts | `find n/John Tan` | - | Both `John Tan` contacts are shown. |
| Ambiguous pin | `pin n/John Tan` | - | Error `Multiple matches identified! Please provide more arguments.` and the conflicting contacts are shown. |
| Disambiguated pin | `pin n/John Tan p/81234567` | - | The correct `John Tan` is pinned. |
| Disambiguated unpin | `unpin n/John Tan p/81234567` | - | The same contact is unpinned. |
| Ambiguous edit | `edit n/John Tan >> e/john.updated@example.com` | - | Ambiguity error because more than one `John Tan` exists. |
| Disambiguated edit | `edit n/John Tan p/81234567 >> e/john.updated@example.com t/teammate` | - | The contact with phone `81234567` is updated. |
| Clear tags with edit | `edit n/John Tan p/81234567 >> t/` | - | All tags are cleared for that contact. |
| Profile photo support | `edit n/John Tan p/81234567 >> pfp/profile.png` | Place any `.png`, `.jpg`, or `.jpeg` file outside NAB's `data/images/` folder, for example `profile.png` beside the jar, before running the command | The selected contact's photo is updated and the image is copied into NAB's managed image storage. |
| Tag multiple contacts | `tag label/cs2103-team n/Alex Yeoh n/Mary Lim` | - | Both contacts receive the `cs2103-team` tag. |
| Tag multiple contacts with multiple labels | `tag label/demo label/testing n/John Tan p/81234567 n/Mary Lim` | - | Both contacts receive both tags. |
| Ambiguous tag target | `tag label/demo n/John Tan` | - | Ambiguity error because the target name is not unique. |
| Ambiguous delete | `delete n/John Tan` | - | Ambiguity error because both duplicate contacts match. |

### Event add, event view, clash handling, and event delete

| Feature | Command | Manual action | Expected |
| --- | --- | --- | --- |
| Ambiguous event view | `event view n/John Tan` | - | Ambiguity error because both duplicate contacts match. |
| Disambiguated event view | `event view n/John Tan p/81234567` | - | Events linked to the contact with phone `81234567` are shown. |
| Ambiguous event add | `event add title/John Sync start/2026-10-12 1000 end/2026-10-12 1100 n/John Tan` | - | Ambiguity error because both duplicate contacts match. |
| Add new event | `event add title/NAB Demo start/2026-10-10 1000 end/2026-10-10 1130 n/John Tan p/81234567` | - | The event is created and linked to that contact. |
| Add new event with description | `event add title/NAB Debrief desc/Post demo review start/2026-10-11 1400 end/2026-10-11 1500 n/John Tan p/81234567` | - | The event is created with the description and linked to that contact. |
| View newly added event | `event view n/John Tan p/81234567` | - | The event list shows the newly added event. |
| Reuse existing global event | `event add title/CS2103 Meeting start/2026-08-19 1400 end/2026-08-19 1530 n/Roy Balakrishnan` | - | The existing sample event is linked to Roy instead of creating a second duplicate event. |
| Reject clashing event | `event add title/Clash Test start/2026-08-19 1430 end/2026-08-19 1500 n/Mary Lim` | - | Error indicating that the event clashes with an existing event in the calendar. |
| Reject incomplete event command | `event add title/NAB Demo n/John Tan p/81234567` | - | Format error because `start/` and `end/` are missing. |
| Ambiguous event delete | `event delete start/2026-10-10 1000 n/John Tan` | - | Ambiguity error because both duplicate contacts match. |
| Delete linked event | `event delete start/2026-10-10 1000 n/John Tan p/81234567` | - | The `NAB Demo` event is unlinked from that contact. |
| Delete missing event link | `event delete start/2026-10-10 1000 n/John Tan p/81234567` | - | Error indicating that the contact no longer has that event. |

### Cleanup after disambiguation tests

| Feature | Command | Manual action | Expected |
| --- | --- | --- | --- |
| Disambiguated delete | `delete n/John Tan p/82345678` | - | The second `John Tan` is deleted. |

### Export and import

| Feature | Command | Manual action | Expected |
| --- | --- | --- | --- |
| Export current filtered list | `filter t/friends` then `export t/current f/manual_test_subset` | Run the commands in sequence | NAB creates `manual_test_subset_persons.csv` and `manual_test_subset_events.csv` in the same folder as the save file. |
| Export full data | `list` then `export t/all f/manual_test_full` | Run the commands in sequence | NAB creates `manual_test_full_persons.csv` and `manual_test_full_events.csv`. |
| Reject duplicate export type prefix | `export t/all t/current f/manual_test_full` | - | Error indicating that the export type prefix is specified more than once. |
| Clear data | `clear` | - | All contacts are removed from the app. |
| Import exported full data | `import t/add f/manual_test_full` | - | The exported contacts are loaded back into NAB. |
| Reject duplicate import filename prefix | `import t/add f/manual_test_full f/manual_test_subset` | - | Error indicating that the filename prefix is specified more than once. |
| Overwrite with exported subset | `import t/overwrite f/manual_test_subset` | - | NAB replaces the current data with only the subset exported earlier. |
| Invalid import path | `import t/add f/file_that_does_not_exist` | - | Error indicating that NAB cannot read the required CSV files. |
