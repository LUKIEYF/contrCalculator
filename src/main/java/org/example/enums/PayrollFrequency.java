package org.example.enums;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;
import org.example.util.logic.MPFPayrollDateCalculatorLogic;
import org.example.util.logic.MPFMonDateCalculator;
import org.example.util.logic.MPFNonCalendarMonDateCalculator;
import org.example.util.logic.MPFSemiMonDateCalculator;
import org.example.util.logic.MPFFortNightlyDateCalculation;
import org.example.util.logic.MPFWeeklyDateCalculator;

/**
 * Payroll Frequency Types for MPF Contribution Calculation
 * if it needs to improve the performance, change to instance clone in the factory function.
 */
public enum PayrollFrequency {
    MONTHLY(1, "Calendar month", MPFMonDateCalculator::new),
    NON_CALENDAR_MONTH(2, "Non-calendar month", MPFNonCalendarMonDateCalculator::new),
    SEMI_MONTHLY(3, "Semi-monthly", MPFSemiMonDateCalculator::new),
    FORTNIGHTLY(4, "Fortnightly", MPFFortNightlyDateCalculation::new),
    WEEKLY(5, "Weekly", MPFWeeklyDateCalculator::new);

    private final int value;
    private final String description;
    private final Function<List<LocalDate>, MPFPayrollDateCalculatorLogic> calculatorFactory;

    PayrollFrequency(int value, String description, Function<List<LocalDate>, MPFPayrollDateCalculatorLogic> calculatorFactory) {
        this.value = value;
        this.description = description;
        this.calculatorFactory = calculatorFactory;
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static PayrollFrequency fromValue(int value) {
        for (PayrollFrequency frequency : values()) {
            if (frequency.value == value) {
                return frequency;
            }
        }
        throw new IllegalArgumentException("Invalid payroll frequency value: " + value);
    }

    /**
     * Creates and returns the appropriate MPFPayrollDateCalculatorLogic child class instance
     * based on the payroll frequency type using pre-stored factory function.
     * 
     * @param publicHolidays List of public holidays to be used in calculations
     * @return The corresponding calculator instance
     */
    public MPFPayrollDateCalculatorLogic getCalculator(List<LocalDate> publicHolidays) {
        return calculatorFactory.apply(publicHolidays);
    }
}
