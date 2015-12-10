package com.atlassian.jira.webtests.ztests.customfield;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.navigation.BulkChangeWizard;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;

import static com.atlassian.jira.functest.framework.suite.Category.CUSTOM_FIELDS;
import static com.atlassian.jira.functest.framework.suite.Category.FIELDS;
import static com.atlassian.jira.functest.framework.suite.Category.FUNC_TEST;
import static com.atlassian.jira.functest.framework.suite.Category.SECURITY;


@WebTest ( { FUNC_TEST, CUSTOM_FIELDS, FIELDS, SECURITY })
public class TestCustomFieldXss extends FuncTestCase
{

    private static final String ON_DEMAND_FEATURE = "com.atlassian.jira.config.CoreFeatures.ON_DEMAND";

    private static final String RAW_DESC_TEMPLATE = "description *wiki* markup <div>%s</div>";
    private static final String HTML_DESC_TEMPLATE = "description *wiki* markup <div>%s</div>";
    private static final String WIKI_DESC_TEMPLATE = "<p>description <b>wiki</b> markup &lt;div&gt;%s&lt;/div&gt;</p>";

    private static final String CUSTOM_FIELD_TITLE = "<div>xsstest</div>";
    private static final String CUSTOM_FIELD_WIKI = "&lt;div&gt;xsstest&lt;/div&gt;";

    private static final Iterable<String> CUSTOM_FIELD_TYPES = ImmutableList.of(
        builtInCustomFieldKey(CUSTOM_FIELD_TYPE_SELECT),
        builtInCustomFieldKey(CUSTOM_FIELD_TYPE_RADIO),
        builtInCustomFieldKey(CUSTOM_FIELD_TYPE_CHECKBOX),
        builtInCustomFieldKey(CUSTOM_FIELD_TYPE_TEXTFIELD),
        builtInCustomFieldKey(CUSTOM_FIELD_TYPE_MULTISELECT),
        builtInCustomFieldKey(CUSTOM_FIELD_TYPE_USERPICKER),
        builtInCustomFieldKey(CUSTOM_FIELD_TYPE_MULTIUSERPICKER),
        builtInCustomFieldKey(CUSTOM_FIELD_TYPE_DATEPICKER),
        builtInCustomFieldKey(CUSTOM_FIELD_TYPE_DATETIME),
        builtInCustomFieldKey(CUSTOM_FIELD_TYPE_GROUPPICKER),
        builtInCustomFieldKey(CUSTOM_FIELD_TYPE_MULTIGROUPPICKER)
    );

    private static final Map<String, List<String>> SEARCHERS = ImmutableMap.<String, List<String>>builder()
        .put(builtInCustomFieldKey(CUSTOM_FIELD_TEXT_SEARCHER), toBuiltInCustomFieldKeys(CUSTOM_FIELD_TYPE_TEXTFIELD))
        .put(builtInCustomFieldKey(CUSTOM_FIELD_EXACT_TEXT_SEARCHER), toBuiltInCustomFieldKeys(CUSTOM_FIELD_TYPE_URL))
        .put(builtInCustomFieldKey(CUSTOM_FIELD_DATE_RANGE), toBuiltInCustomFieldKeys(CUSTOM_FIELD_TYPE_DATEPICKER))
        .put(builtInCustomFieldKey(CUSTOM_FIELD_EXACT_NUMBER), toBuiltInCustomFieldKeys(CUSTOM_FIELD_TYPE_FLOAT))
        .put(builtInCustomFieldKey(CUSTOM_FIELD_NUMBER_RANGE), toBuiltInCustomFieldKeys(CUSTOM_FIELD_TYPE_FLOAT))
        .put(builtInCustomFieldKey(CUSTOM_FIELD_PROJECT_SEARCHER), toBuiltInCustomFieldKeys(CUSTOM_FIELD_TYPE_PROJECT))
        .put(builtInCustomFieldKey(CUSTOM_FIELD_GROUP_PICKER_SEARCHER), toBuiltInCustomFieldKeys(CUSTOM_FIELD_TYPE_MULTIGROUPPICKER))
        .put(builtInCustomFieldKey(CUSTOM_FIELD_MULTI_SELECT_SEARCHER), toBuiltInCustomFieldKeys(CUSTOM_FIELD_TYPE_SELECT, CUSTOM_FIELD_TYPE_RADIO, CUSTOM_FIELD_TYPE_MULTICHECKBOXES))
        .put(builtInCustomFieldKey(CUSTOM_FIELD_CASCADING_SELECT_SEARCHER), toBuiltInCustomFieldKeys(CUSTOM_FIELD_TYPE_CASCADINGSELECT))
        .put(builtInCustomFieldKey(CUSTOM_FIELD_LABEL_SEARCHER), toBuiltInCustomFieldKeys(CUSTOM_FIELD_TYPE_LABELS))
        .build();

    private static final ImmutableMap<String, String> SEARCHERS_NON_RENDERING = ImmutableMap.of(
            builtInCustomFieldKey(CUSTOM_FIELD_VERSION_SEARCHER), builtInCustomFieldKey(CUSTOM_FIELD_TYPE_VERSION),
            builtInCustomFieldKey(CUSTOM_FIELD_USER_PICKER_GROUP_SEARCHER), builtInCustomFieldKey(CUSTOM_FIELD_TYPE_USERPICKER)
    );

    private static List<String> toBuiltInCustomFieldKeys(String... keys)
    {
        List<String> customFieldKeys = Lists.newArrayListWithCapacity(keys.length);

        for (String key : keys)
        {
            customFieldKeys.add(builtInCustomFieldKey(key));
        }

        return customFieldKeys;
    }

    @Override
    protected void setUpTest()
    {
        backdoor.restoreBlankInstance();

    }

    @Override
    protected void tearDownTest()
    {
        backdoor.darkFeatures().disableForSite(ON_DEMAND_FEATURE);
    }

    public void testCustomFieldDescriptionsCanBeRenderedAsRawHtmlOrWikiMarkup() throws Exception
    {
        for (String type : CUSTOM_FIELD_TYPES)
        {
            testSingleCustomFieldDescriptionOnCustomFieldsScreen(type);
        }
    }

    public void testCustomFieldDescriptionsCanBeRenderedAsRawHtmlOrWikiMarkUpInIssueNavigator() throws Exception
    {
        for(Map.Entry<String, List<String>> entry : SEARCHERS.entrySet())
        {
            testSingleCustomFieldDescriptionOnIssueNavigatorScreen(entry.getValue(), entry.getKey());
        }
    }

    public void testCustomFieldDescriptionsInIssueNavigatorNoXss() throws Exception
    {
        for(Map.Entry<String, String> entry : SEARCHERS_NON_RENDERING.entrySet())
        {
            testSingleCustomFieldOnIssueNavigatorScreen(entry.getValue(), entry.getKey());
        }
    }

    /**
     * Just for this test, we are restoring from an XML backup because currently there's no way to put
     * custom fields into screens.
     * @throws Exception
     */
    public void testCustomFieldDescriptionsInBulkChangeIssue() throws Exception
    {
        restoreDataForTest();

        backdoor.project().addProject("TestProject", "TEST", "admin");
        backdoor.issues().createIssue("TEST", "This is just a test");

        backdoor.darkFeatures().enableForSite(ON_DEMAND_FEATURE);
        navigation.issueNavigator().displayAllIssues();

        final String closeIssue = "jira_2_6";

        navigation.issueNavigator()
                .bulkChange(IssueNavigatorNavigation.BulkChangeOption.ALL_PAGES)
                .selectAllIssues()
                .chooseOperation(BulkChangeWizard.BulkOperationsImpl.TRANSITION)
                .chooseWorkflowTransition(new BulkChangeWizard.BulkOperationsCustom(closeIssue));

        assertFalse("ON_DEMAND is enabled so the description should not be rendered as html in this page", getPageSource().contains(CUSTOM_FIELD_TITLE));
        assertTrue("ON_DEMAND is enabled so the description should be rendered as wiki markup in this page", getPageSource().contains(CUSTOM_FIELD_WIKI));
    }

    private void testSingleCustomFieldOnIssueNavigatorScreen(String customFieldType, String customFieldSearcher)
    {
        final String fieldId = backdoor.customFields().createCustomField(customFieldType + "-name", fieldDescription(customFieldType), customFieldType, customFieldSearcher);

        // now test that the description is does not contain raw HTML
        backdoor.darkFeatures().enableForSite(ON_DEMAND_FEATURE);
        navigation.issueNavigator().displayAllIssues();
        assertFalse("ON_DEMAND is enabled so the description should be rendered as wiki markup for " + customFieldSearcher, getPageSource().contains("<div>" + customFieldType + "</div>"));

        backdoor.customFields().deleteCustomField(fieldId);
    }

    private void testSingleCustomFieldDescriptionOnIssueNavigatorScreen(List<String> customFieldTypes, String customFieldSearcher)
    {
        for (String customFieldType : customFieldTypes)
        {
            testSingleCustomFieldDescriptionOnIssueNavigatorScreen(customFieldType, customFieldSearcher);
        }
    }

    private void testSingleCustomFieldDescriptionOnIssueNavigatorScreen(String customFieldType, String customFieldSearcher)
    {
        final String fieldId = backdoor.customFields().createCustomField(customFieldType + "-name", fieldDescription(customFieldType), customFieldType, customFieldSearcher);

        // test that the description is rendered as raw HTML
        backdoor.darkFeatures().disableForSite(ON_DEMAND_FEATURE);
        tester.gotoPage("secure/QueryComponentRendererEdit!Default.jspa?fieldId=" + fieldId + "&decorator=none&jqlContext=");
        assertTrue("ON_DEMAND is disabled so the description should be rendered as raw HTML for " + customFieldSearcher, getPageSource().contains(fieldDescriptionHtml(customFieldType)));

        // now test that the description is rendered as Wiki markup
        backdoor.darkFeatures().enableForSite(ON_DEMAND_FEATURE);
        tester.gotoPage("secure/QueryComponentRendererEdit!Default.jspa?fieldId=" + fieldId + "&decorator=none&jqlContext=");
        assertTrue("ON_DEMAND is enabled so the description should be rendered as wiki markup for " + customFieldSearcher, getPageSource().contains(fieldDescriptionWikiFormat(customFieldType)));
        assertFalse("ON_DEMAND is enabled so the description should be rendered as wiki markup for " + customFieldSearcher, getPageSource().contains("<div>" + customFieldType + "</div>"));

        backdoor.customFields().deleteCustomField(fieldId);
    }

    private void testSingleCustomFieldDescriptionOnCustomFieldsScreen(String customFieldType)
    {
        final String fieldId = backdoor.customFields().createCustomField(customFieldType + "-name", fieldDescription(customFieldType), customFieldType, null);

        // test that the description is rendered as raw HTML
        backdoor.darkFeatures().disableForSite(ON_DEMAND_FEATURE);
        goToCustomFields();
        assertTrue("ON_DEMAND is disabled so the description should be rendered as raw HTML for " + customFieldType, getPageSource().contains(fieldDescriptionHtml(customFieldType)));

        // now test that the description is rendered as Wiki markup
        backdoor.darkFeatures().enableForSite(ON_DEMAND_FEATURE);
        goToCustomFields();
        assertTrue("ON_DEMAND is enabled so the description should be rendered as wiki markup for " + customFieldType, getPageSource().contains(fieldDescriptionWikiFormat(customFieldType)));
        assertFalse("ON_DEMAND is enabled so the description should be rendered as wiki markup for " + customFieldType, getPageSource().contains("<div>" + customFieldType + "</div>"));

        backdoor.customFields().deleteCustomField(fieldId);
    }


    private void restoreDataForTest()
    {
        administration.restoreData("TestCustomFieldTitle.xml");

    }

    private static String fieldDescription(String fieldId)
    {
        return String.format(RAW_DESC_TEMPLATE, fieldId);
    }

    private static String fieldDescriptionHtml(String fieldId)
    {
        return String.format(HTML_DESC_TEMPLATE, fieldId);
    }

    private static String fieldDescriptionWikiFormat(String fieldId)
    {
        return String.format(WIKI_DESC_TEMPLATE, fieldId);
    }

    private void goToCustomFields()
    {
        navigation.gotoAdminSection("view_custom_fields");
    }

    private String getPageSource()
    {
        return tester.getDialog().getResponseText();
    }

    private CssLocator locatorForDescription(String fieldId)
    {
        return locator.css(String.format("#custom-fields-%s-name div.description", fieldId));
    }
}
