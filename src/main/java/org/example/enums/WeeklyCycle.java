package org.example.enums;

/**
 * Weekly Cycle Types for Weekly Payroll Frequency
 */
public enum WeeklyCycle {
    MONDAY_TO_SUNDAY(1, "Monday to Sunday"),
    TUESDAY_TO_MONDAY(2, "Tuesday to Monday"),
    WEDNESDAY_TO_TUESDAY(3, "Wednesday to Tuesday"),
    THURSDAY_TO_WEDNESDAY(4, "Thursday to Wednesday"),
    FRIDAY_TO_THURSDAY(5, "Friday to Thursday"),
    SATURDAY_TO_FRIDAY(6, "Saturday to Friday"),
    SUNDAY_TO_SATURDAY(7, "Sunday to Saturday");

    private final int value;
    private final String description;

    WeeklyCycle(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static WeeklyCycle fromValue(int value) {
        for (WeeklyCycle cycle : values()) {
            if (cycle.value == value) {
                return cycle;
            }
        }
        throw new IllegalArgumentException("Invalid weekly cycle value: " + value);
    }
}
