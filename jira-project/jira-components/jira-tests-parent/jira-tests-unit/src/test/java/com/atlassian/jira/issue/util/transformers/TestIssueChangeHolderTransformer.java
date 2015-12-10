package com.atlassian.jira.issue.util.transformers;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.issue.util.IssueUpdateBean;
import com.atlassian.jira.user.ApplicationUser;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestIssueChangeHolderTransformer
{
    @Test
    public void toIssueUpdateBeanExtractsRequiredInformationFromTheIssueChangeHolder()
    {
        List<ChangeItemBean> changes = Arrays.asList(mock(ChangeItemBean.class));
        Comment comment = mock(Comment.class);
        IssueChangeHolder changeHolder = changeHolderWith(true, changes, comment);

        ApplicationUser user = mock(ApplicationUser.class);
        Long eventType = EventType.ISSUE_ASSIGNED_ID;
        boolean sendMail = true;

        IssueUpdateBean issueUpdateBean = IssueChangeHolderTransformer.toIssueUpdateBean(changeHolder, eventType, user, sendMail);

        assertThat(issueUpdateBean.isSubtasksUpdated(), is(true));
        assertThat(issueUpdateBean.getEventTypeId(), is(eventType));
        assertThat(issueUpdateBean.getApplicationUser(), is(user));
        assertThat(issueUpdateBean.isSendMail(), is(sendMail));
        assertThat(issueUpdateBean.getChangeItems(), CoreMatchers.<Collection<ChangeItemBean>>is(changes));
        assertThat(issueUpdateBean.getComment(), is(comment));
    }

    private IssueChangeHolder changeHolderWith(boolean isSubtaskUpdated, List<ChangeItemBean> changes, Comment comment)
    {
        IssueChangeHolder changeHolder = mock(IssueChangeHolder.class);
        when(changeHolder.isSubtasksUpdated()).thenReturn(isSubtaskUpdated);
        when(changeHolder.getChangeItems()).thenReturn(changes);
        when(changeHolder.getComment()).thenReturn(comment);
        return changeHolder;
    }
}
