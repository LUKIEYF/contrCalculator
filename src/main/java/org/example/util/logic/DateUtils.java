package org.example.util.logic;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Date utility methods for MPF contribution calculations
 */
public class DateUtils {
    
    private final List<LocalDate> publicHolidays;
    
    public DateUtils(List<LocalDate> publicHolidays) {
        this.publicHolidays = publicHolidays != null ? publicHolidays : List.of();
    }
    
    /**
     * Get the last date of the month
     */
    public LocalDate getLastDateOfMonth(LocalDate date) {
        return date.withDayOfMonth(date.lengthOfMonth());
    }
    
    /**
     * Add days to a date
     */
    public LocalDate getDateAfter(int days, LocalDate date) {
        return date.plusDays(days);
    }
    
    /**
     * Calculate the number of days between two dates (inclusive)
     */
    public long getDateCount(LocalDate startDate, LocalDate endDate) {
        return Math.abs(ChronoUnit.DAYS.between(startDate, endDate)) + 1;
    }
    
    /**
     * Calculate the difference between two dates in days
     */
    public long getDateDiff(LocalDate startDate, LocalDate endDate) {
        return Math.abs(ChronoUnit.DAYS.between(startDate, endDate));
    }
    
    /**
     * Check if two dates are the same
     */
    public boolean isSameDate(LocalDate date1, LocalDate date2) {
        return date1.equals(date2);
    }
    
    /**
     * Check if a year is a leap year
     */
    public boolean isLeapYear(LocalDate date) {
        return date.isLeapYear();
    }

    /**
     * Get larger date
     */
    public LocalDate getLargerDate(LocalDate d1, LocalDate d2) {
        if (d1.isAfter(d2)) {
            return d1;
        }
        return d2;
    }

    /**
     * Get the coming date by specific day of week
     * @param startDate starting date
     * @param dayOfWeek target day (0=Sunday, 1=Monday, ..., 6=Saturday)
     */
    public LocalDate getCommingDateByDay(LocalDate startDate, DayOfWeek dayOfWeek) {
        LocalDate current = startDate;
        for (int i = 0; i < 8; i++) {
            if (current.getDayOfWeek() == dayOfWeek) {
                return current;
            }
            current = current.plusDays(1);
        }
        return current;
    }
    
    /**
     * Check if a date is a public holiday
     */
    public boolean isPubHoliday(LocalDate date) {
        return publicHolidays.contains(date);
    }

    /**
     * Get the next date that is not a public holiday or Saturday or Sunday
     */
    public LocalDate getDateAfterPublicHolidayAndWeekend(LocalDate date) {
        LocalDate current = date;
        while (isPubHoliday(current) ||
                current.getDayOfWeek() == DayOfWeek.SATURDAY ||
                current.getDayOfWeek() == DayOfWeek.SUNDAY
        ) {
            current = current.plusDays(1);
        }
        return current;
    }

    /**
     * Get the next date that is not a public holiday or Saturday
     */
    public LocalDate getDateAfterPubHolidayOrSat(LocalDate date) {
        LocalDate current = date;
        while (isPubHoliday(current) || current.getDayOfWeek() == DayOfWeek.SATURDAY) {
            current = current.plusDays(1);
        }
        return current;
    }
    
    /**
     * Get the next date that is not a public holiday
     */
    public LocalDate getDateAfterPubHoliday(LocalDate date) {
        LocalDate current = date;
        while (isPubHoliday(current)) {
            current = current.plusDays(1);
        }
        return current;
    }
    
    /**
     * Calculate age between two dates
     */
    public int getAge(LocalDate birthDate, LocalDate currentDate) {
        if (birthDate.getYear() == currentDate.getYear()) {
            return 0;
        }
        
        if (birthDate.getMonthValue() == currentDate.getMonthValue()) {
            return birthDate.getDayOfMonth() > currentDate.getDayOfMonth() 
                ? currentDate.getYear() - birthDate.getYear() - 1 
                : currentDate.getYear() - birthDate.getYear();
        }
        
        return birthDate.getMonthValue() > currentDate.getMonthValue() 
            ? currentDate.getYear() - birthDate.getYear() - 1 
            : currentDate.getYear() - birthDate.getYear();
    }
    
    /**
     * Check if an age date falls within a period
     */
    public boolean isBetween(LocalDate ageDate, LocalDate periodStart, LocalDate periodEnd) {
        return (ageDate.equals(periodStart) || ageDate.equals(periodEnd) || 
                (ageDate.isAfter(periodStart) && ageDate.isBefore(periodEnd)));
    }

}
