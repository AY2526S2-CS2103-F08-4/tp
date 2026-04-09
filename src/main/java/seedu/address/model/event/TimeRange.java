package seedu.address.model.event;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Objects;

/**
 * Represents a required start and end date-time range for an Event.
 * Guarantees: immutable; end is strictly after start.
 */
public class TimeRange {
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HHmm";
    public static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HHmm").withResolverStyle(ResolverStyle.STRICT);
    public static final String MESSAGE_INVALID_DATETIME_FORMAT =
            "Invalid date/time format. Expected: " + DATE_TIME_PATTERN + " (e.g. 2026-03-25 0900).";
    public static final String MESSAGE_END_NOT_AFTER_START =
            "End time must be strictly after start time.";
    public static final String MESSAGE_CONSTRAINTS =
            MESSAGE_INVALID_DATETIME_FORMAT + " " + MESSAGE_END_NOT_AFTER_START;
    public static final String DATE_TIME_REGEX = "^\\d{4}-\\d{2}-\\d{2} \\d{4}$"; // Regex for YYYY-MM-DD-HHmm format
    public static final String MESSAGE_INVALID_DATE_VALUE = "The provided date or time does not exist.";

    private final LocalDateTime startTime;
    private final LocalDateTime endTime;

    /**
     * Construct a {@code TimeRange}
     * @param startTimeStr A string representation of the start time
     * @param endTimeStr A string representation of the end time
     */
    public TimeRange(String startTimeStr, String endTimeStr) {
        requireNonNull(startTimeStr);
        requireNonNull(endTimeStr);

        // Check for Format Syntax
        checkArgument(isValidSyntax(startTimeStr) && isValidSyntax(endTimeStr),
                MESSAGE_INVALID_DATETIME_FORMAT);

        // Check if provided values exists
        checkArgument(isValidDateValue(startTimeStr) && isValidDateValue(endTimeStr),
                MESSAGE_INVALID_DATE_VALUE);

        checkArgument(isValidTimeRange(startTimeStr, endTimeStr), MESSAGE_END_NOT_AFTER_START);
        this.startTime = LocalDateTime.parse(startTimeStr, DATE_TIME_FORMATTER);
        this.endTime = LocalDateTime.parse(endTimeStr, DATE_TIME_FORMATTER);
    }

    /**
     * Returns true if the string matches the YYYY-MM-DD HHmm
     */
    public static boolean isValidSyntax(String dateTimeStr) {
        return dateTimeStr.matches(DATE_TIME_REGEX);
    }

    /**
     * Returns true if {@code dateTimeStr} can be parsed according to {@code DATE_TIME_PATTERN}.
     */
    public static boolean isValidDateValue(String dateTimeStr) {
        try {
            LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Returns true if {@code dateTimeStr} is fully valid (both syntax and calendar value).
     */
    public static boolean isValidDateTimeFormat(String dateTimeStr) {
        return isValidSyntax(dateTimeStr) && isValidDateValue(dateTimeStr);
    }

    private static boolean isValidTimeRange(String startTimeStr, String endTimeStr) {
        try {
            LocalDateTime startTime = LocalDateTime.parse(startTimeStr, DATE_TIME_FORMATTER);
            LocalDateTime endTime = LocalDateTime.parse(endTimeStr, DATE_TIME_FORMATTER);
            return endTime.isAfter(startTime);
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Returns the start time.
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }

    /**
     * Returns the end time.
     */
    public LocalDateTime getEndTime() {
        return endTime;
    }

    /**
     * Returns the start time formatted using {@link #DATE_TIME_FORMATTER}.
     */
    public String getStartTimeFormatted() {
        return startTime.format(DATE_TIME_FORMATTER);
    }

    /**
     * Returns the end time formatted using {@link #DATE_TIME_FORMATTER}.
     */
    public String getEndTimeFormatted() {
        return endTime.format(DATE_TIME_FORMATTER);
    }

    /**
     * Returns true if both timeRanges of an event are overlapping
     * @param other the reference timeRange object of another event
     * @return
     */
    public boolean isOverlapping(TimeRange other) {
        return startTime.isBefore(other.endTime) && other.startTime.isBefore(endTime);
    }

    /**
     * Returns true if this time range starts at {@code otherStartTime}.
     */
    public boolean hasSameStartTime(LocalDateTime otherStartTime) {
        requireNonNull(otherStartTime);
        return startTime.equals(otherStartTime);
    }

    /**
     * Returns a human-readable representation of this time range.
     */
    @Override
    public String toString() {
        return String.format("%s to %s", getStartTimeFormatted(), getEndTimeFormatted());
    }

    /**
     * Return true if both timeRange is the same
     * @param other   the reference object with which to compare.
     * @return
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof TimeRange otherTimeRange) {
            return startTime.equals(otherTimeRange.startTime)
                    && endTime.equals(otherTimeRange.endTime);
        }
        return false;
    }

    /**
     * Returns the hash code of this time range.
     */
    @Override
    public int hashCode() {
        return Objects.hash(startTime, endTime);
    }
}
