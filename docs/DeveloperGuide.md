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

**`Main`** (consisting of classes [`Main`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `delete 1`.

<puml src="diagrams/ArchitectureSequenceDiagram.puml" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* implements its functionality using a concrete `{Component Name}Manager` class (which follows the corresponding API `interface` mentioned in the previous point.

For example, the `Logic` component defines its API in the `Logic.java` interface and implements its functionality using the `LogicManager.java` class which follows the `Logic` interface. Other components interact with a given component through its interface rather than the concrete class (reason: to prevent outside component's being coupled to the implementation of a component), as illustrated in the (partial) class diagram below.

<puml src="diagrams/ComponentManagers.puml" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/Ui.java)

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` that is made up of parts e.g.`CommandBox`, `ResultDisplay`, `PersonListPanel`, `StatusBarFooter` etc. All these, including the `MainWindow`, inherit from the abstract `UiPart` class which captures the commonalities between classes that represent parts of the visible GUI.

The `UI` component uses the JavaFx UI framework. The layout of these UI parts are defined in matching `.fxml` files that are in the `src/main/resources/view` folder. For example, the layout of the [`MainWindow`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/MainWindow.java) is specified in [`MainWindow.fxml`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/resources/view/MainWindow.fxml)

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component, as it displays `Person` object residing in the `Model`.

### Logic component

**API** : [`Logic.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<puml src="diagrams/LogicClassDiagram.puml" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete 1")` API call as an example.

<puml src="diagrams/DeleteSequenceDiagram.puml" alt="Interactions Inside the Logic Component for the `delete 1` Command" />

<box type="info" seamless>

**Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X) but due to a limitation of PlantUML, the lifeline continues till the end of diagram.
</box>

How the `Logic` component works:

1. When `Logic` is called upon to execute a command, it is passed to an `AddressBookParser` object which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a person).<br>
   Note that although this is shown as a single step in the diagram above (for simplicity), in the code it can take several interactions (between the command object and the `Model`) to achieve.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned back from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name e.g., `AddCommandParser`) which uses the other classes shown above to parse the user command and create a `XYZCommand` object (e.g., `AddCommand`) which the `AddressBookParser` returns back as a `Command` object.
* All `XYZCommandParser` classes (e.g., `AddCommandParser`, `DeleteCommandParser`, ...) inherit from the `Parser` interface so that they can be treated similarly where possible e.g, during testing.

### Model component
**API** : [`Model.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="450" />


The `Model` component,

* stores the address book data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* stores the currently 'selected' `Person` objects (e.g., results of a search query) as a separate _filtered_ list which is exposed to outsiders as an unmodifiable `ObservableList<Person>` that can be 'observed' e.g. the UI can be bound to this list so that the UI automatically updates when the data in the list change.
* stores a `UserPref` object that represents the user’s preferences. This is exposed to the outside as a `ReadOnlyUserPref` objects.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)

<box type="info" seamless>

**Note:** An alternative (arguably, a more OOP) model is given below. It has a `Tag` list in the `AddressBook`, which `Person` references. This allows `AddressBook` to only require one `Tag` object per unique tag, instead of each `Person` needing their own `Tag` objects.<br>

<puml src="diagrams/BetterModelClassDiagram.puml" width="450" />

</box>


### Storage component

**API** : [`Storage.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="550" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* inherits from both `AddressBookStorage` and `UserPrefStorage`, which means it can be treated as either one (if only the functionality of only one is needed).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

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

Step 2. The user executes `delete 5` command to delete the 5th person in the address book. The `delete` command calls `Model#commitAddressBook()`, causing the modified state of the address book after the `delete 5` command executes to be saved in the `addressBookStateList`, and the `currentStatePointer` is shifted to the newly inserted address book state.

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
  * Pros: Will use less memory (e.g. for `delete`, just save the person being deleted).
  * Cons: We must ensure that the implementation of each individual command are correct.

_{more aspects and alternatives to be added}_

### \[Proposed\] Data archiving

_{Explain here how the data archiving feature will be implemented}_


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
| `* * *`  | organized student   | categorise my peers according to context (e.g. modules, tutorial class, CCA, orientation group)  | search for contacts in a specific grouping                                                  |
| `* * *`  | organized student   | create an event for a commitment I have (e.g. module/project/CCA) linked to relevant contacts    | keep track of events and remind/contact involved individuals                                |
| `* * *`  | organized student   | delete an existing event for a commitment I have                                                  | remove any old or cancelled events so I don't mix up confirmed arrangements                 |
| `* * *`  | organized student   | view all events related to a specific contact                                                     | easily view my arranged commitments with the specified contact                              |
| `* * *`  | efficient student   | filter my peers by context                                                                        | quickly find someone from a certain grouping (e.g. tutorial class)                         |
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

<panel header="**UC1 - Add Contact**" type="light">

**Use case:** `UC1` - Add Contact<br>
**Guarantee:** New contact is successfully saved in the system.<br>
**MSS**
1. User requests to add a contact.
2. User enters the necessary contact information.
3. NAB saves the contact into the contact list/database.
<br> *Use case ends.*

**Extensions**

* 2a. NAB detects an existing contact number entered.
  * 2a1. NAB requests for a different contact number.
  * 2a2. User enters a new contact number.
  * Steps 2a1 - 2a2 are repeated until a unique contact number is entered.
<br> *Use case continues from step 3.*<br><br>
* 2b. NAB detects invalid contact information.
  * 2b1. NAB requests for the correct information.
  * 2b2. User enters the correct contact information.
  * Steps 2b1 - 2b2 are repeated until all contact information are valid entries.
  <br> *Use case continues from step 3.*
</panel>

<panel header="**UC2 - Find Contact**" type="light">

**Use case:** `UC2` - Find Contact<br>
**MSS**
1. User requests to find a contact.
2. User provides a keyword.
3. NAB checks whether the entered keyword is valid.
4. NAB identifies the specific contact matching the name.
5. NAB displays a list of contacts matching the user’s keyword.
   <br> *Use case ends.*

**Extensions**

* 3a. NAB detects invalid characters in the provided keyword
    * 3a1. NAB returns an error message
      <br> *Use case ends.*<br><br>
* 4a. NAB detects finds multiple possible contacts matching the keyword provided.
    * 4a1. User provides more information to enrich the search.
      <br> *Use case resumes from step 3.*<br><br>
* 4b. NAB finds no available contacts matching the keyword provided.
    * 4b1. NAB informs the user that no matches were found.
      <br> *Use case ends.*
</panel>

<panel header="**UC3 - Delete Contact**" type="light">

**Use case:** `UC3` - Delete Contact<br>
**MSS**
1. User requests to delete a specific contact by providing their name.
2. NAB checks whether the provided name is valid.
3. NAB identifies the specific contact matching the name.
4. NAB deletes the contact.
   <br> *Use case ends.*

**Extensions**

* 2a. NAB detects invalid characters in the provided name.
    * 2a1. NAB returns an error message.
      <br> *Use case ends.*<br><br>
* 3a. NAB finds multiple contacts that match the name provided.
    * 3a1. User provides more information to enrich the search.
      <br> *Use case resumes from step 2.*<br><br>
* 3b. NAB finds no available contacts that match the name provided.
    * 3b1. NAB informs the user that no matches were found.
      <br> *Use case ends.*
</panel>

<panel header="**UC4 - Adding an Event for a Contact**" type="light">

**Use case:** `UC4` - Adding an Event for a Contact<br>
**Preconditions:** Contact that the event will be tagged to already exists in NAB.<br>
**Guarantees:** Event is added to the system and is tagged to the specified contact.<br>
**MSS**
1. User requests to create a new event for a specific contact.
2. User enters the necessary event information and the information of the contact to be tagged to.
3. NAB saves the event into the event list/database.
   <br> *Use case ends.*

**Extensions**

* 2a. NAB finds a duplicate event that has already been registered to the contact.
    * 2a1. NAB rejects the event from being added.
    <br> *Use case ends.*<br><br>
* 2b. NAB is unable to find the specified contact.
    * 2b1. NAB informs the user that the contact does not exist.
    <br> *Use case ends.*
</panel>

<panel header="**UC5 - View Event**" type="light">

**Use case:** `UC5` - View Event<br>
**MSS**
1. User requests to view the event list for a specific contact by providing their name.
2. NAB checks whether the provided name is valid.
3. NAB identifies the specific contact.
4. NAB retrieves the event list associated with the contact.
5. NAB displays the formatted event list to the user.
   <br> *Use case ends.*

**Extensions**

* 1a. User requests to view events without specifying a contact name (i.e. view own events).
    * 1a1. NAB returns the user’s own event list.
    <br> *Use case resumes from step 5.*<br><br>
* 2a. NAB detects invalid characters in the provided name.
    * 2b1. NAB returns an error message.
    <br> *Use case ends.*<br><br>
* 3a. NAB is unable to find a contact matching the provided name.
    * 3a1. NAB informs the user that contact does not exist.
    <br> *Use case ends.*<br><br>
* 4a. NAB finds no events associated with the contact.
    * 4a1. NAB informs the user that there are no events associated with the contact.
    <br> *Use case ends.*
</panel>

<panel header="**UC6 - Filter Contact by Tag**" type="light">

**Use case:** `UC6` - Filter Contact by Tag<br>
**MSS**
1. User requests to find contacts with specific tag(s).
2. User enters the necessary tag(s).
3. NAB checks whether the provided tag(s) are valid.
4. NAB retrieves a list of contacts matching the tag(s).
5. NAB displays the list of contacts to the user.
   <br> *Use case ends.*

**Extensions**

* 3a. NAB detects invalid characters in the provided tag
    * 3a1. NAB returns an error message.
    <br> *Use case ends.*<br><br>
* 4a. NAB finds no available contacts matching the tag(s) provided.
    * 4a1. NAB informs the user that no matches were found.
    <br> *Use case ends.*
</panel>

<panel header="**UC7 - Export Contacts**" type="light">

**Use case:** `UC7` - Export Contacts<br>
**MSS**
1. User requests to export all contacts out of NAB.
2. NAB saves a formatted file containing the list of contacts to a file directory.
   <br> *Use case ends.*

**Extensions**

* 2a. NAB is unable to save the file to the user’s file directory.
    * 2a1. NAB informs the user of the error.
    <br> *Use case ends.*
</panel>

<panel header="**UC8 - Import Contacts**" type="light">

**Use case:** `UC8` - Import Contacts<br>
**Preconditions:** Only contact information from a specified file format can be imported<br>
**MSS**
1. User requests to import new contacts from an external contact list.
2. NAB adds the list of new contacts to the existing contact list/database.
   <br> *Use case ends.*

**Extensions**

* 1a. NAB is unable to read the file.
    * 1a1. NAB informs the user of the error.
    <br> *Use case ends.*<br><br>
* 1b. NAB finds a contact number that already exists in the database while reading the file.
    * 1b1. NAB informs the user of the error.
    * 1b2. User acknowledges the error.
    * 1b3. NAB skips the contact information with the existing contact number and
      continues reading the rest of the file.
    <br> *Use case ends.*
</panel>

*{More to be added}*

### Non-Functional Requirements

###### Portability:
1.  Should work on any _mainstream OS_ as long as it has Java `17` or above installed.
2.  Should be packaged as a single JAR file not exceeding size of 100MB.
3.  Should be fully functional offline and must not depend on any remote server.

###### Scalability:
4.  NAB is intended for single-user use only and does not support multi-user scenarios.

###### Usability:
5.  GUI should work well (i.e. should not cause any resolution-related inconveniences to the user), for standard screen resolutions 1980x1080 and higher, and for screens scaled by 100% to 125%.
6.  GUI should remain usable (i.e. all functions can be used even if the user experieince is not optimal) for resolutions 1280x720 and higher, and for screens scaled by 150%.

###### Performance:
7. NAB should be able to hold up to 1000 persons without a noticeable sluggishness in performance for typical usage.

###### Data Persistence:
8.  The data file should be stored locally in a human-editable text file, allowing advanced users to manipulate data directly by editing the file.

###### Data Synchronization:
9.  All modifications to data should be propogated and reflected in local data storage within 3 seconds.

###### Stability:
10.  All exceptions and errors should be handled gracefully by the application, i.e. there should not be any application crashes.

###### Fault Tolerance:
11.  Should be able to recover at least uncorrupted portions of local storage file or from a backup file should data file be corrupted.

###### Efficiency:
12.  A user with above average typing speed for regular English text (i.e. not code, not system admin commands) should be able to accomplish most of the tasks faster using commands than using the mouse.

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

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<box type="info" seamless>

**Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.

</box>

### Launch and shutdown

1. Initial launch

   1. Download the jar file and copy into an empty folder

   2. Double-click the jar file Expected: Shows the GUI with a set of sample contacts. The window size may not be optimum.

2. Saving window preferences

   1. Resize the window to an optimum size. Move the window to a different location. Close the window.

   2. Re-launch the app by double-clicking the jar file.<br>
      Expected: The most recent window size and location is retained.

3. _{ more test cases …​ }_

### Deleting a person

1. Deleting a person while all persons are being shown

   1. Prerequisites: List all persons using the `list` command. Multiple persons in the list.

   2. Test case: `delete 1`<br>
      Expected: First contact is deleted from the list. Details of the deleted contact shown in the status message. Timestamp in the status bar is updated.

   2. Test case: `delete 0`<br>
      Expected: No person is deleted. Error details shown in the status message. Status bar remains the same.

   3. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.

2. _{ more test cases …​ }_

### Saving data

1. Dealing with missing/corrupted data files

   1. _{explain how to simulate a missing/corrupted file, and the expected behavior}_

2. _{ more test cases …​ }_
