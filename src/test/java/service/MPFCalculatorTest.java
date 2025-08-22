package service;

import org.example.dto.ContributionPeriod;
import org.example.dto.ContributionPeriodMore;
import org.example.enums.CalUserType;
import org.example.enums.PayrollFrequency;
import org.example.util.intf.MPFPayrollLogger;
import org.example.util.logic.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalLong;
import java.util.stream.Collectors;

public class MPFCalculatorTest {
    private MPFPayrollLogger mpfLog;
    private List<LocalDate> publicHolidays;
    private MPFDateCalculatorConfig monthConfig;
    private MPFDateCalculatorConfig nonCalendarMonthConfig;
    private MPFDateCalculatorConfig semiMonthConfig;
    private MPFDateCalculatorConfig fortyNightConfig;
    private MPFDateCalculatorConfig weeklyConfig;

    @BeforeEach
    public void setUp(){
        // Sample public holidays
        publicHolidays = Arrays.asList(
                LocalDate.of(2025, 1, 1),   // New Year's Day
                LocalDate.of(2025, 2, 12),  // Chinese New Year
                LocalDate.of(2025, 2, 13),  // Chinese New Year
                LocalDate.of(2025, 3, 29),  // Good Friday
                LocalDate.of(2025, 5, 1),   // Labour Day
                LocalDate.of(2025, 5, 15),  // Buddha's Birthday
                LocalDate.of(2025, 12, 25)  // Christmas Day
        );

        // month config
        monthConfig = new MPFDateCalculatorConfig()
                .setDateOfEmployment(LocalDate.of(2025, 1, 5))
                .setDateOfBirth(LocalDate.of(1990, 5, 20))
                .setMorePeriod(OptionalLong.of(3)) // the period needed after exemption
                .setEndOfEmployment(LocalDate.of(2025, 4, 15))
        ;

        nonCalendarMonthConfig = new MPFDateCalculatorConfig()
                .setDateOfEmployment(LocalDate.of(2025, 1, 5))
                .setDateOfBirth(LocalDate.of(1990, 5, 20))
                .setMorePeriod(OptionalLong.of(3))
                .setEndOfEmployment(LocalDate.of(2025, 4, 15))
                .setNonCalStartDay(4)
        ;

        semiMonthConfig = new MPFDateCalculatorConfig()
                .setDateOfEmployment(LocalDate.of(2025, 1, 2))
                .setDateOfBirth(LocalDate.of(1990, 5, 20))
                .setMorePeriod(OptionalLong.of(3))
                .setEndOfEmployment(LocalDate.of(2025, 4, 15))
        ;

        fortyNightConfig = new MPFDateCalculatorConfig()
                .setDateOfEmployment(LocalDate.of(2025, 1, 2))
                .setDateOfBirth(LocalDate.of(1990, 5, 20))
                .setMorePeriod(OptionalLong.of(3))
                .setEndOfEmployment(LocalDate.of(2025, 4, 15))
                .setPryllFnightStartDate(LocalDate.of(2025, 1, 3))
        ;

        weeklyConfig = new MPFDateCalculatorConfig()
                .setDateOfEmployment(LocalDate.of(2025, 1, 2))
                .setDateOfBirth(LocalDate.of(1990, 5, 20))
                .setMorePeriod(OptionalLong.of(3))
                .setEndOfEmployment(LocalDate.of(2025, 4, 15))
                .setWeeklyCycle(DayOfWeek.WEDNESDAY)
        ;

    }

    private void MPFDateInfoLogAll(MPFPayrollDateCalculatorLogic calculator) {
        try {
            // logger
            MPFPayrollLogger mpfLog = calculator;

            ContributionPeriodMore periods = calculator.calculate();
            mpfLog.printNonPayPeriods(periods);
            mpfLog.printPayPeriods(periods);

            // Demonstrate contribution date calculations
            System.out.println("\n=== CONTRIBUTION DATE CALCULATIONS ===");
            for (ContributionPeriod c : periods.getPeriod()) {

                long eeDays = calculator.getEeContrDateDiff(c.getStartDate(), c.getEndDate());
                long erDays = calculator.getErContrDateDiff(c.getStartDate(), c.getEndDate());

                System.out.println("Period: " + mpfLog.formatDate(c.getStartDate()) + " to " + mpfLog.formatDate(c.getEndDate()));
                System.out.println("Employee contribution days: " + eeDays);
                System.out.println("Employer contribution days: " + erDays);
            }

            for (ContributionPeriod c : periods.getPeriodMore()) {

                long eeDays = calculator.getEeContrDateDiff(c.getStartDate(), c.getEndDate());
                long erDays = calculator.getErContrDateDiff(c.getStartDate(), c.getEndDate());

                System.out.println("More Period: " + mpfLog.formatDate(c.getStartDate()) + " to " + mpfLog.formatDate(c.getEndDate()));
                System.out.println("Employee contribution days: " + eeDays);
                System.out.println("Employer contribution days: " + erDays);
            }

            // Show important dates
            System.out.println("\n=== IMPORTANT DATES ===");
            System.out.println("Employment Date: " + mpfLog.formatDate(calculator.getDateOfEmployment()));
            System.out.println("Birth Date: " + mpfLog.formatDate(calculator.getDateOfBirth()));
            System.out.println("18th Birthday: " + mpfLog.formatDate(calculator.getThe18thAgeDate()));
            System.out.println("65th Birthday: " + mpfLog.formatDate(calculator.getThe65thAgeDate()));
            System.out.println("60th Day of Employment: " + mpfLog.formatDate(calculator.getThe60thDOE()));
            System.out.println("30th Day of Employment: " + mpfLog.formatDate(calculator.getThe30thDOE()));
            System.out.println("31st Day of Employment: " + mpfLog.formatDate(calculator.getThe31stDOE()));
            System.out.println("Deadline of Enrollment: " + mpfLog.formatDate(calculator.getDeadlineForEnrol()));

        } catch (Exception e) {
            System.out.println("error msg:" + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void testCalDateMonth() {
        // Create calculator
        MPFMonDateCalculator calculator = new MPFMonDateCalculator(publicHolidays);

        // Set up employee details
        LocalDate employmentDate = LocalDate.of(2025, 1, 1);
        LocalDate birthDate = LocalDate.of(1990, 5, 20);
//            LocalDate deadlineForEnrol = LocalDate.of(2025, 12, 31);

        calculator.setDateOfEmployment(employmentDate);
        calculator.setDateOfBirth(birthDate);
//            calculator.setDeadlineForEnrol(deadlineForEnrol);
        calculator.setMorePeriod(OptionalLong.of(1));

        // set end of employment date
        calculator.setEndOfEmployment(LocalDate.of(2025, 4, 15));

        MPFDateInfoLogAll(calculator);
    }

    @Test
    public void testCalDateNonMonth() {
        // Create calculator
        MPFNonCalendarMonDateCalculator calculator = new MPFNonCalendarMonDateCalculator(publicHolidays);
        calculator.setStartDay(1);

        // logger
        MPFPayrollLogger mpfLog = calculator;

        // Set up employee details
        LocalDate employmentDate = LocalDate.of(2025, 2, 1);
        LocalDate birthDate = LocalDate.of(1990, 5, 20);
//            LocalDate deadlineForEnrol = LocalDate.of(2025, 12, 31);

        calculator.setDateOfEmployment(employmentDate);
        calculator.setDateOfBirth(birthDate);
//            calculator.setDeadlineForEnrol(deadlineForEnrol);
//            calculator.setMorePeriod(OptionalLong.of(0));

        MPFDateInfoLogAll(calculator);
    }

    @Test
    public void testCalAmtMonth() {
        try {
            // new a monthly amount calculator
            MPFAmtCalculator amtCctr = new MPFAmtCalculator(PayrollFrequency.MONTHLY, publicHolidays);

            // config and then cal the date
            ContributionPeriodMore contrPeriods = amtCctr.configure(monthConfig).calculateDate();

            // salary size
            int salariesSize = contrPeriods.getPeriod().size() + contrPeriods.getPeriodMore().size();

            // fake data
            BigDecimal[] salaries = new BigDecimal[salariesSize];
            Arrays.fill(salaries,new BigDecimal("99999"));

            // cal the amount
            amtCctr.calculateAmount(CalUserType.EMPLOYEE, salaries);

            // report
            System.out.println(amtCctr.getAmtReport());

            System.out.println(amtCctr.getDateReport());

        }catch (Exception e) {
            System.out.println("error msg:" + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void testCalAmtNonMonth() {
        try {
            // new a monthly amount calculator
            MPFAmtCalculator amtCctr = new MPFAmtCalculator(PayrollFrequency.NON_CALENDAR_MONTH, publicHolidays);

            // config and then cal the date
            ContributionPeriodMore contrPeriods = amtCctr.configure(nonCalendarMonthConfig).calculateDate();

            int salariesSize = contrPeriods.getPeriod().size() + contrPeriods.getPeriodMore().size();

            // fake data
            BigDecimal[] salaries = new BigDecimal[salariesSize];
            Arrays.fill(salaries,new BigDecimal("99999"));

            // cal the amount
            amtCctr.calculateAmount(CalUserType.EMPLOYEE, salaries);

            // report
            System.out.println(amtCctr.getAmtReport());

            System.out.println(amtCctr.getDateReport());

        }catch (Exception e) {
            System.out.println("error msg:" + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void testCalAmtSemiMonth() {
        try{
            // new a monthly amount calculator
            MPFAmtCalculator amtCctr = new MPFAmtCalculator(PayrollFrequency.SEMI_MONTHLY, publicHolidays);

            // config and then cal the date
            ContributionPeriodMore contrPeriods = amtCctr.configure(semiMonthConfig).calculateDate();

            int salariesSize = contrPeriods.getPeriod().size() + contrPeriods.getPeriodMore().size();

            // fake data
            BigDecimal[] salaries = new BigDecimal[salariesSize];
            Arrays.fill(salaries,new BigDecimal("9999"));

            amtCctr.calculateAmount(CalUserType.EMPLOYEE, salaries);

            System.out.println(amtCctr.getAmtReport());

            System.out.println(amtCctr.getDateReport());

        }catch (Exception e){
            System.out.println("error msg:" + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void testCalAmtFortNightly(){
        try {
            // new a monthly amount calculator
            MPFAmtCalculator amtCctr = new MPFAmtCalculator(PayrollFrequency.FORTNIGHTLY, publicHolidays);

            // config and then cal the date
            ContributionPeriodMore contrPeriods = amtCctr.configure(fortyNightConfig).calculateDate();

            // salary size
            int salariesSize = contrPeriods.getPeriod().size() + contrPeriods.getPeriodMore().size();

            // fake data
            BigDecimal[] salaries = new BigDecimal[salariesSize];
            Arrays.fill(salaries,new BigDecimal("6999.99999999999999999999999999999999"));

            // cal the amount
            amtCctr.calculateAmount(CalUserType.EMPLOYEE, salaries);

            // report
            System.out.println(amtCctr.getAmtReport());

            System.out.println(amtCctr.getDateReport());
        }catch (Exception e) {
            System.out.println("error msg:" + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void testCalAmtWeekly() {
        try {
            // new a monthly amount calculator
            MPFAmtCalculator amtCctr = new MPFAmtCalculator(PayrollFrequency.WEEKLY, publicHolidays);

            // config and then cal the date
            ContributionPeriodMore contrPeriods = amtCctr.configure(weeklyConfig).calculateDate();

            // salary size
            int salariesSize = contrPeriods.getPeriod().size() + contrPeriods.getPeriodMore().size();

            // fake data
            BigDecimal[] salaries = new BigDecimal[salariesSize];
            Arrays.fill(salaries,new BigDecimal("6999.99999999999999999999999999999999"));

            // cal the amount
            amtCctr.calculateAmount(CalUserType.EMPLOYEE, salaries);

            // report
            System.out.println(amtCctr.getAmtReport());

            System.out.println(amtCctr.getDateReport());
        }catch (Exception e) {
            System.out.println("error msg:" + e.getMessage());
            e.printStackTrace();
        }
    }
}
