package com.atlassian.jira.issue.fields.rest.json.beans;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * JAXB bean for link issue requests.
 *
 * @since v4.3
 */
public class LinkIssueRequestJsonBean
{
    /**
     * The name of issue link type
     */
    @JsonProperty
    private IssueLinkTypeJsonBean type;

    /**
     * the issue key to link from.
     */
    @JsonProperty
    private IssueRefJsonBean inwardIssue;

    /**
     * the issue key to create the link to.
     */
    @JsonProperty
    private IssueRefJsonBean outwardIssue;

    /**
     * An optional comment to add to the issue the link is created from.
     */
    @JsonProperty
    private CommentJsonBean comment;

    @SuppressWarnings ({ "UnusedDeclaration" })
    public LinkIssueRequestJsonBean(){}

    public LinkIssueRequestJsonBean(IssueRefJsonBean fromIssueKey, IssueRefJsonBean toIssueKey, IssueLinkTypeJsonBean type, CommentJsonBean comment)
    {
        this.inwardIssue = fromIssueKey;
        this.outwardIssue = toIssueKey;
        this.type = type;
        this.comment = comment;
    }

    public IssueLinkTypeJsonBean getType()
    {
        return type;
    }

    public CommentJsonBean getComment()
    {
        return comment;
    }

    public IssueRefJsonBean inwardIssue()
    {
        return inwardIssue;
    }

    public IssueRefJsonBean outwardIssue()
    {
        return outwardIssue;
    }
}
