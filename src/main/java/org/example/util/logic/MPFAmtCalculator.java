package org.example.util.logic;

import org.example.constant.MPFCalculatorConstants;
import org.example.dto.ContributionPeriod;
import org.example.dto.ContributionPeriodMore;
import org.example.dto.MPFAmtResult;
import org.example.enums.CalUserType;
import org.example.enums.PayrollFrequency;
import org.example.util.intf.TriFunction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MPFAmtCalculator {
    private final PayrollFrequency pf;
    private MPFPayrollDateCalculatorLogic logic;
    private ContributionPeriodMore cache;
    private List<MPFAmtResult> results;

    /**
     * constructor
     * @param pf payroll frequency enum
     * @param holidays the holidays need to be skipped.
     */
    public MPFAmtCalculator(PayrollFrequency pf, List<LocalDate> holidays) {
        this.pf = pf;
        this.logic = pf.getCalculator(holidays);
    }

    /**
     * constructor with configuration
     * @param pf payroll frequency enum
     * @param holidays the holidays need to be skipped.
     * @param cfg configuration object
     */
    public MPFAmtCalculator(PayrollFrequency pf, List<LocalDate> holidays, MPFDateCalculatorConfig cfg) {
        this.pf = pf;
        this.logic = pf.getCalculator(holidays);
        configure(cfg);
    }

    public MPFAmtCalculator configure(MPFDateCalculatorConfig cfg){
        // Configure base properties
        if (cfg.getDateOfBirth() != null) {
            logic.setDateOfBirth(cfg.getDateOfBirth());
        }
        if (cfg.getDateOfEmployment() != null) {
            logic.setDateOfEmployment(cfg.getDateOfEmployment());
        }
        if (cfg.getEndOfEmployment() != null) {
            logic.setEndOfEmployment(cfg.getEndOfEmployment());
        }
        if (cfg.getDeadlineForEnrol() != null) {
            logic.setDeadlineForEnrol(cfg.getDeadlineForEnrol());
        }
        if (cfg.getMorePeriod() != null) {
            logic.setMorePeriod(cfg.getMorePeriod());
        }

        // Configure frequency-specific properties
        switch (pf) {
            case WEEKLY:
                if (logic instanceof MPFWeeklyDateCalculator && cfg.getWeeklyCycle() != null) {
                    ((MPFWeeklyDateCalculator) logic).setWeeklyCycle(cfg.getWeeklyCycle());
                }
                break;
            case FORTNIGHTLY:
                if (logic instanceof MPFFortNightlyDateCalculation && cfg.getPryllFnightStartDate() != null) {
                    ((MPFFortNightlyDateCalculation) logic).setFnightStartDate(cfg.getPryllFnightStartDate());
                }
                break;
            case NON_CALENDAR_MONTH:
                if (logic instanceof MPFNonCalendarMonDateCalculator && cfg.getNonCalStartDay() != null) {
                    ((MPFNonCalendarMonDateCalculator) logic).setStartDay(cfg.getNonCalStartDay());
                }
                break;
            case MONTHLY:
            case SEMI_MONTHLY:
                // These don't require additional configuration
                break;
        }
        return this;
    }

    public ContributionPeriodMore calculateDate() throws Exception {
        cache = logic.calculate();
        return cache;
    }

    public List<MPFAmtResult> calculateAmount(CalUserType userType, BigDecimal ...salaries) {
        boolean isEE = userType.equals(CalUserType.EMPLOYEE);
        return switch (pf) {
            case MONTHLY -> {
                if (isEE) {
                    yield calAmt(this::calEEMonthBase, salaries);
                }
                yield calAmt(this::calERMonthBase, salaries);
            }
            case NON_CALENDAR_MONTH, SEMI_MONTHLY, WEEKLY, FORTNIGHTLY -> {
                if (isEE) {
                    yield calAmt(this::calEEDayBase, salaries);
                }
                yield calAmt(this::calERDayBase, salaries);
            }
        };
    }

    public List<MPFAmtResult> calAmt(
            TriFunction<ContributionPeriod,BigDecimal,Boolean,BigDecimal> calFunc,
            BigDecimal ...salaries
    ){
        if (cache == null) {
            throw new IllegalStateException("calAmt cache is null, call calculateDate() first");
        }

        List<ContributionPeriod> periods = Stream.concat(
                cache.getPeriod().stream(),
                cache.getPeriodMore().stream()
                ).toList();
        if (periods.size() != salaries.length) {
            throw new IllegalStateException("calAmt cache size is different with salaries");
        }

        // result set
        List<MPFAmtResult> result = new ArrayList<>(periods.size());

        long boundary = cache.getPeriod().size();
        for(int i = 0 ; i < periods.size(); i++){
            ContributionPeriod p = periods.get(i);
            boolean afterExemption = false; //   if (i < boundary)
            if (!cache.getPeriodMore().isEmpty() && i >= boundary) {
                afterExemption = true;
            }
            BigDecimal amt = calFunc.apply(p,salaries[i], afterExemption);
            result.add(
                    new MPFAmtResult(
                            periods.get(i),
                            salaries[i],
                            amt
                    )
            );
        }

        results = result; // cache the result

        return result;
    }

    /**
     *
     * @param c the contribution period
     * @param salary the wage occurred in the period
     * @param afterExemption is after exemption date of employee or not
     * @return amount
     */
    private BigDecimal calEEMonthBase(ContributionPeriod c, BigDecimal salary, boolean afterExemption) {
        if (!afterExemption) {
           return BigDecimal.ZERO;
        }
        return calMonthBase(salary);
    }

    /**
     *
     * @param c the contribution period
     * @param salary the wage occurred in the period
     * @param afterExemption is after exemption date of employee or not
     * @return amount
     */
    private BigDecimal calERMonthBase(ContributionPeriod c, BigDecimal salary, boolean afterExemption) {
        return calMonthBase(salary);
    }

    /**
     *
     * @param salary the salary in the period
     * @return amount
     */
    private BigDecimal calMonthBase(BigDecimal salary) {
        if (salary.compareTo(MPFCalculatorConstants.G_INT_MIN_CALENDAR_MONTH) < 0) { // if month salary < min, no need to pay
            return BigDecimal.ZERO;// //todo   employer should no need
        }else if (salary.compareTo(MPFCalculatorConstants.G_INT_MAX_CALENDAR_MONTH) > 0) { // if month salary > max, pay the max
            return MPFCalculatorConstants.G_INT_MAX_CALENDAR_MONTH.multiply(MPFCalculatorConstants.FLOAT_RATIO);
        }

        return salary.multiply(MPFCalculatorConstants.FLOAT_RATIO);
    }

    /**
     *
     * @param c contribution period
     * @param salary salary in that period
     * @param afterExemption is after exemption or not?
     * @return amount
     */
    private BigDecimal calEEDayBase(ContributionPeriod c, BigDecimal salary, boolean afterExemption) {
        // if the period is before the employee's exemption date.
        if (!afterExemption) {
            return BigDecimal.ZERO;
        }

        // employee the days in period
        BigDecimal eeDiff = new BigDecimal(
                Long.toString(
                        logic.getEeContrDateDiff(c.getStartDate(),c.getEndDate())
                )
        );
        return calDayBase(eeDiff,salary);
    }

    /**
     *
     * @param c contribution period
     * @param salary the salary in that period
     * @param afterExemption for compatible, plz change if it has new design.
     * @return amount
     */
    private BigDecimal calERDayBase(ContributionPeriod c, BigDecimal salary, boolean afterExemption){
        // employee the days in period
        BigDecimal eeDiff = new BigDecimal(
                Long.toString(
                        logic.getErContrDateDiff(c.getStartDate(),c.getEndDate())
                )
        );
        return calDayBase(eeDiff,salary);
    }

    /**
     * calculate the amount need to pay in the period
     * @param diff the days during period
     * @param salary the wage occurred during the period
     * @return amount
     */
    private BigDecimal calDayBase(BigDecimal diff, BigDecimal salary){
        if (salary.compareTo(MPFCalculatorConstants.G_INT_MIN_DAY.multiply(diff)) < 0) { // salary < minimum_day * days_in_period
            return BigDecimal.ZERO;
        }else if (salary.compareTo(MPFCalculatorConstants.G_INT_MAX_DAY.multiply(diff)) > 0) { // salary > maximum_day * days_in_period
            return MPFCalculatorConstants.G_INT_MAX_DAY.multiply(diff).multiply(MPFCalculatorConstants.FLOAT_RATIO);
        }

        return MPFCalculatorConstants.FLOAT_RATIO.multiply(salary);
    }

    public String getDateReport(){

        StringBuilder report = new StringBuilder();

        logic.printNonPayPeriods(cache);
        logic.printPayPeriods(cache);

        // Demonstrate contribution date calculations
        report.append("\n=== CONTRIBUTION DATE CALCULATIONS ===\n");
        for (ContributionPeriod c : cache.getPeriod()) {

            long eeDays = logic.getEeContrDateDiff(c.getStartDate(), c.getEndDate());
            long erDays = logic.getErContrDateDiff(c.getStartDate(), c.getEndDate());

            report.append("\nPeriod: " + logic.formatDate(c.getStartDate()) + " to " + logic.formatDate(c.getEndDate()) + "\n");
            report.append("\nEmployee contribution days: " + eeDays + "\n");
            report.append("\nEmployer contribution days: " + erDays + "\n");
        }

        for (ContributionPeriod c : cache.getPeriodMore()) {

            long eeDays = logic.getEeContrDateDiff(c.getStartDate(), c.getEndDate());
            long erDays = logic.getErContrDateDiff(c.getStartDate(), c.getEndDate());

            report.append("\nMore Period: " + logic.formatDate(c.getStartDate()) + " to " + logic.formatDate(c.getEndDate()) + "\n");
            report.append("\nEmployee contribution days: " + eeDays + "\n");
            report.append("\nEmployer contribution days: " + erDays + "\n");
        }

        // Show important dates
        report.append("\n=== IMPORTANT DATES ===\n")
                .append("\nEmployment Date: " + logic.formatDate(logic.getDateOfEmployment()) + "\n")
                .append("\nBirth Date: " + logic.formatDate(logic.getDateOfBirth()) + "\n")
                .append("\n18th Birthday: " + logic.formatDate(logic.getThe18thAgeDate()) + "\n")
                .append("\n65th Birthday: " + logic.formatDate(logic.getThe65thAgeDate()) + "\n")
                .append("\n60th Day of Employment: " + logic.formatDate(logic.getThe60thDOE()) + "\n")
                .append("\n30th Day of Employment: " + logic.formatDate(logic.getThe30thDOE()) + "\n")
                .append("\n31st Day of Employment: " + logic.formatDate(logic.getThe31stDOE()) + "\n")
                .append("\nDeadline of Enrollment: " + logic.formatDate(logic.getDeadlineForEnrol()) + "\n");

        return report.toString();
    }

    public String getAmtReport(){
        return results.stream().map(MPFAmtResult::toString).collect(Collectors.joining(", "));
    }
}
