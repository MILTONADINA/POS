package POSPD;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;

/**
 * Centralized parsing and formatting for the {@code M/d/yyyy} (and legacy {@code M/d/yy}) date
 * format used throughout the application and its CSV persistence. Keeping every date conversion
 * here means the on-disk format is defined in exactly one place.
 */
public final class DateUtils {

    /** Canonical output format for dates written to disk and shown in reports. */
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("M/d/yyyy");

    /** Lenient input format that accepts both 4-digit and 2-digit years. */
    private static final DateTimeFormatter FLEXIBLE_FORMATTER =
            new DateTimeFormatterBuilder()
                    .appendPattern("[M/d/yyyy][M/d/yy]")
                    .parseDefaulting(ChronoField.ERA, 1)
                    .toFormatter();

    private DateUtils() {
        // Utility class; not instantiable.
    }

    /**
     * Parses a date string in {@code M/d/yyyy} or {@code M/d/yy} form.
     *
     * @param dateStr the date string
     * @return the parsed {@link LocalDate}
     * @throws DateTimeParseException if the string is not a recognized date
     */
    public static LocalDate parseDate(String dateStr) {
        return LocalDate.parse(dateStr, FLEXIBLE_FORMATTER);
    }

    /**
     * Formats a date using the canonical {@link #DATE_FORMAT}.
     *
     * @param date the date to format
     * @return the formatted string
     */
    public static String format(LocalDate date) {
        return date.format(DATE_FORMAT);
    }
}
