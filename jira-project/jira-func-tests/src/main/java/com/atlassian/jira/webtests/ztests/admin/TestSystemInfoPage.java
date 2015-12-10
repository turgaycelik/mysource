/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.webtests.ztests.admin;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.TableCellLocator;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.framework.util.env.EnvironmentUtils;
import com.atlassian.jira.webtests.LicenseKeys;
import com.meterware.httpunit.WebTable;
import org.apache.commons.lang.StringUtils;

/**
 * Check that the SystemInfo page is correct, added for the new ReleaseInfo integration.
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.BROWSING })
public class TestSystemInfoPage extends FuncTestCase
{
    private static final String SYSTEM_INFO_SECTION = "system_info";

    @Override
    protected void tearDownTest()
    {
        administration.generalConfiguration().setJiraLocaleToSystemDefault();
        super.tearDownTest();
    }

    /**
     * installation type comes from the release.info file
     */
    public void testInstallationType()
    {
        String installationType = getEnvironmentData().getReleaseInfo();
        assertFalse("You must have the 'jira.release.info' property set in your localtest.properties", StringUtils.isBlank(installationType));

        administration.restoreBlankInstance();
        navigation.gotoAdminSection(SYSTEM_INFO_SECTION);
        text.assertTextSequence(new WebPageLocator(tester), new String[] {"Installation Type", installationType});
    }

    public void testSupportRequestContainsMemoryAndInputArgsInfo()
    {
        administration.restoreBlankInstance();
        navigation.gotoAdminSection(SYSTEM_INFO_SECTION);

        if(!isSunJVM())
        {
            if(isJvmWithPermGen())
            {
                tester.assertTextNotPresent("PermGen Memory Graph");
            }
            tester.assertTextNotPresent("Unable to determine, this requires running JDK 1.5 and higher.");
        }
        else
        {
            tester.assertTextPresent("JVM Input Arguments");
            if(isJvmWithPermGen())
            {
                tester.assertTextPresent("Used PermGen Memory");
                tester.assertTextPresent("Free PermGen Memory");
                tester.assertTextPresent("PermGen Memory Graph");
            }
            tester.assertTextNotPresent("Unable to determine, this requires running JDK 1.5 and higher.");
        }
    }

    public void testSystemInfoContainsTimezoneInfo() throws Exception
    {
        administration.restoreBlankInstance();
        navigation.gotoAdminSection(SYSTEM_INFO_SECTION);

        final WebTable table = tester.getDialog().getWebTableBySummaryOrId("system_info_table");
        final String systemTime = table.getCellAsText(2, 1).trim();
        assertTrue("System time does not contain GMT offset '" + systemTime + "'", systemTime.matches(".*[-+]\\d{4}"));
        final String timezoneLabel = table.getCellAsText(12, 0);
        assertEquals("User Timezone", timezoneLabel.trim());
        final String timezoneValue = table.getCellAsText(12, 1);
        assertTrue("Timezone value not present in text '" + timezoneValue + "'", timezoneValue.trim().length() > 0);
    }

    public void testApplicationPropertiesPresent()
    {
        administration.restoreBlankInstance();
        navigation.gotoAdminSection(SYSTEM_INFO_SECTION);

        tester.assertTextInTable("application_properties", "jira.baseurl");
        tester.assertTextInTable("application_properties", "jira.option.voting");
        tester.assertTextInTable("application_properties", "jira.option.watching");

        tester.assertTextNotInTable("application_properties", "License Hash 1");
        tester.assertTextNotInTable("application_properties", "License Hash 1 Text");
        tester.assertTextNotInTable("application_properties", "License Message");
        tester.assertTextNotInTable("application_properties", "License Message Text");
        tester.assertTextNotInTable("application_properties", "License20");
        tester.assertTextNotInTable("application_properties", "jira.sid.key");
        tester.assertTextNotInTable("application_properties", "org.apache.shindig.common.crypto.BlobCrypter:key");

    }

    public void testSystemInfoContainsJiraHome()
    {
        administration.restoreBlankInstance();
        navigation.gotoAdminSection(SYSTEM_INFO_SECTION);

        tester.assertTextInTable("file_paths", "Location of JIRA Local Home");
    }

    public void testLicenseInfoMaintenanceStatus() throws Exception
    {
        // restore commercial, non-expired license
        administration.restoreBlankInstance();
        navigation.gotoAdminSection(SYSTEM_INFO_SECTION);
        tester.assertTableRowsEqual("license_info", 3, new String[][] {new String[]{"Maintenance Status", "Supported"}});

        // restore commercial, expired license
        administration.switchToLicense(LicenseKeys.V2_EVAL_EXPIRED.getLicenseString(), "Evaluation");
        navigation.gotoAdminSection(SYSTEM_INFO_SECTION);
        tester.assertTableRowsEqual("license_info", 3, new String[][] {new String[]{"Maintenance Status", "Expired"}});

        // restore personal license
        administration.switchToPersonalLicense();
        navigation.gotoAdminSection(SYSTEM_INFO_SECTION);
        tester.assertTableRowsEqual("license_info", 4, new String[][] {new String[]{"Maintenance Status", "Unsupported"}});
    }

    /**
     * A user with no predefined language gets the language options in the system's default language
     */
    public void testShowsLanguageListInDefaultLanguage()
    {
        administration.restoreData("TestUserProfileI18n.xml");

        administration.generalConfiguration().setJiraLocale("Deutsch (Deutschland)");

        navigation.gotoAdminSection("system_info");

        // assert that the page defaults to German
        final int lastRow = page.getHtmlTable("jirainfo").getRowCount() - 1;
        text.assertTextPresent(new TableCellLocator(tester, "jirainfo", lastRow, 1), "Deutsch (Deutschland)");
        text.assertTextPresent(new TableCellLocator(tester, "jirainfo", lastRow - 1, 1), "Deutsch (Deutschland)");
    }

    /**
     * A user with a language preference that is different from the system's language gets the list of languages in his preferred language.
     */
    public void testShowsLanguageListInTheUsersLanguage()
    {
        administration.restoreData("TestUserProfileI18n.xml");

        // set the system locale to something other than English just to be different
        administration.generalConfiguration().setJiraLocale("Deutsch (Deutschland)");

        navigation.login(FRED_USERNAME);

        navigation.gotoAdminSection("system_info");

        // assert that the page defaults to Spanish
        final int lastRow = page.getHtmlTable("jirainfo").getRowCount() - 1;
        text.assertTextPresent(new TableCellLocator(tester, "jirainfo", lastRow, 1), "alem\u00e1n (Alemania)");
        text.assertTextPresent(new TableCellLocator(tester, "jirainfo", lastRow - 1, 1), "espa\u00f1ol (Espa\u00f1a)");
    }

    private boolean isBeforeJdk15()
    {
        return new EnvironmentUtils(tester, getEnvironmentData(), navigation).isJavaBeforeJdk15();
    }

    private boolean isJvmWithPermGen()
    {
        return new EnvironmentUtils(tester, getEnvironmentData(), navigation).isJvmWithPermGen();
    }

    private boolean isSunJVM()
    {
        return new EnvironmentUtils(tester, getEnvironmentData(), navigation).isSunJVM();
    }
}