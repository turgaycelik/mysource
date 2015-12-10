package com.atlassian.jira.webtests.ztests.license;

import com.atlassian.core.util.DateUtils;
import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.xmlbackup.XmlBackupCopier;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.webtests.LicenseKeys;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;


/**
 * Uses the following xml files:
 * <p/>
 * TestLicenseMessagesNewBuildOldLicenseFull.xml
 * <p/>
 * Most of other scenarios unit tested in: com.atlassian.jira.license.TestDefaultLicenseDetails
 *
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.LICENSING })
public class TestLicenseMessages extends FuncTestCase
{
    private static final String URL_WWW_ATLASSIAN_COM_ORDER = "http://www.atlassian.com/order";
    private static final String URL_WWW_ATLASSIAN_COM_EXPIRED_EVAL = "http://www.atlassian.com/software/jira/expiredevaluation.jsp";

    private static final String[] NOT_EXPIRED_MESSAGES = new String[] {
            "JIRA support and updates for this license ended on ",
            "You are currently running a version of JIRA that was created after that date.",
            "Your evaluation period will expire in ",
            "If you wish to have access to support and updates, please "
    };
    private static final String[] EXPIRED_MESSAGES = new String[] {
            "JIRA support and updates for this license ended on ",
            "You are currently running a version of JIRA that was created after that date.",
            "Your evaluation period has expired.",
            "If you wish to have access to support and updates, please "
    };

    private final static Pattern dateToken = Pattern.compile("TIMESTAMPTOCHANGE");
    private final static DataLicenseInfo licenseInfo = new DataLicenseInfo("TestLicenseMessagesNewBuildOldLicenseFull.xml",
            EXPIRED_MESSAGES,
            NOT_EXPIRED_MESSAGES,
            new LicenseInfoUrl[] { new LicenseInfoUrl("renew your maintenance", URL_WWW_ATLASSIAN_COM_ORDER) });

    private File importDirectory;
    private final String timestampNow;
    private final String timestampExpired;

    public TestLicenseMessages()
    {
        final long now = System.currentTimeMillis();
        timestampNow = Long.toString(now);
        timestampExpired = Long.toString(now - 31 * DateUtils.DAY_MILLIS);
    }

    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreBlankInstance();
        importDirectory = new File(administration.getJiraHomeDirectory(), "import");
    }

    public void testSwitchableLicenses() throws Exception
    {
        final List<LicenseInfo> switchableLicenses = ImmutableList.of(
                new LicenseInfo(LicenseKeys.V2_EVAL_EXPIRED,
                        new String[] {
                                "(Your evaluation has expired.)",
                                "Your JIRA evaluation period expired on ",
                                "You are not able to create new issues in JIRA.",
                                "To reactivate JIRA, please "
                        },
                        new String[] { },
                        new LicenseInfoUrl[] {
                                new LicenseInfoUrl("purchase JIRA", URL_WWW_ATLASSIAN_COM_EXPIRED_EVAL)
                        }),
                new LicenseInfo(LicenseKeys.V2_COMMERCIAL,
                        new String[] {
                                "(Support and updates available until "
                        },
                        new String[] {
                        },
                        new LicenseInfoUrl[] { }),
                createSupportedLicense(LicenseKeys.V2_COMMUNITY),
                createSupportedLicense(LicenseKeys.V2_DEVELOPER),
                createUnsupportedLicense(LicenseKeys.V2_PERSONAL),
                createSupportedLicense(LicenseKeys.V2_OPEN_SOURCE),
                createUnsupportedLicense(LicenseKeys.V2_DEMO));

        for (final LicenseInfo licenseInfo : switchableLicenses)
        {
            log("Testing license for " + licenseInfo.description);
            administration.switchToLicense(licenseInfo.license, licenseInfo.description);
            assertLicense(licenseInfo);

            if (licenseInfo.license.equals(LicenseKeys.V2_EVAL_EXPIRED.getLicenseString()))
            {
                // make sure we CANT create issues
                tester.gotoPage("secure/CreateIssue!default.jspa");
                assertCantCreateIssues();

                // make sure we CANT create issues even if they jump to the second step
                tester.gotoPage("secure/CreateIssue.jspa?pid=10000&issuetype=1");
                assertCantCreateIssues();
            }
        }
    }

    private void assertCantCreateIssues()
    {
        final CssLocator locator = new CssLocator(tester, ".aui-message.error");
        text.assertTextPresent(locator, "You will not be able to create new issues because your JIRA evaluation period has expired, please contact your JIRA administrators.");
    }

    public void testNewBuildOldLicensesCommercialFullExpired() throws Exception
    {
        testNewBuildOldLicense(licenseInfo, true);
    }

    public void testNewBuildOldLicensesCommercialFullNotExpired() throws Exception
    {
        testNewBuildOldLicense(licenseInfo, false);
    }

    private void testNewBuildOldLicense(final DataLicenseInfo info, final boolean expired) throws Exception
    {
        modifyTimestampAndRestore(importDirectory, info.dataFile, expired ? timestampExpired : timestampNow);
        navigation.gotoAdminSection("license_details");
        assertDataLicense(expired ? info.expiredMessages : info.notExpiredMessages, info.urls);
    }

    private void modifyTimestampAndRestore(final File importDirectory, final String fileName, final String timestamp)
            throws Exception
    {
        File generatedDataFile = null;
        try
        {
            log("Modifying timestamp for backup data at " + fileName);
            final File templateDataFile = new File(getEnvironmentData().getXMLDataLocation(), fileName);
            generatedDataFile = File.createTempFile("generated", ".xml", importDirectory);

            final XmlBackupCopier xmlBackupCopier = new XmlBackupCopier(getEnvironmentData().getBaseUrl());
            xmlBackupCopier.copyXmlBackupTo(templateDataFile.getAbsolutePath(),
                    generatedDataFile.getAbsolutePath(),
                    MapBuilder.build(dateToken, timestamp));

            // import the data
            log("Restoring data '" + generatedDataFile.getAbsolutePath() + "'");
            navigation.disableWebSudo();
            tester.gotoPage("secure/admin/XmlRestore!default.jspa");
            tester.setWorkingForm("restore-xml-data-backup");
            tester.setFormElement("filename", generatedDataFile.getName());
            tester.submit();
            log("Waiting for restore...");
            administration.waitForRestore();
            tester.assertTextPresent("Your import has been successful");
            navigation.disableWebSudo();
            navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
            administration.generalConfiguration().setBaseUrl(getEnvironmentData().getBaseUrl().toString());
        }
        finally
        {
            if (generatedDataFile != null && generatedDataFile.exists() && !generatedDataFile.delete())
            {
                fail("Failed to delete generated xml file: " + generatedDataFile.getAbsolutePath());
            }
        }
    }

    private void assertLicense(final LicenseInfo licenseInfo)
    {
        final Locator pageLocator = new WebPageLocator(tester);
        text.assertTextSequence(pageLocator, licenseInfo.messages);
        for (final String notMessage : licenseInfo.notMessages)
        {
            text.assertTextNotPresent(pageLocator, notMessage);
        }
        for (final LicenseInfoUrl licenseInfoUrl : licenseInfo.urls)
        {
            assertions.getLinkAssertions().assertLinkLocationEndsWith(licenseInfoUrl.text, licenseInfoUrl.url);
        }
    }

    private void assertDataLicense(final String[] messages, final LicenseInfoUrl[] urls)
    {
        final Locator pageLocator = new WebPageLocator(tester);
        text.assertTextSequence(pageLocator, messages);
        for (final LicenseInfoUrl licenseInfoUrl : urls)
        {
            assertions.getLinkAssertions().assertLinkLocationEndsWith(licenseInfoUrl.text, licenseInfoUrl.url);
        }
    }

    private static LicenseInfo createSupportedLicense(final LicenseKeys.License forLicense)
    {
        return new LicenseInfo(forLicense,
                new String[] {
                        "(Support and updates available until "
                },
                new String[] {
                        "JIRA support and updates for this license ended on ",
                        "JIRA support and updates created after ",
                        "are not valid for this license."
                },
                new LicenseInfoUrl[] { }
        );
    }

    private static LicenseInfo createUnsupportedLicense(final LicenseKeys.License forLicense)
    {
        return new LicenseInfo(forLicense,
                new String[] {
                        "(Updates available until "
                },
                new String[] {
                },
                new LicenseInfoUrl[] { }
        );
    }

    private static class LicenseInfo
    {
        final String license;
        final String description;
        final String[] messages;
        final String[] notMessages;
        final LicenseInfoUrl[] urls;

        private LicenseInfo(final LicenseKeys.License forLicense, final String[] messages, final String[] notMessages, final LicenseInfoUrl[] urls)
        {
            this.license = forLicense.getLicenseString();
            this.description = forLicense.getDescription();
            this.messages = messages;
            this.notMessages = notMessages;
            this.urls = urls;
        }
    }

    private static class DataLicenseInfo
    {
        final String dataFile;
        final String[] expiredMessages;
        final String[] notExpiredMessages;
        final LicenseInfoUrl[] urls;

        private DataLicenseInfo(final String dataFile, final String[] expiredMessages, final String[] notExpiredMessages, final LicenseInfoUrl[] urls)
        {
            this.dataFile = dataFile;
            this.expiredMessages = expiredMessages;
            this.notExpiredMessages = notExpiredMessages;
            this.urls = urls;
        }
    }

    private static class LicenseInfoUrl
    {
        String text;
        String url;

        private LicenseInfoUrl(final String text, final String url)
        {
            this.text = text;
            this.url = url;
        }
    }
}
