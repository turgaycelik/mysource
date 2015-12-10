/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.web.action.admin.mail;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.atlassian.jira.JiraApplicationContext;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mail.CssInliner;
import com.atlassian.jira.mail.Email;
import com.atlassian.jira.mail.TemplateContext;
import com.atlassian.jira.mail.TemplateContextFactory;
import com.atlassian.jira.matchers.IterableMatchers;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.mock.MockProjectRoleManager;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.permission.Permission;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleActor;
import com.atlassian.jira.security.roles.ProjectRoleActors;
import com.atlassian.jira.security.roles.ProjectRoleActorsImpl;
import com.atlassian.jira.security.roles.ProjectRoleImpl;
import com.atlassian.jira.security.roles.RoleActor;
import com.atlassian.jira.security.roles.RoleActorDoesNotExistException;
import com.atlassian.jira.template.TemplateSource;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.user.MockGroup;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.AnswerWith;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.mail.MailException;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.mail.server.SMTPMailServer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.apache.velocity.VelocityContext;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericValue;

import webwork.action.Action;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestSendBulkMail
{
    @Rule
    public final RuleChain mockito = MockitoMocksInContainer.forTest(this);

    @Mock
    private MailServerManager mailServerManager;

    @Mock
    private PermissionManager permissionManager;

    @Mock
    private ProjectRoleService projectRoleService;

    @Mock
    private ProjectManager projectManager;

    @Mock
    private UserUtil userUtil;

    @Mock
    private GroupManager groupManager;

    @Mock
    private SMTPMailServer smtpMailServer;

    @Mock
    @AvailableInContainer
    private JiraAuthenticationContext jiraAuthenticationContext;

    @Mock
    @AvailableInContainer
    private JiraApplicationContext jiraApplicationContext;

    @Mock
    @AvailableInContainer
    private TemplateContextFactory templateContextFactory;

    @Mock
    @AvailableInContainer
    private VelocityTemplatingEngine velocityTemplatingEngine;

    @Mock
    @AvailableInContainer
    private CssInliner cssInliner;

    @AvailableInContainer (instantiateMe = true)
    private MockApplicationProperties applicationProperties;

    private final I18nHelper i18nHelper = new MockI18nHelper();

    private final String userEmail = "quite@delicio.us";
    private final User user = new MockUser("testuser", "Test User", userEmail);

    private SendBulkMail sendBulkEmail;

    @Before
    public void setUp() throws Exception
    {
        when(jiraAuthenticationContext.getLoggedInUser()).thenReturn(user);
        when(jiraAuthenticationContext.getI18nHelper()).thenReturn(i18nHelper);

        final TemplateContext templateContext = mock(TemplateContext.class);
        when(templateContextFactory.getTemplateContext(any(Locale.class))).thenReturn(templateContext);
        when(templateContext.getTemplateParams()).thenReturn(Maps.<String, Object>newHashMap());

        final VelocityTemplatingEngine.RenderRequest renderRequest = mock(VelocityTemplatingEngine.RenderRequest.class);
        when(velocityTemplatingEngine.render(any(TemplateSource.class))).thenReturn(renderRequest);
        when(renderRequest.applying(anyMap())).thenAnswer(AnswerWith.mockInstance());
        when(renderRequest.applying(any(VelocityContext.class))).thenAnswer(AnswerWith.mockInstance());
        when(renderRequest.asPlainText()).thenReturn("Plain Text");
        when(cssInliner.applyStyles(anyString())).thenAnswer(AnswerWith.firstParameter());

        sendBulkEmail = new SendBulkMail(mailServerManager, permissionManager, projectRoleService, projectManager, userUtil, groupManager);
    }

    @Test
    public void hasMailServerShouldReturnFalseWhenNoMailServerAvailable() throws Exception
    {
        assertFalse("Should not provide an existing SMTP server when none provided.", sendBulkEmail.isHasMailServer());
    }

    @Test
    public void hasMailServerShouldReturnTrueWhenServerAvailable() throws Exception
    {
        when(mailServerManager.getDefaultSMTPMailServer()).thenReturn(smtpMailServer);
        assertTrue("Should figure out the smtp server.", sendBulkEmail.isHasMailServer());
    }

    @Test
    public void byDefaultHtmlAndTextMimeTypesShouldBePresent()
    {
        assertEquals(ImmutableMap.of(
                NotificationRecipient.MIMETYPE_HTML, NotificationRecipient.MIMETYPE_HTML_DISPLAY,
                NotificationRecipient.MIMETYPE_TEXT, NotificationRecipient.MIMETYPE_TEXT_DISPLAY
        ), sendBulkEmail.getMimeTypes());
    }

    @Test
    public void gettingGroupsFieldSizeShouldReturnTrimmedSize()
            throws OperationNotPermittedException, InvalidGroupException
    {
        final Group dummy = mock(Group.class);
        final List<Group> groupList = Lists.newArrayList();

        // the #getGroupsFieldSize figures out the size of a dropdown list that shows groups. The logic
        // here is showing the number of system groups +1 decorated row, but cap at a certain size.
        // This is tested by mocking increasing number of groups in the system.
        for (int i = 0; i < SendBulkMail.MAX_MULTISELECT_SIZE + 2; i++)
        {
            groupList.add(dummy);
            when(groupManager.getAllGroups()).thenReturn(ImmutableList.copyOf(groupList));
            assertEquals(Math.min(groupList.size() + 1, SendBulkMail.MAX_MULTISELECT_SIZE), sendBulkEmail.getGroupsFieldSize());
        }
    }

    @Test
    public void gettingProjectRolesFieldSizeShouldReturnTrimmedSize()
    {
        //Prepare mock manager for returning a mock project collection to SendBulkMail
        final List<GenericValue> mockProjectList = Lists.newArrayList();

        //Prepare mock service for returning a mock project role collection to SendBulkMail
        final List<ProjectRole> mockProjectRoleList = Lists.newArrayList();

        // the #getProjectsRolesFieldSize figures out the size of a dropdown list that shows roles and projects. The
        // logic here is showing the number of these +1 decorated row, but cap at a certain size.
        // This is tested by mocking increasing number of projects and rows in the system.

        //Test by incrementing number of projects
        for (int i = 1; i <= SendBulkMail.MAX_MULTISELECT_SIZE - 1; i++)
        {
            mockProjectList.add(new MockGenericValue("Project " + i, emptyMap()));
            when(permissionManager.getProjects(Permission.BROWSE.getId(), user)).thenReturn(ImmutableList.copyOf(mockProjectList));
            assertEquals(i + 1, sendBulkEmail.getProjectsRolesFieldSize());
        }

        mockProjectList.clear();
        when(permissionManager.getProjects(Permission.BROWSE.getId(), user)).thenReturn(Collections.<GenericValue>emptyList());
        assertEquals(1, sendBulkEmail.getProjectsRolesFieldSize());

        //Test by incrementing number of roles
        for (int i = 1; i <= SendBulkMail.MAX_MULTISELECT_SIZE - 1; i++)
        {
            mockProjectRoleList.add(mock(ProjectRole.class));
            when(projectRoleService.getProjectRoles(same(user), any(ErrorCollection.class))).thenReturn(ImmutableList.copyOf(mockProjectRoleList));
            assertEquals(i + 1, sendBulkEmail.getProjectsRolesFieldSize());
        }

        mockProjectRoleList.clear();
        when(projectRoleService.getProjectRoles(same(user), any(ErrorCollection.class))).thenReturn(Collections.<ProjectRole>emptyList());
        assertEquals(1, sendBulkEmail.getProjectsRolesFieldSize());
    }

    @Test
    public void executeShouldFailWhenNoMailServerDefined() throws Exception
    {
        assertEquals(Action.INPUT, sendBulkEmail.execute());
        assertThat(sendBulkEmail.getErrorMessages(), Matchers.contains("admin.errors.no.mail.server"));
    }

    @Test
    public void sendToGroupsShouldFailWhenNoGroupsGiven() throws Exception
    {
        when(mailServerManager.getDefaultSMTPMailServer()).thenReturn(smtpMailServer);
        sendBulkEmail.setSendToRoles(false);

        assertEquals(Action.INPUT, sendBulkEmail.execute());
        assertMapsEqual(ImmutableMap.of(
                "sendToRoles", "admin.errors.select.one.group",
                "subject", "admin.errors.no.subject",
                "messageType", "admin.errors.no.message.type",
                "message", "admin.errors.no.body"
        ), sendBulkEmail.getErrors());
    }

    @Test
    public void sendingToRolesShouldFailWhenNoRolesOrProjectsGiven() throws Exception
    {
        when(mailServerManager.getDefaultSMTPMailServer()).thenReturn(smtpMailServer);
        sendBulkEmail.setSendToRoles(true);

        assertEquals(Action.INPUT, sendBulkEmail.execute());
        assertMapsEqual(ImmutableMap.of(
                "sendToRoles", "admin.errors.select.one.project.and.role",
                "subject", "admin.errors.no.subject",
                "messageType", "admin.errors.no.message.type",
                "message", "admin.errors.no.body"
        ), sendBulkEmail.getErrors());
    }

    @Test
    public void sendingToRolesShouldFailWhenNoProjectsGiven() throws Exception
    {
        when(mailServerManager.getDefaultSMTPMailServer()).thenReturn(smtpMailServer);
        sendBulkEmail.setSendToRoles(true);

        sendBulkEmail.setRoles(asArray("something"));
        assertEquals(Action.INPUT, sendBulkEmail.execute());
        assertMapsEqual(ImmutableMap.of(
                "sendToRoles", "admin.errors.select.one.project",
                "subject", "admin.errors.no.subject",
                "messageType", "admin.errors.no.message.type",
                "message", "admin.errors.no.body"
        ), sendBulkEmail.getErrors());
    }

    @Test
    public void sendingToRolesShouldFailWhenNoRolesGiven() throws Exception
    {
        when(mailServerManager.getDefaultSMTPMailServer()).thenReturn(smtpMailServer);
        sendBulkEmail.setSendToRoles(true);

        sendBulkEmail.setProjects(new String[] { "something" });
        assertEquals(Action.INPUT, sendBulkEmail.execute());
        assertMapsEqual(ImmutableMap.of(
                "sendToRoles", "admin.errors.select.one.role",
                "subject", "admin.errors.no.subject",
                "messageType", "admin.errors.no.message.type",
                "message", "admin.errors.no.body"
        ), sendBulkEmail.getErrors());
    }

    @Test
    public void sendingToGroupsShouldFailWhenGroupHasNoUsers() throws Exception
    {
        when(mailServerManager.getDefaultSMTPMailServer()).thenReturn(smtpMailServer);

        // Setup a group
        final Group testGroup = new MockGroup("testGroup");
        when(groupManager.getGroup(testGroup.getName())).thenReturn(testGroup);

        setValidMailSettings(false);
        sendBulkEmail.setGroups(new String[] { testGroup.getName() });

        assertEquals(Action.INPUT, sendBulkEmail.execute());
        assertEquals(singletonMap("sendToRoles", "admin.errors.empty.groups"), sendBulkEmail.getErrors());
    }

    @Test
    public void sendingToRolesShouldFailWhenRolesHaveNoActors() throws Exception
    {
        when(mailServerManager.getDefaultSMTPMailServer()).thenReturn(smtpMailServer);

        final Project project = new MockProject(2L, "TST");
        final ProjectRole pr = new ProjectRoleImpl(1L, "Test Project Role", "a test project role");
        final ProjectRoleActors actors = new ProjectRoleActorsImpl(null, pr.getId(), Collections.<RoleActor>emptySet());
        when(projectManager.getProjectObj(project.getId())).thenReturn(project);
        when(projectRoleService.getProjectRole(same(user), eq(pr.getId()), any(ErrorCollection.class))).thenReturn(pr);
        when(projectRoleService.getProjectRoleActors(same(user), same(pr), same(project), any(ErrorCollection.class))).thenReturn(actors);

        setValidMailSettings(true);
        sendBulkEmail.setRoles(asArray(Long.toString(pr.getId())));
        sendBulkEmail.setProjects(asArray(Long.toString(project.getId())));

        assertEquals(Action.INPUT, sendBulkEmail.execute());
        assertEquals(singletonMap("sendToRoles", "admin.errors.empty.projectroles"), sendBulkEmail.getErrors());
    }

    @Test
    public void sendingWithInvalidReplyToEmailShouldFail() throws Exception
    {
        when(mailServerManager.getDefaultSMTPMailServer()).thenReturn(smtpMailServer);

        // Setup a group
        final Group testGroup = new MockGroup("test group");
        final User testUser = new MockUser("Test User 1");
        when(groupManager.getGroup(testGroup.getName())).thenReturn(testGroup);
        when(userUtil.getUsersInGroupNames(singletonList(testGroup.getName()))).thenReturn(Sets.newTreeSet(singletonList(testUser)));

        setValidMailSettings(false);
        sendBulkEmail.setGroups(asArray(testGroup.getName()));
        sendBulkEmail.setReplyTo("bademail");

        assertEquals(Action.INPUT, sendBulkEmail.execute());
        assertEquals(singletonMap("replyTo", "admin.errors.invalid.email"), sendBulkEmail.getErrors());
    }

    @Test
    public void emailingToProjectRolesShouldCorrectlyFindUsers() throws Exception
    {
        when(mailServerManager.getDefaultSMTPMailServer()).thenReturn(smtpMailServer);

        final Project project = new MockProject(1L, "TST");
        when(projectManager.getProjectObj(1L)).thenReturn(project);

        final ProjectRole pr1 = new ProjectRoleImpl(1L, "Test Project Role 1", "a test project role1");
        final ProjectRole pr2 = new ProjectRoleImpl(2L, "Test Project Role 2", "a test project role2");
        final ProjectRole pr3 = new ProjectRoleImpl(3L, "Test Project Role 3", "a test project role3");

        final User user1 = new MockUser("tu1", "Test User 1", "tu1@example.com");
        final User user2 = new MockUser("tu2", "Test User 2", "tu2@example.com");
        final User user3 = new MockUser("tu3", "Test User 3", "tu3@example.com");
        final User user4 = new MockUser("tu4", "Test User 4", "tu4@example.com");
        ((MockUser) user4).setActive(false);

        when(projectRoleService.getProjectRole(same(user), eq(pr1.getId()), any(ErrorCollection.class))).thenReturn(pr1);
        when(projectRoleService.getProjectRole(same(user), eq(pr2.getId()), any(ErrorCollection.class))).thenReturn(pr2);
        when(projectRoleService.getProjectRole(same(user), eq(pr3.getId()), any(ErrorCollection.class))).thenReturn(pr3);

        when(projectRoleService.getProjectRoleActors(same(user), same(pr1), same(project), any(ErrorCollection.class)))
                .thenReturn(makeMockProjectRoleActors(ImmutableList.of(user1, user2)));
        when(projectRoleService.getProjectRoleActors(same(user), same(pr2), same(project), any(ErrorCollection.class)))
                .thenReturn(makeMockProjectRoleActors(ImmutableList.of(user2, user3, user4)));
        when(projectRoleService.getProjectRoleActors(same(user), same(pr3), same(project), any(ErrorCollection.class)))
                .thenReturn(makeMockProjectRoleActors(Collections.<User>emptyList()));

        setValidMailSettings(true);
        sendBulkEmail.setProjects(asArray("1"));
        sendBulkEmail.setRoles(asArray("1", "2", "3"));

        // Let SendBulkMail initialize the users collection
        assertEquals(Action.SUCCESS, sendBulkEmail.execute());

        // Test that the correct users are returned
        assertEquals(ImmutableList.of(user1, user2, user3), sendBulkEmail.getUsers());
        verify(smtpMailServer).send(any(Email.class));
    }

    @Test
    public void emailingToGroupsShouldCorrectlyFindUsers() throws Exception
    {
        when(mailServerManager.getDefaultSMTPMailServer()).thenReturn(smtpMailServer);

        // Setup a group
        final Group testGroup1 = new MockGroup("test group 1");
        final Group testGroup2 = new MockGroup("test group 2");

        // Setup users
        final String testEmail1 = "email1@email.com";
        final User testUser1 = new MockUser("Test User 1", "", testEmail1);

        final String testEmail2 = "email2@email.com";
        final User testUser2 = new MockUser("Test User 2", "", testEmail2);

        final String testEmail3 = "email3@email.com";
        final User testUser3 = new MockUser("Test User 3", "", testEmail3);

        final String testEmail4 = "email4@email.com";
        final User testUser4 = new MockUser("Test User 4", "", testEmail4);
        ((MockUser) testUser4).setActive(false);

        when(userUtil.getUsersInGroupNames(eq(ImmutableList.of(testGroup1.getName(), testGroup2.getName()))))
                .thenReturn(Sets.newTreeSet(ImmutableList.of(testUser1, testUser2, testUser3, testUser4)));

        setValidMailSettings(false);
        sendBulkEmail.setGroups(asArray(testGroup1.getName(), testGroup2.getName()));

        // Let SendBulkMail initialize the users collection
        assertEquals(Action.SUCCESS, sendBulkEmail.execute());

        // Test that the correct users are returned
        assertThat((Iterable<User>) sendBulkEmail.getUsers(), containsInAnyOrder(testUser1, testUser2, testUser3));
        verify(smtpMailServer).send(any(Email.class));
    }

    @Test
    public void actionShouldHandleEmailExceptionsThrownByTheServer() throws Exception
    {
        final String expectedExceptionMessage = "Test Mail Exception.";
        when(mailServerManager.getDefaultSMTPMailServer()).thenReturn(smtpMailServer);
        doThrow(new MailException(expectedExceptionMessage)).when(smtpMailServer).send(any(Email.class));

        final Group testGroup1 = new MockGroup("test group");
        when(userUtil.getUsersInGroupNames(eq(singletonList(testGroup1.getName())))).thenReturn(Sets.newTreeSet(singletonList(user)));

        setValidMailSettings(false);
        sendBulkEmail.setGroups(asArray(testGroup1.getName()));

        assertEquals(Action.ERROR, sendBulkEmail.execute());
        assertEquals(singletonList("admin.errors.the.error.was " + expectedExceptionMessage), sendBulkEmail.getErrorMessages());
        assertThat(sendBulkEmail.getStatus(), containsString("admin.errors.failed.to.send"));
    }

    @Test
    public void sendingEmailWithNoReplyToAddressShouldSucceed() throws Exception
    {
        when(mailServerManager.getDefaultSMTPMailServer()).thenReturn(smtpMailServer);

        final Group testGroup = new MockGroup("test group");
        when(userUtil.getUsersInGroupNames(eq(singletonList(testGroup.getName())))).thenReturn(Sets.newTreeSet(singletonList(user)));

        setValidMailSettings(false);
        sendBulkEmail.setGroups(asArray(testGroup.getName()));

        assertEquals(Action.SUCCESS, sendBulkEmail.execute());
        verify(smtpMailServer).send(any(Email.class));
    }

    @Test
    public void sendingEmailWithReplyToAddressShouldSucceed() throws Exception
    {
        when(mailServerManager.getDefaultSMTPMailServer()).thenReturn(smtpMailServer);

        final Group testGroup = new MockGroup("test group");
        when(userUtil.getUsersInGroupNames(eq(singletonList(testGroup.getName())))).thenReturn(Sets.newTreeSet(singletonList(user)));

        setValidMailSettings(false);
        sendBulkEmail.setGroups(asArray(testGroup.getName()));
        sendBulkEmail.setReplyTo("recepient@validemail.net");

        assertEquals(Action.SUCCESS, sendBulkEmail.execute());
        verify(smtpMailServer).send(any(Email.class));
    }

    @Test
    public void emailShouldBeCorrectlyAddressed() throws Exception
    {
        when(mailServerManager.getDefaultSMTPMailServer()).thenReturn(smtpMailServer);

        final Group testGroup = new MockGroup("test group");
        when(userUtil.getUsersInGroupNames(eq(singletonList(testGroup.getName())))).thenReturn(Sets.newTreeSet(singletonList(user)));

        setValidMailSettings(false);
        final String replyTo = "recepient@validemail.net";
        sendBulkEmail.setGroups(asArray(testGroup.getName()));
        sendBulkEmail.setReplyTo(replyTo);

        assertEquals(Action.SUCCESS, sendBulkEmail.execute());
        final ArgumentCaptor<Email> emailArgumentCaptor = ArgumentCaptor.forClass(Email.class);
        verify(smtpMailServer).send(emailArgumentCaptor.capture());

        final Email email = emailArgumentCaptor.getValue();
        assertEquals(user.getName(), email.getFromName());
        assertEquals(user.getEmailAddress(), email.getTo());
        assertEquals(replyTo, email.getReplyTo());
    }

    @Test
    public void emailShouldBeCorrectlyAddressedIfNoReplyToGiven() throws Exception
    {
        when(mailServerManager.getDefaultSMTPMailServer()).thenReturn(smtpMailServer);

        final Group testGroup = new MockGroup("test group");
        when(userUtil.getUsersInGroupNames(eq(singletonList(testGroup.getName())))).thenReturn(Sets.newTreeSet(singletonList(user)));

        setValidMailSettings(false);
        sendBulkEmail.setGroups(asArray(testGroup.getName()));

        assertEquals(Action.SUCCESS, sendBulkEmail.execute());
        final ArgumentCaptor<Email> emailArgumentCaptor = ArgumentCaptor.forClass(Email.class);
        verify(smtpMailServer).send(emailArgumentCaptor.capture());

        final Email email = emailArgumentCaptor.getValue();
        assertEquals(user.getName(), email.getFromName());
        assertEquals(user.getEmailAddress(), email.getTo());
        assertEquals(userEmail, email.getReplyTo());
    }

    @Test
    public void emailingManyUsersHShouldBeSplitIntoBatches() throws Exception
    {
        applicationProperties.setString(APKeys.JIRA_SENDMAIL_RECIPENT_BATCH_SIZE, "3");

        //application properties are read in constructor, so I have to construct it again...
        sendBulkEmail = new SendBulkMail(mailServerManager, permissionManager, projectRoleService, projectManager, userUtil, groupManager);

        when(mailServerManager.getDefaultSMTPMailServer()).thenReturn(smtpMailServer);

        // Setup a group
        final Group testGroup1 = new MockGroup("test group 1");
        final Group testGroup2 = new MockGroup("test group 2");
        final Group testGroup3 = new MockGroup("test group 3");

        // Setup users
        final String testEmail1 = "email1@email.com";
        final User testUser1 = new MockUser("Test User 1", "", testEmail1);

        final String testEmail2 = "email2@email.com";
        final User testUser2 = new MockUser("Test User 2", "", testEmail2);

        final String testEmail3 = "email3@email.com";
        final User testUser3 = new MockUser("Test User 3", "", testEmail3);

        final String testEmail4 = "email4@email.com";
        final User testUser4 = new MockUser("Test User 4", "", testEmail4);

        when(userUtil.getUsersInGroupNames(eq(ImmutableList.of(testGroup1.getName(), testGroup2.getName(), testGroup3.getName()))))
                .thenReturn(Sets.newTreeSet(ImmutableList.of(testUser1, testUser2, testUser3, testUser4)));

        setValidMailSettings(false);
        sendBulkEmail.setGroups(asArray(testGroup1.getName(), testGroup2.getName(), testGroup3.getName()));

        assertEquals(Action.SUCCESS, sendBulkEmail.execute());
        assertEquals("admin.errors.message.sent.successfully", sendBulkEmail.getStatus());

        final ArgumentCaptor<Email> emailArgumentCaptor = ArgumentCaptor.forClass(Email.class);
        verify(smtpMailServer, times(2)).send(emailArgumentCaptor.capture());

        final List<Email> emailList = emailArgumentCaptor.getAllValues();
        assertThat(emailList, IterableMatchers.iterableWithSize(2, Email.class));
        assertEquals(emailList.get(0).getTo(), "email1@email.com,email2@email.com,email3@email.com");
        assertEquals(emailList.get(1).getTo(), "email4@email.com");
    }

    private TemplateSource sendingEmailWithTemplate(boolean htmlOrText) throws Exception
    {
        sendEmail(htmlOrText);

        ArgumentCaptor<TemplateSource> captor = ArgumentCaptor.forClass(TemplateSource.class);
        verify(velocityTemplatingEngine, times(2)).render(captor.capture()); // 1st for subject, 2nd for body
        assertThat(captor.getAllValues(), IterableMatchers.iterableWithSize(2, TemplateSource.class));
        // return body template only
        return captor.getAllValues().get(1);
    }

    @Test
    public void sendingTextEmailWithCorrectTemplate() throws Exception
    {
        TemplateSource templateSource = sendingEmailWithTemplate(false);

        assertThat(templateSource, Matchers.instanceOf(TemplateSource.File.class));
        assertThat(((TemplateSource.File) templateSource).getPath(), is("templates/email/html/emailfromadmintext.vm"));
    }

    @Test
    public void sendingHtmlEmailWithCorrectTemplate() throws Exception
    {
        TemplateSource templateSource = sendingEmailWithTemplate(true);

        assertThat(templateSource, Matchers.instanceOf(TemplateSource.File.class));
        assertThat(((TemplateSource.File) templateSource).getPath(), is("templates/email/html/emailfromadmin.vm"));
    }

    private void sendEmail(final boolean htmlOrText) throws Exception
    {
        when(mailServerManager.getDefaultSMTPMailServer()).thenReturn(smtpMailServer);

        final Group testGroup = new MockGroup("test group");
        when(userUtil.getUsersInGroupNames(eq(singletonList(testGroup.getName())))).thenReturn(Sets.newTreeSet(singletonList(user)));

        setValidMailSettings(false);
        sendBulkEmail.setGroups(asArray(testGroup.getName()));
        sendBulkEmail.setReplyTo("recepient@validemail.net");
        sendBulkEmail.setMessageType(htmlOrText ? NotificationRecipient.MIMETYPE_HTML : NotificationRecipient.MIMETYPE_TEXT);

        sendBulkEmail.execute();
    }

    public Email sendEmailWithMimeType(boolean htmlOrText) throws Exception
    {
        sendEmail(htmlOrText);

        ArgumentCaptor<Email> captor = ArgumentCaptor.forClass(Email.class);
        verify(smtpMailServer).send(captor.capture());
        return captor.getValue();
    }

    @Test
    public void sendingTextEmailWithCorrectMimeType() throws Exception
    {
        Email email = sendEmailWithMimeType(false);
        assertThat(email.getMimeType(), is("text/plain"));
    }

    @Test
    public void sendingHtmlEmailWithCorrectMimeType() throws Exception
    {
        Email email = sendEmailWithMimeType(true);
        assertThat(email.getMimeType(), is("text/html"));
    }

    private void setValidMailSettings(final boolean setSendToRoles)
    {
        sendBulkEmail.setSendToRoles(setSendToRoles);
        sendBulkEmail.setMessageType(NotificationRecipient.MIMETYPE_HTML);
        sendBulkEmail.setSubject("Test Subject");
        sendBulkEmail.setMessage("Test Message Body");
    }

    // uses standard equals() and toString() for comparison, but ensures map keys are comparable and makes nice comparable
    // output on mismatch by resorting keys in ordered maps.
    private static <K extends Comparable<K>, V> void assertMapsEqual(final Map<K, ? extends V> expected, final Map<K, ? extends V> actual)
    {
        final SortedMap<K, V> expectedSortedMap = MapBuilder.<K, V>newBuilder().addAll(expected).toTreeMap();
        final SortedMap<K, V> actualSortedMap = MapBuilder.<K, V>newBuilder().addAll(actual).toTreeMap();
        assertEquals(expectedSortedMap, actualSortedMap);
    }

    private static <T> T[] asArray(final T... elements)
    {
        return elements;
    }

    private ProjectRoleActors makeMockProjectRoleActors(final Collection<User> users)
            throws RoleActorDoesNotExistException
    {
        final Set<User> userSet = new HashSet<User>(users);
        final Set<ProjectRoleActor> actors = new HashSet<ProjectRoleActor>();
        actors.add(new MockProjectRoleManager.MockRoleActor(null, null, null, userSet, "testType", "testParameter"));
        return new ProjectRoleActorsImpl(null, null, actors);
    }
}
