package org.example.util.logic;

import org.example.dto.ContributionPeriod;
import org.example.dto.ContributionPeriodMore;
import org.example.util.intf.ContributionPeriodMiddleware;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

/**
 * Example middleware for validating contribution periods.
 * This demonstrates how additional middlewares can be created and used
 * to extend the processing pipeline without modifying core logic.
 */
public class ValidationMiddleware implements ContributionPeriodMiddleware {
    
    private static final Logger logger = Logger.getLogger(ValidationMiddleware.class.getName());
    
    private final boolean strictValidation;
    
    public ValidationMiddleware(boolean strictValidation) {
        this.strictValidation = strictValidation;
    }
    
    @Override
    public ContributionPeriodMore process(ContributionPeriodMore contributionPeriod) {
        validatePeriods(contributionPeriod.getPeriod(), "Regular periods");
        validatePeriods(contributionPeriod.getPeriodMore(), "Additional periods");
        
        return contributionPeriod;
    }
    
    /**
     * Validate a list of contribution periods.
     * 
     * @param periods the periods to validate
     * @param periodType description of the period type for logging
     */
    private void validatePeriods(List<ContributionPeriod> periods, String periodType) {
        if (periods == null || periods.isEmpty()) {
            return;
        }
        
        for (int i = 0; i < periods.size(); i++) {
            ContributionPeriod period = periods.get(i);
            
            // Validate individual period
            validatePeriod(period, periodType + " [" + i + "]");
            
            // Validate sequence (no gaps or overlaps)
            if (i > 0) {
                validateSequence(periods.get(i - 1), period, periodType);
            }
        }
        
        logger.info("Validated " + periods.size() + " " + periodType.toLowerCase());
    }
    
    /**
     * Validate an individual contribution period.
     * 
     * @param period the period to validate
     * @param context context information for error reporting
     */
    private void validatePeriod(ContributionPeriod period, String context) {
        if (period == null) {
            handleValidationError("Null period found in " + context);
            return;
        }
        
        if (period.getStartDate() == null || period.getEndDate() == null) {
            handleValidationError("Period with null dates in " + context);
            return;
        }
        
        if (period.getStartDate().isAfter(period.getEndDate())) {
            handleValidationError("Invalid period: start date after end date in " + context);
        }
        
        if (period.getStartDate().equals(period.getEndDate())) {
            logger.warning("Single-day period detected in " + context);
        }
    }
    
    /**
     * Validate the sequence between two consecutive periods.
     * 
     * @param previous the previous period
     * @param current the current period
     * @param periodType the type of periods being validated
     */
    private void validateSequence(ContributionPeriod previous, ContributionPeriod current, String periodType) {
        LocalDate expectedStart = previous.getEndDate().plusDays(1);
        
        if (!current.getStartDate().equals(expectedStart)) {
            if (current.getStartDate().isBefore(expectedStart)) {
                handleValidationError("Overlapping periods detected in " + periodType);
            } else {
                handleValidationError("Gap between periods detected in " + periodType);
            }
        }
    }
    
    /**
     * Handle validation errors based on the strictValidation setting.
     * 
     * @param message the error message
     */
    private void handleValidationError(String message) {
        if (strictValidation) {
            throw new IllegalStateException(message);
        } else {
            logger.warning(message);
        }
    }
}
