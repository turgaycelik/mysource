/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin;

import com.atlassian.core.util.FileSize;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.util.AttachmentPathManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import webwork.config.Configuration;

@WebSudoRequired
public class ViewAttachmentSettings extends ProjectActionSupport
{
    private final AttachmentPathManager attachmentPathManager;

    public ViewAttachmentSettings(ProjectManager projectManager, PermissionManager permissionManager, AttachmentPathManager attachmentPathManager)
    {
        super(projectManager, permissionManager);
        this.attachmentPathManager = attachmentPathManager;
    }

    public String getAttachmentPath()
    {
        if (attachmentPathManager.getMode() == AttachmentPathManager.Mode.DISABLED)
        {
            return "";
        }

        if (attachmentPathManager.getUseDefaultDirectory())
        {
            return getText("admin.attachmentsettings.defaultpath") + " [" + attachmentPathManager.getAttachmentPath() + "]";
        }
        else
        {
            return attachmentPathManager.getAttachmentPath();
        }
    }

    public String getPrettyAttachmentSize()
    {
        return FileSize.format(new Long(Configuration.getString(APKeys.JIRA_ATTACHMENT_SIZE)));
    }

    public boolean getZipSupport()
    {
        return getApplicationProperties().getOption(APKeys.JIRA_OPTION_ALLOW_ZIP_SUPPORT);
    }

    public boolean isAllowedToBetSet()
    {
        final String flag = getApplicationProperties().getDefaultBackedString(APKeys.JIRA_ATTACHMENT_PATH_ALLOWED);
        return flag != null && Boolean.valueOf(flag);
    }
}
