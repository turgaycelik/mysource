package com.atlassian.jira.webtests.ztests.tpm.ldap;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.TableLocator;

import com.meterware.httpunit.TableCell;
import com.meterware.httpunit.WebTable;

/**
 * Provides some shared functionality for the Lab Manager LDAP func tests.
 *
 * @since v4.3
 */
public abstract class AbstractTpmLdapTest extends FuncTestCase
{

    protected boolean isActiveDirectory()
    {
        String ldapType = getConfigurationOption("ldap.type");
        if (ldapType == null)
        {
            throw new IllegalStateException("Missing configuration for 'ldap.type'.");
        }
        if (ldapType.equals("ActiveDirectory"))
        {
            return true;
        }
        if (ldapType.equals("OpenLdap"))
        {
            return false;
        }
        throw new IllegalStateException("Unknown LDAP type '" + ldapType + "'");
    }


    /**
     * Will return "OpenLDAP" or "Microsoft Active Directory" depending on the LDAP server under test.
     *
     * @return "OpenLDAP" or "Microsoft Active Directory" depending on the LDAP server under test.
     */
    protected String getTypeDisplayName()
    {
        String ldapType = getConfigurationOption("ldap.type");
        if (ldapType == null)
        {
            throw new IllegalStateException("Missing configuration for 'ldap.type'.");
        }
        if (ldapType.equals("ActiveDirectory"))
        {
            return "Microsoft Active Directory";
        }
        if (ldapType.equals("OpenLdap"))
        {
            return "OpenLDAP";
        }
        throw new IllegalStateException("Unknown LDAP type '" + ldapType + "'");
    }

    protected String getUserDn()
    {
        String userDn = getConfigurationOption("ldap.userdn");
        if (userDn == null)
        {
            if (isActiveDirectory())
            {
                if (getLdapServer().equals("crowd-ad1.sydney.atlassian.com"))
                {
                    // Running the test locally against crowd-ad1:
                    return "cn=Administrator,cn=Users,dc=sydney,dc=atlassian,dc=com";
                }
                else
                {
                    // Running a proper Lab manager test:
                    return "cn=Administrator,cn=Users,dc=tpm,dc=atlassian,dc=com";
                }
            } else {
                // Open LDAP
                if (getLdapServer().equals("crowd-op23") || getLdapServer().equals("172.22.200.133"))
                {
                    // Running the test locally against crowd-op23:
                    return "o=sgi,c=us";
                }
                else
                {
                    return "cn=admin,dc=example,dc=com";
                }
            }
        } else
        {
            return userDn;
        }
    }

    protected String getPassword()
    {
        String password = getConfigurationOption("ldap.password");
        if (password == null)
        {
            if (isActiveDirectory())
            {
                if (getLdapServer().equals("crowd-ad1.sydney.atlassian.com"))
                {
                    // Running the test locally against crowd-ad1:
                    return "atlassian";
                }
                else
                {
                    // Running a proper Lab manager test:
                    return "5P3rtaaah";
                }
            } else {
                // Open LDAP
                return "secret";
            }
        }
        else
        {
            return password;
        }
    }

    protected String getBaseDn()
    {
        String baseDn = getConfigurationOption("ldap.basedn");
        if (baseDn == null)
        {
            if (isActiveDirectory())
            {
                if (getLdapServer().equals("crowd-ad1.sydney.atlassian.com"))
                {
                    // Running the test locally against crowd-ad1:
                    return "OU=People,dc=sydney,dc=atlassian,dc=com";
                }
                else
                {
                    // Running a proper Lab manager test:
                    return "dc=tpm,dc=atlassian,dc=com";
                }
            } else {
                // Open LDAP
                if (getLdapServer().equals("crowd-op23") || getLdapServer().equals("172.22.200.133"))
                {
                    // Running the test locally against crowd-op23:
                    return "ou=JIRA-TPM,o=sgi,c=us";
                }
                else
                {
                    return "dc=example,dc=com";
                }
            }
        }
        else
        {
            return baseDn;
        }
    }

    protected String getConfigurationOption(final String key)
    {
        String value = System.getProperty(key);
        if (value != null)
        {
            return value;
        }
        return environmentData.getProperty(key);
    }

    protected String getLdapServer()
    {
        // this allows us to connect to a remote ldap server for running inside IDE (set [ldap.server = crowd-op23] in localtest.properties)
        String server = getConfigurationOption("ldap.server");
        if (server == null)
        {
            if (isActiveDirectory())
            {
                // Use the actual hostname because of checks against the SSL certificate
                return "atlas-win32ie9.tpm.atlassian.com";
            }
            else
            {
                return "localhost";
            }
        }
        else
        {
            return server;
        }
    }

    protected void assertExtendedTestPageAndReturnToDirectoryList()
    {
        // Assert we are on the "Extended test" page
        tester.assertTextPresent("Test Remote Directory Connection");
        tester.assertTextPresent("For extended testing enter the credentials of a user in the remote directory");
        // click the link to go back to directory list
        tester.clickLinkWithText("Back to directory list");
    }

    protected void synchroniseDirectory(int row) throws InterruptedException
    {
        navigation.gotoAdminSection("user_directories");

        WebTable tblDirectoryList = new TableLocator(tester, "directory-list").getTable();
        log("Attempting to synchronise Directory " + row);
        TableCell operations = tblDirectoryList.getTableCell(row, 4);
        if (operations.asText().contains("Synchronising"))
        {
            // Looks like an automatic synch was kicked off in the background - just wait for it to complete.
            log("Directory " + row + " is already synchronising ... we will wait until it is complete.");
        }
        else
        {
            navigation.clickLink(operations.getLinkWith("Synchronise"));
        }
        // Synchronise is asynchronous, so we need to check until it is complete
        int attempts = 0;
        while (true)
        {
            Thread.sleep(100);
            // refresh the page to get latest synchronised status
            navigation.gotoAdminSection("user_directories");
            operations = new UserDirectoryTable(this).getTableCell(row, 4);
            if (operations.asText().contains("Synchronising"))
            {
                // synch in progress
                attempts++;
                if (attempts > 100)
                {
                    // Just in case...
                    fail("Directory did not finish synchronising. Giving up after " + attempts + " retries.");
                }
                log("Still synchronising ...");
            }
            else
            {
                // assert that the Synchronise link is now present
                assertNotNull(operations.getLinkWith("Synchronise"));
                log("Synchronise is finished.");
                break;
            }
        }
    }

}
