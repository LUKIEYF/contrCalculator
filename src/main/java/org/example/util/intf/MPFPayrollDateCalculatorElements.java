package org.example.util.intf;

import java.time.LocalDate;

public interface MPFPayrollDateCalculatorElements {
    MPFPayrollDateCalculatorElements setDateOfEmployment(LocalDate dateOfEmployment);
    MPFPayrollDateCalculatorElements setDateOfBirth(LocalDate dateOfBirth);
    MPFPayrollDateCalculatorElements setDeadlineForEnrol(LocalDate deadlineForEnrol);
    MPFPayrollDateCalculatorElements setEndOfEmployment(LocalDate endOfEmployment);
    void setThe1stPeriodStartDate(boolean isFirst);
    void setAge65Within1stPeriod(boolean isWithin);
    LocalDate getDateOfEmployment();
    LocalDate getDateOfBirth();
    LocalDate getEndOfEmployment();
    LocalDate getThe18thAgeDate();
    LocalDate getThe65thAgeDate();
    LocalDate getThe30thDOE();
    LocalDate getThe31stDOE();
    LocalDate getThe60thDOE();
    LocalDate getDeadlineForEnrol();
}
