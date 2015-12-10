package com.atlassian.jira.bc.admin;

import com.atlassian.validation.IntegerValidator;
import com.atlassian.validation.Validator;

public class ViewIssueMinimumHiddenCommentsValidator implements Validator
{
    @Override
    public Result validate(final String value)
    {
        return new IntegerValidator(0, Integer.MAX_VALUE).validate(value);
    }
}
