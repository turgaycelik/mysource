package com.atlassian.jira.bc.issue.properties;

import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.util.ErrorCollection.Reason.FORBIDDEN;
import static com.atlassian.jira.util.ErrorCollection.Reason.NOT_LOGGED_IN;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v6.2
 */
@RunWith (MockitoJUnitRunner.class)
public class TestIssuePropertyHelper
{
    @Mock public PermissionManager permissionManager;
    @Mock public IssueManager issueManager;
    private final I18nHelper i18n = new MockI18nHelper();

    private IssuePropertyHelper propertyHelper;

    @Before
    public void setUp()
    {
        this.propertyHelper = new IssuePropertyHelper(i18n, issueManager, permissionManager);
    }

    @Test
    public void getEntityByKey() throws Exception
    {
        MutableIssue issue = mock(MutableIssue.class);
        when(issueManager.getIssueObject(eq("HSP-1"))).thenReturn(issue);

        assertThat(propertyHelper.getEntityByKeyFunction().apply("HSP-1").isDefined(), is(true));
        assertThat(propertyHelper.getEntityByKeyFunction().apply("HSP-2").isEmpty(), is(true));
    }

    @Test
    public void getEntityById()
    {
        MutableIssue issue = mock(MutableIssue.class);
        when(issueManager.getIssueObject(1l)).thenReturn(issue);

        assertThat(propertyHelper.getEntityByIdFunction().apply(1l).isDefined(), is(true));
        assertThat(propertyHelper.getEntityByIdFunction().apply(2l).isDefined(), is(false));
    }

    @Test
    public void hasEditPermission() throws Exception
    {
        MutableIssue issue = mock(MutableIssue.class);
        ApplicationUser user = mock(ApplicationUser.class);
        when(permissionManager.hasPermission(eq(Permissions.EDIT_ISSUE), eq(issue), eq(user))).thenReturn(true);

        assertThat(propertyHelper.hasEditPermissionFunction().apply(user, issue).hasAnyErrors(), is(false));
    }

    @Test
    public void noEditPermission() throws Exception
    {
        MutableIssue issue = mock(MutableIssue.class);
        ApplicationUser user = mock(ApplicationUser.class);
        when(permissionManager.hasPermission(eq(Permissions.EDIT_ISSUE), eq(issue), eq(user))).thenReturn(false);

        final ErrorCollection errorCollection = propertyHelper.hasEditPermissionFunction().apply(user, issue);
        assertHasError(errorCollection, "editissue.error.no.edit.permission", FORBIDDEN);
    }

    @Test
    public void noEditPermissionForNotLoggedInUser()
    {
        MutableIssue issue = mock(MutableIssue.class);
        when(permissionManager.hasPermission(eq(Permissions.EDIT_ISSUE), eq(issue), any(ApplicationUser.class))).thenReturn(false);

        final ErrorCollection errorCollection = propertyHelper.hasEditPermissionFunction().apply(null, issue);
        assertHasError(errorCollection, "editissue.error.no.edit.permission", NOT_LOGGED_IN);
    }

    @Test
    public void noReadPermission()
    {
        MutableIssue issue = mock(MutableIssue.class);
        ApplicationUser user = mock(ApplicationUser.class);
        when(permissionManager.hasPermission(eq(Permissions.BROWSE), eq(issue), eq(user))).thenReturn(false);

        final ErrorCollection errorCollection = propertyHelper.hasReadPermissionFunction().apply(user, issue);
        assertHasError(errorCollection, "admin.errors.issues.no.browse.permission", FORBIDDEN);
    }

    @Test
    public void noReadPermissionForNotLoggedInUser()
    {
        MutableIssue issue = mock(MutableIssue.class);
        when(permissionManager.hasPermission(eq(Permissions.BROWSE), eq(issue), any(ApplicationUser.class))).thenReturn(false);

        final ErrorCollection errorCollection = propertyHelper.hasReadPermissionFunction().apply(null, issue);
        assertHasError(errorCollection, "admin.errors.issues.no.browse.permission", NOT_LOGGED_IN);
    }

    private static void assertHasError(ErrorCollection errorCollection, String errorMsg, ErrorCollection.Reason reason)
    {
        assertThat(errorCollection.hasAnyErrors(), is(true));
        assertThat(errorCollection.getErrorMessages(), hasItem(errorMsg));
        assertThat(errorCollection.getReasons(), hasItem(reason));
    }
}
