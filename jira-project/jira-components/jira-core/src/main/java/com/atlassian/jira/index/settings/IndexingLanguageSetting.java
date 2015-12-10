package com.atlassian.jira.index.settings;

import com.atlassian.jira.config.ReindexMessageManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.user.ApplicationUser;

import static com.atlassian.jira.user.ApplicationUsers.toDirectoryUser;

/**
 * Represents the indexing language setting for this JIRA instance.
 *
 * @since v6.0
 */
public class IndexingLanguageSetting
{
    private static final String REINDEX_NOTIFICATION_MSG_I18N_KEY = "admin.notifications.task.general.configuration.indexing.language";
    private final ApplicationProperties applicationProperties;
    private final ReindexMessageManager reindexMessageManager;

    public IndexingLanguageSetting(final ApplicationProperties applicationProperties, final ReindexMessageManager reindexMessageManager)
    {
        this.applicationProperties = applicationProperties;
        this.reindexMessageManager = reindexMessageManager;
    }

    public String getValue()
    {
        return applicationProperties.getString(APKeys.JIRA_I18N_LANGUAGE_INPUT);
    }

    public void setValueTo(final ApplicationUser user, final String indexingLanguage)
    {
        if (!getValue().equals(indexingLanguage))
        {
            applicationProperties.setString(APKeys.JIRA_I18N_LANGUAGE_INPUT, indexingLanguage);
            reindexMessageManager.pushMessage(toDirectoryUser(user), REINDEX_NOTIFICATION_MSG_I18N_KEY);
        }
    }
}
