package com.atlassian.jira.webtests.ztests.admin.scheme;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.meterware.httpunit.WebTable;
import org.xml.sax.SAXException;

/**
 * 
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.SCHEMES })
public class TestSchemePurgeTool extends JIRAWebTest
{
    public TestSchemePurgeTool(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        //we can re-use the same data here.
        restoreData("TestSchemeMergeTool.xml");
    }

    public void testDeleteAllPermissionSchemes() throws SAXException
    {
        gotoPage("secure/admin/SchemePurgeTypePicker!default.jspa");

        assertTextPresent("Bulk Delete Schemes: Select Schemes");

        //Check that the correct schemes are being merged.
        WebTable schemeTable = getDialog().getResponse().getTableWithID("purge_schemes_PermissionScheme");
        assertEquals("Correct number of rows", 4, schemeTable.getRowCount());
        assertTrue(tableCellHasText(schemeTable, 1, 1, "Another Permission Scheme"));
        assertTrue(tableCellHasText(schemeTable, 2, 1, "Copy of Another Permission Scheme"));
        assertTrue(tableCellHasText(schemeTable, 3, 1, "Copy of Default Permission Scheme"));

        //Select all unassociated permission schemes to be deleted.
        checkCheckbox("selectedSchemeIds", "10000");
        checkCheckbox("selectedSchemeIds", "10001");
        checkCheckbox("selectedSchemeIds", "10002");

        submit("Preview");

        //Check that the preview screen has all selected schemes
        assertTextPresent("Bulk Delete Schemes: Confirm Schemes to Delete");
        assertTextPresent("Another Permission Scheme");
        assertTextPresent("Copy of Another Permission Scheme");
        assertTextPresent("Copy of Default Permission Scheme");

        submit("Delete Schemes");

        //Check that all schemes have been deleted successfully.
        assertTextPresent("Bulk Delete Schemes: Results");
        assertTextPresent("Another Permission Scheme");
        assertTextPresent("Copy of Another Permission Scheme");
        assertTextPresent("Copy of Default Permission Scheme");

        //Check on the ViewPermissionSchemes page that the schemes have really been deleted.
        gotoPage("secure/admin/ViewPermissionSchemes.jspa");
        assertTextNotPresent("Another Permission Scheme");
        assertTextNotPresent("Copy of Another Permission Scheme");
        assertTextNotPresent("Copy of Default Permission Scheme");
        assertTextPresent("Default Permission Scheme");

    }

    public void testDeleteAllNotificationSchemes() throws SAXException
    {
        gotoPage("secure/admin/SchemePurgeTypePicker!return.jspa?selectedSchemeType=NotificationScheme");

        assertTextPresent("Bulk Delete Schemes: Select Schemes");

        //Check that the correct schemes are being merged.
        WebTable schemeTable = getDialog().getResponse().getTableWithID("purge_schemes_NotificationScheme");
        assertEquals("Correct number of rows", 4, schemeTable.getRowCount());
        assertTrue(tableCellHasText(schemeTable, 1, 1, "Another Notification Scheme"));
        assertTrue(tableCellHasText(schemeTable, 2, 1, "Copy of Another Notification Scheme"));
        assertTrue(tableCellHasText(schemeTable, 3, 1, "Copy of Default Notification Scheme"));

        //Select all unassociated notification schemes to be deleted.
        checkCheckbox("selectedSchemeIds", "10010");
        checkCheckbox("selectedSchemeIds", "10011");
        checkCheckbox("selectedSchemeIds", "10012");

        submit("Preview");

        //Check that the preview screen has all selected schemes
        assertTextPresent("Bulk Delete Schemes: Confirm Schemes to Delete");
        assertTextPresent("Another Notification Scheme");
        assertTextPresent("Copy of Another Notification Scheme");
        assertTextPresent("Copy of Default Notification Scheme");

        submit("Delete Schemes");

        //Check that all schemes have been deleted successfully.
        assertTextPresent("Bulk Delete Schemes: Results");
        assertTextPresent("Another Notification Scheme");
        assertTextPresent("Copy of Another Notification Scheme");
        assertTextPresent("Copy of Default Notification Scheme");

        //Check on the ViewNotificationSchemes page that the schemes have really been deleted.
        gotoPage("secure/admin/ViewNotificationSchemes.jspa");
        assertTextNotPresent("Another Notification Scheme");
        assertTextNotPresent("Copy of Another Notification Scheme");
        assertTextNotPresent("Copy of Default Notification Scheme");
        assertTextPresent("Default Notification Scheme");
    }

    public void testDeleteWithNoSelectedSchemes()
    {
        gotoPage("secure/admin/SchemePurgeTypePicker!default.jspa");

        assertTextPresent("Bulk Delete Schemes: Select Schemes");

        //Preview without selecting anything
        submit("Preview");

        assertTextPresent("No schemes were selected. Please select at least one scheme.");
    }

    public void testAssociatingASchemeWhileDeleting()
    {
        gotoPage("secure/admin/SchemePurgeTypePicker!default.jspa");

        //Select all unassociated permission schemes to be deleted.
        checkCheckbox("selectedSchemeIds", "10000");
        checkCheckbox("selectedSchemeIds", "10001");
        checkCheckbox("selectedSchemeIds", "10002");

        submit("Preview");

        //Associate one of the schemes above with a project.
        associatePermSchemeToProject(PROJECT_HOMOSAP, "Another Permission Scheme");

        gotoPage("secure/admin/SchemePurgeToolPreview!default.jspa?selectedSchemeType=PermissionScheme");

        submit("Delete Schemes");

        // Check that the newly associated Scheme could not be deleted.
        assertTextPresent("Can not delete scheme, <strong>Another Permission Scheme</strong>. It has been associated with a project since it was originally selected.");
        assertTextPresent("Copy of Another Permission Scheme");
        assertTextPresent("Copy of Default Permission Scheme");

         //Check on the ViewPermissionSchemes page that the correct schemes have been deleted.
        gotoPage("secure/admin/ViewPermissionSchemes.jspa");
        assertTextPresent("Another Permission Scheme");
        assertTextNotPresent("Copy of Another Permission Scheme");
        assertTextNotPresent("Copy of Default Permission Scheme");
        assertTextPresent("Default Permission Scheme");
    }

    public void testCannotDeleteDefaultPermissionScheme()
    {
        restoreData("TestPurgeDefaultPermissionScheme.xml");

        gotoPage("secure/admin/SchemePurgeTypePicker!default.jspa");

        assertTextNotPresent("Default Permission Scheme");

        gotoPage("secure/admin/SchemePurgeToolPreview!default.jspa?selectedSchemeType=PermissionScheme&selectedSchemeIds=0");

        submit("Delete Schemes");

        //Check on the ViewPermissionSchemes page that the Default Permission Scheme has not been deleted.
        gotoPage("secure/admin/ViewPermissionSchemes.jspa");
        assertTextPresent("Default Permission Scheme");

        
    }
}