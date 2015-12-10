package com.atlassian.jira.config.properties;

import com.atlassian.validation.Failure;
import com.atlassian.validation.Success;
import com.atlassian.validation.Validator;

/**
 * A validator for testing.
 *
 * @since v4.4
 */
public class MockValidator implements Validator
{
    @Override
    public Result validate(String value)
    {
        if (value.equals("mock"))
        {
            return new Success(value);
        }
        else
        {
            return new Failure("mock is the only success value");
        }
    }
}
