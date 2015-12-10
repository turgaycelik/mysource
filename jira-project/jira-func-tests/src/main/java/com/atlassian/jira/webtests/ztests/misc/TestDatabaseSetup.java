package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.jira.testkit.client.log.FuncTestOut;
import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.setup.JiraSetupInstanceHelper;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * @since v4.3
 */
@WebTest ( { Category.FUNC_TEST, Category.SETUP })
public class TestDatabaseSetup extends FuncTestCase
{
    /**
     * Because we cannot guarantee the order of test executions and at the end we need to have the db configure
     */
    public void testAll()
    {
        if (getEnvironmentData().getProperty("databaseType") != null)
        {
            tester.getDialog().getWebClient().getClientProperties().setHiddenFieldsEditable(true);
            // Direct JDBC tests
            _testDirectJdbcSuccessful();
            _testDirectJdbcMissingHostname();
            _testDirectJdbcMissingPortNumber();
            _testDirectJdbcInvalidPort();
            _testDirectJdbcIncorrectPort();
            _testDirectJdbcMissingUsername();
            _testDirectJdbcInvalidCredential();

            // Set up database properly
            configureDirectJdbc();
        }
        else
        {
            FuncTestOut.log("Skipping TestDatabaseSetup: Internal DB configured.");
        }
    }

    protected boolean shouldSkipSetup()
    {
        return true;
    }

    private void configureDirectJdbc()
    {
        JiraSetupInstanceHelper.setupDirectJDBCConnection(tester, getEnvironmentData());
        text.assertTextPresent(locator.page(), "Set Up Application Properties");
    }

    private void _testDirectJdbcSuccessful()
    {
        fillValidDirectJdbcValues();
        assertTestConnectionSuccessful();
    }

    private void _testDirectJdbcMissingHostname()
    {
        fillValidDirectJdbcValues();
        // Make username field empty (most likely missing field since it is not auto-populated)
        tester.setFormElement("jdbcHostname", "");
        tester.setFormElement("testingConnection", "true");
        tester.submit();
        text.assertTextPresent(locator.page(), "Hostname required");
    }

    private void _testDirectJdbcMissingPortNumber()
    {
        fillValidDirectJdbcValues();
        // Make username field empty (most likely missing field since it is not auto-populated)
        tester.setFormElement("jdbcPort", "");
        tester.setFormElement("testingConnection", "true");
        tester.submit();
        text.assertTextPresent(locator.page(), "Port required");
    }

    private void _testDirectJdbcInvalidPort()
    {
        fillValidDirectJdbcValues();
        // Make URL invalid
        tester.setFormElement("jdbcPort", "not-a-number");
        assertTestConnectionFailed();
    }

    private void _testDirectJdbcIncorrectPort()
    {
        fillValidDirectJdbcValues();
        // Make URL invalid
        tester.setFormElement("jdbcPort", "999");
        assertTestConnectionFailed();
    }

    private void _testDirectJdbcInvalidCredential()
    {
        fillValidDirectJdbcValues();
        // Modify password to invalidate credentials
        tester.setFormElement("jdbcPassword", getEnvironmentData().getProperty("password") + "extra-text-to-invalidate-password");
        assertTestConnectionFailed();
    }

    private void _testDirectJdbcMissingUsername()
    {
        fillValidDirectJdbcValues();
        // Make username field empty (most likely missing field since it is not auto-populated)
        tester.setFormElement("jdbcUsername", "");
        tester.setFormElement("testingConnection", "true");
        tester.submit();
        text.assertTextPresent(locator.page(), "Username required");
    }

    private void fillValidDirectJdbcValues()
    {
        navigation.gotoPage("/secure/SetupDatabase!default.jspa");
        text.assertTextPresent(locator.page(), "Database Connection");
        tester.checkCheckbox("databaseOption", "EXTERNAL");
        tester.setFormElement("databaseType", getEnvironmentData().getProperty("databaseType"));
        tester.setFormElement("jdbcHostname", environmentData.getProperty("db.host"));
        tester.setFormElement("jdbcPort", environmentData.getProperty("db.port"));
        // SID is only used for Oracle
        tester.setFormElement("jdbcSid", environmentData.getProperty("db.instance"));
        // Database is used for all DBs except Oracle
        tester.setFormElement("jdbcDatabase", environmentData.getProperty("db.instance"));

        tester.setFormElement("jdbcUsername", getEnvironmentData().getProperty("username"));
        tester.setFormElement("jdbcPassword", getEnvironmentData().getProperty("password"));
        tester.setFormElement("schemaName", getEnvironmentData().getProperty("schema-name"));
    }

    private void assertTestConnectionSuccessful()
    {
        tester.setFormElement("testingConnection", "true");
        tester.submit();
        text.assertTextPresent(locator.page(), "The database connection test was successful.");
    }

    private void assertTestConnectionFailed()
    {
        tester.setFormElement("testingConnection", "true");
        tester.submit();
        text.assertTextPresent(locator.page(), "Error connecting to database");
    }
}
