package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.issue.fields.rest.json.beans.IssueTypeJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.issuetype.IssueType;

import javax.ws.rs.core.UriInfo;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Builder class for IssueType instances.
 *
 * @since v4.2
 */
public class IssueTypeBeanBuilder
{
    /**
     * The issue type that we want to convert.
     */
    private IssueType issueType;

    /**
     * The base URL.
     */
    private JiraBaseUrls baseURI;

    /**
     * The context.
     */
    private UriInfo context;

    /**
     * Creates a new IssueTypeBeanBuilder.
     */
    public IssueTypeBeanBuilder()
    {
        // empty
    }

    /**
     * Sets the issue type.
     *
     * @param issueType an IssueType
     * @return this
     */
    public IssueTypeBeanBuilder issueType(IssueType issueType)
    {
        this.issueType = issueType;
        return this;
    }

    /**
     * Sets the base URI for JIRA.
     *
     * @param baseURI the base URI
     * @return this
     */
    public IssueTypeBeanBuilder jiraBaseUrls(JiraBaseUrls baseURI)
    {
        this.baseURI = baseURI;
        return this;
    }

    /**
     * Sets the request context.
     *
     * @param context a UriInfo
     * @return this
     */
    public IssueTypeBeanBuilder context(UriInfo context)
    {
        this.context = context;
        return this;
    }

    public IssueTypeJsonBean build()
    {
        verifyPreconditions();

        String iconAbsoluteURL;
        try
        {
            iconAbsoluteURL = new URL(issueType.getIconUrl()).toString();
        }
        catch (MalformedURLException e)
        {
            iconAbsoluteURL = baseURI.baseUrl() + issueType.getIconUrl();
        }

        return IssueTypeJsonBean.shortBean(
                new ResourceUriBuilder().build(context, IssueTypeResource.class, issueType.getId()).toString(),
                issueType.getId(),
                issueType.getNameTranslation(),
                issueType.getDescTranslation(),
                issueType.isSubTask(),
                iconAbsoluteURL
        );
    }

    public IssueTypeJsonBean buildShort()
    {
        return build();
    }

    private void verifyPreconditions()
    {
        verifyPreconditionsShort();

        if (baseURI == null)
        {
            throw new IllegalStateException("baseURI not set");
        }
    }

    private void verifyPreconditionsShort()
    {
        if (issueType == null)
        {
            throw new IllegalStateException("issueType not set");
        }

        if (context == null)
        {
            throw new IllegalStateException("context not set");
        }
    }
}
