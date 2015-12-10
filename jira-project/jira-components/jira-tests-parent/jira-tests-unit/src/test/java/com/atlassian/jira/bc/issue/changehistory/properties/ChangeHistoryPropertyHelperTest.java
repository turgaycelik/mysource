package com.atlassian.jira.bc.issue.changehistory.properties;

import com.atlassian.jira.bc.issue.properties.IssuePropertyHelper;
import com.atlassian.jira.entity.property.EntityPropertyHelper;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.changehistory.ChangeHistory;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class ChangeHistoryPropertyHelperTest
{
    private ChangeHistoryPropertyHelper changeHistoryPropertyHelper;

    @Mock
    private IssueManager issueManager;

    @Mock
    private ChangeHistoryManager changeHistoryManager;

    @Mock
    private I18nHelper i18nHelper;

    @Mock
    private IssuePropertyHelper issuePropertyHelper;

    private final ApplicationUser user = new MockApplicationUser("user");
    private final MutableIssue issue = new MockIssue();
    private final ChangeHistory history = mock(ChangeHistory.class);

    @Test
    public void testHasEditPermission() throws Exception
    {
        // having
        final ErrorCollection expected = new SimpleErrorCollection();
        when(issuePropertyHelper.hasEditPermissionFunction()).thenReturn(checkPermissionFunctionStub(expected));
        changeHistoryPropertyHelper = new ChangeHistoryPropertyHelper(issueManager, issuePropertyHelper, i18nHelper, changeHistoryManager);
        when(issueManager.getIssueObject(anyLong())).thenReturn(issue);

        // when
        final ErrorCollection result = changeHistoryPropertyHelper.hasEditPermissionFunction().apply(user, history);

        // then
        assertThat(result, sameInstance(expected));
    }

    @Test
    public void testHasReadPermission() throws Exception
    {
        // having
        final ErrorCollection expected = new SimpleErrorCollection();
        when(issuePropertyHelper.hasReadPermissionFunction()).thenReturn(checkPermissionFunctionStub(expected));
        changeHistoryPropertyHelper = new ChangeHistoryPropertyHelper(issueManager, issuePropertyHelper, i18nHelper, changeHistoryManager);
        when(issueManager.getIssueObject(anyLong())).thenReturn(issue);

        // when
        final ErrorCollection result = changeHistoryPropertyHelper.hasReadPermissionFunction().apply(user, history);

        // then
        assertThat(result, sameInstance(expected));
    }

    private EntityPropertyHelper.CheckPermissionFunction<Issue> checkPermissionFunctionStub(final ErrorCollection expectedResult)
    {
        return new EntityPropertyHelper.CheckPermissionFunction<Issue>()
        {
            @Override
            public ErrorCollection apply(final ApplicationUser user, final Issue issue)
            {
                assertThat(user, sameInstance(ChangeHistoryPropertyHelperTest.this.user));
                assertThat(issue, sameInstance((Issue) ChangeHistoryPropertyHelperTest.this.issue));
                return expectedResult;
            }
        };
    }

}
