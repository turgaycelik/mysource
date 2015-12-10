package com.atlassian.jira.functest.framework.setup;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.NavigableSet;
import java.util.NoSuchElementException;

import com.atlassian.jira.functest.framework.FuncTestWebClientListener;
import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.atlassian.jira.functest.framework.util.text.MsgOfD;
import com.atlassian.jira.testkit.client.log.FuncTestOut;
import com.atlassian.jira.webtests.LicenseKeys;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;

import com.google.common.collect.Sets;

import junit.framework.Assert;
import net.sourceforge.jwebunit.WebTester;

import org.apache.commons.lang.StringUtils;

/**
 * This contains common code between the old and new style func test frameworks that can detect if JIRA is setup and the
 * conditions that it can be in.
 *
 * @since v4.0
 */
public class JiraSetupInstanceHelper
{

    private static final String SYSTEM_TENANT_NAME = "_jiraSystemTenant";

    private final WebTester tester;
    private final JIRAEnvironmentData jiraEnvironmentData;

    public JiraSetupInstanceHelper(final WebTester tester, final JIRAEnvironmentData jiraEnvironmentData)
    {
        this.tester = tester;
        this.jiraEnvironmentData = jiraEnvironmentData;
    }

    public void ensureJIRAIsReadyToGo(final FuncTestWebClientListener webClientListener)
    {
        FuncTestOut.log("Checking that JIRA is setup and ready to be tested...");
        if (!isJiraSetup(webClientListener))
        {
            detectJohnson();
            synchronized (JiraSetupInstanceHelper.class)
            {
                setupJIRA();
            }
        }
        login(FunctTestConstants.ADMIN_USERNAME, FunctTestConstants.ADMIN_PASSWORD);

        FuncTestOut.log("JIRA is setup and 'admin' is logged in. " + new MsgOfD());
    }

    public boolean isJiraSetup()
    {
        tester.beginAt("/");

        return hasBeenSetup();
    }

    private boolean isJiraSetup(final FuncTestWebClientListener webClientListener)
    {
        tester.beginAt("/");
        tester.getDialog().getWebClient().addClientListener(webClientListener);

        return hasBeenSetup();
    }


    private boolean hasBeenSetup()
    {
        String currentURL = tester.getDialog().getResponse().getURL().getPath();
        boolean hasBeenSetUp = (jiraEnvironmentData.getContext() + "/secure/Dashboard.jspa").equals(currentURL) ||
                (jiraEnvironmentData.getContext() + "/login.jsp").equals(currentURL);
        return hasBeenSetUp;
    }

    private void detectJohnson()
    {
        final String response = tester.getDialog().getResponseText();
        if (response.contains("JIRA Access Constraints"))
        {
            FuncTestOut.log("Test failed because we are getting the following johnson page:\n" + response);
            Assert.fail("It appears that JIRA is currenty being Johnsoned.  That cant be good!");
        }
    }

    /**
     * Setup stages put in order. Additional stages START and END are just empty aliases - so users of this class won't
     * bother what step is a first step. <br> <em>setupJIRA also contains information about setup step order, it does
     * some checks</em>
     */
    public static enum SetupStage
    {
        START
                {
                    @Override
                    protected void perform(final JiraSetupInstanceHelper helper)
                    {
                    }
                },
        WELCOME
                {
                    @Override
                    protected void perform(final JiraSetupInstanceHelper helper)
                    {
                        helper.setupWelcome();
                    }
                },
        DATABASE
                {
                    @Override
                    protected void perform(final JiraSetupInstanceHelper helper)
                    {
                        // if database set up...
                        if (!helper.isDatabaseSetUp())
                        {
                            helper.setupDatabase();
                        }
                    }
                },
        APPLICATION_PROPERTIES
                {
                    @Override
                    protected void perform(final JiraSetupInstanceHelper helper)
                    {
                        helper.setupJiraApplicationProperties();
                    }
                },
        BUNDLE
                {
                    @Override
                    protected void perform(final JiraSetupInstanceHelper helper)
                    {
                        helper.skipSelectBundle();
                    }
                },
        LICENSE
                {
                    @Override
                    protected void perform(final JiraSetupInstanceHelper helper)
                    {
                        helper.setupJiraUsingExistingLicense(LicenseKeys.V2_COMMERCIAL, "Enterprise");
                    }
                },
        ACCOUNT
                {
                    @Override
                    protected void perform(final JiraSetupInstanceHelper helper)
                    {
                        helper.setupJiraAdminAccount(FunctTestConstants.ADMIN_USERNAME, FunctTestConstants.ADMIN_PASSWORD, FunctTestConstants.ADMIN_FULLNAME, FunctTestConstants.ADMIN_EMAIL);
                    }
                },
        EMAIL
                {
                    @Override
                    protected void perform(final JiraSetupInstanceHelper helper)
                    {
                        helper.setupJiraEmailNotifications();
                    }
                },
        LOGIN
                {
                    @Override
                    protected void perform(final JiraSetupInstanceHelper helper)
                    {
                        helper.login(FunctTestConstants.ADMIN_USERNAME, FunctTestConstants.ADMIN_PASSWORD);
                    }
                },
        END
                {
                    @Override
                    protected void perform(final JiraSetupInstanceHelper helper)
                    {
                    }
                };

        private static NavigableSet<SetupStage> NAVIGABLE_ENUMS = Sets.newTreeSet(Arrays.asList(SetupStage.values()));

        protected abstract void perform(JiraSetupInstanceHelper helper);

        public SetupStage next()
        {
            final SetupStage nextOrNull = NAVIGABLE_ENUMS.higher(this);
            if ( null==nextOrNull) {
                throw new NoSuchElementException("no next item for: "+this);
            }
            return nextOrNull;
        }

        public SetupStage prev()
        {
            final SetupStage prevOrNull = NAVIGABLE_ENUMS.lower(this);
            if ( null==prevOrNull) {
                throw new NoSuchElementException("no prev item for: "+this);
            }
            return prevOrNull;
        }

        public EnumSet<SetupStage> to(SetupStage endStage)
        {
            if (this.compareTo(endStage) > 0)
            {
                return EnumSet.noneOf(SetupStage.class);
            }
            return EnumSet.range(this, endStage);
        }
    }

    /**
     * Performs JIRA setup steps in given range. So if U want to setup JIRA up to EMAIL notification make call.
     *
     * @param stepsInRange stepsp to perform - by EnumSet definition they are always in proper order. Use EnumSet.range
     * and SetupStage.next, prev.
     * @see com.atlassian.jira.functest.framework.setup.JiraSetupInstanceHelper.SetupStage#to(com.atlassian.jira.functest.framework.setup.JiraSetupInstanceHelper.SetupStage)
     * @see java.util.EnumSet#range(Enum, Enum)
     */
    public void setupJIRAStepsInSet(EnumSet<SetupStage> stepsInRange)
    {
        try
        {
            for (SetupStage step : stepsInRange)
            {
                step.perform(this);
            }
        }
        catch (RuntimeException rte)
        {
            FuncTestOut.log("Unable to setup JIRA because of " + rte.getMessage());
            throw rte;
        }
    }

    private void setupJIRA()
    {
        FuncTestOut.log("JIRA is not setup.  Installing a new V2 license and completing setup steps");
        try
        {
            setupJIRAStepsInSet(SetupStage.START.to(SetupStage.LICENSE.prev()));

            tester.assertTextPresent("the license key");
            SetupStage.LICENSE.perform(this);

            // We can jump straight from the license key page to the email notifications step 4 if the admin user is already configured:
            boolean step3Skipped = true;
            if (!tester.getDialog().isTextInResponse("Set Up Email Notifications"))
            {
                step3Skipped = false;
                tester.assertTextPresent("Set Up Administrator Account");
                SetupStage.ACCOUNT.perform(this);
            }

            tester.assertTextPresent("Set Up Email Notifications");
            SetupStage.EMAIL.perform(this);
            setupJIRAStepsInSet(SetupStage.EMAIL.next().to(SetupStage.LOGIN.prev()));

            // During SetupComplete, we attempt to automatically log the user in
            // If we have skipped Step 3 (creating the admin user) we cannot automatically log the user in, as the user
            // object will not be stored in the session
            if (step3Skipped)
            {
                tester.assertTextPresent("JIRA is now ready to use, please log in and get started.");
            }
            else
            {
                // Assert that the user is logged in by checking if the profile link is present
                tester.assertLinkPresent("header-details-user-fullname");
            }
        }
        catch (RuntimeException rte)
        {
            FuncTestOut.log("Unable to setup JIRA because of " + rte.getMessage());
            throw rte;
        }
    }

    private boolean isDatabaseSetUp()
    {
        return !tester.getDialog().isTextInResponse("Follow these steps to set up JIRA");
    }

    private void setupWelcome()
    {
        tester.setFormElement("setupOption", "classic");
        tester.submit();
    }

    private void setupDatabase()
    {
        if (StringUtils.isNotEmpty(jiraEnvironmentData.getProperty("databaseType")))
        {
            FuncTestOut.log("Setting up external db");
            setupDirectJDBCConnection(tester, jiraEnvironmentData);
        }
        else
        {
            FuncTestOut.log("Setting up internal db");
            // setup internal db
            tester.checkCheckbox("databaseOption", "INTERNAL");
            tester.submit("next");
        }
        Assert.assertTrue(tester.getDialog().getResponseText(), tester.getDialog().getResponseText().contains("Set Up Application Properties"));
    }

    public static void setupDirectJDBCConnection(WebTester webTester, JIRAEnvironmentData environmentData)
    {
        webTester.checkCheckbox("databaseOption", "EXTERNAL");
        webTester.setFormElement("databaseType", environmentData.getProperty("databaseType"));
        webTester.setFormElement("jdbcHostname", environmentData.getProperty("db.host"));
        webTester.setFormElement("jdbcPort", environmentData.getProperty("db.port"));
        // SID is only used for Oracle
        webTester.setFormElement("jdbcSid", environmentData.getProperty("db.instance"));
        // Database is used for all DBs except Oracle
        webTester.setFormElement("jdbcDatabase", environmentData.getProperty("db.instance"));
        webTester.setFormElement("jdbcUsername", environmentData.getProperty("username"));
        webTester.setFormElement("jdbcPassword", environmentData.getProperty("password"));
        webTester.setFormElement("schemaName", environmentData.getProperty("schema-name"));
        webTester.submit();
    }

    private void setupJiraAdminAccount(String username, String password, String fullName, String email)
    {
        tester.setFormElement("username", username);
        tester.setFormElement("password", password);
        tester.setFormElement("confirm", password);
        tester.setFormElement("fullname", fullName);
        tester.setFormElement("email", email);
        tester.submit();
    }

    private void setupJiraEmailNotifications()
    {
        tester.submit("finish");
    }

    private void setupJiraApplicationProperties()
    {
        tester.setFormElement("title", "Your Company JIRA");
        tester.submit();
    }

    private void skipSelectBundle()
    {
        tester.gotoPage("/secure/SetupLicense!default.jspa");
    }

    private void setupJiraUsingExistingLicense(LicenseKeys.License licenseKey, String buildType)
    {
        tester.checkRadioOption("licenseSetupSelector", "existingLicense");
        tester.setWorkingForm("setupLicenseForm");
        tester.setFormElement("setupLicenseKey", licenseKey.getLicenseString());
        tester.submit();
        tester.assertTextNotPresent("Invalid license type for this edition of JIRA. License should be of type " + buildType + ".");
    }

    private void login(String username, String password)
    {
        tester.beginAt("/login.jsp");
        tester.setFormElement("os_username", username);
        tester.setFormElement("os_password", password);
        tester.setWorkingForm("login-form");
        tester.submit();
    }

    private void logLicense(final LicenseKeys.License licenseKey)
    {
        FuncTestOut.log("Using a '" + licenseKey.getDescription() + "' license which allows " + licenseKey.getMaxUsers() + " maximum users");
    }
}
