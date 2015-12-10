package com.atlassian.jira.issue.customfields.searchers.renderer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.user.search.UserSearchParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.UserFieldSearchConstants;
import com.atlassian.jira.issue.search.searchers.util.UserSearcherHelper;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockComponentContainer;
import com.atlassian.jira.mock.issue.search.MockSearchContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.UserFilter;
import com.atlassian.jira.user.UserFilterManager;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestUserPickerGroupCustomFieldRenderer
{
    @Rule
    public MockComponentContainer mockComponentContainer = new MockComponentContainer(this);
    @Mock
    @AvailableInContainer
    private UserSearcherHelper userSearcherHelper;
    @Mock
    private CustomField field;
    @Mock
    private UserFieldSearchConstants searchConstants;
    @Mock
    private UserFilterManager userFilterManager;
    @Mock
    private PermissionManager permissionManager;

    private UserPickerGroupCustomFieldRenderer renderer;

    @Before
    public void setUp() throws Exception
    {
        when(searchConstants.getSearcherId()).thenReturn("searcherId");
        renderer = new UserPickerGroupCustomFieldRenderer(field, searchConstants, null, null, null, null, null, null, null, permissionManager, userFilterManager);
    }

    @Test
    public void addUserGroupSuggestionParamsCallsUserSearcherHelperWithSearchParams() throws Exception
    {
        FieldConfig fieldConfig = mock(FieldConfig.class);
        when(field.getReleventConfig(any(SearchContext.class))).thenReturn(fieldConfig);
        UserFilter filter = UserFilter.DISABLED;
        when(userFilterManager.getFilter(fieldConfig)).thenReturn(filter);

        final ImmutableList<String> selectedUsers = ImmutableList.of();
        final SearchContext searchContext = new MockSearchContext();
        renderer.addUserGroupSuggestionParams(null, null, searchContext, selectedUsers);
        UserSearchParams userSearchParams = UserSearchParams.builder().allowEmptyQuery(true).filter(filter).filterByProjectIds(searchContext.getProjectIds()).build();
        verify(userSearcherHelper, times(1)).addUserGroupSuggestionParams(any(User.class), eq(selectedUsers), eq(userSearchParams), eq(Maps.<String, Object>newHashMap()));
    }

    @Test
    public void addUserGroupSuggestionParamsCallsUserSearcherHelperWithNullSearchParams() throws Exception
    {
        when(field.getReleventConfig(any(SearchContext.class))).thenReturn(null);

        renderer.addUserGroupSuggestionParams(null, null, new MockSearchContext(), ImmutableList.<String>of());
        verify(userSearcherHelper, times(1)).addUserGroupSuggestionParams(any(User.class), anyListOf(String.class), isNull(UserSearchParams.class), eq(Maps.<String, Object>newHashMap()));
    }
}
