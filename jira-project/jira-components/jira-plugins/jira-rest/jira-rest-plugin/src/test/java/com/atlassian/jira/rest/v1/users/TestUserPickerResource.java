package com.atlassian.jira.rest.v1.users;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Response;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.bc.user.search.UserSearchParams;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.UserFilter;
import com.atlassian.jira.user.UserFilterManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.I18nHelper;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * @since v5.0.2
 */
@RunWith(MockitoJUnitRunner.class)
public class TestUserPickerResource
{
    public static final String SAMPLE_QUERY_USER = "User";
    private static String EMPTY_RESULT = new UserPickerResource.UserPickerResultsWrapper(null, null, null).toString();
    @Mock
    private UserPickerSearchService searchService;

    @Mock
    private UserManager userManager;

    @Mock
    private AvatarService avatarService;
    
    @Mock
    private JiraAuthenticationContext authenticationContext;

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private JiraServiceContext ctx;

    @Mock
    protected PermissionManager permissionManager;

    @Mock
    private I18nHelper i18nHelper;

    @Mock
    private UserFilterManager userFilterManager;

    @Mock
    private FieldConfigManager fieldConfigManager;

    @Mock
    private FieldConfig fieldConfig;

    @Mock
    private CustomField customField;

    @Mock
    private UserFilter filter;

    @Before
    public void setUp() throws Exception
    {
        when(i18nHelper.getText(Mockito.<String>any(), Mockito.<String>any(), Mockito.<String>any())).thenReturn("");
        when(authenticationContext.getI18nHelper()).thenReturn(i18nHelper);
        when(applicationProperties.getDefaultBackedString(APKeys.JIRA_AJAX_AUTOCOMPLETE_LIMIT)).thenReturn("20");
    }

    @Test
    public void testExcludedUsers()
    {
        final List<User> matchingUsers = new ArrayList<User>();

        for (int i = 0; i < 3; ++i)
        {
            matchingUsers.add(new MockUser(SAMPLE_QUERY_USER + String.valueOf(i), "Full Name" + String.valueOf(i), "user" + String.valueOf(i) + "@somewhere.com"));
        }

        when(searchService.findUsers(Mockito.<JiraServiceContext>any(), eq(SAMPLE_QUERY_USER), Mockito.<UserSearchParams>any())).thenReturn(matchingUsers);
        when(searchService.canPerformAjaxSearch(Mockito.<JiraServiceContext>any())).thenReturn(true);

        final UserPickerResource resource = new UserPickerResource(authenticationContext,
                i18nHelper, searchService, applicationProperties, avatarService, null, fieldConfigManager);

        final List<UserPickerResource.UserPickerUser> expectedUsers = new ArrayList<UserPickerResource.UserPickerUser>();
        expectedUsers.add(new UserPickerResource.UserPickerUser("User2", "Full Name2", "<div  id=\"blah_i_User2\" class=\"yad\" >Full Name2&nbsp;(<b>User</b>2)</div>", null));

        final UserPickerResource.UserPickerResultsWrapper expectedResult = new UserPickerResource.UserPickerResultsWrapper(expectedUsers, "", 1);

        final List<String> excluded = new ArrayList<String>();
        excluded.add("User0");
        excluded.add("User1");

        final Response response = resource.getUsersResponse("blah", null, null, SAMPLE_QUERY_USER, false, excluded);

        assertThat(response.getEntity().toString(), equalTo(expectedResult.toString()));
    }

    @Test
    public void testWithCustomFieldInvalidCustomField()
    {
        final UserPickerResource resource = new UserPickerResource(authenticationContext,
                i18nHelper, searchService, applicationProperties, avatarService, userFilterManager, fieldConfigManager);

        when(searchService.canPerformAjaxSearch(any(JiraServiceContext.class))).thenReturn(true);

        final String customFieldId = FieldManager.CUSTOM_FIELD_PREFIX + "10000";
        // non-existing fieldconfig
        assertThat(resource.getUsersResponse(customFieldId, "1", ImmutableList.<String>of(), SAMPLE_QUERY_USER, false, null).getEntity().toString(), equalTo(EMPTY_RESULT));

        // non-existing customfield, expecting DataAccessException
        when(fieldConfigManager.getFieldConfig(1L)).thenReturn(fieldConfig);
        assertThat(resource.getUsersResponse(customFieldId, "1", ImmutableList.<String>of(), SAMPLE_QUERY_USER, false, null).getEntity().toString(), equalTo(EMPTY_RESULT));

        // unmatched customfield id
        when(fieldConfig.getCustomField()).thenReturn(customField);
        assertThat(resource.getUsersResponse(customFieldId, "1", ImmutableList.<String>of(), SAMPLE_QUERY_USER, false, null).getEntity().toString(), equalTo(EMPTY_RESULT));

        // no filter
        when(customField.getId()).thenReturn(customFieldId);
        assertThat(resource.getUsersResponse(customFieldId, "1", ImmutableList.<String>of(), SAMPLE_QUERY_USER, false, null).getEntity().toString(), equalTo(EMPTY_RESULT));

        verify(searchService, times(4)).canPerformAjaxSearch(any(JiraServiceContext.class));
        verify(searchService, times(4)).canShowEmailAddresses(any(JiraServiceContext.class));
        verify(searchService, never()).findUsers(any(JiraServiceContext.class), anyString(), any(UserSearchParams.class));
        verifyNoMoreInteractions(searchService);
    }

    @Test
    public void testWithCustomFieldWithGroupFilters()
    {
        final List<User> matchingUsers = new ArrayList<User>();

        for (int i = 0; i < 3; ++i)
        {
            matchingUsers.add(new MockUser(SAMPLE_QUERY_USER + String.valueOf(i), "Full Name" + String.valueOf(i), "user" + String.valueOf(i) + "@somewhere.com"));
        }

        final Set<String> groups = ImmutableSet.of("group 1", "group 2");

        final UserPickerResource resource = new UserPickerResource(authenticationContext,
                i18nHelper, searchService, applicationProperties, avatarService, userFilterManager, fieldConfigManager);

        final String customFieldId = FieldManager.CUSTOM_FIELD_PREFIX + "10000";
        final List<UserPickerResource.UserPickerUser> expectedUsers = new ArrayList<UserPickerResource.UserPickerUser>();
        expectedUsers.add(new UserPickerResource.UserPickerUser("User2", "Full Name2", "<div  id=\"" + customFieldId + "_i_User2\" class=\"yad\" >Full Name2&nbsp;(<b>User</b>2)</div>", null));

        final UserPickerResource.UserPickerResultsWrapper expectedResult = new UserPickerResource.UserPickerResultsWrapper(expectedUsers, "", 1);

        final List<String> excluded = new ArrayList<String>();
        excluded.add("User0");
        excluded.add("User1");

        when(fieldConfigManager.getFieldConfig(1L)).thenReturn(fieldConfig);
        when(fieldConfig.getCustomField()).thenReturn(customField);
        when(customField.getId()).thenReturn(customFieldId);
        when(userFilterManager.getFilter(eq(fieldConfig))).thenReturn(filter);
        when(filter.isEnabled()).thenReturn(true);
        when(filter.getGroups()).thenReturn(ImmutableSet.copyOf(groups));

        when(searchService.findUsers(any(JiraServiceContext.class), anyString(), eqParamsGroups(groups))).thenReturn(matchingUsers);
        when(searchService.canPerformAjaxSearch(any(JiraServiceContext.class))).thenReturn(true);

        final Response response = resource.getUsersResponse(customFieldId, "1", ImmutableList.<String>of(), SAMPLE_QUERY_USER, false, excluded);

        verify(searchService).canPerformAjaxSearch(any(JiraServiceContext.class));
        verify(searchService).canShowEmailAddresses(any(JiraServiceContext.class));
        verify(searchService).findUsers(any(JiraServiceContext.class), eq(SAMPLE_QUERY_USER), eqParamsGroups(groups));
        verifyNoMoreInteractions(searchService);
        assertThat(response.getEntity().toString(), equalTo(expectedResult.toString()));
    }

    private static class UserSearchParamsGroupsMatcher extends TypeSafeMatcher<UserSearchParams>
    {
        private Set<String> groups;

        private UserSearchParamsGroupsMatcher(final Set<String> groups)
        {
            this.groups = groups;
        }

        @Override
        protected boolean matchesSafely(final UserSearchParams userSearchParams)
        {
            return groups.equals(userSearchParams.getUserFilter().getGroups());
        }

        @Override
        public void describeTo(final Description description)
        {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    private UserSearchParams eqParamsGroups(Set<String> groups)
    {
        return argThat(new UserSearchParamsGroupsMatcher(groups));
    }
}
