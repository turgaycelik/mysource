package com.atlassian.jira.plugin.link.remotejira;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.bc.issue.link.IssueLinkService;
import com.atlassian.jira.bc.issue.link.RemoteIssueLinkService;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.event.issue.IssueEventBundle;
import com.atlassian.jira.event.issue.IssueEventBundleFactory;
import com.atlassian.jira.event.issue.IssueEventManager;
import com.atlassian.jira.event.issue.IssueEventSource;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserHistoryManager;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.util.UserUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestLinkJiraIssue
{
    @Mock
    private IssueEventManager issueEventManager;
    @Mock
    private IssueEventBundleFactory issueEventBundleFactory;
    @Mock
    private JiraAuthenticationContext authenticationContext;
    @Mock
    private ApplicationUser applicationUser;
    @Mock
    private User user;

    private LinkJiraIssue action;

    @Before
    public void setUp()
    {
        when(authenticationContext.getUser()).thenReturn(applicationUser);
        when(authenticationContext.getLoggedInUser()).thenReturn(user);

        MockComponentWorker worker = new MockComponentWorker();
        worker.addMock(JiraAuthenticationContext.class, authenticationContext);
        worker.init();

        action = new LinkJiraIssue(
                mock(SubTaskManager.class),
                mock(FieldScreenRendererFactory.class),
                mock(FieldManager.class),
                mock(ProjectRoleManager.class),
                mock(CommentService.class),
                mock(UserHistoryManager.class),
                mock(UserPropertyManager.class),
                mock(IssueLinkService.class),
                mock(UserUtil.class),
                mock(IssueLinkTypeManager.class),
                mock(RemoteIssueLinkService.class),
                mock(EventPublisher.class),
                mock(ApplicationLinkService.class),
                mock(RemoteJiraRestService.class),
                mock(IssueManager.class),
                mock(IssueLinkManager.class),
                mock(InternalHostApplication.class),
                issueEventManager,
                issueEventBundleFactory
        );
    }
    
    @Test
    public void commentEventsAreCorrectlyDispatched()
    {
        Issue issue = mock(Issue.class);
        Comment comment = mock(Comment.class);

        IssueEventBundle issueEventBundle = mock(IssueEventBundle.class);
        when(issueEventBundleFactory.createCommentAddedBundle(issue, applicationUser, comment, expectedParams())).thenReturn(issueEventBundle);

        action.dispatchCommentEvents(issue, comment);

        verify(issueEventManager).dispatchRedundantEvent(EventType.ISSUE_COMMENTED_ID, issue, user, comment, null, null, expectedParams());
        verify(issueEventManager).dispatchEvent(issueEventBundle);
    }

    private Map<String, Object> expectedParams()
    {
        Map<String, Object> expectedParams = new HashMap<String, Object>();
        expectedParams.put("eventsource", IssueEventSource.ACTION);
        return expectedParams;
    }
}
