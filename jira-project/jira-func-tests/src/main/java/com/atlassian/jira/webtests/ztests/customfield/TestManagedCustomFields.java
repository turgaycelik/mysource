package com.atlassian.jira.webtests.ztests.customfield;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.backdoor.ManagedConfigurationControl;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.TableCellLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Tests the Managed Configuration Entities API.
 *
 * @since v5.2
 */
@WebTest ({ Category.FUNC_TEST, Category.CUSTOM_FIELDS, Category.FIELDS })
public class TestManagedCustomFields extends FuncTestCase
{
    private static final Long MY_TEXT_FIELD_ID_LONG = 10000L;
    private static final String MY_TEXT_FIELD_ID = "customfield_" + MY_TEXT_FIELD_ID_LONG;
    private static final String MY_TEXT_FIELD_NAME = "MY TEXT FIELD";

    public void testManagedCustomField() throws Exception
    {
        administration.restoreData("TestEditCustomFieldDescription.xml");

        ManagedConfigurationControl managedConfiguration = backdoor.managedConfiguration();

        // by default the field should not be managed
        ManagedConfigurationControl.ManagedEntity managedCustomField = managedConfiguration.getManagedCustomField(MY_TEXT_FIELD_ID);
        assertFalse("Custom field should not be locked", managedCustomField.isLocked());
        assertFalse("Custom field should not be managed", managedCustomField.isManaged());
        verifyCustomField(false, false);

        // make the field managed
        managedCustomField = managedConfiguration.postManagedCustomField(MY_TEXT_FIELD_ID, true, false);
        assertFalse("Custom field should not be locked", managedCustomField.isLocked());
        assertTrue("Custom field should be managed", managedCustomField.isManaged());
        verifyCustomField(true, false);

        // make the field locked
        managedCustomField = managedConfiguration.postManagedCustomField(MY_TEXT_FIELD_ID, true, true);
        assertTrue("Custom field should be locked", managedCustomField.isLocked());
        assertTrue("Custom field should be managed", managedCustomField.isManaged());
        verifyCustomField(true, true);
    }

    // note: this verification could be a lot more extensive - need to expand
    private void verifyCustomField(boolean isManaged, boolean isLocked)
    {
        navigation.gotoCustomFields();

        Locator locator = new TableCellLocator(getTester(), "custom-fields", 1, 0);
        assertions.getTextAssertions().assertTextPresent(locator, MY_TEXT_FIELD_NAME);

        if (isLocked)
        {
            assertions.getTextAssertions().assertTextPresent(locator, "Locked");
            assertions.assertNodeByIdDoesNotExist("edit_" + MY_TEXT_FIELD_NAME);
            assertions.assertNodeByIdDoesNotExist("del_" + MY_TEXT_FIELD_ID);
            assertions.getLinkAssertions().assertLinkByIdHasExactText("config_" + MY_TEXT_FIELD_ID, "View");

            // attempt to edit the field
            tester.gotoPage("/secure/admin/EditCustomField!default.jspa?id=" + MY_TEXT_FIELD_ID_LONG);
            assertions.getJiraMessageAssertions().assertHasMessage("You cannot edit field '" + MY_TEXT_FIELD_NAME + "' as it is locked.");
            assertions.assertNodeByIdDoesNotExist("update_submit");
        }
        else if (isManaged)
        {
            assertions.getTextAssertions().assertTextPresent(locator, "Managed");
            assertions.assertNodeByIdExists("edit_" + MY_TEXT_FIELD_NAME);
            assertions.assertNodeByIdExists("del_" + MY_TEXT_FIELD_ID);
            assertions.getLinkAssertions().assertLinkByIdHasExactText("config_" + MY_TEXT_FIELD_ID, "Configure");

            // attempt to edit the field
            tester.gotoPage("/secure/admin/EditCustomField!default.jspa?id=" + MY_TEXT_FIELD_ID_LONG);
            assertions.assertNodeByIdExists("update_submit");
        }
        else
        {
            assertions.getTextAssertions().assertTextNotPresent(locator, "Managed");
            assertions.getTextAssertions().assertTextNotPresent(locator, "Locked");
        }
    }
}
