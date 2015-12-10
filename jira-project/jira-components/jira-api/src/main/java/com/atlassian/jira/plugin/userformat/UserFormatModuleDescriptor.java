package com.atlassian.jira.plugin.userformat;

import com.atlassian.jira.plugin.JiraResourcedModuleDescriptor;
import com.atlassian.jira.plugin.profile.UserFormat;

/**
 * <p>A module descriptor that produces a {@link com.atlassian.jira.plugin.profile.UserFormat} that can be used to
 * format a user for display. The provided user formats are tied to a &quot;type&quot;.</p>
 *
 * <p>There can be many user formats for a specific &quot;type&quot;, but only one of them will be used when
 * formatting the user name. </p>
 *
 * <p>Administrators configure which user format to use for a specific type in UI.<p/>
 *
 * @see com.atlassian.jira.plugin.profile.UserFormat
 * @since v4.4
 */
public interface UserFormatModuleDescriptor extends JiraResourcedModuleDescriptor<UserFormat>
{
    /**
     * Gets the user format &quot;type&quot; that this descriptor applies to.
     * @return The user format &quot;type&quot; for this descriptor.
     */
    public String getType();

    /**
     * Gets an i18n key to display a user friendly description for the type that this descriptor applies to.
     * @return An i18n key to display a user friendly description for the type.
     */
    public String getTypeI18nKey();
}
