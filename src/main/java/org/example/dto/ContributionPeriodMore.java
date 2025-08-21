package org.example.dto;

import java.util.List;

public class ContributionPeriodMore {
    private List<ContributionPeriod> period;
    private List<ContributionPeriod> periodMore;

    public ContributionPeriodMore(List<ContributionPeriod> period, List<ContributionPeriod> periodMore) {
        this.period = period;
        this.periodMore = periodMore;
    }

    public List<ContributionPeriod> getPeriod() {
        return period;
    }

    public List<ContributionPeriod> getPeriodMore() {
        return periodMore;
    }

    public ContributionPeriodMore setPeriod(List<ContributionPeriod> period) {
        this.period = period;
        return this;
    }

    public ContributionPeriodMore setPeriodMore(List<ContributionPeriod> periodMore) {
        this.periodMore = periodMore;
        return this;
    }

    @Override
    public String toString() {
        return "ContributionPeriodMore{" +
                "period=" + period +
                ", periodMore=" + periodMore +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContributionPeriodMore that = (ContributionPeriodMore) o;

        if (!period.equals(that.period)) return false;
        return periodMore.equals(that.periodMore);
    }

    @Override
    public int hashCode() {
        int result = period.hashCode();
        result = 31 * result + periodMore.hashCode();
        return result;
    }
}
