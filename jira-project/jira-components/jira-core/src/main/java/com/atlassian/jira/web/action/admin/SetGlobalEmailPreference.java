package com.atlassian.jira.web.action.admin;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.user.util.GlobalUserPreferencesUtil;
import com.atlassian.sal.api.websudo.WebSudoRequired;

/**
 * Updates all the users preference to the current default preference.
 */
@WebSudoRequired
public class SetGlobalEmailPreference extends ViewUserDefaultSettings
{
    public long effectedUsers;
    private GlobalUserPreferencesUtil userUtil;

    public SetGlobalEmailPreference(GlobalUserPreferencesUtil userUtil)
    {
        this.userUtil = userUtil;
    }

    public String doDefault() throws Exception
    {
        // get the effected users count
        effectedUsers = userUtil.getTotalUpdateUserCountMailMimeType(getApplicationProperties().getDefaultBackedString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE));

        // clear any caches, to ensure they are refreshed (defensive code)
        ComponentAccessor.getUserPreferencesManager().clearCache();

        return super.doDefault();
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        String preference = getApplicationProperties().getDefaultBackedString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE);

        userUtil.updateUserMailMimetypePreference(preference);
        // update all prefs that need updating

        // clear any caches, to ensure they are refreshed (defensive code)
        ComponentAccessor.getUserPreferencesManager().clearCache();

        return getRedirect("ViewUserDefaultSettings.jspa");
    }

    public long getEffectedUsers()
    {
        return effectedUsers;
    }

    public void setEffectedUsers(long effectedUsers)
    {
        this.effectedUsers = effectedUsers;
    }

    public String getOtherMimeType()
    {
        String preference = getApplicationProperties().getDefaultBackedString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE);
        if ("text".equalsIgnoreCase(preference))
        {
            return "html";
        }
        else
        {
            return "text";
        }
    }

}
