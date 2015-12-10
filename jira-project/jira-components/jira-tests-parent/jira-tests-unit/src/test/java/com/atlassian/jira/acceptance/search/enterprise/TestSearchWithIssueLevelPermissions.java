/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.acceptance.search.enterprise;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.apache.lucene.search.Query;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.parameters.lucene.DefaultPermissionQueryFactory;
import com.atlassian.jira.issue.search.parameters.lucene.PermissionQueryFactory;
import com.atlassian.jira.issue.search.parameters.lucene.PermissionsFilterGenerator;
import com.atlassian.jira.issue.search.parameters.lucene.PermissionsFilterGeneratorImpl;
import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.security.IssueSecurityLevelPermission;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.issue.security.IssueSecurityTypeManager;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.permission.PermissionTypeManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.JiraAuthenticationContextImpl;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.type.CurrentAssignee;
import com.atlassian.jira.security.type.CurrentReporter;
import com.atlassian.jira.security.type.GroupDropdown;
import com.atlassian.jira.security.type.ProjectLead;
import com.atlassian.jira.security.type.SecurityType;
import com.atlassian.jira.security.type.SingleUser;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.UserKeyService;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.AnswerWith;
import com.atlassian.jira.util.I18nHelper;

public class TestSearchWithIssueLevelPermissions
{

    @Rule
    public RuleChain mockitoMocksInContainer = MockitoMocksInContainer.forTest(this);

    @Mock
    private ProjectManager projectManager;

    @Mock
    @AvailableInContainer
    private UserKeyService userKeyService;

    @Mock
    private UserManager userManager;

    @Mock
    @AvailableInContainer
    private GroupManager groupManager;

    @Mock
    private PermissionTypeManager permissionTypeManager;
    @Mock
    private PermissionManager permissionManager;
    @Mock
    @AvailableInContainer
    private PermissionSchemeManager permissionSchemeManager;
    @Mock
    private IssueSecuritySchemeManager issueSecuritySchemeManager;
    @Mock
    private IssueSecurityTypeManager issueSecurityTypeManager;
    @Mock
    private IssueSecurityLevelManager issueSecurityLevelManager;

    @Mock
    private JiraAuthenticationContext jiraAuthenticationContext;

    @Mock
    private Project project;
    @Mock
    private IssueSecurityLevel issueSecurityLevel;
    @Mock
    private GenericValue projectPermissionSchemeGV;

    private final User user = new MockUser("username");

    private PermissionsFilterGenerator permissionsFilterGenerator;

    @Before
    public void setUp() throws Exception
    {
        JiraAuthenticationContextImpl.getRequestCache().clear();

        final PermissionQueryFactory permissionQueryFactory = new DefaultPermissionQueryFactory(issueSecurityLevelManager, permissionManager,
                permissionSchemeManager, permissionTypeManager, issueSecuritySchemeManager, issueSecurityTypeManager,
                mock(ProjectFactory.class));
        permissionsFilterGenerator = new PermissionsFilterGeneratorImpl(permissionQueryFactory);

        when(userKeyService.getKeyForUsername(user.getName())).thenReturn(user.getName());
        final ApplicationUser developerApplicationUser = ApplicationUsers.from(user);
        when(userManager.getUserByKeyEvenWhenUnknown(user.getName())).thenReturn(developerApplicationUser);

        final GenericValue projectGV = mock(GenericValue.class);
        when(project.getGenericValue()).thenReturn(projectGV);
        when(project.getId()).thenReturn(1L);
        when(permissionManager.getProjects(Matchers.anyInt(), Matchers.eq(developerApplicationUser))).thenReturn(
                Collections.singletonList(project));

        when(permissionSchemeManager.getSchemes(project.getGenericValue())).thenReturn(Collections.singletonList(projectPermissionSchemeGV));

        when(jiraAuthenticationContext.getI18nHelper()).thenReturn(mock(I18nHelper.class));
        when(jiraAuthenticationContext.getI18nHelper().getText(Matchers.anyString())).thenAnswer(AnswerWith.firstParameter());

        when(issueSecurityLevel.getId()).thenReturn(1L);

    }

    private void setupSecurityType(final SecurityType securityType, final String parameter) throws Exception
    {
        final String type = securityType.getType();

        final GenericValue security = mock(GenericValue.class);
        when(security.getString("type")).thenReturn(type);
        when(security.getString("parameter")).thenReturn(parameter);

        final IssueSecurityLevelPermission issueSecurityLevelPermission = mock(IssueSecurityLevelPermission.class);
        when(issueSecurityLevelPermission.getParameter()).thenReturn(parameter);
        when(issueSecurityLevelPermission.getType()).thenReturn(securityType.getType());
        when(issueSecuritySchemeManager.getPermissionsBySecurityLevel(issueSecurityLevel.getId())).thenReturn(
                Collections.singletonList(issueSecurityLevelPermission));

        when(issueSecurityLevelManager.getUsersSecurityLevels(Matchers.eq(project), Matchers.eq(user))).thenReturn(
                Collections.singletonList(issueSecurityLevel));
        when(issueSecurityTypeManager.getSecurityType(securityType.getType())).thenReturn(securityType);

        when(permissionTypeManager.getSecurityType(type)).thenReturn(securityType);
        when(permissionSchemeManager.getSchemes(project.getGenericValue())).thenReturn(Collections.singletonList(projectPermissionSchemeGV));
        when(permissionSchemeManager.getEntities(projectPermissionSchemeGV, (long) Permissions.BROWSE)).thenReturn(
                Collections.singletonList(security));
        when(permissionSchemeManager.getEntities(projectPermissionSchemeGV, type, (long) Permissions.BROWSE)).thenReturn(
                Collections.singletonList(security));
    }

    @Test
    public void testCurrentReporterIssueLevelPermissionReturnsTheCorrectIssues() throws Exception
    {
        setupSecurityType(new CurrentReporter(jiraAuthenticationContext), null);
        final Query query = permissionsFilterGenerator.getQuery(user);
        final StringBuilder expected = new StringBuilder().append("+(((+projid:")
                .append(project.getId())
                .append(" +issue_author:")
                .append(user.getName())
                .append("))) +(issue_security_level:-1 (+(+issue_security_level:")
                .append(issueSecurityLevel.getId())
                .append(" +issue_author:")
                .append(user.getName())
                .append(")))");
        assertEquals(expected.toString(), query.toString());
    }

    @Test
    public void testGroupIssueLevelPermissionReturnsTheCorrectIssues() throws Exception
    {
        final String groupname = "groupname";
        setupSecurityType(new GroupDropdown(jiraAuthenticationContext), groupname);
        when(groupManager.isUserInGroup(user.getName(), groupname)).thenReturn(Boolean.TRUE);

        Query query;

        // current user is a member of group
        query = permissionsFilterGenerator.getQuery(user);
        final StringBuilder expected = new StringBuilder().append("+(projid:")
                .append(project.getId())
                .append(") +(issue_security_level:-1 issue_security_level:")
                .append(issueSecurityLevel.getId())
                .append(")");
        assertEquals(expected.toString(), query.toString());

        // current user is not a member of group
        JiraAuthenticationContextImpl.getRequestCache().clear();
        when(groupManager.isUserInGroup(user.getName(), groupname)).thenReturn(Boolean.FALSE);
        query = permissionsFilterGenerator.getQuery(user);
        assertEquals("", query.toString());
    }

    @Test
    public void testSingleUserIssueLevelPermissionReturnsTheCorrectIssues() throws Exception
    {
        setupSecurityType(new SingleUser(jiraAuthenticationContext, userManager), user.getName());

        Query query;

        // single user who match current user
        query = permissionsFilterGenerator.getQuery(user);
        final StringBuilder expected = new StringBuilder().append("+(projid:")
                .append(project.getId())
                .append(") +(issue_security_level:-1 issue_security_level:")
                .append(issueSecurityLevel.getId())
                .append(")");
        assertEquals(expected.toString(), query.toString());

        // single user who does not match current user
        JiraAuthenticationContextImpl.getRequestCache().clear();
        setupSecurityType(new SingleUser(jiraAuthenticationContext, userManager), "unexisting_developer");
        query = permissionsFilterGenerator.getQuery(user);
        assertEquals("", query.toString());
    }

    @Test
    public void testProjectLeadLevelPermissionReturnsTheCorrectIssues() throws Exception
    {
        setupSecurityType(new ProjectLead(jiraAuthenticationContext), null);
        final String developerName = user.getName();
        when(project.getLeadUserKey()).thenReturn(developerName);

        Query query;

        // current user is project lead
        query = permissionsFilterGenerator.getQuery(user);
        final StringBuilder expected = new StringBuilder().append("+(projid:")
                .append(project.getId())
                .append(") +(issue_security_level:-1 ")
                .append("(+projid:")
                .append(project.getId())
                .append(" +issue_security_level:")
                .append(issueSecurityLevel.getId())
                .append(")")
                .append(")");
        assertEquals(expected.toString(), query.toString());

        // current user is not project lead
        JiraAuthenticationContextImpl.getRequestCache().clear();
        when(project.getLeadUserKey()).thenReturn("unexisting_user");
        query = permissionsFilterGenerator.getQuery(user);
        assertEquals("", query.toString());
    }

    @Test
    public void testCurrentAssigneeLevelPermissionReturnsTheCorrectIssues() throws Exception
    {
        setupSecurityType(new CurrentAssignee(jiraAuthenticationContext), null);

        final Query query = permissionsFilterGenerator.getQuery(user);
        final StringBuilder expected = new StringBuilder().append("+(((+projid:")
                .append(project.getId())
                .append(" +issue_assignee:")
                .append(user.getName())
                .append("))) +(issue_security_level:-1 (+(+issue_security_level:")
                .append(issueSecurityLevel.getId())
                .append(" +issue_assignee:")
                .append(user.getName())
                .append(")))");
        assertEquals(expected.toString(), query.toString());
    }

}
