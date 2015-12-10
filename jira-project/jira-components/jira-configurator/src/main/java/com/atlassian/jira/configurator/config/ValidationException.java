
package com.atlassian.jira.configurator.config;

public class ValidationException extends Exception
{
    public ValidationException(String message)
    {
        super(message);
    }
    public ValidationException(String label, String explanation)
    {
        super("Invalid value for field \"" + label + "\": " + explanation);
    }
}

