package com.atlassian.jira.webtests.ztests.fields;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

import com.google.common.collect.Lists;
import com.meterware.httpunit.WebTable;
import com.opensymphony.util.TextUtils;

import org.hamcrest.Matchers;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;

import static com.atlassian.jira.functest.framework.NavigationImpl.PROJECT_PLUGIN_PREFIX;
import static org.junit.Assert.assertThat;

@WebTest ({ Category.FUNC_TEST, Category.FIELDS, Category.SCHEMES })
public class TestFieldConfigurationSchemes extends JIRAWebTest
{
    private static final String FIELD_CONFIGURATION_NAME_ONE = "Test Field Configuration One";

    public TestFieldConfigurationSchemes(String name)
    {
        super(name);
    }

    //JRADEV-10348: The associate page did not correctly link to the create "Field Configuration Scheme" page.
    public void testAssociateFieldConfigurationSchemeWhenNoSchemeExists()
    {
        final String name = "New testAssociateFieldLayoutSchemeWhenNoSchemeExists Scheme";

        administration.restoreBlankInstance();
        tester.gotoPage("/secure/admin/SelectFieldLayoutScheme!default.jspa?projectId=10000");
        tester.clickLink("add-new-scheme");
        tester.setFormElement("fieldLayoutSchemeName", name);
        tester.submit("Add");

        tester.assertLinkPresent("add-issue-type-field-configuration-association");
        assertEquals(name, locator.xpath("//span[@data-scheme-field='name']").getText());
    }

    public void testFieldConfigurationSchemes()
    {
		administration.restoreBlankInstance();
        if (projectExists(PROJECT_HOMOSAP))
        {
            log("Project '" + PROJECT_HOMOSAP + "' exists");
            if (!(componentExists(COMPONENT_NAME_ONE, PROJECT_HOMOSAP)))
            {
                addComponent(PROJECT_HOMOSAP, COMPONENT_NAME_ONE);
            }
            if (!(versionExists(VERSION_NAME_ONE, PROJECT_HOMOSAP)))
            {
                addVersion(PROJECT_HOMOSAP, VERSION_NAME_ONE, "Version 1");
            }
        }
        else
        {
            addProject(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, ADMIN_USERNAME);
            addComponent(PROJECT_HOMOSAP, COMPONENT_NAME_ONE);
            addVersion(PROJECT_HOMOSAP, VERSION_NAME_ONE, "Version 1");
        }

        if (projectExists(PROJECT_NEO))
        {
            log("Project '" + PROJECT_NEO + "' exists");
        }
        else
        {
            addProject(PROJECT_NEO, PROJECT_NEO_KEY, ADMIN_USERNAME);
        }

        if (projectExists(PROJECT_MONKEY))
        {
            log("Project '" + PROJECT_MONKEY + "' exists");
        }
        else
        {
            addProject(PROJECT_MONKEY, PROJECT_MONKEY_KEY, ADMIN_USERNAME);
        }

        if (fieldSchemeExists(FIELD_SCHEME_NAME))
        {
            deleteFieldLayoutScheme(FIELD_SCHEME_NAME);
        }

        resetFields();
        String issueKey = addIssue(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, "Bug", "test for field layout schemes", "Major", null, null, null, ADMIN_FULLNAME, "test environment 2", "test description for field layout schemes", null, null, null);

        fieldSchemesAddScheme();
        fieldSchemesAddDuplicateScheme();
        fieldSchemesAddInvalidScheme();
        fieldSchemesAssociateWithProject();

        fieldSchemesCreateIssueWithFieldConfigurationSchemeHidden();
        fieldSchemesCreateIssueWithFieldLayoutSchemeRequired();

        checkNavigatorFields();
        checkProjectTabPanels();

        fieldSchemesEditIssueWithFieldConfigurationSchemeHidden(issueKey);
        fieldSchemesEditIssueWithFieldLayoutSchemeRequired(issueKey);

        String issueKey2 = addIssue(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, "Bug", "test for field layout schemes", "Major", new String[]{COMPONENT_NAME_ONE}, new String[]{VERSION_NAME_ONE}, new String[]{VERSION_NAME_ONE}, ADMIN_FULLNAME, "test environment 2", "test description for field layout schemes", null, null, null);
        fieldSchemeMoveIssueWithFieldSchemeHidden(issueKey);
        fieldSchemeMoveIssueWithFieldSchemeRequired(issueKey);

        fieldSchemeCreateSubTaskWithFieldSchemeHidden(issueKey);
        fieldSchemeCreateSubTaskWithFieldSchemeRequired(issueKey);

        fieldSchemesDeleteScheme();

        deleteIssue(issueKey);
        deleteIssue(issueKey2);
    }

    public void fieldSchemesAddScheme()
    {
        log("Field Configuration Scheme: Adding a scheme");
        addFieldLayoutScheme(FIELD_SCHEME_NAME, FIELD_SCHEME_DESC);
        assertTextPresent(FIELD_SCHEME_NAME);
    }

    public void fieldSchemesDeleteScheme()
    {
        log("Field Configuration Scheme: Deleting a scheme");
        deleteFieldLayoutScheme(FIELD_SCHEME_NAME);
        assertLinkNotPresentWithText(FIELD_SCHEME_NAME);
        assertLinkNotPresentWithText(FIELD_SCHEME_DESC);
//        addFieldLayoutScheme(FIELD_SCHEME_NAME, FIELD_SCHEME_DESC);
//        assertLinkPresentWithText(FIELD_SCHEME_NAME);
    }

    /**
     * Tests the error handling if a duplicate scheme is made
     */
    public void fieldSchemesAddDuplicateScheme()
    {
        log("Field Configuration Scheme: Adding a scheme with a duplicate name");
        addFieldLayoutScheme(FIELD_SCHEME_NAME, FIELD_SCHEME_DESC);
        assertTextPresent("A field configuration scheme with this name already exists.");
    }

    /**
     * Tests the error handling if a scheme with an invalid name is made
     */
    public void fieldSchemesAddInvalidScheme()
    {
        log("Field Configuration Scheme: Adding a scheme with a duplicate name");
        addFieldLayoutScheme("", "");
        assertTextPresent("The field configuration scheme name must not be empty.");
    }

    /**
     * Tests the ability to associate a field scheme to an issue type in a project
     */
    public void fieldSchemesAssociateWithProject()
    {
        log("Field Configuration Scheme: associate a scheme to an issue type in a project");
        associateFieldLayoutScheme(PROJECT_NEO, "Bug", FIELD_SCHEME_NAME);
        assertTextPresent(FIELD_SCHEME_NAME);
        removeAssociationWithFieldLayoutScheme(PROJECT_NEO, "Bug", FIELD_SCHEME_NAME);
        assertTextNotPresent(FIELD_SCHEME_NAME);
    }

    /**
     * Tests the functionality of Field Layout Schemes for 'Create Issue' using 'Required' fields
     */
    public void fieldSchemesCreateIssueWithFieldLayoutSchemeRequired()
    {
        log("Create Issue: Attempt to create with issue field configuration");
        addFieldLayoutSchemeEntry("Bug", FIELD_CONFIGURATION_NAME_ONE, FIELD_SCHEME_NAME);

        associateFieldLayoutScheme(PROJECT_HOMOSAP, "Bug", FIELD_SCHEME_NAME);

        setRequiredFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, COMPONENTS_FIELD_ID);
        setRequiredFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, AFFECTS_VERSIONS_FIELD_ID);
        setRequiredFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, FIX_VERSIONS_FIELD_ID);

        createIssueStep1();
        setFormElement("summary", "test summary");
        submit();

        assertTextPresent("CreateIssueDetails.jspa");
        assertTextPresent("Component/s is required");
        assertTextPresent("Affects Version/s is required");
        assertTextPresent("Fix Version/s is required");

        removeAssociationWithFieldLayoutScheme(PROJECT_HOMOSAP, "Bug", FIELD_SCHEME_NAME);
        removeFieldConfigurationSchemeEntry("Bug", FIELD_SCHEME_NAME);

        setOptionalFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, COMPONENTS_FIELD_ID);
        setOptionalFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, AFFECTS_VERSIONS_FIELD_ID);
        setOptionalFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, FIX_VERSIONS_FIELD_ID);
    }

    /**
     * Tests the functionality of Field Layout Schemes for 'Create Issue' using 'Hidden' fields
     */
    public void fieldSchemesCreateIssueWithFieldConfigurationSchemeHidden()
    {
        copyFieldLayout(FIELD_CONFIGURATION_NAME_ONE);
        addFieldLayoutSchemeEntry("Bug", FIELD_CONFIGURATION_NAME_ONE, FIELD_SCHEME_NAME);

        associateFieldLayoutScheme(PROJECT_HOMOSAP, "Bug", FIELD_SCHEME_NAME);
        // Set fields to be hidden
        setHiddenFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, COMPONENTS_FIELD_ID);
        setHiddenFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, AFFECTS_VERSIONS_FIELD_ID);
        setHiddenFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, FIX_VERSIONS_FIELD_ID);

        log("Create Issue: Test the creation of am issue using hidden fields");
        createIssueStep1();

        assertFormElementNotPresent("components");
        assertFormElementNotPresent("versions");
        assertFormElementNotPresent("fixVersions");

        // Reset fields to be optional
        setShownFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, COMPONENTS_FIELD_ID);
        setShownFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, AFFECTS_VERSIONS_FIELD_ID);
        setShownFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, FIX_VERSIONS_FIELD_ID);

        removeAssociationWithFieldLayoutScheme(PROJECT_HOMOSAP, "Bug", FIELD_SCHEME_NAME);
        removeFieldConfigurationSchemeEntry("Bug", FIELD_SCHEME_NAME);
    }

    private void removeFieldConfigurationSchemeEntry(String issueTypeName, String schemeName)
    {
        gotoFieldLayoutSchemes();
        clickLinkWithText(schemeName);
        assertTextInTable("scheme_entries", issueTypeName);

        try
        {
            WebTable table = getDialog().getResponse().getTableWithID("scheme_entries");
            for (int i = 0; i < table.getRowCount(); i++)
            {
                String cellAsText = table.getCellAsText(i, 0);
                if (TextUtils.stringSet(cellAsText) && cellAsText.contains(issueTypeName))
                {
                    table.getTableCell(i, 2).getLinkWith("Delete").click();
                }
            }
        }
        catch (SAXException e)
        {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Tests the functionality of Field Configuration Schemes for 'Create Issue' using 'Hidden' fields
     */
    public void fieldSchemesEditIssueWithFieldConfigurationSchemeHidden(String issueKey)
    {
        addFieldLayoutSchemeEntry("Bug", FIELD_CONFIGURATION_NAME_ONE, FIELD_SCHEME_NAME);
        associateFieldLayoutScheme(PROJECT_HOMOSAP, "Bug", FIELD_SCHEME_NAME);

        // Set fields to be hidden
        setHiddenFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, COMPONENTS_FIELD_ID);
        setHiddenFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, AFFECTS_VERSIONS_FIELD_ID);
        setHiddenFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, FIX_VERSIONS_FIELD_ID);

        log("Edit Issue: Test the updating of an issue using hidden fields");
        navigation.issue().viewIssue(issueKey);
        tester.clickLink("edit-issue");

        assertFormElementNotPresent("components");
        assertFormElementNotPresent("versions");
        assertFormElementNotPresent("fixVersions");

        // Reset fields to be optional
        setShownFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, COMPONENTS_FIELD_ID);
        setShownFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, AFFECTS_VERSIONS_FIELD_ID);
        setShownFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, FIX_VERSIONS_FIELD_ID);
        removeAssociationWithFieldLayoutScheme(PROJECT_HOMOSAP, "Bug", FIELD_SCHEME_NAME);
        removeFieldConfigurationSchemeEntry("Bug", FIELD_SCHEME_NAME);
    }

    private void assertSearcherFields(boolean presenceFlag)
    {
        String json = tester.getDialog().getElement("criteriaJson").getTextContent();
        //This is a hack, but certainly good enough for this test
        String[] searchers = json.split("},");
        List<String> fieldsToCheck = Lists.newArrayList(
                                  "\"id\":\"priority\"",
                                  "\"id\":\"resolution\"",
                                  "\"id\":\"duedate\"");
        String presenceString = null;
        if (presenceFlag)
        {
            fieldsToCheck.add("\"id\":\"assignee\"");
            presenceString = "\"isShown\":true";
        }
        else
        {
            presenceString = "\"isShown\":false";
        }

        int count = 0;
        for (int index = 0; count < fieldsToCheck.size() && index < searchers.length; index++)
        {
            String s = searchers[index];
            if (s.contains(fieldsToCheck.get(count)))
            {
                assertThat(s, Matchers.containsString(presenceString));
                count++;
            }
        }
        assertEquals(fieldsToCheck.size(), count);
    }

    // Test for the presence of fields when hidden/displayed in a field layout.
    public void checkNavigatorFields()
    {
        // Encountered Javascript error when attempting to select project in issue nav.
        // Hence - not testing components/fix for/versions.

        // Test navigator fields with hidden/visible fields
        log("Check Issue Navigator display for hidden/visible fields");

        // Include Sub-task issue type
        activateSubTasks();

        addFieldLayoutSchemeEntry("Bug", FIELD_CONFIGURATION_NAME_ONE, FIELD_SCHEME_NAME);
        associateFieldLayoutScheme(PROJECT_HOMOSAP, "Bug", FIELD_SCHEME_NAME);

        // Set field to be hidden
        setHiddenFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, ASSIGNEE_FIELD_ID);
        setHiddenFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, DUE_DATE_FIELD_ID);
        setHiddenFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, PRIORITY_FIELD_ID);
        setHiddenFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, RESOLUTION_FIELD_ID);

        // Field is visible as only hidden in one issue type/project field configuration association
        getNavigation().issueNavigator().displayAllIssues();
        assertSearcherFields(true);

        associateFieldLayoutScheme(PROJECT_NEO, "Bug", FIELD_SCHEME_NAME);
        associateFieldLayoutScheme(PROJECT_MONKEY, "Bug", FIELD_SCHEME_NAME);
        addFieldLayoutSchemeEntry("New Feature", FIELD_CONFIGURATION_NAME_ONE, FIELD_SCHEME_NAME);
        addFieldLayoutSchemeEntry("Improvement", FIELD_CONFIGURATION_NAME_ONE, FIELD_SCHEME_NAME);
        addFieldLayoutSchemeEntry("Task", FIELD_CONFIGURATION_NAME_ONE, FIELD_SCHEME_NAME);
        addFieldLayoutSchemeEntry("Sub-task", FIELD_CONFIGURATION_NAME_ONE, FIELD_SCHEME_NAME);

        // Field is not visible as hidden in all issue type/project field configuration associations
        getNavigation().issueNavigator().displayAllIssues();
        assertSearcherFields(false);

        // Reset field to be optional
        setShownFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, ASSIGNEE_FIELD_ID);
        setShownFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, DUE_DATE_FIELD_ID);
        setShownFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, PRIORITY_FIELD_ID);
        setShownFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, RESOLUTION_FIELD_ID);

        getNavigation().issueNavigator().displayAllIssues();
        assertSearcherFields(true);

        removeAssociationWithFieldLayoutScheme(PROJECT_HOMOSAP, "Bug", FIELD_SCHEME_NAME);
        removeAssociationWithFieldLayoutScheme(PROJECT_NEO, "Bug", FIELD_SCHEME_NAME);
        removeAssociationWithFieldLayoutScheme(PROJECT_MONKEY, "Bug", FIELD_SCHEME_NAME);
        removeFieldConfigurationSchemeEntry("Bug", FIELD_SCHEME_NAME);
        removeFieldConfigurationSchemeEntry("New Feature", FIELD_SCHEME_NAME);
        removeFieldConfigurationSchemeEntry("Improvement", FIELD_SCHEME_NAME);
        removeFieldConfigurationSchemeEntry("Task", FIELD_SCHEME_NAME);
        removeFieldConfigurationSchemeEntry("Sub-task", FIELD_SCHEME_NAME);

        deactivateSubTasks();
    }

    // Test for the presence of fields when hidden/displayed in a field configuration.
    public void checkProjectTabPanels()
    {
        // Test navigator fields with hidden/visible fields
        log("Check Project Tab Panel for hidden/visible fields");

        // Include Sub-task issue type
        activateSubTasks();

        addFieldLayoutSchemeEntry("Bug", FIELD_CONFIGURATION_NAME_ONE, FIELD_SCHEME_NAME);
        associateFieldLayoutScheme(PROJECT_HOMOSAP, "Bug", FIELD_SCHEME_NAME);

        // Set fields to be hidden
        setHiddenFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, COMPONENTS_FIELD_ID);
        setHiddenFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, FIX_VERSIONS_FIELD_ID);
        setHiddenFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, VERSIONS_FIELD_ID);

        gotoPage("/browse/HSP");
        assertLinkPresentWithText("Components");
        assertLinkPresentWithText("Versions");
        assertLinkPresentWithText("Road Map");
        assertLinkPresentWithText("Change Log");

        associateFieldLayoutScheme(PROJECT_NEO, "Bug", FIELD_SCHEME_NAME);
        associateFieldLayoutScheme(PROJECT_MONKEY, "Bug", FIELD_SCHEME_NAME);
        addFieldLayoutSchemeEntry("New Feature", FIELD_CONFIGURATION_NAME_ONE, FIELD_SCHEME_NAME);
        addFieldLayoutSchemeEntry("Improvement", FIELD_CONFIGURATION_NAME_ONE, FIELD_SCHEME_NAME);
        addFieldLayoutSchemeEntry("Task", FIELD_CONFIGURATION_NAME_ONE, FIELD_SCHEME_NAME);
        addFieldLayoutSchemeEntry("Sub-task", FIELD_CONFIGURATION_NAME_ONE, FIELD_SCHEME_NAME);


        gotoPage("/browse/HSP");
        assertLinkNotPresentWithText("Open Issues");
        assertLinkNotPresentWithText("Components");
        assertLinkNotPresentWithText("Versions");
        assertLinkNotPresentWithText("Road Map");
        assertLinkNotPresentWithText("Change Log");

        // Reset field to be optional
        setShownFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, COMPONENTS_FIELD_ID);
        setShownFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, FIX_VERSIONS_FIELD_ID);
        setShownFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, VERSIONS_FIELD_ID);

        navigation.browseProjectTabPanel("HSP", PROJECT_PLUGIN_PREFIX + "issues-panel-panel");
        assertTextPresent("Unresolved: By Component");
        assertTextPresent("Unresolved: By Version");

        removeAssociationWithFieldLayoutScheme(PROJECT_HOMOSAP, "Bug", FIELD_SCHEME_NAME);
        removeAssociationWithFieldLayoutScheme(PROJECT_NEO, "Bug", FIELD_SCHEME_NAME);
        removeAssociationWithFieldLayoutScheme(PROJECT_MONKEY, "Bug", FIELD_SCHEME_NAME);
        removeFieldConfigurationSchemeEntry("Bug", FIELD_SCHEME_NAME);
        removeFieldConfigurationSchemeEntry("New Feature", FIELD_SCHEME_NAME);
        removeFieldConfigurationSchemeEntry("Improvement", FIELD_SCHEME_NAME);
        removeFieldConfigurationSchemeEntry("Task", FIELD_SCHEME_NAME);
        removeFieldConfigurationSchemeEntry("Sub-task", FIELD_SCHEME_NAME);

        deactivateSubTasks();
    }

    /**
     * Tests the functionality of Field Configuration Schemes for 'Create Issue' using 'Required' fields
     */
    public void fieldSchemesEditIssueWithFieldLayoutSchemeRequired(String issueKey)
    {
        log("Edit Issue: Attempt to edit an issue with issue field configuration");
        addFieldLayoutSchemeEntry("Bug", FIELD_CONFIGURATION_NAME_ONE, FIELD_SCHEME_NAME);

        associateFieldLayoutScheme(PROJECT_HOMOSAP, "Bug", FIELD_SCHEME_NAME);
        setRequiredFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, COMPONENTS_FIELD_ID);
        setRequiredFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, AFFECTS_VERSIONS_FIELD_ID);
        setRequiredFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, FIX_VERSIONS_FIELD_ID);

        gotoIssue(issueKey);
        clickLink("edit-issue");

        assertTextPresent("Edit Issue");
        submit("Update");

        assertTextPresent("Component/s is required");
        assertTextPresent("Affects Version/s is required");
        assertTextPresent("Fix Version/s is required");

        removeAssociationWithFieldLayoutScheme(PROJECT_HOMOSAP, "Bug", FIELD_SCHEME_NAME);
        removeFieldConfigurationSchemeEntry("Bug", FIELD_SCHEME_NAME);
        setOptionalFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, COMPONENTS_FIELD_ID);
        setOptionalFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, AFFECTS_VERSIONS_FIELD_ID);
        setOptionalFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, FIX_VERSIONS_FIELD_ID);
    }

    /**
     * Tests that fields are made hidden when task is moved to a type with a different Field Configuration Scheme
     */
    public void fieldSchemeMoveIssueWithFieldSchemeHidden(String issueKey)
    {
        log("Move Issue: Test the abilty to hide a field in a particular Field Configuration Scheme");
        addFieldLayoutSchemeEntry("Improvement", FIELD_CONFIGURATION_NAME_ONE, FIELD_SCHEME_NAME);
        associateFieldLayoutScheme(PROJECT_HOMOSAP, "Improvement", FIELD_SCHEME_NAME);
        setHiddenFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, COMPONENTS_FIELD_ID);
        setHiddenFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, AFFECTS_VERSIONS_FIELD_ID);
        setHiddenFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, FIX_VERSIONS_FIELD_ID);

        gotoIssue(issueKey);
        clickLink("move-issue");
        assertTextPresent("Move Issue");
        selectOption("issuetype", "Improvement");
        submit();

        assertTextPresent("All fields will be updated automatically.");

        setShownFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, COMPONENTS_FIELD_ID);
        setShownFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, AFFECTS_VERSIONS_FIELD_ID);
        setShownFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, FIX_VERSIONS_FIELD_ID);
        removeAssociationWithFieldLayoutScheme(PROJECT_HOMOSAP, "Improvement", FIELD_SCHEME_NAME);
        removeFieldConfigurationSchemeEntry("Improvement", FIELD_SCHEME_NAME);
    }

    /**
     * Tests that fields are made required when task is moved to a type with a different Field Configuration Scheme
     */
    public void fieldSchemeMoveIssueWithFieldSchemeRequired(String issueKey)
    {
        log("Move Issue: Test the abilty to make a field required in a particular Field Configuration Scheme");
        addFieldLayoutSchemeEntry("Improvement", FIELD_CONFIGURATION_NAME_ONE, FIELD_SCHEME_NAME);
        associateFieldLayoutScheme(PROJECT_HOMOSAP, "Improvement", FIELD_SCHEME_NAME);
        setRequiredFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, COMPONENTS_FIELD_ID);
        setRequiredFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, AFFECTS_VERSIONS_FIELD_ID);
        setRequiredFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, FIX_VERSIONS_FIELD_ID);

        gotoIssue(issueKey);

        clickLink("move-issue");
        assertTextPresent("Move Issue");
        selectOption("issuetype", "Improvement");
        submit();
        assertTextPresent("Step 3 of 4");
        getDialog().setWorkingForm("jiraform");
        submit();

        assertTextPresent("Step 3 of 4");
        assertTextPresent("Component/s is required");
        assertTextPresent("Affects Version/s is required");
        assertTextPresent("Fix Version/s is required");

        setOptionalFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, COMPONENTS_FIELD_ID);
        setOptionalFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, AFFECTS_VERSIONS_FIELD_ID);
        setOptionalFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, FIX_VERSIONS_FIELD_ID);
        removeAssociationWithFieldLayoutScheme(PROJECT_HOMOSAP, "Improvement", FIELD_SCHEME_NAME);
        removeFieldConfigurationSchemeEntry("Improvement", FIELD_SCHEME_NAME);
    }

    /**
     * Tests that field configuration schemes can be enforced on sub tasks with required fields
     */
    public void fieldSchemeCreateSubTaskWithFieldSchemeRequired(String issueKey)
    {
        log("Sub Task Create: Enforce Sub Tasks on a field configuration scheme");
        activateSubTasks();
        addFieldLayoutSchemeEntry(SUB_TASK_DEFAULT_TYPE, FIELD_CONFIGURATION_NAME_ONE, FIELD_SCHEME_NAME);
        associateFieldLayoutScheme(PROJECT_HOMOSAP, SUB_TASK_DEFAULT_TYPE, FIELD_SCHEME_NAME);
        setRequiredFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, COMPONENTS_FIELD_ID);
        setRequiredFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, AFFECTS_VERSIONS_FIELD_ID);
        setRequiredFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, FIX_VERSIONS_FIELD_ID);

        gotoIssue(issueKey);
        clickLink("create-subtask");
        assertTextPresent("Create Sub-Task");
        setFormElement("summary", "test summary");
        submit();

        assertTextPresent("Create Sub-Task");
        assertTextPresent("Component/s is required");
        assertTextPresent("Affects Version/s is required");
        assertTextPresent("Fix Version/s is required");

        setOptionalFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, COMPONENTS_FIELD_ID);
        setOptionalFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, AFFECTS_VERSIONS_FIELD_ID);
        setOptionalFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, FIX_VERSIONS_FIELD_ID);
        removeAssociationWithFieldLayoutScheme(PROJECT_HOMOSAP, SUB_TASK_DEFAULT_TYPE, FIELD_SCHEME_NAME);
        removeFieldConfigurationSchemeEntry(SUB_TASK_DEFAULT_TYPE, FIELD_SCHEME_NAME);
        deactivateSubTasks();
    }

    /**
     * Tests that field configuration schemes can be enforced on sub tasks with hidden field
     */
    public void fieldSchemeCreateSubTaskWithFieldSchemeHidden(String issueKey)
    {
        log("Sub Task Create: Enforce Sub Tasks on a field configuration scheme");
        activateSubTasks();
        addFieldLayoutSchemeEntry(SUB_TASK_DEFAULT_TYPE, FIELD_CONFIGURATION_NAME_ONE, FIELD_SCHEME_NAME);
        associateFieldLayoutScheme(PROJECT_HOMOSAP, SUB_TASK_DEFAULT_TYPE, FIELD_SCHEME_NAME);
        setHiddenFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, COMPONENTS_FIELD_ID);
        setHiddenFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, AFFECTS_VERSIONS_FIELD_ID);
        setHiddenFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, FIX_VERSIONS_FIELD_ID);

        gotoIssue(issueKey);
        clickLink("create-subtask");
        assertTextPresent("Create Sub-Task");
        assertFormElementNotPresent("components");
        assertFormElementNotPresent("versions");
        assertFormElementNotPresent("fixVersions");

        setShownFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, COMPONENTS_FIELD_ID);
        setShownFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, AFFECTS_VERSIONS_FIELD_ID);
        setShownFieldsOnEnterprise(FIELD_CONFIGURATION_NAME_ONE, FIX_VERSIONS_FIELD_ID);
        removeAssociationWithFieldLayoutScheme(PROJECT_HOMOSAP, SUB_TASK_DEFAULT_TYPE, FIELD_SCHEME_NAME);
        removeFieldConfigurationSchemeEntry(SUB_TASK_DEFAULT_TYPE, FIELD_SCHEME_NAME);
        deactivateSubTasks();
    }
}
