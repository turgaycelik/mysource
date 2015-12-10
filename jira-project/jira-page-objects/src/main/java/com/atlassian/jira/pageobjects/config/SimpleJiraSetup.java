package com.atlassian.jira.pageobjects.config;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.pages.setup.AdminSetupPage;
import com.atlassian.jira.pageobjects.pages.setup.ApplicationSetupPage;
import com.atlassian.jira.pageobjects.pages.setup.BundleSetupPage;
import com.atlassian.jira.pageobjects.pages.setup.DatabaseSetupPage;
import com.atlassian.jira.pageobjects.pages.setup.LicenseSetupPage;
import com.atlassian.jira.pageobjects.pages.setup.MailSetupPage;
import com.atlassian.jira.webtests.LicenseKeys;
import com.atlassian.pageobjects.DelayedBinder;
import com.atlassian.pageobjects.PageBinder;
import org.apache.log4j.Logger;

import javax.inject.Inject;

/**
 * Implementation of JIRA setup that performs the simplest set up possible via UI.
 *
 * @since v4.4
 */
public class SimpleJiraSetup
{
    private static final Logger log = Logger.getLogger(SimpleJiraSetup.class);

    private final PageBinder pageBinder;
    private final JiraTestedProduct jira;

    @Inject
    public SimpleJiraSetup(PageBinder pageBinder, JiraTestedProduct jira)
    {
        this.pageBinder = pageBinder;
        this.jira = jira;
    }

    public void performSetUp()
    {
        DelayedBinder<DatabaseSetupPage> delayedSetup = jira.visitDelayed(DatabaseSetupPage.class);
        if (delayedSetup.canBind())
        {
            doDbSetup(delayedSetup.bind());
        }
        if (jira.isAt(ApplicationSetupPage.class))
        {
            setupMail(setupAdmin(setupLicense(setupBundle(setupApplication(pageBinder.bind(ApplicationSetupPage.class))))));
        }
        else if (jira.isAt(LicenseSetupPage.class))
        {
            setupMail(setupAdmin(setupLicense(pageBinder.bind(LicenseSetupPage.class))));
        }
        else if (jira.isAt(AdminSetupPage.class))
        {
            setupMail(setupAdmin(pageBinder.bind(AdminSetupPage.class)));
        }
        else if (jira.isAt(MailSetupPage.class))
        {
            setupMail(pageBinder.bind(MailSetupPage.class));
        }
        else
        {
            log.warn("Already set up, skipping");
        }
    }

    private void doDbSetup(DatabaseSetupPage setupPage)
    {
        setupPage.submitInternalDb();
    }

    private BundleSetupPage setupApplication(ApplicationSetupPage applicationSetupPage)
    {
        return applicationSetupPage.setTitle("Testing JIRA")
                .submit();
    }

    private LicenseSetupPage setupBundle(final BundleSetupPage setupBundlePage)
    {
        return setupBundlePage.chooseTrackingBundle().submit();
    }

    private AdminSetupPage setupLicense(LicenseSetupPage licenseSetupPage)
    {
        return licenseSetupPage.selectExistingLicense(LicenseKeys.V2_COMMERCIAL.getLicenseString()).submit();
    }

    private MailSetupPage setupAdmin(AdminSetupPage adminSetupPage)
    {
        return adminSetupPage.setUsername("admin")
                            .setPasswordAndConfirmation("admin")
                            .setFullName("Administrator")
                            .setEmail("admin@stuff.com.com")
                            .submit();
    }

    private DashboardPage setupMail(MailSetupPage mailSetupPage)
    {
        return mailSetupPage.submitDisabledEmail();
    }
}
