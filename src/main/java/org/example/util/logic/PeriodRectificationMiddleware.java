package org.example.util.logic;

import org.example.dto.ContributionPeriod;
import org.example.dto.ContributionPeriodMore;
import org.example.util.intf.ContributionPeriodMiddleware;

import java.time.LocalDate;
import java.util.List;

/**
 * Middleware implementation for rectifying contribution periods based on end of employment date.
 * This middleware ensures that contribution periods are properly adjusted when an employee
 * has an end of employment date.
 */
public class PeriodRectificationMiddleware implements ContributionPeriodMiddleware {
    
    private final LocalDate endOfEmployment;
    private final PeriodRectificationStrategy rectificationStrategy;
    
    /**
     * Constructor for the rectification middleware.
     * 
     * @param endOfEmployment the end of employment date, can be null if employee is still active
     * @param rectificationStrategy the strategy to use for calculating the last period
     */
    public PeriodRectificationMiddleware(LocalDate endOfEmployment, PeriodRectificationStrategy rectificationStrategy) {
        this.endOfEmployment = endOfEmployment;
        this.rectificationStrategy = rectificationStrategy;
    }
    
    @Override
    public ContributionPeriodMore process(ContributionPeriodMore contributionPeriod) {
        if (endOfEmployment == null) {
            return contributionPeriod; // No rectification needed
        }
        
        List<ContributionPeriod> morePeriods = contributionPeriod.getPeriodMore();
        if (morePeriods.isEmpty()) {
            return contributionPeriod;
        }
        
        List<ContributionPeriod> rectifiedPeriods = rectifyPeriods(morePeriods, morePeriods.size() - 1);
        contributionPeriod.setPeriodMore(rectifiedPeriods);
        
        return contributionPeriod;
    }
    
    /**
     * Recursively rectify periods based on end of employment date.
     * 
     * @param periods the list of contribution periods
     * @param index the current index being processed (recursive iteration)
     * @return the rectified list of periods
     */
    private List<ContributionPeriod> rectifyPeriods(List<ContributionPeriod> periods, int index) {
        if (index < 0) {
            return periods;
        }
        
        if (index >= periods.size() || shouldTruncateAtIndex(periods, index)) {
            if (index >= 0) {
                periods = periods.subList(0, index); // Cut periods after end of employment
            }
            
            // Add the final period using the strategy
            ContributionPeriod lastPeriod = rectificationStrategy.calculateLastPeriod(endOfEmployment);
            periods.add(lastPeriod);
            return periods;
        }
        
        return rectifyPeriods(periods, index - 1);
    }
    
    /**
     * Determines if the period at the given index should be truncated due to end of employment.
     * 
     * @param periods the list of periods
     * @param index the index to check
     * @return true if the period should be truncated
     */
    private boolean shouldTruncateAtIndex(List<ContributionPeriod> periods, int index) {
        ContributionPeriod period = periods.get(index);
        return period.getStartDate().isBefore(endOfEmployment) && 
               period.getEndDate().isAfter(endOfEmployment);
    }
    
    /**
     * Strategy interface for calculating the last contribution period.
     * This allows for different calculation strategies based on payroll frequency.
     */
    @FunctionalInterface
    public interface PeriodRectificationStrategy {
        ContributionPeriod calculateLastPeriod(LocalDate endOfEmployment);
    }
}
