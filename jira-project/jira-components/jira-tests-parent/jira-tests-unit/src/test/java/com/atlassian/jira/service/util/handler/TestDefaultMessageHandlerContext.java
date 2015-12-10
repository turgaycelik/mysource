package com.atlassian.jira.service.util.handler;


import com.atlassian.core.util.FileSize;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.gzipfilter.org.apache.commons.lang.StringUtils;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.fields.TextFieldCharacterLengthValidator;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockComponentContainer;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserKeyService;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.util.AttachmentExceedsLimitException;
import com.atlassian.jira.web.util.AttachmentException;
import org.apache.commons.io.FileUtils;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestDefaultMessageHandlerContext
{

    private static final String SHORT_TEXT = "This is ok";
    private static final String LONG_TEXT = "Too long text due to character limit setting";
    private static final int MAX_LENGTH = SHORT_TEXT.length();
    private static final String LONG_TEXT_TRIMMED = StringUtils.abbreviate(LONG_TEXT, MAX_LENGTH);
    private static final String COMMENT_FILENAME = "comment.txt";
    private static final String DESCRIPTION_FILENAME = "description.txt";
    private static final String ENVIRONMENT_FILENAME = "environment.txt";
    private static final String MIME_TYPE = "text/plain";
    private static final String FILENAME = "filename";

    private static final long MAX_ATTACHMENT_SIZE = 1024L;
    private static final String ATTACHMENT_EXCEEDING_SIZE = "{0} is too large to attach. Attachment is {1} but the largest allowed attachment is {2}.";
    private static final String username = "Robert'); DROP Table Students; --";
    private static final String password = "' or '1'='1";
    private static final String email = "root@localhost";
    private static final String fullname = "Little Bobby tables";

    DefaultMessageHandlerContext messageHandlerContext;

    @Rule
    public MockComponentContainer container = new MockComponentContainer(this);

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    CommentManager commentManager;

    @Mock
    MessageHandlerExecutionMonitor messageHandlerExecutionMonitor;

    @Mock
    IssueManager issueManager;

    @Mock
    AttachmentManager attachmentManager;

    @Mock
    TextFieldCharacterLengthValidator textFieldCharacterLengthValidator;

    @Mock
    User user;

    @Mock (answer = Answers.RETURNS_SMART_NULLS)
    Issue issue;

    @Mock (answer = Answers.RETURNS_SMART_NULLS)
    MutableIssue clone;

    @Mock
    Issue createdIssue;

    @Mock
    Comment createdComment;

    @Mock
    @AvailableInContainer
    JiraAuthenticationContext jiraAuthenticationContext;

    @Mock
    @AvailableInContainer
    IssueFactory issueFactory;

    @Mock
    I18nHelper i18nHelper;

    @Mock
    @AvailableInContainer
    UserKeyService userKeyService;

    @Mock
    @AvailableInContainer
    UserUtil userUtil;

    @Mock
    PermissionManager permissionManager;

    @Mock
    ApplicationProperties applicationProperties;

    @Mock
    Project project;

    @Mock
    File file;

    @Before
    public void setUp() throws CreateException
    {
        messageHandlerContext = new DefaultMessageHandlerContext(commentManager, messageHandlerExecutionMonitor, issueManager, attachmentManager, textFieldCharacterLengthValidator, permissionManager, applicationProperties)
        {
            @Override
            File createTempFile() throws IOException
            {
                return temporaryFolder.newFile();
            }
        };
        when(textFieldCharacterLengthValidator.isTextTooLong(anyString())).thenAnswer(new Answer<Object>()
        {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable
            {
                String input = (String) invocation.getArguments()[0];
                return input.length() > MAX_LENGTH;
            }
        });
        when(textFieldCharacterLengthValidator.getMaximumNumberOfCharacters()).thenReturn((long) MAX_LENGTH);
        when(issueManager.createIssueObject(any(User.class), eq(issue))).thenReturn(createdIssue);
        when(commentManager.create(eq(issue), any(ApplicationUser.class), anyString(), eq(false))).thenReturn(createdComment);

        when(userKeyService.getKeyForUsername(anyString())).thenReturn("key");

        when(jiraAuthenticationContext.getI18nHelper()).thenReturn(i18nHelper);
        when(i18nHelper.getText("messagehandlercontext.issue.comment.extracted.from.mail.filename")).thenReturn(COMMENT_FILENAME);
        when(i18nHelper.getText("messagehandlercontext.issue.description.extracted.from.mail.filename")).thenReturn(DESCRIPTION_FILENAME);
        when(i18nHelper.getText("messagehandlercontext.issue.environment.extracted.from.mail.filename")).thenReturn(ENVIRONMENT_FILENAME);

        when(user.getName()).thenReturn("Dude");

        when(attachmentManager.attachmentsEnabled()).thenReturn(true);
        when(permissionManager.hasPermission(eq(ProjectPermissions.CREATE_ATTACHMENTS), any(Issue.class), any(ApplicationUser.class))).thenReturn(true);
        when(file.length()).thenReturn(MAX_ATTACHMENT_SIZE / 2);
        when(applicationProperties.getDefaultBackedString(APKeys.JIRA_ATTACHMENT_SIZE)).thenReturn(String.valueOf(MAX_ATTACHMENT_SIZE));
        when(applicationProperties.getEncoding()).thenReturn("UTF-8");

        when(issue.getProjectObject()).thenReturn(project);
        when(clone.getKey()).thenReturn("clone:key");
    }

    @Test
    public void verifiesIssueDescriptionDoesNotExceedMaximumNumberOfCharacters() throws Exception
    {
        when(issue.getDescription()).thenReturn(SHORT_TEXT);
        messageHandlerContext.createIssue(user, issue);
        verify(textFieldCharacterLengthValidator).isTextTooLong(eq(SHORT_TEXT));
    }

    @Test
    public void verifiesCommentDoesNotExceedMaximumNumberOfCharacters() throws Exception
    {
        messageHandlerContext.createComment(issue, user, SHORT_TEXT, false);
        verify(textFieldCharacterLengthValidator).isTextTooLong(eq(SHORT_TEXT));
    }

    @Test
    public void verifiesIssueEnvironmentDoesNotExceedMaximumNumberOfCharacters() throws Exception
    {
        when(issue.getEnvironment()).thenReturn(SHORT_TEXT);
        messageHandlerContext.createIssue(user, issue);
        verify(textFieldCharacterLengthValidator).isTextTooLong(eq(SHORT_TEXT));
    }

    @Test
    public void createsIssueWhenDescriptionDoesNotExceedMaximumNumberOfCharacters() throws Exception
    {
        when(issue.getDescription()).thenReturn(SHORT_TEXT);
        messageHandlerContext.createIssue(user, issue);
        verify(issueManager).createIssueObject(user, issue);
    }

    @Test
    public void createsIssueWhenEnvironmentDoesNotExceedMaximumNumberOfCharacters() throws Exception
    {
        when(issue.getEnvironment()).thenReturn(SHORT_TEXT);
        messageHandlerContext.createIssue(user, issue);
        verify(issueManager).createIssueObject(user, issue);
    }

    @Test
    public void returnsIssueCreatedByIssueManager() throws Exception
    {
        Issue theIssue = messageHandlerContext.createIssue(user, issue);
        assertThat(theIssue, sameInstance(createdIssue));
    }

    @Test
    public void createIssueWithDescriptionExceedingCharacterLimitCreatesIssueWithTrimmedDescriptionAndFullTextAsAttachment()
            throws Exception
    {
        when(issue.getDescription()).thenReturn(LONG_TEXT);
        when(issue.getEnvironment()).thenReturn(SHORT_TEXT);

        when(issueFactory.cloneIssue(eq(issue))).thenReturn(clone);
        when(issueManager.createIssueObject(user, clone)).thenReturn(clone);

        messageHandlerContext.createIssue(user, issue);

        verify(clone).setDescription(LONG_TEXT_TRIMMED);
        verify(issueManager).createIssueObject(user, clone);

        verify(attachmentManager).createAttachment(any(File.class), eq(DESCRIPTION_FILENAME), eq(MIME_TYPE), eq(user), eq(clone));
        verifyNoMoreInteractions(issueManager);
    }

    @Test
    public void createIssueWithEnvironmentExceedingCharacterLimitCreatesIssueWithTrimmedDescriptionAndFullTextAsAttachment()
            throws Exception
    {
        when(issue.getDescription()).thenReturn(SHORT_TEXT);
        when(issue.getEnvironment()).thenReturn(LONG_TEXT);

        when(issueFactory.cloneIssue(eq(issue))).thenReturn(clone);
        when(issueManager.createIssueObject(user, clone)).thenReturn(clone);

        messageHandlerContext.createIssue(user, issue);

        verify(clone).setEnvironment(LONG_TEXT_TRIMMED);
        verify(issueManager).createIssueObject(user, clone);

        verify(attachmentManager).createAttachment(any(File.class), eq(ENVIRONMENT_FILENAME), eq(MIME_TYPE), eq(user), eq(clone));
        verifyNoMoreInteractions(issueManager);
    }

    @Test
    public void returnsCommentCreatedByCommentManager() throws Exception
    {
        Comment comment = messageHandlerContext.createComment(issue, user, SHORT_TEXT, false);
        assertThat(comment, sameInstance(createdComment));
    }

    @Test
    public void createCommentHappyPath()
            throws Exception
    {
        messageHandlerContext.createComment(issue, user, SHORT_TEXT, false);

        verify(textFieldCharacterLengthValidator).isTextTooLong(eq(SHORT_TEXT));
        verify(commentManager).create(eq(issue), any(ApplicationUser.class), eq(SHORT_TEXT), eq(false));
        verifyNoMoreInteractions(commentManager, attachmentManager);
    }

    @Test
    public void createCommentExceedingCharacterLimitCreatesTrimmedCommentAndAttachmentWithOriginalBody()
            throws Exception
    {
        messageHandlerContext.createComment(issue, user, LONG_TEXT, false);

        verify(textFieldCharacterLengthValidator, times(2)).isTextTooLong(eq(LONG_TEXT));
        verify(messageHandlerExecutionMonitor).info(argThat(containsString("exceeds character limit")));
        verify(commentManager).create(eq(issue), any(ApplicationUser.class), eq(LONG_TEXT_TRIMMED), eq(false));
        verifyNoMoreInteractions(commentManager);

        verify(attachmentManager).createAttachment(any(File.class), eq(COMMENT_FILENAME), eq(MIME_TYPE), any(User.class), eq(issue));
    }

    @Test
    public void createAttachmentHappyPath() throws Exception
    {
        String filename = "attachment1";
        String mimeType = "some/type";

        ChangeItemBean attachment = mock(ChangeItemBean.class);
        when(attachmentManager.createAttachment(file, filename, mimeType, user, issue)).thenReturn(attachment);

        final ChangeItemBean returnedAttachment = messageHandlerContext.createAttachment(file, filename, mimeType, user, issue);

        verify(attachmentManager).createAttachment(file, filename, mimeType, user, issue);

        assertThat(returnedAttachment, is(attachment));
    }

    @Test
    public void createAttachmentIgnoresExceedingFileSize() throws Exception
    {
        String filename = "attachment";
        String mimeType = "some/type";

        when(applicationProperties.getDefaultBackedString(APKeys.JIRA_ATTACHMENT_SIZE)).thenReturn("1");
        when(i18nHelper.getText("upload.too.big", filename, FileSize.format(file.length()), FileSize.format(1))).thenReturn(ATTACHMENT_EXCEEDING_SIZE);

        ChangeItemBean cib = null;
        try
        {
            cib = messageHandlerContext.createAttachment(file, filename, mimeType, user, issue);
        }
        catch (AttachmentExceedsLimitException ex)
        {
            assertThat(ex.getMessage(), containsString("is too large to attach"));
        }
        assertThat("createAttachment() returns null", cib == null, is(true));
        verify(attachmentManager, never()).createAttachment(any(File.class), anyString(), anyString(), eq(user), eq(issue));
    }

    @Test
    public void addAttachmentDeletesTempFileOnAttachmentExceptionAndAddsWarning() throws Exception
    {
        final File f = temporaryFolder.newFile();
        final String attachmentExceptionMessage = "Forced exception";
        DefaultMessageHandlerContext messageHandlerContextWithTempFile = getMessageHandlerContextWithTempFileAndAttachmentException(f, attachmentExceptionMessage);

        messageHandlerContextWithTempFile.addTextAsIssueAttachment(user, "content", FILENAME, issue);

        verify(messageHandlerExecutionMonitor).info(
                argThat(Matchers.allOf(containsString("Failed to attach file"), containsString(attachmentExceptionMessage))),
                argThat(Matchers.<AttachmentException>instanceOf(AttachmentException.class)));
        assertThat("Temporary file created should be deleted when adding attachment fails", f.exists(), is(false));
    }

    private DefaultMessageHandlerContext getMessageHandlerContextWithTempFileAndAttachmentException(final File f, final String exceptionMessage)
    {
        return new DefaultMessageHandlerContext(commentManager, messageHandlerExecutionMonitor, issueManager, attachmentManager, textFieldCharacterLengthValidator, permissionManager, applicationProperties)
        {
            @Override
            File createTempFile() throws IOException
            {
                return f;
            }

            @Override
            public ChangeItemBean createAttachment(File file, String filename, String contentType, User author, Issue issue)
                    throws AttachmentException
            {
                throw new AttachmentException(exceptionMessage);
            }
        };
    }

    @Test
    public void addAttachmentDeletesTempFileOnAttachmentSuccessfullyAdded() throws Exception
    {
        File f = temporaryFolder.newFile();
        DefaultMessageHandlerContext messageHandlerContextWithTempFile = getMessageHandlerContextWithTempFile(f);

        messageHandlerContextWithTempFile.addTextAsIssueAttachment(user, "content", FILENAME, issue);

        assertThat("Temporary file created should be deleted after attachment is added", f.exists(), is(false));
    }

    private DefaultMessageHandlerContext getMessageHandlerContextWithTempFile(final File f)
    {
        return new DefaultMessageHandlerContext(commentManager, messageHandlerExecutionMonitor, issueManager, attachmentManager, textFieldCharacterLengthValidator, permissionManager, applicationProperties)
        {
            @Override
            File createTempFile() throws IOException
            {
                return f;
            }

        };
    }

    @Test
    public void addAttachmentDoesNotAddFileWhenAttachmentsAreOff() throws Exception
    {
        when(attachmentManager.attachmentsEnabled()).thenReturn(false);

        messageHandlerContext.addTextAsIssueAttachment(user, "content", FILENAME, issue);

        verify(messageHandlerExecutionMonitor).info(
                argThat(containsString("Failed to attach file")),
                argThat(Matchers.<AttachmentException>instanceOf(AttachmentException.class)));

        verify(attachmentManager, never()).createAttachment(any(File.class), anyString(), anyString(), eq(user), eq(issue));
    }

    @Test
    public void addAttachmentDoesNotAddFileWhenIOExceptionOccurs() throws Exception
    {
        // folder will trigger a IOException when opening a stream
        messageHandlerContext = getMessageHandlerContextWithTempFile(temporaryFolder.newFolder());

        messageHandlerContext.addTextAsIssueAttachment(user, "content", FILENAME, issue);

        verify(messageHandlerExecutionMonitor).info(
                argThat(containsString("Failed to attach file")),
                argThat(Matchers.<AttachmentException>instanceOf(AttachmentException.class)));

        verify(attachmentManager, never()).createAttachment(any(File.class), anyString(), anyString(), eq(user), eq(issue));
    }

    @Test
    public void addAttachmentDoesNotAddFileWhenUserIsMissingPermission() throws Exception
    {
        when(permissionManager.hasPermission(eq(ProjectPermissions.CREATE_ATTACHMENTS), eq(issue), any(ApplicationUser.class))).thenReturn(false);

        messageHandlerContext.addTextAsIssueAttachment(user, "content", FILENAME, issue);

        verify(messageHandlerExecutionMonitor).info(
                argThat(containsString("Failed to attach file")),
                argThat(Matchers.<PermissionException>instanceOf(PermissionException.class)));

        verify(attachmentManager, never()).createAttachment(any(File.class), anyString(), anyString(), eq(user), eq(issue));
    }

    @Test
    public void addAttachmentDoesNotThrowNPEWhenAuthorIsNull() throws Exception
    {
        when(permissionManager.hasPermission(eq(ProjectPermissions.CREATE_ATTACHMENTS), eq(issue), any(ApplicationUser.class))).thenReturn(false);

        messageHandlerContext.addTextAsIssueAttachment(null, "content", FILENAME, issue);

        verify(messageHandlerExecutionMonitor).info(
                argThat(containsString("Failed to attach file")),
                argThat(Matchers.<PermissionException>instanceOf(PermissionException.class)));

        verify(attachmentManager, never()).createAttachment(any(File.class), anyString(), anyString(), any(User.class), eq(issue));
    }

    @Test
    public void addAttachmentDoesNotAddFileWhenItExceedsSizeLimit() throws Exception
    {
        when(applicationProperties.getDefaultBackedString(APKeys.JIRA_ATTACHMENT_SIZE)).thenReturn("1");

        messageHandlerContext.addTextAsIssueAttachment(user, "content", FILENAME, issue);

        verify(messageHandlerExecutionMonitor).info(
                argThat(containsString("Failed to attach file")),
                argThat(Matchers.<AttachmentException>instanceOf(AttachmentException.class)));

        verify(attachmentManager, never()).createAttachment(any(File.class), anyString(), anyString(), eq(user), eq(issue));
    }

    @Test
    public void addAttachmentUsesJiraEncoding() throws Exception
    {
        // pick the one that is not the default jvm encoding
        final String encoding = ("UTF-8").equalsIgnoreCase(Charset.defaultCharset().name()) ? "UTF-16" : "UTF-8";
        final String content = "Testing encoding \uD83D\uDE02";

        final String[] fileContent = new String[1];

        when(applicationProperties.getEncoding()).thenReturn(encoding);
        when(attachmentManager.createAttachment(any(File.class), eq(FILENAME), eq(MIME_TYPE), eq(user), eq(issue))).thenAnswer(
                new Answer<ChangeItemBean>()
                {
                    @Override
                    public ChangeItemBean answer(final InvocationOnMock invocation) throws Throwable
                    {
                        final File f = (File) invocation.getArguments()[0];
                        // read the temporary file created using jira encoding
                        fileContent[0] = FileUtils.readFileToString(f, encoding);
                        return null;
                    }
                }
        );
        messageHandlerContext.addTextAsIssueAttachment(user, content, FILENAME, issue);

        assertThat("Attachment file is written using JIRA encoding", fileContent[0], is(content));
    }

    @Test
    public void trimToCharacterLimitReturnsOriginalTextIfNotExceedingTheLimit()
    {
        final String text = "Original text, a very very long text";
        when(textFieldCharacterLengthValidator.getMaximumNumberOfCharacters()).thenReturn((long) text.length() + 1);

        final String s = messageHandlerContext.trimToCharacterLimit(text);

        assertThat(s, is(text));
    }

    @Test
    public void trimToCharacterLimitReturnsFirstNCharactersWhenLimitIsLessThan4()
    {
        when(textFieldCharacterLengthValidator.isTextTooLong("DEADBEEF")).thenReturn(true);
        when(textFieldCharacterLengthValidator.getMaximumNumberOfCharacters()).thenReturn((long) 3);

        final String s = messageHandlerContext.trimToCharacterLimit("DEADBEEF");

        assertThat(s, is("DEA"));
    }

    @Test
    public void trimToCharacterLimitTrimsTextAndAddsEllipsis()
    {
        when(textFieldCharacterLengthValidator.getMaximumNumberOfCharacters()).thenReturn((long) 10);

        final String s = messageHandlerContext.trimToCharacterLimit("1234567890ABCDEF");

        assertThat(s, is("1234567..."));
    }

    @Test
    public void createUserWithNullEventTypeCreatesUserWithoutNotification() throws Exception
    {
        when(userUtil.createUserNoNotification(username, password, email, fullname)).thenReturn(user);

        final User createdUser = messageHandlerContext.createUser(username, password, email, fullname, null);

        verify(userUtil).createUserNoNotification(username, password, email, fullname);

        assertThat(createdUser, is(user));
    }

    @Test
    public void createUserWithAnEventTypeCreatesUserWithNotification() throws Exception
    {
        final Integer notificationType = 1;
        when(userUtil.createUserWithNotification(username, password, email, fullname, notificationType)).thenReturn(user);

        final User createdUser = messageHandlerContext.createUser(username, password, email, fullname, notificationType);

        verify(userUtil).createUserWithNotification(username, password, email, fullname, notificationType);

        assertThat(createdUser, is(user));
    }

}