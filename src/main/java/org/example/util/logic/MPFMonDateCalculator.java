package org.example.util.logic;

import org.example.dto.ContributionPeriod;
import org.example.dto.ContributionPeriodMore;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/***
 * CALENDAR MONTH CALCULATION
 */
public class MPFMonDateCalculator extends MPFPayrollDateCalculatorLogic{
    public MPFMonDateCalculator(List<LocalDate> publicHolidays) {
        super(publicHolidays);
    }

    @Override
    void validateCustomized(){}

    /**
     * Calculate contribution periods for Calendar Month payroll frequency
     */
    @Override
    public ContributionPeriodMore calDate() {
        List<ContributionPeriod> periods = new ArrayList<>();
        List<ContributionPeriod> morePeriods = new ArrayList<>(
                morePeriod.isPresent()?
                        (int) morePeriod.getAsLong() :
                        0
        );
        boolean continueLoop = true;
        long totalDays = 0;
        int morePeriodIndex = 0;

        long breaker = morePeriod.isPresent()? morePeriod.getAsLong(): 0;

        LocalDate currentStart = dateOfEmployment;
        LocalDate adjustedStart = currentStart;

        // Handle 18th age adjustment
        LocalDate age18Date = getThe18thAgeDate();
        if (age18Date.isAfter(adjustedStart)) {
            adjustedStart = getThe18thAgePeriodsStartForCalMonth(age18Date, currentStart);
            if (adjustedStart.isAfter(currentStart)) {
                totalDays += dateUtils.getDateCount(adjustedStart, currentStart);
            }
        }

        long totalDaysToDeadline = dateUtils.getDateCount(adjustedStart, deadlineForEnrol);

        // Set first period start date flag
        setThe1stPeriodStartDate(adjustedStart.getDayOfMonth() == 1);

        while (continueLoop) {
            LocalDate periodEnd = dateUtils.getLastDateOfMonth(adjustedStart);

            // calculate the period
            ContributionPeriod period = new ContributionPeriod(adjustedStart, periodEnd);

            // Check age 65 within this period
            setAge65WithinPeriod(adjustedStart, periodEnd);

            totalDays += dateUtils.getDateCount(adjustedStart, periodEnd);

            adjustedStart = dateUtils.getDateAfter(1, periodEnd);

            // record the extract periods data
            if (morePeriodIndex < breaker &&
                    totalDays >= totalDaysToDeadline) {
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

        return new ContributionPeriodMore(
                periods,
                morePeriods
        );
    }

    /**
     * Get 18th age period start for calendar month
     */
    protected LocalDate getThe18thAgePeriodsStartForCalMonth(LocalDate age18Date, LocalDate employmentDate) {
        LocalDate firstOfMonth = LocalDate.of(age18Date.getYear(), age18Date.getMonth(), 1);
        return employmentDate.isAfter(firstOfMonth) ? employmentDate : firstOfMonth;
    }

    /**
     * Calculate last contribution period for Calendar Month
     */
    @Override
    public ContributionPeriod calTheLastPeriod(LocalDate lastDOE) {
        LocalDate periodStart = LocalDate.of(lastDOE.getYear(), lastDOE.getMonth(), 1);
        return new ContributionPeriod(periodStart, lastDOE);
    }
}
