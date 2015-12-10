package com.atlassian.jira.webtests.ztests.fields;

import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

import java.io.IOException;

/**
 * @since v5.0
 */
@WebTest ({ Category.FUNC_TEST, Category.ADMINISTRATION, Category.FIELDS, Category.ISSUE_TYPES, Category.SCREENS })
public class TestFieldScreens extends JIRAWebTest
{
    private String issueKey;
    private String issueKey2;
    private String customFieldId;
    private String customFieldId2;

    private static final String ADDED_SCREEN_NAME = "Test Add Screen";
    private static final String COPIED_SCREEN_NAME = "Test Copy Screen";
    private static final String ADDED_SCREEN_SCHEME_NAME = "Test Add Screen Scheme";
    private static final String COPIED_SCREEN_SCHEME_NAME = "Test Copy Screen Scheme";
    private static final String ADDED_ISSUE_TYPE_SCREEN_SCHEME_NAME = "Test Add Issue Type Screen Scheme";
    private static final String COPIED_ISSUE_TYPE_SCREEN_SCHEME_NAME = "Test Copy Issue Tyep Screen Scheme";
    private static final String CUSTOM_FIELD_NAME = "Animal";
    private static final String CUSTOM_FIELD_NAME_TWO = "Approval Rating";
    private static final String TAB_NAME = "Tab for Testing";
    private static final String DEFAULT_TAB_NAME = "Field Tab";

    public TestFieldScreens(String name)
    {
        super(name);
    }

    public void setUp ()
    {
        super.setUp();

        navigation.login(ADMIN_USERNAME);
        getBackdoor().restoreBlankInstance();

        resetSettings();
        customFieldId = addCustomField("textfield", "global", CUSTOM_FIELD_NAME, "custom field 1", null, null, null);
        customFieldId2 = addCustomField("textfield", "global", CUSTOM_FIELD_NAME_TWO, "custom field 2", null, null, null);
        issueKey = addIssue(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, "Bug", "test field screen", "Minor", null,null,null, ADMIN_FULLNAME, "priority is added to assign issue screen", "test description for field screens", null, null, null);
        createIssueWithCustomField();
    }

    public void tearDown ()
    {
        try
        {
            deleteAllIssuesInAllPages();
            removeAllCustomFields();
            super.tearDown();
        }
        catch (Throwable t)
        {
            log("Some problem in tear down of " + getClass().getName(), t);
        }
        finally
        {
            customFieldId = null;
            customFieldId2 = null;
            issueKey = null;
        }
    }

    public void testFieldScreens()
    {
        fieldScreensAddFieldToFieldScreen();
        fieldScreensSetFieldInWorkflow();
        fieldScreensRemoveFieldFromFieldScreen();
        fieldScreensAddScreen();
        fieldScreensAddScreenCancel();
        fieldScreensAddScreenWithDuplicateName();
        fieldScreensAddScreenWithInvalidName();
        fieldScreensStandardScreens();

        fieldScreensAddScreenScheme();
        fieldScreensAddScreenSchemeWithDuplicateName();
        fieldScreensAddScreenSchemeWithInvalidName();

        fieldScreensAddIssueTypeScreenScheme();
        fieldScreensAddIssueTypeScreenSchemeWithDuplicateName();
        fieldScreensAddIssueTypeToScreenAssociation();

        fieldScreensProjectScreenSchemes();

        fieldScreensIssueTypeScreenSchemes();
        fieldScreensCopyIssueTypeScreenSchemes();
        fieldScreensDeleteIssueTypeScreenSchemes();

        fieldScreensCopyScreenScheme();
        fieldScreensDeleteScreenScheme();

        fieldScreensDeleteScreen();
    }



    public void testDeleteIssueTypeSchemeFailsWhenAssignedToProject()
    {
        Long issueTypeScreenSchemeId = getBackdoor().issueTypeScreenSchemes().createScheme("DELETEME", "blah", 1L);
        gotoIssueTypeScreenSchemes();
        getBackdoor().project().setIssueTypeScreenScheme(10000L, issueTypeScreenSchemeId);
        tester.clickLink("delete_issuetypescreenscheme_10000");

        tester.assertTextPresent("Cannot delete an issue type screen scheme that is being used by a project");

        tester.submit("Delete");
        tester.assertTextPresent("Cannot delete an issue type screen scheme that is being used by a project");
    }

    /**
     * Check that fields are added correctly and in the right position
     */
    private void fieldScreensAddFieldToFieldScreen()
    {
        addFieldToFieldScreen(ASSIGN_FIELD_SCREEN_NAME, CUSTOM_FIELD_NAME);
        addFieldToFieldScreen(ASSIGN_FIELD_SCREEN_NAME, "Issue Type", "2");
    }

    /**
     * Test error checking if an invalid field position is entered.
     */
    private void fieldScreensAddFieldToFieldScreenWithInvalidPosition()
    {
        addFieldToFieldScreen(ASSIGN_FIELD_SCREEN_NAME, "Affects Version/s", "0");
    }

    /**
     * Test the configuration of the assign issue screen
     */
    private void fieldScreensSetFieldInWorkflow()
    {
        navigation.issue().gotoIssue(issueKey);
        tester.clickLinkWithText("Close Issue");
        tester.assertFormElementNotPresent(CUSTOM_FIELD_PREFIX + customFieldId);
        tester.setWorkingForm("issue-workflow-transition");
        tester.submit("Transition");
        tester.clickLinkWithText("Reopen Issue");
        tester.setFormElement(CUSTOM_FIELD_PREFIX + customFieldId, "Polar Bear");
        tester.setWorkingForm("issue-workflow-transition");
        tester.submit("Transition");
    }

    /**
     * Test that field screens can be removed from field screens
     */
    private void fieldScreensRemoveFieldFromFieldScreen()
    {
        String[] fieldNames = new String[] {CUSTOM_FIELD_NAME, "Issue Type"};

        removeFieldFromFieldScreen(ASSIGN_FIELD_SCREEN_NAME, fieldNames);

        for (String fieldName : fieldNames)
        {
            if (findRowWithName(FIELD_TABLE_ID, SCREEN_TABLE_NAME_COLUMN_INDEX, fieldName) != null)
            {
                fail("Field " + fieldName + " was not removed");
            }
        }
    }

    /**
     * Test that a screen is added properly
     */
    private void fieldScreensAddScreen()
    {
        addScreen(ADDED_SCREEN_NAME, "");
        gotoFieldScreens();
        tester.assertLinkPresent("delete_fieldscreen_" + ADDED_SCREEN_NAME);
    }

    /**
     * Test that hitting Cancel goes back to the View Screens page
     */
    private void fieldScreensAddScreenCancel()
    {
        gotoFieldScreens();
        tester.clickLink("add-field-screen");
        tester.clickLink("field-screen-add-cancel");
        text.assertTextPresent(locator.css("header h2"), "View Screens");
    }

    /**
     * Test error checking if a screen is added with a duplicate name
     */
    private void fieldScreensAddScreenWithDuplicateName()
    {
        addScreen(ADDED_SCREEN_NAME, "");
        tester.assertTextPresent("A Screen with this name already exists.");
    }

    /**
     * Test error checking if a screen is added with a empty string as the name.
     */
    private void fieldScreensAddScreenWithInvalidName()
    {
        addScreen("", "");
        tester.assertTextPresent("You must enter a valid name.");
    }

    /**
     * Check screen is deleted correctly
     */
    private void fieldScreensDeleteScreen()
    {
        deleteScreen(ADDED_SCREEN_NAME);
        assertLinkNotPresent("delete_fieldscreen_" + ADDED_SCREEN_NAME);
    }

    /**
     * Check screen is copied correctly
     */


    /**
     * Check a screen scheme is added
     */
    private void fieldScreensAddScreenScheme()
    {
        addFieldScreenScheme(ADDED_SCREEN_SCHEME_NAME, "", DEFAULT_FIELD_SCREEN_NAME);
        gotoFieldScreenSchemes();
        tester.assertLinkPresent("delete_fieldscreenscheme_" + ADDED_SCREEN_SCHEME_NAME);
    }

    /**
     * Test error checking if a screen scheme is added with a duplicate name
     */
    private void fieldScreensAddScreenSchemeWithDuplicateName()
    {
        addFieldScreenScheme(ADDED_SCREEN_SCHEME_NAME, "", DEFAULT_FIELD_SCREEN_NAME);
        tester.assertTextPresent("A Screen Scheme with this name already exists.");
    }

    /**
     * Test error checking if a screen scheme is added with an invalid name
     */
    private void fieldScreensAddScreenSchemeWithInvalidName()
    {
        addFieldScreenScheme("", "", DEFAULT_FIELD_SCREEN_NAME);
        tester.assertTextPresent("You must enter a valid name.");
    }

    /**
     * Check that the screen scheme is copied correctly
     */
    private void fieldScreensCopyScreenScheme()
    {
        copyFieldScreenScheme(ADDED_SCREEN_SCHEME_NAME, COPIED_SCREEN_SCHEME_NAME, "");
        tester.clickLink("configure_fieldscreenscheme_" + COPIED_SCREEN_SCHEME_NAME);
        tester.assertLinkPresent("edit_fieldscreenscheme_" + DEFAULT_OPERATION_SCREEN);
//        assertLinkPresent("edit_fieldscreenscheme_" + CREATE_ISSUE_OPERATION_SCREEN);
    }

    /**
     * Check that the screen scheme is deleted
     */
    private void fieldScreensDeleteScreenScheme()
    {
        deleteFieldScreenScheme(COPIED_SCREEN_SCHEME_NAME);
        tester.assertLinkNotPresent("delete_fieldscreenscheme_" + COPIED_SCREEN_SCHEME_NAME);
        deleteFieldScreenScheme(ADDED_SCREEN_SCHEME_NAME);
        tester.assertLinkNotPresent("delete_fieldscreenscheme_" + ADDED_SCREEN_SCHEME_NAME);
    }

    private void fieldScreensAddIssueTypeScreenScheme()
    {
        addIssueTypeFieldScreenScheme(ADDED_ISSUE_TYPE_SCREEN_SCHEME_NAME, "", DEFAULT_SCREEN_SCHEME);
    }

    private void fieldScreensAddIssueTypeScreenSchemeWithDuplicateName()
    {
        addIssueTypeFieldScreenScheme(ADDED_ISSUE_TYPE_SCREEN_SCHEME_NAME, "", DEFAULT_SCREEN_SCHEME);
        tester.assertTextPresent("A scheme with this name already exists.");
    }

    private void fieldScreensAddIssueTypeToScreenAssociation()
    {
        addIssueTypeToScreenAssociation("10000", "Bug", ADDED_SCREEN_SCHEME_NAME);
        tester.assertLinkPresent("delete_issuetypescreenschemeentity_Bug");
    }

    private void fieldScreensCopyIssueTypeScreenSchemes()
    {
        copyIssueTypeFieldScreenSchemeName("10000", COPIED_ISSUE_TYPE_SCREEN_SCHEME_NAME, "");
        tester.clickLink("configure_issuetypescreenscheme_10001");
        tester.assertLinkPresent("edit_issuetypescreenschemeentity_default");
        tester.assertLinkPresent("edit_issuetypescreenschemeentity_Bug");
    }

    private void fieldScreensDeleteIssueTypeScreenSchemes()
    {
        deleteIssueTypeFieldScreenScheme("10001");
        tester.assertLinkNotPresent("delete_issuetypescreenscheme_10001");

        associateIssueTypeScreenSchemeToProject(PROJECT_NEO, DEFAULT_ISSUE_TYPE_SCREEN_SCHEME);
        deleteIssueTypeFieldScreenScheme("10000");
        tester.assertLinkNotPresent("delete_issuetypescreenscheme_10000");
    }

    private void fieldScreensAddTab()
    {
        log("Adding tabs");
        addTabToScreen(ADDED_SCREEN_NAME, TAB_NAME);
        tester.assertTextPresent(TAB_NAME);
        tester.assertLinkPresentWithText(DEFAULT_TAB_NAME);
    }

    private void fieldScreensDeleteTab()
    {
        log("Deleting tabs");
        deleteTabFromScreen(ADDED_SCREEN_NAME, TAB_NAME);
        tester.assertLinkNotPresentWithText(TAB_NAME);
    }

    private void fieldScreensAddFieldToTab()
    {
        addFieldToFieldScreenTab(ADDED_SCREEN_NAME, TAB_NAME, CUSTOM_FIELD_NAME, "");
        if (findRowWithName(FIELD_TABLE_ID, SCREEN_TABLE_NAME_COLUMN_INDEX, CUSTOM_FIELD_NAME) == null)
            fail("Field was not added to tab");
        addFieldToFieldScreenTab(ADDED_SCREEN_NAME, DEFAULT_TAB_NAME, CUSTOM_FIELD_NAME_TWO, "");
        addFieldToFieldScreenTab(ADDED_SCREEN_NAME, DEFAULT_TAB_NAME, "Summary", "");
    }

    private void fieldScreensAddTabWithDuplicateName()
    {
        addTabToScreen(ADDED_SCREEN_NAME, TAB_NAME);
        tester.assertTextPresent("Field Tab with this name already exists.");
    }

    private void fieldScreensAddTabWithInvalidName()
    {
        addTabToScreen(ADDED_SCREEN_NAME, "");
        tester.assertTextPresent("You must enter a valid name.");
    }

    private void fieldScreensRemoveFieldFromTab()
    {
        removeFieldFromFieldScreenTab(ADDED_SCREEN_NAME, TAB_NAME, new String[] {CUSTOM_FIELD_NAME});
        if (findRowWithName(FIELD_TABLE_ID, SCREEN_TABLE_NAME_COLUMN_INDEX, CUSTOM_FIELD_NAME) != null)
        {
            fail("Fields not deleted.");
        }
        removeFieldFromFieldScreenTab(ADDED_SCREEN_NAME, DEFAULT_TAB_NAME, new String[] {"Summary", CUSTOM_FIELD_NAME_TWO});
        if (findRowWithName(FIELD_TABLE_ID, SCREEN_TABLE_NAME_COLUMN_INDEX, CUSTOM_FIELD_NAME_TWO) != null)
        {
            fail("Fields not deleted.");
        }
    }

    /**
     * Check schemes using issue type based schemes
     */
    private void fieldScreensIssueTypeScreenSchemes()
    {
        log("Check schemes using issue type based schemes");
        associateIssueTypeScreenSchemeToProject(PROJECT_NEO, ADDED_ISSUE_TYPE_SCREEN_SCHEME_NAME);
        addFieldToFieldScreen(ADDED_SCREEN_NAME, "Summary");
        addFieldToFieldScreen(ADDED_SCREEN_NAME, CUSTOM_FIELD_NAME);

        String issueKeyCustomField = checkCreateIssueScreenScheme(ADDED_SCREEN_SCHEME_NAME, PROJECT_NEO, PROJECT_NEO_KEY, "Bug");
        checkViewIssueScreenScheme(ADDED_SCREEN_SCHEME_NAME, PROJECT_NEO, "Bug", issueKeyCustomField);
        checkEditIssueScreenScheme(ADDED_SCREEN_SCHEME_NAME, PROJECT_NEO, "Bug", issueKeyCustomField);

        addIssueOperationToScreenAssociation(ADDED_SCREEN_SCHEME_NAME, CREATE_ISSUE_OPERATION_SCREEN, ADDED_SCREEN_NAME);
        addIssueOperationToScreenAssociation(ADDED_SCREEN_SCHEME_NAME, VIEW_ISSUE_OPERATION_SCREEN, ADDED_SCREEN_NAME);
        addIssueOperationToScreenAssociation(ADDED_SCREEN_SCHEME_NAME, EDIT_ISSUE_OPERATION_SCREEN, ADDED_SCREEN_NAME);
        checkNoScreenScheme(PROJECT_NEO, "Improvement", issueKey2);
        deleteIssueOperationFromScreenAssociation(ADDED_SCREEN_SCHEME_NAME, CREATE_ISSUE_OPERATION_SCREEN);
        deleteIssueOperationFromScreenAssociation(ADDED_SCREEN_SCHEME_NAME, VIEW_ISSUE_OPERATION_SCREEN);
        deleteIssueOperationFromScreenAssociation(ADDED_SCREEN_SCHEME_NAME, EDIT_ISSUE_OPERATION_SCREEN);

        checkNoScreenScheme(PROJECT_NEO, "Bug", issueKeyCustomField);

        removeFieldFromFieldScreen(ADDED_SCREEN_NAME, new String[] {"Summary", CUSTOM_FIELD_NAME});
        associateIssueTypeScreenSchemeToProject(PROJECT_NEO, DEFAULT_ISSUE_TYPE_SCREEN_SCHEME);
    }

    /**
     * Check screens using project based schemes
     */
    private void fieldScreensProjectScreenSchemes()
    {
        log("Check screens using project based schemes");
        associateIssueTypeScreenSchemeToProject(PROJECT_HOMOSAP, ADDED_ISSUE_TYPE_SCREEN_SCHEME_NAME);
        addFieldToFieldScreen(ADDED_SCREEN_NAME, "Summary");
        addFieldToFieldScreen(ADDED_SCREEN_NAME, CUSTOM_FIELD_NAME);

        String issueKeyCustomField = checkCreateIssueScreenScheme(ADDED_SCREEN_SCHEME_NAME, PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, "Bug");
        checkViewIssueScreenScheme(ADDED_SCREEN_SCHEME_NAME, PROJECT_HOMOSAP, "Bug", issueKeyCustomField);
        checkEditIssueScreenScheme(ADDED_SCREEN_SCHEME_NAME, PROJECT_HOMOSAP, "Bug", issueKeyCustomField);

        addIssueOperationToScreenAssociation(ADDED_SCREEN_SCHEME_NAME, CREATE_ISSUE_OPERATION_SCREEN, ADDED_SCREEN_NAME);
        addIssueOperationToScreenAssociation(ADDED_SCREEN_SCHEME_NAME, VIEW_ISSUE_OPERATION_SCREEN, ADDED_SCREEN_NAME);
        addIssueOperationToScreenAssociation(ADDED_SCREEN_SCHEME_NAME, EDIT_ISSUE_OPERATION_SCREEN, ADDED_SCREEN_NAME);
        checkNoScreenScheme(PROJECT_NEO, "Bug", issueKey2);
        deleteIssueOperationFromScreenAssociation(ADDED_SCREEN_SCHEME_NAME, CREATE_ISSUE_OPERATION_SCREEN);
        deleteIssueOperationFromScreenAssociation(ADDED_SCREEN_SCHEME_NAME, VIEW_ISSUE_OPERATION_SCREEN);
        deleteIssueOperationFromScreenAssociation(ADDED_SCREEN_SCHEME_NAME, EDIT_ISSUE_OPERATION_SCREEN);

        removeFieldFromFieldScreen(ADDED_SCREEN_NAME, new String[] {"Summary", CUSTOM_FIELD_NAME});
        checkNoScreenScheme(PROJECT_HOMOSAP, "Bug", issueKeyCustomField);

        associateIssueTypeScreenSchemeToProject(PROJECT_HOMOSAP, DEFAULT_ISSUE_TYPE_SCREEN_SCHEME);
    }

    /**
     * Check screen functionality using standard settings
     */
    private void fieldScreensStandardScreens()
    {
        log("Check screens for standard settings");
        addFieldToFieldScreen(ADDED_SCREEN_NAME, "Summary");
        addFieldToFieldScreen(ADDED_SCREEN_NAME, CUSTOM_FIELD_NAME);

        String issueKeyCustomField = checkCreateIssueScreenScheme(DEFAULT_SCREEN_SCHEME, PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, "Bug");
        checkViewIssueScreenScheme(DEFAULT_SCREEN_SCHEME, PROJECT_HOMOSAP, "Bug", issueKeyCustomField);
        checkEditIssueScreenScheme(DEFAULT_SCREEN_SCHEME, PROJECT_HOMOSAP, "Bug", issueKeyCustomField);

        removeFieldFromFieldScreen(ADDED_SCREEN_NAME, new String[] {"Summary", CUSTOM_FIELD_NAME});
        checkNoScreenScheme(PROJECT_HOMOSAP, "Bug", issueKeyCustomField);
    }

    /**
     * Check the tab functionality in the create, edit and view screens
     */
    protected void fieldScreensTabViews()
    {
        addIssueOperationToScreenAssociation(DEFAULT_SCREEN_SCHEME, CREATE_ISSUE_OPERATION_SCREEN, ADDED_SCREEN_NAME);
        addIssueOperationToScreenAssociation(DEFAULT_SCREEN_SCHEME, VIEW_ISSUE_OPERATION_SCREEN, ADDED_SCREEN_NAME);
        addIssueOperationToScreenAssociation(DEFAULT_SCREEN_SCHEME, EDIT_ISSUE_OPERATION_SCREEN, ADDED_SCREEN_NAME);

        createIssueStep1();

        tester.setFormElement(CUSTOM_FIELD_PREFIX + customFieldId2, "High");
        tester.setFormElement("summary", "This is a test issue");
        tester.setFormElement(CUSTOM_FIELD_PREFIX + customFieldId, "Rhino");

        tester.submit("Create");

        text.assertTextPresent(getCustomFieldLabel(1,1), CUSTOM_FIELD_NAME_TWO);
        text.assertTextPresent(getCustomFieldValue(1,1), "High");

        text.assertTextPresent(getCustomFieldLabel(2,1), CUSTOM_FIELD_NAME);
        text.assertTextPresent(getCustomFieldValue(2,1), "Rhino");

        tester.clickLink("edit-issue");
        tester.setFormElement(CUSTOM_FIELD_PREFIX + customFieldId2, "Low");
        tester.setFormElement(CUSTOM_FIELD_PREFIX + customFieldId, "Tiger");
        tester.submit("Update");

        text.assertTextPresent(getCustomFieldLabel(1,1), CUSTOM_FIELD_NAME_TWO);
        text.assertTextPresent(getCustomFieldValue(1,1), "Low");

        text.assertTextPresent(getCustomFieldLabel(2,1), CUSTOM_FIELD_NAME);
        text.assertTextPresent(getCustomFieldValue(2,1), "Tiger");

        deleteIssueOperationFromScreenAssociation(DEFAULT_SCREEN_SCHEME, CREATE_ISSUE_OPERATION_SCREEN);
        deleteIssueOperationFromScreenAssociation(DEFAULT_SCREEN_SCHEME, VIEW_ISSUE_OPERATION_SCREEN);
        deleteIssueOperationFromScreenAssociation(DEFAULT_SCREEN_SCHEME, EDIT_ISSUE_OPERATION_SCREEN);
    }

    private Locator getCustomFieldLabel(int customfieldTabIndex, int fieldIndex)
    {
        return locator.xpath(String.format("//[@id='customfield-panel-%d']//li[@class='item']//*[@class='name']",
                customfieldTabIndex, fieldIndex));
    }

    private Locator getCustomFieldValue(int customfieldTabIndex, int fieldIndex)
    {
        return locator.xpath(String.format("//[@id='customfield-panel-%d']//li[@class='item']//*[@class='value']",
                customfieldTabIndex, fieldIndex));
    }

    // Helper Functions
    private void resetSettings()
    {
        if (projectExists(PROJECT_HOMOSAP))
        {
            log("Project " + PROJECT_HOMOSAP + " exists");
        }
        else
        {
            administration.project().addProject(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, ADMIN_USERNAME);
        }
        if (projectExists(PROJECT_NEO))
        {
            log("Project: " + PROJECT_NEO + " exists");
        }
        else
        {
            administration.project().addProject(PROJECT_NEO, PROJECT_NEO_KEY, ADMIN_USERNAME);
        }

        associateIssueTypeScreenSchemeToProject(PROJECT_HOMOSAP, DEFAULT_ISSUE_TYPE_SCREEN_SCHEME);
        associateIssueTypeScreenSchemeToProject(PROJECT_NEO, DEFAULT_ISSUE_TYPE_SCREEN_SCHEME);
        removeAllIssueTypeScreenSchemes();

//        removeFieldFromFieldScreen(ASSIGN_FIELD_SCREEN_NAME, new String[] {CUSTOM_FIELD_NAME, "Issue Type"});

        removeAllCustomFields();

        removeAllScreenAssociationsFromDefault();
        removeAllFieldScreenSchemes();
        removeAllFieldScreens();
    }

    private void createIssueWithCustomField()
    {
        addFieldToFieldScreen(DEFAULT_FIELD_SCREEN_NAME, CUSTOM_FIELD_NAME);
        navigation.issue().goToCreateIssueForm(PROJECT_NEO, "Improvement");

        tester.setFormElement("summary", "This is an issue in project 2 with a custom field");
        tester.setFormElement(CUSTOM_FIELD_PREFIX + customFieldId, "Elephant");

        tester.submit();

        issueKey2 = getIssueKey(PROJECT_NEO_KEY);

        removeFieldFromFieldScreen(DEFAULT_FIELD_SCREEN_NAME, new String[] {CUSTOM_FIELD_NAME});
    }

    private String getIssueKey(String projectKey)
    {
        try
        {
            String text = tester.getDialog().getResponse().getText();
            int projectIdLocation = text.indexOf(projectKey);
            int endOfIssueKey = text.indexOf("]", projectIdLocation);
            String issueKeyCustomField = text.substring(projectIdLocation, endOfIssueKey);
            return issueKeyCustomField;
        }
        catch (IOException e)
        {
            fail("Unable to retrieve issue key" + e.getMessage());
        }

        return null;
    }

    /**
     * Check that field screen is not shown for all screens
     */
    private void checkNoScreenScheme(String project, String issueType, String issueKeyCustomField)
    {
        log("Checking scheme association for with no scheme selected");
        createIssueStep1(project, issueType);

        tester.assertFormElementNotPresent(CUSTOM_FIELD_PREFIX + customFieldId);

        tester.submit();

        navigation.issue().gotoIssue(issueKeyCustomField);
        tester.assertTextNotPresent(CUSTOM_FIELD_NAME);

        tester.clickLink("edit-issue");
        tester.assertFormElementNotPresent(CUSTOM_FIELD_PREFIX + customFieldId);
    }

    /**
     * Check that field screen is shown for the Create Issue screen but not for view issue or edit issue
     */
    private String checkCreateIssueScreenScheme(String screenScheme, String project, String project_key, String issueType)
    {
        log("Checking scheme association for Create");
        addIssueOperationToScreenAssociation(screenScheme, CREATE_ISSUE_OPERATION_SCREEN, ADDED_SCREEN_NAME);

        createIssueStep1(project, issueType);

        tester.setFormElement("summary", "This is a test to see if field is shown");
        tester.setFormElement(CUSTOM_FIELD_PREFIX + customFieldId, "Elephant");

        tester.submit();

        tester.assertTextNotPresent("Elephant");
        tester.assertTextNotPresent(CUSTOM_FIELD_NAME);

        String issueKeyCustomField = getIssueKey(project_key);

        tester.clickLink("edit-issue");
        tester.assertFormElementNotPresent(CUSTOM_FIELD_PREFIX + customFieldId);

        deleteIssueOperationFromScreenAssociation(screenScheme, CREATE_ISSUE_OPERATION_SCREEN);

        return issueKeyCustomField;
    }

    /**
     * Check field screen is shown for View issue screen but not create issue or edit issue screens
     */
    private void checkViewIssueScreenScheme(String screenScheme, String project, String issueType, String issueKeyCustomField)
    {
        log("Checking scheme association for View");
        addIssueOperationToScreenAssociation(screenScheme, VIEW_ISSUE_OPERATION_SCREEN, ADDED_SCREEN_NAME);

        createIssueStep1(project, issueType);
        tester.assertFormElementNotPresent(CUSTOM_FIELD_PREFIX + customFieldId);

        gotoIssue(issueKeyCustomField);
        tester.assertTextPresent("Elephant");
        tester.assertTextPresent(CUSTOM_FIELD_NAME);

        tester.clickLink("edit-issue");
        tester.assertFormElementNotPresent(CUSTOM_FIELD_PREFIX + customFieldId);

        deleteIssueOperationFromScreenAssociation(screenScheme, VIEW_ISSUE_OPERATION_SCREEN);
    }

    /**
     * Check field screen is shown for Edit issue screen but not create issue or view issue screens
     */
    private void checkEditIssueScreenScheme(String screenScheme, String project, String issueType, String issueKeyCustomField)
    {
        log("Checking scheme association for Edit");
        addIssueOperationToScreenAssociation(screenScheme, EDIT_ISSUE_OPERATION_SCREEN, ADDED_SCREEN_NAME);

        createIssueStep1(project, issueType);
        tester.assertFormElementNotPresent(CUSTOM_FIELD_PREFIX + customFieldId);

        navigation.issue().gotoIssue(issueKeyCustomField);
        tester.clickLink("edit-issue");

        tester.setFormElement(CUSTOM_FIELD_PREFIX + customFieldId, "Whale");
        tester.submit();
        tester.assertTextNotPresent("Whale");
        tester.assertTextNotPresent(CUSTOM_FIELD_NAME);

        deleteIssueOperationFromScreenAssociation(screenScheme, EDIT_ISSUE_OPERATION_SCREEN);
    }
}