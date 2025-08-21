# MPF Payroll Calculator - Java Implementation

This is a comprehensive Java implementation of the MPF (Mandatory Provident Fund) payroll calculation logic, converted from the original JavaScript code.

## Overview

The calculator supports 5 different payroll frequency types:
1. **Monthly** - Calendar month (1st to last day of month)
2. **Non-Calendar Month** - Custom month periods (e.g., 15th to 14th)
3. **Semi-Monthly** - Twice per month (1st-15th, 16th-end of month)
4. **Fortnightly** - Every 14 days
5. **Weekly** - Every 7 days with configurable start day

## File Structure

### Core Classes

1. **`org.example.enums.PayrollFrequency.java`** - Enum defining the 5 payroll frequency types
2. **`WeeklyCycle.java`** - Enum defining weekly cycles (Monday-Sunday, Tuesday-Monday, etc.)
3. **`MPFCalculatorConstants.java`** - All constants used in calculations
4. **`DateUtils.java`** - Utility methods for date calculations
5. **`ContributionPeriod.java`** - Data class representing a contribution period
6. **`MPFPayrollCalculator.java`** - Main calculator with all calculation logic
7. **`MPFCalculatorExample.java`** - Example usage and demonstration

## Key Features

### Date Calculations
- Handles leap years correctly
- Public holiday awareness
- Age-based adjustments (18th and 65th birthdays)
- Weekend and holiday date adjustments

### Payroll Frequency Support

#### Monthly (Calendar Month)
- Periods run from 1st to last day of each month
- Handles partial first month if employment starts mid-month
- Automatically adjusts for different month lengths

#### Non-Calendar Month
- Configurable start day (e.g., 15th of each month)
- Handles months where the start day doesn't exist (e.g., 31st in February)
- Proper period boundary calculations

#### Semi-Monthly
- First period: 1st to 15th
- Second period: 16th to last day of month
- Special logic for employment starting mid-period

#### Fortnightly
- 14-day cycles starting from a configurable date
- Handles alignment with employment start date
- Proper period boundary management

#### Weekly
- 7-day cycles with configurable start day
- Supports all weekly cycles (Monday-Sunday through Sunday-Saturday)
- Employment date alignment

### Contribution Calculations
- Employee contribution date differences
- Employer contribution date differences
- Age-based contribution rules (before 18, after 65)
- Complete cycle validation

## Usage Example

```java
import org.example.enums.PayrollFrequency;

// Create calculator with public holidays
List<LocalDate> publicHolidays = Arrays.asList(
        LocalDate.of(2024, 1, 1),   // New Year's Day
        LocalDate.of(2024, 12, 25)  // Christmas Day
);

MPFPayrollCalculator calculator = new MPFPayrollCalculator(publicHolidays);

// Set employee details
calculator.

setDateOfEmployment(LocalDate.of(2024, 1,15));
        calculator.

setDateOfBirth(LocalDate.of(1990, 5,20));
        calculator.

setDeadlineForEnrol(LocalDate.of(2024, 12,31));

// Calculate monthly contribution periods
List<ContributionPeriod> periods = calculator.calculateContributionPeriods(PayrollFrequency.MONTHLY);

// Calculate contribution days for a specific period
long eeDays = calculator.getEeContrDateDiff(periodStart, periodEnd);
long erDays = calculator.getErContrDateDiff(periodStart, periodEnd);
```

## Key Constants

### Contribution Limits
- Monthly: $4,000 - $20,000
- Semi-Monthly: $2,000 - $10,000
- Fortnightly: $1,820 - $9,100
- Weekly: $910 - $4,550
- Daily: $130 - $650

### Important Dates
- Effective Date: 1 Feb 2003
- No Saturday Deadline: 1 Dec 2008

## Special Logic

### Age-Based Adjustments
- **18th Birthday**: Contributions start from the beginning of the payroll period containing the 18th birthday
- **65th Birthday**: Contributions stop at the end of the payroll period containing the 65th birthday

### Public Holiday Handling
- Deadline dates are adjusted if they fall on public holidays
- Saturday deadline adjustments for certain date ranges

### Employment Date Alignment
- First period may be partial if employment starts mid-period
- Period start date flags indicate if the first period starts on the expected boundary

## Conversion Notes

This Java implementation faithfully converts the original JavaScript logic while:
- Using proper Java date/time APIs (`LocalDate` instead of `Date`)
- Implementing type-safe enums instead of integer constants
- Following Java naming conventions and best practices
- Adding comprehensive documentation and examples
- Maintaining the exact calculation logic and special cases

## Testing

Run the example class to see the calculator in action:
```bash
javac *.java
java MPFCalculatorExample
```

The example demonstrates all payroll frequency types and shows the calculated contribution periods for each.

## Dependencies

- Java 17+ (uses modern switch expressions and text blocks)
- No external dependencies required

## Architecture

The code is designed with separation of concerns:
- **DateUtils**: Pure date calculation functions
- **MPFPayrollCalculator**: Core business logic
- **Enums**: Type-safe constants and configurations
- **ContributionPeriod**: Data transfer object
- **Example**: Demonstration and testing

This modular design makes it easy to:
- Unit test individual components
- Extend with new payroll frequency types
- Integrate into larger applications
- Maintain and debug calculation logic
