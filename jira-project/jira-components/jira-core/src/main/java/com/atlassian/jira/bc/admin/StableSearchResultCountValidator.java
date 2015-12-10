package com.atlassian.jira.bc.admin;

import com.atlassian.validation.IntegerValidator;
import com.atlassian.validation.Validator;

/**
 * Validates a Stable Search max result property value.
 *
 * @since v6.0
 */
public class StableSearchResultCountValidator implements Validator
{
    @Override
    public Result validate(String value)
    {
        return new IntegerValidator(1000, Integer.MAX_VALUE).validate(value);
    }
}
