package com.atlassian.jira.web.action.admin;

import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.LocaleManager;
import com.atlassian.jira.startup.PluginInfoProvider;
import com.atlassian.jira.upgrade.UpgradeHistoryItem;

import java.util.Date;
import java.util.List;

/**
 * @since v4.1
 */
public class ViewUpgradeHistory extends ViewSystemInfo
{
    public ViewUpgradeHistory(LocaleManager localeManager, final PluginInfoProvider pluginInfoProvider, FeatureManager featureManager)
    {
        super(localeManager, pluginInfoProvider, featureManager);
    }

    public List<UpgradeHistoryItem> getUpgradeHistory()
    {
        return getExtendedSystemInfoUtils().getUpgradeHistory();
    }

    public String getFormattedTimePerformed(final Date timePerformed)
    {
        if (timePerformed == null)
        {
            return getText("common.words.unknown");
        }
        else
        {
            return getOutlookDate().formatDMYHMS(timePerformed);
        }
    }
}
