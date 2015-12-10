package com.atlassian.jira.web.ui.header;

import com.atlassian.jira.config.FeatureManager;

/**
 * Uses a flag defined through a {@link com.atlassian.jira.plugin.profile.DarkFeatures dark feature} to retrieve
 * the current JIRA header
 *
 * @since v5.2
 * @see com.atlassian.jira.config.FeatureManager
 */
public class CurrentHeaderImpl implements CurrentHeader
{
    @Override
    @Deprecated // Since com.atlassian.jira.darkfeature.CommonHeader is permanently enabled
    public Header get()
    {
        return Header.COMMON;
    }
}
