package com.atlassian.validation;

/**
 * Do-nothing validator for a String. All valid, all the time.
 */
public final class NonValidator implements Validator
{
    @Override
    public Result validate(String value)
    {
        return new Success(value);
    }
}
