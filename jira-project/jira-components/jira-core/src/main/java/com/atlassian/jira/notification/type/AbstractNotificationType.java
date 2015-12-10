/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.notification.type;

import com.atlassian.jira.notification.NotificationType;

import java.util.Map;

public abstract class AbstractNotificationType implements NotificationType
{
    public String getType()
    {
        return null;
    }

    public boolean doValidation(String key, Map parameters)
    {
        return true;
    }

    public String getArgumentDisplay(String argument)
    {
        return argument;
    }

    public String getArgumentValue(String displayValue)
    {
        return displayValue;
    }
}
