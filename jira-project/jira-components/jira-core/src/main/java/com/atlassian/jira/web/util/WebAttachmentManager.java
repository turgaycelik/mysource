package com.atlassian.jira.web.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.TemporaryAttachment;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.project.Project;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import webwork.multipart.MultiPartRequestWrapper;

import java.io.InputStream;
import java.util.Map;

/**
 * Manager for issue attachments.
 *
 * @see com.atlassian.jira.issue.AttachmentManager
 */
public interface WebAttachmentManager
{
    /**
     * Create an issue's attachment.
     * @param requestWrapper eg. obtained from {@link webwork.action.ServletActionContext#getMultiPartRequest()}
     * @param remoteUser eg. from {@link com.atlassian.jira.web.action.JiraWebActionSupport#getLoggedInUser()}
     * @param issue Issue to associate attachment with
     * @param fileParamName Name of form parameter specifying filename (in requestWrapper).
     * @param attachmentProperties Arbitrary key:value properties to store with this attachment. Key is a String, value is an Object mappable to a {@link com.opensymphony.module.propertyset.PropertySet}.
     *  Eg. {"attachment.copyright.licensed" -> Boolean.TRUE}
     * @throws AttachmentException
     */
    ChangeItemBean createAttachment(MultiPartRequestWrapper requestWrapper, User remoteUser, Issue issue, String fileParamName, Map<String, Object> attachmentProperties) throws AttachmentException;

    /**
     * Create an issue's attachment.
     * @param requestWrapper eg. obtained from {@link webwork.action.ServletActionContext#getMultiPartRequest()}
     * @param remoteUser eg. from {@link com.atlassian.jira.web.action.JiraWebActionSupport#getLoggedInUser()}
     * @param issue Issue to associate attachment with
     * @param fileParamName Name of form parameter specifying filename (in requestWrapper).
     * @param attachmentProperties Arbitrary key:value properties to store with this attachment. Key is a String, value is an Object mappable to a {@link com.opensymphony.module.propertyset.PropertySet}.
     *  Eg. {"attachment.copyright.licensed" -> Boolean.TRUE}
     * @throws AttachmentException
     * @throws GenericEntityException
     *
     * @deprecated Use {@link #createAttachment(webwork.multipart.MultiPartRequestWrapper, com.atlassian.crowd.embedded.api.User, com.atlassian.jira.issue.Issue, String, java.util.Map)} instead. Since v5.0.
     */
    ChangeItemBean createAttachment(MultiPartRequestWrapper requestWrapper, User remoteUser, GenericValue issue, String fileParamName, Map<String, Object> attachmentProperties) throws AttachmentException, GenericEntityException;

    /**
     * Creates a temporary attachment on disk.  These attachment generally only live for the duration of a user's session
     * and will also be deleted on exit of the JVM. This method will not create a link to the issue yet, but simply
     * copy the attachment to a temp directory in the attachments folder and store all the relevant details in the
     * returned {@link com.atlassian.jira.issue.attachment.TemporaryAttachment} object
     *
     * @param requestWrapper the wrapper containing getFile() and getFilesystemName() describing the attachment
     * @param fileParamName ame of form parameter specifying filename (in requestWrapper).
     * @param issue The issue that this temporary attachment is for.  Can be null when creating a new issue
     * @param project The project where the attachment is to be placed. This is used to do security checks when creating an issue and
     *  there is no issue to run a check on. Will be ignored when issue is not null.
     * @param formToken
     * @return A {@link com.atlassian.jira.issue.attachment.TemporaryAttachment} containing details about where the temp attachment was created
     * @throws AttachmentException if there was an error saving the temporary attachment.
     */
    TemporaryAttachment createTemporaryAttachment(final MultiPartRequestWrapper requestWrapper, final String fileParamName,
            final Issue issue, final Project project, final String formToken) throws AttachmentException;

    /**
     * Creates a temporary attachment on disk.  These attachment generally only live for the duration of a user's session
     * and will also be deleted on exit of the JVM. This method will not create a link to the issue yet, but simply
     * copy the attachment to a temp directory in the attachments folder and store all the relevant details in the
     * returned {@link com.atlassian.jira.issue.attachment.TemporaryAttachment} object
     *
     * @param requestWrapper the wrapper containing getFile() and getFilesystemName() describing the attachment
     * @param fileParamName ame of form parameter specifying filename (in requestWrapper).
     * @param issue The issue that this temporary attachment is for.  Can be null when creating a new issue
     * @param project The project where the attachment is to be placed. This is used to do security checks when creating an issue and
     *  there is no issue to run a check on. Will be ignored when issue is not null.
     * @return A {@link com.atlassian.jira.issue.attachment.TemporaryAttachment} containing details about where the temp attachment was created
     * @throws AttachmentException if there was an error saving the temporary attachment.
     *
     * @deprecated Use {@link #createTemporaryAttachment(webwork.multipart.MultiPartRequestWrapper, String, com.atlassian.jira.issue.Issue, com.atlassian.jira.project.Project, String)}
     */
    TemporaryAttachment createTemporaryAttachment(final MultiPartRequestWrapper requestWrapper, final String fileParamName,
            final Issue issue, final Project project) throws AttachmentException;

    /**
     * Creates a temporary attachment on disk.  These attachment generally only live for the duration of a user's session
     * and will also be deleted on exit of the JVM. This method will not create a link to the issue yet, but simply
     * copy the attachment to a temp directory in the attachments folder and store all the relevant details in the
     * returned {@link com.atlassian.jira.issue.attachment.TemporaryAttachment} object
     *
     * @param stream the input stream for the attachment.
     * @param fileName the name of the attachment.
     * @param contentType the content type of the passed stream.
     * @param size the size of the passed stream.
     * @param issue The issue that this temporary attachment is for.  Can be null when creating a new issue.
     * @param project The project where the attachment is to be placed. This is used to do security checks when creating an issue and
     *  there is no issue to run a check on. Will be ignored when issue is not null.
     * @param formToken
     * @return A {@link com.atlassian.jira.issue.attachment.TemporaryAttachment} containing details about where the temp attachment was created
     * @throws AttachmentException if there was an error saving the temporary attachment.
     */
    TemporaryAttachment createTemporaryAttachment(final InputStream stream, final String fileName,
            final String contentType, long size, final Issue issue, final Project project, final String formToken) throws AttachmentException;

    /**
     * Creates a temporary attachment on disk.  These attachment generally only live for the duration of a user's session
     * and will also be deleted on exit of the JVM. This method will not create a link to the issue yet, but simply
     * copy the attachment to a temp directory in the attachments folder and store all the relevant details in the
     * returned {@link com.atlassian.jira.issue.attachment.TemporaryAttachment} object
     *
     * @param stream the input stream for the attachment.
     * @param fileName the name of the attachment.
     * @param contentType the content type of the passed stream.
     * @param size the size of the passed stream.
     * @param issue The issue that this temporary attachment is for.  Can be null when creating a new issue.
     * @param project The project where the attachment is to be placed. This is used to do security checks when creating an issue and
     *  there is no issue to run a check on. Will be ignored when issue is not null.
     * @return A {@link com.atlassian.jira.issue.attachment.TemporaryAttachment} containing details about where the temp attachment was created
     * @throws AttachmentException if there was an error saving the temporary attachment.
     *
     * @deprecated Use {@link #createTemporaryAttachment(java.io.InputStream, String, String, long, com.atlassian.jira.issue.Issue, com.atlassian.jira.project.Project, String)}
     */
    TemporaryAttachment createTemporaryAttachment(final InputStream stream, final String fileName,
            final String contentType, long size, final Issue issue, final Project project) throws AttachmentException;

    /**
     * Determine whether an attachment exists and is valid (i.e. non-zero and contains no invalid characters)
     * @param requestWrapper the wrapper containing getFile() and getFilesystemName() describing the attachment
     * @param fileParamName the parameter in the wrapper to use to find attachment info
     * @param required whether having an valid and existent attachment is mandatory
     * @return whether the attachment is valid and exists
     * @throws AttachmentException if the attachment is zero-length, contains invalid characters, or simply doesn't exist
     * when required
     */
    boolean validateAttachmentIfExists(MultiPartRequestWrapper requestWrapper, String fileParamName, boolean required) throws AttachmentException;
}
