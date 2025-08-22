package org.example.util.logic;

import org.example.util.intf.MPFPayrollDateCalculatorElements;

import java.time.LocalDate;
import java.util.List;

/**
 * MPF Payroll Date Calculator for elements injection and fundamental calculation for various elements
 * that the calculation of period needed.
 */
abstract class MPFPayrollDateCalculatorBase implements MPFPayrollDateCalculatorElements {
    
    protected final DateUtils dateUtils;
    protected LocalDate dateOfEmployment;
    protected LocalDate dateOfBirth;
    protected LocalDate deadlineForEnrol;
    protected LocalDate deadlineFor30Exemption;
    protected boolean isThe1stPeriodStartDate;
    protected boolean age65Within1stPeriod;
    protected LocalDate endOfEmployment;
    
    public MPFPayrollDateCalculatorBase(List<LocalDate> publicHolidays) {
        this.dateUtils = new DateUtils(publicHolidays);
    }
    
    // Setters for configuration
    public MPFPayrollDateCalculatorBase setDateOfEmployment(LocalDate dateOfEmployment) {
        this.dateOfEmployment = dateOfEmployment;
        return this;
    }
    
    public MPFPayrollDateCalculatorBase setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        return this;
    }
    
    public MPFPayrollDateCalculatorBase setDeadlineForEnrol(LocalDate deadlineForEnrol) {
        this.deadlineForEnrol = deadlineForEnrol;
        return this;
    }

    public MPFPayrollDateCalculatorBase setEndOfEmployment(LocalDate endOfEmployment) {
        this.endOfEmployment = endOfEmployment;
        return this;
    }
    
    // Getters
    public LocalDate getDateOfEmployment() {
        return dateOfEmployment;
    }
    
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public LocalDate getEndOfEmployment() {
        return endOfEmployment;
    }

    public LocalDate getDeadlineFor30Exemption(){
        return deadlineFor30Exemption;
    }
    
    /**
     * Get the 18th birthday date
     */
    public LocalDate getThe18thAgeDate() {
        int eeAge = dateUtils.getAge(dateOfBirth, dateOfEmployment);
        int yearDiff = eeAge - 18;
        int targetYear = dateOfEmployment.getYear() - yearDiff;
        
        LocalDate candidate = LocalDate.of(targetYear, dateOfBirth.getMonth(), dateOfBirth.getDayOfMonth());
        
        if (dateUtils.getAge(dateOfBirth, candidate) > 18) {
            return LocalDate.of(targetYear - 1, dateOfBirth.getMonth(), dateOfBirth.getDayOfMonth());
        }
        return candidate;
    }
    
    /**
     * Get the 65th birthday date
     */
    public LocalDate getThe65thAgeDate() {
        int eeAge = dateUtils.getAge(dateOfBirth, dateOfEmployment);
        int yearDiff = eeAge - 65;
        int targetYear = dateOfEmployment.getYear() - yearDiff;
        
        return LocalDate.of(targetYear, dateOfBirth.getMonth(), dateOfBirth.getDayOfMonth());
    }

    /**
     * Get the 30th day after employment
     */
    public LocalDate getThe30thDOE(){
        return dateOfEmployment.plusDays(29);
    }

    /**
     * Get the 31st day after employment
     */
    public LocalDate getThe31stDOE(){
        return dateOfEmployment.plusDays(30);
    }

    /**
     * Get the 60th day after employment
     */
    public LocalDate getThe60thDOE() {
        return dateOfEmployment.plusDays(59);
    }

    /**
     * Get deadline of enrollment
     */
    public LocalDate getDeadlineForEnrol() {
        return deadlineForEnrol;
    }

    /**
     * Set first period start date flag
     */
    public void setThe1stPeriodStartDate(boolean isFirst) {
        this.isThe1stPeriodStartDate = isFirst;
    }

    /**
     * Set age 65 within period flag
     */
    public void setAge65Within1stPeriod(boolean isWithin) {
        this.age65Within1stPeriod = isWithin;
    }
    
    /**
     * Check if age 65 falls within a period
     */
    public void setAge65WithinPeriod(LocalDate periodStart, LocalDate periodEnd) {
        LocalDate age65Date = getThe65thAgeDate();
        setAge65Within1stPeriod(dateUtils.isBetween(age65Date, periodStart, periodEnd));
    }
}
