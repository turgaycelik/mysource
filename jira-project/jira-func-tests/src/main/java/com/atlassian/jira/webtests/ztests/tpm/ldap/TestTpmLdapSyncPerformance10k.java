package com.atlassian.jira.webtests.ztests.tpm.ldap;

import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.TableLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebTable;
import org.junit.Ignore;

/**
 * This TestCase is basically a standalone test to test LDAP performance against active directory.
 *
 * This is not run automatically
 *
 * @since v4.3
 */
@Ignore ("LDAP Performance test.  Only ever run singularly in a special TPM environment")
@WebTest({ Category.TPM, Category.LDAP, Category.PERFORMANCE })
public class TestTpmLdapSyncPerformance10k extends AbstractTpmLdapTest
{
    public void testADSyncPerformance() throws Exception
    {
        administration.restoreBlankInstance();
        navigation.gotoAdminSection("user_directories");
        tester.assertTextPresent("User Directories");

        log("Attempting to add an Active Directory LDAP User Directory...");
        addActiveDirectory();

        // Currently the Internal Directory is first in the list:
        WebTable table = assertions.getTableAssertions().getWebTable("directory-list");
        assertions.getTableAssertions().assertTableCellHasText(table, 1, 1, "JIRA Internal Directory");
        assertions.getTableAssertions().assertTableCellHasText(table, 2, 1, "LDAP Directory");
        // Move LDAP to top
        WebTable tblDirectoryList = new TableLocator(tester, "directory-list").getTable();
        WebLink link = tblDirectoryList.getTableCell(2, 3).getLinkWith("up");

        navigation.clickLink(link);
        table = assertions.getTableAssertions().getWebTable("directory-list");
        assertions.getTableAssertions().assertTableCellHasText(table, 1, 1, "LDAP Directory");
        assertions.getTableAssertions().assertTableCellHasText(table, 2, 1, "JIRA Internal Directory");

        // Now wait for the synchronisation to end
        int count = 0;
        while (!tester.getDialog().isTextInResponse("Last synchronised"))
        {
            try
            {
                Thread.sleep(10000);
            }
            catch (InterruptedException e)
            {
                // Not expected.
                throw new RuntimeException(e);
            }
            // Safety net to make sure that we don't get in an infinite loop.
            count++;
            if (count >= 60)
            {
                fail("Active directory sync has taken too long!");
            }
            navigation.gotoAdminSection("user_directories");
        }
        String response = tester.getDialog().getResponseText();
        int i = response.indexOf("Last synchronised");
        int indexSecondsTakenStart = response.indexOf("(took ", i) + 6;
        int indexSecondsTakenEnd = response.indexOf("s).", indexSecondsTakenStart);
        int seconds = Integer.parseInt(response.substring(indexSecondsTakenStart, indexSecondsTakenEnd));

        if (seconds > 300)
        {
            fail("Active directory sync has taken too long! ( " + seconds + ")" );
        }


    }

    private void addActiveDirectory()
    {
        navigation.gotoPage("/plugins/servlet/embedded-crowd/configure/ldap/");
        text.assertTextPresent(new IdLocator(tester, "embcwd"), "Server Settings");

        tester.setWorkingForm("configure-ldap-form");
        // Set the required Simple fields
        tester.setFormElement("name", "LDAP Directory");
        tester.selectOption("type", "Microsoft Active Directory");
        tester.setFormElement("hostname", getLdapServer());
        // AD will not allow mutating operations unless you use SSL
//        tester.setFormElement("port", "636");
//        tester.checkCheckbox("useSSL", "true");
        tester.setFormElement("ldapUserdn", getUserDn());
        tester.setFormElement("ldapPassword", getPassword());
        tester.setFormElement("ldapBasedn", getBaseDn10K());

        // Set the advanced fields manually - Func tests don't have javascript to do this for us
        tester.setFormElement("ldapUserObjectclass", "user");
        tester.setFormElement("ldapUserFilter", "(&(objectCategory=Person)(sAMAccountName=*))");
        tester.setFormElement("ldapUserUsername", "sAMAccountName");
        tester.setFormElement("ldapUserUsernameRdn", "cn");
        tester.setFormElement("ldapUserFirstname", "givenName");
        tester.setFormElement("ldapUserLastname", "sn");
        tester.setFormElement("ldapUserDisplayname", "displayName");
        tester.setFormElement("ldapUserEmail", "mail");
        tester.setFormElement("ldapUserGroup", "memberOf");
        tester.setFormElement("ldapUserPassword", "unicodePwd");
        tester.setFormElement("ldapGroupObjectclass", "group");
        tester.setFormElement("ldapGroupFilter", "(objectCategory=Group)");
        tester.setFormElement("ldapGroupName", "cn");
        tester.setFormElement("ldapGroupDescription", "description");
        tester.setFormElement("ldapGroupUsernames", "member");
        tester.setFormElement("ldapPermissionOption", "READ_WRITE");
        tester.checkCheckbox("ldapPagedresults", "true");
        
        // Add the new Directory
        tester.submit("test");
        text.assertTextPresent("Connection test successful");

        tester.submit("save");

        text.assertTextPresent("The table below shows the user directories currently configured for JIRA.");
        tester.assertTextPresent("JIRA Internal Directory");
        tester.assertTextPresent("LDAP Directory");
    }

    private String getBaseDn10K()
    {
        if (getLdapServer().equals("crowd-ad1"))
        {
            // Running the test locally against crowd-ad1:
            return "ou=loadTesting10k,dc=sydney,dc=atlassian,dc=com";
        }
        else
        {
            // Running a proper Lab manager test:
            return "ou=loadTesting10k,dc=tpm,dc=atlassian,dc=com";
        }
    }

}