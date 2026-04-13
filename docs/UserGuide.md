---
  layout: default.md
  title: "User Guide"
  pageNav: 3
---

# NAB User Guide

___All your NUS connections, right at your fingertips___

NUS Address Book (NAB) is a desktop application built for **NUS students** to manage contacts across multiple modules, project groups, and CCAs with ease.
It is **optimized for use** via a Command Line Interface (CLI) while still having the benefits of a **Graphical User Interface (GUI)**.
If you can type fast, NAB can help you organize and retrieve context-specific contacts and track events faster than traditional GUI apps.

Here is how NAB can **make student networking easier**:
- Store and edit contact cards for your friends
- Track events tied to contacts
- Make bulk organisation easier with tags

<!-- * Table of Contents -->
<page-nav-print />

---

## Navigating this User Guide

This section provides a quick overview of how this guide is structured, so you can easily find the information you need and understand the notation used throughout.

### Who is this guide for?
This guide is for NUS students who want to use NAB to manage contacts and related events efficiently.

Whether you are new to NAB or just looking for a specific command, this guide is organised to help you find what you need quickly.

|                 Looking to...                  |                Head to...                |
|:----------------------------------------------:|:----------------------------------------:|
|         Set up NAB for the first time          |     **[Quick Start](#quick-start)**      |
|      Check command syntax and usage rules      |        **[Features](#features)**         |
|     Find a command quickly while using NAB     | **[Command summary](#command-summary)**  |
| Understand the comprehensive technical details | **[Developer Guide](DeveloperGuide.md)** |
### Conventions used
This guide uses **callout boxes** to help you quickly identify different types of information:
<div>
<box type="info" icon=":fa-solid-code:">

This **blue box** with the **code mark icon** provides you with **example commands** that demonstrate how a feature works.

</box>

<box type="important" icon=":fa-solid-exclamation-triangle:">

This **red box** with the **exclamation triangle icon** draws your attention to **warnings, important notes or limitations**

</box>

<box theme="success" icon=":fa-solid-lightbulb:">

This **green box** with a **lightbulb icon** highlights **helpful tips** for using NAB more effectively.

</box>

</div>



--------------------------------------------------------------------------------------------------------------------

# Quick Start

1. Ensure you have Java `17` or above installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

2. Download the latest `.jar` file from [here](https://github.com/AY2526S2-CS2103-F08-4/tp/releases).

3. Copy the file to the folder you want to use as the _home folder_ for your AddressBook.

4. Open a command terminal, `cd` into the folder you put the jar file in, and use the `java -jar NAB.jar` command to run the application.<br><br>
   A GUI similar to the below should appear in a few seconds. Note how the app contains some sample data.<br>
   ![Ui](images/Ui.png)

5. Type the command in the command box and press Enter to execute it. <br> e.g. typing **`help`** and pressing Enter will open the help window.<br>

    Some example commands you can try:

   * `list` : Lists all contacts.

   * `add n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01` : Adds a contact named `John Doe` to the address book.

   * `delete n/John Doe` : Deletes a contact with name 'John Doe' from the address book.

   * `clear` : Deletes all contacts.

   * `exit` : Exits the app.

6. Refer to the [Features](#features) below for details of each command.

<box type="info" seamless>

**Understanding the GUI**


The labelled interface below highlights the main parts of NAB's GUI.

![Labelled UI](images/Ui-guide.png)

* **Command input box**: where you type commands.
* **Command output box**: shows feedback after each command is executed.
* **Person list panel**: displays the contacts currently shown.
* **Event list panel**: displays events related to the current context, such as a selected or uniquely matched person.

</box>

--------------------------------------------------------------------------------------------------------------------

# Features

## Quick utilities

<box type="info" seamless>

**Notes about the command format in Quick Utilities:**
* Extraneous parameters for commands that do not take in parameters (such as `help`, `list`, `exit` and `clear`) will be ignored.
    * e.g. if the command specifies `help 123`, it will be interpreted as `help`.<br><br>
* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</box>

### Navigating command history

NAB keeps track of the commands you have entered, making it easier to reuse previous commands without typing them again.

This feature allows you to use **arrow keys** while in the command box to navigate through previously entered commands for the current session.

* Press ↑ to go back to older commands.
* Press ↓ to go forward to more recent commands.
* Pressing ↓ past the most recent history entry restores the text you were typing before you started navigating.
* Command history is session-only and is not saved after you exit NAB.

### Copying a Contact's Information

NAB allows you to copy a contact’s information, making it easier to reuse their details without typing them out manually.
A double-click on a person's contact copies their information to your clipboard.

This feature allows you to copy the displayed information of a contact for use outside NAB.

The copied text includes the following fields (optional fields that are not present are omitted):
- Name
- Phone number
- Tags
- Address
- Email

Each field is placed on a separate line.

### Viewing help: `help`

NAB provides quick access to both the online and offline versions of the User Guide, allowing you to refer to it whenever you need guidance on NAB’s features and commands.

This feature displays a message explaining how to access the online and offline help pages.

![help message](images/helpMessage.png)

Format: `help`

### Exiting the program: `exit`

When it is time to say goodbye, NAB will not make it awkward.

This feature closes the program and ends the current session.

Format: `exit`

## Contact Management

### Parameters constraints & format

Before examining the individual commands for managing contacts, please refer to the formatting requirements and constraints for each parameter. Unless stated otherwise, ensure all inputs adhere to the rules stated in this section

<box type="info" seamless>

**Parameters Requirements and Constraints**


| Parameter | Format                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    | Example |
|-----------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------|
| `n/NAME` | • Must contain only alphabetic characters and spaces.<br>• Cannot be blank or start with a space (the first character must be a letter).                                                                                                                                                                                                                                                                                                                                                                                                  | `n/John Doe` |
| `p/PHONE_NUMBER` | • Must contain strictly numbers.<br>• Must be between 7 and 15 digits long.                                                                                                                                                                                                                                                                                                                                                                                                                                                               | `p/98765432` |
| `e/EMAIL` | • Must be of the standard format: `local-part@domain`.<br>• **Local-part:** Can only contain alphanumeric characters and the special characters `+`, `_`, `.`, and `-`. It cannot start or end with a special character.<br>• **Domain:** Made up of domain labels separated by periods (`.`).<br>&nbsp;&nbsp;◦ Must end with a domain label at least 2 characters long.<br>&nbsp;&nbsp;◦ Each label must start and end with alphanumeric characters.<br>&nbsp;&nbsp;◦ Labels can contain hyphens (`-`), but no other special characters. | `e/johnd@example.com` |
| `a/ADDRESS` | • Can contain alphanumeric characters, spaces, and the following special characters: `#`, `_`, `,` (comma), and `-` (hyphen).<br>• Cannot be blank or consist only of spaces (must start with an alphanumeric or allowed special character).                                                                                                                                                                                                                                                                                              | `a/John street, block 123, #01-01` |
| `t/TAG` | • Can contain letters, digits, spaces, hyphens (`-`), and underscores (`_`).<br>• Must start with an alphanumeric character (a letter or digit).<br>• Must be between 1 and 20 characters long.                                                                                                                                                                                                                                                                                                                                           | `t/friend` |
| `pfp/PHOTO_PATH` | • File path must end with a valid image extension: `.png`, `.jpg`, or `.jpeg` (case-insensitive).<br>• Can be absolute (e.g., `C:/Users/Alex/Pictures/me.jpg`) or relative to the app folder (e.g., `images/me.png`).<br>• The specified file must exist on your computer; NAB will copy it into the `data/images/` directory<br>• File path cannot be referencing any subfolder/files residing in NAB's `data/images/` folder.                                                                                                                                                                                                             | `pfp/images/me.png` |

</box>

<box type="info" seamless>

**Notes about the command format in Contact Management:**
* Words in `UPPER_CASE` are the parameters to be supplied by the user.
    * e.g. in `add n/NAME`, `NAME` is a parameter which can be used as `add n/John Doe`.<br><br>
* Items in square brackets are **optional**.
    * e.g. `n/NAME [t/TAG]` can be used as `n/John Doe t/friend` or as `n/John Doe`.<br><br>
* Items with `...` after them can be used multiple times including zero times.
    * e.g. `[t/TAG]...` can be used as ` ` (i.e. 0 times), `t/friend`, `t/friend t/family` etc.<br><br>
* Parameters can be in any order. (except [Assigning tag(s) to person(s)](#assigning-tag-s-to-person-s-tag))
    * e.g. if the command format specifies `n/NAME p/PHONE_NUMBER`, `p/PHONE_NUMBER n/NAME` is also acceptable.<br><br>
* `NAME` and `TAG` are case-insensitive.
    * `t/Friends` and `t/friends` are treated as 1 unique tag.
    * `n/aLeX YeOH` will match `Alex Yeoh`.<br><br>
* Only full words will be matched
    * e.g. `Han` will not match `Hans`.
</box>

### User Disambiguation

Commands in NAB identify a contact by name. If two or more contacts share the same name,
NAB cannot determine which one you meant, and will display the following error:
<box type="important" icon=":fa-solid-exclamation-triangle:">

**ERROR MESSAGE**<br>
`Multiple matches identified! Please provide more arguments.`<br>
This error means your command matched more than one contact. No changes have been made —
retry the command with additional details to uniquely identify the contact you want.

</box>

<box type="info" seamless>

**Disambiguate with Optional Parameters**


Add one or more optional parameters **immediately after `n/NAME`** to narrow the match down to a single contact.

| Parameter | Prefix | Example               |
|-----------|--------|-----------------------|
| Phone number | `p/` | `p/91234567`          |
| Email address | `e/` | `e/Irene@example.com` |
| Home address | `a/` | `a/Clementi Ave 6`    |
| Tag | `t/` | `t/CS2103`            |

Supply as many parameters as needed — NAB will only proceed once exactly one contact matches all the criteria you provide.

<box theme="success" icon=":fa-solid-lightbulb:">

**TIP**

Use `find n/NAME` first to see all contacts that share a name. Their details will help you
decide which parameter to add for disambiguation.

</box>

Here is what NAB looks like when you **disambiguate duplicates:**


![disambiguation.png](images/disambiguation.png)
</box>


<br>

### Adding a person: `add`
Build your NUS network instantly with NAB by contacts of the people you meet across modules, project groups, and CCAs.

This `add` feature allows you to add a person to the address book.

Format: `add n/NAME p/PHONE_NUMBER [e/EMAIL] [a/ADDRESS] [t/TAG]... [pfp/PHOTO_PATH]`


<box type="info">

**Examples**


- `add n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01`<br>
  Adds a new contact named John Doe with a phone number, email, and address.

- `add n/Betsy Crower t/friend e/betsycrowe@example.com a/Newgate Prison p/1234567 t/criminal`<br>
  Adds a new contact named Betsy Crower with a phone number, email, address, and two tags: _friend_ and _criminal_.

- `add n/Kim Chaewon p/67676969 pfp/C:\Users\User\Desktop\Photos\Le_sserafim.jpg`<br>
  Adds a new contact named Kim Chaewon with a phone number and a profile photo.

</box>

<box type="important">

**Important**


- `add` command with `pfp/` succeeds only if the image file exists, is readable, and is a supported image format.
- Contact cannot be added if the added phone number is already registered in the address book.
- Refer to the [user disambiguation](#user-disambiguation) section if you encounter the error: `Multiple matches identified!`

</box>

<box theme="success">

**Tip**


Can associate 0 or more tags during the add process.

</box>


### Listing all persons: `list`

Shows a list of all persons in the address book.

Format: `list`

### Editing a person: `edit`

Edits an existing person in the address book.

Format: `edit n/NAME [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]... -- [n/NAME] [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]... [pfp/PHOTO_PATH]`

* The segment before `--` identifies which contact to edit.
* The segment after `--` specifies fields to be updated.
  * Updatable fields: `n/NAME`, `p/PHONE_NUMBER`, `e/EMAIL`, `a/ADDRESS`, `t/TAG`, `pfp/PHOTO_PATH`.
* `n/NAME` in the target segment is required.
* Existing values will be updated to the input values.
* To add tags, you can specify new tags by typing `t/TAG` in the updated field.
* To delete a specific tag, type an existing tag in the updated field.
* You can remove all the person’s tags by typing `t/` without specifying any tags after it.

<box type="info">

**Examples**


- `edit n/John Doe -- p/91234567 e/johndoe@example.com`<br>
  Edits John Doe's phone and email.

- `edit n/John Doe p/98765432 -- n/Johnathan Doe t/teammate`<br>
  Uniquely identifies John Doe by phone number, then updates name and tags.

- `edit n/Betsy Crower -- t/`<br>
  Clears all tags for Betsy Crower.

- `edit n/Alex Yeoh -- pfp/C:/Users/Alex/Pictures/profile.jpg`<br>
  Updates Alex Yeoh's profile picture.

</box>

<box type="important">

**Disambiguating contacts with the same name**


- If you encounter the error `Multiple matches identified! Please provide more arguments.`, add optional parameters immediately after n/NAME to narrow down the match — Phone number, Email, Address, or Tag.
- See [User Disambiguation](#user-disambiguation) for details.

</box>

### Finding a person: `find`

Finds persons who match the given contact information.

Format: `find n/NAME [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]...`

<div style="text-align: center;">
  <img src="images/find_command.png" style="width: 80%;">
</div>

<box type="info">

**Examples**


- `find n/John`<br>
  Returns contacts named John

- `find n/John t/cs2106`<br>
  Uniquely identifies a John Doe with a cs2106 tag

- `find n/John t/cs2106 t/cs2109s t/cs2103`<br>
  Uniquely identifies a John Doe with a cs2106, cs2109s and cs2103 tag

</box>

<box type="important">

**Disambiguating contacts with the same name**


- If you encounter the error `Multiple matches identified! Please provide more arguments.`, add optional parameters immediately after n/NAME to narrow down the match — Phone number, Email, Address, or Tag.
- See [User Disambiguation](#user-disambiguation) for details.

</box>

### Filtering persons by context: `filter`

Filters persons with the given tag(s).

Format: `filter t/TAG[, TAG]...`

<div style="text-align: center;">
  <img src="images/filter_command.png" style="width: 80%;">
</div>

<box type="info">

**Examples**


- `filter t/friends`<br>
Filters all contacts to show only contacts that are tagged friends.

- `filter t/cs2103, cs2105, cs2109s`<br>
Filters all contacts to show only contacts that have any of these tags.

</box>

<box theme="success">

**Tip**


Can associate 1 or more tags during the filter process.

</box>

### Pinning a person: `pin`

Pins the person identified by their name.

Format: `pin n/NAME [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]...`

<div style="text-align: center;">
  <img src="images/pin_command.png" style="width: 80%;">
</div>

* Pinned persons are shown first in the full list and in filtered tag views.

<box type="info">

**Examples**


- `pin n/John Doe`<br>
Pins John Doe when the name uniquely identifies the contact.

- `pin n/John Doe p/91234567`<br>
Pins the matching John Doe contact by name and phone number.

</box>

<box type="important">

**Disambiguating contacts with the same name**


- If you encounter the error `Multiple matches identified! Please provide more arguments.`, add optional parameters immediately after n/NAME to narrow down the match — Phone number, Email, Address, or Tag.
- See [User Disambiguation](#user-disambiguation) for details.

</box>

### Unpinning a person: `unpin`

Unpins the person identified by their name.

Format: `unpin n/NAME [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]...`

<box type="info">

**Examples**


- `unpin n/John Doe`<br>
Unpins John Doe when the name uniquely identifies the contact.

- `unpin n/John Doe p/91234567`<br>
Unpins the matching John Doe contact by name and phone number.

</box>

<box type="important">

**Disambiguating contacts with the same name**


- If you encounter the error `Multiple matches identified! Please provide more arguments.`, add optional parameters immediately after n/NAME to narrow down the match — Phone number, Email, Address, or Tag.
- See [User Disambiguation](#user-disambiguation) for details.

</box>

### Assigning tag(s) to person(s): `tag`

Assigns one or more tags to one or more contacts in one command.

Format: `tag label/TAG_TO_ASSIGN [label/TAG_TO_ASSIGN]... n/NAME [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]... [n/NAME [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]...]...`

<div style="text-align: center;">
  <img src="images/tag_command.png" style="width: 80%;">
</div>

* Start by listing the tag(s) using `label/...`; these tags are applied to **every** specified contact.
* After the tags, add one or more contact segments, each starting with `n/NAME`.
* Optional fields placed immediately after a contact's `n/NAME` (such as `p/`, `e/`, `a/`, `t/`) apply only to that contact.
* Do not mix tag and contact segments. All `label/...` entries must come before the first `n/...`.
* If a tag does not exist yet, NAB creates it automatically.
* If a person segment matches multiple contacts, NAB shows those matches and asks for a more specific command.

<box type="info">

**Examples**


- `tag label/CS2103 n/John Doe`<br>
Assigns 1 tag (`CS2103`) to 1 user (`John Doe`).

- `tag label/CS2103 label/CS2030S label/CS2040 n/John Doe e/johndoe@example.com`<br>
Assigns 3 tags (`CS2103`, `CS2030S`, `CS2040`) to 1 user (`John Doe`) using additional fields to disambiguate the user.

- `tag label/CS2103 n/John Doe n/Betsy Crower`<br>
Assigns 1 tag (`CS2103`) to 2 users (`John Doe` and `Betsy Crower`).

- `tag label/CS2103 label/CS2030S n/John Doe p/91234567 n/Betsy Crower e/betsycrower@example.com`<br>
Assigns 2 tags (`CS2103`, `CS2030S`) to 2 users (`John Doe` and `Betsy Crower`) using additional fields to disambiguate both users.

</box>

<box type="important">

**Disambiguating contacts with the same name**


- If you encounter the error `Multiple matches identified! Please provide more arguments.`, add optional parameters immediately after n/NAME to narrow down the match — Phone number, Email, Address, or Tag.
- See [User Disambiguation](#user-disambiguation) for details.

</box>

<box theme="success">

**Tip**


- Can associate 1 or more tags during the tagging process.
- Can associate 1 or more persons during the tagging process.

</box>

### Deleting a person: `delete`

Deletes the specified person from the address book.

Format: `delete n/NAME [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]...`

<box type="info">

**Examples**


- `delete n/John Doe`<br>
Deletes John Doe when the name uniquely identifies the contact.

- `delete n/John Doe p/91234567`<br>
Deletes the matching John Doe contact by name and phone number.

</box>

<box type="important">

**Disambiguating contacts with the same name**


- If you encounter the error `Multiple matches identified! Please provide more arguments.`, add optional parameters immediately after n/NAME to narrow down the match — Phone number, Email, Address, or Tag.
- See [User Disambiguation](#user-disambiguation) for details.

</box>

### Clearing all entries: `clear`

Clears all entries from the address book.

Format: `clear`

## Event Management

### Parameters constraints & format

Before examining individual event commands, refer to this section for event-related parameter rules.
For shared contact-identification parameters used in event commands (`n/`, `p/`, `e/`, `a/`, `t/`),
the same constraints in [Contact Management: Parameters constraints & format](#parameters-constraints-and-amp-format) apply.

<box type="info" seamless>

**Parameters Requirements and Constraints**


| Parameter | Format                                                                                                                                                                      | Example |
|-----------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------|
| `title/TITLE` | • Must be 1 to 50 characters.<br>• May contain letters, digits, spaces, periods (`.`), hyphens (`-`), colons (`:`), and parentheses (`(`, `)`).<br>• Must not contain `/`.<br>• No leading/trailing spaces and no consecutive spaces. | `title/CS2103 Milestone 2 - Draft Submission` |
| `desc/DESCRIPTION` | • Must be 0 to 1000 characters.<br> • May contain letters, digits, spaces, periods (`.`), hyphens (`-`), colons (`:`), and parentheses (`(`, `)`).<br> • Must not contain `/`.<br> • No leading/trailing spaces and no consecutive spaces. | `desc/Project Meeting (Online): bring printout.` |
| `start/START_DATE` | • Must follow `YYYY-MM-DD HHmm` in 24-hour format.                                                                                                                          | `start/2026-03-25 0900` |
| `end/END_DATE` | • Must follow `YYYY-MM-DD HHmm` in 24-hour format.<br> • Must be strictly after `START_DATE` provided.                                                                      | `end/2026-03-25 1000` |
</box>

### Adding an event: `event add`

Creates a new event for a specified person.

Format: `event add title/TITLE [desc/DESCRIPTION] start/START_DATE end/END_DATE n/NAME [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]...`

<div style="text-align: center;">
  <img src="images/event_add.png" style="width: 80%;">
</div>

<box type="info">

**Examples**


- `event add title/CS2109S Meeting desc/Final discussion on problem set 1 start/2026-03-25 0900 end/2026-03-25 1000 n/David Li`<br>
  Adds an event titled "CS2109S Meeting" to David Li.

- `event add title/CS2109S Meeting desc/Final discussion on problem set 1 start/2026-03-25 0900 end/2026-03-25 1000 n/David Li p/99272758`<br>
  Adds an event to the David Li with phone number `99272758`, disambiguating between multiple contacts with the same name.

</box>

<box type="important">

**Event uniqueness and time clashes**


- NAB treats the event list as **your schedule** (user point of view).
- Time clashes are checked **globally** across the event list, not per contact.
  - Reason: from your point of view, one user cannot be in two places at the same time, so overlapping events are blocked.
- Overlap means the 2 ranges share actual time in common. Back-to-back events are allowed (for example, one ends at `1000` and another starts at `1000`).
- If a clash is found, NAB shows: `This event clashes with an existing event in the calendar.`

</box>

<box type="important">

**Disambiguating contacts with the same name**


- If you encounter the error `Multiple matches identified! Please provide more arguments.`, add optional parameters immediately after n/NAME to narrow down the match — Phone number, Email, Address, or Tag.
- See [User Disambiguation](#user-disambiguation) for details.

</box>

### View an event: `event view`

Views all events for a specified person.
<div style="text-align: center;">
  <img src="images/event_view.png" style="width: 560px;">
</div>
<br>

Format: `event view n/NAME [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]...`


<box type="info">

**Examples**


- `event view n/Bernice Yu`<br>
  Views all events for Bernice Yu.

- `event view n/Bernice Yu e/berniceyu@example.com`<br>
  Views events for the Bernice Yu with the given email, disambiguating between multiple contacts with the same name.

</box>

<box type="important">

**Disambiguating contacts with the same name**


- If you encounter the error `Multiple matches identified! Please provide more arguments.`, add optional parameters immediately after n/NAME to narrow down the match — Phone number, Email, Address, or Tag.
- See [User Disambiguation](#user-disambiguation) for details.

</box>

### Delete an event: `event delete`

Deletes an event for a specified person.

Format: `event delete start/START_DATE n/NAME [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]...`

<div style="text-align: center;">
  <img src="images/event_delete.png" style="width: 80%;">
</div>


<box type="info">

**Examples**


- `event delete start/2026-03-12 1100 n/David Li`<br>
  Deletes the event only if David Li has an assigned event that starts at `2026-03-12 1100`.

- `event delete start/2026-03-12 1100 n/David Li p/99272758`<br>
  Deletes the event only if the David Li with phone number `99272758` has an assigned event that starts at `2026-03-12 1100`, disambiguating between multiple contacts with the same name.

</box>

<box type="important">

**Disambiguating contacts with the same name**


- If you encounter the error `Multiple matches identified! Please provide more arguments.`, add optional parameters immediately after n/NAME to narrow down the match — Phone number, Email, Address, or Tag.
- See [User Disambiguation](#user-disambiguation) for details.

</box>

## Data and Storage

### Exporting contacts: `export`
Back up your NAB contacts in seconds so you can share, archive, or migrate your data anytime.

This `export` feature allows you to write contacts from NAB into 2 CSV files (`<FILENAME>_persons.csv` and `<FILENAME>_events.csv`).

Format: `export t/EXPORT_TYPE f/FILENAME`

<box type="info">

**Examples**


- `export t/all f/save_file`<br>
  Exports all contact information in NAB to `save_file_persons.csv` and `save_file_events.csv`.

- `export t/current f/save_file`<br>
  Exports only the currently displayed contact information to `save_file_persons.csv` and `save_file_events.csv`.

</box>


<box type="important">

**Important**


- `EXPORT_TYPE` must be either:
    - `all` (export every contact in NAB), or
    - `current` (export only the contacts currently shown in the contact list).
- Enter `FILENAME` without specifying the `_persons.csv` or `_events.csv` extension. NAB will automatically append it for you.
- The exported files are saved in the same directory as the current NAB data file.
  - If a file with the same name already exists, it will be overwritten.
- Order of parameters does not matter.

</box>

<box theme="success">

**Tip**


Use `export t/current ...` after `find` or `filter` to quickly export a specific subset of contacts.

</box>


### Importing contacts: `import`
Bring your contact data into NAB quickly when switching devices or restoring from a backup.

This `import` feature allows you to load contacts from 2 CSV files (`<FILENAME>_persons.csv` and `<FILENAME>_events.csv`) into NAB.

Format: `import t/IMPORT_TYPE f/FILENAME`


<box type="info">

**Examples**


- `import t/overwrite f/save_file`<br>
  Imports contacts from `save_file_persons.csv` and event definitions from `save_file_events.csv`, replacing the existing data on the address book.

- `import t/add f/save_file`<br>
  Imports contacts from `save_file_persons.csv` and event definitions from `save_file_events.csv`, adding them to the current address book.

</box>

<box type="important">

**Important**


- `IMPORT_TYPE` must be either:
    - `add` (adds imported data to the current address book), or
    - `overwrite` (replaces the current address book with imported data).
- Enter `FILENAME` without specifying the `_persons.csv` or `_events.csv` extension, as NAB automatically looks for the 2 CSV files belonging to the specified `FILENAME`.
- The CSV files must be placed in the same directory as the current NAB data file.
- Contacts in the CSV file that already exist in NAB are skipped to avoid duplicates.
- Rows with invalid or missing required fields are skipped.
- Order of parameters does not matter.

</box>

<box theme="success">

**Tip**


If you are unsure, run `import t/add ...` first to avoid accidental data loss. Use `import t/overwrite ...` only when you want a full replacement.

</box>


### Saving the data
Focus on managing your contacts! NAB does the heavy lifting by saving your data automatically in the background.

<box theme="success">

**Tip**


- There is no manual save command in NAB.
- If a command succeeds, your latest data is already persistent in the data file.

</box>

### Editing the data file

AddressBook data is saved automatically as a JSON file `[JAR file location]/data/addressbook.json`. Advanced users are welcome to update data directly by editing that data file.

<box type="important">

**Important**


- If your changes to the data file make its format invalid, AddressBook will discard all data and start with an empty data file at the next run.  Hence, it is recommended to take a backup of the file before editing it.
- If your changes to the data file make its data inconsistent, NAB will first attempt to repair it automatically. If the inconsistency cannot be resolved, NAB will clear the entire address book to maintain data integrity.
- Furthermore, certain edits can cause the AddressBook to behave in unexpected ways (e.g. if a value entered is outside the acceptable range). Therefore, edit the data file only if you are confident that you can update it correctly.

</box>

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Install the app in the other computer and overwrite the empty data file it creates with the file that contains the data of your previous AddressBook home folder.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the application before running the application again.
2. **If you minimize the Help Window** and then run the `help` command (or use the `Help` menu, or the keyboard shortcut `F1`) again, the original Help Window will remain minimized, and no new Help Window will appear. The remedy is to manually restore the minimized Help Window.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action     | Format, Examples
-----------|------
**Add**    | `add n/NAME p/PHONE_NUMBER [e/EMAIL] [a/ADDRESS] [t/TAG]... [pfp/PHOTO_PATH]` <br> e.g., `add n/James Ho p/22224444 e/jamesho@example.com a/123, Clementi Rd, 1234665 t/friend t/colleague pfp/images/james.jpg`
**Clear**  | `clear`
**Delete** | `delete n/NAME [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]...`<br> e.g., `delete n/Alex Yeoh t/cs2103 t/cs2105`
**Edit**   | `edit n/NAME [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]... -- [n/NAME] [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]... [pfp/PHOTO_PATH]`<br> e.g.,`edit n/James Lee e/jameslee@example.com -- t/CS2100 pfp/images/james.jpg`
**Event Add** | `event add title/TITLE [desc/DESCRIPTION] start/START_DATE end/END_DATE n/NAME [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]...`<br> e.g., `event add title/CS2109S Meeting desc/Final discussion on problem set 1 start/2026-03-25 0900 end/2026-03-25 1000 n/David Li`
**Event Delete** | `event delete start/START_DATE n/NAME [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]...`<br> e.g., `event delete start/2026-03-12 1100 n/David Li`
**Event View** | `event view n/NAME [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]...`<br> e.g., `event view n/Bernice Yu`
**Exit**   | `exit`
**Filter** | `filter t/TAG[, TAG]...`<br> e.g., `filter t/friends`
**Pin**    | `pin n/NAME [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]...`<br> e.g., `pin n/John Doe p/91234567`
**Unpin**  | `unpin n/NAME [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]...`<br> e.g., `unpin n/John Doe p/91234567`
**Find**   | `find n/NAME [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]...`<br> e.g., `find n/James Jake p/67676969`
**Help**   | `help`
**List**   | `list`
**Tag**    | `tag label/TAG_TO_ASSIGN [label/TAG_TO_ASSIGN]... n/NAME [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]... [n/NAME ...]...`<br> e.g., `tag label/CS2103 label/CS2030S n/Alice n/Joe t/Family`
**Export**   | `export t/EXPORT_TYPE f/FILENAME`<br> e.g., `export t/all f/save_file`
**Import**   | `import t/IMPORT_TYPE f/FILENAME`<br> e.g., `import t/overwrite f/save_file`
