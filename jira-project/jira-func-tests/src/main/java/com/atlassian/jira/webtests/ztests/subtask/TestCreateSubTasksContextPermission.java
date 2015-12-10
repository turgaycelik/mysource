package com.atlassian.jira.webtests.ztests.subtask;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

@WebTest ({ Category.FUNC_TEST, Category.PERMISSIONS, Category.SUB_TASKS })
public class TestCreateSubTasksContextPermission extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        administration.restoreData("TestCreateSubTaskContextPermission.xml");
    }

    public void testCreateSubTasks()
    {
        navigation.issue().createSubTask("HSP-1", "Sub-task", "summary", "Subby 99");
        tester.assertTextPresent("Subby 99");
        tester.assertTextPresent("HSP-1");
    }
}
