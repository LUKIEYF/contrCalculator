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
    public void setUp() {
        // Sample public holidays
        publicHolidays = Arrays.asList(
                LocalDate.of(2025, 1, 1),   // New Year's Day
                LocalDate.of(2025, 1, 28),   //New Year
                LocalDate.of(2025, 1, 29),   //New Year
                LocalDate.of(2025, 1, 30),   //New Year
                LocalDate.of(2025, 1, 31),   //New Year
                LocalDate.of(2025, 2, 1),    //New Year
                LocalDate.of(2025, 2, 2),    //New Year
                LocalDate.of(2025, 2, 12),  // Chinese New Year
                LocalDate.of(2025, 2, 13),  // Chinese New Year
                LocalDate.of(2025, 3, 29),  // Good Friday
                LocalDate.of(2025, 5, 1),   // Labour Day
                LocalDate.of(2025, 5, 15),  // Buddha's Birthday
                LocalDate.of(2025, 7, 1),   //
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
        } catch (Exception e) {
            System.out.println("error msg:" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * The first payment to the computer
     * This computer is only applicable for calculating the first contribution of employees
     * aged between 18 and 65 (excluding temporary employees). This computer is not applicable
     * to situations where an employee transfers to a new employer's company due to business
     * ownership transfer or internal transfer within the group, or when an employee leaves
     * the company or reaches the age of 65 during any contribution period covered by the first contribution.
     *
     * Without considering the cases of less than 18 and greater than 65, as well as the situation of resignation,
     * the page calculator cannot verify
     */

    @Test
    public void testCalAmtMonthForERMCAndEEMC() {
        try {
            // month config
            MPFDateCalculatorConfig monthConfig = new MPFDateCalculatorConfig()
                    .setDateOfEmployment(LocalDate.of(2025, 1, 1))
                    .setDateOfBirth(LocalDate.of(1990, 5, 20))
                    .setMorePeriod(OptionalLong.of(3)); // the period needed after exemption

            MPFAmtCalculator amtCctr = new MPFAmtCalculator(PayrollFrequency.MONTHLY, publicHolidays, monthConfig);
            ContributionPeriodMore contrPeriods = amtCctr.calculateDate();

            int salariesSize = contrPeriods.getPeriod().size() + contrPeriods.getPeriodMore().size();


            BigDecimal[] salaries1 = new BigDecimal[salariesSize];
            Arrays.fill(salaries1, new BigDecimal("10000"));
            amtCctr.calculateAmount(CalUserType.EMPLOYEE, salaries1);
            System.out.println("--------------EMPLOYEE salary = 10000-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());

            amtCctr.calculateAmount(CalUserType.EMPLOYER, salaries1);
            System.out.println("--------------EMPLOYER salary = 10000-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());

            BigDecimal[] salaries2 = new BigDecimal[salariesSize];
            Arrays.fill(salaries2, new BigDecimal("7000"));
            amtCctr.calculateAmount(CalUserType.EMPLOYEE, salaries2);
            System.out.println("--------------EMPLOYEE salary = 7000-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());
            amtCctr.calculateAmount(CalUserType.EMPLOYER, salaries2);
            System.out.println("--------------EMPLOYER salary = 7000-----------");   // todo  error  not 0 should be 350
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());

            BigDecimal[] salaries3 = new BigDecimal[salariesSize];
            Arrays.fill(salaries3, new BigDecimal("7100"));
            amtCctr.calculateAmount(CalUserType.EMPLOYEE, salaries3);
            System.out.println("--------------EMPLOYEE salary = 7100-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());
            amtCctr.calculateAmount(CalUserType.EMPLOYER, salaries3);
            System.out.println("--------------EMPLOYER salary = 7100-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());


            BigDecimal[] salaries4 = new BigDecimal[salariesSize];
            Arrays.fill(salaries4, new BigDecimal("30000"));
            amtCctr.calculateAmount(CalUserType.EMPLOYEE, salaries4);
            System.out.println("--------------EMPLOYEE salary = 30000-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());
            amtCctr.calculateAmount(CalUserType.EMPLOYER, salaries4);
            System.out.println("--------------EMPLOYER salary = 30000-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());

            BigDecimal[] salaries5 = new BigDecimal[salariesSize];
            Arrays.fill(salaries5, new BigDecimal("40000"));
            amtCctr.calculateAmount(CalUserType.EMPLOYEE, salaries5);
            System.out.println("--------------EMPLOYEE salary = 40000-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());
            amtCctr.calculateAmount(CalUserType.EMPLOYER, salaries5);
            System.out.println("--------------EMPLOYER salary = 40000-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());
        } catch (Exception e) {
            System.out.println("error msg:" + e.getMessage());
            e.printStackTrace();
        }
    }


    @Test
    public void testCalAmtMonthForPeriod() {
        try {
            // month config
            MPFDateCalculatorConfig monthConfig = new MPFDateCalculatorConfig()
                    .setDateOfBirth(LocalDate.of(1990, 5, 20))
                    .setMorePeriod(OptionalLong.of(4)); // the period needed after exemption

            monthConfig.setDateOfEmployment(LocalDate.of(2025, 1, 1));
            MPFAmtCalculator amtCctr1 = new MPFAmtCalculator(PayrollFrequency.MONTHLY, publicHolidays, monthConfig);
            ContributionPeriodMore contrPeriods1 = amtCctr1.calculateDate();
            int salariesSize1 = contrPeriods1.getPeriod().size() + contrPeriods1.getPeriodMore().size();
            BigDecimal[] salaries1 = new BigDecimal[salariesSize1];
            Arrays.fill(salaries1, new BigDecimal("10000"));
            amtCctr1.calculateAmount(CalUserType.EMPLOYEE, salaries1);
            System.out.println("------------DateOfEmployment = 20250101-------------");
            System.out.println(amtCctr1.getAmtReport());
            System.out.println(amtCctr1.getDateReport());


            monthConfig.setDateOfEmployment(LocalDate.of(2025, 1, 15));
            MPFAmtCalculator amtCctr2 = new MPFAmtCalculator(PayrollFrequency.MONTHLY, publicHolidays, monthConfig);
            ContributionPeriodMore contrPeriods2 = amtCctr2.calculateDate();
            int salariesSize2 = contrPeriods2.getPeriod().size() + contrPeriods2.getPeriodMore().size();
            BigDecimal[] salaries2 = new BigDecimal[salariesSize2];
            Arrays.fill(salaries2, new BigDecimal("10000"));
            amtCctr2.calculateAmount(CalUserType.EMPLOYEE, salaries2);
            System.out.println("------------DateOfEmployment = 20250115-------------");
            System.out.println(amtCctr2.getAmtReport());
            System.out.println(amtCctr2.getDateReport());

            monthConfig.setDateOfEmployment(LocalDate.of(2025, 1, 31));
            MPFAmtCalculator amtCctr3 = new MPFAmtCalculator(PayrollFrequency.MONTHLY, publicHolidays, monthConfig);
            ContributionPeriodMore contrPeriods3 = amtCctr3.calculateDate();
            int salariesSize3 = contrPeriods3.getPeriod().size() + contrPeriods3.getPeriodMore().size();
            BigDecimal[] salaries3 = new BigDecimal[salariesSize3];
            Arrays.fill(salaries3, new BigDecimal("10000"));
            amtCctr3.calculateAmount(CalUserType.EMPLOYEE, salaries3);
            System.out.println("------------DateOfEmployment = 20250131-------------");
            System.out.println(amtCctr3.getAmtReport());
            System.out.println(amtCctr3.getDateReport());

            monthConfig.setDateOfEmployment(LocalDate.of(2025, 2, 1));
            MPFAmtCalculator amtCctr4 = new MPFAmtCalculator(PayrollFrequency.MONTHLY, publicHolidays, monthConfig);
            ContributionPeriodMore contrPeriods4 = amtCctr4.calculateDate();
            int salariesSize4 = contrPeriods4.getPeriod().size() + contrPeriods4.getPeriodMore().size();
            BigDecimal[] salaries4 = new BigDecimal[salariesSize4];
            Arrays.fill(salaries4, new BigDecimal("10000"));
            amtCctr4.calculateAmount(CalUserType.EMPLOYEE, salaries4);
            System.out.println("------------DateOfEmployment = 20250201-------------");
            System.out.println(amtCctr4.getAmtReport());
            System.out.println(amtCctr4.getDateReport());

            monthConfig.setDateOfEmployment(LocalDate.of(2025, 2, 28));
            MPFAmtCalculator amtCctr5 = new MPFAmtCalculator(PayrollFrequency.MONTHLY, publicHolidays, monthConfig);
            ContributionPeriodMore contrPeriods5 = amtCctr5.calculateDate();
            int salariesSize5 = contrPeriods5.getPeriod().size() + contrPeriods5.getPeriodMore().size();
            BigDecimal[] salaries5 = new BigDecimal[salariesSize5];
            Arrays.fill(salaries5, new BigDecimal("10000"));
            amtCctr5.calculateAmount(CalUserType.EMPLOYEE, salaries5);
            System.out.println("------------DateOfEmployment = 20250228-------------");
            System.out.println(amtCctr5.getAmtReport());
            System.out.println(amtCctr5.getDateReport());

            //todo 20250701-20250731 error amount=0 should be amount=500.00  & 01/07/2025 to 31/07/2025 should be PeriodMore
            monthConfig.setDateOfEmployment(LocalDate.of(2025, 6, 1));
            MPFAmtCalculator amtCctr6 = new MPFAmtCalculator(PayrollFrequency.MONTHLY, publicHolidays, monthConfig);
            ContributionPeriodMore contrPeriods6 = amtCctr6.calculateDate();
            int salariesSize6 = contrPeriods6.getPeriod().size() + contrPeriods6.getPeriodMore().size();
            BigDecimal[] salaries6 = new BigDecimal[salariesSize6];
            Arrays.fill(salaries6, new BigDecimal("10000"));
            amtCctr6.calculateAmount(CalUserType.EMPLOYEE, salaries6);
            System.out.println("------------DateOfEmployment = 20250601-------------");
            System.out.println(amtCctr6.getAmtReport());
            System.out.println(amtCctr6.getDateReport());

            //todo 20250601-20250630 error amount=0 should be amount=500.00  & 20250601-20250630 should be PeriodMore
            monthConfig.setDateOfEmployment(LocalDate.of(2025, 5, 2));//72
            MPFAmtCalculator amtCctr7 = new MPFAmtCalculator(PayrollFrequency.MONTHLY, publicHolidays, monthConfig);
            ContributionPeriodMore contrPeriods7 = amtCctr7.calculateDate();
            int salariesSize7 = contrPeriods7.getPeriod().size() + contrPeriods7.getPeriodMore().size();
            BigDecimal[] salaries7 = new BigDecimal[salariesSize7];
            Arrays.fill(salaries7, new BigDecimal("10000"));
            amtCctr7.calculateAmount(CalUserType.EMPLOYEE, salaries7);
            System.out.println("------------DateOfEmployment = 20250502-------------");
            System.out.println(amtCctr7.getAmtReport());
            System.out.println(amtCctr7.getDateReport());


            //todo 20250801-20250831 error amount=0 should be amount=500.00  & 20250801-20250831 should be PeriodMore
            monthConfig.setDateOfEmployment(LocalDate.of(2025, 7, 2));//72
            MPFAmtCalculator amtCctr8 = new MPFAmtCalculator(PayrollFrequency.MONTHLY, publicHolidays, monthConfig);
            ContributionPeriodMore contrPeriods8 = amtCctr8.calculateDate();
            int salariesSize8 = contrPeriods8.getPeriod().size() + contrPeriods8.getPeriodMore().size();
            BigDecimal[] salaries8 = new BigDecimal[salariesSize8];
            Arrays.fill(salaries8, new BigDecimal("10000"));
            amtCctr8.calculateAmount(CalUserType.EMPLOYEE, salaries8);
            System.out.println("------------DateOfEmployment = 20250702-------------");
            System.out.println(amtCctr8.getAmtReport());
            System.out.println(amtCctr8.getDateReport());

            //todo 20250101-20250131 error amount=0 should be amount=500.00  & 20250101-20250131 should be PeriodMore
            // Deadline of Enrollment: 30/01/2025  should be  2025/02/03   add publicHolidays normal
            monthConfig.setDateOfEmployment(LocalDate.of(2024, 12, 31));//
            MPFAmtCalculator amtCctr9 = new MPFAmtCalculator(PayrollFrequency.MONTHLY, publicHolidays, monthConfig);
            ContributionPeriodMore contrPeriods9 = amtCctr9.calculateDate();
            int salariesSize9 = contrPeriods9.getPeriod().size() + contrPeriods9.getPeriodMore().size();
            BigDecimal[] salaries9 = new BigDecimal[salariesSize9];
            Arrays.fill(salaries9, new BigDecimal("10000"));
            amtCctr9.calculateAmount(CalUserType.EMPLOYEE, salaries9);
            System.out.println("------------DateOfEmployment = 20241202-------------");
            System.out.println(amtCctr9.getAmtReport());
            System.out.println(amtCctr9.getDateReport());
        } catch (Exception e) {
            System.out.println("error msg:" + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void testCalAmtNonMonthForERMCAndEEMC() {
        try {
            MPFDateCalculatorConfig nonCalendarMonthConfig = new MPFDateCalculatorConfig()
                    .setDateOfEmployment(LocalDate.of(2025, 1, 1))
                    .setDateOfBirth(LocalDate.of(1990, 5, 20))
                    .setMorePeriod(OptionalLong.of(3))
                    .setNonCalStartDay(1);

            MPFAmtCalculator amtCctr = new MPFAmtCalculator(PayrollFrequency.NON_CALENDAR_MONTH, publicHolidays, nonCalendarMonthConfig);
            ContributionPeriodMore contrPeriods = amtCctr.calculateDate();

            int salariesSize = contrPeriods.getPeriod().size() + contrPeriods.getPeriodMore().size();

            BigDecimal[] salaries1 = new BigDecimal[salariesSize];
            Arrays.fill(salaries1, new BigDecimal("10000"));
            amtCctr.calculateAmount(CalUserType.EMPLOYEE, salaries1);
            System.out.println("--------------EMPLOYEE salary = 10000-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());

            //todo 01/01/2025 to 01/01/2025   salary=10000, amount=50.00   ,but web calculator is 500
            amtCctr.calculateAmount(CalUserType.EMPLOYER, salaries1);
            System.out.println("--------------EMPLOYER salary = 10000-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());

            BigDecimal[] salaries2 = new BigDecimal[salariesSize];
            Arrays.fill(salaries2, new BigDecimal("7000"));
            amtCctr.calculateAmount(CalUserType.EMPLOYEE, salaries2);
            System.out.println("--------------EMPLOYEE salary = 7000-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());
            //todo  startDate=2025-01-01, endDate=2025-01-01 salary=7000, amount=50.00
            // & after all startDate=2025-01-02, endDate=2025-02-01 amout=0  ,but web calculator is 350
            amtCctr.calculateAmount(CalUserType.EMPLOYER, salaries2);
            System.out.println("--------------EMPLOYER salary = 7000-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());

            BigDecimal[] salaries3 = new BigDecimal[salariesSize];
            Arrays.fill(salaries3, new BigDecimal("7100"));
            //todo after startDate=2025-02-02, endDate=2025-03-01  salary=7100, amount=0   but web calculator is 355
            amtCctr.calculateAmount(CalUserType.EMPLOYEE, salaries3);
            System.out.println("--------------EMPLOYEE salary = 7100-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());
            //todo startDate=2025-01-01, endDate=2025-01-01 salary=7100 amount=50,but web calculator is 355
            //todo after startDate=2025-01-02, endDate=2025-02-01 salary=7100 all amount=0  ,but web calculator is 355
            amtCctr.calculateAmount(CalUserType.EMPLOYER, salaries3);
            System.out.println("--------------EMPLOYER salary = 7100-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());


            BigDecimal[] salaries4 = new BigDecimal[salariesSize];
            Arrays.fill(salaries4, new BigDecimal("30000"));
            //todo startDate=2025-02-02 endDate=2025-03-01 salary=30000, amount=1400.00  ,but web calculator is 1500
            amtCctr.calculateAmount(CalUserType.EMPLOYEE, salaries4);
            System.out.println("--------------EMPLOYEE salary = 30000-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());
            amtCctr.calculateAmount(CalUserType.EMPLOYER, salaries4);
            //todo startDate=2025-01-01, endDate=2025-01-01  amount=50         ,but web calculator is 1500
            //todo startDate=2025-02-02, endDate=2025-03-01  amount=1400       ,but web calculator is 1500
            System.out.println("--------------EMPLOYER salary = 30000-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());

            BigDecimal[] salaries5 = new BigDecimal[salariesSize];
            Arrays.fill(salaries5, new BigDecimal("40000"));
            //todo startDate=2025-02-02 endDate=2025-03-01 salary=40000, amount=1400.00  ,but web calculator is 1500
            amtCctr.calculateAmount(CalUserType.EMPLOYEE, salaries5);
            System.out.println("--------------EMPLOYEE salary = 40000-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());
            //todo startDate=2025-01-01, endDate=2025-01-01  amount=50         ,but web calculator is 1500
            //todo startDate=2025-02-02, endDate=2025-03-01  amount=1400       ,but web calculator is 1500
            amtCctr.calculateAmount(CalUserType.EMPLOYER, salaries5);
            System.out.println("--------------EMPLOYER salary = 40000-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());
        } catch (Exception e) {
            System.out.println("error msg:" + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void testCalAmtNonMonthForPeriod() {
        try {
            // month config nonCalendarMonthConfig
            MPFDateCalculatorConfig nonCalendarMonthConfig = new MPFDateCalculatorConfig()
                    .setDateOfEmployment(LocalDate.of(2025, 1, 1))
                    .setDateOfBirth(LocalDate.of(1990, 5, 20))
                    .setMorePeriod(OptionalLong.of(3));

            nonCalendarMonthConfig.setNonCalStartDay(1);
            nonCalendarMonthConfig.setDateOfEmployment(LocalDate.of(2025, 1, 1));
            MPFAmtCalculator amtCctr1 = new MPFAmtCalculator(PayrollFrequency.NON_CALENDAR_MONTH, publicHolidays, nonCalendarMonthConfig);
            ContributionPeriodMore contrPeriods1 = amtCctr1.calculateDate();
            int salariesSize1 = contrPeriods1.getPeriod().size() + contrPeriods1.getPeriodMore().size();
            BigDecimal[] salaries1 = new BigDecimal[salariesSize1];
            Arrays.fill(salaries1, new BigDecimal("10000"));
            amtCctr1.calculateAmount(CalUserType.EMPLOYEE, salaries1);
            System.out.println("------------DateOfEmployment = 20250101  NonCalStartDay=1-------------");
            System.out.println(amtCctr1.getAmtReport());
            System.out.println(amtCctr1.getDateReport());

            nonCalendarMonthConfig.setNonCalStartDay(1);
            nonCalendarMonthConfig.setDateOfEmployment(LocalDate.of(2025, 1, 2));
            MPFAmtCalculator amtCctr2 = new MPFAmtCalculator(PayrollFrequency.NON_CALENDAR_MONTH, publicHolidays, nonCalendarMonthConfig);
            ContributionPeriodMore contrPeriods2 = amtCctr2.calculateDate();
            int salariesSize2 = contrPeriods2.getPeriod().size() + contrPeriods2.getPeriodMore().size();
            BigDecimal[] salaries2 = new BigDecimal[salariesSize2];
            Arrays.fill(salaries2, new BigDecimal("10000"));
            amtCctr2.calculateAmount(CalUserType.EMPLOYEE, salaries2);
            System.out.println("------------DateOfEmployment = 20250102 NonCalStartDay=1 -------------");
            System.out.println(amtCctr2.getAmtReport());
            System.out.println(amtCctr2.getDateReport());


            //todo 02/02/2025 to 01/03/2025   salary=10000, amount=0   ,but web calculator is 500
            nonCalendarMonthConfig.setNonCalStartDay(1);
            nonCalendarMonthConfig.setDateOfEmployment(LocalDate.of(2025, 1, 3));
            MPFAmtCalculator amtCctr3 = new MPFAmtCalculator(PayrollFrequency.NON_CALENDAR_MONTH, publicHolidays, nonCalendarMonthConfig);
            ContributionPeriodMore contrPeriods3 = amtCctr3.calculateDate();
            int salariesSize3 = contrPeriods3.getPeriod().size() + contrPeriods3.getPeriodMore().size();
            BigDecimal[] salaries3 = new BigDecimal[salariesSize3];
            Arrays.fill(salaries3, new BigDecimal("10000"));
            amtCctr3.calculateAmount(CalUserType.EMPLOYEE, salaries3);
            System.out.println("------------DateOfEmployment = 20250103  NonCalStartDay=1-------------");
            System.out.println(amtCctr3.getAmtReport());
            System.out.println(amtCctr3.getDateReport());

            //todo 01/03/2025 to 29/03/2025  salary=10000, amount=0   ,but web calculator is 500,PeriodMore
            nonCalendarMonthConfig.setNonCalStartDay(29);
            nonCalendarMonthConfig.setDateOfEmployment(LocalDate.of(2025, 1, 30));
            MPFAmtCalculator amtCctr4 = new MPFAmtCalculator(PayrollFrequency.NON_CALENDAR_MONTH, publicHolidays, nonCalendarMonthConfig);
            ContributionPeriodMore contrPeriods4 = amtCctr4.calculateDate();
            int salariesSize4 = contrPeriods4.getPeriod().size() + contrPeriods4.getPeriodMore().size();
            BigDecimal[] salaries4 = new BigDecimal[salariesSize4];
            Arrays.fill(salaries4, new BigDecimal("10000"));
            amtCctr4.calculateAmount(CalUserType.EMPLOYEE, salaries4);
            System.out.println("------------DateOfEmployment = 20250130  NonCalStartDay=29-------------");
            System.out.println(amtCctr4.getAmtReport());
            System.out.println(amtCctr4.getDateReport());

            //todo  code error  Invalid date 'FEBRUARY 31'
//            nonCalendarMonthConfig.setNonCalStartDay(30);
//            nonCalendarMonthConfig.setDateOfEmployment(LocalDate.of(2025, 2, 1));
//            MPFAmtCalculator amtCctr5 = new MPFAmtCalculator(PayrollFrequency.NON_CALENDAR_MONTH, publicHolidays, nonCalendarMonthConfig);
//            ContributionPeriodMore contrPeriods5 = amtCctr5.calculateDate();
//            int salariesSize5 = contrPeriods5.getPeriod().size() + contrPeriods5.getPeriodMore().size();
//            BigDecimal[] salaries5 = new BigDecimal[salariesSize5];
//            Arrays.fill(salaries5, new BigDecimal("10000"));
//            amtCctr5.calculateAmount(CalUserType.EMPLOYEE, salaries5);
//            System.out.println("------------DateOfEmployment = 20250201 NonCalStartDay=30-------------");
//            System.out.println(amtCctr5.getAmtReport());
//            System.out.println(amtCctr5.getDateReport());

            //todo 20250701-20250731 error amount=0 should be amount=500.00  & 01/07/2025 to 31/07/2025 should be PeriodMore
            nonCalendarMonthConfig.setDateOfEmployment(LocalDate.of(2025, 6, 1));
            nonCalendarMonthConfig.setNonCalStartDay(15);
            MPFAmtCalculator amtCctr6 = new MPFAmtCalculator(PayrollFrequency.NON_CALENDAR_MONTH, publicHolidays, nonCalendarMonthConfig);
            ContributionPeriodMore contrPeriods6 = amtCctr6.calculateDate();
            int salariesSize6 = contrPeriods6.getPeriod().size() + contrPeriods6.getPeriodMore().size();
            BigDecimal[] salaries6 = new BigDecimal[salariesSize6];
            Arrays.fill(salaries6, new BigDecimal("10000"));
            amtCctr6.calculateAmount(CalUserType.EMPLOYEE, salaries6);
            System.out.println("------------DateOfEmployment = 20250601  NonCalStartDay=15-------------");
            System.out.println(amtCctr6.getAmtReport());
            System.out.println(amtCctr6.getDateReport());

            //todo 20250601-20250630 error amount=0 should be amount=500.00  & 20250601-20250630 should be PeriodMore
            nonCalendarMonthConfig.setDateOfEmployment(LocalDate.of(2024, 12, 1));//72
            nonCalendarMonthConfig.setNonCalStartDay(29);
            MPFAmtCalculator amtCctr7 = new MPFAmtCalculator(PayrollFrequency.NON_CALENDAR_MONTH, publicHolidays, nonCalendarMonthConfig);
            ContributionPeriodMore contrPeriods7 = amtCctr7.calculateDate();
            int salariesSize7 = contrPeriods7.getPeriod().size() + contrPeriods7.getPeriodMore().size();
            BigDecimal[] salaries7 = new BigDecimal[salariesSize7];
            Arrays.fill(salaries7, new BigDecimal("10000"));
            amtCctr7.calculateAmount(CalUserType.EMPLOYEE, salaries7);
            System.out.println("------------DateOfEmployment = 20250502-------------");
            System.out.println(amtCctr7.getAmtReport());
            System.out.println(amtCctr7.getDateReport());
        } catch (Exception e) {
            System.out.println("error msg:" + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void testCalAmtSemiMonthForERMCAndEEMC() {
        try {
            MPFDateCalculatorConfig semiMonthConfig = new MPFDateCalculatorConfig()
                    .setDateOfEmployment(LocalDate.of(2025, 1, 1))
                    .setDateOfBirth(LocalDate.of(1990, 5, 20))
                    .setMorePeriod(OptionalLong.of(4));

            MPFAmtCalculator amtCctr = new MPFAmtCalculator(PayrollFrequency.SEMI_MONTHLY, publicHolidays, semiMonthConfig);
            ContributionPeriodMore contrPeriods = amtCctr.calculateDate();

            int salariesSize = contrPeriods.getPeriod().size() + contrPeriods.getPeriodMore().size();

            BigDecimal[] salaries1 = new BigDecimal[salariesSize];
            Arrays.fill(salaries1, new BigDecimal("10000"));
            amtCctr.calculateAmount(CalUserType.EMPLOYEE, salaries1);
            System.out.println("--------------EMPLOYEE salary = 10000-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());
            amtCctr.calculateAmount(CalUserType.EMPLOYER, salaries1);
            System.out.println("--------------EMPLOYER salary = 10000-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());

            BigDecimal[] salaries2 = new BigDecimal[salariesSize];
            Arrays.fill(salaries2, new BigDecimal("3500"));
            amtCctr.calculateAmount(CalUserType.EMPLOYEE, salaries2);
            System.out.println("--------------EMPLOYEE salary = 3500-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());
            //todo  startDate=2025-01-01, endDate=2025-01-15 salary=3500, amount=0 ，but web calculator is 175
            amtCctr.calculateAmount(CalUserType.EMPLOYER, salaries2);
            System.out.println("--------------EMPLOYER salary = 3500-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());

            BigDecimal[] salaries3 = new BigDecimal[salariesSize];
            Arrays.fill(salaries3, new BigDecimal("7000"));
            amtCctr.calculateAmount(CalUserType.EMPLOYEE, salaries3);
            System.out.println("--------------EMPLOYEE salary = 7000-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());
            amtCctr.calculateAmount(CalUserType.EMPLOYER, salaries3);
            System.out.println("--------------EMPLOYER salary = 7000-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());


            BigDecimal[] salaries4 = new BigDecimal[salariesSize];
            Arrays.fill(salaries4, new BigDecimal("15000"));
            amtCctr.calculateAmount(CalUserType.EMPLOYEE, salaries4);
            System.out.println("--------------EMPLOYEE salary = 15000-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());
            amtCctr.calculateAmount(CalUserType.EMPLOYER, salaries4);
            System.out.println("--------------EMPLOYER salary = 15000-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());

            BigDecimal[] salaries5 = new BigDecimal[salariesSize];
            Arrays.fill(salaries5, new BigDecimal("30000"));
            amtCctr.calculateAmount(CalUserType.EMPLOYEE, salaries5);
            System.out.println("--------------EMPLOYEE salary = 30000-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());
            amtCctr.calculateAmount(CalUserType.EMPLOYER, salaries5);
            System.out.println("--------------EMPLOYER salary = 30000-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());
        } catch (Exception e) {
            System.out.println("error msg:" + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void testCalAmtSemiMonthForPeriod() {
        try {
            // semiMonthConfig config
            MPFDateCalculatorConfig semiMonthConfig = new MPFDateCalculatorConfig()
                    .setDateOfEmployment(LocalDate.of(2025, 1, 1))
                    .setDateOfBirth(LocalDate.of(1990, 5, 20))
                    .setMorePeriod(OptionalLong.of(4));

            semiMonthConfig.setDateOfEmployment(LocalDate.of(2025, 1, 1));
            MPFAmtCalculator amtCctr1 = new MPFAmtCalculator(PayrollFrequency.SEMI_MONTHLY, publicHolidays, semiMonthConfig);
            ContributionPeriodMore contrPeriods1 = amtCctr1.calculateDate();
            int salariesSize1 = contrPeriods1.getPeriod().size() + contrPeriods1.getPeriodMore().size();
            BigDecimal[] salaries1 = new BigDecimal[salariesSize1];
            Arrays.fill(salaries1, new BigDecimal("10000"));
            amtCctr1.calculateAmount(CalUserType.EMPLOYEE, salaries1);
            System.out.println("------------DateOfEmployment = 20250101-------------");
            System.out.println(amtCctr1.getAmtReport());
            System.out.println(amtCctr1.getDateReport());

             //todo Period 1: 15/01/2025 to 31/01/2025 Period 2: 01/02/2025 to 28/02/2025
            //todo  should be 15/01/2025 to 15/01/2025 16/01/2025 to 31/01/2025  01/02/2025 to 15/01/2025  16/02/2025 to 28/02/2025
            semiMonthConfig.setDateOfEmployment(LocalDate.of(2025, 1, 15));
            MPFAmtCalculator amtCctr2 = new MPFAmtCalculator(PayrollFrequency.SEMI_MONTHLY, publicHolidays, semiMonthConfig);
            ContributionPeriodMore contrPeriods2 = amtCctr2.calculateDate();
            int salariesSize2 = contrPeriods2.getPeriod().size() + contrPeriods2.getPeriodMore().size();
            BigDecimal[] salaries2 = new BigDecimal[salariesSize2];
            Arrays.fill(salaries2, new BigDecimal("10000"));
            amtCctr2.calculateAmount(CalUserType.EMPLOYEE, salaries2);
            System.out.println("------------DateOfEmployment = 20250115-------------");
            System.out.println(amtCctr2.getAmtReport());
            System.out.println(amtCctr2.getDateReport());

            semiMonthConfig.setDateOfEmployment(LocalDate.of(2025, 1, 31));
            MPFAmtCalculator amtCctr3 = new MPFAmtCalculator(PayrollFrequency.SEMI_MONTHLY, publicHolidays, semiMonthConfig);
            ContributionPeriodMore contrPeriods3 = amtCctr3.calculateDate();
            int salariesSize3 = contrPeriods3.getPeriod().size() + contrPeriods3.getPeriodMore().size();
            BigDecimal[] salaries3 = new BigDecimal[salariesSize3];
            Arrays.fill(salaries3, new BigDecimal("10000"));
            amtCctr3.calculateAmount(CalUserType.EMPLOYEE, salaries3);
            System.out.println("------------DateOfEmployment = 20250131-------------");
            System.out.println(amtCctr3.getAmtReport());
            System.out.println(amtCctr3.getDateReport());

            semiMonthConfig.setDateOfEmployment(LocalDate.of(2025, 2, 1));
            MPFAmtCalculator amtCctr4 = new MPFAmtCalculator(PayrollFrequency.SEMI_MONTHLY, publicHolidays, semiMonthConfig);
            ContributionPeriodMore contrPeriods4 = amtCctr4.calculateDate();
            int salariesSize4 = contrPeriods4.getPeriod().size() + contrPeriods4.getPeriodMore().size();
            BigDecimal[] salaries4 = new BigDecimal[salariesSize4];
            Arrays.fill(salaries4, new BigDecimal("10000"));
            amtCctr4.calculateAmount(CalUserType.EMPLOYEE, salaries4);
            System.out.println("------------DateOfEmployment = 20250201-------------");
            System.out.println(amtCctr4.getAmtReport());
            System.out.println(amtCctr4.getDateReport());

            semiMonthConfig.setDateOfEmployment(LocalDate.of(2025, 2, 28));
            MPFAmtCalculator amtCctr5 = new MPFAmtCalculator(PayrollFrequency.SEMI_MONTHLY, publicHolidays, semiMonthConfig);
            ContributionPeriodMore contrPeriods5 = amtCctr5.calculateDate();
            int salariesSize5 = contrPeriods5.getPeriod().size() + contrPeriods5.getPeriodMore().size();
            BigDecimal[] salaries5 = new BigDecimal[salariesSize5];
            Arrays.fill(salaries5, new BigDecimal("10000"));
            amtCctr5.calculateAmount(CalUserType.EMPLOYEE, salaries5);
            System.out.println("------------DateOfEmployment = 20250228-------------");
            System.out.println(amtCctr5.getAmtReport());
            System.out.println(amtCctr5.getDateReport());

            //todo 20250701-20250715 error amount=0 should be amount=500.00 & 2025-07-01-20250715 2025-07-16-20250731 should be PeriodMore
            semiMonthConfig.setDateOfEmployment(LocalDate.of(2025, 6, 1));
            MPFAmtCalculator amtCctr6 = new MPFAmtCalculator(PayrollFrequency.SEMI_MONTHLY, publicHolidays, semiMonthConfig);
            ContributionPeriodMore contrPeriods6 = amtCctr6.calculateDate();
            int salariesSize6 = contrPeriods6.getPeriod().size() + contrPeriods6.getPeriodMore().size();
            BigDecimal[] salaries6 = new BigDecimal[salariesSize6];
            Arrays.fill(salaries6, new BigDecimal("10000"));
            amtCctr6.calculateAmount(CalUserType.EMPLOYEE, salaries6);
            System.out.println("------------DateOfEmployment = 20250601-------------");
            System.out.println(amtCctr6.getAmtReport());
            System.out.println(amtCctr6.getDateReport());

            //todo 20250801-20250815 error amount=0 should be amount=500.00  & 20250801-20250815 20250816-20250831 should be PeriodMore
            semiMonthConfig.setDateOfEmployment(LocalDate.of(2025, 7, 2));//72
            MPFAmtCalculator amtCctr8 = new MPFAmtCalculator(PayrollFrequency.SEMI_MONTHLY, publicHolidays, semiMonthConfig);
            ContributionPeriodMore contrPeriods8 = amtCctr8.calculateDate();
            int salariesSize8 = contrPeriods8.getPeriod().size() + contrPeriods8.getPeriodMore().size();
            BigDecimal[] salaries8 = new BigDecimal[salariesSize8];
            Arrays.fill(salaries8, new BigDecimal("10000"));
            amtCctr8.calculateAmount(CalUserType.EMPLOYEE, salaries8);
            System.out.println("------------DateOfEmployment = 20250702-------------");
            System.out.println(amtCctr8.getAmtReport());
            System.out.println(amtCctr8.getDateReport());

            //todo 20250101-20250115 error amount=0 should be amount=500.00  & 20250101-20250115 20250116-20250131 should be PeriodMore
            semiMonthConfig.setDateOfEmployment(LocalDate.of(2024, 12, 2));//
            MPFAmtCalculator amtCctr9 = new MPFAmtCalculator(PayrollFrequency.SEMI_MONTHLY, publicHolidays, semiMonthConfig);
            ContributionPeriodMore contrPeriods9 = amtCctr9.calculateDate();
            int salariesSize9 = contrPeriods9.getPeriod().size() + contrPeriods9.getPeriodMore().size();
            BigDecimal[] salaries9 = new BigDecimal[salariesSize9];
            Arrays.fill(salaries9, new BigDecimal("10000"));
            amtCctr9.calculateAmount(CalUserType.EMPLOYEE, salaries9);
            System.out.println("------------DateOfEmployment = 20241202-------------");
            System.out.println(amtCctr9.getAmtReport());
            System.out.println(amtCctr9.getDateReport());
        } catch (Exception e) {
            System.out.println("error msg:" + e.getMessage());
            e.printStackTrace();
        }
    }



    @Test
    public void testCalAmtFortyNightForERMCAndEEMC() {
        try {
            MPFDateCalculatorConfig fortyNightConfig = new MPFDateCalculatorConfig()
                    .setDateOfEmployment(LocalDate.of(2025, 1, 2))
                    .setDateOfBirth(LocalDate.of(1990, 5, 20))
                    .setMorePeriod(OptionalLong.of(3))
                    .setPryllFnightStartDate(LocalDate.of(2025, 1, 1))
                    ;

            MPFAmtCalculator amtCctr = new MPFAmtCalculator(PayrollFrequency.FORTNIGHTLY, publicHolidays, fortyNightConfig);
            ContributionPeriodMore contrPeriods = amtCctr.calculateDate();

            int salariesSize = contrPeriods.getPeriod().size() + contrPeriods.getPeriodMore().size();

            BigDecimal[] salaries1 = new BigDecimal[salariesSize];
            Arrays.fill(salaries1, new BigDecimal("10000"));
            amtCctr.calculateAmount(CalUserType.EMPLOYEE, salaries1);
            System.out.println("--------------EMPLOYEE salary = 10000-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());
            amtCctr.calculateAmount(CalUserType.EMPLOYER, salaries1);
            System.out.println("--------------EMPLOYER salary = 10000-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());

            BigDecimal[] salaries2 = new BigDecimal[salariesSize];
            Arrays.fill(salaries2, new BigDecimal("3500"));
            amtCctr.calculateAmount(CalUserType.EMPLOYEE, salaries2);
            System.out.println("--------------EMPLOYEE salary = 3500-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());
            //todo amount=0,but 175
            amtCctr.calculateAmount(CalUserType.EMPLOYER, salaries2);
            System.out.println("--------------EMPLOYER salary = 3500-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());


            BigDecimal[] salaries3 = new BigDecimal[salariesSize];
            Arrays.fill(salaries3, new BigDecimal("7000"));
            amtCctr.calculateAmount(CalUserType.EMPLOYEE, salaries3);
            System.out.println("--------------EMPLOYEE salary = 7000-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());
            amtCctr.calculateAmount(CalUserType.EMPLOYER, salaries3);
            System.out.println("--------------EMPLOYER salary = 7000-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());

            //todo startDate=2025-01-02, endDate=2025-01-14  salary=15000, amount=650,but 700
            BigDecimal[] salaries4 = new BigDecimal[salariesSize];
            Arrays.fill(salaries4, new BigDecimal("15000"));
            amtCctr.calculateAmount(CalUserType.EMPLOYEE, salaries4);
            System.out.println("--------------EMPLOYEE salary = 15000-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());
            amtCctr.calculateAmount(CalUserType.EMPLOYER, salaries4);
            System.out.println("--------------EMPLOYER salary = 15000-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());

            BigDecimal[] salaries5 = new BigDecimal[salariesSize];
            Arrays.fill(salaries5, new BigDecimal("30000"));
            amtCctr.calculateAmount(CalUserType.EMPLOYEE, salaries5);
            System.out.println("--------------EMPLOYEE salary = 30000-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());
            //todo startDate=2025-01-02, endDate=2025-01-14  salary=15000, amount=650,but 700
            amtCctr.calculateAmount(CalUserType.EMPLOYER, salaries5);
            System.out.println("--------------EMPLOYER salary = 30000-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());
        } catch (Exception e) {
            System.out.println("error msg:" + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void testCalAmtFortyNightForPeriod() {
        try {
            //todo setDateOfEmployment(LocalDate.of(2025, 1, 1)) &
            // setPryllFnightStartDate(LocalDate.of(2025, 1, 1)) only one Period,no MorePeriod
//            MPFDateCalculatorConfig fortyNightConfig = new MPFDateCalculatorConfig()
//                    .setDateOfEmployment(LocalDate.of(2025, 1, 1))
//                    .setDateOfBirth(LocalDate.of(1990, 5, 20))
//                    .setMorePeriod(OptionalLong.of(3))
//                    .setPryllFnightStartDate(LocalDate.of(2025, 1, 1))
//            ;

            // fortyNightConfig config
            MPFDateCalculatorConfig fortyNightConfig = new MPFDateCalculatorConfig()
                    .setDateOfBirth(LocalDate.of(1990, 5, 20))
                    .setMorePeriod(OptionalLong.of(3));

            fortyNightConfig.setDateOfEmployment(LocalDate.of(2025, 1, 2));
            fortyNightConfig.setPryllFnightStartDate(LocalDate.of(2025, 1, 1));
            MPFAmtCalculator amtCctr1 = new MPFAmtCalculator(PayrollFrequency.FORTNIGHTLY, publicHolidays, fortyNightConfig);
            ContributionPeriodMore contrPeriods1 = amtCctr1.calculateDate();
            int salariesSize1 = contrPeriods1.getPeriod().size() + contrPeriods1.getPeriodMore().size();
            BigDecimal[] salaries1 = new BigDecimal[salariesSize1];
            Arrays.fill(salaries1, new BigDecimal("10000"));
            amtCctr1.calculateAmount(CalUserType.EMPLOYEE, salaries1);
            System.out.println("------------DateOfEmployment = 20250102 start=20250101-------------");
            System.out.println(amtCctr1.getAmtReport());
            System.out.println(amtCctr1.getDateReport());

            //todo setDateOfEmployment(LocalDate.of(2025, 1, 2)) &
            // setPryllFnightStartDate(LocalDate.of(2025, 1, 2)) only one Period,no MorePeriod
            fortyNightConfig.setDateOfEmployment(LocalDate.of(2025, 1, 2));
            fortyNightConfig.setPryllFnightStartDate(LocalDate.of(2025, 1, 2));
            MPFAmtCalculator amtCctr2 = new MPFAmtCalculator(PayrollFrequency.FORTNIGHTLY, publicHolidays, fortyNightConfig);
            ContributionPeriodMore contrPeriods2 = amtCctr2.calculateDate();
            int salariesSize2 = contrPeriods2.getPeriod().size() + contrPeriods2.getPeriodMore().size();
            BigDecimal[] salaries2 = new BigDecimal[salariesSize2];
            Arrays.fill(salaries2, new BigDecimal("10000"));
            amtCctr2.calculateAmount(CalUserType.EMPLOYEE, salaries2);
            System.out.println("------------DateOfEmployment = 20250102 start=20250102--------------");
            System.out.println(amtCctr2.getAmtReport());
            System.out.println(amtCctr2.getDateReport());

            fortyNightConfig.setDateOfEmployment(LocalDate.of(2025, 1, 2));
            fortyNightConfig.setPryllFnightStartDate(LocalDate.of(2025, 1, 3));
            MPFAmtCalculator amtCctr3 = new MPFAmtCalculator(PayrollFrequency.FORTNIGHTLY, publicHolidays, fortyNightConfig);
            ContributionPeriodMore contrPeriods3 = amtCctr3.calculateDate();
            int salariesSize3 = contrPeriods3.getPeriod().size() + contrPeriods3.getPeriodMore().size();
            BigDecimal[] salaries3 = new BigDecimal[salariesSize3];
            Arrays.fill(salaries3, new BigDecimal("10000"));
            amtCctr3.calculateAmount(CalUserType.EMPLOYEE, salaries3);
            System.out.println("------------DateOfEmployment = 20250102 start=20250103-------------");
            System.out.println(amtCctr3.getAmtReport());
            System.out.println(amtCctr3.getDateReport());

            //todo period error startDate=2025-02-01, endDate=2025-01-14
            //todo Employee contribution days: 0 Employer contribution days: -17 error

            //todo  2025/02/01 - 2025/02/11	10,000.00	500.00	0.00
            //todo  2025/02/12 - 2025/02/25	10,000.00	500.00	0.00
            //todo  2025/02/26 - 2025/03/11	10,000.00	500.00	0.00
            //todo  2025/03/12 - 2025/03/25	10,000.00	500.00	500.00
            //todo  2025/03/26 - 2025/04/08	10,000.00	500.00	500.00
            //todo  2025/04/09 - 2025/04/22	10,000.00	500.00	500.00
            fortyNightConfig.setDateOfEmployment(LocalDate.of(2025, 2, 1));
            fortyNightConfig.setPryllFnightStartDate(LocalDate.of(2025, 1, 1));
            MPFAmtCalculator amtCctr4 = new MPFAmtCalculator(PayrollFrequency.FORTNIGHTLY, publicHolidays, fortyNightConfig);
            ContributionPeriodMore contrPeriods4 = amtCctr4.calculateDate();
            int salariesSize4 = contrPeriods4.getPeriod().size() + contrPeriods4.getPeriodMore().size();
            BigDecimal[] salaries4 = new BigDecimal[salariesSize4];
            Arrays.fill(salaries4, new BigDecimal("10000"));
            amtCctr4.calculateAmount(CalUserType.EMPLOYEE, salaries4);
            System.out.println("------------DateOfEmployment = 20250201 start=20250101-------------");
            System.out.println(amtCctr4.getAmtReport());
            System.out.println(amtCctr4.getDateReport());


            fortyNightConfig.setDateOfEmployment(LocalDate.of(2025, 6, 1));
            fortyNightConfig.setPryllFnightStartDate(LocalDate.of(2025, 6, 2));
            MPFAmtCalculator amtCctr5 = new MPFAmtCalculator(PayrollFrequency.FORTNIGHTLY, publicHolidays, fortyNightConfig);
            ContributionPeriodMore contrPeriods5 = amtCctr5.calculateDate();
            int salariesSize5 = contrPeriods5.getPeriod().size() + contrPeriods5.getPeriodMore().size();
            BigDecimal[] salaries5 = new BigDecimal[salariesSize5];
            Arrays.fill(salaries5, new BigDecimal("10000"));
            amtCctr5.calculateAmount(CalUserType.EMPLOYEE, salaries5);
            System.out.println("------------DateOfEmployment = 20250601 start=20250602-------------");
            System.out.println(amtCctr5.getAmtReport());
            System.out.println(amtCctr5.getDateReport());

            fortyNightConfig.setDateOfEmployment(LocalDate.of(2024, 12, 1));
            fortyNightConfig.setPryllFnightStartDate(LocalDate.of(2024, 12, 2));
            MPFAmtCalculator amtCctr6 = new MPFAmtCalculator(PayrollFrequency.FORTNIGHTLY, publicHolidays, fortyNightConfig);
            ContributionPeriodMore contrPeriods6 = amtCctr6.calculateDate();
            int salariesSize6 = contrPeriods6.getPeriod().size() + contrPeriods6.getPeriodMore().size();
            BigDecimal[] salaries6 = new BigDecimal[salariesSize6];
            Arrays.fill(salaries6, new BigDecimal("10000"));
            amtCctr6.calculateAmount(CalUserType.EMPLOYEE, salaries6);
            System.out.println("------------DateOfEmployment = 20241201 start=20241202-------------");
            System.out.println(amtCctr6.getAmtReport());
            System.out.println(amtCctr6.getDateReport());


        } catch (Exception e) {
            System.out.println("error msg:" + e.getMessage());
            e.printStackTrace();
        }
    }


    @Test
    public void testCalAmtWeekForERMCAndEEMC() {
        try {
            MPFDateCalculatorConfig weeklyConfig = new MPFDateCalculatorConfig()
                    .setDateOfEmployment(LocalDate.of(2025, 1, 1))
                    .setDateOfBirth(LocalDate.of(1990, 5, 20))
                    .setMorePeriod(OptionalLong.of(3))
                    .setWeeklyCycle(DayOfWeek.WEDNESDAY)
            ;


            MPFAmtCalculator amtCctr = new MPFAmtCalculator(PayrollFrequency.WEEKLY, publicHolidays, weeklyConfig);
            ContributionPeriodMore contrPeriods = amtCctr.calculateDate();

            int salariesSize = contrPeriods.getPeriod().size() + contrPeriods.getPeriodMore().size();

            BigDecimal[] salaries1 = new BigDecimal[salariesSize];
            Arrays.fill(salaries1, new BigDecimal("10000"));
            amtCctr.calculateAmount(CalUserType.EMPLOYEE, salaries1);
            System.out.println("--------------EMPLOYEE salary = 10000-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());
            amtCctr.calculateAmount(CalUserType.EMPLOYER, salaries1);
            //todo startDate=2025-01-01, endDate=2025-01-01 salary=10000, amount=50.00 but 350
            System.out.println("--------------EMPLOYER salary = 10000-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());

            BigDecimal[] salaries2 = new BigDecimal[salariesSize];
            Arrays.fill(salaries2, new BigDecimal("3500"));
            amtCctr.calculateAmount(CalUserType.EMPLOYEE, salaries2);
            System.out.println("--------------EMPLOYEE salary = 3500-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());
            //todo startDate=2025-01-01, endDate=2025-01-01 salary=3500, amount=50.00 but 175
            amtCctr.calculateAmount(CalUserType.EMPLOYER, salaries2);
            System.out.println("--------------EMPLOYER salary = 3500-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());


            BigDecimal[] salaries3 = new BigDecimal[salariesSize];
            Arrays.fill(salaries3, new BigDecimal("1500"));
            amtCctr.calculateAmount(CalUserType.EMPLOYEE, salaries3);
            System.out.println("--------------EMPLOYEE salary = 1500-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());
            //todo all amount = 0 but 75
            amtCctr.calculateAmount(CalUserType.EMPLOYER, salaries3);
            System.out.println("--------------EMPLOYER salary = 1500-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());

            BigDecimal[] salaries4 = new BigDecimal[salariesSize];
            Arrays.fill(salaries4, new BigDecimal("7000"));
            amtCctr.calculateAmount(CalUserType.EMPLOYEE, salaries4);
            System.out.println("--------------EMPLOYEE salary = 7000-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());
            //todo startDate=2025-01-01, endDate=2025-01-01 salary=7000, amount=50.00 but 350
            amtCctr.calculateAmount(CalUserType.EMPLOYER, salaries4);
            System.out.println("--------------EMPLOYER salary = 7000-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());

            BigDecimal[] salaries5 = new BigDecimal[salariesSize];
            Arrays.fill(salaries5, new BigDecimal("2000"));
            amtCctr.calculateAmount(CalUserType.EMPLOYEE, salaries5);
            System.out.println("--------------EMPLOYEE salary = 2000-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());
            //todo startDate=2025-01-01, endDate=2025-01-01 salary=2000, amount=50.00 but 100
            amtCctr.calculateAmount(CalUserType.EMPLOYER, salaries5);
            System.out.println("--------------EMPLOYER salary = 2000-----------");
            System.out.println(amtCctr.getAmtReport());
            System.out.println(amtCctr.getDateReport());
        } catch (Exception e) {
            System.out.println("error msg:" + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void testCalAmtWeekForPeriod() {
        try {
            MPFDateCalculatorConfig weeklyConfig = new MPFDateCalculatorConfig()
                    .setDateOfBirth(LocalDate.of(1990, 5, 20))
                    .setMorePeriod(OptionalLong.of(3));

            weeklyConfig.setDateOfEmployment(LocalDate.of(2025, 1, 1));
            weeklyConfig.setWeeklyCycle(DayOfWeek.SUNDAY);
            MPFAmtCalculator amtCctr1 = new MPFAmtCalculator(PayrollFrequency.WEEKLY, publicHolidays, weeklyConfig);
            ContributionPeriodMore contrPeriods1 = amtCctr1.calculateDate();
            int salariesSize1 = contrPeriods1.getPeriod().size() + contrPeriods1.getPeriodMore().size();
            BigDecimal[] salaries1 = new BigDecimal[salariesSize1];
            Arrays.fill(salaries1, new BigDecimal("5000"));
            amtCctr1.calculateAmount(CalUserType.EMPLOYEE, salaries1);
            System.out.println("------------DateOfEmployment = 20250101 SUNDAY-------------");
            System.out.println(amtCctr1.getAmtReport());
            System.out.println(amtCctr1.getDateReport());

            weeklyConfig.setDateOfEmployment(LocalDate.of(2025, 1, 1));
            weeklyConfig.setWeeklyCycle(DayOfWeek.WEDNESDAY);
            MPFAmtCalculator amtCctr2 = new MPFAmtCalculator(PayrollFrequency.WEEKLY, publicHolidays, weeklyConfig);
            ContributionPeriodMore contrPeriods2 = amtCctr2.calculateDate();
            int salariesSize2 = contrPeriods2.getPeriod().size() + contrPeriods2.getPeriodMore().size();
            BigDecimal[] salaries2 = new BigDecimal[salariesSize2];
            Arrays.fill(salaries2, new BigDecimal("5000"));
            amtCctr2.calculateAmount(CalUserType.EMPLOYEE, salaries2);
            System.out.println("------------DateOfEmployment = 20250102 WEDNESDAY--------------");
            System.out.println(amtCctr2.getAmtReport());
            System.out.println(amtCctr2.getDateReport());

            weeklyConfig.setDateOfEmployment(LocalDate.of(2025, 1, 2));
            weeklyConfig.setWeeklyCycle(DayOfWeek.SUNDAY);
            MPFAmtCalculator amtCctr3 = new MPFAmtCalculator(PayrollFrequency.WEEKLY, publicHolidays, weeklyConfig);
            ContributionPeriodMore contrPeriods3 = amtCctr3.calculateDate();
            int salariesSize3 = contrPeriods3.getPeriod().size() + contrPeriods3.getPeriodMore().size();
            BigDecimal[] salaries3 = new BigDecimal[salariesSize3];
            Arrays.fill(salaries3, new BigDecimal("5000"));
            amtCctr3.calculateAmount(CalUserType.EMPLOYEE, salaries3);
            System.out.println("------------DateOfEmployment = 20250102 SUNDAY-------------");
            System.out.println(amtCctr3.getAmtReport());
            System.out.println(amtCctr3.getDateReport());

            //todo startDate=2025-03-03, endDate=2025-03-09 amount=0 but 250
            weeklyConfig.setDateOfEmployment(LocalDate.of(2025, 2, 1));
            weeklyConfig.setWeeklyCycle(DayOfWeek.SUNDAY);
            MPFAmtCalculator amtCctr4 = new MPFAmtCalculator(PayrollFrequency.WEEKLY, publicHolidays, weeklyConfig);
            ContributionPeriodMore contrPeriods4 = amtCctr4.calculateDate();
            int salariesSize4 = contrPeriods4.getPeriod().size() + contrPeriods4.getPeriodMore().size();
            BigDecimal[] salaries4 = new BigDecimal[salariesSize4];
            Arrays.fill(salaries4, new BigDecimal("5000"));
            amtCctr4.calculateAmount(CalUserType.EMPLOYEE, salaries4);
            System.out.println("------------DateOfEmployment = 20250201 SUNDAY-------------");
            System.out.println(amtCctr4.getAmtReport());
            System.out.println(amtCctr4.getDateReport());


            weeklyConfig.setDateOfEmployment(LocalDate.of(2025, 6, 1));
            weeklyConfig.setWeeklyCycle(DayOfWeek.SUNDAY);
            MPFAmtCalculator amtCctr5 = new MPFAmtCalculator(PayrollFrequency.WEEKLY, publicHolidays, weeklyConfig);
            ContributionPeriodMore contrPeriods5 = amtCctr5.calculateDate();
            int salariesSize5 = contrPeriods5.getPeriod().size() + contrPeriods5.getPeriodMore().size();
            BigDecimal[] salaries5 = new BigDecimal[salariesSize5];
            Arrays.fill(salaries5, new BigDecimal("5000"));
            amtCctr5.calculateAmount(CalUserType.EMPLOYEE, salaries5);
            System.out.println("------------DateOfEmployment = 20250601 SUNDAY-------------");
            System.out.println(amtCctr5.getAmtReport());
            System.out.println(amtCctr5.getDateReport());

            weeklyConfig.setDateOfEmployment(LocalDate.of(2024, 12, 1));
            weeklyConfig.setWeeklyCycle(DayOfWeek.SUNDAY);
            MPFAmtCalculator amtCctr6 = new MPFAmtCalculator(PayrollFrequency.WEEKLY, publicHolidays, weeklyConfig);
            ContributionPeriodMore contrPeriods6 = amtCctr6.calculateDate();
            int salariesSize6 = contrPeriods6.getPeriod().size() + contrPeriods6.getPeriodMore().size();
            BigDecimal[] salaries6 = new BigDecimal[salariesSize6];
            Arrays.fill(salaries6, new BigDecimal("5000"));
            amtCctr6.calculateAmount(CalUserType.EMPLOYEE, salaries6);
            System.out.println("------------DateOfEmployment = 20241201 SUNDAY-------------");
            System.out.println(amtCctr6.getAmtReport());
            System.out.println(amtCctr6.getDateReport());


        } catch (Exception e) {
            System.out.println("error msg:" + e.getMessage());
            e.printStackTrace();
        }
    }

}
