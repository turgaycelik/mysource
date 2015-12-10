/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.tags;

import com.opensymphony.util.TextUtils;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;
import java.io.IOException;

/**
 * A wrapper around {@link com.opensymphony.util.TextUtils#plainTextToHtml(java.lang.String)}.
 */
public class Text2Html extends BodyTagSupport
{
    public int doEndTag() throws JspException
    {
        try
        {
            String body = bodyContent.getString();
            bodyContent.clearBody();
            bodyContent.getEnclosingWriter().write(TextUtils.plainTextToHtml(body));
        }
        catch (IOException e)
        {
            throw new JspException("IOException: " + e);
        }
        return super.doEndTag();
    }
}
