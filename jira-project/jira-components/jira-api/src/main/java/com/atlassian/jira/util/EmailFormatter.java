package com.atlassian.jira.util;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Returned email address appropriately masked/hidden for the current user.
 */
@PublicApi
public interface EmailFormatter
{
    /**
     * Returns whether or not email addresses are visible to this user, as determined by the
     * {@link com.atlassian.jira.config.properties.APKeys#JIRA_OPTION_EMAIL_VISIBLE} setting.
     *
     * @param currentUser the user to which email addresses will be shown, or {@code null} if
     *          no user is logged in (browsing anonymously)
     * @return {@code true} if email addresses should be visible (even if they will be masked);
     *          {@code false} otherwise
     * @since v4.3 (moved to API in v6.0)
     */
    boolean emailVisible(@Nullable User currentUser);

    /**
     * Formats {@code user}'s email address for the purpose of displaying it to {@code currentUser},
     * as determined by the
     * {@link com.atlassian.jira.config.properties.APKeys#JIRA_OPTION_EMAIL_VISIBLE} setting.
     * <p/>
     * This convenience method is exactly equivalent to
     * {@link #formatEmail(String, boolean) formatEmail(user.getEmailAddress(), currentUser != null)},
     * except that it is {@code null}-safe.
     *
     * @param user owner of the email address to format and display ({@code null} is permitted)
     * @param currentUser the user to which email addresses will be shown, or {@code null} if
     *          no user is logged in (browsing anonymously)
     * @return the formatted email address; {@code null} if either {@code user} is {@code null}
     *          or {@code currentUser} is not permitted to see email addresses
     * @since v4.3 (moved to API in v6.0)
     */
    @Nullable String formatEmail(@Nullable User user, @Nullable User currentUser);

    /**
     * Formats an email address for the purpose of displaying it to a user
     * as determined by the
     * {@link com.atlassian.jira.config.properties.APKeys#JIRA_OPTION_EMAIL_VISIBLE} setting.
     * <p/>
     * <table>
     * <tr><th>Setting</th><th>Behaviour</th></tr>
     * <tr><td>{@code "show"}</td><td>The email is shown as-is to everyone.</td></tr>
     * <tr><td>{@code "user"}</td><td>The email is shown as-is to users that
     *          are logged in, but not shown to anonymous users.</td></tr>
     * <tr><td>{@code "mask"}</td><td>The email is shown to all users with the
     *          e-mail address slightly obscured, such that {@code "user@example.com"} appears
     *          as {@code "user at example dot com"}, instead</td></tr>
     * </table>
     *
     * @param email The email address to show/mask/hide.
     * @param isCurrentUserLoggedIn {@code true} if a user is currently logged in; {@code false}
     *          if the user is browsing anonymously
     * @return the formatted email address; {@code null} if either {@code email} is {@code null}
     *          or the user is not permitted to see email addresses
     */
    @Nullable String formatEmail(@Nullable String email, boolean isCurrentUserLoggedIn);

    /**
     * Formats an email address for the purpose of displaying it to a user
     * as determined by the
     * {@link com.atlassian.jira.config.properties.APKeys#JIRA_OPTION_EMAIL_VISIBLE} setting.
     * <p/>
     * This convenience method is exactly equivalent to
     * {@link #formatEmail(String, boolean) formatEmail(email, currentUser != null)}.
     *
     * @param email The email address to show/mask/hide.
     * @param currentUser The user viewing the email address.
     * @return the formatted email address; {@code null} if either {@code email} is {@code null}
     *          or the user is not permitted to see email addresses
     * @since v4.3 (moved to API in v6.0)
     */
    @Nullable String formatEmail(@Nullable String email, @Nullable User currentUser);

    /**
     * Returns email address as HTML links, if appropriate.  If email addresses are masked,
     * then they are returned as HTML-escaped text, but not as links.
     *
     * @return {@code <a href="foo@bar.com">foo@bar.com</a>} (public),
     *         {@code foo at bar.com} (masked), or
     *         an empty string ({@code ""}) if either {@code email} is {@code null}
     *         or the user is not permitted to see email addresses
     * @since v4.3 (moved to API in v6.0)
     */
    @Nonnull String formatEmailAsLink(String email, @Nullable User currentUser);
}
