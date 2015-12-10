package com.atlassian.jira.user.preferences;


/**
 * Keys used when storing user preferences.
 *
 */
public interface PreferenceKeys
{
    public static final String USER_ISSUES_PER_PAGE = "user.issues.per.page";
    public static final String USER_NOTIFICATIONS_MIMETYPE = "user.notifications.mimetype";
    public static final String USER_NOTIFY_OWN_CHANGES = "user.notify.own.changes";
    public static final String USER_ADDPORTLET_COLUMNS = "jira.user.dashboard.portlets.columns";
    public static final String USER_LOCALE = "jira.user.locale";
    public static final String USER_TIMEZONE = "jira.user.timezone";
    public static final String USER_DEFAULT_SHARE_PRIVATE = "user.default.share.private";
    public static final String USER_SHOW_ACTIONS_IN_NAVIGATOR = "user.show.actions.in.navigator";

    /**
     * <b>Ignored</b>. JQL can no longer be disabled on a per-user basis.
     *
     * @deprecated Since v6.0. JQL can no longer be disabled on a per-user basis.
     */
    @Deprecated
    public static final String USER_JQL_AUTOCOMPLETE_DISABLED = "user.jql.autocomplete.disabled";
    public static final String USER_WATCHES_REPORT_SORTS = "user.watches.report.sorts";
    public static final String USER_WATCHES_REPORT_UNRESOLVED_ONLY = "user.watches.report.unresolved.only";
    public static final String USER_VOTES_REPORT_SORTS = "user.votes.report.sorts";
    public static final String USER_VOTES_REPORT_UNRESOLVED_ONLY = "user.votes.report.unresolved.only";
    public static final String USER_KEYBOARD_SHORTCUTS_DISABLED = "user.keyboard.shortcuts.disabled";
    public static final String USER_AUTOWATCH_DISABLED = "user.autowatch.disabled";
}
