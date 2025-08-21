package org.example.dto;

import java.time.LocalDate;

/**
 * Represents a contribution period with start and end dates
 */
public class ContributionPeriod {
    private LocalDate startDate;
    private LocalDate endDate;
    
    public ContributionPeriod(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }

    public ContributionPeriod setStartDate(LocalDate startDate) {
        this.startDate = startDate;
        return this;
    }

    public ContributionPeriod setEndDate(LocalDate endDate) {
        this.endDate = endDate;
        return this;
    }

    @Override
    public String toString() {
        return "ContributionPeriod{" +
                "startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        ContributionPeriod that = (ContributionPeriod) o;
        
        if (!startDate.equals(that.startDate)) return false;
        return endDate.equals(that.endDate);
    }
    
    @Override
    public int hashCode() {
        int result = startDate.hashCode();
        result = 31 * result + endDate.hashCode();
        return result;
    }
}
