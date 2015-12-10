package com.atlassian.jira.webtests.ztests.customfield;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Func test for Deleting Custom Fields.
 * Originally written for JRA-13904.
 *
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.CUSTOM_FIELDS, Category.FIELDS })
public class TestDeleteCustomField extends FuncTestCase
{
    @Override
    public void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestDeleteCustomField.xml");
    }

    /**
     * This test makes sure that if a Custom field is used in a notification schem, then deleting the Custom Field
     * will also delete the notification entity.
     */
    public void testRemoveCustomFieldFromNotifications()
    {
        // Go to notification_schemes
        navigation.gotoAdminSection("notification_schemes");
        tester.clickLink("10000_edit");
        text.assertTextPresent(locator.page(), "(My Best Group)");
        text.assertTextPresent(locator.page(), "(My Friend)");

        // Delete the Group picker Custom Field
        navigation.gotoAdminSection("view_custom_fields");
        tester.clickLink("del_customfield_10041");
        tester.submit("Delete");

        navigation.gotoAdminSection("notification_schemes");
        tester.clickLink("10000_edit");

        // Assert group picker is gone, and the user picker is still there.
        text.assertTextPresent(locator.page(), "(My Friend)");
        text.assertTextNotPresent(locator.page(), "(My Best Group)");

        // Delete the user picker Custom Field
        navigation.gotoAdminSection("view_custom_fields");
        tester.clickLink("del_customfield_10040");
        tester.submit("Delete");

        navigation.gotoAdminSection("notification_schemes");
        tester.clickLink("10000_edit");

        // Assert user picker gone.
        text.assertTextNotPresent(locator.page(), "(My Friend)");
        text.assertTextNotPresent(locator.page(), "(My Best Group)");
    }
}
