package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.exception.IssueNotFoundException;
import com.atlassian.jira.exception.IssuePermissionException;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockHttp;
import com.atlassian.jira.junit.rules.MockitoContainer;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserIssueHistoryManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @since v6.1
 */
public class TestAbstractIssueSelectAction
{

    @Rule
    public MockHttp.DefaultMocks mockHttp = MockHttp.withDefaultMocks();
    @Rule
    public MockitoContainer mockitoContainer = new MockitoContainer(this);

    private AbstractIssueSelectActionImpl abstractIssueSelectAction;

    @Mock
    private SubTaskManager subTaskManager;

    private MockIssue issue;

    @Mock
    @AvailableInContainer
    private JiraAuthenticationContext jiraAuthenticationContext;

    @AvailableInContainer (instantiateMe = true)
    private MockI18nHelper i18nHelper;

    @Mock
    @AvailableInContainer
    private PermissionManager permissionManager;

    @Mock
    @AvailableInContainer
    private UserIssueHistoryManager userHistoryManager;

    @Mock
    private ApplicationUser currentUser;


    @Before
    public void setUp()
    {
        issue = new MockIssue(123, "KEY-111");
        when(jiraAuthenticationContext.getI18nHelper()).thenReturn(i18nHelper);
        when(jiraAuthenticationContext.getUser()).thenReturn(currentUser);
        mockHttp.mockRequest().setAttribute(AbstractIssueSelectAction.PREPOPULATED_ISSUE_OBJECT, issue);

        when(permissionManager.hasPermission(Permissions.BROWSE, issue, currentUser)).thenReturn(true);

        abstractIssueSelectAction = new AbstractIssueSelectActionImpl(subTaskManager);
        abstractIssueSelectAction.setKey("KEY-111");
    }

    @Test
    public void testIsAndAssertIssueValidNoPermission()
    {
        when(permissionManager.hasPermission(Permissions.BROWSE, issue, currentUser)).thenReturn(false);
        Assert.assertFalse("User has not view permission", abstractIssueSelectAction.isIssueValid());
        try
        {
            abstractIssueSelectAction.assertIssueIsValid();
            fail("IssuePermissionException should be here");
        }
        catch (IssuePermissionException e)
        {

        }
        assertThat(abstractIssueSelectAction.getErrorMessages(), contains("admin.errors.issues.no.permission.to.see"));

        when(permissionManager.hasPermission(Permissions.BROWSE, issue, currentUser)).thenReturn(true);
        Assert.assertTrue("User has view permission", abstractIssueSelectAction.isIssueValid());
    }

    @Test
    public void testIsAndAssertIssueValidNoIssue()
    {
        abstractIssueSelectAction = new AbstractIssueSelectActionImpl(subTaskManager);
        Assert.assertFalse("'clear' action, has neither key nor issue ID defined", abstractIssueSelectAction.isIssueValid());
        try
        {
            abstractIssueSelectAction.assertIssueIsValid();
            fail("IssueNotFoundException should be here");
        }
        catch (IssueNotFoundException e)
        {

        }
        assertThat(abstractIssueSelectAction.getErrorMessages(), contains("issue.wasdeleted"));
    }

    @Test
    public void testIsSubtask()
    {
        when(subTaskManager.isSubTask(issue.getGenericValue())).thenReturn(true);
        assertTrue(abstractIssueSelectAction.isSubTask());

        when(subTaskManager.isSubTask(issue.getGenericValue())).thenReturn(false);
        assertFalse(abstractIssueSelectAction.isSubTask());
    }

    @Test
    public void shouldAddIssueToTheHistoryOnGet()
    {
        abstractIssueSelectAction.getIssueObject();
        verify(userHistoryManager).addIssueToHistory(currentUser.getDirectoryUser(), issue);
    }

    private class AbstractIssueSelectActionImpl extends AbstractIssueSelectAction
    {
        private AbstractIssueSelectActionImpl(final SubTaskManager subTaskManager)
        {
            super(subTaskManager);
        }
    }
}
