package com.atlassian.jira.issue.customfields.converters;

import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.security.JiraAuthenticationContext;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParsePosition;

public class DoubleConverterImpl implements DoubleConverter
{
    private static final DecimalFormat CHANGELOG_FORMAT;
    static
    {
        CHANGELOG_FORMAT = new DecimalFormat();
        CHANGELOG_FORMAT.setGroupingUsed(false);
        CHANGELOG_FORMAT.setMinimumFractionDigits(0);
        DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols();
        formatSymbols.setDecimalSeparator('.');
        CHANGELOG_FORMAT.setDecimalFormatSymbols(formatSymbols);
    }

    private static final DecimalFormat LUCENE_FORMAT;
    static
    {
        LUCENE_FORMAT = new DecimalFormat();
        LUCENE_FORMAT.setGroupingUsed(false);
        LUCENE_FORMAT.setMinimumIntegerDigits(14);
        LUCENE_FORMAT.setMaximumIntegerDigits(14);
        LUCENE_FORMAT.setMinimumFractionDigits(3);
        LUCENE_FORMAT.setMaximumFractionDigits(3);
        DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols();
        formatSymbols.setDecimalSeparator('.');
        LUCENE_FORMAT.setDecimalFormatSymbols(formatSymbols);
    }

    private static final Double MAX_VALUE = new Double("100000000000000");

    private final JiraAuthenticationContext jiraAuthenticationContext;

    public DoubleConverterImpl(JiraAuthenticationContext jiraAuthenticationContext) {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    public String getString(Double value)
    {
        if (value != null)
        {
            return getDisplayFormat().format(value);
        }
        else
        {
            return null;
        }
    }

    public String getStringForLucene(Double value)
    {
        if (value != null)
        {
            return LUCENE_FORMAT.format(value);
        }
        else
        {
            return null;
        }
    }

    public String getStringForLucene(String value)
    {
        return getStringForLucene(getDouble(value));
    }

    public String getDisplayDoubleFromLucene(String luceneValue)
    {
        Double d = getDouble(luceneValue);
        return getString(d);
    }

    public String getStringForChangelog(final Double value)
    {
        if (value != null)
        {
            return CHANGELOG_FORMAT.format(value);
        }
        else
        {
            return null;
        }
    }

    public Double getDouble(String stringValue) throws FieldValidationException
    {
        if (stringValue != null)
        {
            final ParsePosition pp = new ParsePosition(0);
            final Number aNumber = getDisplayFormat().parse(stringValue, pp);
            if (aNumber == null || pp.getIndex() != stringValue.length()) {
                throw new FieldValidationException(
                        jiraAuthenticationContext.getI18nHelper().getText("fields.validation.number.invalid",
                                stringValue));
            }

            final Double aDouble = aNumber.doubleValue();

            if (aDouble.compareTo(MAX_VALUE) > 0)
            {
                throw new FieldValidationException(jiraAuthenticationContext.getI18nHelper().getText(
                        "fields.validation.number.too.large", stringValue, getString(MAX_VALUE)));
            }

            return aDouble;
        }
        else
        {
            return null;
        }
    }

    /**
     * Returns current display format for the user. Do not cache as it may change.
     *
     * @return display format depending on user's locale
     */
    private DecimalFormat getDisplayFormat() {
        DecimalFormat displayFormat = new DecimalFormat();
        displayFormat.setGroupingUsed(false);
        displayFormat.setMinimumFractionDigits(0);
        DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols(jiraAuthenticationContext.getI18nHelper().getLocale());
        displayFormat.setDecimalFormatSymbols(formatSymbols);
        return displayFormat;
    }
}
