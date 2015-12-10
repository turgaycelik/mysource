package com.atlassian.validation;

/**
 * Validates boolean values.
 *
 * @since v4.4
 */
public final class BooleanValidator implements Validator
{

    @Override
    public Result validate(String value)
    {
        try
        {
            Boolean.parseBoolean(value);
            return new Success(value);
        }
        catch (Exception e)
        {
            return new Failure("Not a valid boolean value, must be \"true\" or \"false\".");
        }

    }
}
