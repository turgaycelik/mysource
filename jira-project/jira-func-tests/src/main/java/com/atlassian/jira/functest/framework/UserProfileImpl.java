package com.atlassian.jira.functest.framework;

import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import net.sourceforge.jwebunit.WebTester;

import static com.atlassian.jira.functest.framework.util.dom.DomKit.getCollapsedText;

/**
 * Default implementation of UserProfile
 *
 * @since v3.13
 */
public class UserProfileImpl extends AbstractFuncTestUtil implements UserProfile
{
    private final Navigation navigation;

    public UserProfileImpl(final WebTester tester, final JIRAEnvironmentData environmentData, final Navigation navigation)
    {
        super(tester, environmentData, 2);
        this.navigation = navigation;
    }

    @Override
    public String userName()
    {
        gotoCurrentUserProfile();

        return getCollapsedText(locators.id("up-user-title-name").getNode());
    }

    @Override
    public Link link()
    {
        return new DefaultLink(tester);
    }

    public void changeUserNotificationType(final boolean useHtml)
    {
        log("Changing user notification type to " + (useHtml ? "HTML" : "plain text"));

        gotoCurrentUserProfile();
        gotoEditUserPreferences();

        if (useHtml)
        {
            tester.selectOption("userNotificationsMimeType", "HTML");
        }
        else
        {
            tester.selectOption("userNotificationsMimeType", "Text");
        }
        tester.submit();
    }

    public void changeUserSharingType(boolean global)
    {
        log("changing user sharing default to '" + (global ? "public" : "private")  + "'.");

        gotoCurrentUserProfile();
        gotoEditUserPreferences();

        tester.setFormElement("shareDefault", String.valueOf(!global));
        tester.submit();
    }

    public void changeDefaultSharingType(boolean global)
    {
        log("changing default user sharing to '" + (global ? "public" : "private")  + "'.");

        gotoEditDefaultPreferences();
        
        tester.setFormElement("sharePublic", String.valueOf(!global));
        tester.submit("Update");
    }

    public void changeUserLanguage(final String lang)
    {
        log("Changing user language to '" + lang + "'");

        gotoCurrentUserProfile();
        gotoEditUserPreferences();

        tester.selectOption("userLocale", lang);
        tester.submit();
    }

    @Override
    public void changeUserLanguageByValue(String langValue)
    {
        log("Changing user language to '" + langValue + "'");

        gotoCurrentUserProfile();
        gotoEditUserPreferences();

        tester.getDialog().setFormParameter("userLocale", langValue);
        tester.submit();
    }

    @Override
    public void changeUserLanguageToJiraDefault()
    {
        changeUserLanguageByValue("-1");
    }

    private void gotoEditDefaultPreferences()
    {
        tester.gotoPage("secure/admin/jira/EditUserDefaultSettings!default.jspa");
    }

    private void gotoEditUserPreferences()
    {
        tester.clickLink("edit_prefs_lnk");
    }

    public void gotoCurrentUserProfile()
    {
        tester.clickLink("view_profile");
    }

    public void gotoUserProfile(String userName)
    {
        tester.gotoPage("/secure/ViewProfile.jspa?name=" + userName);
    }

    @Override
    public UserProfile changeUserTimeZone(String timeZoneID)
    {
        gotoCurrentUserProfile();
        gotoEditUserPreferences();
        tester.setFormElement("defaultUserTimeZone", timeZoneID == null ? "JIRA" : timeZoneID);
        tester.submit();

        return this;
    }

    @Override
    public UserProfile changeNotifyMyChanges(boolean notify)
    {
        gotoCurrentUserProfile();
        gotoEditUserPreferences();
        tester.selectOption("notifyOwnChanges", notify ? "Notify me" : "Do not notify me");
        tester.submit();

        return this;
    }

    @Override
    public UserProfile changeAutowatch(boolean autowatch)
    {
        gotoCurrentUserProfile();
        gotoEditUserPreferences();
        tester.selectOption("autoWatchPreference", autowatch ? "Enabled" : "Disabled");
        tester.submit();

        return this;
    }

    public static class DefaultLink implements Link
    {
        private final WebTester tester;

        public DefaultLink(WebTester tester)
        {
            this.tester = tester;
        }

        @Override
        public boolean isPresent()
        {
            return new IdLocator(tester, "header-details-user-fullname").exists();
        }
    }
}
