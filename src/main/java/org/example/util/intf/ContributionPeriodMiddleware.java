package org.example.util.intf;

import org.example.dto.ContributionPeriodMore;

/**
 * Functional interface for processing ContributionPeriodMore objects in a middleware pattern.
 * This allows for composable and reusable middleware components that can be chained together.
 */
@FunctionalInterface
public interface ContributionPeriodMiddleware {
    
    /**
     * Process the contribution period and return the modified result.
     * 
     * @param contributionPeriod the input contribution period to process
     * @return the processed contribution period
     */
    ContributionPeriodMore process(ContributionPeriodMore contributionPeriod);
    
    /**
     * Chains this middleware with another middleware.
     * The current middleware will be executed first, then the next middleware.
     * 
     * @param next the next middleware to execute after this one
     * @return a new middleware that represents the composition of both middlewares
     */
    default ContributionPeriodMiddleware andThen(ContributionPeriodMiddleware next) {
        return contributionPeriod -> next.process(this.process(contributionPeriod));
    }
    
    /**
     * Creates a middleware that executes the given middleware before this one.
     * 
     * @param before the middleware to execute before this one
     * @return a new middleware that represents the composition of both middlewares
     */
    default ContributionPeriodMiddleware compose(ContributionPeriodMiddleware before) {
        return contributionPeriod -> this.process(before.process(contributionPeriod));
    }
    
    /**
     * Returns an identity middleware that returns the input unchanged.
     * Useful as a starting point for middleware chains.
     * 
     * @return an identity middleware
     */
    static ContributionPeriodMiddleware identity() {
        return contributionPeriod -> contributionPeriod;
    }
}
