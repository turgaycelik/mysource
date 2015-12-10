package com.atlassian.jira.webtests.ztests.license;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

import static com.atlassian.jira.webtests.LicenseKeys.V2_COMMERCIAL;
import static com.atlassian.jira.webtests.LicenseKeys.V2_COMMUNITY;
import static com.atlassian.jira.webtests.LicenseKeys.V2_DEMO;
import static com.atlassian.jira.webtests.LicenseKeys.V2_DEVELOPER;
import static com.atlassian.jira.webtests.LicenseKeys.V2_EVAL_EXPIRED;
import static com.atlassian.jira.webtests.LicenseKeys.V2_OPEN_SOURCE;
import static com.atlassian.jira.webtests.LicenseKeys.V2_PERSONAL;

@WebTest ({ Category.FUNC_TEST, Category.LICENSING })
public class TestLicenseFooters extends JIRAWebTest
{
    public TestLicenseFooters(String name)
    {
        super(name);
    }

    public void testEnterpriseCommunityLicense()
    {
        getAdministration().restoreBlankInstanceWithLicense(V2_COMMUNITY);
        assertTextPresentBeforeText("Powered by a free Atlassian", "community license for Atlassian.");

        assertTextNotPresent("site is for non-production use only.");
        assertTextNotPresent("open source license for Atlassian.");
        assertTextNotPresent("This JIRA site is for demonstration purposes only.");
    }

    public void testEnterpriseCommunityLicenseLoggedOut()
    {
        try
        {
            getAdministration().restoreBlankInstanceWithLicense(V2_COMMUNITY);
            logout();
            assertTextPresentBeforeText("Powered by a free Atlassian", "community license for Atlassian.");

            assertTextNotPresent("site is for non-production use only.");
            assertTextNotPresent("open source license for Atlassian.");
            assertTextNotPresent("This JIRA site is for demonstration purposes only.");
        }
        finally
        {
            login(ADMIN_USERNAME, ADMIN_PASSWORD);
        }
    }

    public void testEnterpriseDeveloperLicense()
    {
        getAdministration().restoreBlankInstanceWithLicense(V2_DEVELOPER);
        assertTextPresentBeforeText("This", "site is for non-production use only.");

        assertTextNotPresent("Powered by a free Atlassian");
        assertTextNotPresent("community license for Atlassian.");
        assertTextNotPresent("open source license for Atlassian.");
        assertTextNotPresent("This JIRA site is for demonstration purposes only.");
    }

    public void testEnterpriseDeveloperLicenseLoggedOut()
    {
        try
        {
            getAdministration().restoreBlankInstanceWithLicense(V2_DEVELOPER);
            logout();
            assertTextPresentBeforeText("This", "site is for non-production use only.");

            assertTextNotPresent("Powered by a free Atlassian");
            assertTextNotPresent("community license for Atlassian.");
            assertTextNotPresent("open source license for Atlassian.");
            assertTextNotPresent("This JIRA site is for demonstration purposes only.");
        }
        finally
        {
            login(ADMIN_USERNAME, ADMIN_PASSWORD);
        }
    }

    public void testEnterprisePersonalLicense()
    {
        getAdministration().restoreBlankInstanceWithLicense(V2_PERSONAL);
        assertTextSequence(new String[] { "A", "free bug tracker", "for up to three users? Try", "JIRA Personal", "Edition." });

        assertTextNotPresent("Powered by a free Atlassian");
        assertTextNotPresent("community license for Atlassian.");
        assertTextNotPresent("open source license for Atlassian.");
        assertTextNotPresent("This JIRA site is for demonstration purposes only.");
        assertTextNotPresent("site is for non-production use only.");
    }

    public void testEnterprisePersonalLicenseLoggedOut()
    {
        try
        {
            getAdministration().restoreBlankInstanceWithLicense(V2_PERSONAL);
            logout();
            assertTextSequence(new String[] { "A", "free bug tracker", "for up to three users? Try", "JIRA Personal", "Edition." });

            assertTextNotPresent("Powered by a free Atlassian");
            assertTextNotPresent("community license for Atlassian.");
            assertTextNotPresent("open source license for Atlassian.");
            assertTextNotPresent("This JIRA site is for demonstration purposes only.");
            assertTextNotPresent("site is for non-production use only.");
        }
        finally
        {
            login(ADMIN_USERNAME, ADMIN_PASSWORD);
        }
    }

    public void testEnterpriseOpenSourceLicense()
    {
        getAdministration().restoreBlankInstanceWithLicense(V2_OPEN_SOURCE);
        assertTextPresentBeforeText("Powered by a free Atlassian", "open source license for Atlassian.");

        assertTextNotPresent("community license for Atlassian.");
        assertTextNotPresent("site is for non-production use only.");
        assertTextNotPresent("This JIRA site is for demonstration purposes only.");
    }

    public void testEnterpriseOpenSourceLicenseLoggedOut()
    {
        try
        {
            getAdministration().restoreBlankInstanceWithLicense(V2_OPEN_SOURCE);
            logout();
            assertTextPresentBeforeText("Powered by a free Atlassian", "open source license for Atlassian.");

            assertTextNotPresent("community license for Atlassian.");
            assertTextNotPresent("site is for non-production use only.");
            assertTextNotPresent("This JIRA site is for demonstration purposes only.");
        }
        finally
        {
            login(ADMIN_USERNAME, ADMIN_PASSWORD);
        }

    }

    public void testEnterpriseDemonstrationLicense()
    {
        getAdministration().restoreBlankInstanceWithLicense(V2_DEMO);
        assertTextPresentBeforeText("This JIRA site is for demonstration purposes only.", "bug tracking software for your team.");

        assertTextNotPresent("Powered by a free Atlassian");
        assertTextNotPresent("community license for Atlassian.");
        assertTextNotPresent("site is for non-production use only.");
        assertTextNotPresent("open source license for Atlassian.");
    }

    public void testEnterpriseDemonstrationLicenseLoggedOut()
    {
        try
        {
            getAdministration().restoreBlankInstanceWithLicense(V2_DEMO);
            logout();
            assertTextPresentBeforeText("This JIRA site is for demonstration purposes only.", "bug tracking software for your team.");

            assertTextNotPresent("Powered by a free Atlassian");
            assertTextNotPresent("community license for Atlassian.");
            assertTextNotPresent("site is for non-production use only.");
            assertTextNotPresent("open source license for Atlassian.");
        }
        finally
        {
            login(ADMIN_USERNAME, ADMIN_PASSWORD);
        }
    }

    public void testEnterpriseLicense()
    {
        getAdministration().restoreBlankInstanceWithLicense(V2_COMMERCIAL);

        assertTextNotPresent("Powered by a free Atlassian");
        assertTextNotPresent("community license for Atlassian.");
        assertTextNotPresent("site is for non-production use only.");
        assertTextNotPresent("Powered by a free Atlassian");
        assertTextNotPresent("open source license for Atlassian.");
        assertTextNotPresent("This JIRA site is for demonstration purposes only.");
    }

    public void testEnterpriseLicenseLoggedOut()
    {
        try
        {
            getAdministration().restoreBlankInstanceWithLicense(V2_COMMERCIAL);
            logout();
            assertTextNotPresent("Powered by a free Atlassian");
            assertTextNotPresent("community license for Atlassian.");
            assertTextNotPresent("site is for non-production use only.");
            assertTextNotPresent("open source license for Atlassian.");
            assertTextNotPresent("This JIRA site is for demonstration purposes only.");
        }
        finally
        {
            login(ADMIN_USERNAME, ADMIN_PASSWORD);
        }
    }

    public void testEnterpriseEvaluationLicense()
    {
        getAdministration().restoreBlankInstanceWithLicense(V2_EVAL_EXPIRED);
        navigation.gotoAdminSection("license_details");
        assertTextPresent("(Your evaluation has expired.)");

        navigation.gotoPage("secure/BrowseProjects.jspa");

        assertTextSequence(new String[] { "Powered by a free Atlassian", "JIRA evaluation license", "Please consider", "purchasing it", "today" });
        assertTextNotPresent("community license for Atlassian.");
        assertTextNotPresent("site is for non-production use only.");
        assertTextNotPresent("open source license for Atlassian.");
        assertTextNotPresent("This JIRA site is for demonstration purposes only.");
        assertTextNotPresent("for up to three users? Try");
    }

    public void testEnterpriseEvaluationLicenseLoggedOut()
    {
        try
        {
            getAdministration().restoreBlankInstanceWithLicense(V2_EVAL_EXPIRED);
            logout();
            assertTextSequence(new String[] { "Powered by a free Atlassian", "JIRA evaluation license", "Please consider", "purchasing it", "today" });
            assertTextNotPresent("community license for Atlassian.");
            assertTextNotPresent("site is for non-production use only.");
            assertTextNotPresent("open source license for Atlassian.");
            assertTextNotPresent("This JIRA site is for demonstration purposes only.");
            assertTextNotPresent("for up to three users? Try");
        }
        finally
        {
            login(ADMIN_USERNAME, ADMIN_PASSWORD);
        }
    }
}
