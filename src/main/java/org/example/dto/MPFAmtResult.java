package org.example.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MPFAmtResult {
    private ContributionPeriod contributionPeriod;
    private BigDecimal salary;
    private BigDecimal amount;

    public MPFAmtResult(ContributionPeriod contributionPeriod,BigDecimal salary,BigDecimal amount) {
        this.contributionPeriod = contributionPeriod;
        this.salary = salary;
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "MPFAmtResult{contributionPeriod=" + contributionPeriod +
                ", salary=" + salary +
                ", amount=" + amount + "}";
    }
}
