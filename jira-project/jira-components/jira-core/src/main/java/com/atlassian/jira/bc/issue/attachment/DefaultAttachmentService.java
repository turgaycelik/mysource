package com.atlassian.jira.bc.issue.attachment;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.event.issue.IssueEventSource;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.exception.AttachmentNotFoundException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.util.IssueUpdateBean;
import com.atlassian.jira.issue.util.IssueUpdater;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.MapBuilder;

import java.util.List;

import static com.atlassian.jira.user.util.Users.isAnonymous;

/**
 */
public class DefaultAttachmentService implements AttachmentService
{
    private static final String ERROR_ATTACHMENT_DELETE_NON_EDITABLE_ISSUE = "attachment.service.error.delete.issue.non.editable";
    private static final String ERROR_ATTACHMENT_CREATE_NON_EDITABLE_ISSUE = "attachment.service.error.create.issue.non.editable";
    private static final String ERROR_ATTACHMENT_MANAGE_NO_PERMISSION = "attachment.service.error.manage.no.permission";
    private static final String ERROR_ATTACHMENT_DELETE_NO_PERMISSION = "attachment.service.error.delete.no.permission";
    private static final String ERROR_ATTACHMENT_CREATE_NO_PERMISSION = "attachment.service.error.create.no.permission";
    private static final String ERROR_ATTACHMENTS_DISABLED = "attachment.service.error.attachments.disabled";
    private static final String ERROR_SCREENSHOT_APPLET_DISABLED = "attachment.service.error.screenshot.applet.disabled";
    private static final String ERROR_SCREENSHOT_APPLET_UNSUPPORTED_OS = "attachment.service.error.screenshot.applet.unsupported.os";

    private final AttachmentManager attachmentManager;
    private final PermissionManager permissionManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final IssueUpdater issueUpdater;
    private final IssueManager issueManager;

    public DefaultAttachmentService(AttachmentManager attachmentManager, PermissionManager permissionManager, JiraAuthenticationContext jiraAuthenticationContext, IssueUpdater issueUpdater, IssueManager issueManager)
    {
        this.attachmentManager = attachmentManager;
        this.permissionManager = permissionManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.issueUpdater = issueUpdater;
        this.issueManager = issueManager;
    }

    public boolean canDeleteAttachment(JiraServiceContext jiraServiceContext, Long attachmentId)
    {
        ErrorCollection errorCollection = new SimpleErrorCollection();
        ApplicationUser user = jiraServiceContext.getLoggedInApplicationUser();

        Attachment attachment = getAndVerifyAttachment(attachmentId, errorCollection);

        if (errorCollection.hasAnyErrors())
        {
            jiraServiceContext.getErrorCollection().addErrorCollection(errorCollection);
            return false;
        }

        Issue issue = getAndVerifyIssue(attachment, errorCollection);

        if (errorCollection.hasAnyErrors())
        {
            jiraServiceContext.getErrorCollection().addErrorCollection(errorCollection);
            return false;
        }

        if (!isIssueInEditableWorkflowState(issue))
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(getText(ERROR_ATTACHMENT_DELETE_NON_EDITABLE_ISSUE));
            return false;
        }

        if (userHasAttachmentDeleteAllPermission(issue, user))
        {
            return true;
        }

        if (userHasAttachmentDeleteOwnPermission(issue, user) && isUserAttachmentAuthor(attachment, user))
        {
            return true;
        }

        jiraServiceContext.getErrorCollection().addErrorMessage(getText(ERROR_ATTACHMENT_DELETE_NO_PERMISSION, attachmentId.toString()));
        return false;
    }

    public boolean canManageAttachments(JiraServiceContext jiraServiceContext, Issue issue)
    {
        ErrorCollection errorCollection = new SimpleErrorCollection();
        ApplicationUser user = jiraServiceContext.getLoggedInApplicationUser();

        // Check this first because we will add an error if the issue is null
        boolean hasDeletePerm = canDeleteAnyAttachment(user, issue, errorCollection);

        if (errorCollection.hasAnyErrors())
        {
            jiraServiceContext.getErrorCollection().addErrorCollection(errorCollection);
            return false;
        }

        if (!isAttachmentsEnabledAndPathSet())
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(getText(ERROR_ATTACHMENTS_DISABLED));
            return false;
        }

        boolean hasCreatePerm = userHasCreateAttachmentPermission(issue, user);

        if (hasCreatePerm || hasDeletePerm)
        {
            return true;
        }

        jiraServiceContext.getErrorCollection().addErrorMessage(getText(ERROR_ATTACHMENT_MANAGE_NO_PERMISSION));
        return false;
    }

    public void delete(JiraServiceContext jiraServiceContext, Long attachmentId)
    {
        ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();
        User user = jiraServiceContext.getLoggedInUser();

        Attachment attachment = getAndVerifyAttachment(attachmentId, errorCollection);

        if (errorCollection.hasAnyErrors())
        {
            return;
        }

        Issue issue = getAndVerifyIssue(attachment, errorCollection);

        if (errorCollection.hasAnyErrors())
        {
            return;
        }

        //attempt to delete the attachment from disk and related metadata from database
        try
        {
            attachmentManager.deleteAttachment(attachment);
        }
        catch (RemoveException e)
        {
            errorCollection.addErrorMessage(getText("attachment.service.error.delete.attachment.failed", attachmentId.toString()));
            return;
        }

        IssueUpdateBean issueUpdateBean = constructIssueUpdateBeanForAttachmentDelete(attachment, issue, user);

        //update issue (dispatches change event)
        issueUpdater.doUpdate(issueUpdateBean, true);
    }

    public Attachment getAttachment(JiraServiceContext jiraServiceContext, Long attachmentId) throws AttachmentNotFoundException
    {
        ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();
        return getAndVerifyAttachment(attachmentId, errorCollection);
    }

    public boolean canCreateAttachments(JiraServiceContext jiraServiceContext, Project project)
    {
        final ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();

        if (project == null)
        {
            errorCollection.addErrorMessage(getText("attachment.service.error.null.project"));
            return false;
        }
        if (!isAttachmentsEnabledAndPathSet())
        {
            errorCollection.addErrorMessage(getText(ERROR_ATTACHMENTS_DISABLED));
            return false;
        }

        if (!permissionManager.hasPermission(Permissions.CREATE_ATTACHMENT, project, jiraServiceContext.getLoggedInApplicationUser()))
        {
            errorCollection.addErrorMessage(getText("attachment.service.error.create.no.permission.project"));
            return false;
        }

        return true;
    }

    public boolean canCreateTemporaryAttachments(final JiraServiceContext jiraServiceContext, final Issue issue)
    {
        return canCreateAttachmentsWithoutWorkflow(jiraServiceContext, issue);
    }

    public boolean canCreateAttachments(JiraServiceContext jiraServiceContext, Issue issue)
    {
        if(!canCreateAttachmentsWithoutWorkflow(jiraServiceContext, issue))
        {
            return false;
        }
        if (!isIssueInEditableWorkflowState(issue))
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(getText(ERROR_ATTACHMENT_CREATE_NON_EDITABLE_ISSUE));
            return false;
        }

        return true;
    }

    private boolean canCreateAttachmentsWithoutWorkflow(JiraServiceContext jiraServiceContext, Issue issue)
    {
        final ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();
        if (issue == null)
        {
            errorCollection.addErrorMessage(getText("attachment.service.error.null.issue"));
            return false;
        }

        if (!isAttachmentsEnabledAndPathSet())
        {
            errorCollection.addErrorMessage(getText(ERROR_ATTACHMENTS_DISABLED));
            return false;
        }

        if (!userHasCreateAttachmentPermission(issue, jiraServiceContext.getLoggedInApplicationUser()))
        {
            errorCollection.addErrorMessage(getText(ERROR_ATTACHMENT_CREATE_NO_PERMISSION));
            return false;
        }
        return true;
    }

    public boolean canAttachScreenshots(JiraServiceContext jiraServiceContext, Issue issue)
    {
        // The issue is null checked by the call to canCreateAttachements
        if (!canCreateAttachments(jiraServiceContext, issue))
        {
            return false;
        }

        if (!isScreenshotAppletEnabledAndSupportedByOS(jiraServiceContext))
        {
            return false;
        }

        return true;
    }

    boolean isIssueInEditableWorkflowState(Issue issue)
    {
        return issueManager.isEditable(issue);
    }

    boolean isScreenshotAppletEnabledAndSupportedByOS(JiraServiceContext jiraServiceContext)
    {
        ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();

        if (!attachmentManager.isScreenshotAppletEnabled())
        {
            errorCollection.addErrorMessage(getText(ERROR_SCREENSHOT_APPLET_DISABLED));
            return false;
        }

        if (!attachmentManager.isScreenshotAppletSupportedByOS())
        {
            errorCollection.addErrorMessage(getText(ERROR_SCREENSHOT_APPLET_UNSUPPORTED_OS));
            return false;
        }

        return true;
    }

    boolean isAttachmentsEnabledAndPathSet()
    {
        return attachmentManager.attachmentsEnabled();
    }

    Issue getAndVerifyIssue(Attachment attachment, ErrorCollection errorCollection)
    {
        Issue issue = attachment.getIssueObject();
        if (issue == null)
        {
            errorCollection.addErrorMessage(getText("attachment.service.error.null.issue.for.attachment", attachment.getId().toString()));
            return null;
        }

        return issue;
    }

    Attachment getAndVerifyAttachment(Long attachmentId, ErrorCollection errorCollection)
    {
        if (attachmentId == null)
        {
            errorCollection.addErrorMessage(getText("attachment.service.error.null.attachment.id"));
            return null;
        }

        Attachment attachment = attachmentManager.getAttachment(attachmentId);

        if (attachment == null)
        {
            errorCollection.addErrorMessage(getText("attachment.service.error.null.attachment", attachmentId.toString()));
        }
        return attachment;
    }

    IssueUpdateBean constructIssueUpdateBeanForAttachmentDelete(Attachment attachment, Issue issue, User user)
    {
        //generate change history for issue to show that attachment has been deleted
        ChangeItemBean changeItem = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Attachment", attachment.getId().toString(), attachment.getFilename(), null, null);
        List<ChangeItemBean> changeItemBeans = EasyList.build(changeItem);

        //configure issue update event
        IssueUpdateBean issueUpdateBean = new IssueUpdateBean(issue.getGenericValue(), issue.getGenericValue(), EventType.ISSUE_UPDATED_ID, user);
        issueUpdateBean.setChangeItems(changeItemBeans);
        issueUpdateBean.setDispatchEvent(true);
        issueUpdateBean.setParams(MapBuilder.build("eventsource", IssueEventSource.ACTION));
        return issueUpdateBean;
    }

    // If the user has the ATTACHMENT_DELETE_ALL permission, return true
    boolean userHasAttachmentDeleteAllPermission(Issue issue, ApplicationUser user)
    {
        return permissionManager.hasPermission(Permissions.ATTACHMENT_DELETE_ALL, issue, user);
    }

    // If the user has the ATTACHMENT_DELETE_ALL permission, return true
    boolean userHasAttachmentDeleteOwnPermission(Issue issue, ApplicationUser user)
    {
        return permissionManager.hasPermission(Permissions.ATTACHMENT_DELETE_OWN, issue, user);
    }

    // If the user has the CREATE_ATTACHMENT permission, return true
    boolean userHasCreateAttachmentPermission(Issue issue, ApplicationUser user)
    {
        return permissionManager.hasPermission(Permissions.CREATE_ATTACHMENT, issue, user);
    }

    boolean isUserAttachmentAuthor(Attachment attachment, ApplicationUser user)
    {
        ApplicationUser attachmentAuthor = attachment.getAuthorObject();

        //if the author & the remote user are anonymous, return true
        if (attachmentAuthor == null && isAnonymous(user))
        {
            return true;
        }

        //if the author but not the remote user are anonymous (or vice versa), return false
        else if (attachmentAuthor == null || isAnonymous(user))
        {
            return false;
        }

        //if the attachment author is the remote user, return true
        return attachmentAuthor.equals(user);
    }

    boolean isAuthorOfAtLeastOneAttachment(Issue issue, ApplicationUser user)
    {
        List<Attachment> attachments = attachmentManager.getAttachments(issue);
        for (final Attachment attachment : attachments)
        {
            if (isUserAttachmentAuthor(attachment, user))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves the attachments associated with the specified issue and determines whether the user has permission to
     * delete one or more of them (granted by the {@link com.atlassian.jira.security.Permissions#ATTACHMENT_DELETE_ALL}
     * or {@link com.atlassian.jira.security.Permissions#ATTACHMENT_DELETE_OWN} permission).
     * <p/>
     * If the user has the
     * {@link com.atlassian.jira.security.Permissions#ATTACHMENT_DELETE_ALL} permission the attachments will not be
     * checked.
     *
     * @param user            who the permission checks will be run against (can be null, indicating an anonymous user)
     * @param issue           who's attachments will be checked against the specified user's delete permissions (if the issue
     *                        is null an error will be placed into the supplied ErrorCollection and false will be returned)
     * @param errorCollection will contain any errors in calling the method
     * @return true if the user has permission to delete one or more attachments associated with the issue; false
     *         otherwise
     */
    boolean canDeleteAnyAttachment(ApplicationUser user, Issue issue, ErrorCollection errorCollection)
    {
        if (issue == null)
        {
            errorCollection.addErrorMessage(getText("attachment.service.error.null.issue"));
            return false;
        }

        if (userHasAttachmentDeleteAllPermission(issue, user))
        {
            return true;
        }

        //if the user doesn't have the DELETE_OWN permission, the user can't delete any attachments at all
        if (userHasAttachmentDeleteOwnPermission(issue, user))
        {
            //if the user has the DELETE_OWN permission, check each attachment to see if the user is the author
            return isAuthorOfAtLeastOneAttachment(issue, user);
        }

        //if the user is not the author of any of the issue's associated attachments, the user cannot delete any of the
        //attachments
        return false;
    }

    private String getText(String key)
    {
        return jiraAuthenticationContext.getI18nHelper().getText(key);
    }

    private String getText(String key, String param)
    {
        return jiraAuthenticationContext.getI18nHelper().getText(key, param);
    }
}
