package org.example.util.logic;

import org.example.dto.ContributionPeriod;
import org.example.dto.ContributionPeriodMore;
import org.example.util.intf.ContributionPeriodMiddleware;
import org.example.util.intf.MPFPayrollDateCalLogic;
import org.example.util.intf.MPFPayrollLogger;

import java.time.LocalDate;
import java.util.List;
import java.util.OptionalLong;

/***
 * common calculation logic and perform as a proxy class
 */
public abstract class MPFPayrollDateCalculatorLogic extends MPFPayrollDateCalculatorBase implements MPFPayrollLogger, MPFPayrollDateCalLogic {

    // need more period after the deadline of enrollment
    protected OptionalLong morePeriod = OptionalLong.empty();

    // the employee has 30 days in exemption for contribution payment.
    protected LocalDate deadlineFor30Exemption;
    
    // Middleware registry for processing contribution periods
    protected final MiddlewareRegistry middlewareRegistry;

    public MPFPayrollDateCalculatorLogic(List<LocalDate> publicHolidays) {
        super(publicHolidays);
        this.middlewareRegistry = new MiddlewareRegistry();
        initializeMiddlewares();
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

    /**
     * Get the deadline for 30-day exemption with lazy initialization
     */
    @Override
    public LocalDate getDeadlineFor30Exemption() {
        if (deadlineFor30Exemption == null && dateOfEmployment != null) {
            deadlineFor30Exemption = getThe31stDOE();
        }
        return deadlineFor30Exemption;
    }

    /**
     * Get the deadline of enrollment with lazy initialization
     * @return deadline of enrollment
     */
    @Override
    public LocalDate getDeadlineForEnrol() {
        if (deadlineForEnrol == null && dateOfEmployment != null) {
            boolean isEighteenOrOlder = !dateOfBirth.isAfter(LocalDate.now().minusYears(18));
            if (!isEighteenOrOlder) {
                return dateUtils.getDateAfterPublicHolidayAndWeekend(
                        dateUtils.getLargerDate(dateOfBirth.plusYears(18), getThe60thDOE())
                );
            }
            return dateUtils.getDateAfterPublicHolidayAndWeekend(getThe60thDOE());
        }
        return deadlineForEnrol;
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

    /**
     * Initialize default middlewares. Subclasses can override to add custom middlewares.
     */
    protected void initializeMiddlewares() {
        // Register the period rectification middleware
        middlewareRegistry.register(createPeriodRectificationMiddleware());
    }

    /**
     * Create the period rectification middleware.
     * Can be overridden by subclasses for custom rectification logic.
     */
    protected ContributionPeriodMiddleware createPeriodRectificationMiddleware() {
        return new PeriodRectificationMiddleware(endOfEmployment, this::calTheLastPeriod);
    }

    /**
     * Apply all registered middlewares to the contribution period.
     */
    private ContributionPeriodMore applyMiddleware(ContributionPeriodMore c) {
        return middlewareRegistry.createPipeline().apply(c);
    }

    /**
     * Add a custom middleware to the processing pipeline.
     * Middlewares are executed in the order they are added.
     * 
     * @param middleware the middleware to add
     * @return this instance for method chaining
     */
    public MPFPayrollDateCalculatorLogic addMiddleware(ContributionPeriodMiddleware middleware) {
        middlewareRegistry.register(middleware);
        return this;
    }

    /**
     * Clear all middlewares and reinitialize with defaults.
     * 
     * @return this instance for method chaining
     */
    public MPFPayrollDateCalculatorLogic resetMiddlewares() {
        middlewareRegistry.clear();
        initializeMiddlewares();
        return this;
    }

    /**
     * Get the middleware registry for advanced middleware management.
     * 
     * @return the middleware registry
     */
    protected MiddlewareRegistry getMiddlewareRegistry() {
        return middlewareRegistry;
    }

    // ==================== validate and supplement of data ============

    /**
     * valid birthday
     * @throws Exception
     */
    private void valDateOfBirth() throws Exception {
        if (dateOfBirth.isAfter(LocalDate.now())) {
            throw new Exception("the date of birth is invalid");
        }
    }

    protected void validate() throws Exception {
        valDateOfBirth();
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
