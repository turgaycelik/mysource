package com.atlassian.jira.issue.link;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.ofbiz.AbstractOfBizValueWrapper;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import org.ofbiz.core.entity.GenericValue;

public class IssueLinkImpl extends AbstractOfBizValueWrapper implements IssueLink
{
    private final IssueLinkTypeManager issueLinkTypeManager;
    private final IssueManager issueManager;

    private IssueLinkType issueLinkType;
    static final String LINK_TYPE_ID_FIELD_NAME = "linktype";
    static final String SOURCE_ID_FIELD_NAME = "source";
    static final String DESTINATION_ID_LINK_NAME = "destination";
    static final String SEQUENCE_FIELD_NAME = "sequence";

    public IssueLinkImpl(GenericValue genericValue, IssueLinkTypeManager issueLinkTypeManager, IssueManager issueManager)
    {
        super(genericValue);
        this.issueManager = issueManager;

        if (!OfBizDelegator.ISSUE_LINK.equals(genericValue.getEntityName()))
        {
            throw new IllegalArgumentException("Entity must be an 'IssueLink', not '" + genericValue.getEntityName() + "'.");
        }

        this.issueLinkTypeManager = issueLinkTypeManager;
    }

    public Long getId()
    {
        return getGenericValue().getLong("id");
    }

    public Long getLinkTypeId()
    {
        return getGenericValue().getLong(LINK_TYPE_ID_FIELD_NAME);
    }

    /**
     * Get the Issue ID (ie. {@link com.atlassian.jira.issue.Issue#getId()}) of the linked-from issue.
     */
    public Long getSourceId()
    {
        return getGenericValue().getLong(SOURCE_ID_FIELD_NAME);
    }

    /**
     * Get the issue ID (ie. {@link com.atlassian.jira.issue.Issue#getId()}) of the linked-to issue.
     */
    public Long getDestinationId()
    {
        return getGenericValue().getLong(DESTINATION_ID_LINK_NAME);
    }

    public Long getSequence()
    {
        return getGenericValue().getLong(SEQUENCE_FIELD_NAME);
    }

    public IssueLinkType getIssueLinkType()
    {
        if (issueLinkType == null)
        {
            issueLinkType = issueLinkTypeManager.getIssueLinkType(getLinkTypeId());
        }

        return issueLinkType;
    }

    /**
     * Get the source Issue of the link.
     */
    public Issue getSourceObject()
    {
        return getIssueObject(getSourceId());
    }

    /**
     * Get the destination Issue of the link.
     */
    public Issue getDestinationObject()
    {
        return getIssueObject(getDestinationId());
    }

    /**
     * Checks if this link's type is a System Link type. A System Link Type is a link type
     * that is used by JIRA to denote special relationships. For example, a sub-task is linked to its
     * parent issue using a link type that is a System Link Type.
     */
    public boolean isSystemLink()
    {
        return getIssueLinkType().isSystemLinkType();
    }

    private Issue getIssueObject(Long issueId)
    {
        return issueManager.getIssueObject(issueId);
    }
}
