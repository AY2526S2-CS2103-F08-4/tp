package seedu.address.commons.util;

/**
 * Utility methods for CSV files.
 */
public class CsvUtil {

    /**
     * Sanitizes a string for CSV storage by escaping internal double quotes and
     * wrapping the entire result in double quotes to prevent column shifting.
     *
     * @param value The raw string value to be sanitized.
     * @return A CSV-safe string wrapped in double quotes; returns {@code ""} if null.
     */
    public static String sanitizeAndWrapValue(String value) {
        if (value == null) {
            return "\"\"";
        }

        String escapedQuotes = value.replace("\"", "\"\"");

        return "\"" + escapedQuotes + "\"";
    }

    /**
     * Reverses the CSV sanitization process by stripping leading and trailing double quotes
     * and converting escaped internal double quotes ("") back into single double quotes (").
     *
     * @param value The sanitized string from a CSV row.
     * @return The original raw string value; returns an empty string if the input is null.
     */
    public static String unwrapValue(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        // Remove leading and trailing quotes
        String unwrapped = value.replaceAll("^\"|\"$", "");

        // Restore double quotes
        return unwrapped.replace("\"\"", "\"");
    }

    /**
     * Splits a raw CSV line into individual column values using a comma delimiter.
     * This method uses a positive lookahead regex to ensure commas located inside
     * double-quoted fields (like addresses) are not used as split points.
     *
     * @param line The full CSV row string to be split.
     * @return An array of strings representing each column; returns an empty array if input is null.
     */
    public static String[] splitCsvLine(String line) {
        if (line == null) {
            return new String[0];
        }

        return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    }
}
