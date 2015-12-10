package com.atlassian.jira.service.util.handler;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nullable;

import com.atlassian.annotations.Internal;
import com.atlassian.core.util.FileSize;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.fields.TextFieldCharacterLengthValidator;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.util.AttachmentExceedsLimitException;
import com.atlassian.jira.web.util.AttachmentException;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.primitives.Ints;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

/**
 * This class has been made public only to allow easy unit testing by tests from other packages. This is the standard
 * implementation which dispatches calls to appropriate JIRA managers.
 * This implementation respects the character limit. In case issue description/environment or comment length is too long the text is trimmed to match the size
 * and original text is added as issue attachment, however, if attachments are off or the file with extracted text exceeds the size limit the attachment
 * won't be added and info will be issued in the monitor.
 *
 * @since v5.0
 */
@Internal
public class DefaultMessageHandlerContext implements MessageHandlerContext
{
    private final CommentManager commentManager;
    private final MessageHandlerExecutionMonitor monitor;
    private final IssueManager issueManager;
    private final AttachmentManager attachmentManager;
    private final TextFieldCharacterLengthValidator textFieldCharacterLengthValidator;
    private final PermissionManager permissionManager;
    private final ApplicationProperties applicationProperties;

    public DefaultMessageHandlerContext(CommentManager commentManager, MessageHandlerExecutionMonitor monitor, IssueManager issueManager, AttachmentManager attachmentManager, TextFieldCharacterLengthValidator textFieldCharacterLengthValidator, final PermissionManager permissionManager, final ApplicationProperties applicationProperties)
    {
        this.commentManager = commentManager;
        this.monitor = monitor;
        this.issueManager = issueManager;
        this.attachmentManager = attachmentManager;
        this.textFieldCharacterLengthValidator = textFieldCharacterLengthValidator;
        this.permissionManager = permissionManager;
        this.applicationProperties = applicationProperties;
    }

    @Deprecated
    public DefaultMessageHandlerContext(final CommentManager commentManager, final MessageHandlerExecutionMonitor monitor, final IssueManager issueManager, final AttachmentManager attachmentManager)
    {
        this(commentManager, monitor, issueManager, attachmentManager, ComponentAccessor.getComponent(TextFieldCharacterLengthValidator.class), ComponentAccessor.getPermissionManager(), ComponentAccessor.getApplicationProperties());
    }

    @Override
    public User createUser(String username, String password, String email, String fullname, Integer userEventType)
            throws PermissionException, CreateException
    {
        final User user;
        if (userEventType == null)
        {
            user = ComponentAccessor.getUserUtil().createUserNoNotification(username, password, email, fullname);
        }
        else
        {
            user = ComponentAccessor.getUserUtil().createUserWithNotification(username, password, email, fullname, userEventType);
        }
        monitor.info("Created user '" + user.getName() + ".");
        return user;
    }

    @Override
    public Comment createComment(final Issue issue, final User author, final String body, final boolean dispatchEvent)
    {
        final boolean bodyTooLong = textFieldCharacterLengthValidator.isTextTooLong(body);
        final String commentBody = (bodyTooLong ? trimToCharacterLimit(body) : body);

        if (bodyTooLong)
        {
            monitor.info("Comment body exceeds character limit as has been shortened. Original comment will be added as an attachment");
        }
        final Comment comment = commentManager.create(issue, ApplicationUsers.from(author), commentBody, dispatchEvent);
        monitor.info("Added comment '" + StringUtils.abbreviate(body, 20) + " 'by '" + getAuthorNameOrAnonymousIfNull(author) + "' to issue '" + issue.getKey() + "'");

        if (bodyTooLong)
        {
            addTextAsIssueAttachment(author, body, getI18nBean().getText("messagehandlercontext.issue.comment.extracted.from.mail.filename"), issue);
        }

        return comment;
    }

    private String getAuthorNameOrAnonymousIfNull(final User author)
    {
        return (author == null ? "anonymous" : author.getName());
    }

    @Override
    public Issue createIssue(@Nullable final User reporter, final Issue issue) throws CreateException
    {
        final String originalDescription = issue.getDescription();
        final String originalEnvironment = issue.getEnvironment();
        final boolean descriptionTooLong = textFieldCharacterLengthValidator.isTextTooLong(originalDescription);
        final boolean environmentTooLong = textFieldCharacterLengthValidator.isTextTooLong(originalEnvironment);
        if (descriptionTooLong || environmentTooLong)
        {
            // clone original issue and trim description/environment
            final MutableIssue issueCopy = ComponentAccessor.getIssueFactory().cloneIssue(issue);

            if (descriptionTooLong)
            {
                monitor.info("Issue description exceeds character limit as has been shortened. Original description will be added as an attachment.");
                issueCopy.setDescription(trimToCharacterLimit(originalDescription));
            }
            if (environmentTooLong)
            {
                monitor.info("Issue environment exceeds character limit as has been shortened. Original environment will be added as an attachment.");
                issueCopy.setEnvironment(trimToCharacterLimit(originalEnvironment));
            }
            final Issue createdIssue = createIssueWithIssueManager(reporter, issueCopy);
            // put original description/environment as an attachment(s)
            if (descriptionTooLong)
            {
                addTextAsIssueAttachment(reporter, originalDescription, getI18nBean().getText("messagehandlercontext.issue.description.extracted.from.mail.filename"), createdIssue);
            }
            if (environmentTooLong)
            {
                addTextAsIssueAttachment(reporter, originalEnvironment, getI18nBean().getText("messagehandlercontext.issue.environment.extracted.from.mail.filename"), createdIssue);
            }

            return createdIssue;
        }
        else
        {
            return createIssueWithIssueManager(reporter, issue);
        }
    }

    /**
     * Trims input text if it exceeds character limit and appends "..." at the end (text is trimmed to character limit -
     * 3 to make room for the "..."). If input text does not exceed character limit it's unchanged.
     * If the character limit is set to &lt;4 the first n characters from input text are returned without the "..." appendix.
     *
     * @param text input text
     * @return text trimmed to fit the character limit if necessary otherwise original text
     *
     * @see StringUtils#abbreviate(String, int)
     */
    @VisibleForTesting
    String trimToCharacterLimit(final String text)
    {
        if (textFieldCharacterLengthValidator.isTextTooLong(text))
        {
            final int characterLimit = Ints.saturatedCast(textFieldCharacterLengthValidator.getMaximumNumberOfCharacters());
            // in the unlikely case that character limit is set to less than 4
            // it wouldn't make sense to throw away everything and leave only the ellipsis, take first 1,2 or 3 characters instead
            if (characterLimit < 4)
            {
                return text.substring(0, characterLimit);
            }
            else
            {
                return StringUtils.abbreviate(text, characterLimit);
            }
        }
        else
        {
            return text;
        }
    }

    /**
     * Creates an attachment from input text and adds this attachment to the issue.
     * When a problem occurs (attachments disabled, no permission or attachment file exceeding the limit) it is logged with
     * MessageHandlerExecutionMonitor as an info.
     *
     * @param author reporter user
     * @param content text to be put inside attachment file
     * @param attachmentFileName filename for the attachment to be created
     * @param issue the issue to which the attachment should be added
     */
    @VisibleForTesting
    void addTextAsIssueAttachment(final User author, final String content, final String attachmentFileName, final Issue issue)
    {
        try
        {
            addAttachment(author, content, attachmentFileName, issue);
        }
        catch (AttachmentException ex)
        {
            addFailureAsMonitorInfo(attachmentFileName, issue, ex);
        }
        catch (PermissionException ex)
        {
            addFailureAsMonitorInfo(attachmentFileName, issue, ex);
        }
    }

    private void addFailureAsMonitorInfo(final String attachmentFileName, final Issue issue, final Exception ex)
    {
        monitor.info(String.format("Failed to attach file '%s' as an attachment to issue '%s', reason: '%s'", attachmentFileName, issue.getKey(), ex.getMessage()), ex);
    }

    private void addAttachment(final User author, final String content, final String attachmentFileName, final Issue issue) throws PermissionException, AttachmentException
    {
        validateAddAttachment(author, issue);

        File attachmentFile = null;
        try
        {
            attachmentFile = createTempFile();
            FileUtils.writeStringToFile(attachmentFile, content, applicationProperties.getEncoding());
            createAttachment(attachmentFile, attachmentFileName, "text/plain", author, issue);
        }
        catch (IOException ex)
        {
            throw new AttachmentException("Failed to create attachment file", ex);
        }
        finally
        {
            if (attachmentFile != null)
            {
                attachmentFile.delete();
            }
        }
    }

    @VisibleForTesting
    File createTempFile() throws IOException
    {
        return File.createTempFile("tempattach", "dat");
    }

    @VisibleForTesting
    I18nHelper getI18nBean()
    {
        return ComponentAccessor.getJiraAuthenticationContext().getI18nHelper();
    }

    private Issue createIssueWithIssueManager(final User reporter, final Issue issue) throws CreateException
    {
        final Issue issueObject = issueManager.createIssueObject(reporter, issue);
        monitor.info("Issue " + issueObject.getKey() + " created");
        return issueObject;
    }

    @Override
    public ChangeItemBean createAttachment(File file, String filename, String contentType, User author, Issue issue)
            throws AttachmentException
    {
        validateAttachmentLimitSize(file, filename);
        final ChangeItemBean changeItemBean = attachmentManager.createAttachment(file, filename, contentType, author, issue);
        if (changeItemBean != null)
        {
            monitor.info("Added attachment to issue '" + issue.getKey() + "'");
        }
        return changeItemBean;
    }

    private void validateAddAttachment(final User author, final Issue issue)
            throws AttachmentException, PermissionException
    {
        if (!attachmentManager.attachmentsEnabled())
        {
            throw new AttachmentException("Attachments are disabled");
        }
        if (hasNoPermissionToCreateAttachments(author, issue))
        {
            throw new PermissionException(String.format("User '%s' has no permission to create attachments in project '%s'", getAuthorNameOrAnonymousIfNull(author), issue.getProjectObject().getKey()));
        }
    }

    private void validateAttachmentLimitSize(File file, String fileName) throws AttachmentException {
        long maxAttachmentSize = Long.parseLong(applicationProperties.getDefaultBackedString(APKeys.JIRA_ATTACHMENT_SIZE));
        if (file.length() > maxAttachmentSize)
        {
            final String message = getI18nBean().getText("upload.too.big", fileName,
                    FileSize.format(file.length()), FileSize.format(maxAttachmentSize));
            throw new AttachmentExceedsLimitException(message);
        }
    }

    private boolean hasNoPermissionToCreateAttachments(final User author, final Issue issue)
    {
        return !permissionManager.hasPermission(ProjectPermissions.CREATE_ATTACHMENTS, issue, ApplicationUsers.from(author));
    }

    @Override
    public boolean isRealRun()
    {
        return true;
    }

    @Override
    public MessageHandlerExecutionMonitor getMonitor()
    {
        return monitor;
    }
}
