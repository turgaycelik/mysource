package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.rest.api.gadget.AdminGadget;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.AdminGadgetClient;

/**
 * Tests the "Admin Gadget".
 *
 * @since 6.0
 */
@WebTest ({ Category.FUNC_TEST, Category.REST, Category.GADGETS })
public class TestAdminGadget extends FuncTestCase
{

    private AdminGadgetClient adminGadgetClient;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        adminGadgetClient = new AdminGadgetClient(getEnvironmentData());
    }

    public void testAdminGadgetAfterInstall()
    {
        administration.restoreData("TestAdminGadgetInstall.xml");

        executeAdminGadget();
    }

    public void testAdminGadgetAfterUpgrade()
    {
        administration.restoreData("TestAdminGadgetUpdate.xml");

        executeAdminGadget();
    }

    private void executeAdminGadget()
    {
        adminGadgetClient.setTaskListDone("gettingstarted");
        adminGadgetClient.setTaskListDone("domore");
        AdminGadget adminGadget = adminGadgetClient.get();
        assert(adminGadget.tasks.getGettingStarted().isDismissed());
        assert(adminGadget.tasks.getDoMore().isDismissed());
        adminGadgetClient.setTaskListUnDone("gettingstarted");
        adminGadgetClient.setTaskListUnDone("domore");
        adminGadget = adminGadgetClient.get();
        assertFalse(adminGadget.tasks.getGettingStarted().isDismissed());
        assertFalse(adminGadget.tasks.getDoMore().isDismissed());
    }

}
