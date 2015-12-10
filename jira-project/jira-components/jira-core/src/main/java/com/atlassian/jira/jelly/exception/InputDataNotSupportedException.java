/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.exception;

import com.atlassian.jira.jelly.ActionTagSupport;

public class InputDataNotSupportedException extends Exception
{
    public InputDataNotSupportedException(ActionTagSupport tag)
    {
        super(buildErrorMessage(tag, null));
    }

    public InputDataNotSupportedException(ActionTagSupport tag, String message)
    {
        super(buildErrorMessage(tag, message));
    }

    private static String buildErrorMessage(ActionTagSupport tag, String message)
    {
        StringBuilder buff = new StringBuilder();
        if (message != null)
        {
            buff.append(message + ": ");
        }
        buff.append("Action ").append(tag.toString()).append(" Not implemented yet...");
        return buff.toString();
    }
}
