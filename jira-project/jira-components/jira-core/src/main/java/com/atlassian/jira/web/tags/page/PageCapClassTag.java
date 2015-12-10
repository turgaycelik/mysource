package com.atlassian.jira.web.tags.page;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.jira.web.util.PageCapabilitiesImpl;

import com.opensymphony.module.sitemesh.taglib.AbstractTag;

public class PageCapClassTag extends AbstractTag
{
    public int doEndTag() {
        try
        {
            final HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
            getOut().write(PageCapabilitiesImpl.fromRequest(request).getBodyCssClass());
        }
        catch(Exception e)
        {
            trace(e);
        }
        return EVAL_PAGE;
    }
}
