package org.example.util.logic;

import org.example.dto.ContributionPeriodMore;
import org.example.util.intf.ContributionPeriodMiddleware;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Registry for managing and composing contribution period middlewares.
 * This class provides a centralized way to register, manage, and execute
 * middleware components in a pipeline pattern.
 */
public class MiddlewareRegistry {
    
    private final List<ContributionPeriodMiddleware> middlewares;
    
    public MiddlewareRegistry() {
        this.middlewares = new ArrayList<>();
    }
    
    /**
     * Register a middleware to be executed in the pipeline.
     * Middlewares are executed in the order they are registered.
     * 
     * @param middleware the middleware to register
     * @return this registry for method chaining
     */
    public MiddlewareRegistry register(ContributionPeriodMiddleware middleware) {
        if (middleware != null) {
            middlewares.add(middleware);
        }
        return this;
    }
    
    /**
     * Register multiple middlewares at once.
     * 
     * @param middlewaresToRegister the middlewares to register
     * @return this registry for method chaining
     */
    public MiddlewareRegistry register(ContributionPeriodMiddleware... middlewaresToRegister) {
        for (ContributionPeriodMiddleware middleware : middlewaresToRegister) {
            register(middleware);
        }
        return this;
    }
    
    /**
     * Clear all registered middlewares.
     * 
     * @return this registry for method chaining
     */
    public MiddlewareRegistry clear() {
        middlewares.clear();
        return this;
    }
    
    /**
     * Get the number of registered middlewares.
     * 
     * @return the count of registered middlewares
     */
    public int size() {
        return middlewares.size();
    }
    
    /**
     * Check if the registry is empty.
     * 
     * @return true if no middlewares are registered
     */
    public boolean isEmpty() {
        return middlewares.isEmpty();
    }
    
    /**
     * Create a composed middleware function from all registered middlewares.
     * The middlewares are executed in the order they were registered.
     * 
     * @return a function that applies all registered middlewares in sequence
     */
    public Function<ContributionPeriodMore, ContributionPeriodMore> createPipeline() {
        if (middlewares.isEmpty()) {
            return Function.identity();
        }
        
        return contributionPeriod -> {
            ContributionPeriodMore result = contributionPeriod;
            for (ContributionPeriodMiddleware middleware : middlewares) {
                result = middleware.process(result);
            }
            return result;
        };
    }
    
    /**
     * Create a composed middleware from all registered middlewares.
     * This returns a single middleware that represents the entire pipeline.
     * 
     * @return a composed middleware, or identity middleware if none are registered
     */
    public ContributionPeriodMiddleware createComposedMiddleware() {
        if (middlewares.isEmpty()) {
            return ContributionPeriodMiddleware.identity();
        }
        
        return middlewares.stream()
                .reduce(ContributionPeriodMiddleware.identity(), ContributionPeriodMiddleware::andThen);
    }
    
    /**
     * Remove a specific middleware from the registry.
     * 
     * @param middleware the middleware to remove
     * @return true if the middleware was found and removed
     */
    public boolean remove(ContributionPeriodMiddleware middleware) {
        return middlewares.remove(middleware);
    }
    
    /**
     * Remove middleware at a specific index.
     * 
     * @param index the index of the middleware to remove
     * @return the removed middleware
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    public ContributionPeriodMiddleware remove(int index) {
        return middlewares.remove(index);
    }
    
    /**
     * Get a copy of all registered middlewares.
     * 
     * @return a new list containing all registered middlewares
     */
    public List<ContributionPeriodMiddleware> getMiddlewares() {
        return new ArrayList<>(middlewares);
    }
}
