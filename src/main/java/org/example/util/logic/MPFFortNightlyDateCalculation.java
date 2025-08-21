package org.example.util.logic;

import org.example.dto.ContributionPeriod;
import org.example.dto.ContributionPeriodMore;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/***
 * FORTNIGHTLY CALCULATION
 */
public class MPFFortNightlyDateCalculation extends MPFPayrollDateCalculatorLogic {
    private LocalDate fnightStartDate;

    public MPFFortNightlyDateCalculation setFnightStartDate(LocalDate fnightStartDate) {
        this.fnightStartDate = fnightStartDate;
        return this;
    }

    @Override
    void validateCustomized(){
        if (fnightStartDate == null) {
            throw new IllegalArgumentException("fnightStartDate is null");
        }
    }

    public MPFFortNightlyDateCalculation(List<LocalDate> publicHolidays) {
        super(publicHolidays);
    }

    /**
     * Calculate contribution periods for Fortnightly payroll frequency
     */
    @Override
    ContributionPeriodMore calDate() {
        List<ContributionPeriod> periods = new ArrayList<>();
        List<ContributionPeriod> morePeriods = new ArrayList<>();
        LocalDate currentStart = dateOfEmployment;
        LocalDate adjustedFnightStart = fnightStartDate;
        int periodIndex = 0;

        long breaker = morePeriod.isPresent()? morePeriod.getAsLong(): 0;

        LocalDate age18Date = getThe18thAgeDate();

        adjustedFnightStart = currentStart;
        if (age18Date.isAfter(currentStart)) {
            adjustedFnightStart = age18Date;
        }

        // Adjust fortnightly start date to be before or equal to adjusted start
        while (fnightStartDate.isAfter(adjustedFnightStart)) {
            fnightStartDate = fnightStartDate.minusDays(14);
        }

        // Handle different scenarios
        if (
                fnightStartDate.isAfter(adjustedFnightStart) ||
                dateUtils.isSameDate(fnightStartDate, adjustedFnightStart)
        ) {
            // Simple case: single period
            periods.add(new ContributionPeriod(adjustedFnightStart, fnightStartDate));
        } else {
            // Complex case: multiple fortnightly periods
            boolean continueLoop = true;
            LocalDate periodStart = fnightStartDate;

            while (continueLoop) {
                LocalDate periodEnd = periodStart.plusDays(13);

                // Adjust first period if employment starts after period start
                if (periodIndex == 0 &&
                        (
                                currentStart.isAfter(fnightStartDate) ||
                                currentStart.equals(fnightStartDate)
                        )
                ) {
                        periodStart = currentStart;
                }


                periods.add(new ContributionPeriod(periodStart, periodEnd));
                setAge65WithinPeriod(periodStart, periodEnd);

                periodIndex++;
                periodStart = periodEnd.plusDays(1);

                if (
                        periodEnd.isAfter(deadlineForEnrol) ||
                        periodEnd.equals(deadlineForEnrol)
                ) {
                    // Handle final period if it extends beyond deadline
                    if (
                            dateUtils.isPubHoliday(deadlineForEnrol) &&
                                    dateUtils.isSameDate(periodEnd, deadlineForEnrol)
                    ) {
                        LocalDate finalPeriodEnd = periodStart.plusDays(13);
                        periods.add(new ContributionPeriod(periodStart, finalPeriodEnd));
                    }
                    continueLoop = false;
                }
            }

            for (int i =0; i < breaker; i++){
                LocalDate additionalPeriodEnd = periodStart.plusDays(13);
                morePeriods.add(new ContributionPeriod(periodStart, additionalPeriodEnd));
                periodStart = additionalPeriodEnd.plusDays(1);
            }
        }

        // Set first period start date flag
        if (!periods.isEmpty()) {
            ContributionPeriod firstPeriod = periods.get(0);
            setThe1stPeriodStartDate(firstPeriod.getStartDate().equals(currentStart));

            // Adjust first period start if employment date is different
            if (currentStart.isAfter(fnightStartDate)) {
                periods.set(0, new ContributionPeriod(currentStart, firstPeriod.getEndDate()));
            }
        }

        return new ContributionPeriodMore(
                periods,
                morePeriods
        );
    }

    /**
     * Calculate last contribution period for Fortnightly
     */
    @Override
    public ContributionPeriod calTheLastPeriod(LocalDate lastDOE) {
        // Find the fortnightly period that contains the last DOE
        LocalDate periodStart = fnightStartDate;

        // Move backward to find the period containing lastDOE
        while (periodStart.isAfter(lastDOE)) {
            periodStart = periodStart.minusDays(14);
        }

        // Move forward to find the correct period
        while (periodStart.plusDays(13).isBefore(lastDOE)) {
            periodStart = periodStart.plusDays(14);
        }

        return new ContributionPeriod(periodStart, lastDOE);
    }
}
