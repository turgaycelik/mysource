package com.atlassian.jira.issue.fields;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.option.OptionSetManager;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.search.handlers.IssueTypeSearchHandlerFactory;
import com.atlassian.jira.issue.statistics.IssueTypeStatisticsMapper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.workflow.WorkflowManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestIssueTypeSystemField
{
    @Mock
    private PermissionManager permissionManager;
    @Mock
    private JiraAuthenticationContext jiraAuthenticationContext;

    IssueTypeSystemField field;

    @Before
    public void setUp()
    {
        IssueTypeSearchHandlerFactory issueTypeSearchHandlerFactory = null;

        field = new IssueTypeSystemField(
                mock(VelocityTemplatingEngine.class),
                mock(ApplicationProperties.class),
                jiraAuthenticationContext,
                mock(ConstantsManager.class),
                mock(WorkflowManager.class),
                permissionManager,
                mock(IssueTypeStatisticsMapper.class),
                mock(OptionSetManager.class),
                mock(IssueTypeSchemeManager.class),
                issueTypeSearchHandlerFactory,
                mock(JiraBaseUrls.class)
        );
    }

    @Test
    public void userHasMovePermissionChecksPermissionOverTheIssue()
    {
        Issue issue = mock(Issue.class);
        ApplicationUser user = mock(ApplicationUser.class);

        when(jiraAuthenticationContext.getUser()).thenReturn(user);
        when(permissionManager.hasPermission(Permissions.MOVE_ISSUE, issue, user)).thenReturn(true);

        boolean hasMovePermission = field.userHasMovePermission(issue);

        assertThat(hasMovePermission, is(true));
    }
}
