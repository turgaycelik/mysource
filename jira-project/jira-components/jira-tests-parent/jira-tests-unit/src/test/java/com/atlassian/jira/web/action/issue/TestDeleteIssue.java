package com.atlassian.jira.web.action.issue;

import java.util.List;

import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.link.IssueLink;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockHttp;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.UserIssueHistoryManager;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Answers;
import org.mockito.Mock;

import junit.framework.Assert;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestDeleteIssue
{
    @Rule
    public final TestRule initMocks = MockitoMocksInContainer.forTest(this);
    @Rule
    public final MockHttp.DefaultMocks mockHttp = MockHttp.withDefaultMocks();

    @Mock
    private SubTaskManager mockSubTaskManager;
    @Mock
    private List<IssueLink> mockList;

    @Mock
    @AvailableInContainer
    private PermissionManager mockPermissionManager;
    @Mock
    @AvailableInContainer
    private UserIssueHistoryManager UserIssueHistoryManager;
    @Mock
    @AvailableInContainer
    private IssueManager issueManager;
    @Mock(answer = Answers.RETURNS_MOCKS)
    @AvailableInContainer
    private JiraAuthenticationContext jiraAuthenticationContext;

    private final Issue issue = new MockIssue(634l);
    private final ApplicationUser mockUser = new MockApplicationUser("bob");

    @Test
    public void testGetNumberOfSubTasks()
    {
        when(jiraAuthenticationContext.getUser()).thenReturn(mockUser);
        when(mockPermissionManager.hasPermission(Permissions.BROWSE, issue, mockUser)).thenReturn(Boolean.TRUE);
        mockHttp.mockRequest().setAttribute(AbstractIssueSelectAction.PREPOPULATED_ISSUE_OBJECT,issue);

        final int expectedNumberOfSubTasks = 6;

        when(mockList.size()).thenReturn(expectedNumberOfSubTasks);
        when(mockSubTaskManager.getSubTaskIssueLinks(issue.getId())).thenReturn(mockList);

        final DeleteIssue di = new DeleteIssue(mockSubTaskManager, null);
        di.setId(issue.getId());

        final int result = di.getNumberOfSubTasks();
        Assert.assertEquals(expectedNumberOfSubTasks, result);

        verify(mockList).size();
        verify(mockSubTaskManager).getSubTaskIssueLinks(issue.getId());
        verify(mockPermissionManager, times(2)).hasPermission(Permissions.BROWSE, issue, mockUser);

    }

}
