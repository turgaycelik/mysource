package com.atlassian.jira.gadgets.system.util;

import com.atlassian.jira.charts.ChartFactory;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.rest.v1.model.errors.ValidationError;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.Collection;

/**
 * Helper for validating date related parameters and values for resource classes.
 *
 * @since v4.0
 */
public class ResourceDateValidator
{
    private static String MAX_DAYS_AP_PREFIX = "jira.chart.days.previous.limit.";
    private static final int INVALID_DAYS = -1;

    private final ApplicationProperties applicationProperties;

    public ResourceDateValidator(final ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    public int validateDaysPrevious(String fieldName, final ChartFactory.PeriodName period, String days, Collection<ValidationError> errors)
    {
        final int numberOfDays;
        try
        {
            numberOfDays = Integer.valueOf(days);
            if (numberOfDays < 0)
            {
                errors.add(new ValidationError(fieldName, "gadget.common.negative.days"));
            }
        }
        catch (NumberFormatException e)
        {
            errors.add(new ValidationError(fieldName, "gadget.common.days.nan"));
            return INVALID_DAYS;
        }

        // unless we're valid, there's no point checking the period against the days
        if (numberOfDays >= 0 && period != null)
        {
            validateDaysAgainstPeriod(fieldName, period, numberOfDays, errors);
        }
        return numberOfDays;
    }

    public ChartFactory.PeriodName validatePeriod(String fieldName, String periodName, Collection<ValidationError> errors)
    {
        try
        {
            return ChartFactory.PeriodName.valueOf(periodName);
        }
        catch (IllegalArgumentException e)
        {
            errors.add(new ValidationError(fieldName, "gadget.common.invalid.period"));
        }
        return null;
    }

    /**
     * Ensures that the number of days specified does not exceed the upper limit for the given period, as defined in
     * JIRA's properties.
     *
     * @param fieldName the name of the field
     * @param period the period
     * @param days the number of days
     * @param errors the errors to return
     */
    void validateDaysAgainstPeriod(String fieldName, ChartFactory.PeriodName period, int days, Collection<ValidationError> errors)
    {
        final String maxDaysPropertyKey = MAX_DAYS_AP_PREFIX + period.toString();
        String maxDaysValue = applicationProperties.getDefaultBackedString(maxDaysPropertyKey);
        //if for some reason no application property with this value is available, fall back to defaults.
        if (StringUtils.isBlank(maxDaysValue))
        {
            switch (period)
            {
                case hourly:
                    maxDaysValue = "10";
                    break;
                case daily:
                    maxDaysValue = "300";
                    break;
                case weekly:
                    maxDaysValue = "1750";
                    break;
                case monthly:
                    maxDaysValue = "7500";
                    break;
                case quarterly:
                    maxDaysValue = "22500";
                    break;
                case yearly:
                    maxDaysValue = "36500";
                    break;
                default:
                    maxDaysValue = "300";
            }
        }
        final Integer limitForPeriod = Integer.valueOf(maxDaysValue);

        if (limitForPeriod < days)
        {
            errors.add(new ValidationError(fieldName, "gadget.common.days.overlimit.for.period", Arrays.asList(limitForPeriod.toString(), period.toString())));
        }
    }
}
