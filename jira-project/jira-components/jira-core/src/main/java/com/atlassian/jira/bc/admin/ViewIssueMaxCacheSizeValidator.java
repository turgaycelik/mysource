package com.atlassian.jira.bc.admin;

import com.atlassian.validation.IntegerValidator;
import com.atlassian.validation.Validator;

/**
 * Validates a View Issue max cache size property value.
 *
 * @since v6.0
 */
public class ViewIssueMaxCacheSizeValidator implements Validator
{
    @Override
    public Result validate(String value)
    {
        return new IntegerValidator(1, Integer.MAX_VALUE).validate(value);
    }
}
