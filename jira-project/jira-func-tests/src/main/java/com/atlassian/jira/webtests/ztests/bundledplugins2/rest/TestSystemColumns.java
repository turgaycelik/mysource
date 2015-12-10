package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.backdoor.ColumnControl;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.sun.jersey.api.client.UniformInterfaceException;

import java.util.List;

/**
 * Tests for default system columns
 *
 * @since v6.1
 */
@WebTest({ Category.FUNC_TEST, Category.REST })
public class TestSystemColumns extends RestFuncTest
{
    private Function<ColumnControl.ColumnItem, String> COLUMNITEM_TO_STRING =
            new Function<ColumnControl.ColumnItem, String>()
            {
                @Override
                public String apply(ColumnControl.ColumnItem input)
                {
                    return input.value;
                }
            };

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreBlankInstance();
    }

    public void testGetAndSetSystemDefaultColumnsNotAdmin()
    {
        //Removing admin access
        backdoor.usersAndGroups().removeUserFromGroup("admin", "jira-administrators");

        //Access to get and set are forbidden
        assertFalse("Non admin has no access to set system columns", backdoor.columnControl().setSystemDefaultColumns(Lists.<String>newArrayList()));
        boolean hasException = false;

        try
        {
            backdoor.columnControl().getSystemDefaultColumns();
        }
        catch (UniformInterfaceException e)
        {
            //403 == forbidden
            if (e.getResponse().getStatus() == 403)
            {
                hasException = true;
            }
        }
        assertTrue("Non admin has no access to get system columns", hasException);

        //Need to restore admin back, otherwise it will break the instance and prevent data restore
        backdoor.usersAndGroups().addUserToGroup("admin", "jira-administrators");
    }

    public void testGetAndSetSystemDefaultColumns()
    {
        List<String> defaultColumns = Lists.newArrayList("issuetype", "issuekey", "summary", "assignee", "reporter",
                "priority", "status", "resolution", "created", "updated", "duedate");

        //System default is correct
        assertEquals(defaultColumns, Lists.transform(backdoor.columnControl().getSystemDefaultColumns(), COLUMNITEM_TO_STRING));

        List<String> systemColumns = Lists.newArrayList(defaultColumns);
        systemColumns.add("description");
        systemColumns.add("resolutiondate");
        systemColumns.remove("summary");
        systemColumns.remove("status");

        //Setting new system default
        assertTrue("No errors when setting the column", backdoor.columnControl().setSystemDefaultColumns(systemColumns));
        assertEquals(systemColumns, Lists.transform(backdoor.columnControl().getSystemDefaultColumns(), COLUMNITEM_TO_STRING));

        //User should inherit from system default
        assertEquals(systemColumns, Lists.transform(backdoor.columnControl().getLoggedInUserColumns(), COLUMNITEM_TO_STRING));

        List<String> userColumns = Lists.newArrayList(systemColumns);
        userColumns.add("status");
        userColumns.add("summary");
        //Setting new columns for new user should persist
        assertTrue("No errors when setting the column", backdoor.columnControl().setLoggedInUserColumns(userColumns));
        assertEquals(userColumns, Lists.transform(backdoor.columnControl().getLoggedInUserColumns(), COLUMNITEM_TO_STRING));

        //User columns don't affect system columns
        assertEquals(systemColumns, Lists.transform(backdoor.columnControl().getSystemDefaultColumns(), COLUMNITEM_TO_STRING));
        assertTrue("No errors when removing all columns", backdoor.columnControl().setSystemDefaultColumns(Lists.<String>newArrayList()));
        assertEquals(0, backdoor.columnControl().getSystemDefaultColumns().size());
    }
}
