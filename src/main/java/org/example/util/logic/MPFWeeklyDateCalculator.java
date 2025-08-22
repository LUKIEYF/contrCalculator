package org.example.util.logic;

import org.example.dto.ContributionPeriod;
import org.example.dto.ContributionPeriodMore;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/***
 * WEEKLY CALCULATION
 */
public class MPFWeeklyDateCalculator extends MPFPayrollDateCalculatorLogic {
    private DayOfWeek cycle;

    public MPFWeeklyDateCalculator(List<LocalDate> publicHolidays) {
        super(publicHolidays);
    }

    public MPFWeeklyDateCalculator setWeeklyCycle(DayOfWeek cycle) {
        this.cycle = cycle;
        return this;
    }

    @Override
    void validateCustomized() {
        if (cycle == null) {
            throw new IllegalArgumentException("WeeklyCycle is null");
        }
    }

    /**
     * Calculate contribution periods for Weekly payroll frequency
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
            adjustedStart = getThe18thAgePeriodsStartForWeekly(age18Date, currentStart, cycle);
            if (adjustedStart.isAfter(currentStart)) {
                totalDays += dateUtils.getDateCount(adjustedStart, currentStart);
            }
        }

        long totalDaysToDeadline = dateUtils.getDateCount(adjustedStart, getDeadlineFor30Exemption());

        // Set first period start date flag
        // Check if start date aligns with the weekly cycle (cycle.getValue() + 1 because JS uses 0-6, Java uses 1-7)
        int expectedDayOfWeek = cycle.getValue() % 7; // Convert to Java's 1-7 system
        if (expectedDayOfWeek == 0) expectedDayOfWeek = 7; // Sunday
        setThe1stPeriodStartDate(adjustedStart.getDayOfWeek().getValue() == expectedDayOfWeek);

        while (continueLoop) {
            LocalDate periodEnd = dateUtils.getCommingDateByDay(adjustedStart, cycle);

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
     * Get 18th age period start for weekly
     */
    private LocalDate getThe18thAgePeriodsStartForWeekly(LocalDate age18Date, LocalDate employmentDate, DayOfWeek weeklyCycle) {
        LocalDate candidate = dateUtils.getCommingDateByDay(age18Date.minusDays(7), weeklyCycle);
        return employmentDate.isAfter(candidate) ? employmentDate : candidate;
    }

    /**
     * Calculate last contribution period for Weekly
     */
    @Override
    public ContributionPeriod calTheLastPeriod(LocalDate lastDOE) {
        // Find the weekly period that contains the last DOE
        LocalDate periodStart = lastDOE;

        // Move backward to find the start of the week containing lastDOE
        while (periodStart.getDayOfWeek().getValue() != ((cycle.getValue() + 1) % 7)) {
            periodStart = periodStart.minusDays(1);
        }

        return new ContributionPeriod(periodStart, lastDOE);
    }

}
