package com.atlassian.jira.webtests.ztests.admin.scheme;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.WebTable;
import org.xml.sax.SAXException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * 
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.SCHEMES })
public class TestSchemeMergeTool extends JIRAWebTest
{
    public TestSchemeMergeTool(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestSchemeMergeTool.xml");
    }

    public void testNoPermissionSchemesCanMerge()
    {
        gotoPage("secure/admin/SchemeTypePicker!default.jspa");

        assertTextPresent("Merge Schemes: Select Schemes");

        submit("Analyse Schemes");

        assertTextPresent("Merge Schemes: Choose Schemes to Merge");
        assertTextPresent("There are no associated permission schemes which can be merged at this time");
        assertTextNotPresent("Preview Changes");

    }

    public void testNoNotificationSchemesCanMerge()
    {
        gotoPage("secure/admin/SchemeTypePicker!switch.jspa?typeOfSchemesToDisplay=associated&selectedSchemeType=NotificationScheme");

        assertTextPresent("Merge Schemes: Select Schemes");

        submit("Analyse Schemes");

        assertTextPresent("Merge Schemes: Choose Schemes to Merge");
        assertTextPresent("There are no associated notification schemes which can be merged at this time");
        assertTextNotPresent("Preview Changes");
    }

    public void testPermissionSchemesCanMerge() throws SAXException
    {
        try
        {
            HttpUnitOptions.setScriptingEnabled(true);
            gotoPage("secure/admin/SchemeTypePicker!switch.jspa?typeOfSchemesToDisplay=all&selectedSchemeType=PermissionScheme");

            assertTextPresent("Merge Schemes: Select Schemes");

            submit("Analyse Schemes");

            assertTextPresent("Merge Schemes: Choose Schemes to Merge");
            assertTextPresentBeforeText("4", "2");

            //Check that the correct schemes are being merged.
            WebTable mergedSchemeTable = getDialog().getResponse().getTableWithID("merged_schemes");

            assertTrue(tableCellHasText(mergedSchemeTable, 1, 1, "Copy of Another Permission Scheme"));
            assertTrue(tableCellHasText(mergedSchemeTable, 1, 1, "Another Permission Scheme"));
            assertFalse(tableCellHasText(mergedSchemeTable, 1, 1, "Copy of Default Permission Scheme"));
            assertFalse(tableCellHasText(mergedSchemeTable, 1, 1, "Default Permission Scheme"));

            assertTrue(tableCellHasText(mergedSchemeTable, 2, 1, "Copy of Default Permission Scheme"));
            assertTrue(tableCellHasText(mergedSchemeTable, 2, 1, "Default Permission Scheme"));
            assertFalse(tableCellHasText(mergedSchemeTable, 2, 1, "Copy of Another Permission Scheme"));
            assertFalse(tableCellHasText(mergedSchemeTable, 2, 1, "Another Permission Scheme"));


            checkCheckbox("selectedDistilledSchemes", "Clone of Another Permission Scheme");
            checkCheckbox("selectedDistilledSchemes", "Clone of Copy of Default Permission Scheme");

            setFormElement("Clone of Copy of Default Permission Scheme", "New_Scheme_1");
            setFormElement("Clone of Another Permission Scheme", "New_Scheme_2");

            assertTextPresent("Preview Changes");
            assertTextPresent("Cancel");

            submit("Preview Changes");

            assertTextPresent("<strong>all</strong>");
            assertTextPresent("Adding scheme: <strong>New_Scheme_1</strong>");
            assertTextPresent("Adding scheme: <strong>New_Scheme_2</strong>");

            //Check that the first new scheme contains all the right original schemes and project associations
            WebTable newSchemeTable1 = getDialog().getResponse().getTableWithID("New_Scheme_1_table");
            assertTrue(tableCellHasText(newSchemeTable1, 1, 0, "Copy of Default Permission Scheme"));
            assertTrue(tableCellHasText(newSchemeTable1, 1, 0, "Default Permission Scheme"));
            assertFalse(tableCellHasText(newSchemeTable1, 1, 0, "Copy of Another Permission Scheme"));
            assertFalse(tableCellHasText(newSchemeTable1, 1, 0, "Another Permission Scheme"));
            assertTrue(tableCellHasText(newSchemeTable1, 1, 1, PROJECT_HOMOSAP));

            //Check that the second new scheme contains all the right original schemes and project associations
            WebTable newSchemeTable2 = getDialog().getResponse().getTableWithID("New_Scheme_2_table");
            assertFalse(tableCellHasText(newSchemeTable2, 1, 0, "Copy of Default Permission Scheme"));
            assertFalse(tableCellHasText(newSchemeTable2, 1, 0, "Default Permission Scheme"));
            assertTrue(tableCellHasText(newSchemeTable2, 1, 0, "Copy of Another Permission Scheme"));
            assertTrue(tableCellHasText(newSchemeTable2, 1, 0, "Another Permission Scheme"));
            assertFalse(tableCellHasText(newSchemeTable2, 1, 1, PROJECT_HOMOSAP));

            assertTextPresent("Submit Changes");
            assertTextPresent("Cancel");

            submit("Submit Changes");

            assertTextPresent("Merge Schemes: Results");
            assertTextPresent("You have successfully saved the following merged scheme(s)");
            assertTextPresent("New_Scheme_1");
            assertTextPresent("has been associated with project(s):");
            assertTextPresent(PROJECT_HOMOSAP);
            assertTextPresent("New_Scheme_2");

            // Check that the bulk delete schemes link is present and that it works
            assertTextPresent("bulk delete schemes tool");
            clickLinkWithText("bulk delete schemes tool");
            assertTextPresent("Bulk Delete Schemes: Select Schemes");

            assertThat(backdoor.project().getSchemes(PROJECT_HOMOSAP_KEY).permissionScheme.name, equalTo("New_Scheme_1"));
        }
        finally
        {
            HttpUnitOptions.setScriptingEnabled(false);
        }
    }

    public void testNotificationSchemesCanMerge() throws SAXException
    {
        try
        {
            HttpUnitOptions.setScriptingEnabled(true);
            gotoPage("secure/admin/SchemeTypePicker!switch.jspa?typeOfSchemesToDisplay=all&selectedSchemeType=NotificationScheme");

            assertTextPresent("Merge Schemes: Select Schemes");

            submit("Analyse Schemes");

            assertTextPresent("Merge Schemes: Choose Schemes to Merge");
            assertTextPresentBeforeText("4", "2");

            //Check that the correct schemes are being merged.
            WebTable mergedSchemeTable = getDialog().getResponse().getTableWithID("merged_schemes");
            assertTrue(tableCellHasText(mergedSchemeTable, 1, 1, "Copy of Another Notification Scheme"));
            assertTrue(tableCellHasText(mergedSchemeTable, 1, 1, "Another Notification Scheme"));
            assertFalse(tableCellHasText(mergedSchemeTable, 1, 1, "Copy of Default Notification Scheme"));
            assertFalse(tableCellHasText(mergedSchemeTable, 1, 1, "Default Notification Scheme"));

            assertTrue(tableCellHasText(mergedSchemeTable, 2, 1, "Copy of Default Notification Scheme"));
            assertTrue(tableCellHasText(mergedSchemeTable, 2, 1, "Default Notification Scheme"));
            assertFalse(tableCellHasText(mergedSchemeTable, 2, 1, "Copy of Another Notification Scheme"));
            assertFalse(tableCellHasText(mergedSchemeTable, 2, 1, "Another Notification Scheme"));


            checkCheckbox("selectedDistilledSchemes", "Clone of Another Notification Scheme");
            checkCheckbox("selectedDistilledSchemes", "Clone of Copy of Default Notification Scheme");

            setFormElement("Clone of Copy of Default Notification Scheme", "New_Scheme_1");
            setFormElement("Clone of Another Notification Scheme", "New_Scheme_2");

            assertTextPresent("Preview Changes");
            assertTextPresent("Cancel");

            submit("Preview Changes");

            assertTextPresent("<strong>all</strong>");
            assertTextPresent("Adding scheme: <strong>New_Scheme_1</strong>");
            assertTextPresent("Adding scheme: <strong>New_Scheme_2</strong>");

            //Check that the first new scheme contains all the right original schemes and project associations
            WebTable newSchemeTable1 = getDialog().getResponse().getTableWithID("New_Scheme_1_table");
            assertTrue(tableCellHasText(newSchemeTable1, 1, 0, "Copy of Default Notification Scheme"));
            assertTrue(tableCellHasText(newSchemeTable1, 1, 0, "Default Notification Scheme"));
            assertFalse(tableCellHasText(newSchemeTable1, 1, 0, "Copy of Another Notification Scheme"));
            assertFalse(tableCellHasText(newSchemeTable1, 1, 0, "Another Notification Scheme"));
            assertTrue(tableCellHasText(newSchemeTable1, 1, 1, PROJECT_HOMOSAP));

            //Check that the second new scheme contains all the right original schemes and project associations
            WebTable newSchemeTable2 = getDialog().getResponse().getTableWithID("New_Scheme_2_table");
            assertFalse(tableCellHasText(newSchemeTable2, 1, 0, "Copy of Default Notification Scheme"));
            assertFalse(tableCellHasText(newSchemeTable2, 1, 0, "Default Notification Scheme"));
            assertTrue(tableCellHasText(newSchemeTable2, 1, 0, "Copy of Another Notification Scheme"));
            assertTrue(tableCellHasText(newSchemeTable2, 1, 0, "Another Notification Scheme"));
            assertFalse(tableCellHasText(newSchemeTable2, 1, 1, PROJECT_HOMOSAP));

            assertTextPresent("Submit Changes");
            assertTextPresent("Cancel");

            submit("Submit Changes");

            assertTextPresent("Merge Schemes: Results");
            assertTextPresent("You have successfully saved the following merged scheme(s)");
            assertTextPresent("New_Scheme_1");
            assertTextPresent("has been associated with project(s):");
            assertTextPresent(PROJECT_HOMOSAP);
            assertTextPresent("New_Scheme_2");

            assertThat(backdoor.project().getSchemes(PROJECT_HOMOSAP_KEY).notificationScheme.name, equalTo("New_Scheme_1"));
        }
        finally
        {
            HttpUnitOptions.setScriptingEnabled(false);
        }
    }

    public void testSelectNoSchemes()
    {
        gotoPage("secure/admin/SchemeTypePicker!switch.jspa?typeOfSchemesToDisplay=all&selectedSchemeType=NotificationScheme");

        submit("Analyse Schemes");

        submit("Preview Changes");

        //Check that the correct error msg is shown
        assertTextPresent("You must select at least one set of schemes to merge.");
    }

    public void testEnterDuplicateSchemeNames() throws SAXException
    {
        try
        {
            HttpUnitOptions.setScriptingEnabled(true);
            gotoPage("secure/admin/SchemeTypePicker!switch.jspa?typeOfSchemesToDisplay=all&selectedSchemeType=NotificationScheme");

            submit("Analyse Schemes");

            checkCheckbox("selectedDistilledSchemes", "Clone of Another Notification Scheme");
            checkCheckbox("selectedDistilledSchemes", "Clone of Copy of Default Notification Scheme");

            setFormElement("Clone of Copy of Default Notification Scheme", "New_Scheme_1");
            setFormElement("Clone of Another Notification Scheme", "New_Scheme_1");

            submit("Preview Changes");

            //Check that the correct duplicate scheme name error msg is shown
            WebTable newSchemeTable1 = getDialog().getResponse().getTableWithID("merged_schemes");
            assertTrue(tableCellHasText(newSchemeTable1, 1, 2, "You have entered this scheme name more than once."));
            assertTrue(tableCellHasText(newSchemeTable1, 2, 2, "You have entered this scheme name more than once."));
        }
        finally
        {
            HttpUnitOptions.setScriptingEnabled(false);
        }
    }

    public void testEnterExistingSchemeName() throws SAXException
    {
        try
        {
            HttpUnitOptions.setScriptingEnabled(true);
            gotoPage("secure/admin/SchemeTypePicker!switch.jspa?typeOfSchemesToDisplay=all&selectedSchemeType=NotificationScheme");

            submit("Analyse Schemes");

            checkCheckbox("selectedDistilledSchemes", "Clone of Copy of Default Notification Scheme");

            setFormElement("Clone of Copy of Default Notification Scheme", "Another Notification Scheme");

            submit("Preview Changes");

            //Check that the correct duplicate scheme name error msg is shown
            WebTable newSchemeTable1 = getDialog().getResponse().getTableWithID("merged_schemes");
            assertTrue(tableCellHasText(newSchemeTable1, 2, 2, "A scheme with the name you entered already exists. Please enter a different scheme name."));
        }
        finally
        {
            HttpUnitOptions.setScriptingEnabled(false);
        }
    }

    public void testEnterExistingSchemeNameWithWhitespace() throws SAXException
    {
        try
        {
            HttpUnitOptions.setScriptingEnabled(true);
            gotoPage("secure/admin/SchemeTypePicker!switch.jspa?typeOfSchemesToDisplay=all&selectedSchemeType=NotificationScheme");

            submit("Analyse Schemes");

            checkCheckbox("selectedDistilledSchemes", "Clone of Copy of Default Notification Scheme");

            setFormElement("Clone of Copy of Default Notification Scheme", "Another Notification Scheme     ");

            submit("Preview Changes");

            //Check that the correct duplicate scheme name error msg is shown
            WebTable newSchemeTable1 = getDialog().getResponse().getTableWithID("merged_schemes");
            assertTrue(tableCellHasText(newSchemeTable1, 2, 2, "A scheme with the name you entered already exists. Please enter a different scheme name."));
        }
        finally
        {
            HttpUnitOptions.setScriptingEnabled(false);
        }
    }

    public void testDeleteSchemeDuringPreview()
    {
        try
        {
            HttpUnitOptions.setScriptingEnabled(true);
            gotoPage("secure/admin/SchemeTypePicker!switch.jspa?typeOfSchemesToDisplay=all&selectedSchemeType=NotificationScheme");

            submit("Analyse Schemes");

            checkCheckbox("selectedDistilledSchemes", "Clone of Copy of Default Notification Scheme");

            setFormElement("Clone of Copy of Default Notification Scheme", "New_Scheme_1");

            submit("Preview Changes");

            //Delete a scheme part of the merge whilst previewing
            gotoPage("secure/admin/DeleteNotificationScheme!default.jspa?schemeId=10010");
            submit("Delete");

            // Jump back into the wizard, this should be fine since the session is used to store the state
            gotoPage("secure/admin/SchemeMergePreview!default.jspa?selectedSchemeType=NotificationScheme&typeOfSchemesToDisplay=all");
            submit("Submit Changes");

            assertTextPresent("Could not save merged scheme <strong>New_Scheme_1</strong>:");
            assertTextPresent("Some of the original schemes (<strong>Copy of Default Notification Scheme</strong>)");
        }
        finally
        {
            HttpUnitOptions.setScriptingEnabled(false);
        }
    }

    public void testModifySchemeEntitiesDuringPreview()
    {
        try
        {
            HttpUnitOptions.setScriptingEnabled(true);
            gotoPage("secure/admin/SchemeTypePicker!switch.jspa?typeOfSchemesToDisplay=all&selectedSchemeType=NotificationScheme");

            submit("Analyse Schemes");

            checkCheckbox("selectedDistilledSchemes", "Clone of Copy of Default Notification Scheme");
            checkCheckbox("selectedDistilledSchemes", "Clone of Another Notification Scheme");

            setFormElement("Clone of Copy of Default Notification Scheme", "New_Scheme_1");
            setFormElement("Clone of Another Notification Scheme", "New_Scheme_2");

            submit("Preview Changes");

            //Modify scheme entities for a number of schemes
            gotoPage("secure/admin/DeleteNotification!default.jspa?schemeId=10000&id=10003");
            submit("Delete");
            gotoPage("secure/admin/DeleteNotification!default.jspa?schemeId=10010&id=10078");
            submit("Delete");

            // Jump back into the wizard, this should be fine since the session is used to store the state
            gotoPage("secure/admin/SchemeMergePreview!default.jspa?selectedSchemeType=NotificationScheme&typeOfSchemesToDisplay=all");
            submit("Submit Changes");

            assertTextPresent("Could not save merged scheme <strong>New_Scheme_1</strong>:");
            assertTextPresent("Some of the original schemes (<strong>Copy of Default Notification Scheme, Default Notification Scheme</strong>)");

            assertTextPresent("You have successfully saved the following merged scheme(s)");
            assertTextPresent("New_Scheme_2");
            assertTextNotPresent("has been associated with project(s):");
        }
        finally
        {
            HttpUnitOptions.setScriptingEnabled(false);
        }
    }

    public void testModifySchemeNameDuringPreview()
    {
        try
        {
            HttpUnitOptions.setScriptingEnabled(true);
            gotoPage("secure/admin/SchemeTypePicker!switch.jspa?typeOfSchemesToDisplay=all&selectedSchemeType=NotificationScheme");

            submit("Analyse Schemes");

            checkCheckbox("selectedDistilledSchemes", "Clone of Copy of Default Notification Scheme");
            checkCheckbox("selectedDistilledSchemes", "Clone of Another Notification Scheme");

            setFormElement("Clone of Copy of Default Notification Scheme", "New_Scheme_1");
            setFormElement("Clone of Another Notification Scheme", "New_Scheme_2");

            submit("Preview Changes");

            //Modify scheme name for a scheme
            gotoPage("secure/admin/EditNotificationScheme!default.jspa?schemeId=10010");
            setFormElement("name", "A new name");
            submit("Update");

            // Jump back into the wizard, this should be fine since the session is used to store the state
            gotoPage("secure/admin/SchemeMergePreview!default.jspa?selectedSchemeType=NotificationScheme&typeOfSchemesToDisplay=all");
            submit("Submit Changes");

            assertTextPresent("Could not save merged scheme <strong>New_Scheme_1</strong>:");
            assertTextPresent("Some of the original schemes (<strong>Copy of Default Notification Scheme</strong>)");

            assertTextPresent("You have successfully saved the following merged scheme(s)");
            assertTextPresent("New_Scheme_2");
            assertTextNotPresent("has been associated with project(s):");
        }
        finally
        {
            HttpUnitOptions.setScriptingEnabled(false);
        }
    }

    public void testModifySchemeDescDuringPreview()
    {
        try
        {
            HttpUnitOptions.setScriptingEnabled(true);
            gotoPage("secure/admin/SchemeTypePicker!switch.jspa?typeOfSchemesToDisplay=all&selectedSchemeType=NotificationScheme");

            submit("Analyse Schemes");

            checkCheckbox("selectedDistilledSchemes", "Clone of Copy of Default Notification Scheme");
            checkCheckbox("selectedDistilledSchemes", "Clone of Another Notification Scheme");

            setFormElement("Clone of Copy of Default Notification Scheme", "New_Scheme_1");
            setFormElement("Clone of Another Notification Scheme", "New_Scheme_2");

            submit("Preview Changes");

            //Modify scheme description for a scheme
            gotoPage("secure/admin/EditNotificationScheme!default.jspa?schemeId=10010");
            setFormElement("description", "A new description");
            submit("Update");

            // Jump back into the wizard, this should be fine since the session is used to store the state
            gotoPage("secure/admin/SchemeMergePreview!default.jspa?selectedSchemeType=NotificationScheme&typeOfSchemesToDisplay=all");
            submit("Submit Changes");

            assertTextPresent("Could not save merged scheme <strong>New_Scheme_1</strong>:");
            assertTextPresent("Some of the original schemes (<strong>Copy of Default Notification Scheme</strong>)");

            assertTextPresent("You have successfully saved the following merged scheme(s)");
            assertTextPresent("New_Scheme_2");
            assertTextNotPresent("has been associated with project(s):");
        }
        finally
        {
            HttpUnitOptions.setScriptingEnabled(false);
        }
    }

    public void testCancelSchemePreview() throws SAXException
    {
        try
        {
            HttpUnitOptions.setScriptingEnabled(true);
            gotoPage("secure/admin/SchemeTypePicker!switch.jspa?typeOfSchemesToDisplay=all&selectedSchemeType=NotificationScheme");

            submit("Analyse Schemes");

            checkCheckbox("selectedDistilledSchemes", "Clone of Copy of Default Notification Scheme");
            checkCheckbox("selectedDistilledSchemes", "Clone of Another Notification Scheme");

            setFormElement("Clone of Copy of Default Notification Scheme", "New_Scheme_1");
            setFormElement("Clone of Another Notification Scheme", "New_Scheme_2");

            submit("Preview Changes");

            //Check that the first new scheme contains all the right original schemes and project associations
            WebTable newSchemeTable1 = getDialog().getResponse().getTableWithID("New_Scheme_1_table");
            assertTrue(tableCellHasText(newSchemeTable1, 1, 0, "Copy of Default Notification Scheme"));
            assertTrue(tableCellHasText(newSchemeTable1, 1, 0, "Default Notification Scheme"));
            assertFalse(tableCellHasText(newSchemeTable1, 1, 0, "Copy of Another Notification Scheme"));
            assertFalse(tableCellHasText(newSchemeTable1, 1, 0, "Another Notification Scheme"));
            assertTrue(tableCellHasText(newSchemeTable1, 1, 1, PROJECT_HOMOSAP));

            //Check that the second new scheme contains all the right original schemes and project associations
            WebTable newSchemeTable2 = getDialog().getResponse().getTableWithID("New_Scheme_2_table");
            assertFalse(tableCellHasText(newSchemeTable2, 1, 0, "Copy of Default Notification Scheme"));
            assertFalse(tableCellHasText(newSchemeTable2, 1, 0, "Default Notification Scheme"));
            assertTrue(tableCellHasText(newSchemeTable2, 1, 0, "Copy of Another Notification Scheme"));
            assertTrue(tableCellHasText(newSchemeTable2, 1, 0, "Another Notification Scheme"));
            assertFalse(tableCellHasText(newSchemeTable2, 1, 1, PROJECT_HOMOSAP));
            //hack: Same as Cancel Button
            gotoPage("secure/admin/SchemeMerge!default.jspa?selectedSchemeType=NotificationScheme&typeOfSchemesToDisplay=all");

            // We would like to verify that the checkboxes are in the correct state but they are all named the same
            // thing and we do not have a method to differentiate on the value.

            assertFormElementEquals("Clone of Copy of Default Notification Scheme", "New_Scheme_1");
            assertFormElementEquals("Clone of Another Notification Scheme", "New_Scheme_2");

            // We would like to test more but the default state of the page is determined with javascript which seems
            // to confuse this weak minded tool :)
        }
        finally
        {
            HttpUnitOptions.setScriptingEnabled(false);
        }
    }
}
