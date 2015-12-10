package com.atlassian.jira.web.action.issue;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.event.issue.IssueEventBundle;
import com.atlassian.jira.event.issue.IssueEventBundleFactory;
import com.atlassian.jira.event.issue.IssueEventManager;
import com.atlassian.jira.event.issue.IssueEventSource;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.exception.IssueNotFoundException;
import com.atlassian.jira.exception.IssuePermissionException;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
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
public class TestAddComment
{
    @Mock
    private IssueEventManager issueEventManager;
    @Mock
    private IssueEventBundleFactory issueEventBundleFactory;

    private AddCommentStub action;

    @Before
    public void setUp()
    {
        new MockComponentWorker().init();

        action = new AddCommentStub(issueEventManager, issueEventBundleFactory);
    }

    @Test
    public void alertSystemOfCommentDispatchesEventsAsExpected() throws Exception
    {
        User user = mock(User.class);
        ApplicationUser applicationUser = mock(ApplicationUser.class);
        MutableIssue issue = mock(MutableIssue.class);
        Comment comment = mock(Comment.class);

        action.setUser(user);
        action.setIssue(issue);
        action.setApplicationUser(applicationUser);

        IssueEventBundle issueEventBundle = mock(IssueEventBundle.class);
        when(issueEventBundleFactory.createCommentAddedBundle(issue, applicationUser, comment, eventParams())).thenReturn(issueEventBundle);

        action.alertSystemOfComment(comment);

        verify(issueEventManager).dispatchRedundantEvent(EventType.ISSUE_COMMENTED_ID, issue, user, comment, null, null, eventParams());
        verify(issueEventManager).dispatchEvent(issueEventBundle);
    }

    private Map<String, Object> eventParams()
    {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("eventsource", IssueEventSource.ACTION);
        return params;
    }

    private static class AddCommentStub extends AddComment
    {
        private MutableIssue issue;
        private ApplicationUser applicationUser;
        private User user;

        public AddCommentStub(final IssueEventManager issueEventManager, final IssueEventBundleFactory issueEventBundleFactory)
        {
            super(
                    mock(SubTaskManager.class),
                    mock(FieldManager.class),
                    mock(FieldScreenRendererFactory.class),
                    mock(ProjectRoleManager.class),
                    mock(CommentService.class),
                    mock(PermissionManager.class),
                    mock(UserUtil.class),
                    issueEventManager,
                    issueEventBundleFactory
            );
        }

        public void setIssue(MutableIssue issue)
        {
            this.issue = issue;
        }

        public void setApplicationUser(ApplicationUser applicationUser)
        {
            this.applicationUser = applicationUser;
        }

        public void setUser(User user)
        {
            this.user = user;
        }

        @Override
        @Nonnull
        public MutableIssue getIssueObject() throws IssueNotFoundException, IssuePermissionException
        {
            return issue;
        }

        @Override
        public User getLoggedInUser()
        {
            return user;
        }

        @Override
        public ApplicationUser getLoggedInApplicationUser()
        {
            return applicationUser;
        }
    }
}
