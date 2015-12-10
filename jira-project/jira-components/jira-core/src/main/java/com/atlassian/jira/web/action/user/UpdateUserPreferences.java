package com.atlassian.jira.web.action.user;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.atlassian.core.user.preferences.Preferences;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.config.LocaleManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.event.user.UserPreferencesUpdatedEvent;
import com.atlassian.jira.issue.fields.option.TextOption;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.timezone.RegionInfo;
import com.atlassian.jira.timezone.RegionInfoImpl;
import com.atlassian.jira.timezone.TimeZoneInfo;
import com.atlassian.jira.timezone.TimeZoneInfoImpl;
import com.atlassian.jira.timezone.TimeZoneService;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.preferences.ExtendedPreferences;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.session.SessionPagerFilterManager;
import com.atlassian.jira.web.session.SessionSearchObjectManagerFactory;
import com.atlassian.jira.web.util.HelpUtil;

import com.google.common.collect.ImmutableMap;

import org.apache.commons.lang.StringUtils;

import webwork.action.Action;
import webwork.action.ActionContext;

@SuppressWarnings("unused")
public class UpdateUserPreferences extends JiraWebActionSupport
{
    private static enum AutoWatchPreference
    {
        ENABLED,
        DISABLED,
        INHERIT
    }

    public static final int MAX_ISSUES_PER_PAGE_SETTING = 1000;

    private final UserPreferencesManager userPreferencesManager;
    private final LocaleManager localeManager;
    private final SessionSearchObjectManagerFactory sessionSearchObjectManagerFactory;
    private final TimeZoneService timeZoneManager;
    private final EventPublisher eventPublisher;

    private String username;
    private long userIssuesPerPage;
    private String userNotificationsMimeType;
    private String userLocale;
    private boolean notifyOwnChanges;
    private boolean shareDefault;
    private boolean keyboardShortcutsEnabled;
    private String timeZoneId;
    private String autoWatchPreference;


    public UpdateUserPreferences(final UserPreferencesManager userPreferencesManager,
                                 final LocaleManager localeManager,
                                 final SessionSearchObjectManagerFactory sessionSearchObjectManagerFactory,
                                 final TimeZoneService timeZoneManager,
                                 final EventPublisher eventPublisher)
    {
        this.userPreferencesManager = userPreferencesManager;
        this.localeManager = localeManager;
        this.sessionSearchObjectManagerFactory = sessionSearchObjectManagerFactory;
        this.timeZoneManager = timeZoneManager;
        this.eventPublisher = eventPublisher;
    }

    public String doDefault()
    {
        final ApplicationUser current = getLoggedInApplicationUser();
        if (current == null || !current.getUsername().equals(username))
        {
            return ERROR;
        }

        setUserIssuesPerPage(getUserPreferences().getLong(PreferenceKeys.USER_ISSUES_PER_PAGE));
        setUserNotificationsMimeType(getUserPreferences().getString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE));
        setNotifyOwnChanges(getUserPreferences().getBoolean(PreferenceKeys.USER_NOTIFY_OWN_CHANGES));
        setShareDefault(getUserPreferences().getBoolean(PreferenceKeys.USER_DEFAULT_SHARE_PRIVATE));
        setUserLocale(getUserPreferences().getString(PreferenceKeys.USER_LOCALE));
        setKeyboardShortcutsEnabled(!getUserPreferences().getBoolean(PreferenceKeys.USER_KEYBOARD_SHORTCUTS_DISABLED));
        setAutoWatchPreference(getUserAutoWatchPreference(getUserPreferences()).name());

        return Action.INPUT;
    }

    private AutoWatchPreference getUserAutoWatchPreference(final Preferences userPreferences)
    {
        if (userPreferences instanceof ExtendedPreferences)
        {
            final ExtendedPreferences extendedPreferences = (ExtendedPreferences)userPreferences;
            boolean autoWatchForUserSet = extendedPreferences.containsValue(PreferenceKeys.USER_AUTOWATCH_DISABLED);
            if (autoWatchForUserSet)
            {
                boolean autoWatchDisabled = extendedPreferences.getBoolean(PreferenceKeys.USER_AUTOWATCH_DISABLED);
                if (autoWatchDisabled)
                {
                    return AutoWatchPreference.DISABLED;
                }
                else
                {
                    return AutoWatchPreference.ENABLED;
                }
            }
        }
        return AutoWatchPreference.INHERIT;
    }

    public boolean getShowPluginHints()
    {
        return getApplicationProperties().getOption(APKeys.JIRA_SHOW_MARKETING_LINKS);
    }

    public String getTacUrl()
    {
        return HelpUtil.getInstance().getHelpPath("userpreferences.language.more.from.tac").getUrl();
    }

    public long getUserIssuesPerPage()
    {
        return userIssuesPerPage;
    }

    public void setUserIssuesPerPage(long aLong)
    {
        userIssuesPerPage = aLong;
    }

    public String getUserNotificationsMimeType()
    {
        return userNotificationsMimeType;
    }

    public void setUserNotificationsMimeType(String userNotificationsMimeType)
    {
        this.userNotificationsMimeType = userNotificationsMimeType;
    }

    public void setShareDefault(final boolean isPublic)
    {
        shareDefault = isPublic;
    }

    public boolean isShareDefault()
    {
        return shareDefault;
    }

    /**
     * Gets the available list of options for the Sharing Default preference
     *
     * @return the available list of options for the Sharing Default preference
     */
    public Collection<TextOption> getOwnChangesList()
    {
        final String notify = getText("preferences.notification.on.short");
        final String doNotNotify = getText("preferences.notification.off.short");

        return CollectionBuilder.list(new TextOption("true", notify),
                new TextOption("false", doNotNotify));
    }
    /**
     * Gets the available list of options for the Sharing Default preference
     *
     * @return the available list of options for the Sharing Default preference
     */
    public Collection<TextOption> getShareList()
    {
        final String publicText = getText("preferences.default.share.shared.short");
        final String privateText = getText("preferences.default.share.unshared.short");

        return CollectionBuilder.list(new TextOption("false", publicText),
                new TextOption("true", privateText));
    }

    /**
     * The current value of the Sharing default
     *
     * @return The current value of the Sharing default, false (public) or true (private)
     */
    public String getShareValue()
    {
        return String.valueOf(getUserPreferences().getBoolean(PreferenceKeys.USER_DEFAULT_SHARE_PRIVATE));
    }

    /**
     * Gets the available list of options for the Keyboard shortcut preference
     *
     * @return the available list of options for the keyboard shortcut Default preference
     */
    public Collection<TextOption> getKeyboardShortcutList()
    {
        final String enabledText = getText("preferences.keyboard.shortcuts.enabled");
        final String disabledText = getText("preferences.keyboard.shortcuts.disabled");

        return CollectionBuilder.list(new TextOption("true", enabledText),
                new TextOption("false", disabledText));
    }


    /**
     * The current value of the keyboard shortcut default
     *
     * @return The current value of the keyboard shortcut default, true (enabled) or false (disabled)
     */
    public String getKeyboardShortcutValue()
    {
        return String.valueOf(!getUserPreferences().getBoolean(PreferenceKeys.USER_KEYBOARD_SHORTCUTS_DISABLED));
    }

    public Collection<TextOption> getAutowatchList()
    {
        return CollectionBuilder.list(
                new TextOption(AutoWatchPreference.ENABLED.name(), getText("preferences.autowatch.enabled")),
                new TextOption(AutoWatchPreference.DISABLED.name(), getText("preferences.autowatch.disabled")),
                new TextOption(AutoWatchPreference.INHERIT.name(), getText("preferences.autowatch.inherit"))
        );
    }

    public String getUserLocale()
    {
        return userLocale;
    }

    public void setUserLocale(String userLocale)
    {
        this.userLocale = userLocale;
    }

    public void setDefaultUserTimeZone(String timeZoneId)
    {
        this.timeZoneId = timeZoneId;
    }

    public List<RegionInfo> getTimeZoneRegions()
    {
        List<RegionInfo> regions = timeZoneManager.getTimeZoneRegions(getJiraServiceContext());
        // Add the jira region to the beginning
        regions.add(0, new RegionInfoImpl(TimeZoneService.JIRA, getText("timezone.region.jira")));
        return regions;
    }

    public List<TimeZoneInfo> getTimeZoneInfos()
    {
        List<TimeZoneInfo> timeZoneInfos = timeZoneManager.getTimeZoneInfos(getJiraServiceContext());
        TimeZoneInfo jiraDefaultTimeZone = timeZoneManager.getDefaultTimeZoneInfo(getJiraServiceContext());
        TimeZoneInfoImpl timeZoneInfo = new TimeZoneInfoImpl(TimeZoneService.JIRA, jiraDefaultTimeZone.getDisplayName(),  jiraDefaultTimeZone.toTimeZone(), getJiraServiceContext().getI18nBean(), TimeZoneService.JIRA);
        timeZoneInfos.add(timeZoneInfo);
        return timeZoneInfos;
    }

    public String getConfiguredTimeZoneRegion()
    {
        if (timeZoneManager.usesJiraTimeZone(getJiraServiceContext()))
        {
            return TimeZoneService.JIRA;
        }
        return timeZoneManager.getUserTimeZoneInfo(getJiraServiceContext()).getRegionKey();
    }

    public String getConfiguredTimeZoneId()
    {
        if (timeZoneManager.usesJiraTimeZone(getJiraServiceContext()))
        {
            return TimeZoneService.JIRA;
        }
        return timeZoneManager.getUserTimeZoneInfo(getJiraServiceContext()).getTimeZoneId();
    }

    public Map<String,String> getMimeTypes()
    {
        return ImmutableMap.of(
                NotificationRecipient.MIMETYPE_HTML, NotificationRecipient.MIMETYPE_HTML_DISPLAY,
                NotificationRecipient.MIMETYPE_TEXT, NotificationRecipient.MIMETYPE_TEXT_DISPLAY);
    }

    public boolean getNotifyOwnChanges()
    {
        return notifyOwnChanges;
    }

    public void setNotifyOwnChanges(boolean notifyOwnChanges)
    {
        this.notifyOwnChanges = notifyOwnChanges;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(final String username)
    {
        this.username = username;
    }

    /**
     * @return the installed locales with the default option at the top
     */
    public Map<String, String> getInstalledLocales()
    {
        return localeManager.getInstalledLocalesWithDefault(getApplicationProperties().getDefaultLocale(), this);
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        final ApplicationUser current = getLoggedInApplicationUser();
        if (current == null || !current.getName().equals(username))
        {
            return ERROR;
        }

        final Preferences userPreferences = getUserPreferences();
        userPreferences.setLong(PreferenceKeys.USER_ISSUES_PER_PAGE, getUserIssuesPerPage());
        userPreferences.setString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE, getUserNotificationsMimeType());
        userPreferences.setBoolean(PreferenceKeys.USER_DEFAULT_SHARE_PRIVATE, isShareDefault());
        userPreferences.setBoolean(PreferenceKeys.USER_NOTIFY_OWN_CHANGES, getNotifyOwnChanges());
        userPreferences.setBoolean(PreferenceKeys.USER_KEYBOARD_SHORTCUTS_DISABLED, !isKeyboardShortcutsEnabled());

        if (getShowAutowatch())
        {
            final AutoWatchPreference watchPreference = AutoWatchPreference.valueOf(autoWatchPreference);
            if (watchPreference.equals(AutoWatchPreference.INHERIT))
            {
                if (userPreferences instanceof ExtendedPreferences &&
                        ((ExtendedPreferences)userPreferences).containsValue(PreferenceKeys.USER_AUTOWATCH_DISABLED))
                {
                    userPreferences.remove(PreferenceKeys.USER_AUTOWATCH_DISABLED);
                }
            }
            else
            {
                userPreferences.setBoolean(PreferenceKeys.USER_AUTOWATCH_DISABLED, watchPreference == AutoWatchPreference.DISABLED);
            }
        }

        if (LocaleManager.DEFAULT_LOCALE.equals(getUserLocale()))
        {
            String locale = getUserPreferences().getString(PreferenceKeys.USER_LOCALE);
            if (locale != null)
            {
                getUserPreferences().remove(PreferenceKeys.USER_LOCALE);
            }
        }
        else
        {
            getUserPreferences().setString(PreferenceKeys.USER_LOCALE, getUserLocale());
        }

        if (TimeZoneService.JIRA.equals(timeZoneId))
        {
           timeZoneManager.clearUserDefaultTimeZone(getJiraServiceContext());
        }
        else
        {
           timeZoneManager.setUserDefaultTimeZone(timeZoneId, getJiraServiceContext());
        }
        
        eventPublisher.publish(new UserPreferencesUpdatedEvent(current.getDirectoryUser()));

        // remove any current pagers in the session
        Map<?,?> session = ActionContext.getSession();
        session.remove(SessionKeys.GENERIC_PAGER);
        getSessionPagerFilterManager().setCurrentObject(null);

        return returnComplete("ViewProfile.jspa");
    }

    private SessionPagerFilterManager getSessionPagerFilterManager()
    {
        return sessionSearchObjectManagerFactory.createPagerFilterManager();
    }

    protected void doValidation()
    {
        localeManager.validateUserLocale(getLoggedInUser(), getUserLocale(), this);
        
        if(!StringUtils.equals(getUserNotificationsMimeType(), NotificationRecipient.MIMETYPE_TEXT) &&
                !StringUtils.equals(getUserNotificationsMimeType(), NotificationRecipient.MIMETYPE_HTML))
        {
            addError("userNotificationsMimeType", getText("preferences.invalid.mime.type"));
        }
        
        if (getUserIssuesPerPage() <= 0 || getUserIssuesPerPage() > MAX_ISSUES_PER_PAGE_SETTING)
        {
            addError("userIssuesPerPage", getText("preferences.issues.per.page.error"));
        }
        super.doValidation();
    }

    public boolean isKeyboardShortcutsEnabled()
    {
        return keyboardShortcutsEnabled;
    }

    public void setKeyboardShortcutsEnabled(final boolean keyboardShortcutsEnabled)
    {
        this.keyboardShortcutsEnabled = keyboardShortcutsEnabled;
    }

    public String getAutoWatchPreference()
    {
        return autoWatchPreference;
    }

    public void setAutoWatchPreference(final String autoWatchPreference)
    {
        this.autoWatchPreference = autoWatchPreference;
    }

    public boolean getShowAutowatch()
    {
        return getApplicationProperties().getOption(APKeys.JIRA_OPTION_WATCHING);
    }

}
