package com.atlassian.jira.webtests.ztests.customfield;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.user.UserFilter;
import com.atlassian.jira.user.UserFilterUtils;
import com.atlassian.jira.util.json.JSONException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collection;
import java.util.Map;

@WebTest ({ Category.FUNC_TEST, Category.BROWSING, Category.CUSTOM_FIELDS })
public class TestCustomFieldUserPicker extends FuncTestCase
{
    private static final String FIELD_NAME = "Developer";

    private static final String[] ALL_GROUPS = new String[] {JIRA_USERS_GROUP, JIRA_DEV_GROUP, JIRA_ADMIN_GROUP};
    private static final long[] ALL_ROLE_IDS = new long[] {JIRA_USERS_ROLE_ID, JIRA_DEV_ROLE_ID, JIRA_ADMIN_ROLE_ID};
    private static final Map<Long, String> ALL_ROLE_ID_TO_NAME_MAP = Maps.newHashMap();
    static {
        ALL_ROLE_ID_TO_NAME_MAP.put(JIRA_ADMIN_ROLE_ID, JIRA_ADMIN_ROLE);
        ALL_ROLE_ID_TO_NAME_MAP.put(JIRA_DEV_ROLE_ID, JIRA_DEV_ROLE);
        ALL_ROLE_ID_TO_NAME_MAP.put(JIRA_USERS_ROLE_ID, JIRA_USERS_ROLE);
    }

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreBlankInstance();
        tester.getDialog().getWebClient().getClientProperties().setHiddenFieldsEditable(true);
    }

    public void testCreateAndSetDefaultValue()
    {
        navigation.gotoAdminSection("view_custom_fields");

        createUserPickerAndGoToConfigurationPage();

        // configure - set empty default value - already set by default
        tester.clickLink("customfield_10000-edit-default");
        tester.submit("Set Default");
        tester.assertTextPresent("Default Configuration Scheme for "+ FIELD_NAME);
        tester.assertTextNotPresent(FRED_USERNAME);

        // configure - set default value to Fred
        tester.clickLink("customfield_10000-edit-default");
        tester.assertFormElementEquals("customfield_10000", "");
        tester.setFormElement("customfield_10000", FRED_USERNAME);
        tester.submit("Set Default");
        tester.assertTextPresent("Default Configuration Scheme for "+ FIELD_NAME);
        tester.assertTextPresent(FRED_FULLNAME);

        // configure - set default value to Admin
        tester.clickLink("customfield_10000-edit-default");
        tester.assertFormElementEquals("customfield_" + "10000", FRED_USERNAME); // is sorted
        tester.setFormElement("customfield_10000", ADMIN_USERNAME);
        tester.submit("Set Default");
        tester.assertTextNotPresent(FRED_FULLNAME);
        tester.assertTextPresent("Default Configuration Scheme for "+ FIELD_NAME);

        // configure - clear default value
        tester.clickLink("customfield_10000-edit-default");
        tester.assertFormElementEquals("customfield_" + "10000", ADMIN_USERNAME);
        tester.setFormElement("customfield_" + "10000", "");
        tester.submit("Set Default");
        tester.assertTextNotPresent(FRED_FULLNAME);
        tester.assertTextPresent("Default Configuration Scheme for "+ FIELD_NAME);

        tester.clickLink("customfield_10000-edit-default");
        tester.assertFormElementEquals("customfield_10000", "");
    }


    public void testUserFilterSelection() throws JSONException
    {
        navigation.gotoAdminSection("view_custom_fields");

        createUserPickerAndGoToConfigurationPage();

        // by default user filter is not enabled, and all users are active
        assertUserFilterSummary(UserFilter.DISABLED);

        final UserFilter[] testData = new UserFilter[] {
              UserFilter.DISABLED,
              f(r(), g(JIRA_DEV_GROUP)),
              f(r(), g(JIRA_DEV_GROUP, JIRA_ADMIN_GROUP)),
              f(r(), g(JIRA_DEV_GROUP, JIRA_ADMIN_GROUP, JIRA_USERS_GROUP)),
              f(r(JIRA_ADMIN_ROLE_ID), g(JIRA_DEV_GROUP, JIRA_ADMIN_GROUP, JIRA_USERS_GROUP)),
              f(r(JIRA_ADMIN_ROLE_ID, JIRA_DEV_ROLE_ID), g(JIRA_DEV_GROUP, JIRA_ADMIN_GROUP, JIRA_USERS_GROUP)),
              f(r(JIRA_ADMIN_ROLE_ID, JIRA_DEV_ROLE_ID, JIRA_USERS_ROLE_ID), g(JIRA_DEV_GROUP, JIRA_ADMIN_GROUP, JIRA_USERS_GROUP)),
              f(r(JIRA_ADMIN_ROLE_ID, JIRA_DEV_ROLE_ID, JIRA_USERS_ROLE_ID), g(JIRA_DEV_GROUP, JIRA_ADMIN_GROUP)),
              f(r(JIRA_ADMIN_ROLE_ID, JIRA_DEV_ROLE_ID, JIRA_USERS_ROLE_ID), g(JIRA_DEV_GROUP)),
              f(r(JIRA_ADMIN_ROLE_ID, JIRA_DEV_ROLE_ID, JIRA_USERS_ROLE_ID), g()),
              f(r(JIRA_ADMIN_ROLE_ID, JIRA_DEV_ROLE_ID), g()),
              f(r(JIRA_ADMIN_ROLE_ID), g()),
              f(r(), g())
        };

        UserFilter originalUserFilter = UserFilter.DISABLED;
        for (UserFilter newUserFilter : testData)
        {
            setFilterAndAssert(originalUserFilter, newUserFilter);
            originalUserFilter = newUserFilter;
        }
    }

    public void testSetDefaultValueWithUserFilter() throws JSONException
    {
        navigation.gotoAdminSection("view_custom_fields");

        createUserPickerAndGoToConfigurationPage();

        // set groups to jira-admin
        setUserFilter(f(r(), g(JIRA_ADMIN_GROUP)));
        // set default value to admin
        tester.clickLink("customfield_10000-edit-default");
        tester.setFormElement("customfield_10000", ADMIN_USERNAME);
        tester.submit("Set Default");
        tester.assertTextNotPresent(FRED_FULLNAME);
        tester.assertTextPresent("Default Configuration Scheme for "+ FIELD_NAME);

        // set default to fred, invalid
        tester.clickLink("customfield_10000-edit-default");
        tester.setFormElement("customfield_10000", FRED_USERNAME);
        tester.submit("Set Default");
        tester.assertTextPresent("User &#39;"+FRED_USERNAME+"&#39; is not valid for this user picker.");
        tester.clickLink("cancelButton");
        tester.assertTextPresent("Default Configuration Scheme for "+ FIELD_NAME);

        // set groups to jira-dev, jira-admin
        setUserFilter(f(r(), g(JIRA_ADMIN_GROUP, JIRA_DEV_GROUP)));
        // set default value to admin
        tester.clickLink("customfield_10000-edit-default");
        tester.setFormElement("customfield_10000", ADMIN_USERNAME);
        tester.submit("Set Default");
        tester.assertTextNotPresent(FRED_FULLNAME);
        tester.assertTextPresent("Default Configuration Scheme for "+ FIELD_NAME);

        // set default to fred, invalid
        tester.clickLink("customfield_10000-edit-default");
        tester.setFormElement("customfield_10000", FRED_USERNAME);
        tester.submit("Set Default");
        tester.assertTextPresent("User &#39;"+FRED_USERNAME+"&#39; is not valid for this user picker.");
        tester.clickLink("cancelButton");
        tester.assertTextPresent("Default Configuration Scheme for "+ FIELD_NAME);

        // set groups to jira-dev, jira-admin, jira-user
        setUserFilter(f(r(), g(JIRA_ADMIN_GROUP, JIRA_DEV_GROUP, JIRA_USERS_GROUP)));
        // set default value to admin
        tester.clickLink("customfield_10000-edit-default");
        tester.setFormElement("customfield_10000", ADMIN_USERNAME);
        tester.submit("Set Default");
        tester.assertTextNotPresent(FRED_FULLNAME);
        tester.assertTextPresent("Default Configuration Scheme for "+ FIELD_NAME);

        // set default to fred, valid
        tester.clickLink("customfield_10000-edit-default");
        tester.setFormElement("customfield_10000", FRED_USERNAME);
        tester.submit("Set Default");
        tester.assertTextPresent(FRED_FULLNAME);
        tester.assertTextPresent("Default Configuration Scheme for "+ FIELD_NAME);

        // set groups to jira-user
        setUserFilter(f(r(), g(JIRA_USERS_GROUP)));
        // set default to fred, valid
        tester.clickLink("customfield_10000-edit-default");
        tester.setFormElement("customfield_10000", FRED_USERNAME);
        tester.submit("Set Default");
        tester.assertTextPresent(FRED_FULLNAME);
        tester.assertTextPresent("Default Configuration Scheme for "+ FIELD_NAME);
    }

    public void testSetValueWithUserFilterByGroups() throws JSONException
    {
        navigation.gotoAdminSection("view_custom_fields");

        createUserPickerAndGoToConfigurationPage();

        // create issue without user restrictions first
        navigation.issue().goToCreateIssueForm(PROJECT_HOMOSAP, ISSUE_TYPE_BUG);
        tester.setFormElement("summary", "summary");
        tester.setFormElement("customfield_10000", FRED_USERNAME);
        tester.submit("Create");
        tester.assertTextPresent("There are no comments yet on this issue.");

        setUserFilter(f(r(), g(JIRA_DEV_GROUP)), true);
        // create issue with user restrictions and set invalid custom field value
        navigation.issue().goToCreateIssueForm(PROJECT_HOMOSAP, ISSUE_TYPE_BUG);
        tester.setFormElement("summary", "summary");
        tester.setFormElement("customfield_10000", FRED_USERNAME);
        tester.submit("Create");
        tester.assertTextPresent("User &#39;" + FRED_USERNAME + "&#39; is not valid for this user picker.");
        tester.assertTextNotPresent("There are no comments yet on this issue.");
        // try again with admin
        tester.setFormElement("customfield_10000", ADMIN_USERNAME);
        tester.submit("Create");
        tester.assertTextPresent("There are no comments yet on this issue.");

        // test edit issue that has existing invalid value, should pass
        navigation.issue().gotoEditIssue(PROJECT_HOMOSAP_KEY + "-1");
        tester.setFormElement("customfield_10000", FRED_USERNAME);
        tester.submit("Update");
        tester.assertTextPresent("There are no comments yet on this issue.");

        // test edit issue that has existing valid value, should fail
        navigation.issue().gotoEditIssue(PROJECT_HOMOSAP_KEY + "-2");
        tester.setFormElement("customfield_10000", FRED_USERNAME);
        tester.submit("Update");
        tester.assertTextPresent("User &#39;" + FRED_USERNAME + "&#39; is not valid for this user picker.");
        tester.assertTextNotPresent("There are no comments yet on this issue.");
        // try again with admin
        tester.setFormElement("customfield_10000", ADMIN_USERNAME);
        tester.submit("Update");
        tester.assertTextPresent("There are no comments yet on this issue.");

        // try edit again with a relaxed user restrictions
        setUserFilter(f(r(), g(JIRA_USERS_GROUP)), true);
        navigation.issue().gotoEditIssue(PROJECT_HOMOSAP_KEY+"-2");
        tester.setFormElement("customfield_10000", FRED_USERNAME);
        tester.submit("Update");
        tester.assertTextPresent("There are no comments yet on this issue.");
    }

    public void testSetValueWithUserFilterByProjectRoles() throws JSONException
    {
        // add admin to admin role, fred to user role
        administration.roles().addProjectRoleForUser(PROJECT_HOMOSAP, JIRA_ADMIN_ROLE, ADMIN_USERNAME);
        administration.roles().addProjectRoleForUser(PROJECT_HOMOSAP, JIRA_USERS_ROLE, FRED_USERNAME);

        navigation.gotoAdminSection("view_custom_fields");

        createUserPickerAndGoToConfigurationPage();

        // create issue without user filters first
        navigation.issue().goToCreateIssueForm(PROJECT_HOMOSAP, ISSUE_TYPE_BUG);
        tester.setFormElement("summary", "summary");
        tester.setFormElement("customfield_10000", FRED_USERNAME);
        tester.submit("Create");
        tester.assertTextPresent("There are no comments yet on this issue.");

        setUserFilter(f(r(JIRA_ADMIN_ROLE_ID), g()), true);
        // create issue with user filter and set invalid custom field value
        navigation.issue().goToCreateIssueForm(PROJECT_HOMOSAP, ISSUE_TYPE_BUG);
        tester.setFormElement("summary", "summary");
        tester.setFormElement("customfield_10000", FRED_USERNAME);
        tester.submit("Create");
        tester.assertTextPresent("User &#39;" + FRED_USERNAME + "&#39; is not valid for this user picker.");
        tester.assertTextNotPresent("There are no comments yet on this issue.");
        // try again with admin
        tester.setFormElement("customfield_10000", ADMIN_USERNAME);
        tester.submit("Create");
        tester.assertTextPresent("There are no comments yet on this issue.");

        // test edit issue that has existing invalid value, should pass
        navigation.issue().gotoEditIssue(PROJECT_HOMOSAP_KEY + "-1");
        tester.setFormElement("customfield_10000", FRED_USERNAME);
        tester.submit("Update");
        tester.assertTextPresent("There are no comments yet on this issue.");

        // test edit issue that has existing valid value, should fail
        navigation.issue().gotoEditIssue(PROJECT_HOMOSAP_KEY + "-2");
        tester.setFormElement("customfield_10000", FRED_USERNAME);
        tester.submit("Update");
        tester.assertTextPresent("User &#39;" + FRED_USERNAME + "&#39; is not valid for this user picker.");
        tester.assertTextNotPresent("There are no comments yet on this issue.");
        // try again with admin
        tester.setFormElement("customfield_10000", ADMIN_USERNAME);
        tester.submit("Update");
        tester.assertTextPresent("There are no comments yet on this issue.");

        // try edit again with a relaxed user filter
        setUserFilter(f(r(JIRA_USERS_ROLE_ID), g()), true);
        navigation.issue().gotoEditIssue(PROJECT_HOMOSAP_KEY+"-2");
        tester.setFormElement("customfield_10000", FRED_USERNAME);
        tester.submit("Update");
        tester.assertTextPresent("There are no comments yet on this issue.");
    }

    private void createUserPickerAndGoToConfigurationPage()
    {
        // add Single User Picker
        tester.clickLink("add_custom_fields");
        tester.checkCheckbox("fieldType", "com.atlassian.jira.plugin.system.customfieldtypes:userpicker");
        tester.submit("nextBtn");

        // set Name to 'Developer'
        tester.setFormElement("fieldName", FIELD_NAME);
        tester.submit("nextBtn");

        // associate with 'Default' screen
        tester.checkCheckbox("associatedScreens", "1");
        tester.submit("Update");

        // test the new field is present on the page
        tester.assertTextPresent("Custom Fields");
        text.assertTextPresent(locator.id("custom-fields-customfield_10000-name"),FIELD_NAME);
        text.assertTextPresent(locator.id("custom-fields-customfield_10000-type"),"User Picker");

        tester.clickLink("config_customfield_10000");
    }

    private UserFilter f(Collection<Long> roleIds, Collection<String> groups)
    {
        return new UserFilter(true, roleIds, groups);
    }

    private Collection<Long> r(Long... roleIds)
    {
        return ImmutableList.copyOf(roleIds);
    }

    private Collection<String> g(String... groups)
    {
        return ImmutableList.copyOf(groups);
    }

    private void setFilterAndAssert(final UserFilter originalUserFilter, final UserFilter newUserFilter) throws JSONException
    {
        tester.clickLink("customfield_10000-edit-userpickerfilter");
        tester.assertTextPresent("User filtering for Custom Field : " + FIELD_NAME);
        // TODO need to change the data to be returned as hidden element instead of data attribute of div to allow us to assert on originalUserFilter
        // not testing the javascript (already done in qunit), just directly setting the final json to be submitted
        tester.setFormElement("userFilterJson", UserFilterUtils.toJson(newUserFilter, null).toString());
        tester.submit("Save");
        tester.assertTextPresent("Default Configuration Scheme for " + FIELD_NAME);
        assertUserFilterSummary(newUserFilter);
    }

    private void setUserFilter(UserFilter userFilter) throws JSONException
    {
        setUserFilter(userFilter, false);
    }

    private void setUserFilter(UserFilter userFilter, boolean navigateToPageFirst) throws JSONException
    {
        if (navigateToPageFirst)
        {
            navigation.gotoAdminSection("view_custom_fields");
            tester.clickLink("config_customfield_10000");
        }
        setFilterAndAssert(null, userFilter);
    }

    private void assertUserFilterSummary(UserFilter userFilter)
    {
        if (!userFilter.isEnabled())
        {
            tester.assertTextPresent("All active users are allowed");
            tester.assertTextNotPresent("Filtering users by the following");
        }
        else if (CollectionUtils.isNotEmpty(userFilter.getGroups()) || CollectionUtils.isNotEmpty(userFilter.getRoleIds()))
        {
            // some filters are configured
            tester.assertTextPresent("Filtering users by the following");
            for (Long roleId : userFilter.getRoleIds())
            {
                tester.assertTextPresent("<b>Project Role</b>: " + ALL_ROLE_ID_TO_NAME_MAP.get(roleId));
            }
            for (Long roleId : ALL_ROLE_IDS)
            {
                if (!userFilter.getRoleIds().contains(roleId))
                {
                    tester.assertTextNotPresent("<b>Project Role</b>: " + ALL_ROLE_ID_TO_NAME_MAP.get(roleId));
                }
            }

            for (String groupName : userFilter.getGroups())
            {
                tester.assertTextPresent("<b>Group</b>: " + groupName);
            }
            for (String groupName : ALL_GROUPS)
            {
                if (!userFilter.getGroups().contains(groupName))
                {
                    tester.assertTextNotPresent("<b>Group</b>: " + groupName);
                }
            }
        }
        else
        {
            // no filters
            tester.assertTextPresent("No users are allowed");
            tester.assertTextNotPresent("Filtering users by the following");
        }
    }
}
