package com.atlassian.jira.rest.v2.issue;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.bc.group.search.GroupPickerSearchService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.easymock.EasyMockAnnotations;
import com.atlassian.jira.easymock.Mock;
import com.atlassian.jira.rest.v2.issue.groups.GroupPickerResourceHelper;
import com.atlassian.jira.rest.v2.issue.groups.GroupPickerResourceHelperImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.NoopI18nHelper;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static com.atlassian.jira.rest.assertions.ResponseAssertions.assertResponseBody;
import static com.atlassian.jira.rest.assertions.ResponseAssertions.assertResponseCacheNever;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

/**
 * Tests for {@link GroupPickerResource}
 *
 * @since v4.4
 */
public class TestGroupPickerResource
{
    @Mock
    private GroupPickerSearchService groupPickerSearchService;

    @Mock
    private JiraAuthenticationContext authenticationContext;

    @Mock
    private ApplicationProperties applicationProperties;

    private I18nHelper i18nHelper;

    private List<Group> matchingGroups;
    private GroupPickerResourceHelper groupPickerResourceHelper;

    @Mock
    private PermissionManager permissionManager;

    @Before
    public void setUp()
    {
        EasyMockAnnotations.initMocks(this);
        groupPickerResourceHelper = new GroupPickerResourceHelperImpl(groupPickerSearchService, authenticationContext, applicationProperties, permissionManager);
        matchingGroups = Lists.newArrayList();
        i18nHelper = new NoopI18nHelper();
    }

    @Test
    public void testFindGroupsWithPartialMatch() throws Exception
    {
        matchingGroups.add(new MockGroup("lalo"));

        final MockUser mockUser = new MockUser("mocky");

        expect(groupPickerSearchService.findGroups("la")).andStubReturn(matchingGroups);
        expect(authenticationContext.getLoggedInUser()).andReturn(mockUser);
        expect(permissionManager.hasPermission(Permissions.USER_PICKER, mockUser)).andReturn(true);
        expect(authenticationContext.getI18nHelper()).andStubReturn(i18nHelper);
        expect(applicationProperties.getDefaultBackedString(APKeys.JIRA_AJAX_AUTOCOMPLETE_LIMIT)).andStubReturn("20");

        GroupPickerResource resource = new GroupPickerResource(groupPickerResourceHelper);

        final List<GroupSuggestionBean> expectedGroups =
                Lists.newArrayList(new GroupSuggestionBean("lalo",
                        "<b>la</b>lo"));

        final GroupSuggestionsBean expectedSuggestions = new GroupSuggestionsBean(1,
                NoopI18nHelper.makeTranslation(GroupPickerResourceHelperImpl.MORE_GROUP_RESULTS_I18N_KEY, "1", "1"), expectedGroups);

        replay(groupPickerSearchService, authenticationContext, applicationProperties, permissionManager);

        Response response = resource.findGroups("la", null, null);
        assertResponseCacheNever(response);
        assertResponseBody(expectedSuggestions, response);

        verify(groupPickerSearchService, authenticationContext, applicationProperties);
    }

    @Test
    public void testFindGroupsWithNoMatch() throws Exception
    {
        final MockUser mockUser = new MockUser("mocky");

        expect(authenticationContext.getLoggedInUser()).andReturn(mockUser);
        expect(permissionManager.hasPermission(Permissions.USER_PICKER, mockUser)).andReturn(true);
        expect(groupPickerSearchService.findGroups("la")).andStubReturn(matchingGroups);
        expect(authenticationContext.getI18nHelper()).andStubReturn(i18nHelper);
        expect(applicationProperties.getDefaultBackedString(APKeys.JIRA_AJAX_AUTOCOMPLETE_LIMIT)).andStubReturn("20");

        GroupPickerResource resource = new GroupPickerResource(groupPickerResourceHelper);

        final List<GroupSuggestionBean> expectedGroups =
                Lists.newArrayList();

        final GroupSuggestionsBean expectedSuggestions = new GroupSuggestionsBean(0,
                NoopI18nHelper.makeTranslation(GroupPickerResourceHelperImpl.MORE_GROUP_RESULTS_I18N_KEY, "0", "0"), expectedGroups);

        replay(groupPickerSearchService, authenticationContext, applicationProperties, permissionManager);

        Response response = resource.findGroups("la", null, null);
        assertResponseCacheNever(response);
        assertResponseBody(expectedSuggestions, response);

        verify(groupPickerSearchService, authenticationContext, applicationProperties);
    }

    @Test
    public void testFindGroupsWithExcessMatch() throws Exception
    {
        matchingGroups.add(new MockGroup("lalo"));
        matchingGroups.add(new MockGroup("lolo"));

        final MockUser mockUser = new MockUser("mocky");

        expect(authenticationContext.getLoggedInUser()).andReturn(mockUser);
        expect(permissionManager.hasPermission(Permissions.USER_PICKER, mockUser)).andReturn(true);
        expect(groupPickerSearchService.findGroups("lo")).andStubReturn(matchingGroups);
        expect(authenticationContext.getI18nHelper()).andStubReturn(i18nHelper);
        expect(applicationProperties.getDefaultBackedString(APKeys.JIRA_AJAX_AUTOCOMPLETE_LIMIT)).andStubReturn("1");

        GroupPickerResource resource = new GroupPickerResource(groupPickerResourceHelper);

        final List<GroupSuggestionBean> expectedGroups =
                Lists.newArrayList(new GroupSuggestionBean("lalo",
                        "la<b>lo</b>"));

        final GroupSuggestionsBean expectedSuggestions = new GroupSuggestionsBean(2,
                NoopI18nHelper.makeTranslation(GroupPickerResourceHelperImpl.MORE_GROUP_RESULTS_I18N_KEY, "1", "2"), expectedGroups);

        replay(groupPickerSearchService, authenticationContext, applicationProperties, permissionManager);

        Response response = resource.findGroups("lo", null, null);
        assertResponseCacheNever(response);
        assertResponseBody(expectedSuggestions, response);

        verify(groupPickerSearchService, authenticationContext, applicationProperties);
    }

    @Test
    public void testFindGroupsWithDefaultLimit() throws Exception
    {
        for(int i = 0; i < GroupPickerResourceHelperImpl.DEFAULT_MAX_RESULTS + 1; ++i)
        {
            matchingGroups.add(new MockGroup("a" + String.valueOf(i)));
        }

        final MockUser mockUser = new MockUser("mocky");

        expect(authenticationContext.getLoggedInUser()).andReturn(mockUser);
        expect(permissionManager.hasPermission(Permissions.USER_PICKER, mockUser)).andReturn(true);
        expect(groupPickerSearchService.findGroups("a")).andStubReturn(matchingGroups);
        expect(authenticationContext.getI18nHelper()).andStubReturn(i18nHelper);
        expect(applicationProperties.getDefaultBackedString(APKeys.JIRA_AJAX_AUTOCOMPLETE_LIMIT)).andStubReturn(null);

        GroupPickerResource resource = new GroupPickerResource(groupPickerResourceHelper);

        final List<GroupSuggestionBean> expectedGroups = Lists.newArrayList();
        for(int i = 0; i < GroupPickerResourceHelperImpl.DEFAULT_MAX_RESULTS; ++i)
        {
            expectedGroups.add(new GroupSuggestionBean("a" + String.valueOf(i),"<b>a</b>" + String.valueOf(i)));
        }

        final GroupSuggestionsBean expectedSuggestions = new GroupSuggestionsBean(GroupPickerResourceHelperImpl.DEFAULT_MAX_RESULTS + 1,
                NoopI18nHelper.makeTranslation(GroupPickerResourceHelperImpl.MORE_GROUP_RESULTS_I18N_KEY,
                        String.valueOf(GroupPickerResourceHelperImpl.DEFAULT_MAX_RESULTS),
                        String.valueOf(GroupPickerResourceHelperImpl.DEFAULT_MAX_RESULTS + 1)), expectedGroups);

        replay(groupPickerSearchService, authenticationContext, applicationProperties, permissionManager);

        Response response = resource.findGroups("a", null, null);
        assertResponseCacheNever(response);
        assertResponseBody(expectedSuggestions, response);

        verify(groupPickerSearchService, authenticationContext, applicationProperties);
    }

    @Test
    public void testExcludedGroups()
    {
        for(int i = 1; i < 4; ++i)
        {
            matchingGroups.add(new MockGroup("a"  + String.valueOf(i)));
        }

        final MockUser mockUser = new MockUser("mocky");

        expect(authenticationContext.getLoggedInUser()).andReturn(mockUser);
        expect(permissionManager.hasPermission(Permissions.USER_PICKER, mockUser)).andReturn(true);
        expect(groupPickerSearchService.findGroups("a")).andStubReturn(matchingGroups);
        expect(authenticationContext.getI18nHelper()).andStubReturn(i18nHelper);
        expect(applicationProperties.getDefaultBackedString(APKeys.JIRA_AJAX_AUTOCOMPLETE_LIMIT)).andStubReturn("20");

        GroupPickerResource resource = new GroupPickerResource(groupPickerResourceHelper);
        
        final List<GroupSuggestionBean> expectedGroups = Lists.newArrayList();
        expectedGroups.add(new GroupSuggestionBean("a3","<b>a</b>3"));

        final GroupSuggestionsBean expectedSuggestions = new GroupSuggestionsBean(1,
                NoopI18nHelper.makeTranslation(GroupPickerResourceHelperImpl.MORE_GROUP_RESULTS_I18N_KEY, 1, 1), expectedGroups);

        List<String> excluded = new ArrayList<String>();
        excluded.add("a1");
        excluded.add("a2");

        replay(groupPickerSearchService, authenticationContext, applicationProperties, permissionManager);
        Response response = resource.findGroups("a", excluded, null);
        assertResponseBody(expectedSuggestions, response);
    }

    @Test
    public void testUserWithNoBrowseUserPermissionsReturnsOnlyExactMatchedGroup()
    {
        String mockGroupName = "mockGroup";
        final MockUser mockUser = new MockUser("mocky");
        final MockGroup mockGroup = new MockGroup(mockGroupName);

        expect(authenticationContext.getLoggedInUser()).andReturn(mockUser);
        expect(permissionManager.hasPermission(Permissions.USER_PICKER, mockUser)).andReturn(false);
        expect(groupPickerSearchService.getGroupByName(mockGroupName)).andStubReturn(mockGroup);
        expect(authenticationContext.getI18nHelper()).andStubReturn(i18nHelper);
        expect(applicationProperties.getDefaultBackedString(APKeys.JIRA_AJAX_AUTOCOMPLETE_LIMIT)).andStubReturn("20");

        GroupPickerResource resource = new GroupPickerResource(groupPickerResourceHelper);

        final List<GroupSuggestionBean> expectedGroups = Lists.newArrayList();
        expectedGroups.add(new GroupSuggestionBean(mockGroupName,"<b>" + mockGroupName + "</b>"));

        final GroupSuggestionsBean expectedSuggestions = new GroupSuggestionsBean(1,
                NoopI18nHelper.makeTranslation(GroupPickerResourceHelperImpl.MORE_GROUP_RESULTS_I18N_KEY, 1, 1), expectedGroups);

        replay(groupPickerSearchService, authenticationContext, applicationProperties, permissionManager);
        Response response = resource.findGroups(mockGroupName, null, null);
        assertResponseBody(expectedSuggestions, response);
    }

    @Test
    public void testUserWithNoBrowseUserPermissionsDoesntReturnNonExactMatchedGroup()
    {
        String mockGroupName = "mockGroup";
        final MockUser mockUser = new MockUser("mocky");
        final MockGroup mockGroup = new MockGroup(mockGroupName);

        expect(authenticationContext.getLoggedInUser()).andReturn(mockUser);
        expect(permissionManager.hasPermission(Permissions.USER_PICKER, mockUser)).andReturn(false);
        expect(groupPickerSearchService.getGroupByName(mockGroupName)).andStubReturn(mockGroup);
        expect(authenticationContext.getI18nHelper()).andStubReturn(i18nHelper);
        expect(applicationProperties.getDefaultBackedString(APKeys.JIRA_AJAX_AUTOCOMPLETE_LIMIT)).andStubReturn("20");

        GroupPickerResource resource = new GroupPickerResource(groupPickerResourceHelper);

        final List<GroupSuggestionBean> expectedGroups = Lists.newArrayList();

        final GroupSuggestionsBean expectedSuggestions = new GroupSuggestionsBean(0,
                NoopI18nHelper.makeTranslation(GroupPickerResourceHelperImpl.MORE_GROUP_RESULTS_I18N_KEY, 0, 0), expectedGroups);

        replay(groupPickerSearchService, authenticationContext, applicationProperties, permissionManager);
        Response response = resource.findGroups("mockGr", null, null);
        assertResponseBody(expectedSuggestions, response);
    }

    public static class MockGroup implements Group
    {

        private String name;

        public MockGroup(final String name)
        {
            this.name = name;
        }

        @Override
        public String getName()
        {
            return name;
        }

        @Override
        public int compareTo(Group o)
        {
            return name.compareTo(o.getName());
        }
    }
}
