package com.atlassian.jira.webtests.ztests.plugin.reloadable;

import com.atlassian.jira.functest.framework.Splitable;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.util.collect.MapBuilder;

import java.util.Map;

import static com.atlassian.jira.webtests.ztests.plugin.reloadable.ReferencePluginConstants.REFERENCE_PLUGIN_KEY;

/**
 * Test the reloadability of custom field searchers.
 *
 * @since v4.3
 */
@WebTest ({ Category.FUNC_TEST, Category.RELOADABLE_PLUGINS, Category.REFERENCE_PLUGIN, Category.SLOW_IMPORT})
@Splitable
public class TestCustomFieldTypeSearcherReloadability extends AbstractCustomFieldPluginTest
{
    private static final String TEST_XML = "ReloadablePluginsWithCustomTypesAndSearchersDisabled.xml";
    private static final String TEST_FIELD_INSTANCE_NAME = "Test reference labels field";

    private static final String CUSTOMFIELD_SEARCH_SECTION_ID = "navigator-filter-subheading-customfields";
    private static final String QUERY_STRING = "\"Test reference labels field\" = Fred";
    private static final String JQL_ERROR = "Field 'Test reference labels field' is not searchable, it is only sortable.";

    private final Map<String, String> searchersForCFTypes =
            MapBuilder.<String, String>newBuilder()
                    .add(ReferencePluginConstants.CFTYPE_LABELS_MODULE_KEY, ReferencePluginConstants.CFTYPE_LABELSSEARCHER_MODULE_KEY)
                    .add(ReferencePluginConstants.CFTYPE_USERPICKER_MODULE_KEY, ReferencePluginConstants.CFTYPE_USERPICKERSEARCHER_MODULE_KEY)
                    .add(ReferencePluginConstants.CFTYPE_DATETIME_MODULE_KEY, ReferencePluginConstants.CFTYPE_DATEIMESEARCHER_MODULE_KEY)
                    .add(ReferencePluginConstants.CFTYPE_TEXTAREA_MODULE_KEY, ReferencePluginConstants.CFTYPE_TEXTAREASEARCHER_MODULE_KEY)
                    .add(ReferencePluginConstants.CFTYPE_CASCADINGSELECT_MODULE_KEY, ReferencePluginConstants.CFTYPE_LABELSSEARCHER_MODULE_KEY)
                    .add(ReferencePluginConstants.CFTYPE_SELECT_MODULE_KEY, ReferencePluginConstants.CFTYPE_LABELSSEARCHER_MODULE_KEY)
                    .add(ReferencePluginConstants.CFTYPE_MULTIGROUPPICKER_KEY, ReferencePluginConstants.CFTYPE_LABELSSEARCHER_MODULE_KEY)
                    .toMap();

    private final String[] LABELS_VALUES = {"Fred","Barney","Wilma"};

    @Override
    protected void setUpTest()
    {
        administration.restoreDataSlowOldWay(TEST_XML);
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        administration.plugins().referencePlugin().enable();
    }

    public void testCustomFieldTypesShouldBeAvailableAfterEnablement() throws Exception
    {
       for (String moduleKey : searchersForCFTypes.keySet())
       {
            assertFalse(String.format("Plugin: %s should be disabled", moduleKey),
                      administration.plugins().isPluginModuleEnabled(REFERENCE_PLUGIN_KEY, completeKey(moduleKey)));
            administration.plugins().enablePluginModule(REFERENCE_PLUGIN_KEY, completeKey(moduleKey));
            assertTrue(String.format("Plugin: %s should be enabled", moduleKey),
                      administration.plugins().isPluginModuleEnabled(REFERENCE_PLUGIN_KEY, completeKey(moduleKey)));
       }

    }

    public void testEnabledCustomFieldTypeWithNoSearcher()
    {
        enableLabelCustomType();
        disableCustomLabelTypeSearcher();
        disableLabelTypeSearcher();
        setUpCustomFieldInstance(completeKey(ReferencePluginConstants.CFTYPE_LABELS_MODULE_KEY), TEST_FIELD_INSTANCE_NAME, null);
        _assertCustomFieldSearcherNotVisibleInIssueNav();
    }

    public void testEnabledCustomFieldTypeWithSearcher()
    {
        enableLabelCustomType();
        enableCustomLabelTypeSearcher();
        setUpCustomFieldInstance(completeKey(ReferencePluginConstants.CFTYPE_LABELS_MODULE_KEY), TEST_FIELD_INSTANCE_NAME, null);
        _assertCustomFieldSearcherVisibleInIssueNav();
    }

    public void testJqlSearchWithNoSearcher()
    {
        enableLabelCustomType();
        disableCustomLabelTypeSearcher();
        disableLabelTypeSearcher();
        setUpCustomFieldInstance(completeKey(ReferencePluginConstants.CFTYPE_LABELS_MODULE_KEY), TEST_FIELD_INSTANCE_NAME, null);
        createNewIssue("homosapien", "Bug", "Can't search labels field");
        performJqlSearch(QUERY_STRING);
        text.assertTextPresent(new IdLocator(tester,"jqlerror"),JQL_ERROR);
    }

    public void testJqlSearchWithSearcher()
    {
        enableLabelCustomType();
        enableCustomLabelTypeSearcher();
        setUpCustomFieldInstance(completeKey(ReferencePluginConstants.CFTYPE_LABELS_MODULE_KEY), TEST_FIELD_INSTANCE_NAME, null);
        createNewIssue("homosapien", "Bug", "Can search labels field");
        performJqlSearch(QUERY_STRING);
        _assertSearchWithResults(QUERY_STRING, "HSP-1");
    }

    public void testCustomFieldSearchersShouldNotBeAccessibleAfterDisablingThePlugin() throws Exception
    {
        enableAllCustomFieldTypes();
        enableAllSearchers();
        setUpCustomFieldInstance(completeKey(ReferencePluginConstants.CFTYPE_LABELS_MODULE_KEY), TEST_FIELD_INSTANCE_NAME, null);
        _assertCustomFieldSearcherVisibleInIssueNav();
        administration.plugins().referencePlugin().disable();
        _assertCustomFieldSearcherNotVisibleInIssueNav();
    }

    public void testCustomFieldSearchersShouldBeAccessibleAfterMultiplePluginDisablingAndEnabling() throws Exception
    {
        enableAllCustomFieldTypes();
        enableAllSearchers();
        setUpCustomFieldInstance(completeKey(ReferencePluginConstants.CFTYPE_LABELS_MODULE_KEY), TEST_FIELD_INSTANCE_NAME, null);
        createNewIssue("homosapien", "Bug", "Can search labels field");
        _assertCustomFieldSearcherVisibleInIssueNav();
        administration.plugins().referencePlugin().disable();
        _assertCustomFieldSearcherNotVisibleInIssueNav();
        administration.plugins().referencePlugin().enable();;
        _assertCustomFieldSearcherVisibleInIssueNav();
        performJqlSearch(QUERY_STRING);
        _assertSearchWithResults(QUERY_STRING, "HSP-1");
    }

    public void testDisablingOneCustomSearcherLeavesOtherOneVisible() throws Exception
    {
        enableAllCustomFieldTypes();
        enableAllSearchers();
        setUpCustomFieldInstance(completeKey(ReferencePluginConstants.CFTYPE_LABELS_MODULE_KEY), TEST_FIELD_INSTANCE_NAME, null);
        disableLabelTypeSearcher();
        createNewIssue("homosapien", "Bug", "Can search labels field");
        _assertCustomFieldSearcherVisibleInIssueNav();
        performJqlSearch(QUERY_STRING);
        _assertSearchWithResults(QUERY_STRING, "HSP-1");
    }

    private void _assertCustomFieldSearcherNotVisibleInIssueNav()
    {
        navigation.issueNavigator().displayAllIssues();
        assertFalse("There should be no custom fields searcher", issueNavigatorCustomFieldSearchLocator().exists());
    }

    private void _assertCustomFieldSearcherVisibleInIssueNav()
    {
        navigation.issueNavigator().displayAllIssues();
        assertTrue("There should be a custom fields searcher", issueNavigatorCustomFieldSearchLocator().exists());
    }

    void _assertSearchWithResults(String jqlString, String... issueKeys)
    {
        navigation.issueNavigator().createSearch(jqlString);
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults(issueKeys);
    }

    private void createNewIssue(final String projectName, final String issueType, final String summary)
    {
        navigation.issue().createIssue(projectName,
                issueType,
                summary,
                MapBuilder.<String, String[]>newBuilder().add(customFieldId, LABELS_VALUES).toMap());
    }

    private void performJqlSearch(final String queryString)
    {
        navigation.issueNavigator().createSearch(queryString);
    }


    private void enableAllSearchers()
    {
        enableLabelTypeSearcher();
        enableCustomLabelTypeSearcher();
    }

    private void enableAllCustomFieldTypes()
    {
       for (String moduleKey : searchersForCFTypes.keySet())
       {
            administration.plugins().enablePluginModule(REFERENCE_PLUGIN_KEY, completeKey(moduleKey));
       }
    }

    private void enableLabelCustomType()
    {
        administration.plugins().enablePluginModule(REFERENCE_PLUGIN_KEY, completeKey(ReferencePluginConstants.CFTYPE_LABELS_MODULE_KEY));
    }

    private void enableCustomLabelTypeSearcher()
    {
        administration.plugins().enablePluginModule(REFERENCE_PLUGIN_KEY,completeKey(ReferencePluginConstants.CFTYPE_LABELSSEARCHER_MODULE_KEY));
    }

    private void disableCustomLabelTypeSearcher()
    {
        administration.plugins().disablePluginModule(REFERENCE_PLUGIN_KEY, completeKey(ReferencePluginConstants.CFTYPE_LABELSSEARCHER_MODULE_KEY));
    }

    private void enableLabelTypeSearcher()
    {
        administration.plugins().enablePluginModule(REFERENCE_PLUGIN_KEY,completeKey(ReferencePluginConstants.CFTYPE_STDLABELSSEARCHER_MODULE_KEY));
    }

    private void disableLabelTypeSearcher()
    {
        administration.plugins().disablePluginModule(REFERENCE_PLUGIN_KEY,completeKey(ReferencePluginConstants.CFTYPE_STDLABELSSEARCHER_MODULE_KEY));
    }

    private String completeKey(String moduleKey)
    {
        return REFERENCE_PLUGIN_KEY + ":" + moduleKey;
    }

    private Locator issueNavigatorCustomFieldSearchLocator()
    {
        return locator.id(CUSTOMFIELD_SEARCH_SECTION_ID);
    }
}
