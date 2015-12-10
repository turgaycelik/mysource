/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.attachment.TemporaryAttachment;
import com.atlassian.jira.issue.util.IssueUpdater;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.json.JSONEscaper;
import com.atlassian.jira.web.util.AttachmentException;
import com.atlassian.jira.web.util.AttachmentExceptionMessages;
import com.atlassian.jira.web.util.WebAttachmentManager;
import webwork.action.ServletActionContext;
import webwork.multipart.MultiPartRequestWrapper;

import java.util.Iterator;

/**
 * Used by inline-file-attach.js to upload temporary attachments that can then be converted to real attachments
 * lateron.
 *
 * @since 4.2
 */
public class AttachTemporaryFile extends AbstractIssueSelectAction
{
    public static final String TEMP_FILENAME = "tempFilename";

    protected final transient WebAttachmentManager webAttachmentManager;
    protected final transient IssueUpdater issueUpdater;
    protected final transient ApplicationProperties applicationProperties;

    private boolean create = false;
    private Long projectId;

    private String formToken;

    private TemporaryAttachment temporaryAttachment;
    public AttachTemporaryFile(final SubTaskManager subTaskManager, final WebAttachmentManager webAttachmentManager,
            final IssueUpdater issueUpdater, final ApplicationProperties applicationProperties)
    {
        super(subTaskManager);
        this.webAttachmentManager = webAttachmentManager;
        this.issueUpdater = issueUpdater;
        this.applicationProperties = applicationProperties;
    }

    @Override
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        try
        {
            if (isCreate())
            {
                this.temporaryAttachment = webAttachmentManager.createTemporaryAttachment(getMultipart(), TEMP_FILENAME, null, getProjectObject(), getFormToken());
            }
            else
            {
                this.temporaryAttachment = webAttachmentManager.createTemporaryAttachment(getMultipart(), TEMP_FILENAME, getIssueObject(), null, getFormToken());
            }
        }
        catch (final AttachmentException e)
        {
            addErrorMessage(AttachmentExceptionMessages.get(e, this));
        }
        return "temp_file_json";
    }

    public TemporaryAttachment getTemporaryAttachment()
    {
        return temporaryAttachment;
    }

    public boolean isCreate()
    {
        return create;
    }

    public void setCreate(final boolean create)
    {
        this.create = create;
    }

    public String getFormToken()
    {
        return formToken;
    }

    public void setFormToken(final String formToken)
    {
        this.formToken = formToken;
    }

    protected MultiPartRequestWrapper getMultipart()
    {
        return ServletActionContext.getMultiPartRequest();
    }

    public String encode(final String text)
    {
        return JSONEscaper.escape(text);
    }

    public Project getProjectObject()
    {
        if(projectId != null)
        {
            return getProjectManager().getProjectObj(projectId);
        }
        return null;
    }

    public Long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(final Long projectId)
    {
        this.projectId = projectId;
    }

    public String getErrorMessage()
    {
        if(!getErrorMessages().isEmpty())
        {
            final StringBuilder errorMsgs = new StringBuilder();
            final Iterator errorIterator = getErrorMessages().iterator();
            while (errorIterator.hasNext())
            {
                final String error = (String) errorIterator.next();
                errorMsgs.append(error);
                if(errorIterator.hasNext())
                {
                    errorMsgs.append(", ");
                }
            }

            return errorMsgs.toString();
        }
        return "";
    }
}
