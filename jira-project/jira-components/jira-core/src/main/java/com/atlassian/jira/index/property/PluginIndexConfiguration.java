package com.atlassian.jira.index.property;

import java.sql.Timestamp;

import com.atlassian.jira.index.IndexDocumentConfiguration;

/**
 * @since v6.2
 */
public interface PluginIndexConfiguration
{
    String getPluginKey();
    String getModuleKey();
    IndexDocumentConfiguration getIndexDocumentConfiguration();
    Timestamp getLastUpdated();
}
