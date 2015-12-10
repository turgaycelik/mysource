package com.atlassian.jira.my_home;

import com.atlassian.crowd.embedded.api.User;

import javax.annotation.Nonnull;

/**
 * Update the My JIRA Home to a new value.
 */
public interface MyJiraHomeUpdateService
{
    /**
     * Updates the user's My JIRA Home location to be provided by the given plugin module. It is expected, that the
     * plugin module is a {@link com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor}.
     *
     * @param user the user for which the location is set
     * @param completePluginModuleKey the complete key of the plugin module providing the My JIRA Home link
     *
     * @throws com.atlassian.jira.plugin.myjirahome.MyJiraHomeUpdateException if the update failed
     */
    void updateHome(@Nonnull User user, @Nonnull String completePluginModuleKey);
}
