/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.mail;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.comparator.UserComparator;
import com.atlassian.jira.mail.Email;
import com.atlassian.jira.mail.builder.EmailBuilder;
import com.atlassian.jira.mail.util.MimeTypes;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleActors;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.mail.server.SMTPMailServer;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.opensymphony.util.TextUtils;

import org.apache.log4j.Logger;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

@WebSudoRequired
public class SendBulkMail extends JiraWebActionSupport
{
    private static final transient Logger log = Logger.getLogger(SendBulkMail.class);

    private static final Predicate<User> ONLY_ACTIVE_USERS = new Predicate<User>()
    {
        @Override
        public boolean apply(@Nullable final User input)
        {
            return input.isActive();
        }
    };
    private static final String HTML_BODY_TEMPLATE_PATH = "templates/email/html/emailfromadmin.vm";
    private static final String TEXT_BODY_TEMPLATE_PATH = "templates/email/html/emailfromadmintext.vm";

    private boolean sendToRoles = true;

    private String[] groups;
    private String[] projects;
    private String[] roles;
    private String subject;
    private String messageType;
    private String message;
    private String status;
    private String replyTo;
    private boolean sendBlind = false;
    private List<User> users;
    private int RECIPIENT_BATCH_SIZE = 100;

    protected static final int MAX_MULTISELECT_SIZE = 6;
    private final MailServerManager mailServerManager;

    private final PermissionManager permissionManager;
    private final ProjectRoleService projectRoleService;
    private final ProjectManager projectManager;
    private final UserUtil userUtil;
    private final GroupManager groupManager;

    public SendBulkMail(final MailServerManager mailServerManager, final PermissionManager permissionManager, final ProjectRoleService projectRoleService, final ProjectManager projectManager, final UserUtil userUtil, GroupManager groupManager)
    {
        this.mailServerManager = mailServerManager;
        this.permissionManager = permissionManager;
        this.projectRoleService = projectRoleService;
        this.projectManager = projectManager;
        this.userUtil = userUtil;
        this.groupManager = groupManager;

        try
        {
            RECIPIENT_BATCH_SIZE = Integer.parseInt(getApplicationProperties().getDefaultBackedString(APKeys.JIRA_SENDMAIL_RECIPENT_BATCH_SIZE));
        }
        catch (Exception e)
        {
            log.warn("Exception whilst trying to get property for " + APKeys.JIRA_SENDMAIL_RECIPENT_BATCH_SIZE + ". Defaulting to using " + RECIPIENT_BATCH_SIZE);
        }
    }

    public String doDefault()
    {
        sendBlind = true;
        return INPUT;
    }

    protected void doValidation()
    {
        // Ensure there is a mail server configures
        if (!isHasMailServer())
        {
            // Check if no other error messages exists
            if (!invalidInput())
            {
                // If no error messages exist, i.e. no exception have occurred, then add the error
                addErrorMessage(getText("admin.errors.no.mail.server"));
            }
            return;
        }

        if (isSendToRoles())
        {
            boolean projectNotSelected = getProjects() == null || getProjects().length == 0;
            boolean roleNotSelected = getRoles() == null || getRoles().length == 0;

            if (projectNotSelected && roleNotSelected)
            {
                addError("sendToRoles", getText("admin.errors.select.one.project.and.role"));
            }
            else if (projectNotSelected)
            {
                addError("sendToRoles", getText("admin.errors.select.one.project"));
            }
            else if (roleNotSelected)
            {
                addError("sendToRoles", getText("admin.errors.select.one.role"));
            }

            if (!invalidInput())
            {
                // Get a SET of users that belong to the selected project roles
                // First resolve the selected roles
                User remoteUser = getLoggedInUser();
                Set<ProjectRole> projectRoles = new HashSet<ProjectRole>();
                for (String roleIdAsString : getAsCollection(getRoles()))
                {
                    Long roleId = Long.valueOf(roleIdAsString);
                    projectRoles.add(projectRoleService.getProjectRole(remoteUser, roleId, this));
                }

                // Iterate through the selected projects and get the users with the selected project roles
                Set<User> recipientUsers = newHashSet();
                for (final String s : getAsCollection(getProjects()))
                {
                    Long projectId = Long.valueOf(s);
                    Project project = projectManager.getProjectObj(projectId);
                    for (ProjectRole projectRole : projectRoles)
                    {
                        ProjectRoleActors roleActors = projectRoleService.getProjectRoleActors(remoteUser, projectRole, project, this);
                        recipientUsers.addAll(roleActors.getUsers());
                    }
                }

                users = newArrayList(Iterables.filter(recipientUsers, ONLY_ACTIVE_USERS));

                // Check to ensure we have users to e-mail
                if (users.isEmpty())
                {
                    addError("sendToRoles", getText("admin.errors.empty.projectroles"));
                }
            }
        }
        else
        {
            if (getGroups() == null || getGroups().length == 0)
            {
                addError("sendToRoles", getText("admin.errors.select.one.group"));
            }
            else
            {
                // Get a SET of users that are members of the selected groups
                users = newArrayList(Iterables.filter(userUtil.getUsersInGroupNames(getAsCollection(getGroups())), ONLY_ACTIVE_USERS));

                // Check to ensure we have users to e-mail
                if (users.isEmpty())
                {
                    addError("sendToRoles", getText("admin.errors.empty.groups"));
                }
            }
        }

        if (!invalidInput())
        {
            Collections.sort(users, new UserComparator());
        }

        if (TextUtils.stringSet(getReplyTo()))
        {
            // If the reply to is specified ensure it is a correct email format
            if (!TextUtils.verifyEmail(getReplyTo()))
            {
                addError("replyTo", getText("admin.errors.invalid.email"));
            }
        }

        if (!TextUtils.stringSet(getSubject()))
        {
            addError("subject", getText("admin.errors.no.subject"));
        }
        if (!TextUtils.stringSet(getMessageType()))
        {
            addError("messageType", getText("admin.errors.no.message.type"));
        }
        if (!TextUtils.stringSet(getMessage()))
        {
            addError("message", getText("admin.errors.no.body"));
        }
        super.doValidation();
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        StringBuilder mailSentRecipients = new StringBuilder();
        Iterator<User> recipients = users.iterator();
        while (recipients.hasNext())
        {
            //send batchSize number of recipients at a time only - JRA-9189
            StringBuilder toList = new StringBuilder();
            for (int i = 0; i < RECIPIENT_BATCH_SIZE && recipients.hasNext(); i++)
            {
                User user = recipients.next();
                toList.append(user.getEmailAddress()).append(",");
            }

            // Remove the last ","
            if (toList.length() > 0)
            {
                toList.deleteCharAt(toList.length() - 1);
            }

            try
            {
                final User user = getLoggedInUser();
                final SMTPMailServer server = mailServerManager.getDefaultSMTPMailServer();
                final Email email = new Email((sendBlind ? null : toList.toString()), null, (sendBlind ? toList.toString() : null));
                email.setFromName(user.getName());
                if (TextUtils.stringSet(getReplyTo()))
                {
                    email.setReplyTo(getReplyTo());
                } else
                {
                    email.setReplyTo(user.getEmailAddress());
                }
                final EmailBuilder builder = new EmailBuilder(email, getMimeType(), getLocale())
                        .withSubject(getSubject())
                        .withBodyFromFile(getBodyTemplatePath())
                        .addParameters(getContextParams());

                // NOTE: The message is sent directly, i.e. is NOT queued
                server.send(builder.renderNow());

                status = getText("admin.errors.message.sent.successfully");
                mailSentRecipients.append(toList.toString());
            }
            catch (Exception e)
            {
                status = getText("admin.errors.failed.to.send", "<font color=\"bb0000\">", "</font>");
                addErrorMessage(getText("admin.errors.the.error.was") + " " + e.getMessage());

                log.error("Failed to send email to : " + toList);
                log.error("Error sending e-mail.", e);
            }
        }
        if (mailSentRecipients.length() > 0)
            log.debug("Email successfully sent to : " + mailSentRecipients);

        return getResult();
    }

    private String getBodyTemplatePath()
    {
        if (isHtmlMessage()) {
            return HTML_BODY_TEMPLATE_PATH;
        } else {
            return TEXT_BODY_TEMPLATE_PATH;
        }
    }

    private String getMimeType()
    {
        if (isHtmlMessage())
        {
            return MimeTypes.Text.HTML;
        }
        else
        {
            return MimeTypes.Text.PLAIN;
        }
    }

    private boolean isHtmlMessage() {return NotificationRecipient.MIMETYPE_HTML.equals(getMessageType());}

    private Collection<String> getAsCollection(String[] array)
    {
        if (array != null)
        {
            return Arrays.asList(array);
        }
        else
        {
            return Collections.emptyList();
        }
    }

    public Collection getAllGroups()
    {
        return groupManager.getAllGroups();
    }

    public Collection getAllProjects()
    {
        return permissionManager.getProjects(Permissions.BROWSE, getLoggedInUser());
    }

    public Collection getAllRoles()
    {
        return projectRoleService.getProjectRoles(getLoggedInUser(), this);
    }

    public boolean isSendToRoles()
    {
        return sendToRoles;
    }

    public void setSendToRoles(boolean sendToRoles)
    {
        this.sendToRoles = sendToRoles;
    }

    public String[] getGroups()
    {
        return groups;
    }

    public void setGroups(String[] groups)
    {
        this.groups = groups;
    }

    public String[] getProjects()
    {
        return projects;
    }

    public void setProjects(String[] projects)
    {
        this.projects = projects;
    }

    public String[] getRoles()
    {
        return roles;
    }

    public void setRoles(String[] roles)
    {
        this.roles = roles;
    }

    public String getSubject()
    {
        return subject;
    }

    public void setSubject(String subject)
    {
        this.subject = subject;
    }

    public String getMessageType()
    {
        return messageType;
    }

    public void setMessageType(String messageType)
    {
        this.messageType = messageType;
    }

    public Map getMimeTypes()
    {
        return EasyMap.build(NotificationRecipient.MIMETYPE_HTML, NotificationRecipient.MIMETYPE_HTML_DISPLAY, NotificationRecipient.MIMETYPE_TEXT, NotificationRecipient.MIMETYPE_TEXT_DISPLAY);
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getStatus()
    {
        return status;
    }

    public int getGroupsFieldSize()
    {
        return Math.min(getAllGroups().size() + 1, MAX_MULTISELECT_SIZE);
    }

    public int getProjectsRolesFieldSize()
    {
        int largestFieldSize = Math.max(getAllProjects().size() + 1, getAllRoles().size() + 1);
        return Math.min(largestFieldSize, MAX_MULTISELECT_SIZE);
    }

    public String getReplyTo()
    {
        return replyTo;
    }

    public void setReplyTo(String replyTo)
    {
        this.replyTo = replyTo;
    }

    public boolean isSendBlind()
    {
        return sendBlind;
    }

    public void setSendBlind(boolean sendBlind)
    {
        this.sendBlind = sendBlind;
    }

    public boolean isHasMailServer()
    {
        return (mailServerManager.getDefaultSMTPMailServer() != null);
    }

    public Collection getUsers()
    {
        return users;
    }

    Map<String, Object> getContextParams()
    {
        Map<String,Object> params = Maps.newHashMap();
        params.put("content", getMessage());
        params.put("subject", getSubject());
        params.put("author", getLoggedInApplicationUser());

        return params;
    }

}
