/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.tags;

import com.atlassian.jira.util.JiraKeyUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;
import java.io.IOException;

public class LinkBugKeys extends BodyTagSupport
{
    public int doEndTag() throws JspException
    {
        try
        {
            String body = bodyContent.getString();
            bodyContent.clearBody();
            // Provide the path so that links still work - even if the page is not requested by the "usual" URL  
            final HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
            bodyContent.getEnclosingWriter().write(JiraKeyUtils.linkBugKeys(body));
        }
        catch (IOException e)
        {
            throw new JspException("IOException: " + e);
        }

        return super.doEndTag();
    }
}
