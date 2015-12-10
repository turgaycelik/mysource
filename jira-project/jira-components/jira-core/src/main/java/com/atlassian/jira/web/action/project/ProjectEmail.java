/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.project;

import com.atlassian.core.ofbiz.util.OFBizPropertyUtils;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectKeys;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.mail.server.SMTPMailServer;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericEntityException;

import static java.lang.String.format;

@WebSudoRequired
public class ProjectEmail extends JiraWebActionSupport
{
    private final ProjectManager projectManager;
    private final MailServerManager mailServerManager;

    //getters & setter from action
    private long projectId;
    private String fromAddress;
    private Project project;

    public ProjectEmail(ProjectManager projectManager, MailServerManager mailServerManager)
    {
        this.projectManager = projectManager;
        this.mailServerManager = mailServerManager;
    }

    public String doDefault() throws Exception
    {
        this.fromAddress = getProject().getEmail();

        //if we haven't set the mail server, then put the default value in
        if (fromAddress == null)
        {
            final SMTPMailServer defaultSMTPMailServer = mailServerManager.getDefaultSMTPMailServer();
            if (defaultSMTPMailServer != null)
            { this.fromAddress = defaultSMTPMailServer.getDefaultFrom(); }
        }
        return super.doDefault();
    }

    protected void doValidation()
    {
        // Validate email address
        if (TextUtils.stringSet(getFromAddress()) && !TextUtils.verifyEmail(getFromAddress()))
        {
            addError("fromAddress", getText("admin.errors.projectemail.enter.valid.address"));
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        PropertySet ps = getPropertySet();

        if (StringUtils.isBlank(fromAddress))
        {
            if (ps.exists(ProjectKeys.EMAIL_SENDER))
            {
                ps.remove(ProjectKeys.EMAIL_SENDER);
            }
        }
        else
        {
            ps.setString(ProjectKeys.EMAIL_SENDER, fromAddress);
        }

        return returnComplete(createRedirectUrl());
    }

    @RequiresXsrfCheck
    public String doReset() throws Exception
    {
        PropertySet ps = getPropertySet();
        if (ps.exists(ProjectKeys.EMAIL_SENDER))
        {
            ps.remove(ProjectKeys.EMAIL_SENDER);
        }

        return returnComplete(createRedirectUrl());
    }

    private String createRedirectUrl()
    {
        if (StringUtils.isBlank(getReturnUrl()))
        {
            return format("/plugins/servlet/project-config/%s", getProjectKey());
        }
        else
        {
            return getReturnUrl();
        }
    }

    private PropertySet getPropertySet() throws GenericEntityException
    {
        return OFBizPropertyUtils.getPropertySet(getProject().getGenericValue());
    }

    public String getProjectKey()
    {
        return getProject().getKey();
    }

    public long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(long projectId)
    {
        this.projectId = projectId;
    }

    public void setFromAddress(String fromAddress)
    {
        this.fromAddress = fromAddress;
    }

    public String getFromAddress()
    {
        return fromAddress;
    }

    private Project getProject()
    {
        if (project == null)
        {
            project = projectManager.getProjectObj(projectId);
            if (project == null)
            {
                throw new IllegalArgumentException("Project not found for projectId " + projectId);
            }
        }
        return project;
    }
}
