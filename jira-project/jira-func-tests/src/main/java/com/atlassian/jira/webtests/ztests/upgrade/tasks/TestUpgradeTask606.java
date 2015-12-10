package com.atlassian.jira.webtests.ztests.upgrade.tasks;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Checks blowing away the "jira.path.backup" property
 *
 * @since v4.3
 */
@WebTest ({ Category.FUNC_TEST, Category.UPGRADE_TASKS })
public class TestUpgradeTask606 extends JIRAWebTest
{
    public static final String SYSTEM_INFO_PAGE = "/secure/admin/ViewSystemInfo.jspa";

    public TestUpgradeTask606(String name)
    {
        super(name);
    }

    public void testNoBackupProperty()   throws Exception
    {
        restoreData("TestUpgradeTask606.xml");

        log("Goto Sytem info");
        gotoPage(SYSTEM_INFO_PAGE);

        assertTextPresent("jira.autoexport");
        assertTextNotPresent("jira.path.backup");
    }


    /**
     * This converts a local time string (Which is what we have in the export file we are testing against, to a string in the
     * format we expect to be stored in User Attributes.
     * @param localTimeString  Date/Time as a String in the system local time
     * @return DateTime as a String in UTC time zone
     */
    private String convertLocalTimeStringToUTC(String localTimeString) throws Exception
    {
        DateFormat localDf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        localDf.setTimeZone(TimeZone.getDefault());
        Date date = localDf.parse(localTimeString);

        DateFormat utcDf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        utcDf.setTimeZone(TimeZone.getTimeZone("UTC"));

        return utcDf.format(date);
    }

}