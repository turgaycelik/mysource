package com.atlassian.jira.plugin.viewissue;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestSession;
import com.atlassian.jira.web.SessionKeys;
import org.apache.commons.lang.StringUtils;

public class AttachmentBlockContextHelper
{
    static final String ORDER_DESC = "desc";
    static final String DEFAULT_ISSUE_ATTACHMENTS_ORDER = "asc";
    static final String SORTBY_DATE_TIME = "dateTime";
    static final String DEFAULT_ISSUE_ATTACHMENTS_SORTBY = "fileName";

    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final ApplicationProperties applicationProperties;
    private final IssueManager issueManager;
    private final PermissionManager permissionManager;

    AttachmentBlockContextHelper(VelocityRequestContextFactory velocityRequestContextFactory, ApplicationProperties applicationProperties,
            IssueManager issueManager, PermissionManager permissionManager)
    {
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.applicationProperties = applicationProperties;
        this.issueManager = issueManager;
        this.permissionManager = permissionManager;
    }

    String getAttachmentOrder()
    {
        return getSessionBackedRequestParam("attachmentOrder", DEFAULT_ISSUE_ATTACHMENTS_ORDER, SessionKeys.VIEWISSUE_ATTACHMENT_ORDER);
    }

    String getAttachmentSortBy()
    {
        return getSessionBackedRequestParam("attachmentSortBy", DEFAULT_ISSUE_ATTACHMENTS_SORTBY, SessionKeys.VIEWISSUE_ATTACHMENT_SORTBY);
    }

    private String getSessionBackedRequestParam(String requestParamName, String defaultValue, String sessionKey)
    {
        final VelocityRequestContext requestContext = velocityRequestContextFactory.getJiraVelocityRequestContext();
        final VelocityRequestSession session = requestContext.getSession();

        final String requestParameter = requestContext.getRequestParameter(requestParamName);
        if (StringUtils.isNotBlank(requestParameter))
        {
            if (requestParameter.equals(defaultValue))
            {
                session.removeAttribute(sessionKey);
                return defaultValue;
            }
            else
            {
                session.setAttribute(sessionKey, requestParameter);
                return requestParameter;
            }
        }

        final String sortOrder = (String) session.getAttribute(sessionKey);
        return StringUtils.isNotBlank(sortOrder) ? sortOrder : defaultValue;
    }

    boolean getZipSupport()
    {
        return applicationProperties.getOption(APKeys.JIRA_OPTION_ALLOW_ZIP_SUPPORT);
    }

    int getMaximumNumberOfZipEntriesToShow()
    {
        String maximumNumberOfZipEntriesToShowAsString = applicationProperties.getDefaultBackedString(APKeys.JIRA_ATTACHMENT_NUMBER_OF_ZIP_ENTRIES_TO_SHOW);
        int maximumNumberOfZipEntriesToShow = 30;
        try
        {
            maximumNumberOfZipEntriesToShow = Integer.parseInt(maximumNumberOfZipEntriesToShowAsString);
        }
        catch (NumberFormatException e)
        {
            //Ignoring error, we'll use the default of 30
        }
        return maximumNumberOfZipEntriesToShow;
    }

    boolean canDeleteAttachment(Issue issue, Attachment attachment, ApplicationUser user)
    {
        return issueManager.isEditable(issue)
                && (permissionManager.hasPermission(Permissions.ATTACHMENT_DELETE_ALL, issue, user)
                || (permissionManager.hasPermission(Permissions.ATTACHMENT_DELETE_OWN, issue, user) && isUserAttachmentAuthor(attachment, user)));

    }

    private boolean isUserAttachmentAuthor(Attachment attachment, ApplicationUser user)
    {
        ApplicationUser attachmentAuthor = attachment.getAuthorObject();

        //if the author & the remote user are anonymous, return true
        if (attachmentAuthor == null && user == null)
        {
            return true;
        }

        //if the author but not the remote user are anonymous (or vice versa), return false
        else if (attachmentAuthor == null || user == null)
        {
            return false;
        }

        //if the attachment author is the remote user, return true
        return attachmentAuthor.equals(user);
    }
}
