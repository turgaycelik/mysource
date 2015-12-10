package com.atlassian.validation;

import com.atlassian.core.util.StringUtils;

import java.util.Set;

/**
 * Validates that a field holds one of a fixed static set of values.
 *
 * @since v4.4
 */

public final class EnumValidator implements Validator
{

    private final ApplicationPropertyEnumerator enumerator;

    public EnumValidator(final ApplicationPropertyEnumerator enumerator)
    {
        this.enumerator = enumerator;
    }

    @Override
    public Result validate(String value)
    {
        final Set<String> options = enumerator.getEnumeration();
        if (options.contains(value))
        {
            return new Success(value);
        }
        else
        {
            String csv = StringUtils.createCommaSeperatedString(options);
            return new Failure("Only the following values are allowed: " + csv);
        }

    }

}
