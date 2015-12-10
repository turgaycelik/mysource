/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.util.AttachmentPathManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;

import java.io.File;

@WebSudoRequired
public class EditAttachmentSettings extends ViewAttachmentSettings
{
    // options
    private boolean thumbnailsEnabled;
    private boolean zipSupport;
    private String attachmentPath;
    private String customAttachmentPath;
    private String attachmentSize;
    private final AttachmentPathManager attachmentPathManager;
    private AttachmentPathManager.Mode attachmentMode;

    // added for backwards-compatibility
    public EditAttachmentSettings(AttachmentPathManager attachmentPathManager)
    {
        this(ComponentAccessor.getProjectManager(), ComponentAccessor.getPermissionManager(), attachmentPathManager);
    }

    public EditAttachmentSettings(ProjectManager projectManager, PermissionManager permissionManager, AttachmentPathManager attachmentPathManager)
    {
        super(projectManager, permissionManager, attachmentPathManager);
        this.attachmentPathManager = attachmentPathManager;
    }

    public String doDefault() throws Exception
    {
        setAttachmentPathVariables(attachmentPathManager.getMode());
        final ApplicationProperties applicationProperties = getApplicationProperties();

        attachmentSize = applicationProperties.getDefaultBackedString(APKeys.JIRA_ATTACHMENT_SIZE);
        thumbnailsEnabled = applicationProperties.getOption(APKeys.JIRA_OPTION_ALLOWTHUMBNAILS);
        zipSupport = applicationProperties.getOption(APKeys.JIRA_OPTION_ALLOW_ZIP_SUPPORT);

        return INPUT;
    }

    private void setAttachmentPathVariables(final AttachmentPathManager.Mode mode)
    {
        attachmentMode = mode;

        customAttachmentPath = null;
        if (attachmentMode == AttachmentPathManager.Mode.CUSTOM)
        {
            attachmentPath = attachmentPathManager.getAttachmentPath();
            customAttachmentPath = attachmentPath;
        }
        else
        {
            attachmentPath = attachmentPathManager.getDefaultAttachmentPath();
       }
    }

    protected void doValidation()
    {
        setAttachmentPathVariables(attachmentMode);
        
        // Set the attachmentsEnabled and attachmentPath to the defaults if the user is not a system admin
        if (!isSystemAdministrator())
        {
            attachmentMode = attachmentPathManager.getMode();
        }

        if (attachmentMode == AttachmentPathManager.Mode.DEFAULT)
        {
            File actualPath = new File(attachmentPathManager.getDefaultAttachmentPath());

            tryToCreateAttachmentPath(actualPath);

            if (!actualPath.exists() || !actualPath.isDirectory())
            {
                addError("attachmentPath", getText("admin.errors.path.entered.does.not.exist"));
            }
            else if (!actualPath.canRead() || !actualPath.canWrite())
            {
                addError("attachmentPath", getText("admin.errors.path.entered.is.not.readable"));
            }

            if (!TextUtils.stringSet(attachmentSize))
            {
                addError("attachmentSize", getText("admin.errors.attachments.size.required"));
            }
            else
            {
                try
                {
                    int maxSize = Integer.parseInt(attachmentSize);
                    if (maxSize <= 0)
                    {
                        addError("attachmentSize", getText("admin.errors.attachments.size.must.be.positive"));
                    }
                }
                catch (NumberFormatException e)
                {
                    addError("attachmentSize", getText("admin.errors.attachments.size.must.be.between", String.valueOf(Integer.MAX_VALUE)));
                }
            }
        }
        else if (attachmentMode == AttachmentPathManager.Mode.DISABLED)
        {
            if (thumbnailsEnabled)
            {
                addErrorMessage(getText("admin.errors.attachments.must.be.enabled.to.enable.thumbnails"));
            }
            if (zipSupport)
            {
                addErrorMessage(getText("admin.errors.attachments.must.be.enabled.to.enable.zip.support"));
            }
        }
        super.doValidation();
    }

    private void tryToCreateAttachmentPath(File actualPath)
    {
        if (!actualPath.exists()) // if doesn't exist, try to create it
        {
            actualPath.mkdirs();
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        final ApplicationProperties applicationProperties = getApplicationProperties();
        if (isSystemAdministrator())
        {
            if (attachmentMode == AttachmentPathManager.Mode.DEFAULT)
            {
                attachmentPathManager.setUseDefaultDirectory();
                applicationProperties.setOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS, true);
            }
            else if (attachmentMode == AttachmentPathManager.Mode.DISABLED)
            {
                attachmentPathManager.disableAttachments();
            }
        }
        // if attachments are disabled, then disable thumbnails
        if (attachmentMode == AttachmentPathManager.Mode.DISABLED)
        {
            thumbnailsEnabled = false;
        }
        applicationProperties.setOption(APKeys.JIRA_OPTION_ALLOWTHUMBNAILS, thumbnailsEnabled);
        applicationProperties.setOption(APKeys.JIRA_OPTION_ALLOW_ZIP_SUPPORT, zipSupport);

        //only update the attachment size if attachments is enabled.
        if (attachmentMode != AttachmentPathManager.Mode.DISABLED)
        {
            applicationProperties.setString(APKeys.JIRA_ATTACHMENT_SIZE, attachmentSize);
        }

        return returnComplete("ViewAttachmentSettings.jspa");
    }

    /**
     * Returns the absolute path for the Default Attachment directory that lives under the home directory. This is used
     * for read-only info added to the "Use Default Directory" option.
     *
     * @return the absolute path for the Default Attachment directory that lives under the home directory.
     */
    public String getDefaultAttachmentPath()
    {
        return attachmentPathManager.getDefaultAttachmentPath();
    }

    public String getAttachmentPath()
    {
        return attachmentPath;
    }

    public String getAttachmentSize()
    {
        if (attachmentSize == null)
        {
            attachmentSize = getApplicationProperties().getDefaultBackedString(APKeys.JIRA_ATTACHMENT_SIZE);
        }
        return attachmentSize;
    }

    public String getAttachmentPathOption()
    {
        return attachmentMode.toString();
    }

    public void setAttachmentPathOption(final String attachmentPathOption)
    {
        attachmentMode = AttachmentPathManager.Mode.valueOf(attachmentPathOption);
    }

    public boolean isThumbnailsEnabled()
    {
        return thumbnailsEnabled;
    }

    public void setThumbnailsEnabled(boolean thumbnailsEnabled)
    {
        this.thumbnailsEnabled = thumbnailsEnabled;
    }

    public void setAttachmentSize(String attachmentSize)
    {
        this.attachmentSize = attachmentSize;
    }

    public boolean isZipSupport()
    {
        return zipSupport;
    }

    public void setZipSupport(final boolean zipSupport)
    {
        this.zipSupport = zipSupport;
    }

    public String getCustomAttachmentPath()
    {
        return customAttachmentPath;
    }
}
