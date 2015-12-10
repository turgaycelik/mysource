package com.atlassian.jira.functest.framework.admin;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.atlassian.jira.functest.framework.Navigation;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.google.common.collect.Iterables;
import junit.framework.Assert;
import net.sourceforge.jwebunit.WebTester;

import java.util.Arrays;

import static com.atlassian.jira.functest.framework.FunctTestConstants.ADMIN_PASSWORD;
import static com.google.common.base.Predicates.equalTo;

/**
 * @since v3.13
 */
public class GeneralConfigurationImpl extends AbstractFuncTestUtil implements GeneralConfiguration
{
    public GeneralConfigurationImpl(WebTester tester, JIRAEnvironmentData environmentData)
    {
        super(tester, environmentData, 2);
    }

    @Override
    public void setAllowUnassignedIssues(boolean enable)
    {
        gotoEditGeneralConfig();
        tester.getDialog().setFormParameter("allowUnassigned", String.valueOf(enable));
        tester.submit();
    }

    @Override
    public void setExternalUserManagement(final boolean enable)
    {
        gotoEditGeneralConfig();
        if (enable)
        {
            tester.checkCheckbox("externalUM", "true");
            // JRA-15966. Mode must be private for External User Management
            tester.selectOption("mode", "Private");
        }
        else
        {
            tester.checkCheckbox("externalUM", "false");
        }
        tester.submit();
    }

    @Override
    public void enableVoting()
    {
        gotoEditGeneralConfig();
        tester.getDialog().setFormParameter("voting", "true");
        tester.submit();
    }

    @Override
    public void enableRemoteApi()
    {
        gotoEditGeneralConfig();
        tester.checkCheckbox("allowRpc", "true");
        tester.submit();
    }

    @Override
    public void setCommentVisibility(final CommentVisibility commentVisibility)
    {
        gotoEditGeneralConfig();
        tester.checkCheckbox("groupVisibility", commentVisibility.getCheckBoxValue().toString());
        tester.submit();
    }

    @Override
    public void setUserEmailVisibility(final EmailVisibility emailVisibility)
    {
        gotoEditGeneralConfig();
        tester.getDialog().setFormParameter("emailVisibility", String.valueOf(emailVisibility));
        tester.submit();
    }

    @Override
    public void setJqlAutocomplete(final boolean enable)
    {
        gotoEditGeneralConfig();
        tester.getDialog().setFormParameter("jqlAutocompleteDisabled", String.valueOf(!enable));
        tester.submit();
    }

    @Override
    public void disableVoting()
    {
        gotoEditGeneralConfig();
        tester.getDialog().setFormParameter("voting", "false");
        tester.submit();
    }

    @Override
    public void setBaseUrl(final String baseUrl)
    {
        log("Setting baseurl to '" + baseUrl + "'");
        gotoEditGeneralConfig();
        tester.setFormElement("baseURL", baseUrl);
        tester.submit();
    }

    @Override
    public void fixBaseUrl()
    {
        setBaseUrl(getEnvironmentData().getBaseUrl().toString());
    }

    @Override
    public void setJiraLocale(final String locale)
    {
        log("Setting locale to '" + locale + "'");
        gotoEditGeneralConfig();
        tester.setWorkingForm("jiraform");

        String localeToSelect = null;

        final String[] localeOptions = tester.getDialog().getOptionsFor("defaultLocale");
        for (String localeOption : localeOptions)
        {
            if (localeOption.equals(locale))
            {
                localeToSelect = locale;
            }
            else if (localeOption.equals(locale.concat(" [Default]")))
            {
                localeToSelect = locale.concat(" [Default]");
            }
        }
        Assert.assertNotNull(localeToSelect, "The locale: " + locale + "could not be found as an option in the Default "
                + "Language select list on the Administration --> General Configuration --> Edit Configuration Page" );

        tester.selectOption("defaultLocale", localeToSelect);
        tester.submit();
    }

    @Override
    public void setJiraLocaleByValue(String localeValue)
    {
        gotoEditGeneralConfig();
        tester.setWorkingForm("jiraform");

        final String[] defaultLocales = tester.getDialog().getOptionValuesFromSelectList("defaultLocale");
        Assert.assertTrue("Tried to set locale value to " + localeValue + " but could not find an option for the value",
                Iterables.any(Arrays.asList(defaultLocales), equalTo(localeValue)));

        tester.setFormElement("defaultLocale", localeValue);
        tester.submit();
    }

    @Override
    public void setJiraLocaleToSystemDefault()
    {
        setJiraLocaleByValue("-1");
    }

    @Override
    public void disableWatching()
    {
        gotoEditGeneralConfig();
        tester.getDialog().setFormParameter("watching", "false");
        tester.submit();
    }

    @Override
    public void enableWatching()
    {
        gotoEditGeneralConfig();
        tester.getDialog().setFormParameter("watching", "true");
        tester.submit();
    }

    @Override
    public void turnOnGZipCompression()
    {
        gotoEditGeneralConfig();
        tester.checkCheckbox("useGzip", "true");
        tester.submit();
    }

    @Override
    public void setDefaultUserTimeZone(String timeZoneID)
    {
        gotoEditGeneralConfig();
        tester.setWorkingForm("jiraform");
        tester.setFormElement("defaultTimeZoneId", timeZoneID == null ? "System" : timeZoneID);
        tester.submit();
    }

    @Override
    public GeneralConfiguration useGravatars(boolean useGravatars)
    {
        gotoEditGeneralConfig();
        tester.setWorkingForm("jiraform");
        tester.setFormElement("useGravatar", "true");
        tester.submit();
        return this;
    }

    protected Navigation getNavigation()
    {
        return getFuncTestHelperFactory().getNavigation();
    }

    private void gotoEditGeneralConfig()
    {
        getNavigation().gotoAdminSection("general_configuration");
        getNavigation().webSudoAuthenticate(ADMIN_PASSWORD);
        tester.clickLink("edit-app-properties");
    }
}
