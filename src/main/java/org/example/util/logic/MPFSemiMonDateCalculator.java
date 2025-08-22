package org.example.util.logic;

import org.example.dto.ContributionPeriod;
import org.example.dto.ContributionPeriodMore;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/***
 * SEMI-MONTHLY CALCULATION
 */
public class MPFSemiMonDateCalculator extends MPFPayrollDateCalculatorLogic{
    public MPFSemiMonDateCalculator(List<LocalDate> publicHolidays) {
        super(publicHolidays);
    }

    @Override
    void validateCustomized(){}

    /**
     * Calculate contribution periods for Semi-Monthly payroll frequency
     */
    @Override
    ContributionPeriodMore calDate() {
        List<ContributionPeriod> periods = new ArrayList<>();
        List<ContributionPeriod> morePeriods = new ArrayList<>();
        boolean continueLoop = true;
        long totalDays = 0;
        int morePeriodIndex = 0;

        long breaker = morePeriod.isPresent()? morePeriod.getAsLong(): 0;

        LocalDate currentStart = dateOfEmployment;
        LocalDate adjustedStart = currentStart;

        // Handle 18th age adjustment
        LocalDate age18Date = getThe18thAgeDate();
        if (age18Date.isAfter(adjustedStart)) {
            adjustedStart = getThe18thAgePeriodsStartForSemiMonth(age18Date, currentStart);
            if (adjustedStart.isAfter(currentStart)) {
                totalDays += dateUtils.getDateCount(adjustedStart, currentStart);
            }
        }

//        long totalDaysToDeadline = dateUtils.getDateCount(adjustedStart, deadlineForEnrol);
        long totalDaysToDeadline = dateUtils.getDateCount(adjustedStart, getDeadlineFor30Exemption());

        // Set first period start date flag
        setThe1stPeriodStartDate(adjustedStart.getDayOfMonth() == 1 || adjustedStart.getDayOfMonth() == 16);

        while (continueLoop) {
            LocalDate periodEnd = getCycleEndForSemiMonth(adjustedStart);

            ContributionPeriod period = new ContributionPeriod(adjustedStart, periodEnd);

            // Check age 65 within this period
            setAge65WithinPeriod(adjustedStart, periodEnd);

            totalDays += dateUtils.getDateCount(adjustedStart, periodEnd);

            adjustedStart = dateUtils.getDateAfter(1, periodEnd);

            // record the extract periods data
            if (morePeriodIndex < breaker &&
                    totalDays >= totalDaysToDeadline &&
                    !dateUtils.isBetween(getDeadlineFor30Exemption(), period.getStartDate(), period.getEndDate())
            ) {
                morePeriods.add(period); // employee non-pay period
                morePeriodIndex++;
            }else{
                periods.add(period); // employee pay period
            }

            // stop iteration
            if (morePeriodIndex >= breaker && totalDays >= totalDaysToDeadline) {
                continueLoop = false;
            }
        }

        return new ContributionPeriodMore(periods, morePeriods);
    }

    /**
     * Get 18th age period start for semi-monthly
     */
    private LocalDate getThe18thAgePeriodsStartForSemiMonth(LocalDate age18Date, LocalDate employmentDate) {
        LocalDate candidate = age18Date.getDayOfMonth() < 16
                ? LocalDate.of(age18Date.getYear(), age18Date.getMonth(), 1)
                : LocalDate.of(age18Date.getYear(), age18Date.getMonth(), 16);
        return employmentDate.isAfter(candidate) ? employmentDate : candidate;
    }

    /**
     * Get cycle end date for semi-monthly
     * @param startDate the starting date in the month of employment
     */
    private LocalDate getCycleEndForSemiMonth(LocalDate startDate) {
        return startDate.getDayOfMonth() < 16
                ? LocalDate.of(startDate.getYear(), startDate.getMonth(), 15)
                : dateUtils.getLastDateOfMonth(startDate);
    }

    /**
     * Calculate last contribution period for Semi-Monthly
     * @param lastDOE the last date of employment in the month of termination of employment
     */
    @Override
    public ContributionPeriod calTheLastPeriod(LocalDate lastDOE) {
        LocalDate periodStart;

        if (lastDOE.getDayOfMonth() == 16 || lastDOE.getDayOfMonth() == 1) {
            periodStart = lastDOE;
        } else if (lastDOE.getDayOfMonth() < 16) {
            periodStart = LocalDate.of(lastDOE.getYear(), lastDOE.getMonth(), 1);
        } else {
            periodStart = LocalDate.of(lastDOE.getYear(), lastDOE.getMonth(), 16);
        }

        return new ContributionPeriod(periodStart, lastDOE);
    }
}
