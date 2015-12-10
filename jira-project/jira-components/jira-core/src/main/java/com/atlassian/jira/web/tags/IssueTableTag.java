package com.atlassian.jira.web.tags;

import java.io.Writer;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.web.component.IssuePager;
import com.atlassian.jira.web.component.IssueTableLayoutBean;
import com.atlassian.jira.web.component.IssueTableWebComponent;

import com.google.common.annotations.VisibleForTesting;

import webwork.view.taglib.WebWorkBodyTagSupport;

/**
 * JSP tag to write a table of issues to the JSP output. List of issues and the layout bean must be provided, example:
 * {@code <ui:issuetable layoutBean="<%=layoutBean%>" issues="<%=issues%>"/>}
 */
public class IssueTableTag extends WebWorkBodyTagSupport
{
    private static final long serialVersionUID = -2061218269171318027L;

    private IssueTableLayoutBean layoutBean;
    private List<Issue> issues;
    private IssuePager issuePager;
    private Long selectedIssueId;

    public IssueTableLayoutBean getLayoutBean()
    {
        return layoutBean;
    }

    public void setLayoutBean(IssueTableLayoutBean layoutBean)
    {
        this.layoutBean = layoutBean;
    }

    public IssuePager getIssuePager()
    {
        return issuePager;
    }

    public void setIssuePager(IssuePager issuePager)
    {
        this.issuePager = issuePager;
    }

    public Long getSelectedIssueId()
    {
        return selectedIssueId;
    }

    public void setSelectedIssueId(Long selectedIssueId)
    {
        this.selectedIssueId = selectedIssueId;
    }

    public List<Issue> getIssues()
    {
        return issues;
    }

    public void setIssues(List<Issue> issues)
    {
        this.issues = issues;
    }

    public int doEndTag() throws JspException
    {
        try
        {
            asHtml(getJspWriter());
            return EVAL_PAGE;
        }
        catch (Exception e)
        {
            throw new JspTagException("Failed to render issuetable tag.", e);
        }
    }

    public void asHtml(final Writer writer)
    {
        IssueTableWebComponent issueTableWebComponent = getIssueTableWebComponent();
        issueTableWebComponent.asHtml(writer, layoutBean, issues, issuePager, selectedIssueId);
    }

    @VisibleForTesting
    protected Writer getJspWriter()
    {
        return pageContext.getOut();
    }

    @VisibleForTesting
    protected IssueTableWebComponent getIssueTableWebComponent()
    {
        return new IssueTableWebComponent();
    }
}
