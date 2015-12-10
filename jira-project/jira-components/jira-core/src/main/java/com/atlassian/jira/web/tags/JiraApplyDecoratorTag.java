/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.tags;

import com.atlassian.jira.component.ComponentAccessor;
import com.opensymphony.module.sitemesh.taglib.page.ApplyDecoratorTag;

import javax.servlet.jsp.JspException;

/**
 * This is a very simple subclass of the SiteMesh ApplyDecoratorTag which automatically
 * sets the encoding of the decorator to JIRA's encoding value.
 */
public class JiraApplyDecoratorTag extends ApplyDecoratorTag
{
    private static final String COMPUTED_ID = "jira.sitemesh.decorator.computed.id";
    private static final String CURRENT_ID = "jira.sitemesh.decorator.current.id";

    private String previousId = null;
    private String previousComputedId = null;

    @Override
    public void setId(String id)
    {
        this.id = id;
        super.setId(id);
    }

    public int doStartTag()
    {
        try
        {
            setEncoding(ComponentAccessor.getApplicationProperties().getEncoding());
        }
        catch (Exception e)
        {
            setEncoding("UTF-8"); //if database is down, we should set the encoding to a sensible default
        }
        push();
        return super.doStartTag();
    }

    @Override
    public int doEndTag() throws JspException
    {
        int rc = super.doEndTag();
        pop();
        return rc;
    }


    private void push()
    {
        this.previousId = (String) pageContext.getRequest().getAttribute(CURRENT_ID);
        this.previousComputedId = (String) pageContext.getRequest().getAttribute(COMPUTED_ID);
        
        pageContext.getRequest().setAttribute(CURRENT_ID,id);
        pageContext.getRequest().setAttribute(COMPUTED_ID, computeId(previousComputedId));
    }

    private void pop() {
        pageContext.getRequest().setAttribute(CURRENT_ID, previousId);
        pageContext.getRequest().setAttribute(COMPUTED_ID, previousComputedId);
    }


    private Object computeId(final String previousId)
    {
        StringBuilder sb = new StringBuilder();
        if (previousId != null && previousId.length() > 0)
        {
            sb.append(previousId);
        }
        if (id != null && id.length() > 0)
        {
            sb.append(id).append("-");
        }
        return sb.toString();
    }
}
