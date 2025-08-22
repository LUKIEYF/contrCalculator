package org.example.util.logic;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.OptionalLong;

/**
 * Configuration class to hold all the necessary parameters for MPF calculator logic instances
 */
public class MPFDateCalculatorConfig {
    private LocalDate dateOfBirth;
    private LocalDate dateOfEmployment;
    private LocalDate endOfEmployment;
    private LocalDate deadlineForEnrol;
    private OptionalLong morePeriod = OptionalLong.empty();
    
    // For weekly calculations
    private DayOfWeek weeklyCycle;
    
    // For fortnightly calculations  
    private LocalDate pryllFnightStartDate;
    
    // For non-calendar month calculations
    private Integer nonCalStartDay;

    // Constructors
    public MPFDateCalculatorConfig() {}

    public MPFDateCalculatorConfig(LocalDate dateOfBirth, LocalDate dateOfEmployment) {
        this.dateOfBirth = dateOfBirth;
        this.dateOfEmployment = dateOfEmployment;
    }

    // Getters and setters
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public MPFDateCalculatorConfig setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        return this;
    }

    public LocalDate getDateOfEmployment() {
        return dateOfEmployment;
    }

    public MPFDateCalculatorConfig setDateOfEmployment(LocalDate dateOfEmployment) {
        this.dateOfEmployment = dateOfEmployment;
        return this;
    }

    public LocalDate getEndOfEmployment() {
        return endOfEmployment;
    }

    public MPFDateCalculatorConfig setEndOfEmployment(LocalDate endOfEmployment) {
        this.endOfEmployment = endOfEmployment;
        return this;
    }

    public LocalDate getDeadlineForEnrol() {
        return deadlineForEnrol;
    }

    public MPFDateCalculatorConfig setDeadlineForEnrol(LocalDate deadlineForEnrol) {
        this.deadlineForEnrol = deadlineForEnrol;
        return this;
    }

    public OptionalLong getMorePeriod() {
        return morePeriod;
    }

    public MPFDateCalculatorConfig setMorePeriod(OptionalLong morePeriod) {
        this.morePeriod = morePeriod;
        return this;
    }

    public DayOfWeek getWeeklyCycle() {
        return weeklyCycle;
    }

    public MPFDateCalculatorConfig setWeeklyCycle(DayOfWeek weeklyCycle) {
        this.weeklyCycle = weeklyCycle;
        return this;
    }

    public LocalDate getPryllFnightStartDate() {
        return pryllFnightStartDate;
    }

    public MPFDateCalculatorConfig setPryllFnightStartDate(LocalDate pryllFnightStartDate) {
        this.pryllFnightStartDate = pryllFnightStartDate;
        return this;
    }

    public Integer getNonCalStartDay() {
        return nonCalStartDay;
    }

    public MPFDateCalculatorConfig setNonCalStartDay(Integer nonCalStartDay) {
        this.nonCalStartDay = nonCalStartDay;
        return this;
    }
}
