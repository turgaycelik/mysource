package com.atlassian.jira.security.type;

import java.util.Map;
import java.util.Set;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.atlassian.jira.issue.security.IssueSecurityLevelImpl;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.permission.PermissionContext;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.MockUserKeyService;
import com.atlassian.jira.user.UserKeyService;

import org.apache.lucene.search.BooleanQuery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestAbstractIssueFieldSecurityType
{
    // It seems too easy to mock poor Britney :P
    private static final String USER_KEY = "OopsIDidItAgain";
    private static final String USER_NAME = "britneySpears";

    private User mockUser;

    @Before
    public void setUp()
    {
        mockUser = new MockUser(USER_NAME);
        final MockUserKeyService mockUserKeyService = new MockUserKeyService();
        mockUserKeyService.setMapping(USER_KEY, mockUser.getName());

        new MockComponentWorker()
                .addMock(UserKeyService.class, mockUserKeyService)
                .init();
    }

    @After
    public void tearDown()
    {
        mockUser = null;
    }

    //Test for JRA-27590
    @Test
    public void testQueryForProjectUsesKey()
    {
        final TestableAbstractIssueFieldSecurityType security = new TestableAbstractIssueFieldSecurityType();
        final Project mockProject = new MockProject(10000L);
        final BooleanQuery query = security.getQueryForProject(mockProject, mockUser, "assignee");
        assertNotNull(query);
        assertEquals("+projid:10000 +assignee:" + USER_KEY, query.toString());
    }

    //Test for JRA-27590
    @Test
    public void testQueryForSecurityLevelUsesKey()
    {
        final TestableAbstractIssueFieldSecurityType security = new TestableAbstractIssueFieldSecurityType();
        final IssueSecurityLevel securityLevel = new IssueSecurityLevelImpl(10500L, "", "", 12L);
        final BooleanQuery query = security.getQueryForSecurityLevel(securityLevel, mockUser, "assignee");
        assertNotNull(query);
        assertEquals("+issue_security_level:10500 +assignee:" + USER_KEY, query.toString());
    }

    static class TestableAbstractIssueFieldSecurityType extends AbstractIssueFieldSecurityType
    {

        @Override
        protected String getFieldName(String parameter)
        {
            return "assignee";
        }

        @Override
        protected boolean hasIssuePermission(User user, boolean issueCreation, GenericValue issueGv, String argument)
        {
            return false;
        }

        @Override
        protected boolean hasIssuePermission(User user, boolean issueCreation, Issue issue, String parameter)
        {
            return false;
        }

        @Override
        protected boolean hasProjectPermission(User user, boolean issueCreation, GenericValue project)
        {
            return false;
        }

        @Override
        protected boolean hasProjectPermission(User user, boolean issueCreation, Project project)
        {
            return false;
        }

        @Override
        public String getDisplayName()
        {
            return null;
        }

        @Override
        public String getType()
        {
            return null;
        }

        @Override
        public void doValidation(String key, Map<String,String> parameters, JiraServiceContext jiraServiceContext)
        {
        }

        @Override
        public Set<User> getUsers(final PermissionContext permissionContext, final String argument)
        {
            throw new UnsupportedOperationException();
        }
    }
}
