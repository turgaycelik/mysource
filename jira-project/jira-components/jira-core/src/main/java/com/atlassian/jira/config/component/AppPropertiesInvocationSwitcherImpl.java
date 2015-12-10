/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.config.component;

import com.atlassian.jira.config.properties.ApplicationProperties;

public class AppPropertiesInvocationSwitcherImpl implements InvocationSwitcher
{
    private final ApplicationProperties applicationProperties;
    private final String appPropertiesKey;

    public AppPropertiesInvocationSwitcherImpl(ApplicationProperties applicationProperties, String appPropertiesKey)
    {
        this.applicationProperties = applicationProperties;
        this.appPropertiesKey = appPropertiesKey;
    }

    public boolean isEnabled()
    {
        return applicationProperties.getOption(appPropertiesKey);
    }
}
