package org.example.util.intf;

import org.example.dto.ContributionPeriod;
import org.example.dto.ContributionPeriodMore;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public interface MPFPayrollLogger {

    default void printNonPayPeriods(ContributionPeriodMore contributionPeriod) {
        printPeriods(contributionPeriod.getPeriod());
    }

    default void printPayPeriods(ContributionPeriodMore contributionPeriod) {
        printPeriods(contributionPeriod.getPeriodMore());
    }

    /**
     * print the period out
     */
     default void printPeriods(List<ContributionPeriod> periods) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (int i = 0; i < periods.size(); i++) {  // Show all periods
            ContributionPeriod period = periods.get(i);
            System.out.println("Period " + (i + 1) + ": " +
                    period.getStartDate().format(formatter) + " to " +
                    period.getEndDate().format(formatter));
        }

        System.out.println("Total periods: " + periods.size());
    }

    /**
     * from LocalDate to string
     * @param date
     * @return
     */
    default String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
