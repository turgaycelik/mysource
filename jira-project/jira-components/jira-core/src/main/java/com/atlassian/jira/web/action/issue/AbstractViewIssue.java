/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.attachment.AttachmentFileNameCreationDateComparator;
import com.atlassian.jira.issue.link.LinkCollection;
import com.atlassian.jira.project.VersionProxy;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;

public class AbstractViewIssue extends AbstractIssueSelectAction
{
    private LinkCollection linkCollection;
    private List<Attachment> attachments;

    public AbstractViewIssue(final SubTaskManager subTaskManager)
    {
        super(subTaskManager);
    }

    public Collection<GenericValue> getPossibleComponents() throws Exception
    {
        return getProjectManager().getComponents(getProject());
    }

    public Collection<VersionProxy> getPossibleVersions() throws Exception
    {
        return getPossibleVersions(getProject());
    }

    public Collection<VersionProxy> getPossibleVersionsReleasedFirst() throws Exception
    {
        return getPossibleVersionsReleasedFirst(getProject());
    }

    public Collection<Attachment> getAttachments() throws Exception
    {
        if (attachments == null)
        {
            attachments = attachmentManager.getAttachments(getIssueObject(),
                    new AttachmentFileNameCreationDateComparator(getLocale()));
        }
        return attachments;
    }
}
