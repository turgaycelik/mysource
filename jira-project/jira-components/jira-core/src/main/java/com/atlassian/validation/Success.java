package com.atlassian.validation;

/**
 * Basic implementation of a successful validation result that takes an instance of the value object to be returned as
 * the valid instance.
 */
public final class Success implements Validator.Result
{
    private String value;

    public Success(String value)
    {
        this.value = value;
    }

    @Override
    public boolean isValid()
    {
        return true;
    }

    @Override
    public String getErrorMessage()
    {
        throw new IllegalStateException("There is no error message, you should have called isValid()");
    }

    @Override
    public String getErrorMessageHtml()
    {
        throw new IllegalStateException("There is no error message, you should have called isValid()");
    }

    @Override
    public String get() throws IllegalStateException
    {
        return value;
    }
}
