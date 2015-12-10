package com.atlassian.jira.issue.attachment;

import com.atlassian.jira.web.ServletContextProvider;

import javax.servlet.ServletContext;

public final class MimetypesFileTypeMap
{
    public static String getContentType(String filename)
    {
        ServletContext context = ServletContextProvider.getServletContext();
        if (context != null)
        {
            return context.getMimeType(filename);
        }
        else
        {
            return javax.activation.MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(filename);
        }
    }

    private MimetypesFileTypeMap()
    {
    }
}
