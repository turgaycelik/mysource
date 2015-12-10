package com.atlassian.validation;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Validates that a given string is a valid regular expression. This is not the same question as whether you should let
 * it loose on your production system.
 *
 * @since v4.4
 */
public final class RegexValidator implements Validator
{
    @Override
    public Result validate(String value)
    {
        try
        {
            Pattern.compile(value);
            return new Success(value);
        }
        catch (PatternSyntaxException e)
        {
            return new Failure("Not a valid regular expression " + e.getMessage());
        }
    }
}
