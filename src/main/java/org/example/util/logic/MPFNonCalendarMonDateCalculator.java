package org.example.util.logic;

import org.example.dto.ContributionPeriod;
import org.example.dto.ContributionPeriodMore;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/***
 * Non Calendar Month Calculation
 */
public class MPFNonCalendarMonDateCalculator extends MPFPayrollDateCalculatorLogic{
    private int startDay = 0;

    public MPFNonCalendarMonDateCalculator(List<LocalDate> publicHolidays) {
        super(publicHolidays);
    }

    public MPFNonCalendarMonDateCalculator setStartDay(int startDay) {
        this.startDay = startDay;
        return this;
    }

    @Override
    void validateCustomized(){
        if (startDay <= 0 || startDay > 31) {
            throw new IllegalArgumentException("startDay must be between 0 and 31");
        }
    }

    /**
     * Calculate contribution periods for Non-Calendar Month payroll frequency
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
            adjustedStart = getThe18thAgePeriodsStartForNonCal(age18Date, currentStart, startDay);
            if (adjustedStart.isAfter(currentStart)) {
                totalDays += dateUtils.getDateCount(adjustedStart, currentStart);
            }
        }

        long totalDaysToDeadline = dateUtils.getDateCount(adjustedStart, getDeadlineFor30Exemption());
        LocalDate nextCycleStart = dateUtils.getDateAfter(1, getLastCycleDateForNonCal(adjustedStart, startDay + 1));

        // Set first period start date flag
        setThe1stPeriodStartDate(adjustedStart.getDayOfMonth() == nextCycleStart.getDayOfMonth());

        while (continueLoop) {
            LocalDate periodEnd = getLastCycleDateForNonCal(adjustedStart, startDay + 1);

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

        return new ContributionPeriodMore(
                periods,
                morePeriods
        );
    }

    /**
     * Get 18th age period start for non-calendar month
     */
    private LocalDate getThe18thAgePeriodsStartForNonCal(LocalDate age18Date, LocalDate employmentDate, int startDay) {
        LocalDate candidate = age18Date.getDayOfMonth() < startDay
                ? LocalDate.of(age18Date.getYear(), age18Date.getMonth().minus(1), startDay)
                : LocalDate.of(age18Date.getYear(), age18Date.getMonth(), startDay);
        return employmentDate.isAfter(candidate) ? employmentDate : candidate;
    }

    /**
     * Get last cycle date for non-calendar month
     * @param startDate start date within the month
     */
    private LocalDate getLastCycleDateForNonCal(LocalDate startDate, int cycleDay) {
        if (startDate.getDayOfMonth() == cycleDay - 1) {
            return startDate;
        }

        //todo Invalid date 'FEBRUARY 31'
        // Check if the cycle day exists in current month
        LocalDate candidateInCurrentMonth = LocalDate.of(startDate.getYear(), startDate.getMonth(), cycleDay);
        if (!candidateInCurrentMonth.getMonth().equals(startDate.getMonth())) {
            return dateUtils.getLastDateOfMonth(startDate);
        }

        // Calculate next month's cycle date
        LocalDate nextMonth = startDate.getMonth() == java.time.Month.DECEMBER
                ? LocalDate.of(startDate.getYear() + 1, java.time.Month.JANUARY, 1)
                : LocalDate.of(startDate.getYear(), startDate.getMonth().plus(1), 1);

        int targetDay = Math.min(cycleDay - 1, nextMonth.lengthOfMonth());
        LocalDate nextCycleDate = LocalDate.of(nextMonth.getYear(), nextMonth.getMonth(), targetDay);

        // Calculate previous month's cycle date
        LocalDate prevMonth = nextMonth.getMonth() == java.time.Month.JANUARY
                ? LocalDate.of(nextMonth.getYear() - 1, java.time.Month.DECEMBER, 1)
                : LocalDate.of(nextMonth.getYear(), nextMonth.getMonth().minus(1), 1);

        int prevTargetDay = Math.min(cycleDay - 1, prevMonth.lengthOfMonth());
        LocalDate prevCycleDate = LocalDate.of(prevMonth.getYear(), prevMonth.getMonth(), prevTargetDay);

        return startDate.isBefore(prevCycleDate) ? prevCycleDate : nextCycleDate;
    }

    /**
     * Calculate last contribution period for Non-Calendar Month
     * @param lastDOE last date of employment
     */
    @Override
    public ContributionPeriod calTheLastPeriod(LocalDate lastDOE) {
        if (lastDOE.getDayOfMonth() == startDay) {
            return new ContributionPeriod(lastDOE, lastDOE);
        }

        LocalDate prevMonth = lastDOE.getMonth() == java.time.Month.JANUARY
                ? LocalDate.of(lastDOE.getYear() - 1, java.time.Month.DECEMBER, 1)
                : LocalDate.of(lastDOE.getYear(), lastDOE.getMonth().minus(1), 1);

        int adjustedStartDay = Math.min(startDay, prevMonth.lengthOfMonth() + 1);
        LocalDate periodStart = LocalDate.of(prevMonth.getYear(), prevMonth.getMonth(), adjustedStartDay);

        return new ContributionPeriod(periodStart, lastDOE);
    }
}