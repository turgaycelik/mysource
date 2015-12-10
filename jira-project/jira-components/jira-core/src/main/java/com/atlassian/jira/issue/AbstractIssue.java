package com.atlassian.jira.issue;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.issue.status.Status;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;

public abstract class AbstractIssue implements Issue
{
    protected final ConstantsManager constantsManager;
    protected final IssueManager issueManager;
    protected final AttachmentManager attachmentManager;

    private Collection<Attachment> attachments;

    protected AbstractIssue(ConstantsManager constantsManager, IssueManager issueManager, AttachmentManager attachmentManager)
    {
        this.constantsManager = constantsManager;
        this.issueManager = issueManager;
        this.attachmentManager = attachmentManager;
    }

    public IssueType getIssueTypeObject()
    {
        GenericValue issueType = getIssueType();
        if (issueType != null)
            return constantsManager.getIssueTypeObject(issueType.getString("id"));
        else
            return null;
    }

    public Priority getPriorityObject()
    {
        if (getPriority() != null)
            return constantsManager.getPriorityObject(getPriority().getString("id"));
        else
            return null;
    }

    public Resolution getResolutionObject()
    {
        if (getResolution() != null)
            return constantsManager.getResolutionObject(getResolution().getString("id"));
        else
            return null;
    }

    public Status getStatusObject()
    {
        @SuppressWarnings ({ "deprecation" }) GenericValue statusGV = getStatus();
        if (statusGV != null)
            return constantsManager.getStatusObject(statusGV.getString("id"));
        else
            return null;
    }

    public Issue getParentObject()
    {
        if (isSubTask())
        {
            return issueManager.getIssueObject(getParentId());
        }

        return null;
    }

    /**
     * @deprecated Use {@link #getParentObject()} instead.
     */
    public GenericValue getParent()
    {
        if (isSubTask())
        {
            return issueManager.getIssue(getParentId());
        }

        return null;
    }


    public boolean isEditable()
    {
        if (getGenericValue() != null)
            return issueManager.isEditable(this);
        else
            return true;
    }

    public Collection<Attachment> getAttachments()
    {
        if (attachments == null)
            attachments = attachmentManager.getAttachments(this);
        return attachments;
    }

    @Override
    public final boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof Issue))
        {
            return false;
        }

        String key = getKey();
        String otherKey = ((Issue) o).getKey();

        if (key == null)
        {
            return otherKey == null;
        }
        else
        {
            return key.equals(otherKey);
        }
    }

    @Override
    public final int hashCode()
    {
        String key = getKey();
        return key == null ? 0 : key.hashCode();
    }
}
