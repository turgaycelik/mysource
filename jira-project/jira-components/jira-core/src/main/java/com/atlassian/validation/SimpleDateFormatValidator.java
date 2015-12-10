package com.atlassian.validation;

import java.text.SimpleDateFormat;

/**
 * Validates that the given string is a valid SimpleDateFormat pattern.
 *
 * @since v4.4
 */
public final class SimpleDateFormatValidator implements Validator
{
    @Override
    public Result validate(String value)
    {
        try
        {
            new SimpleDateFormat(value);
            return new Success(value);
        }
        catch (Exception e)
        {
            return new Failure("Invalid date pattern");
        }

    }
}
