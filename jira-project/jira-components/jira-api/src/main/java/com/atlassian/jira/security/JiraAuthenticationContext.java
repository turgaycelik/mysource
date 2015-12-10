package com.atlassian.jira.security;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.api.IncompatibleReturnType;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.util.OutlookDate;

import java.util.Locale;

/**
 * The JiraAuthenticationContext is used for tracking a user's session in JIRA and all it's custom parameters, such as
 * Locale and I18n.
 */
@PublicApi
public interface JiraAuthenticationContext
{
    /**
     * Returns the currently logged in User.
     * <p/>
     * <b>Warning:</b> previous incarnations of this method returned <code>com.atlassian.crowd.embedded.api.User</code>.
     * This method had previously been deprecated and has been reused to return an {@link ApplicationUser} instead. That
     * class has different semantics for {@link Object#equals(Object)} and therefore does not extend the {@link User}
     * class.  This means that the 6.0 version is not binary or source compatible with earlier versions when this method
     * is used.
     *
     * @return The logged in {@code ApplicationUser}, or {@code null}
     */
    @IncompatibleReturnType (since = "6.0", was = "com.atlassian.crowd.embedded.api.User")
    ApplicationUser getUser();

    /**
     * Returns the currently logged in User.
     *
     * @return The logged in User, or null
     * @deprecated use {@link #getUser()}. Since v6.0
     */
    @Deprecated
    User getLoggedInUser();

    /**
     * Returns a boolean indicating whether there is a currently logged in user.
     *
     * @return true if there is a currently logged in user
     * @since v5.0.4
     */
    boolean isLoggedInUser();

    /**
     * Get the users locale.
     *
     * @return The user's locale, or the default system locale.
     */
    Locale getLocale();

    /**
     * Method used to get a nice representation of a date using a user's locale.
     *
     * @return A {@link OutlookDate}
     * @deprecated Use {@link com.atlassian.jira.datetime.DateTimeFormatter} instead. Since v5.0.
     */
    @Deprecated
    OutlookDate getOutlookDate();

    /**
     * @param key the text key
     * @return the translated text
     * @deprecated Use getText() method on {@link #getI18nHelper()}.
     */
    @Deprecated
    String getText(String key);

    /**
     * Useful for localisation of messages.
     *
     * @return An instance of {@link I18nHelper}
     */
    I18nHelper getI18nHelper();

    /**
     * Useful for localisation of messages.
     *
     * @return An instance of {@link I18nHelper}
     * @deprecated Use {@link #getI18nHelper()} instead. Deprecated since v4.0
     */
    @Deprecated
    I18nHelper getI18nBean();

    /**
     * This id usedin places like Jelly where we need to switch the identity of a user during execution.
     *
     * @param user the currently logged in user
     * @deprecated use {@link #setLoggedInUser(com.atlassian.jira.user.ApplicationUser)}. Since v6.0
     */
    void setLoggedInUser(User user);

    /**
     * This is used in places like Jelly where we need to switch the identity of a user during execution.
     *
     * @param user the currently logged in user
     */
    void setLoggedInUser(ApplicationUser user);

    /**
     * Clears any logged in user from authentication scope.  After this call the anonymous user is considered to be
     * about, ie no-one is about
     */
    void clearLoggedInUser();
}
