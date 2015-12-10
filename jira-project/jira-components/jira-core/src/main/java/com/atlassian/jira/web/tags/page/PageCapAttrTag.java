package com.atlassian.jira.web.tags.page;

import java.util.EnumSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;

import com.atlassian.jira.web.util.PageCapabilitiesImpl;
import com.atlassian.sal.api.page.PageCapabilities;
import com.atlassian.sal.api.page.PageCapability;

import com.google.common.collect.Sets;

import webwork.view.taglib.WebWorkTagSupport;

public class PageCapAttrTag extends WebWorkTagSupport
{
    private String name;

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public void release()
    {
        super.release();
        name = null;
    }

    @Override
    public int doStartTag() throws JspException
    {
        final HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        final PageCapabilitiesImpl pageCaps = PageCapabilitiesImpl.fromRequest(request);

        if(pageCaps.getPageCaps().isEmpty())
        {
            return SKIP_BODY;
        }
        else
        {
            pageCaps.setRequestAttribute(request, name);
            return EVAL_BODY_INCLUDE;
        }
    }

    @Override
    public int doEndTag() throws JspException
    {
        final HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        final PageCapabilitiesImpl pageCaps = PageCapabilitiesImpl.fromRequest(request);

        if(!pageCaps.getPageCaps().isEmpty())
        {
            request.removeAttribute(name);
        }

        return super.doEndTag();
    }
}
