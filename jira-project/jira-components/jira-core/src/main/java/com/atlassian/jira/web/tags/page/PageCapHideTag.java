package com.atlassian.jira.web.tags.page;

import java.util.EnumSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;

import com.atlassian.jira.web.util.PageCapabilitiesImpl;
import com.atlassian.sal.api.page.PageCapabilities;
import com.atlassian.sal.api.page.PageCapability;

import com.google.common.collect.Sets;

import webwork.view.taglib.WebWorkTagSupport;

public class PageCapHideTag extends WebWorkTagSupport
{
    private String value;

    public void setValue(String value)
    {
        this.value = value;
    }

    @Override
    public void release()
    {
        super.release();
        value = null;
    }

    @Override
    public int doStartTag() throws JspException
    {
        final HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        PageCapabilitiesImpl requestPageCaps = PageCapabilitiesImpl.fromRequest(request);
        EnumSet<PageCapability> attrPageCaps = PageCapabilities.valueOf(value);

        if(Sets.intersection(requestPageCaps.getPageCaps(), attrPageCaps).size() == attrPageCaps.size())
        {
            return SKIP_BODY;
        }
        else
        {
            return EVAL_BODY_INCLUDE;
        }
    }
}
