package seedu.address.logic.parser;

/**
 * Contains Command Line Interface (CLI) syntax definitions common to multiple commands
 */
public class CliSyntax {

    /* Prefix definitions */
    public static final Prefix PREFIX_NAME = new Prefix("n/");
    public static final Prefix PREFIX_PHONE = new Prefix("p/");
    public static final Prefix PREFIX_EMAIL = new Prefix("e/");
    public static final Prefix PREFIX_ADDRESS = new Prefix("a/");
    public static final Prefix PREFIX_TAG = new Prefix("t/");
    public static final Prefix PREFIX_TAG_ASSIGN = new Prefix("label/");
    public static final Prefix PREFIX_PHOTO = new Prefix("pfp/");

    // Event prefixes
    public static final Prefix PREFIX_TITLE = new Prefix("title/");
    public static final Prefix PREFIX_DESC = new Prefix("desc/");
    public static final Prefix PREFIX_START = new Prefix("start/");
    public static final Prefix PREFIX_END = new Prefix("end/");
    public static final Prefix PREFIX_TO = new Prefix("to/");
}
