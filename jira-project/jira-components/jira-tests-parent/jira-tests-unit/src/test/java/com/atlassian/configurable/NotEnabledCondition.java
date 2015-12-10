package com.atlassian.configurable;

/**
 * Test EnabledCondition implementation
 */
public class NotEnabledCondition implements EnabledCondition
{
    public boolean isEnabled()
    {
        return false;
    }
}
