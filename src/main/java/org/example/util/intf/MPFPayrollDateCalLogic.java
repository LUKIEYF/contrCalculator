package org.example.util.intf;

import org.example.dto.ContributionPeriodMore;

import java.time.LocalDate;

public interface MPFPayrollDateCalLogic {
    long getEeContrDateDiff(LocalDate startDate, LocalDate endDate);
    long getErContrDateDiff(LocalDate startDate, LocalDate endDate);
}
