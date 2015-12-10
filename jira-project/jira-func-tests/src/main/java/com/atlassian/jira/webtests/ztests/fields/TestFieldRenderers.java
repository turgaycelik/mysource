package com.atlassian.jira.webtests.ztests.fields;

import com.atlassian.jira.functest.framework.FuncTestHelperFactory;
import com.atlassian.jira.functest.framework.Navigation;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.Groups;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.atlassian.jira.webtests.ztests.bulk.TestBulkMoveIssues;
import com.meterware.httpunit.HttpUnitOptions;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * A test suite that exercises the renderer framework and implmentations.
 */
@WebTest ({ Category.FUNC_TEST, Category.FIELDS })
public class TestFieldRenderers extends JIRAWebTest
{
    private static final String CUSTOM_FIELD_NAME_TEXT_FIELD = "Custom Field Text Field";
    private static final String CUSTOM_FIELD_NAME_TEXT_AREA_FIELD = "Custom Field Text Area Field";
    private static final String DEFAULT_FIELD_CONFIGURATION = "Default Field Configuration";
    private static final String CUSTOM_FIELD_CONFIGURATION = "Renderer Custom Field Configuration";
    private static final String WIKI_STYLE_RENDERER = "Wiki Style Renderer";
    private static final String DEFAULT_TEXT_RENDERER = "Default Text Renderer";
    private static final String CUSTOM_FIELD_TYPE_AREA = "textarea";
    private static final String CUSTOM_FIELD_CONFIGURATION_SCHEME = "Renderer Custom Field Configuration Scheme";
    private static final String OTHER_PROJECT = "Renderer Test Project";
    private static final String OTHER_PROJECT_KEY = "OTH";

    private String issueKey;
    private String textAreaCustomFieldId;
    private String textCustomFieldId;
    private long projectId;
    private static final String COLOUR_MACRO_KEY = "com.atlassian.jira.plugin.system.renderers.wiki.macros:color";
    private static final String HTML_CODE = "<b>testWikiRendererBadLink</b>";

    protected Navigation navigation;
    private static final String TEXT_AREA_CF_ID = "customfield_10000";
    private static final String TEXT_FIELD_CF_ID = "customfield_10001";
    private static final String COMMENT_FIELD_ID = "comment";
    private static final String DESCRIPTION_ID = "description";
    private static final String ENVIRONMENT_ID = "environment";

    public TestFieldRenderers(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreBlankInstance();
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        if (projectExists(PROJECT_HOMOSAP))
        {
            deleteProject(PROJECT_HOMOSAP);
        }
        projectId = addProject(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, ADMIN_USERNAME);
        setFieldConfigurationFieldToRenderer(DEFAULT_FIELD_CONFIGURATION, COMMENT_FIELD_ID, WIKI_STYLE_RENDERER);
        setFieldConfigurationFieldToRenderer(DEFAULT_FIELD_CONFIGURATION, DESCRIPTION_ID, WIKI_STYLE_RENDERER);
        setFieldConfigurationFieldToRenderer(DEFAULT_FIELD_CONFIGURATION, ENVIRONMENT_ID, WIKI_STYLE_RENDERER);

        textAreaCustomFieldId = createCustomFields(CUSTOM_FIELD_TYPE_AREA, "global", CUSTOM_FIELD_NAME_TEXT_AREA_FIELD, "custom field testing for filter", null, null, null);
        textCustomFieldId = createCustomFields(CUSTOM_FIELD_TYPE_TEXTFIELD, "global", CUSTOM_FIELD_NAME_TEXT_FIELD, "custom field testing for filter", null, null, null);
        addFieldsToFieldScreen("Workflow Screen", new String[] { CUSTOM_FIELD_NAME_TEXT_AREA_FIELD, CUSTOM_FIELD_NAME_TEXT_FIELD });
        addFieldsToFieldScreen("Default Screen", new String[] { CUSTOM_FIELD_NAME_TEXT_AREA_FIELD, CUSTOM_FIELD_NAME_TEXT_FIELD });
        addFieldsToFieldScreen("Resolve Issue Screen", new String[] { CUSTOM_FIELD_NAME_TEXT_AREA_FIELD, CUSTOM_FIELD_NAME_TEXT_FIELD });

        setFieldConfigurationFieldToRenderer(DEFAULT_FIELD_CONFIGURATION, TEXT_AREA_CF_ID, WIKI_STYLE_RENDERER);
        setFieldConfigurationFieldToRenderer(DEFAULT_FIELD_CONFIGURATION, TEXT_FIELD_CF_ID, WIKI_STYLE_RENDERER);

        gotoFieldLayouts();
        clickLink("copy-" + DEFAULT_FIELD_CONFIGURATION);
        setFormElement("fieldLayoutName", CUSTOM_FIELD_CONFIGURATION);
        submit("Copy");

        // create an issue to play around with
        issueKey = addIssue(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, "Bug", "This is a test issue", "Major", null, null, null, ADMIN_FULLNAME, "test environment", "description for this is a test issue", null, null, null);

        // Ensure that attachments are enabled
        administration.attachments().enable();
        grantGlobalPermission(BULK_CHANGE, Groups.USERS);

        final FuncTestHelperFactory funcTestHelperFactory = new FuncTestHelperFactory(tester, getEnvironmentData());
        navigation = funcTestHelperFactory.getNavigation();
    }

    public void tearDown()
    {
        deleteCustomField(textAreaCustomFieldId);
        deleteCustomField(textCustomFieldId);
        setFieldConfigurationFieldToRenderer(DEFAULT_FIELD_CONFIGURATION, COMMENT_FIELD_ID, DEFAULT_TEXT_RENDERER);
        setFieldConfigurationFieldToRenderer(DEFAULT_FIELD_CONFIGURATION, DESCRIPTION_ID, DEFAULT_TEXT_RENDERER);
        setFieldConfigurationFieldToRenderer(DEFAULT_FIELD_CONFIGURATION, ENVIRONMENT_ID, DEFAULT_TEXT_RENDERER);

        gotoFieldLayouts();
        clickLink("delete-" + CUSTOM_FIELD_CONFIGURATION);
        submit("Delete");

        deleteIssue(issueKey);
        deleteProject(PROJECT_HOMOSAP);
        removeGlobalPermission(BULK_CHANGE, Groups.USERS);
        // to ensure that nothing was left behind
        restoreBlankInstance();
        super.tearDown();
    }

    public void testFieldRenderers()
    {
        log("-- begin testFieldRenderers");

        _testWikiRendererVisible();
        _testRendererConfiguration();
        _testRendererPluginConfiguration();
        _testRendererWithBulkOperations();
        _testRendererAlternativeViews();
        _testWikiRendererBadLink();
        _testWikiRendererBadMacro();
        // this should be tested but the support from http unit is not yet present _testWikiPreview();

        log("-- end   testFieldRenderers");
    }

    public void _testRendererAlternativeViews()
    {
        _testRendererExcelView();
        _testRendererRssView();
        _testIssueNavigatorColumnView();
    }

    public void _testIssueNavigatorColumnView()
    {
        try
        {
            gotoIssue(issueKey);
            clickLink("edit-issue");
            setFormElement(DESCRIPTION_ID, "{color:blue}blue functional test text{color}");
            submit("Update");
            assertTextPresent("<font color=\"blue\">blue functional test text</font>");

            addColumnToIssueNavigatorById(new String[] { DESCRIPTION_ID });
            displayAllIssues();
            assertTextPresent("<font color=\"blue\">blue functional test text</font>");
        }
        finally
        {
            restoreColumnDefaults();
        }
    }

    public void _testRendererRssView()
    {
        log("--- begin testRendererRssView");

        // test the issue rss
        try
        {
            gotoIssue(issueKey);
            clickLink("edit-issue");
            setFormElement(DESCRIPTION_ID, "{color:blue}blue functional test text{color}");
            submit("Update");
            assertTextPresent("<font color=\"blue\">blue functional test text</font>");
            clickLinkWithText("XML");
            assertTextPresent("&lt;font color=&quot;blue&quot;&gt;blue functional test text&lt;/font&gt;");
        }
        finally
        {
            // browse back to the application since we are now viewing the xml for the issue
            beginAt("/secure/Dashboard.jspa");
        }

        try
        {
            // test the issue navigator rss
            tester.gotoPage("/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?jqlQuery=&tempMax=1000");
            assertTextPresent("&lt;font color=&quot;blue&quot;&gt;blue functional test text&lt;/font&gt;");
        }
        finally
        {
            // browse back to the application since we are now viewing the xml for the issue
            beginAt("/secure/Dashboard.jspa");
        }

        log("--- end   testRendererRssView");
    }

    public void _testRendererExcelView()
    {
        log("--- begin testRendererExcelView");
        try
        {
            gotoIssue(issueKey);
            clickLink("edit-issue");
            setFormElement(DESCRIPTION_ID, "{color:blue}blue functional test text{color}");
            submit("Update");

            // test the issue navigator rss
            tester.gotoPage("/sr/jira.issueviews:searchrequest-excel-all-fields/temp/SearchRequest.xls?jqlQuery=&tempMax=1000");
            assertTextPresent("{color:blue}blue functional test text{color}");
        }
        finally
        {
            // browse back to the application since we are now viewing the xml for the issue
            beginAt("/secure/Dashboard.jspa");
        }

        log("--- end   testRendererExcelView");
    }

    public void _testRendererWithBulkOperations()
    {
        _testBulkEditWithSameRendererType();
        _testBulkOperationsWithDifferentRendererTypes();
    }

    public void _testBulkOperationsWithDifferentRendererTypes()
    {
        String otherProjectIssueKey = null;
        try
        {
            // now we will create an issue in another project that is linked to another fieldLayoutScheme, give it
            // a different renderer type.
            addFieldLayoutScheme(CUSTOM_FIELD_CONFIGURATION_SCHEME, "random description");
            addProject(OTHER_PROJECT, OTHER_PROJECT_KEY, ADMIN_USERNAME);

            // change the field configuration scheme to use the custom field configuration
            associatedSchemeWithConfiguration(CUSTOM_FIELD_CONFIGURATION_SCHEME, CUSTOM_FIELD_CONFIGURATION);
            associateFieldLayoutScheme(OTHER_PROJECT, null, CUSTOM_FIELD_CONFIGURATION_SCHEME);

            // set the renderer types to different renderer types for the two configurations
            setFieldConfigurationFieldToRenderer(CUSTOM_FIELD_CONFIGURATION, TEXT_AREA_CF_ID, DEFAULT_TEXT_RENDERER);
            setFieldConfigurationFieldToRenderer(DEFAULT_FIELD_CONFIGURATION, TEXT_AREA_CF_ID, WIKI_STYLE_RENDERER);

            setFieldConfigurationFieldToRenderer(CUSTOM_FIELD_CONFIGURATION, TEXT_FIELD_CF_ID, DEFAULT_TEXT_RENDERER);
            setFieldConfigurationFieldToRenderer(DEFAULT_FIELD_CONFIGURATION, TEXT_FIELD_CF_ID, WIKI_STYLE_RENDERER);

            // create an issue to play around with
            otherProjectIssueKey = addIssue(OTHER_PROJECT, OTHER_PROJECT_KEY, "Bug", "This is a test issue", "Major", null, null, null, ADMIN_FULLNAME, "test environment", "description for this is a test issue", null, null, null);
            // set the value on the textArea since some of the validation only kicks in if the field is not empty
            editIssueWithCustomFields(otherProjectIssueKey, textAreaCustomFieldId, "test value", CUSTOM_FIELD_TYPE_TEXTFIELD);

            _testBulkEditWithDifferentRendererTypes();
            _testBulkMoveWithDifferentRendererTypes();
        }
        finally
        {
            // tear down this tests setup
            if (otherProjectIssueKey != null)
            {
                deleteIssue(otherProjectIssueKey);
            }
            if (projectExists(OTHER_PROJECT))
            {
                removeAssociationWithFieldLayoutScheme(OTHER_PROJECT, null, CUSTOM_FIELD_CONFIGURATION_SCHEME);
                deleteProject(OTHER_PROJECT);
            }
            deleteFieldLayoutScheme(CUSTOM_FIELD_CONFIGURATION_SCHEME);
        }
    }

    private void associatedSchemeWithConfiguration(String schemeName, String fieldConfigurationName)
    {
        gotoFieldLayoutSchemes();
        clickLinkWithText(schemeName);
        clickLink("edit_fieldlayoutschemeentity");
        selectOption("fieldConfigurationId", fieldConfigurationName);
        submit("Update");
    }

    public void _testBulkMoveWithDifferentRendererTypes()
    {
        log("--- begin testBulkMoveWithDifferentRendererTypes");

        try
        {
            // we will test that when the renderer type is different for where you are going
            // that you will receive a warning message.
            displayAllIssues();
            bulkChangeIncludeAllPages();
            // Only turn this on so that the wiki renderer stuff will showup
            HttpUnitOptions.setScriptingEnabled(true);
            bulkChangeChooseIssuesAll();
            isStepChooseOperation();
            chooseOperationBulkMove();

            selectOption(projectId + "_1_pid", PROJECT_HOMOSAP);
            checkCheckbox(TestBulkMoveIssues.SAME_FOR_ALL, projectId + "_1_");
            navigation.clickOnNext();

            // check that there is a warning for the text area custom field and not for the text field
            assertTextPresent("warning-" + CUSTOM_FIELD_NAME_TEXT_AREA_FIELD);
            assertTextNotPresent("warning-" + CUSTOM_FIELD_NAME_TEXT_FIELD);

            bulkChangeCancel();
        }
        finally
        {
            HttpUnitOptions.setScriptingEnabled(false);
        }
        log("--- end   testBulkMoveWithDifferentRendererTypes");
    }

    public void _testBulkEditWithDifferentRendererTypes()
    {
        log("--- begin testBulkEditWithDifferentRendererTypes");

        try
        {
            // confirm that we can not bulk edit the field.
            displayAllIssues();
            bulkChangeIncludeAllPages();
            // Only turn this on so that the wiki renderer stuff will showup
            HttpUnitOptions.setScriptingEnabled(true);
            bulkChangeChooseIssuesAll();
            isStepChooseOperation();
            bulkChangeChooseOperationEdit();

            // this could check a bit better that the warning message is for the specific field
            assertTextPresent("This field has inconsistent renderer types for the project(s) of the selected issues.");

            bulkChangeCancel();
        }
        finally
        {
            HttpUnitOptions.setScriptingEnabled(false);
        }
        log("--- end   testBulkEditWithDifferentRendererTypes");
    }

    public void _testBulkEditWithSameRendererType()
    {
        log("--- begin testBulkEditWithSameRendererType");

        try
        {
            // test that we can correctly edit a custom field value through bulk edit
            displayAllIssues();
            bulkChangeIncludeAllPages();
            // Only turn this on so that the wiki renderer stuff will showup
            HttpUnitOptions.setScriptingEnabled(true);
            bulkChangeChooseIssuesAll();
            isStepChooseOperation();
            bulkChangeChooseOperationEdit();

            // do the validation we came here to do, we pass the existing issue key because
            // for bulk edit the first selected issue will be the key if all renderer types match
            validateWikiRendererForField(CUSTOM_FIELD_PREFIX + textAreaCustomFieldId, CUSTOM_FIELD_NAME_TEXT_AREA_FIELD, issueKey);
            validateWikiRendererForField(CUSTOM_FIELD_PREFIX + textCustomFieldId, CUSTOM_FIELD_NAME_TEXT_FIELD, issueKey);

            bulkChangeCancel();
        }
        finally
        {
            HttpUnitOptions.setScriptingEnabled(false);
        }
        log("--- end   testBulkEditWithSameRendererType");
    }

    private void _testRendererPluginConfiguration()
    {
        _testTextRendererDoesNotAllowDisable();
        _testWikiRendererDisabled();
        _testWikiRendererMacroDisabled();
    }

    public void _testWikiRendererMacroDisabled()
    {

        log("--- begin testWikiRendererMacroDisabled");
        try
        {
            gotoIssue(issueKey);
            clickLink("edit-issue");
            setFormElement(DESCRIPTION_ID, "{color:blue}blue functional test text{color}");
            submit("Update");
            assertTextPresent("<font color=\"blue\">blue functional test text</font>");

            administration.plugins().disablePluginModule("com.atlassian.jira.plugin.system.renderers.wiki.macros","com.atlassian.jira.plugin.system.renderers.wiki.macros:color");

            gotoIssue(issueKey);
            assertTextPresent("{color:blue}blue functional test text{color}");

        }
        finally
        {
            administration.plugins().enablePluginModule("com.atlassian.jira.plugin.system.renderers.wiki.macros","com.atlassian.jira.plugin.system.renderers.wiki.macros:color");
        }
        log("--- end   testWikiRendererMacroDisabled");
    }

    public void _testTextRendererDoesNotAllowDisable()
    {
        log("--- begin _testTextRendererDoesNotAllowDisable");
        assertFalse(administration.plugins().canDisablePluginModule("com.atlassian.jira.plugin.system.jirarenderers","com.atlassian.jira.plugin.system.jirarenderers:jira-text-renderer"));
        log("--- end   _testTextRendererDoesNotAllowDisable");
    }

    public void _testWikiRendererDisabled()
    {
        log("--- begin testWikiRendererDisabled");
        administration.plugins().disablePluginModule("com.atlassian.jira.plugin.system.jirarenderers","com.atlassian.jira.plugin.system.jirarenderers:atlassian-wiki-renderer");

        // now wander off to the edit screen and make sure the text renderer is used and that we have a warninig
        gotoIssue(issueKey);
        clickLink("edit-issue");
        validateNoWikiRendererForField(DESCRIPTION_ID, "Description", issueKey);
        assertTextPresent("This field is configured to use the \"atlassian-wiki-renderer\" which is not currently available, using \"Default Text Renderer\" instead.");
        // turn the renderer back on
        administration.plugins().enablePluginModule("com.atlassian.jira.plugin.system.jirarenderers","com.atlassian.jira.plugin.system.jirarenderers:atlassian-wiki-renderer");
        log("--- end   testWikiRendererDisabled");
    }

    public void _testRendererConfiguration()
    {
        _testRendererConfigurationWarningMessage();
        _testRendererConfigurationChangeRendererType();
        _testCustomLayoutRendererConfiguration();
    }

    public void _testCustomLayoutRendererConfiguration()
    {
        log("--- begin testCustomLayoutRendererConfiguration");

        // Make sure configuring renderers on a layout other than the default works
        // this will also test that the warning does not show up on the edit pages where
        // there are not effected issues, this is the only safe place for this test since we
        // now that there are no issues associated with this layout yet.

        setFieldConfigurationFieldToRenderer(CUSTOM_FIELD_CONFIGURATION, DESCRIPTION_ID, WIKI_STYLE_RENDERER, true);
        setFieldConfigurationFieldToRenderer(CUSTOM_FIELD_CONFIGURATION, DESCRIPTION_ID, DEFAULT_TEXT_RENDERER, true);
        log("--- end   testCustomLayoutRendererConfiguration");
    }

    public void _testRendererConfigurationChangeRendererType()
    {
        log("--- begin testRendererConfigurationChangeRendererType");
        try
        {
            HttpUnitOptions.setScriptingEnabled(true);
            setFieldConfigurationFieldToRenderer(DEFAULT_FIELD_CONFIGURATION, COMMENT_FIELD_ID, DEFAULT_TEXT_RENDERER);
            gotoIssue(issueKey);
            validateNoWikiRendererForField(COMMENT_FIELD_ID, "Comment", issueKey);
            setFieldConfigurationFieldToRenderer(DEFAULT_FIELD_CONFIGURATION, COMMENT_FIELD_ID, WIKI_STYLE_RENDERER);
        }
        finally
        {
            HttpUnitOptions.setScriptingEnabled(false);
        }
        log("--- end   testRendererConfigurationChangeRendererType");
    }

    public void _testRendererConfigurationWarningMessage()
    {
        log("--- begin testRendererConfigurationWarningMessage");
        gotoFieldLayoutConfiguration(DEFAULT_FIELD_CONFIGURATION);
        clickLink("renderer_description");
        assertTextPresent("Edit Field Renderer");
        assertTextPresent("A renderer determines how the value of a field will be displayed within the system.");
        log("--- end   testRendererConfigurationWarningMessage");
    }

    // this will not work since it seems httpunit does not support ajax style requests.
    public void _testWikiPreview()
    {
        log("--- begin testWikiPreview");
        try
        {
            // turn on javascript so we can check the comment field on the view issue screen
            HttpUnitOptions.setScriptingEnabled(true);
            gotoIssue(issueKey);
            clickLink("edit-issue");

            setFormElement(DESCRIPTION_ID, ":)");
            clickLink("description-preview_link");
            assertTextPresent("<p><img class=\"emoticon\" src=\"http://localhost:8080/images/icons/emoticons/smile.gif\" alt=\"\" align=\"middle\" border=\"0\" height=\"20\" width=\"20\"></p>");
        }
        finally
        {
            HttpUnitOptions.setScriptingEnabled(false);
        }
        log("--- end   testWikiPreview");
    }

    public void _testWikiRendererVisible()
    {
        _testWikiRendererVisibleOnViewIssue();
        _testWikiRendererVisibleOnEditIssue();
        _testWikiRendererVisibleOnCreateIssue();
        _testWikiRendererVisibleOnAssignIssue();
        _testWikiRendererVisibleOnAttachFile();
        _testWikiRendererVisibleOnLinkIssue();
    }

    public void _testWikiRendererVisibleOnLinkIssue()
    {
        log("--- begin testWikiRendererVisibleOnLinkIssue");
        try
        {
            createIssueLinkType();
            // turn on javascript so we can check the comment field on the view issue screen
            HttpUnitOptions.setScriptingEnabled(true);
            gotoIssue(issueKey);
            clickLink("link-issue");
            validateWikiRendererForField(COMMENT_FIELD_ID, "Comment", issueKey);
        }
        finally
        {
            HttpUnitOptions.setScriptingEnabled(false);
            deactivateIssueLinking();
        }
        log("--- end   testWikiRendererVisibleOnLinkIssue");
    }

    public void _testWikiRendererVisibleOnAssignIssue()
    {
        log("--- begin testWikiRendererVisibleOnAttachFile");
        try
        {
            // turn on javascript so we can check the comment field on the view issue screen
            HttpUnitOptions.setScriptingEnabled(true);
            gotoIssue(issueKey);
            clickLink("assign-issue");
            validateWikiRendererForField(COMMENT_FIELD_ID, "Comment", issueKey);
        }
        finally
        {
            HttpUnitOptions.setScriptingEnabled(false);
        }
        log("--- end   testWikiRendererVisibleOnAttachFile");
    }

    public void _testWikiRendererVisibleOnAttachFile()
    {
        log("--- begin testWikiRendererVisibleOnAssignIssue");
        try
        {
            // turn on javascript so we can check the comment field on the view issue screen
            HttpUnitOptions.setScriptingEnabled(true);
            gotoIssue(issueKey);
            clickLink("attach-file");
            validateWikiRendererForField(COMMENT_FIELD_ID, "Comment", issueKey);
        }
        finally
        {
            HttpUnitOptions.setScriptingEnabled(false);
        }
        log("--- end   testWikiRendererVisibleOnAssignIssue");
    }

    public void _testWikiRendererVisibleOnCreateIssue()
    {
        log("--- begin testWikiRendererVisibleOnCreateIssue");
        try
        {
            HttpUnitOptions.setScriptingEnabled(true);
            navigation.issue().goToCreateIssueForm(PROJECT_HOMOSAP,"Bug");
            assertTextPresent("CreateIssueDetails.jspa");

            validateWikiRendererForField(DESCRIPTION_ID, "Description", "");
            validateWikiRendererForField(ENVIRONMENT_ID, "Environment", "");
            validateWikiRendererForField(CUSTOM_FIELD_PREFIX + textAreaCustomFieldId, CUSTOM_FIELD_NAME_TEXT_AREA_FIELD, "");
            validateWikiRendererForField(CUSTOM_FIELD_PREFIX + textCustomFieldId, CUSTOM_FIELD_NAME_TEXT_FIELD, "");
        }
        finally
        {
            HttpUnitOptions.setScriptingEnabled(false);
        }
        log("--- end   testWikiRendererVisibleOnCreateIssue");
    }

    public void _testWikiRendererVisibleOnEditIssue()
    {
        log("--- begin testWikiRendererVisibleOnEditIssue");
        try
        {
            // turn on javascript so we can check the comment field on the view issue screen
            HttpUnitOptions.setScriptingEnabled(true);
            gotoIssue(issueKey);
            clickLink("edit-issue");

            validateWikiRendererForField(COMMENT_FIELD_ID, "Comment", issueKey);
            validateWikiRendererForField(DESCRIPTION_ID, "Description", issueKey);
            validateWikiRendererForField(ENVIRONMENT_ID, "Environment", issueKey);
            validateWikiRendererForField(CUSTOM_FIELD_PREFIX + textAreaCustomFieldId, CUSTOM_FIELD_NAME_TEXT_AREA_FIELD, issueKey);
            validateWikiRendererForField(CUSTOM_FIELD_PREFIX + textCustomFieldId, CUSTOM_FIELD_NAME_TEXT_FIELD, issueKey);
        }
        finally
        {
            HttpUnitOptions.setScriptingEnabled(false);
        }
        log("--- end   testWikiRendererVisibleOnEditIssue");
    }

    public void _testWikiRendererVisibleOnViewIssue()
    {
        log("--- begin testWikiRendererVisibleOnViewIssue");
        try
        {
            // turn on javascript so we can check the comment field on the view issue screen
            HttpUnitOptions.setScriptingEnabled(true);
            gotoIssue(issueKey);
            validateWikiRendererForField(COMMENT_FIELD_ID, "Comment", issueKey);
        }
        finally
        {
            HttpUnitOptions.setScriptingEnabled(false);
        }
        log("--- end   testWikiRendererVisibleOnViewIssue");
    }

    /**
     * When a link using [square brackets] fails to render, the contents of the
     * link are printed literally, creating a vulnerability.
     */
    public void _testWikiRendererBadLink()
    {
        // test correct use of link
        assertValidIssueDescription("[" + HTML_CODE + "|http://www.google.com/]", HTML_CODE);
        // test incorrect use of link
        assertValidIssueDescription("[" + HTML_CODE + "]", HTML_CODE);
    }

    /**
     * When a macro fails to render, the contents of the macro tags is printed literally
     * creating a vulnerability. (See JRA-9090)
     */
    public void _testWikiRendererBadMacro()
    {
        // test valid macro
        assertValidIssueDescription("{code}" + HTML_CODE + "{code}", HTML_CODE);
        // test invalid macro
        assertValidIssueDescription("{noSuchMacro}" + HTML_CODE + "{noSuchMacro}", HTML_CODE);
        try
        {
            administration.plugins().disablePluginModule("com.atlassian.jira.plugin.system.renderers.wiki.macros","com.atlassian.jira.plugin.system.renderers.wiki.macros:color");
            assertValidIssueDescription("{color:orange}" + HTML_CODE + "{color:orange}", HTML_CODE);
        }
        finally
        {
            administration.plugins().enablePluginModule("com.atlassian.jira.plugin.system.renderers.wiki.macros","com.atlassian.jira.plugin.system.renderers.wiki.macros:color");
        }

    }

    /**
     * Creates an issue with the given description and asserts the given text is not present.
     *
     * @param descriptionToEnter
     * @param assertNotPresent
     */
    private void assertValidIssueDescription(String descriptionToEnter, String assertNotPresent)
    {
        String issueKey = addIssue(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, "Bug", "testing cross site scripting", "Major", null, null, null, ADMIN_FULLNAME, "test environment", descriptionToEnter, null, null, null);
        gotoIssue(issueKey);
        assertTextNotPresent(assertNotPresent);
    }

    // Create an issue link type 'Duplicate' - should be only called once!
    private void createIssueLinkType()
    {
        activateIssueLinking();
        setFormElement("name", "Duplicate");
        setFormElement("outward", "is a duplicate of");
        setFormElement("inward", "duplicates");
        submit();
    }

    private void validateNoWikiRendererForField(String field, String fieldDisplayName, String issueKey)
    {
        assertTextNotPresent("<dd>" + field + "-preview_link</dd>");
        assertTextNotPresent("<dd>" + fieldDisplayName + "</dd>");
        assertTextNotPresent("<dd>" + issueKey + "</dd>");
    }

    private void validateWikiRendererForField(String field, String fieldDisplayName, String issueKey)
    {
        assertTextPresent("<dd>" + field + "-preview_link</dd>");
        assertTextPresent("<dd>" + fieldDisplayName + "</dd>");
        assertTextPresent("<dd>" + issueKey + "</dd>");
    }

}
