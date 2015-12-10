package com.atlassian.validation;

import com.opensymphony.util.TextUtils;

/**
 * Validates that a string is an integer, optionally within a specific range of values.
 *
 * @since v4.4
 */
public final class IntegerValidator implements Validator
{

    private final int min;
    private final int max;

    public IntegerValidator()
    {
        this(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public IntegerValidator(int minValue, int maxValue)
    {
        this.min = minValue;
        this.max = maxValue;
    }

    @Override
    public Result validate(String value)
    {
        try
        {
            int integerValue = Integer.parseInt(value);
            if (integerValue < min)
            {
                // TODO i18n
                return new Failure("Value must be at least " + min);
            }
            else if (integerValue > max)
            {
                // TODO i18n
                return new Failure("Value must be no more than " + max);
            }
            return new Success(value);
        }
        catch (NumberFormatException e)
        {
            // TODO i18n
            String html = '\'' + TextUtils.htmlEncode(value) + "' cannot be parsed to an integer.";
            String text = '\'' + value + "' cannot be parsed to an integer.";
            return new Failure(text, html);
        }
    }


}
