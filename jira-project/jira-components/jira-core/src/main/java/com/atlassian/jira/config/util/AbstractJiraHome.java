package com.atlassian.jira.config.util;

import com.atlassian.jira.cluster.ClusterNodeProperties;
import com.atlassian.jira.cluster.ClusterNodePropertiesImpl;

import org.apache.axis.utils.StringUtils;

import java.io.File;

import javax.annotation.Nonnull;

/**
 * Class that helps with the implementation of the {@link com.atlassian.jira.config.util.JiraHome} interface.
 *
 * @since v4.1
 */
public abstract class AbstractJiraHome implements JiraHome
{
    @Nonnull
    @Override
    public File getHome()
    {
        ClusterNodeProperties clusterNodeProperties = new ClusterNodePropertiesImpl(this);
        String sharedHomePath = clusterNodeProperties.getSharedHome();
        if (StringUtils.isEmpty(sharedHomePath))
        {
            return getLocalHome();
        }
        return new File(sharedHomePath);
    }

    public final File getLogDirectory()
    {
        return new File(getLocalHome(), JiraHome.LOG);
    }

    public final File getCachesDirectory()
    {
        return new File(getLocalHome(), JiraHome.CACHES);
    }

    public File getSharedCachesDirectory()
    {
        return new File(getHome(), JiraHome.CACHES);
    }

    public final File getExportDirectory()
    {
        return new File(getHome(), JiraHome.EXPORT);
    }

    public final File getImportDirectory()
    {
        return new File(getHome(), JiraHome.IMPORT);
    }

    public final File getImportAttachmentsDirectory()
    {
        return new File(getImportDirectory(), JiraHome.IMPORT_ATTACHMENTS);
    }

    public final File getPluginsDirectory()
    {
        return new File(getHome(), JiraHome.PLUGINS);
    }

    public final File getDataDirectory()
    {
        return new File(getHome(), JiraHome.DATA);
    }

    public final String getHomePath()
    {
        return getHome().getAbsolutePath();
    }

    public final String getLocalHomePath()
    {
        return getLocalHome().getAbsolutePath();
    }
}
