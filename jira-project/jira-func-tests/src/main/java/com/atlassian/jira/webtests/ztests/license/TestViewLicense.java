package com.atlassian.jira.webtests.ztests.license;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.LicenseKeys;
import com.atlassian.jira.webtests.util.RunOnce;

@WebTest ({ Category.FUNC_TEST, Category.LICENSING })
public class TestViewLicense extends FuncTestCase
{
    private static final String LICENSE_INPUT_FIELD = "license";
    private static final String INVALID_LICENSE = "abc";

    private static final RunOnce RESTORE_ONCE = new RunOnce();
    
    @Override
    protected void setUpTest()
    {
        RESTORE_ONCE.run(new Runnable()
        {
            @Override
            public void run()
            {
                administration.restoreBlankInstance();
            }
        });
    }

    public void testUpdateLicenseWithBlankLicense()
    {
        gotoViewLicense();
        submitLicense("");
        tester.assertTextPresent("Invalid JIRA license key specified.");
        tester.assertFormElementEquals(LICENSE_INPUT_FIELD, "");
    }

    public void testUpdateLicenseWithInvalidLicense()
    {
        gotoViewLicense();
        submitLicense(INVALID_LICENSE);
        tester.assertTextPresent("Invalid JIRA license key specified.");
        tester.assertFormElementEquals(LICENSE_INPUT_FIELD, INVALID_LICENSE);
    }

    public void testUpdateLicenseV1Enterprise()
    {
        assertLicenseVersion1NotAccepted(LicenseKeys.V1_ENTERPRISE);
    }

    public void testUpdateLicenseV2Commercial()
    {
        assertLicenseVersion2Accepted(LicenseKeys.V2_COMMERCIAL);
    }

    public void testUpdateLicenseV2Starter()
    {
        assertLicenseVersion2Accepted(LicenseKeys.V2_STARTER);
    }

    public void testUpdateLicenseV2OpenSource()
    {
        assertLicenseVersion2Accepted(LicenseKeys.V2_OPEN_SOURCE);
    }

    public void testUpdateLicenseV2Developer()
    {
        assertLicenseVersion2Accepted(LicenseKeys.V2_DEVELOPER_LIMITED);
    }

    private void assertLicenseVersion2Accepted(final LicenseKeys.License license)
    {
        gotoViewLicense();
        submitLicense(license.getLicenseString());

        tester.setWorkingForm("jiraform");

        Locator locator = new XPathLocator(tester, "//span[@class='errMsg']");
        assertEquals("There should not be any error messages", 0, locator.getNodes().length);

        final String licenseString = tester.getDialog().getForm().getParameterValue(LICENSE_INPUT_FIELD);
        assertEquals("The license input field should be reset to blank", "", licenseString);

        locator = new XPathLocator(tester, "//table[@id='license_table']");
        text.assertTextSequence(locator, "Organisation", license.getOrganisation());
        text.assertTextSequence(locator, "License Type", license.getDescription());
        text.assertTextSequence(locator, "Support Entitlement Number", license.getSen());
    }

    private void assertLicenseVersion1NotAccepted(final LicenseKeys.License license)
    {
        gotoViewLicense();
        submitLicense(license.getLicenseString());

        tester.setWorkingForm("jiraform");

        Locator locator = new XPathLocator(tester, "//span[@class='errMsg']");
        assertEquals("There should be 1 error message", 1, locator.getNodes().length);
        text.assertTextPresent(locator, "This license version (v1) is incompatible with this JIRA installation. Please get a new license or generate an evaluation license");

        final String licenseString = tester.getDialog().getForm().getParameterValue(LICENSE_INPUT_FIELD);
        assertEquals("The license input field should be the input licensed", license.getLicenseString(), licenseString);
    }

    private void gotoViewLicense()
    {
        navigation.gotoAdmin();
        tester.clickLink("license_details");

        Locator locator = new WebPageLocator(tester);
        text.assertTextSequence(locator, new String[] { "Copy and paste the license key below.", "You can access your license key on", "My Account" });
    }

    private void submitLicense(String license)
    {
        tester.setWorkingForm("jiraform");
        tester.setFormElement(LICENSE_INPUT_FIELD, license);
        tester.submit();
    }
}
