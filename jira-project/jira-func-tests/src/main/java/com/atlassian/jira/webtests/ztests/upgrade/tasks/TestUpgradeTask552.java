package com.atlassian.jira.webtests.ztests.upgrade.tasks;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.backdoor.ColumnControl;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.TableLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.meterware.httpunit.WebTable;

import java.util.List;

/**
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.UPGRADE_TASKS, Category.SLOW_IMPORT })
public class TestUpgradeTask552 extends FuncTestCase
{
    private static final String CUSTOM_FIELDS_ADMIN_TABLE_SELECTOR = "custom-fields";

    public void testUpgrade()
    {
         //this data has the following setup:
        // * 4 labels custom fields: 'Labels', 'Labels' (with a project context), 'Epic', and 'Tags'.
        // * default navigator columns with all 4 labels fields
        // * user navigator column default with 'Labels', 'Labels', and 'Epic' on it for user admin
        // * a filter ('custom field that becomes system field') navigator column layout with 'Labels', 'Labels', and 'Tags'
        // * a filter ('custom field that becomes system field') that should be renamed
        // * a filter ('custom field that remains a custom field') whose query shouldn't be renamed
        //
        // Once imported, the two 'Labels' custom fields should be merged into the 'Labels' system field.
        //
        // The data also has a custom screen config & custom field config.  Once upgraded, these should show the
        // new labels system field correctly.
        //don't want to refresh the caches here to make sure the upgrade task updates the caches properly!
        administration.restoreDataSlowOldWay("TestUpgradeTask552.xml");

        //check 'Labels' custom fields are gone
        navigation.gotoAdminSection("view_custom_fields");
        tester.assertTextPresent("Custom Fields");
        WebTable customFieldsTable = new TableLocator(tester, CUSTOM_FIELDS_ADMIN_TABLE_SELECTOR).getTable();
        assertEquals(3, customFieldsTable.getRowCount());
        text.assertTextPresent(new TableLocator(tester, CUSTOM_FIELDS_ADMIN_TABLE_SELECTOR), "Epic");
        text.assertTextPresent(new TableLocator(tester, CUSTOM_FIELDS_ADMIN_TABLE_SELECTOR), "Tags");

        //check the default issue navigator columns
        final Function<ColumnControl.ColumnItem, String> extractLabelsFromColumns = new Function<ColumnControl.ColumnItem, String>() {
            public String apply(ColumnControl.ColumnItem column) { return column.label; }
        };
        List<String> systemColumns = Lists.transform(backdoor.columnControl().getSystemDefaultColumns(), extractLabelsFromColumns);
        assertTrue("Column Labels is present", systemColumns.contains("Labels"));
        assertTrue("Column Epic is present", systemColumns.contains("Epic"));
        assertTrue("Column Tags is present", systemColumns.contains("Tags"));
    }

    public void testUpgradeNoSystemField()
    {
        //don't want to refresh the caches here to make sure the upgrade task updates the caches properly!
        administration.restoreDataSlowOldWay("TestUpgradeTask552NoSystemField.xml");

        navigation.gotoAdminSection("view_custom_fields");
        text.assertTextPresent("Epic/Theme");

        navigation.issue().viewIssue("HSP-1");
        text.assertTextPresent("Epic/Theme");
        text.assertTextSequence(new IdLocator(tester, "customfield_10000-val"), "This", "a", "is", "test");
    }

    public void testUpgradeSystemField()
    {
        //don't want to refresh the caches here to make sure the upgrade task updates the caches properly!
        administration.restoreDataSlowOldWay("TestUpgradeTask552SystemField.xml");

        navigation.issue().viewIssue("UPG-2");
        text.assertTextPresent("Label");
        text.assertTextPresent("TestLabel");
        text.assertTextPresent(new IdLocator(tester, "labels-10001-value"), "two");
    }
}
