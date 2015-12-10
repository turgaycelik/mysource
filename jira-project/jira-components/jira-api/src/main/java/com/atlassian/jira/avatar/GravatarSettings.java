package com.atlassian.jira.avatar;

import javax.annotation.Nullable;

/**
 * Gravatar settings for this JIRA.
 *
 * @since JIRA 6.3
 */
public interface GravatarSettings
{
    /**
     * Gets the "gravatars allowed" JIRA-wide setting.
     *
     * @return true if Gravatar support is enabled
     * @since JIRA 6.3
     */
    boolean isAllowGravatars();

    /**
     * Sets the "gravatars allowed" JIRA-wide setting.
     *
     * @param allowGravatar whether to allow Gravatars
     * @since JIRA 6.3
     */
    void setAllowGravatars(boolean allowGravatar);

    /**
     * Gets the custom Gravatar server's API address.
     *
     * @return the custom server's API address, or {@code null}
     */
    @Nullable
    String getCustomApiAddress();

    /**
     * Sets a custom Gravatar server's API address.
     *
     * @param gravatarServer the server's API URL ({@code null} to use Gravatar.com)
     */
    void setCustomApiAddress(@Nullable String gravatarServer);
}
