package com.atlassian.jira.webtests.ztests.admin.index;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

/**
 * Tests admin reindex page
 *
 * @since v3.13
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.BROWSING })
public class TestIndexAdmin extends JIRAWebTest
{
    private static final int RETRY_COUNT = 100;
    private static final int SLEEP_TIME = 1000;
    private static final String REFRESH_BUTTON = "Refresh";
    private static final String ACKNOWLEDGE_BUTTON = "Acknowledge";
    private static final String REINDEX_BUTTON = "reindex";
    private static final String DONE_BUTTON = "Done";

    public TestIndexAdmin(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestIndexAdmin.xml");
    }

    /** Make sure that a SystemAdmin can re-index. */

    public void testReindexAsSystemAdmin()
    {
        gotoIndexing();

        assertTextPresent("JIRA will be unavailable to all users until the re-index is complete.");
        executeReindex();
    }


    /** Now that nobody can change index paths under normal circumstances we don't show the silly message any more.
     * so this is the same as a sysadmin.  */

    public void testReindexAsNormalAdmin()
    {
        //login as a user who only has administrator permissions.
        login("miniadmin", "miniadmin");

        gotoIndexing();

        assertTextPresent("JIRA will be unavailable to all users until the re-index is complete.");

        executeReindex();
    }

    /** Test task acknowledgement. */

    public void testTaskAcknowledgement()
    {
        gotoIndexing();

        assertTextPresent("JIRA will be unavailable to all users until the re-index is complete.");

        submit(REINDEX_BUTTON);
        String taskPage = waitForIndexCompletetion();

        //switch users.
        login("miniadmin", "miniadmin");

        //this user should only see a reindex page
        gotoPage(taskPage);
        assertSubmitButtonPresent(DONE_BUTTON);
        assertSubmitButtonNotPresent(ACKNOWLEDGE_BUTTON);
        assertTextPresent("who started this task should acknowledge it.");
        validateProgressBarUI(DONE_BUTTON);
        //the user who started the task should be mentioned.
        assertLinkPresentWithText(ADMIN_USERNAME, 0);
        assertLinkPresentWithText(ADMIN_USERNAME, 1);

        //okay lets go back to the index screen.
        submit(DONE_BUTTON);
        assertTrue(getRedirect().indexOf("IndexAdmin.jspa?reindexTime=") != -1);

        //lets now ack the task and make sure that it still works.
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        gotoPage(taskPage);
        acknowledgeSuccessfulTask();

        //make sure the submitting user cannot see the an acked task.
        gotoPage(taskPage);
        checkNoTaskPage();

        //make sure the submitting user other user cannot see the acked task.
        login("miniadmin", "miniadmin");
        gotoPage(taskPage);
        checkNoTaskPage();
    }

    private void checkNoTaskPage()
    {
        assertTextPresent("A task could not be found for the given task id");
        assertTextPresent("Task Not Found");

        submit(DONE_BUTTON);

        assertTrue(getRedirect().indexOf("IndexAdmin.jspa") != -1);

        assertTextNotPresent("successful");
    }

    private void gotoIndexing()
    {
        gotoAdmin();
        clickLink("indexing");
    }

    private void executeReindex()
    {

        submit(REINDEX_BUTTON);

        waitForIndexCompletetion();
        acknowledgeSuccessfulTask();
    }

    private String waitForIndexCompletetion()
    {
        return this.waitForIndexCompletetion(SLEEP_TIME, RETRY_COUNT);
    }

    private String waitForIndexCompletetion(long sleepTime, int retryCount)
    {
        String redirect = getRedirect();
        assertTrue(redirect.indexOf("IndexProgress.jspa?taskId=") != -1);

        for (int i = 0; i < retryCount; i++)
        {
            if (getDialog().hasSubmitButton(REFRESH_BUTTON))
            {
                validateProgressBarUI(REFRESH_BUTTON);
                submit(REFRESH_BUTTON);
            }
            else if (getDialog().hasSubmitButton(ACKNOWLEDGE_BUTTON))
            {
                validateProgressBarUI(ACKNOWLEDGE_BUTTON);
                //we should be the submitting user.
                assertTextNotPresent("who started this task should acknowledge it.");

                return redirect;
            }
            else
            {
                fail("Unexpected button on progress screen.");
            }

            try
            {
                Thread.sleep(sleepTime);
            }
            catch (InterruptedException e)
            {
                //ignore me.
            }
        }

        fail("Index operation did not complete after " + (sleepTime * retryCount / 1000d) + " sec.");

        return redirect;
    }

    private void acknowledgeSuccessfulTask()
    {
        submit(ACKNOWLEDGE_BUTTON);

        //do we get redirected to the correct page.
        assertTrue(getRedirect().indexOf("IndexAdmin.jspa?reindexTime=") != -1);

        //we should be successful. 
        assertTextPresent("successful");
    }
}
