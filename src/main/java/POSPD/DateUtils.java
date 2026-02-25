package POSPD;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;

public class DateUtils {

    // Formatter that accepts M/d/yy or M/d/yyyy etc.
    private static final DateTimeFormatter FLEXIBLE_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("[M/d/yyyy][M/d/yy]")
            .parseDefaulting(ChronoField.ERA, 1)
            .toFormatter();

    public static LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, FLEXIBLE_FORMATTER);
        } catch (DateTimeParseException e) {
            // Fallback to strict M/d/yy format matching original logic if the string has 2
            // digits but didn't parse clean
            if (dateStr.split("/")[2].length() == 2) {
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("M/d/yy"));
            }
            throw e; // re-throw if it honestly fails both
        }
    }
}
