package com.atlassian.jira.avatar;

import com.atlassian.jira.config.properties.ApplicationProperties;

import javax.annotation.Nullable;

/**
 * Gravatar settings.
 */
public class GravatarSettingsImpl implements GravatarSettings
{
    private static final String ALLOW_GRAVATAR = "jira.user.avatar.gravatar.enabled";
    private static final String CUSTOM_SERVER = "jira.user.avatar.gravatar.server";

    private final ApplicationProperties applicationProperties;

    public GravatarSettingsImpl(ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    @Override
    public boolean isAllowGravatars()
    {
        return applicationProperties.getOption(ALLOW_GRAVATAR);
    }

    @Override
    public void setAllowGravatars(boolean allowGravatar)
    {
        applicationProperties.setOption(ALLOW_GRAVATAR, allowGravatar);
    }

    @Nullable
    @Override
    public String getCustomApiAddress()
    {
        return applicationProperties.getString(CUSTOM_SERVER);
    }

    @Override
    public void setCustomApiAddress(@Nullable String customApiAddress)
    {
        applicationProperties.setString(CUSTOM_SERVER, customApiAddress);
    }
}
