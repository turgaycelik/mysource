package com.atlassian.jira.webtests.ztests.tpm.ldap;

import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.TableLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebTable;
import org.apache.commons.lang.StringUtils;

import junit.framework.AssertionFailedError;

/**
 * This TestCase must be run first in the TPM LDAP test suite - it creates the LDAP Directory.
 * To be run against Active Directory or Open LDAP in TPM.
 *
 * @since v4.3
 */
@WebTest ({ Category.FUNC_TEST, Category.LDAP, Category.TPM })
public class TestTpmLdapSetup extends AbstractTpmLdapTest
{
    /*
     * You can easily run it from IDE with -Dldap.type=ActiveDirectory -Dldap.server=crowd-ad1.sydney.atlassian.com -Dldap.nossl=true
     * You can also run it against arbitrary LDAP e.g. the one in Gdansk office:
     * -Dldap.type=OpenLdap -Dldap.server=rabbit -Dldap.userdn="cn=admin,dc=atlassian,dc=pl" -Dldap.password=admin123 -Dldap.basedn="dc=atlassian,dc=pl"
     *
     * Notice: Active Directory will not allow for modification via unencrypted connection. You can read more about
     * configuring SSL with Active Directory here: https://extranet.atlassian.com/x/6QDDHQ
     */
    public void testAddLdapDirectory() throws Exception
    {
        administration.restoreBlankInstance();
        navigation.gotoAdminSection("user_directories");
        tester.assertTextPresent("User Directories");

        // We run these tests against Active Directory and OpenLDAP, but we need different connection settings for each.
        if (isActiveDirectory())
        {
            log("Attempting to add an Active Directory LDAP User Directory...");
            addActiveDirectory();
        }
        else
        {
            log("Attempting to add an Open LDAP User Directory...");
            addOpenLdap();
        }

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

        synchroniseDirectory(1);
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
        if (!Boolean.valueOf(StringUtils.defaultString(getConfigurationOption("ldap.nossl"), "false")))
        {
            tester.setFormElement("port", "636");
            tester.checkCheckbox("useSSL", "true");
        }
        tester.setFormElement("ldapUserdn", getUserDn());
        tester.setFormElement("ldapPassword", getPassword());
        tester.setFormElement("ldapBasedn", getBaseDn());

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
        tester.setFormElement("ldapExternalId", "objectGUID");

        // Add the new Directory
        tester.submit("test");
        text.assertTextPresent("Connection test successful");

        tester.submit("save");
        // Now we are forced to the "Extended test" page
        assertExtendedTestPageAndReturnToDirectoryList();

        text.assertTextPresent("The table below shows the user directories currently configured for JIRA.");
        tester.assertTextPresent("JIRA Internal Directory");
        tester.assertTextPresent("LDAP Directory");
    }

    private void addOpenLdap()
    {
        navigation.gotoPage("/plugins/servlet/embedded-crowd/configure/ldap/");
        text.assertTextPresent(new IdLocator(tester, "embcwd"), "Server Settings");

        tester.setWorkingForm("configure-ldap-form");
        // Set the required Simple fields
        tester.setFormElement("name", "LDAP Directory");
        tester.selectOption("type", "OpenLDAP");
        // Allow flexibility of setting up localtest.properties to run test locally against crowd-op23
        tester.setFormElement("hostname", getLdapServer());
        tester.setFormElement("port", "389");
        tester.setFormElement("ldapUserdn", getUserDn());
        tester.setFormElement("ldapBasedn", getBaseDn());
        tester.setFormElement("ldapPassword", getPassword());
        // Set the advanced fields manually - Func tests don't have javascript to do this for us
        tester.setFormElement("ldapUserObjectclass", "inetorgperson");
        tester.setFormElement("ldapUserFilter", "(objectclass=inetorgperson)");
        tester.setFormElement("ldapUserUsername", "cn");
        tester.setFormElement("ldapUserUsernameRdn", "cn");
        tester.setFormElement("ldapUserFirstname", "givenName");
        tester.setFormElement("ldapUserLastname", "sn");
        tester.setFormElement("ldapUserDisplayname", "displayName");
        tester.setFormElement("ldapUserEmail", "mail");
        tester.setFormElement("ldapUserGroup", "memberOf");
        tester.setFormElement("ldapUserPassword", "userPassword");
        tester.setFormElement("ldapGroupObjectclass", "groupOfUniqueNames");
        tester.setFormElement("ldapGroupFilter", "(objectclass=groupOfUniqueNames)");
        tester.setFormElement("ldapGroupName", "cn");
        tester.setFormElement("ldapGroupDescription", "description");
        tester.setFormElement("ldapGroupUsernames", "uniqueMember");
        tester.setFormElement("ldapPermissionOption", "READ_WRITE");
        tester.setFormElement("ldapExternalId", "entryUUID");

        // Add the new Directory
        tester.submit("test");
        text.assertTextPresent("Connection test successful");

        tester.submit("save");
        // Now we are forced to the "Extended test" page
        assertExtendedTestPageAndReturnToDirectoryList();

        text.assertTextPresent("The table below shows the user directories currently configured for JIRA.");
        tester.assertTextPresent("JIRA Internal Directory");
        tester.assertTextPresent("LDAP Directory");
    }
}
