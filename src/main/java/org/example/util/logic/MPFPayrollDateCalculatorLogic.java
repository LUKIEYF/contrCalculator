package org.example.util.logic;

import org.example.dto.ContributionPeriod;
import org.example.dto.ContributionPeriodMore;
import org.example.enums.WeeklyCycle;
import org.example.util.intf.MPFPayrollDateCalLogic;
import org.example.util.intf.MPFPayrollLogger;

import java.time.LocalDate;
import java.util.List;
import java.util.OptionalLong;
import java.util.function.Function;

/***
 * common calculation logic and perform as a proxy class
 */
public abstract class MPFPayrollDateCalculatorLogic extends MPFPayrollDateCalculatorBase implements MPFPayrollLogger, MPFPayrollDateCalLogic {

    // need more period after the deadline of enrollment
    protected OptionalLong morePeriod = OptionalLong.empty();

    public MPFPayrollDateCalculatorLogic(List<LocalDate> publicHolidays) {
        super(publicHolidays);
    }

    public MPFPayrollDateCalculatorLogic setMorePeriod(OptionalLong morePeriod) {
        if (morePeriod.isPresent() &&
                morePeriod.getAsLong() > 0 &&
                morePeriod.getAsLong() <= 30
        ) {
            this.morePeriod = morePeriod;
        } else {
            throw new IllegalArgumentException("morePeriod must be a positive number (0,30)");
        }
        return this;
    }

    public ContributionPeriodMore calculate() throws Exception {
        validate();
        return applyMiddleware(
                calDate()
        );
    }

    abstract ContributionPeriodMore calDate();

    abstract ContributionPeriod calTheLastPeriod(LocalDate lastDOE);

    // ==================== middleware =================================

    private ContributionPeriodMore applyMiddleware(ContributionPeriodMore c){
        return createMiddlewarePipeline().apply(c);
    }

    /**
     * Create the middleware pipeline
     */
    private Function<ContributionPeriodMore, ContributionPeriodMore> createMiddlewarePipeline() {
        return Function.<ContributionPeriodMore>identity()
                .andThen(this::rectifyPeriodMiddleware);
    }

    /***
     * check the period is between the period or not
     * @param c the contribution period before and after exemption date
     * @return the contribution period before and after exemption date
     */
    private ContributionPeriodMore rectifyPeriodMiddleware(ContributionPeriodMore c){
        if (endOfEmployment != null){
            List<ContributionPeriod> m = c.getPeriodMore();
            if (!m.isEmpty()){
                c.setPeriodMore(rectifyPeriod(m,m.size() - 1)); // recursive
            }
        }
        return c;
    }

    private List<ContributionPeriod> rectifyPeriod(List<ContributionPeriod> cs, int i){
        if (i < 0){
            return cs;
        } else if (
                i >= cs.size() || // reversely iterate
                (
                        cs.get(i).getStartDate().isBefore(endOfEmployment) && // means the end of employment date between [start,end]
                                cs.get(i).getEndDate().isAfter(endOfEmployment)
                )
        ){
            if(i >= 0){
                cs = cs.subList(0, i); // cut the period if is not before the end of employment date
            }
            // add the last period
            cs.add(calTheLastPeriod(endOfEmployment));
            return cs;
        }

        return rectifyPeriod(cs, i - 1);
    }

    // ==================== validate and supplement of data ============
    private void valDateOfBirth() throws Exception {
        if (dateOfBirth.isAfter(LocalDate.now())) {
            throw new Exception("the date of birth is invalid");
        }
    }

    private void valDeadlineOfEnrol(){
        if(deadlineForEnrol == null){
            deadlineForEnrol = calDeadlineForEnrol();
        }
    }

    protected void validate() throws Exception {
        valDateOfBirth();
        valDeadlineOfEnrol();
        validateCustomized();
    }

    abstract void validateCustomized();

    /**
     * Calculate Employee Contribution Date Difference
     */
    public long getEeContrDateDiff(LocalDate startDate, LocalDate endDate) {
        long startDateNum = dateUtils.getDateDiff(LocalDate.EPOCH, startDate) + 1;
        long endDateNum = dateUtils.getDateDiff(LocalDate.EPOCH, endDate) + 1;
        long firstContrDateNum = dateUtils.getDateDiff(LocalDate.EPOCH, getEeContCommenceDate(startDate, endDate)) + 1;

        LocalDate age65Date = getThe65thAgeDate();

        // Check age 65 conditions
        if (endDate.isAfter(age65Date) ||
                (startDate.isBefore(age65Date) || startDate.equals(age65Date)) &&
                        (age65Date.isBefore(endDate) || age65Date.equals(endDate))) {
            return 0;
        }

        if (endDateNum >= firstContrDateNum && firstContrDateNum >= startDateNum) {
            // Special handling for semi-monthly
            // This would need to be implemented based on current payroll frequency
            // For now, return standard calculation
            return endDateNum - firstContrDateNum + 1;
        }

        if (endDateNum < firstContrDateNum) {
            return 0;
        }

        return endDateNum - startDateNum + 1;
    }

    /**
     * Calculate Employer Contribution Date Difference
     */
    public long getErContrDateDiff(LocalDate startDate, LocalDate endDate) {
        long startDateNum = dateUtils.getDateDiff(LocalDate.EPOCH, startDate) + 1;
        long endDateNum = dateUtils.getDateDiff(LocalDate.EPOCH, endDate) + 1;
        long firstContrDateNum = dateUtils.getDateDiff(LocalDate.EPOCH, getErContCommenceDate(startDate, endDate)) + 1;

        LocalDate age65Date = getThe65thAgeDate();

        // Check age 65 conditions
        if (endDate.isAfter(age65Date) ||
                (startDate.isBefore(age65Date) || startDate.equals(age65Date)) &&
                        (age65Date.isBefore(endDate) || age65Date.equals(endDate))) {
            return 0;
        }

        if (endDateNum >= firstContrDateNum && firstContrDateNum >= startDateNum) {
            return endDateNum - firstContrDateNum + 1;
        }

        return endDateNum - startDateNum + 1;
    }

    /**
     * Get Employee Contribution Commencement Date
     */
    private LocalDate getEeContCommenceDate(LocalDate startDate, LocalDate endDate) {
        // This would implement the logic to determine when EE contributions commence
        // For now, return the latter of employment date or period start
        return dateOfEmployment.isAfter(startDate) ? dateOfEmployment : startDate;
    }

    /**
     * Get Employer Contribution Commencement Date
     */
    private LocalDate getErContCommenceDate(LocalDate startDate, LocalDate endDate) {
        // This would implement the logic to determine when ER contributions commence
        // For now, return the latter of employment date or period start
        return dateOfEmployment.isAfter(startDate) ? dateOfEmployment : startDate;
    }
}
