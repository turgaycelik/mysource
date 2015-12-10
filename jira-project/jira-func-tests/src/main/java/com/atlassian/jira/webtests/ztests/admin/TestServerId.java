package com.atlassian.jira.webtests.ztests.admin;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.license.DefaultSIDManager;
import com.atlassian.license.SIDManager;
import com.meterware.httpunit.WebTable;
import org.xml.sax.SAXException;

/**
 * 
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.BROWSING })
public class TestServerId extends FuncTestCase
{
    private final SIDManager sidManager = new DefaultSIDManager();

    public void testServerIdIdenticalAfterRestore() throws SAXException
    {
        administration.restoreData("TestServerId.xml");

        navigation.gotoAdminSection("license_details");

        text.assertTextPresent(locator.page(), "Server ID");

        // Get the serverId from the license table.
        final WebTable licenseTable = tester.getDialog().getResponse().getTableWithID("license_table");
        final String serverId = licenseTable.getCellAsText(3,1);

        assertEquals("Server ID same as stored", "ALA0-Y7A3-TR93-KAHT", serverId.trim());
    }

    public void testServerIdGeneratedFresh() throws SAXException
    {
        administration.restoreBlankInstance();

        navigation.gotoAdminSection("license_details");

        text.assertTextPresent(locator.page(), "Server ID");

        //Get the serverId from the license table.
        final WebTable licenseTable = tester.getDialog().getResponse().getTableWithID("license_table");
        final String serverId = licenseTable.getCellAsText(3,1);

        assertTrue("Valid SID", sidManager.isValidSID(serverId.trim()));
    }
}
