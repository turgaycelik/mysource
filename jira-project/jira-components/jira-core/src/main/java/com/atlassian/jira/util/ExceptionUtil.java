package com.atlassian.jira.util;

import com.atlassian.jira.issue.search.SearchException;

import com.opensymphony.util.TextUtils;

import org.apache.commons.lang.exception.ExceptionUtils;

public class ExceptionUtil
{

    /**
     * Extracts the closest thing to an end-user-comprehensible message from an exception.
     *
     * @param ex exception to get message for.
     * @return string representing exception without stacktrace.
     */
    public static String getMessage(Exception ex)
    {
        StringBuilder sb = new StringBuilder();
        appendExceptionMessageOrClassName(ex, sb);
        if (ex.getCause() != null)
        {
            sb.append(" caused by: ");
            appendExceptionMessageOrClassName(ex.getCause(), sb);
        }

        return sb.toString();
    }

    private static void appendExceptionMessageOrClassName(final Throwable ex, final StringBuilder sb)
    {
        if (ex.getMessage() != null)
        {
            sb.append(ex.getMessage());
        }
        else
        {
            sb.append(ex.getClass().getSimpleName());
        }
    }

    public static String getExceptionAsHtml(SearchException e)
    {
        return TextUtils.plainTextToHtml(ExceptionUtils.getFullStackTrace(e));
    }
}
