package com.atlassian.jira.config;

import com.atlassian.jira.util.I18nHelper;

/**
 * @since v6.1
 */
public class ForegroundIndexTaskContext extends IndexTaskContext
{
    @Override
    public String getTaskInProgressMessage(final I18nHelper i18n)
    {
        return i18n.getText("admin.notifications.reindex.in.progress.foreground");
    }
}
