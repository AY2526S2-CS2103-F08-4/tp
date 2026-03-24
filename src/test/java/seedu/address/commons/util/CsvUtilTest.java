package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.commons.util.CsvUtil.sanitizeAndWrapValue;
import static seedu.address.commons.util.CsvUtil.splitCsvLine;
import static seedu.address.commons.util.CsvUtil.unwrapValue;

import org.junit.jupiter.api.Test;

public class CsvUtilTest {
    @Test
    public void sanitizeValue_containsComma_wrappedCorrectly() {
        String input = "Ng, David";
        assertEquals("\"Ng, David\"", sanitizeAndWrapValue(input));
    }

    @Test
    public void unwrapValue_escapedQuotes_restoredCorrectly() {
        String input = "\"Yik \"\"The Programmer\"\" Leong\"";
        assertEquals("Yik \"The Programmer\" Leong", unwrapValue(input));
    }

    @Test
    public void splitCsvLine_complexRow_splitsCorrectly() {
        String row = "\"David\",\"91234567\",\"test@d.com\",\"Blk 123, Jurong\",\"tag1;tag2\"";
        String[] expected = {"\"David\"", "\"91234567\"", "\"test@d.com\"", "\"Blk 123, Jurong\"", "\"tag1;tag2\""};
        assertArrayEquals(expected, splitCsvLine(row));
    }

    @Test
    public void sanitizeAndWrapValue_null_returnsEmptyQuotes() {
        assertEquals("\"\"", sanitizeAndWrapValue(null));
    }

    @Test
    public void unwrapValue_nullOrEmpty_returnsEmptyString() {
        assertEquals("", unwrapValue(null));
        assertEquals("", unwrapValue(""));
    }

    @Test
    public void splitCsvLine_null_returnsEmptyArray() {
        assertEquals(0, splitCsvLine(null).length);
    }
}
