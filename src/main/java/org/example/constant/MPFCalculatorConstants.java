package org.example.constant;

import java.math.BigDecimal;

/**
 * Constants for MPF Contribution Calculator
 */
public class MPFCalculatorConstants {
    
    // Payroll Frequency Constants
    public static final int G_INT_PF_MONTHLY = 1;
    public static final int G_INT_PF_NONCALMONTH = 2;
    public static final int G_INT_PF_SEMIMONTH = 3;
    public static final int G_INT_PF_FORTNIGHTLY = 4;
    public static final int G_INT_PF_WEEKLY = 5;

    // Age Group Constants
    public static final int G_INT_AGE_BELOW18 = 1;
    public static final int G_INT_AGE_BETWEEN1865 = 2;
    public static final int G_INT_AGE_ABOVE65 = 3;
    public static final int G_INT_AGE_DEPENDS = 0;

    // Weekly Cycle Constants
    public static final int G_INT_WC_MON_SUN = 0;
    public static final int G_INT_WC_TUE_MON = 1;
    public static final int G_INT_WC_WED_TUE = 2;
    public static final int G_INT_WC_THU_WED = 3;
    public static final int G_INT_WC_FRI_THU = 4;
    public static final int G_INT_WC_SAT_FRI = 5;
    public static final int G_INT_WC_SUN_SAT = 6;

    // Contribution Ratio
    public static final BigDecimal FLOAT_RATIO = new BigDecimal("0.05");

    // Contribution Limits
    public static final BigDecimal G_INT_MIN_DAY = new BigDecimal("280");
    public static final BigDecimal G_INT_MAX_DAY = new BigDecimal("1000");

    public static final BigDecimal G_INT_MIN_WEEK = new BigDecimal("1960");
    public static final BigDecimal G_INT_MAX_WEEK = new BigDecimal("7000");

    public static final BigDecimal G_INT_MIN_CALENDAR_MONTH = new BigDecimal("7100");
    public static final BigDecimal G_INT_MAX_CALENDAR_MONTH = new BigDecimal("30000");

    private MPFCalculatorConstants() {
        // Private constructor to prevent instantiation
    }
}
