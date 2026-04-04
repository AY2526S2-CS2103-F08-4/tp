---
  layout: default.md
  title: "User Guide"
  pageNav: 3
---

# NAB User Guide

NAB is a **desktop app for NUS students to manage contacts across multiple modules, project groups, and CCAs, optimized for use via a Command Line Interface** (CLI) while still having the benefits of a Graphical User Interface (GUI). If you can type fast, NAB can help you organize and retrieve context-specific contacts and track commitments faster than traditional GUI apps.

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## Quick Start

1. Ensure you have Java `17` or above installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download the latest `.jar` file from [here](https://github.com/AY2526S2-CS2103-F08-4/tp/releases).

1. Copy the file to the folder you want to use as the _home folder_ for your AddressBook.

1. Open a command terminal, `cd` into the folder you put the jar file in, and use the `java -jar NAB.jar` command to run the application.<br><br>
   A GUI similar to the below should appear in a few seconds. Note how the app contains some sample data.<br>
   ![Ui](images/Ui.png)

1. Type the command in the command box and press Enter to execute it. <br> e.g. typing **`help`** and pressing Enter will open the help window.<br>

    Some example commands you can try:

   * `list` : Lists all contacts.

   * `add n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01` : Adds a contact named `John Doe` to the address book.

   * `delete n/John Doe` : Deletes a contact with name 'John Doe' from the address book.

   * `clear` : Deletes all contacts.

   * `exit` : Exits the app.

1. Refer to the [Features](#features) below for details of each command.

--------------------------------------------------------------------------------------------------------------------

## Features

<box type="info" seamless>

**Notes about the command format:**<br>

* Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  e.g. in `add n/NAME`, `NAME` is a parameter which can be used as `add n/John Doe`.

* Items in square brackets are optional.<br>
  e.g `n/NAME [t/TAG]` can be used as `n/John Doe t/friend` or as `n/John Doe`.

* Items with `...` after them can be used multiple times including zero times.<br>
  e.g. `[t/TAG]...` can be used as ` ` (i.e. 0 times), `t/friend`, `t/friend t/family` etc.

* Parameters can be in any order.<br>
  e.g. if the command specifies `n/NAME p/PHONE_NUMBER`, `p/PHONE_NUMBER n/NAME` is also acceptable.

* Extraneous parameters for commands that do not take in parameters (such as `help`, `list`, `exit` and `clear`) will be ignored.<br>
  e.g. if the command specifies `help 123`, it will be interpreted as `help`.

* Tags are case-insensitive. t/Friends and t/friends are treated as 1 unique tag. Multiple use of the same tags with different case sensitivity should not be used.
e.g `find n/John Doe t/Friends t/friends`

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</box>

### Parameter Constraints & Formats
Throughout this guide, various commands require specific parameters. <br><br>
Unless stated otherwise, please ensure your inputs adhere to the following rules:

* **`NAME`**
    * Must contain only alphabetic characters and spaces.
    * Cannot be blank or start with a space (the first character must be a letter).

* **`PHONE_NUMBER`**
    * Must contain strictly numbers.
    * Must be between 7 and 15 digits long.

* **`EMAIL`**
    * Must be of the standard format: `local-part@domain`.
    * **Local-part:** Can only contain alphanumeric characters and the special characters `+`, `_`, `.`, and `-`. It cannot start or end with a special character.
    * **Domain:** Made up of domain labels separated by periods (`.`).
        * Must end with a domain label at least 2 characters long.
        * Each label must start and end with alphanumeric characters.
        * Labels can contain hyphens (`-`), but no other special characters.

* **`ADDRESS`**
    * Can contain alphanumeric characters, spaces, and the following special characters: `#`, `_`, `,` (comma), and `-` (hyphen).
    * Cannot be blank or consist only of spaces (must start with an alphanumeric or allowed special character).

* **`TAG`**
    * Can contain letters, digits, spaces, hyphens (`-`), and underscores (`_`).
    * Must start with an alphanumeric character (a letter or digit).
    * Must be between 1 and 20 characters long.

* **`PHOTO_PATH`**
    * The file path must end with a valid image extension: `.png`, `.jpg`, or `.jpeg`.
    * The extension is case-insensitive (e.g., `.PNG` is also accepted).

### Managing Profile Pictures
When using the `pfp/PHOTO_PATH` parameter in commands like `add` and `edit`, please note:
* Accepted file extensions are `.png`, `.jpg`, and `.jpeg`.
* `PHOTO_PATH` can be absolute (e.g., `C:/Users/Alex/Pictures/me.jpg`) or relative to the app folder (e.g., `images/me.png`).
* The specified file must exist on your computer; NAB will copy it into the `data/images/` directory.

### Viewing help: `help`

Shows a message explaining how to access the help page.

![help message](images/helpMessage.png)

Format: `help`


### Adding a person: `add`

Adds a person to the address book.

Format: `add n/NAME p/PHONE_NUMBER [e/EMAIL] [a/ADDRESS] [t/TAG]... [pfp/PHOTO_PATH]`

<box type="tip" seamless>

**Tip:** Can associate 0 or more tags during the add process
</box>

* Contact cannot be added if the added phone number is already registered in the address book.

Examples:
* `add n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01`
* `add n/Betsy Crower t/friend e/betsycrowe@example.com a/Newgate Prison p/1234567 t/criminal`
* `add n/Kim Chaewon p/67676969 pfp/C:\Users\User\Desktop\Photos\Le_sserafim.jpg`
* `add n/Hibiscus p/12345678 t/enemy pfp//home/Desktop/Pictures/hibiscus_flower.png`

### Listing all persons: `list`

Shows a list of all persons in the address book.

Format: `list`

### Editing a person: `edit`

Edits an existing person in the address book.

Format: `edit n/NAME [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]... -- [n/NAME] [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]... [pfp/PHOTO_PATH]`

<box type="tip" seamless>

**Tip:** If there are multiple contacts with the same `NAME`, utilize the other optional parameters to narrow down the updating of the correct contact. This can be done by supplying any of the following information just after `edit n/NAME`: Phone number, Email, Address or Tag.

</box>

* The segment before `--` identifies which contact to edit.
* The segment after `--` specifies fields to be updated.
  * Updatable fields: `n/NAME`, `p/PHONE_NUMBER`, `e/EMAIL`, `a/ADDRESS`, `t/TAG`, `pfp/PHOTO_PATH`.
* `n/NAME` in the target segment is required.
* Existing values will be updated to the input values.
* To add tags, you can specify new tags by typing `t/TAG` in the updated field.
* To delete a specific tag, type an existing tag in the updated field.
* You can remove all the person’s tags by typing `t/` without specifying any tags after it.
* Tags are case-insensitive.

Examples:
*  `edit n/John Doe -- p/91234567 e/johndoe@example.com` edits John Doe's phone and email.
*  `edit n/John Doe p/98765432 -- n/Johnathan Doe t/teammate` uniquely identifies John Doe by phone, then updates name and tags.
*  `edit n/Betsy Crower -- t/` clears all tags for Betsy Crower.
*  `edit n/Alex Yeoh -- pfp/C:/Users/Alex/Pictures/profile.jpg` updates Alex Yeoh's profile picture.

### Finding a person: `find`

Finds persons who match the given contact information.

Format: `find n/NAME [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]...`

<box type="tip" seamless>

**Tip:** If there are multiple contacts with the same `NAME`, utilize the other optional parameters to narrow down the
search to a specific contact. This can be done by supplying any of the following information just after `find n/NAME`: Phone number, Email, Address or Tag.
</box>

* The search is case-insensitive. e.g. `hans` will match `Hans`.
* Only full words will be matched e.g. `Han` will not match `Hans`.
* Order of parameters does not matter.

Examples:
* `find n/John` returns contacts named `John`
* `find n/John t/cs2106` returns contacts named `John` with tag `cs2106`

### Filtering persons by context: `filter`

Finds persons with the given tag(s).

Format: `filter t/TAG[, TAG]...`

* The search is case-insensitive. e.g. `friend` will match `Friend` tag.
* Only full words will be matched e.g. `frie` will not match `friend` tag.

Examples:
* `filter t/friends` finds all contacts that are tagged `friends`
* `filter t/cs2103, cs2105, cs2109s` finds all contacts that have any of these tags.

### Pinning a person: `pin`

Pins the person identified by their name.

Format: `pin n/NAME [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]...`

* Pinned persons are shown first when the `list` command is used.
* The `NAME` is case-insensitive. e.g. `aLeX YeOH` will match `Alex Yeoh`.
* Only full words will be matched e.g. `Alex Yeo` will not match `Alex Yeoh`.
* Order of parameters does not matter.

Examples:
* `pin n/John Doe` pins John Doe when the name uniquely identifies the contact.
* `pin n/John Doe p/91234567` pins the matching John Doe contact by name and phone number.

### Unpinning a person: `unpin`

Unpins the person identified by their name.

Format: `unpin n/NAME [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]...`

* The `NAME` is case-insensitive. e.g. `aLeX YeOH` will match `Alex Yeoh`.
* Only full words will be matched e.g. `Alex Yeo` will not match `Alex Yeoh`.
* Order of parameters does not matter.

Examples:
* `unpin n/John Doe` unpins John Doe when the name uniquely identifies the contact.
* `unpin n/John Doe p/91234567` unpins the matching John Doe contact by name and phone number.
### Assigning tag(s) to person(s): `tag`

Assigns one or more tags to one or more contacts in one command.

Format: `tag label/TAG_TO_ASSIGN [label/TAG_TO_ASSIGN]... n/NAME [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]... [n/NAME [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]...]...`

<box type="tip" seamless>

**Tip:** Use optional fields immediately after each `n/NAME` to disambiguate contacts with the same name.
</box>

How it works:
* `label/...` are the tags that will be assigned to **all** specified contacts.
* Contact segments start with `n/NAME`.
* Optional fields after a given `n/NAME` apply only to that contact segment.
* The tag-assignment segment (`label/...`) and person segments (`n/...`) cannot be mixed.
  All `label/...` entries must appear before the first `n/...`.
* If a tag does not exist yet, NAB creates it automatically.
* If a person segment matches multiple contacts, NAB shows those matches and asks for a more specific command.

Examples:
* `tag label/CS2103 label/CS2030S n/Alice n/Bob`
* Suppose there are multiple `Alice` and `Bob`, an enriched search would be `tag label/CS2103 label/CS2030S n/Alice p/81234567 n/Bob a/Clementi`,
  where Alice has a phone number of `81234567` and Bob has an address of `Clementi`.

### Deleting a person: `delete`

Deletes the specified person from the address book.

Format: `delete n/NAME [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]...`

<box type="tip" seamless>

**Tip:** If there are multiple contacts with the same `NAME`, utilize the other optional parameters to narrow down the
deletion of the correct contact. This can be done by supplying any of the
following information just after `delete n/NAME`: Phone number, Email, Address or Tag.
</box>

* The `NAME` is case-insensitive. e.g. `aLeX YeOH` will match `Alex Yeoh`.
* Only full words will be matched e.g. `Alex Yeo` will not match `Alex Yeoh`.
* Order of parameters does not matter.

Examples:
* `delete n/Alex Yeoh` deletes the contact with a matching name.
* Suppose there are multiple `Alex Yeoh`, an enriched search would be `delete n/Alex Yeoh t/cs2103 t/cs2105`

### Clearing all entries: `clear`

Clears all entries from the address book.

Format: `clear`

### Adding an event: `event add`

Creates a new event for a specified person.

Format: `event add title/TITLE [desc/DESCRIPTION] start/START_DATE end/END_DATE to/NAME [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]...`

<box type="tip" seamless>

**Tip:** If there are multiple contacts with the same `NAME`, utilize the other optional parameters to narrow down the
creation of event for the correct contact. This can be done by supplying any of the
following information just after `event ... to/NAME`: Phone number, Email, Address or Tag.

</box>

* The `NAME` is case-insensitive. e.g. `aLeX YeOH` will match `Alex Yeoh`.
* Only full words will be matched e.g. `Alex Yeo` will not match `Alex Yeoh`.
* Order of parameters does not matter.
* The date time format for start/ and end/ is `YYYY-MM-DD HHmm` or `DD-MM-YYYY HHmm`.

Examples:
* `event add title/CS2109S Meeting desc/Final discussion on problem set 1 start/2026-03-25 0900 end/2026-03-25 1000 to/David Li` adds an event to David Li.
* Suppose there are multiple `David Li`, an enriched search would be `event add title/CS2109S Meeting desc/Final discussion on problem set 1 start/2026-03-25 0900 end/2026-03-25 1000 to/David Li p/99272758`

### View an event: `event view`

Views all events for a specified person.

Format: `event view n/NAME [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]...`

<box type="tip" seamless>

**Tip:** If there are multiple contacts with the same `NAME`, utilize the other optional parameters to
view the events of the correct contact. This can be done by supplying any of the
following information just after `event view n/NAME`: Phone number, Email, Address or Tag.

</box>

* The `NAME` is case-insensitive. e.g. `aLeX YeOH` will match `Alex Yeoh`.
* Only full words will be matched e.g. `Alex Yeo` will not match `Alex Yeoh`.

Examples:
* `event view n/Bernice Yu` views all events that Bernice Yu has.
* Suppose there are multiple `Bernice Yu`, an enriched search would be `event view n/Bernice Yu e/berniceyu@example.com`

### Delete an event: `event delete`

Deletes an event for a specified person.

Format: `event delete title/TITLE start/START_DATE end/END_DATE n/NAME [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]...`
<box type="tip" seamless>

**Tip:** If there are multiple contacts with the same `NAME`, utilize the other optional parameters to narrow down the
deletion of event for the correct contact. This can be done by supplying any of the
following information just after `event ... n/NAME`: Phone number, Email, Address or Tag.

</box>

* The `NAME` is case-insensitive. e.g. `aLeX YeOH` will match `Alex Yeoh`.
* Only full words will be matched e.g. `Alex Yeo` will not match `Alex Yeoh`.
* Order of parameters does not matter.
* The date time format for start/ and end/ is `YYYY-MM-DD HHmm` or `DD-MM-YYYY HHmm`.

Examples:
* `event delete title/Meeting start/2026-03-12 1100 end/2026-04-12 2359 n/David Li` deletes the event that titled Meeting which starts at 12 March 2026 1100 and ends at 12 April 2026 2359 assigned to David Li.
* Suppose there are multiple `David Li`, an enriched search would be `event delete title/Meeting start/2026-03-12 1100 end/2026-04-12 2359 n/David Li p/99272758`

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Exporting Contacts: `export`

Exports a list of contacts into a CSV formatted file.

Format: `export t/EXPORT_TYPE f/FILENAME`

* `EXPORT_TYPE` dictates the scope of the export. Expected inputs are `all`
(exports the entire address book) or `current` (exports the currently filtered list)
* `FILENAME` dictates the name of the target CSV file. Do not include the `.csv` extension in the input.
* The exported file is saved in the same directory that contains the current NAB data file.
* Order of parameters does not matter.

Examples:
* Exports all contacts stored in NAB into a CSV file : `export t/all f/save_file`
* Exports contacts from the currently filtered list : `export t/current f/save_file`

### Importing Contacts: `import`

Imports a list of contacts from a CSV formatted file.

Format: `import t/IMPORT_TYPE f/FILENAME`

* `IMPORT_TYPE` dictates how the data is imported. Expected inputs are `add`
(appends the contacts from the list to existing address book) or `overwrite`
(replaces all the current address book information with contacts from the list).
* `FILENAME` dictates the name of the source CSV file. Do not include the `.csv` extension in the input.
* The file to be imported must be located in the same directory as your current AddressBook data file.
* Any contacts in the CSV file that already exist in your current address book will be skipped to prevent duplicates.
* Rows in the CSV that are malformed or have missing mandatory fields will be safely skipped.
* Order of parameters does not matter.

Examples:
* Import from the CSV file and overwrite existing data in NAB : `import t/overwrite f/save_file`
* Import from the CSV file and append to the existing data in NAB : `import t/add f/save_file`

### Saving the data

AddressBook data are saved in the hard disk automatically after any command that changes the data. There is no need to save manually.

### Editing the data file

AddressBook data are saved automatically as a JSON file `[JAR file location]/data/addressbook.json`. Advanced users are welcome to update data directly by editing that data file.

<box type="warning" seamless>

**Caution:**
If your changes to the data file makes its format invalid, AddressBook will discard all data and start with an empty data file at the next run.  Hence, it is recommended to take a backup of the file before editing it.<br>
Furthermore, certain edits can cause the AddressBook to behave in unexpected ways (e.g., if a value entered is outside the acceptable range). Therefore, edit the data file only if you are confident that you can update it correctly.
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
-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------
**Add**    | `add n/NAME p/PHONE_NUMBER [e/EMAIL] [a/ADDRESS] [t/TAG]... [pfp/PHOTO_PATH]` <br> e.g., `add n/James Ho p/22224444 e/jamesho@example.com a/123, Clementi Rd, 1234665 t/friend t/colleague pfp/images/james.jpg`
**Clear**  | `clear`
**Delete** | `delete n/NAME [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]...`<br> e.g., `delete n/Alex Yeoh t/cs2103 t/cs2105`
**Edit**   | `edit n/NAME [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]... -- [n/NAME] [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]... [pfp/PHOTO_PATH]`<br> e.g.,`edit n/James Lee e/jameslee@example.com -- t/CS2100 pfp/images/james.jpg`
**Event Add** | `event add title/TITLE [desc/DESCRIPTION] start/START_DATE end/END_DATE to/NAME [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]...`<br> e.g., `event add title/CS2109S Meeting desc/Final discussion on problem set 1 start/2026-03-25 0900 end/2026-03-25 1000 to/David Li`
**Event Delete** | `event delete title/TITLE start/START_DATE end/END_DATE n/NAME [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]...`<br> e.g., `event delete title/Meeting start/2026-03-12 1100 end/2026-04-12 2359 n/David Li`
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
