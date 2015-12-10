/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.user;

import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.util.LocaleParser;
import com.atlassian.jira.web.util.JiraLocaleUtils;
import com.opensymphony.util.TextUtils;

public class ViewUserPreferences extends ViewProfile
{
    private static final long serialVersionUID = -5088947875273392836L;

    public ViewUserPreferences(UserPropertyManager userPropertyManager)
    {
        super(userPropertyManager);
    }

    public String getUserLocale()
    {
        String userLocale = getUserPreferences().getString(PreferenceKeys.USER_LOCALE);
        if (TextUtils.stringSet(userLocale))
        {
            return LocaleParser.parseLocale(userLocale).getDisplayName();
        }
        else
        {
            return getText(JiraLocaleUtils.DEFAULT_LOCALE_I18N_KEY, getApplicationProperties().getDefaultLocale().getDisplayName());
        }
    }
}
