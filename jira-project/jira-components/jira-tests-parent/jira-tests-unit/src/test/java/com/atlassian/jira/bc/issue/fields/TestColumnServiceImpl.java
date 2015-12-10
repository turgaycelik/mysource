package com.atlassian.jira.bc.issue.fields;


import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceResult;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.MockFieldManager;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayout;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutManager;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutStorageException;
import com.atlassian.jira.issue.fields.layout.column.EditableDefaultColumnLayout;
import com.atlassian.jira.issue.fields.layout.column.EditableSearchRequestColumnLayout;
import com.atlassian.jira.issue.fields.layout.column.EditableUserColumnLayout;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.I18nHelper;

import com.google.common.collect.Lists;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link ColumnServiceImpl}
 *
 * @since v6.1
 */
@RunWith (MockitoJUnitRunner.class)
public class TestColumnServiceImpl
{
    @Mock
    ColumnLayoutManager columnLayoutManager;
    @Mock
    GlobalPermissionManager globalPermissionManager;
    @Mock
    SearchRequestService searchRequestService;
    @Mock
    I18nHelper.BeanFactory beanFactory;
    FieldManager fieldManager;

    MockApplicationUser filteruser = new MockApplicationUser("filteruser");
    private MockApplicationUser admin = new MockApplicationUser("adminuser");
    private ColumnServiceImpl columnService = null;

    @Before
    public void setup()
    {
        fieldManager = new MockFieldManager();
        columnService = new ColumnServiceImpl(beanFactory, columnLayoutManager,
                fieldManager, globalPermissionManager, searchRequestService);
    }

    @Test
    public void userColumnLayout() throws Exception
    {
        ColumnLayout expected = mock(ColumnLayout.class);
        when(globalPermissionManager.hasPermission(anyInt(), any(ApplicationUser.class))).thenReturn(true);
        when(columnLayoutManager.getColumnLayout(argThat(new UserArgumentMatcher(filteruser.getUsername())))).thenReturn(expected);

        final ServiceOutcome<ColumnLayout> outcome = columnService.getColumnLayout(admin, filteruser);

        Assert.assertEquals(expected, outcome.getReturnedValue());
        Assert.assertTrue(outcome.isValid());
    }

    @Test
    public void filterColumnLayout() throws Exception
    {
        ColumnLayout expected = mock(ColumnLayout.class);
        when(globalPermissionManager.hasPermission(anyInt(), eq(admin))).thenReturn(true);
        when(searchRequestService.getFilter(any(JiraServiceContext.class), anyLong())).thenReturn(mock(SearchRequest.class));
        when(columnLayoutManager.hasColumnLayout(any(SearchRequest.class))).thenReturn(true);
        when(columnLayoutManager.getColumnLayout(argThat(new UserArgumentMatcher(admin.getUsername())), any(SearchRequest.class))).thenReturn(expected);

        final ServiceOutcome<ColumnLayout> outcome = columnService.getColumnLayout(admin, 123L);

        Assert.assertEquals(expected, outcome.getReturnedValue());
        Assert.assertTrue(outcome.isValid());
    }

    @Test
    public void noFilterColumnLayout() throws Exception
    {
        when(globalPermissionManager.hasPermission(anyInt(), any(ApplicationUser.class))).thenReturn(true);
        when(searchRequestService.getFilter(any(JiraServiceContext.class), anyLong())).thenReturn(mock(SearchRequest.class));
        when(columnLayoutManager.hasColumnLayout(any(SearchRequest.class))).thenReturn(false);

        final ServiceOutcome<ColumnLayout> outcome = columnService.getColumnLayout(admin, 123L);

        Assert.assertNull(outcome.getReturnedValue());
        Assert.assertTrue(outcome.isValid());
    }

    @Test
    public void userColumnLayoutNoPermission() {
        final MockApplicationUser nonadmin = new MockApplicationUser("nonadmin");
        when(globalPermissionManager.hasPermission(anyInt(), eq(nonadmin))).thenReturn(false);
        I18nHelper helper = mock(I18nHelper.class);
        when(helper.getText(any(String.class))).thenReturn("talk to the hand");
        when(beanFactory.getInstance(any(ApplicationUser.class))).thenReturn(helper);

        final ServiceOutcome<ColumnLayout> outcome = columnService.getColumnLayout(nonadmin, filteruser);

        Assert.assertFalse(outcome.isValid());
        final Collection<String> errorMessages = outcome.getErrorCollection().getErrorMessages();
        Assert.assertEquals(Arrays.asList("talk to the hand"), errorMessages);
    }

    @Test
    public void usersOwnColumnLayoutNotAdmin() throws Exception {
        final MockApplicationUser nonadmin = new MockApplicationUser("nonadmin");
        when(globalPermissionManager.hasPermission(eq(Permissions.ADMINISTER), eq(nonadmin))).thenReturn(false);
        ColumnLayout expected = mock(ColumnLayout.class);
        when(globalPermissionManager.hasPermission(anyInt(), any(ApplicationUser.class))).thenReturn(true);
        when(columnLayoutManager.getColumnLayout(argThat(new UserArgumentMatcher(nonadmin.getUsername())))).thenReturn(expected);

        final ServiceOutcome<ColumnLayout> outcome = columnService.getColumnLayout(nonadmin, nonadmin);

        Assert.assertEquals(expected, outcome.getReturnedValue());
        Assert.assertTrue(outcome.isValid());
    }

    @Test
    public void exceptionThing() throws Exception {
        when(globalPermissionManager.hasPermission(anyInt(), any(ApplicationUser.class))).thenReturn(true);
        when(columnLayoutManager.getColumnLayout(any(User.class)))
                .thenThrow(new ColumnLayoutStorageException("What the hell this won't happen"));
        I18nHelper helper = mock(I18nHelper.class);
        when(helper.getText(any(String.class), any(String.class))).thenReturn("yeah nah");
        when(beanFactory.getInstance(any(ApplicationUser.class))).thenReturn(helper);

        final ServiceOutcome<ColumnLayout> outcome = columnService.getColumnLayout(admin, filteruser);

        Assert.assertFalse(outcome.isValid());
    }

    @Test
    public void testSetColumnsForUser() throws Exception
    {
        List<String> columns = Lists.newArrayList("Pokemon", "Digimon", "WUT?");
        List<NavigableField> fields = Lists.newArrayList();
        EditableUserColumnLayout expected = mock(EditableUserColumnLayout.class);
        for (String column : columns)
        {
            fields.add(fieldManager.getNavigableField(column));
        }

        when(globalPermissionManager.hasPermission(anyInt(), any(ApplicationUser.class))).thenReturn(true);
        when(columnLayoutManager.getEditableUserColumnLayout(argThat(new UserArgumentMatcher(filteruser.getUsername())))).thenReturn(expected);

        ServiceResult outcome = columnService.setColumns(admin, filteruser, columns);

        verify(expected).setColumns(fields);
        verify(columnLayoutManager).storeEditableUserColumnLayout(expected);
        Assert.assertTrue(outcome.isValid());
    }

    @Test
    public void testSetColumnsForFilter() throws Exception
    {
        List<String> columns = Lists.newArrayList("Pokemon", "Digimon", "WUT?");
        List<NavigableField> fields = Lists.newArrayList();
        EditableSearchRequestColumnLayout expected = mock(EditableSearchRequestColumnLayout.class);
        for (String column : columns)
        {
            fields.add(fieldManager.getNavigableField(column));
        }
        SearchRequest searchRequest = mock(SearchRequest.class);
        when(searchRequest.getOwner()).thenReturn(admin); // the search request must be owned by the service caller
        when(globalPermissionManager.hasPermission(anyInt(), eq(admin))).thenReturn(true);
        when(searchRequestService.getFilter(any(JiraServiceContext.class), anyLong())).thenReturn(searchRequest);
        when(columnLayoutManager.hasColumnLayout(any(SearchRequest.class))).thenReturn(true);
        when(columnLayoutManager.getEditableSearchRequestColumnLayout(argThat(new UserArgumentMatcher(admin.getUsername())), any(SearchRequest.class))).thenReturn(expected);

        ServiceResult outcome = columnService.setColumns(admin, 123L, columns);

        verify(expected).setColumns(fields);
        verify(columnLayoutManager).storeEditableSearchRequestColumnLayout(expected);
        Assert.assertTrue(outcome.isValid());
    }

    @Test
    public void testGetDefaultColumnLayout() throws Exception
    {
        EditableDefaultColumnLayout expected = mock(EditableDefaultColumnLayout.class);
        when(globalPermissionManager.hasPermission(anyInt(), any(ApplicationUser.class))).thenReturn(true);
        when(columnLayoutManager.getEditableDefaultColumnLayout()).thenReturn(expected);

        final ServiceOutcome<ColumnLayout> outcome = columnService.getDefaultColumnLayout(admin);

        Assert.assertEquals(expected, outcome.getReturnedValue());
        Assert.assertTrue(outcome.isValid());
    }

    @Test
    public void testGetDefaultColumnLayoutNotAdmin() {
        final MockApplicationUser nonadmin = new MockApplicationUser("nonadmin");
        when(globalPermissionManager.hasPermission(eq(Permissions.ADMINISTER), eq(nonadmin))).thenReturn(false);
        I18nHelper helper = mock(I18nHelper.class);
        when(helper.getText(any(String.class))).thenReturn("talk to the hand");
        when(beanFactory.getInstance(any(ApplicationUser.class))).thenReturn(helper);

        final ServiceOutcome<ColumnLayout> outcome = columnService.getDefaultColumnLayout(nonadmin);

        Assert.assertFalse(outcome.isValid());
        final Collection<String> errorMessages = outcome.getErrorCollection().getErrorMessages();
        Assert.assertEquals(Arrays.asList("talk to the hand"), errorMessages);
    }

    @Test
    public void testSetDefaultColumns() throws Exception
    {
        List<String> columns = Lists.newArrayList("Pokemon", "Digimon", "WUT?");
        List<NavigableField> fields = Lists.newArrayList();
        EditableDefaultColumnLayout expected = mock(EditableDefaultColumnLayout.class);
        for (String column : columns)
        {
            fields.add(fieldManager.getNavigableField(column));
        }

        when(globalPermissionManager.hasPermission(eq(Permissions.ADMINISTER), any(ApplicationUser.class))).thenReturn(true);
        when(columnLayoutManager.getEditableDefaultColumnLayout()).thenReturn(expected);

        ServiceResult outcome = columnService.setDefaultColumns(admin, columns);

        verify(expected).setColumns(fields);
        verify(columnLayoutManager).storeEditableDefaultColumnLayout(expected);
        Assert.assertTrue(outcome.isValid());
    }

    @Test
    public void testSetDefaultColumnsNotAdmin() throws Exception
    {
        final MockApplicationUser nonadmin = new MockApplicationUser("nonadmin");
        when(globalPermissionManager.hasPermission(eq(Permissions.ADMINISTER), eq(nonadmin))).thenReturn(false);
        I18nHelper helper = mock(I18nHelper.class);
        when(helper.getText(any(String.class))).thenReturn("I'll be back");
        when(beanFactory.getInstance(any(ApplicationUser.class))).thenReturn(helper);

        ServiceResult outcome = columnService.setDefaultColumns(nonadmin, null);

        Assert.assertFalse(outcome.isValid());
        final Collection<String> errorMessages = outcome.getErrorCollection().getErrorMessages();
        Assert.assertEquals(Arrays.asList("I'll be back"), errorMessages);
    }

    private static class UserArgumentMatcher extends ArgumentMatcher<User>
    {
        private final String username;

        public UserArgumentMatcher(String username)
        {
            this.username = username;
        }

        @Override
        public boolean matches(Object argument)
        {
            if (User.class.isInstance(argument)) {
                final User theUser = (User) argument;
                return theUser.getName().equals(username);
            }
            return false;
        }
    }
}
