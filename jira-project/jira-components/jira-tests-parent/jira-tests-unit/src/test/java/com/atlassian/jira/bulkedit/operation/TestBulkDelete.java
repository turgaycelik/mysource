package com.atlassian.jira.bulkedit.operation;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bulkedit.BulkOperationManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.action.issue.bulkedit.BulkDelete;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.jira.web.bean.BulkEditBeanImpl;
import com.atlassian.jira.web.bean.BulkEditBeanSessionHelper;
import com.google.common.collect.ImmutableList;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import webwork.action.Action;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

public class TestBulkDelete
{
    @Rule
    public RuleChain mocksInContainer = MockitoMocksInContainer.forTest(this);

    @Mock
    private PermissionManager permissionManager;

    @Mock
    private ApplicationUser user;

    @Mock
    private User directoryUser;

    @Mock
    private BulkOperationManager bulkOperationManager;

    @Mock
    private ProgressAwareBulkOperation bulkDeleteOperation;

    @Mock
    private BulkEditBeanSessionHelper bulkEditBeanSessionHelper;

    @Mock
    private IssueManager issueManager;

    private BulkEditBean bulkEditBean;

    private BulkDelete bulkDelete;

    @Mock
    @AvailableInContainer
    private JiraAuthenticationContext jiraAuthenticationContext;

    @Mock
    private I18nHelper i18nHelper;

    @Before
    public void setUp() throws Exception
    {
        bulkEditBean = new BulkEditBeanImpl(issueManager);
        when(bulkEditBeanSessionHelper.getFromSession()).thenReturn(bulkEditBean);

        final List<Issue> issues = ImmutableList.<Issue>of(
                new MockIssue(100, "ABC-1"), new MockIssue(101, "ABC-2"), new MockIssue(102, "ABC-3"));
        when(issueManager.getIssueObject(100L)).thenReturn((MutableIssue) issues.get(0));
        when(issueManager.getIssueObject(101L)).thenReturn((MutableIssue) issues.get(1));
        when(issueManager.getIssueObject(102L)).thenReturn((MutableIssue) issues.get(2));
        bulkEditBean.initSelectedIssues(issues);

        when(bulkOperationManager.getProgressAwareOperation(BulkDeleteOperation.NAME_KEY))
                .thenReturn(bulkDeleteOperation);

        bulkDelete = new BulkDelete(null, bulkOperationManager, permissionManager, bulkEditBeanSessionHelper, null,
                i18nHelper);

        when(jiraAuthenticationContext.getLoggedInUser()).thenReturn(directoryUser);
        when(jiraAuthenticationContext.getUser()).thenReturn(user);
        when(jiraAuthenticationContext.getI18nHelper()).thenReturn(i18nHelper);
    }

    @Test
    public void testDoPerformValidationCannotPerform()
    {
        // given:
        when(permissionManager.hasPermission(Permissions.BULK_CHANGE, directoryUser)).thenReturn(true);
        when(permissionManager.hasPermission(Permissions.BULK_CHANGE, user)).thenReturn(true);
        when(bulkDeleteOperation.canPerform(bulkEditBean, user)).thenReturn(false);
        when(i18nHelper.getText("bulk.delete.cannotperform.error", "3")).thenReturn("Test successful");

        // when:
        bulkDelete.doPerformValidation();

        // then:
        assertThat(bulkDelete.getErrorMessages(), contains("Test successful"));
    }

    @Test
    public void testDoPerformValidationException()
    {
        // given:
        when(permissionManager.hasPermission(Permissions.BULK_CHANGE, directoryUser)).thenReturn(true);
        when(permissionManager.hasPermission(Permissions.BULK_CHANGE, user)).thenReturn(true);
        when(bulkDeleteOperation.canPerform(bulkEditBean, user)).thenThrow(
                new RuntimeException());
        when(i18nHelper.getText("bulk.canperform.error")).thenReturn("Test successful");

        // when:
        bulkDelete.doPerformValidation();

        // then
        assertThat(bulkDelete.getErrorMessages(), contains("Test successful"));
    }

    @Test
    public void testDoPerformValidation()
    {
        // given:
        when(permissionManager.hasPermission(Permissions.BULK_CHANGE, directoryUser)).thenReturn(true);
        when(permissionManager.hasPermission(Permissions.BULK_CHANGE, user)).thenReturn(true);
        when(bulkDeleteOperation.canPerform(bulkEditBean, user)).thenReturn(true);

        // when:
        bulkDelete.doPerformValidation();

        // then
        assertThat(bulkDelete.getErrorMessages(),
                Matchers.<Collection<String>>equalTo(Collections.<String>emptyList()));
        assertThat(bulkDelete.getErrors(),
                Matchers.<Map<String, String>>equalTo(Collections.<String, String>emptyMap()));
    }

    @Test
    public void testDoDetailsValidation() throws Exception
    {
        final String result = bulkDelete.doDetailsValidation();
        assertEquals(Action.INPUT, result);
        assertEquals(4, bulkEditBean.getCurrentStep());
        assertTrue("It should be possible to return to step 2.", bulkEditBean.isAvailablePreviousStep(2));
    }

    @Test
    public void doPerformShouldReturnErrorWhenDeleteOperationNotPermitted() throws Exception
    {
        // given:
        when(permissionManager.hasPermission(Permissions.BULK_CHANGE, user)).thenReturn(true);
        when(bulkDeleteOperation.canPerform(bulkEditBean, user)).thenReturn(false);

        // when
        final String result = bulkDelete.doPerform();

        // then:
        assertEquals(Action.ERROR, result);
    }

    @Test
    public void doPerformShouldReturnErrorWhenBulkOperationsNotPermitted() throws Exception
    {
        // given:
        when(permissionManager.hasPermission(Permissions.BULK_CHANGE, user)).thenReturn(false);
        when(bulkDeleteOperation.canPerform(bulkEditBean, user)).thenReturn(true);

        // when
        final String result = bulkDelete.doPerform();

        // then:
        assertEquals(Action.ERROR, result);

    }

    @Test
    public void testDoPerformException() throws Exception
    {
        // given:
        when(permissionManager.hasPermission(Permissions.BULK_CHANGE, directoryUser)).thenReturn(true);
        when(permissionManager.hasPermission(Permissions.BULK_CHANGE, user)).thenReturn(true);
        when(bulkDeleteOperation.canPerform(bulkEditBean, user)).thenReturn(true);
        doThrow(new RuntimeException()).when(bulkDeleteOperation).canPerform(bulkEditBean, user);
        when(i18nHelper.getText("bulk.canperform.error")).thenReturn("Test successful");

        // when
        final String result = bulkDelete.doPerform();

        // then:
        assertEquals(Action.ERROR, result);
        assertThat(bulkDelete.getErrorMessages(), contains("Test successful"));
    }
}
