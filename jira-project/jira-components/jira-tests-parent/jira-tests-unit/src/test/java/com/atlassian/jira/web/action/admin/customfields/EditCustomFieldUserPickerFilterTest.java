package com.atlassian.jira.web.action.admin.customfields;

import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemService;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.mock.MockProjectRoleManager;
import com.atlassian.jira.security.groups.MockGroupManager;
import com.atlassian.jira.user.UserFilter;
import com.atlassian.jira.user.UserFilterManager;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Set;

import static com.atlassian.jira.mock.MockProjectRoleManager.PROJECT_ROLE_TYPE_1;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EditCustomFieldUserPickerFilterTest
{
    private MockGroupManager groupManager = new MockGroupManager();
    @Mock
    private ManagedConfigurationItemService managedConfigurationItemService;
    private MockProjectRoleManager projectRoleManager = new MockProjectRoleManager();
    @Mock
    private UserFilterManager userFilterManager;
    @Mock
    private WebResourceManager webResourceManager;

    private EditCustomFieldUserPickerFilter editCustomFieldUserPickerFilter;

    @Mock
    private FieldConfig fieldConfig;

    @Before
    public void setUp() throws Exception
    {
        editCustomFieldUserPickerFilter = new EditCustomFieldUserPickerFilter(groupManager, managedConfigurationItemService, projectRoleManager, userFilterManager, webResourceManager);
        groupManager.addGroup("group1");
        groupManager.addGroup("group2");
        // MockProjectRoleManager has 3 default roles
    }

    @Test
    public void testGetFilterEnabledFilter()
    {
        final UserFilter userFilter = new UserFilter(true, ImmutableSet.of(PROJECT_ROLE_TYPE_1.getId()), ImmutableSet.of("group1", "group4"));
        when(userFilterManager.getFilter(fieldConfig)).thenReturn(userFilter);
        final UserFilter actual = editCustomFieldUserPickerFilter.getFilter(fieldConfig, groupManager.getAllGroups());
        assertThat(actual.isEnabled(), is(true));
        assertThat(actual.getGroups(), equalTo((Set)ImmutableSet.<String>of("group1")));
        assertThat(actual.getRoleIds(), equalTo((Set)ImmutableSet.<Long>of(PROJECT_ROLE_TYPE_1.getId())));
    }

    @Test
    public void testGetFilterDisabledFilter()
    {
        final UserFilter userFilter = new UserFilter(false, ImmutableSet.of(PROJECT_ROLE_TYPE_1.getId()), ImmutableSet.of("group1", "group4"));
        when(userFilterManager.getFilter(fieldConfig)).thenReturn(userFilter);
        final UserFilter actual = editCustomFieldUserPickerFilter.getFilter(fieldConfig, groupManager.getAllGroups());
        assertThat(actual, sameInstance(userFilter));
    }
}
