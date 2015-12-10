package com.atlassian.jira.web.action.admin;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.option.TextOption;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;

import java.util.Collection;

import static com.atlassian.jira.web.action.user.UpdateUserPreferences.MAX_ISSUES_PER_PAGE_SETTING;

/**
 * Controls setting the email prefs.
 */
@WebSudoRequired
@SuppressWarnings("unused")
public class EditUserDefaultSettings extends ViewUserDefaultSettings
{
    public String preference;
    public String numIssues;
    public boolean emailUser = false;
    public boolean sharePublic;
    private boolean autoWatch = false;

    public String doDefault() throws Exception
    {
        preference = getApplicationProperties().getDefaultBackedString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE);
        autoWatch = !getApplicationProperties().getOption(PreferenceKeys.USER_AUTOWATCH_DISABLED);
        emailUser = getApplicationProperties().getOption(PreferenceKeys.USER_NOTIFY_OWN_CHANGES);
        // clear any caches, to ensure they are refreshed (defensive code)
        ComponentAccessor.getUserPreferencesManager().clearCache();

        return super.doDefault();
    }

    protected void doValidation()
    {
        if (!TextUtils.stringSet(preference) || (!preference.equals("html") && !preference.equals("text")))
        {
            addError("preference", getText("admin.errors.specify.notification.preference"));
        }

        if (TextUtils.stringSet(numIssues))
        {
            try
            {
                int issues = Integer.parseInt(numIssues);
                if (issues <= 0 || issues > MAX_ISSUES_PER_PAGE_SETTING)
                {
                    addError("numIssues", getText("preferences.issues.per.page.error"));
                }
            }
            catch (NumberFormatException e)
            {
                addError("numIssues", getText("admin.errors.num.issues.displayed.must.be.num"));
            }
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        getApplicationProperties().setString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE, preference);
        getApplicationProperties().setOption(PreferenceKeys.USER_NOTIFY_OWN_CHANGES, emailUser);
        getApplicationProperties().setOption(PreferenceKeys.USER_DEFAULT_SHARE_PRIVATE, sharePublic);
        getApplicationProperties().setString(PreferenceKeys.USER_ISSUES_PER_PAGE, numIssues);
        getApplicationProperties().setOption(PreferenceKeys.USER_AUTOWATCH_DISABLED, !autoWatch);

        // clear any caches, to ensure they are refreshed (defensive code)
        ComponentAccessor.getUserPreferencesManager().clearCache();

        return returnComplete("ViewUserDefaultSettings.jspa");
    }


    public Collection getEmailFormats()
    {
        return EasyList.build(new TextOption("text", getText("admin.userdefaults.text")),
                new TextOption("html", getText("admin.userdefaults.html")));
    }

    public String getSelectedEmailFormat()
    {
        return getApplicationProperties().getString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE);
    }

    /**
     * Get the list of options avaiable for sharing
     *
     * @return List options available
     */
    public Collection /*<Option>*/ getShareList()
    {
        return EasyList.build(new TextOption("false", getText("admin.common.words.public")),
                new TextOption("true", getText("admin.common.words.private")));
    }

    /**
     * The current value of the sharing default
     * @return current value of the sharing default, false (public) or true (private)
     */
    public String getShareValue()
    {
        return String.valueOf(getApplicationProperties().getOption(PreferenceKeys.USER_DEFAULT_SHARE_PRIVATE));
    }

    public String getPreference()
    {
        return preference;
    }

    public void setPreference(String preference)
    {
        this.preference = preference;
    }

    public boolean isEmailUser()
    {
        return emailUser;
    }

    public void setEmailUser(boolean emailUser)
    {
        this.emailUser = emailUser;
    }

    public boolean isSharePublic()
    {
        return sharePublic;
    }

    public void setSharePublic(final boolean sharePublic)
    {
        this.sharePublic = sharePublic;
    }

    public String getNumIssues()
    {
        return numIssues;
    }

    public void setNumIssues(String numIssues)
    {
        this.numIssues = numIssues;
    }

    public boolean isAutoWatch()
    {
        return autoWatch;
    }

    public void setAutoWatch(boolean autoWatch)
    {
        this.autoWatch = autoWatch;
    }

}
