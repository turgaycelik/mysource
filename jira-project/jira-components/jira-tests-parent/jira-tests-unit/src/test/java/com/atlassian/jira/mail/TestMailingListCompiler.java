/*
 * Copyright (c) 2002-2006
 * All rights reserved.
 */

package com.atlassian.jira.mail;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraApplicationContext;
import com.atlassian.jira.event.issue.IssueEventSource;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.MockComment;
import com.atlassian.jira.issue.worklog.TimeTrackingIssueUpdater;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.mock.MockProjectRoleManager;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.template.TemplateManager;
import com.atlassian.jira.template.TemplateSource;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.user.MockGroup;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.MockUserLocaleStore;
import com.atlassian.jira.user.UserLocaleStore;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.util.AnswerWith;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.mail.queue.MailQueue;
import com.atlassian.mail.queue.MailQueueItem;
import com.atlassian.mail.server.MailServerManager;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.opensymphony.module.propertyset.map.MapPropertySet;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.exception.VelocityException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestMailingListCompiler
{
    @Rule
    public final RuleChain mockito = MockitoMocksInContainer.forTest(this);

    private User user1;
    private User user2;
    private User user3;
    private User user4;
    private Group userGroup;

    public Issue templateIssue;

    @Mock
    @AvailableInContainer
    private ProjectRoleManager projectRoleMangerMock;

    @AvailableInContainer
    private UserLocaleStore localeStore = new MockUserLocaleStore(Locale.ENGLISH);

    @Mock
    @AvailableInContainer
    private UserPropertyManager userPropertyManager;

    @Mock
    @AvailableInContainer
    private MailServerManager mailServerManager;

    @AvailableInContainer (interfaceClass = OfBizDelegator.class, instantiateMe = true)
    private MockOfBizDelegator mockOfBizDelegator;

    @Mock
    @AvailableInContainer
    private VelocityTemplatingEngine velocityTemplatingEngine;

    @Mock
    @AvailableInContainer
    private CssInliner cssInliner;

    @Mock
    @AvailableInContainer
    private TemplateManager templateManager;

    @Mock
    @AvailableInContainer
    private MailQueue mailQueue;

    @Mock
    @AvailableInContainer
    private ProjectManager projectManager;

    @Mock
    @AvailableInContainer
    private IssueManager issueManager;

    @Mock
    @AvailableInContainer
    private GroupManager groupManager;

    @Mock
    @AvailableInContainer
    private TemplateContextFactory templateContextFactory;

    @Mock
    @AvailableInContainer
    private JiraApplicationContext jiraApplicationContext;

    @AvailableInContainer (instantiateMe = true)
    private MockApplicationProperties applicationProperties;

    @Mock
    @AvailableInContainer
    private JiraAuthenticationContext jiraAuthenticationContext;

    private final MockI18nHelper i18nHelper = new MockI18nHelper();

    @AvailableInContainer
    private final I18nHelper.BeanFactory i18nBeanFactory = i18nHelper.factory();

    @Before
    public void setUp() throws Exception
    {

        user1 = new MockUser("text1", "text1", "text1@atlassian.com");
        final MapPropertySet u1Properties = new MapPropertySet();
        u1Properties.setMap(new HashMap());
        u1Properties.setString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE, "text");

        user2 = new MockUser("html1", "html1", "html1@atlassian.com");
        final MapPropertySet u2Properties = new MapPropertySet();
        u2Properties.setMap(new HashMap());
        u2Properties.setString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE, "html");


        user3 = new MockUser("text2", "text2", "text2@atlassian.com");
        final MapPropertySet u3Properties = new MapPropertySet();
        u3Properties.setMap(new HashMap());
        u3Properties.setString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE, "text");

        user4 = new MockUser("html2", "html2", "html2@atlassian.com");
        final MapPropertySet u4Properties = new MapPropertySet();
        u4Properties.setMap(new HashMap());
        u4Properties.setString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE, "html");

        userGroup = new MockGroup("testgroup");
        when(groupManager.isUserInGroup(user3.getName(), userGroup.getName())).thenReturn(true);
        when(groupManager.isUserInGroup(user4.getName(), userGroup.getName())).thenReturn(true);

        // Setting up the issue to be used by the tests
        final MockProject project = new MockProject(10000L, "TST", "Test Project");
        when(projectManager.getProjectObj(project.getId())).thenReturn(project);

        final MockIssue issue = new MockIssue(1, "TST-1");
        issue.setProjectObject(project);
        when(issueManager.getIssueObject(issue.getId())).thenReturn(issue);
        templateIssue = new TemplateIssue(issue, null, null, null, null, null);

        // Setting up the ProjectRoleManager
        when(projectRoleMangerMock.getProjectRole(MockComment.COMMENT_ROLE_NAME)).thenReturn(MockProjectRoleManager.PROJECT_ROLE_TYPE_1);
        when(projectRoleMangerMock.isUserInProjectRole(any(User.class), any(ProjectRole.class), any(Project.class))).thenReturn(true);

        when(userPropertyManager.getPropertySet(user1)).thenReturn(u1Properties);
        when(userPropertyManager.getPropertySet(user2)).thenReturn(u2Properties);
        when(userPropertyManager.getPropertySet(user3)).thenReturn(u3Properties);
        when(userPropertyManager.getPropertySet(user4)).thenReturn(u4Properties);

        final TemplateContext templateContext = mock(TemplateContext.class);
        when(templateContextFactory.getTemplateContext(any(Locale.class))).thenReturn(templateContext);
        when(templateContext.getTemplateParams()).thenReturn(Maps.<String, Object>newHashMap());

        final VelocityTemplatingEngine.RenderRequest renderRequest = mock(VelocityTemplatingEngine.RenderRequest.class);
        when(velocityTemplatingEngine.render(any(TemplateSource.class))).thenReturn(renderRequest);
        when(renderRequest.applying(anyMap())).thenAnswer(AnswerWith.mockInstance());
        when(renderRequest.applying(any(VelocityContext.class))).thenAnswer(AnswerWith.mockInstance());
        when(renderRequest.asPlainText()).thenReturn("Plain Text");
        when(cssInliner.applyStyles(anyString())).thenAnswer(AnswerWith.firstParameter());

        when(jiraAuthenticationContext.getI18nHelper()).thenReturn(i18nHelper);
    }

    @Test
    public void getEmailAddressedShouldJoinOnColon()
    {
        final Set<String> addresses = ImmutableSet.of("address1", "address2", "address3");
        assertEquals("address1,address2,address3", MailingListCompiler.getEmailAddresses(addresses));
    }

    @Test
    public void sendingJustToEmailAddressNoUserDoesNotThrowException() throws Exception
    {
        final Comment comment = new MockComment("John Citizen", "bodytext");
        sendWorkflow("test@example.com", comment, true);
    }

    @Test
    public void noUserOrEmailAddressDoesNotThrowException() throws Exception
    {
        final Comment comment = new MockComment("John Citizen", "bodytext");
        sendWorkflow((String) null, comment, true);
        sendWorkflow("", comment, true);
    }

    /* Test by sending a subscription email to the mailing list */
    @Test
    public void sendingSubscriptionToUserShouldSucceed() throws Exception
    {
        sendList(ImmutableSet.of(new NotificationRecipient(user1)), Maps.<String, Object>newHashMap(), 1);
    }

    /* Test by sending a non restricted worklog to two html users */
    @Test
    public void testSendingHtmlWorklog() throws Exception
    {
        sendWorklog(user2, user4, mock(Worklog.class), null, 2);
    }

    @Test
    public void testSendingHtmlWorklogWithRoleRestriction() throws Exception
    {
        final Worklog worklog = mock(Worklog.class);
        when(worklog.getRoleLevel()).thenReturn(new MockProjectRoleManager.MockProjectRole(1, "My Role", "Test Role Desc"));
        sendWorklog(user2, user4, worklog, null, 2);
    }

    /* Test by sending a non restricted worklog with an updated worklog to two html users */
    @Test
    public void testSendingHtmlUpdatedWorklog() throws Exception
    {
        final Worklog worklog = mock(Worklog.class);
        final Worklog originalWorklog = mock(Worklog.class);
        sendWorklog(user2, user4, worklog, originalWorklog, 2);
    }

    @Test
    public void testSendingHtmlUpdatedWorklogWithRoleRestriction() throws Exception
    {
        final MockProjectRoleManager.MockProjectRole mockProjectRole = new MockProjectRoleManager.MockProjectRole(1, "My Role", "Test Role Desc");

        final Worklog worklog = mock(Worklog.class);
        when(worklog.getRoleLevel()).thenReturn(mockProjectRole);
        final Worklog originalWorklog = mock(Worklog.class);
        when(originalWorklog.getRoleLevel()).thenReturn(mockProjectRole);
        sendWorklog(user2, user4, worklog, originalWorklog, 2);
    }

    /* Test by sending a non restricted worklog to two html users */
    @Test
    public void testSendingTextWorklog() throws Exception
    {
        final Worklog worklog = mock(Worklog.class);
        sendWorklog(user1, user3, worklog, null, 2);
    }

    /* Test by sending a non restricted worklog with an updated worklog to two html users */
    @Test
    public void testSendingTextUpdatedWorklog() throws Exception
    {
        final Worklog worklog = mock(Worklog.class);
        final Worklog originalWorklog = mock(Worklog.class);
        sendWorklog(user1, user3, worklog, originalWorklog, 2);
    }

    /* Test by sending a non restricted comment to two html users */
    @Test
    public void testSendingHtmlComment() throws Exception
    {
        final Comment comment = new MockComment("John Citizen", "bodytext");
        sendComment(user2, user4, comment, 2);
    }

    /* Test by sending a non restricted comment to two text users */
    @Test
    public void testSendingTextComment() throws Exception
    {
        final Comment comment = new MockComment("John Citizen", "bodytext");
        sendComment(user1, user3, comment, 2);
    }

    /* Test by sending a restricted comment to two html users */
    @Test
    public void testSendingHtmlCommentWithGroupRestriction() throws Exception
    {
        final Comment comment = new MockComment("John Citizen", "some test comment", userGroup.getName(), null);
        sendComment(user2, user4, comment, 1);
    }

    /* Test by sending a restricted comment to two text users */
    @Test
    public void testSendingTextCommentWithGroupRestriction() throws Exception
    {
        final Comment comment = new MockComment("John Citizen", "some test comment", userGroup.getName(), null);
        sendComment(user1, user3, comment, 1);
    }

    /* Test by sending a non restricted workflow */
    @Test
    public void testSendingTextWorkflow() throws Exception
    {
        final Comment comment = new MockComment("John Citizen", "some test comment");
        sendWorkflow(user1, comment, true);
    }

    /* Test by sending a non restricted workflow */
    @Test
    public void testSendingHtmlWorkflow() throws Exception
    {
        final Comment comment = new MockComment("John Citizen", "some test comment");
        sendWorkflow(user2, comment, true);
    }

    /* Test by sending a non restricted workflow */
    @Test
    public void testSendingTextWorkflowToUserWithGroup() throws Exception
    {
        final Comment comment = new MockComment("John Citizen", "some test comment");
        sendWorkflow(user3, comment, true);
    }

    /* Test by sending a non restricted workflow */
    @Test
    public void testSendingHtmlWorkflowToUserWithGroup() throws Exception
    {
        final Comment comment = new MockComment("John Citizen", "some test comment");
        sendWorkflow(user4, comment, true);
    }

    /* Test by sending a non restricted workflow */
    @Test
    public void testSendingRestrictedTextWorkflowToUserOutsideGroup() throws Exception
    {
        final Comment comment = new MockComment("John Citizen", "some test comment", userGroup.getName(), null);
        sendWorkflow(user1, comment, false);
    }

    /* Test by sending a non restricted workflow */
    @Test
    public void testSendingRestrictedHtmlWorkflowToUserOutsideGroup() throws Exception
    {
        final Comment comment = new MockComment("John Citizen", "some test comment", userGroup.getName(), null);
        sendWorkflow(user2, comment, false);
    }

    /* Test by sending a non restricted workflow */
    @Test
    public void testSendingRestrictedTextWorkflowToUserWithinGroup() throws Exception
    {
        final Comment comment = new MockComment("John Citizen", "some test comment", userGroup.getName(), null);
        sendWorkflow(user3, comment, true);
    }

    /* Test by sending a non restricted workflow */
    @Test
    public void testSendingRestrictedHtmlWorkflowToUserWithinGroup() throws Exception
    {
        final Comment comment = new MockComment("John Citizen", "some test comment", userGroup.getName(), null);
        sendWorkflow(user4, comment, true);
    }

    /* Send a mail with two users and an is html or not */
    private void sendComment(final User user, final User userWithGroup, final Comment comment, final int numberOfRecipients)
            throws VelocityException
    {
        final Set<NotificationRecipient> users =
                ImmutableSet.of(new NotificationRecipient(user), new NotificationRecipient(userWithGroup));

        final Map<String, Object> eventSource = ImmutableMap.<String, Object>of("eventsource", IssueEventSource.ACTION);

        final Map<String, Object> commentMap =
                MapBuilder.<String, Object>newBuilder().
                        add("params", eventSource).add("comment", comment).add("issue", templateIssue).toMutableMap();

        sendList(users, commentMap, numberOfRecipients);
    }


    private void sendWorklog(final User user, final User userWithGroup, final Worklog worklog, final Worklog originalWorklog, final int numberOfRecipients)
            throws VelocityException
    {
        final Set<NotificationRecipient> users =
                ImmutableSet.of(new NotificationRecipient(user), new NotificationRecipient(userWithGroup));

        final Map<String, Object> eventSource = MapBuilder.<String, Object>newBuilder().
                add("eventsource", IssueEventSource.ACTION).toMutableMap();

        if (originalWorklog != null)
        {
            eventSource.put(TimeTrackingIssueUpdater.EVENT_ORIGINAL_WORKLOG_PARAMETER, originalWorklog);
        }
        final Map<String, Object> worklogMap = MapBuilder.<String, Object>newBuilder().
                add("params", eventSource).add("worklog", worklog).add("issue", templateIssue).toMutableMap();

        sendList(users, worklogMap, numberOfRecipients);
    }

    /* Send a mail with a user and an is html or not */
    private void sendWorkflow(final User user, final Comment comment, final boolean seeComment) throws VelocityException
    {
        final Set<NotificationRecipient> users = ImmutableSet.of(new NotificationRecipient(user));

        final Map<String, Object> eventSource = ImmutableMap.<String, Object>of("eventsource", IssueEventSource.WORKFLOW);

        final Map<String, Object> commentMap = MapBuilder.<String, Object>newBuilder().
                add("params", eventSource).add("comment", comment).toMutableMap();

        final Map<String, Object> commentExpectedMap = MapBuilder.<String, Object>newBuilder().add("params", eventSource).toMutableMap();
        if (seeComment)
        {
            commentExpectedMap.put("comment", comment);
        }
        sendList(users, commentMap, 1);
    }


    private void sendWorkflow(final String emailAddress, final Comment comment, final boolean seeComment)
            throws VelocityException
    {
        final Set<NotificationRecipient> users = ImmutableSet.of(new NotificationRecipient(emailAddress));

        final Map<String, Object> eventSource = ImmutableMap.<String, Object>of("eventsource", IssueEventSource.WORKFLOW);
        final Map<String, Object> commentMap = MapBuilder.<String, Object>newBuilder().
                add("params", eventSource).add("comment", comment).toMutableMap();

        final Map<String, Object> commentExpectedMap = MapBuilder.<String, Object>newBuilder().add("params", eventSource).toMutableMap();
        if (seeComment)
        {
            commentExpectedMap.put("comment", comment);
        }
        sendList(users, commentMap, ((emailAddress != null) && (emailAddress.length() > 0)) ? 1 : 0);
    }

    private void sendList(final Set<NotificationRecipient> users, final Map<String, Object> contextParamsIn,
            final int numberOfRecipients) throws VelocityException
    {
        final MailingListCompiler mailingListCompiler = new MailingListCompiler(templateManager, projectRoleMangerMock);
        mailingListCompiler.sendLists(users, user1.getEmailAddress(), null, 1L, "base", contextParamsIn, null);

        verify(mailQueue, times(numberOfRecipients)).addItem(Mockito.any(MailQueueItem.class));
    }
}
