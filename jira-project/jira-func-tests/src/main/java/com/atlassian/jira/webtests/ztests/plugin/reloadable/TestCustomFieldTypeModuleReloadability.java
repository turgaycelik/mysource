package com.atlassian.jira.webtests.ztests.plugin.reloadable;

import com.atlassian.jira.functest.framework.Splitable;
import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.navigation.BulkChangeWizard;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import org.apache.commons.lang.ArrayUtils;

import java.util.Collections;

/**
 * Test that the 'custom field type' plugin module type behaves correctly when reloaded.
 *
 * @since v4.3
 */
@Splitable
@WebTest ({ Category.FUNC_TEST, Category.RELOADABLE_PLUGINS, Category.REFERENCE_PLUGIN, Category.SLOW_IMPORT})
public class TestCustomFieldTypeModuleReloadability extends AbstractCustomFieldPluginTest
{
    private static final String TEST_FIELD_INSTANCE_NAME = "Test reference field";
    private static final String DEFAULT_FIELD_VALUE = "3";
    private static final String NONE_OPTION = "None";
    private static final String[] ALL_FIELD_VALUES = {"1", "2", DEFAULT_FIELD_VALUE, "4", "5"};
    private static final String[] ALL_VISIBLE_FIELD_VALUES = (String[]) ArrayUtils.add(ALL_FIELD_VALUES, 0, NONE_OPTION);

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
    }

    public void testShouldNotBeVisibleInTheAdminSectionGivenReferencePluginNotEnabled() throws Exception
    {
        navigation.gotoCustomFields();
        tester.clickLink("add_custom_fields");
        tester.assertRadioOptionNotPresent("fieldType", ReferencePluginConstants.SELECT_CUSTOM_FIELD_TYPE_KEY);
    }

    public void testShouldBeVisibleInTheAdminSection() throws Exception
    {
        administration.plugins().referencePlugin().enable();
        navigation.gotoCustomFields();
        tester.clickLink("add_custom_fields");
        tester.setWorkingForm("jiraform");
        tester.assertRadioOptionPresent("fieldType", ReferencePluginConstants.SELECT_CUSTOM_FIELD_TYPE_KEY);
    }

    public void testShouldBeVisibleInTheCreateIssueScreen() throws Exception
    {
        administration.plugins().referencePlugin().enable();
        setUpCustomFieldInstance(ReferencePluginConstants.SELECT_CUSTOM_FIELD_TYPE_KEY, TEST_FIELD_INSTANCE_NAME, DEFAULT_FIELD_VALUE, ALL_FIELD_VALUES);
        navigation.issue().goToCreateIssueForm(PROJECT_HOMOSAP, ISSUE_TYPE_BUG);
        text.assertTextPresent(editIssueCustomFieldLabelLocator(), TEST_FIELD_INSTANCE_NAME);
        tester.assertOptionEquals(customFieldId, DEFAULT_FIELD_VALUE);
        tester.assertOptionsEqual(customFieldId, ALL_VISIBLE_FIELD_VALUES);
    }

    public void testShouldBeVisibleInTheViewIssueScreen() throws Exception
    {
        administration.plugins().referencePlugin().enable();
        setUpCustomFieldInstance(ReferencePluginConstants.SELECT_CUSTOM_FIELD_TYPE_KEY, TEST_FIELD_INSTANCE_NAME, DEFAULT_FIELD_VALUE, ALL_FIELD_VALUES);
        navigation.issue().gotoIssue(createTestIssue("4"));
        text.assertTextSequence(viewIssueCustomFieldRowLocator(), TEST_FIELD_INSTANCE_NAME, "4");
    }

    public void testShouldBeVisibleInTheEditIssueScreen() throws Exception
    {
        administration.plugins().referencePlugin().enable();
        setUpCustomFieldInstance(ReferencePluginConstants.SELECT_CUSTOM_FIELD_TYPE_KEY, TEST_FIELD_INSTANCE_NAME, DEFAULT_FIELD_VALUE, ALL_FIELD_VALUES);
        navigation.issue().gotoEditIssue(createTestIssue("2"));
        text.assertTextPresent(editIssueCustomFieldLabelLocator(), TEST_FIELD_INSTANCE_NAME);
        tester.assertOptionEquals(customFieldId, "2");
        tester.assertOptionsEqual(customFieldId, ALL_VISIBLE_FIELD_VALUES);
    }

    public void testShouldBeVisibleInBulkEdit() throws Exception
    {
        administration.plugins().referencePlugin().enable();
        setUpCustomFieldInstance(ReferencePluginConstants.SELECT_CUSTOM_FIELD_TYPE_KEY, TEST_FIELD_INSTANCE_NAME, DEFAULT_FIELD_VALUE, ALL_FIELD_VALUES);
        createTestIssue("5");
        goToBulkEditOperationDetails();
        text.assertTextPresent(bulkEditIssueCustomFieldLabelLocator(), TEST_FIELD_INSTANCE_NAME);
        tester.assertOptionEquals(customFieldId, DEFAULT_FIELD_VALUE);
        tester.assertOptionsEqual(customFieldId, ALL_VISIBLE_FIELD_VALUES);
    }

    public void testShouldBeVisibleInConfigureDefaultScreen() throws Exception
    {
        administration.plugins().referencePlugin().enable();
        setUpCustomFieldInstance(ReferencePluginConstants.SELECT_CUSTOM_FIELD_TYPE_KEY, TEST_FIELD_INSTANCE_NAME, DEFAULT_FIELD_VALUE, ALL_FIELD_VALUES);
        createTestIssue("1");
        administration.viewFieldScreens().goTo().configureScreen("Default Screen");
        text.assertTextPresent(configurationFieldTableLocator(), TEST_FIELD_INSTANCE_NAME);
    }

    public void testShouldBeVisibleInConfigureResolveIssueScreen() throws Exception
    {
        administration.plugins().referencePlugin().enable();
        setUpCustomFieldInstance(ReferencePluginConstants.SELECT_CUSTOM_FIELD_TYPE_KEY, TEST_FIELD_INSTANCE_NAME, DEFAULT_FIELD_VALUE, ALL_FIELD_VALUES);
        createTestIssue("3");
        administration.viewFieldScreens().goTo().configureScreen("Resolve Issue Screen");
        text.assertTextPresent(configureScreenSelectFieldLocator(), TEST_FIELD_INSTANCE_NAME);
    }

    public void testShouldBeVisibleInConfigureWorkflowScreen() throws Exception
    {
        administration.plugins().referencePlugin().enable();
        setUpCustomFieldInstance(ReferencePluginConstants.SELECT_CUSTOM_FIELD_TYPE_KEY, TEST_FIELD_INSTANCE_NAME, DEFAULT_FIELD_VALUE, ALL_FIELD_VALUES);
        createTestIssue("3");
        administration.viewFieldScreens().goTo().configureScreen("Workflow Screen");
        text.assertTextPresent(configureScreenSelectFieldLocator(), TEST_FIELD_INSTANCE_NAME);
    }

    public void testShouldBeVisibleInIssueNavigatorColumns() throws Exception
    {
        administration.plugins().referencePlugin().enable();
        setUpCustomFieldInstance(ReferencePluginConstants.SELECT_CUSTOM_FIELD_TYPE_KEY, TEST_FIELD_INSTANCE_NAME, DEFAULT_FIELD_VALUE, ALL_FIELD_VALUES);
        createTestIssue("3");
        navigation.issueNavigator().displayAllIssues();
        navigation.issueNavigator().goToConfigureColumns();
        text.assertTextPresent(configureColumnsSelectFieldLocator(), TEST_FIELD_INSTANCE_NAME);
    }

    public void testShouldBeVisibleInFieldConfigurations() throws Exception
    {
        administration.plugins().referencePlugin().enable();
        setUpCustomFieldInstance(ReferencePluginConstants.SELECT_CUSTOM_FIELD_TYPE_KEY, TEST_FIELD_INSTANCE_NAME, DEFAULT_FIELD_VALUE, ALL_FIELD_VALUES);
        administration.fieldConfigurations().defaultFieldConfiguration();
        text.assertTextPresent(configurationFieldTableLocator(), TEST_FIELD_INSTANCE_NAME);
    }

    public void testCustomFieldShouldNotBeAccessibleAfterDisablingThePluginModule() throws Exception
    {
        administration.plugins().referencePlugin().enable();
        setUpCustomFieldInstance(ReferencePluginConstants.SELECT_CUSTOM_FIELD_TYPE_KEY, TEST_FIELD_INSTANCE_NAME, DEFAULT_FIELD_VALUE, ALL_FIELD_VALUES);
        administration.plugins().referencePlugin().disableModule(ReferencePluginConstants.SELECT_CUSTOM_FIELD_TYPE_KEY);
        _assertNotVisibleInFieldConfigurations();
        _assertNotVisibleInTheCreateIssueScreen();
        _assertNotVisibleInTheViewIssueScreen();
        _assertNotVisibleInTheEditIssueScreen();
        _assertNotVisibleInBulkEdit();
        _assertNotVisibleInConfigureDefaultScreen();
        _assertNotVisibleInConfigureResolveIssueScreen();
        _assertNotVisibleInConfigureWorkflowScreen();
        _assertNotVisibleInIssueNavigatorColumns();
    }

    public void testCustomFieldModuleReloadability() throws Exception
    {
        administration.plugins().referencePlugin().enable();
        setUpCustomFieldInstance(ReferencePluginConstants.SELECT_CUSTOM_FIELD_TYPE_KEY, TEST_FIELD_INSTANCE_NAME, DEFAULT_FIELD_VALUE, ALL_FIELD_VALUES);
        administration.plugins().referencePlugin().disableModule(ReferencePluginConstants.SELECT_CUSTOM_FIELD_TYPE_KEY);
        _assertNotVisibleInTheCreateIssueScreen();
        administration.plugins().referencePlugin().enableModule(ReferencePluginConstants.SELECT_CUSTOM_FIELD_TYPE_KEY);
        administration.fieldConfigurations().defaultFieldConfiguration();
        text.assertTextPresent(configurationFieldTableLocator(), TEST_FIELD_INSTANCE_NAME);
    }

    private void _assertNotVisibleInTheCreateIssueScreen()
    {
        navigation.issue().goToCreateIssueForm(PROJECT_HOMOSAP, ISSUE_TYPE_BUG);
        text.assertTextNotPresent(editIssueCustomFieldLabelLocator(), TEST_FIELD_INSTANCE_NAME);
    }

    private void _assertNotVisibleInTheViewIssueScreen()
    {
        navigation.issue().gotoIssue(createTestIssue("4"));
        text.assertTextNotPresent(viewIssueCustomFieldRowLocator(), TEST_FIELD_INSTANCE_NAME);
    }

    private void _assertNotVisibleInTheEditIssueScreen()
    {
        navigation.issue().gotoEditIssue(createTestIssue("2"));
        text.assertTextNotPresent(editIssueCustomFieldLabelLocator(), TEST_FIELD_INSTANCE_NAME);
    }

    private void _assertNotVisibleInBulkEdit()
    {
        createTestIssue("5");
        goToBulkEditOperationDetails();
        text.assertTextNotPresent(bulkEditIssueCustomFieldLabelLocator(), TEST_FIELD_INSTANCE_NAME);
    }

    private void _assertNotVisibleInConfigureDefaultScreen()
    {
        createTestIssue("1");
        administration.viewFieldScreens().goTo().configureScreen("Default Screen");
        text.assertTextNotPresent(configurationFieldTableLocator(), TEST_FIELD_INSTANCE_NAME);
    }

    private void _assertNotVisibleInConfigureResolveIssueScreen()
    {
        createTestIssue("3");
        administration.viewFieldScreens().goTo().configureScreen("Resolve Issue Screen");
        text.assertTextNotPresent(configureScreenSelectFieldLocator(), TEST_FIELD_INSTANCE_NAME);
    }

    private void _assertNotVisibleInConfigureWorkflowScreen()
    {
        createTestIssue("3");
        administration.viewFieldScreens().goTo().configureScreen("Workflow Screen");
        text.assertTextNotPresent(configureScreenSelectFieldLocator(), TEST_FIELD_INSTANCE_NAME);
    }

    private void _assertNotVisibleInIssueNavigatorColumns() throws Exception
    {
        createTestIssue("3");
        navigation.issueNavigator().displayAllIssues();
        navigation.issueNavigator().goToConfigureColumns();
        text.assertTextNotPresent(configureColumnsSelectFieldLocator(), TEST_FIELD_INSTANCE_NAME);
    }


    private void _assertNotVisibleInFieldConfigurations()
    {
        administration.fieldConfigurations().defaultFieldConfiguration();
        text.assertTextNotPresent(configurationFieldTableLocator(), TEST_FIELD_INSTANCE_NAME);
    }

    private String createTestIssue(String customFieldValue)
    {
        return navigation.issue().createIssue(PROJECT_HOMOSAP, ISSUE_TYPE_BUG, "Test summary",
                Collections.singletonMap(customFieldId, new String[] { customFieldValue }));
    }

    private void goToBulkEditOperationDetails()
    {
        navigation.issueNavigator().displayAllIssues();
        BulkChangeWizard bulkChange = navigation.issueNavigator().bulkChange(IssueNavigatorNavigation.BulkChangeOption.ALL_PAGES);
        bulkChange.selectAllIssues().chooseOperation(BulkChangeWizard.BulkOperationsImpl.EDIT);
    }

    private CssLocator editIssueCustomFieldLabelLocator()
    {
        return locator.css("label[for=" + customFieldId +"]");
    }

    private Locator viewIssueCustomFieldRowLocator()
    {
        return locator.id("rowForcustomfield_" + customFieldNumericId);
    }

    private Locator bulkEditIssueCustomFieldLabelLocator()
    {
        return locator.css("label[for=cb" + customFieldId +"]");
    }

    private Locator configurationFieldTableLocator()
    {
        return locator.id(configurationFieldTableId());
    }

    private String configurationFieldTableId()
    {
        return "field_table";
    }

    private Locator configureScreenSelectFieldLocator()
    {
        return locator.css("select[name=fieldId]");
    }

    private Locator configureColumnsSelectFieldLocator()
    {
        return locator.id("issue-nav-add-columns-column-select");
    }
}
