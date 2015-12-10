package com.atlassian.jira.bc.issue.attachment;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.exception.AttachmentNotFoundException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.project.Project;

/**
 * AttachmentService contains methods for managing issue attachments in JIRA.
 */
@PublicApi
public interface AttachmentService
{
    /**
     * Retrieves the attachment specified by the attachment id and determines if the user can delete it. The user can
     * delete it if:
     * <ul>
     * <li>They have the DELETE_ALL permission OR they have the DELETE_OWN permission and they are the author of the
     * specified attachment; and</li>
     * <li>The specified attachment exists</li>
     * </ul>
     *
     * @param jiraServiceContext containing the user who the permission checks will be run against (can be null,
     * indicating an anonymous user) and the errorCollection that will contain any errors in calling the method
     * @param attachmentId    specifies the attachment to be deleted
     * @return true if the user has permission to delete the attachment; false otherwise
     */
    boolean canDeleteAttachment(JiraServiceContext jiraServiceContext, Long attachmentId);

    /**
     * Checks whether the user has permission to manage the attachments of the specified issue. This is true if
     * <ul>
     * <li>Attachments are enabled in JIRA</li>
     * <li>The Attachment path is set</li>
     * <li>The user is allowed to create OR delete attachments associated with the specified issue</li>
     * </ul>
     *
     * @param jiraServiceContext containing the user who the permission checks will be run against (can be null,
     * indicating an anonymous user) and the errorCollection that will contain any errors in calling the method
     * @param issue           who's attachments will be checked against the specified user's delete permissions (if the issue
     *                        is null an error will be placed into the supplied ErrorCollection and false will be returned)
     * @return true if the user has permission to manage attachments
     */
    boolean canManageAttachments(JiraServiceContext jiraServiceContext, Issue issue);

    /**
     * Deletes the specified attachment and updates the issue change history and 'updated' date.
     * <p/>
     * This method expects that {@link #canDeleteAttachment(com.atlassian.jira.bc.JiraServiceContext, Long)}
     * has been successfully called.
     *
     * @param jiraServiceContext containing the user who is attempting to delete the attachment and the errorCollection
     * that will contain any errors in calling the method
     * @param attachmentId    of the target attachment, must not be null and must identify a valid attachment
     */
    void delete(JiraServiceContext jiraServiceContext, Long attachmentId);

    /**
     * Retrieves the specified issue. Does not perform permission checks.
     * 
     * @param jiraServiceContext containing the user who is attempting to retrieve the attachment and the errorCollection
     * that will contain any errors in calling the method
     * @param attachmentId    of the target attachment, must not be null and must identify a valid attachment
     * @return target attachment if no errors occur, null otherwise
     * @throws com.atlassian.jira.exception.AttachmentNotFoundException if the attachment does not exist
     */
    Attachment getAttachment(JiraServiceContext jiraServiceContext, Long attachmentId) throws AttachmentNotFoundException;

    /**
     * Determines whether attachments are enabled in JIRA and that the user has the required permission
     * ({@link com.atlassian.jira.security.Permissions#CREATE_ATTACHMENT}) to create an attachment for this issue.
     * This method also checks that the provided issue is in an editable workflow state.
     *
     * @param jiraServiceContext containing the user who wishes to create an attachment and the errorCollection
     * that will contain any errors in calling the method
     * @param issue that will have an attachment attached to it
     * @return true if the user has permission to attach an attachment to the issue and the issue is in an editable
     * workflow state, false otherwise
     */
    boolean canCreateAttachments(JiraServiceContext jiraServiceContext, Issue issue);

    /**
     * Determines whether attachments are enabled in JIRA and that the user has the required permission
     * ({@link com.atlassian.jira.security.Permissions#CREATE_ATTACHMENT}) to create an attachment for this project.
     *
     * @param jiraServiceContext containing the user who wishes to create an attachment and the errorCollection
     * that will contain any errors in calling the method
     * @param project where the attachment will be created in
     * @return true if the user has permission to attach an attachment in the project provided, false otherwise
     */
    boolean canCreateAttachments(JiraServiceContext jiraServiceContext, Project project);

    /**
     * Determines whether:
     * <ul>
     * <li>the user has the required permission ({@link com.atlassian.jira.security.Permissions#CREATE_ATTACHMENT})
     * to create an attachment</li>
     * <li>attachments are enabled</li>
     * </ul>
     * This method does *not* check if the issue is in an editable workflow step, since temporary
     * attachments may be created when reopening an issue *before* the issue is actually reopened!
     *
     * @param jiraServiceContext containing the user who wishes to attach a file and the errorCollection
     * that will contain any errors in calling the method
     * @param issue that will have the file attached to it
     * @return true if the user may attach a file, false otherwise
     */
    boolean canCreateTemporaryAttachments(JiraServiceContext jiraServiceContext, Issue issue);

    /**
     * Determines whether the user:
     * <ul>
     * <li>has the required permission ({@link com.atlassian.jira.security.Permissions#CREATE_ATTACHMENT})
     * to create an attachment</li>
     * <li>has the screenshot enabled</li>
     * <li>is using a screenshot applet compatible OS (Windows or OSX)</li>
     * <li>the issue is in an editable workflow state</li>
     * </ul>
     *
     * @param jiraServiceContext containing the user who wishes to attach a screenshot and the errorCollection
     * that will contain any errors in calling the method
     * @param issue that will have the screenshot attached to it
     * @return true if the user may attach a screenshot, false otherwise
     */
    boolean canAttachScreenshots(JiraServiceContext jiraServiceContext, Issue issue);
}
