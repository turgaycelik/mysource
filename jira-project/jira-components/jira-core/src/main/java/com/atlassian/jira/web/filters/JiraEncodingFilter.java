/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.filters;

import com.atlassian.core.filters.encoding.AbstractEncodingFilter;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.util.mime.MimeManager;
import com.atlassian.jira.web.ExecutingHttpRequest;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * This filter sets the request and response encoding.
 */
public class JiraEncodingFilter extends AbstractEncodingFilter
{
    private static final String IMAGE = "image";

    protected String getEncoding()
    {
        try
        {
            return ComponentAccessor.getApplicationProperties().getEncoding();
        }
        catch (Exception e)
        {
            return "UTF-8";
        }
    }

    protected String getContentType()
    {
        try
        {
            HttpServletRequest httpServletRequest = ExecutingHttpRequest.get();
            if (null != httpServletRequest)
            {
                String url = httpServletRequest.getRequestURI().toString();
                if(!StringUtils.isBlank(url))
                {
                    String mimeType = ComponentAccessor.getComponent(MimeManager.class).getSuggestedMimeType(url);
                    // The check is performed in such a way that, only for images, we do the handling
                    // It is too dangerous to change the mime type behaviour for all other extensions/requests
                    if (StringUtils.startsWith(mimeType, IMAGE))
                    {
                        return mimeType;//Just for images, the charset addition is avoided
                    }
                }
            }
            // In all other cases, do the existing handling (default text/html mime type)
            return ComponentAccessor.getApplicationProperties().getContentType();
        }
        catch (Exception e)
        {
            return ComponentAccessor.getApplicationProperties().getContentType();
        }
    }
}
