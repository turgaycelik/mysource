package com.atlassian.jira.avatar.types.project;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.types.BasicAvatarsImageResolver;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;

public class ProjectAvatarImageResolver extends BasicAvatarsImageResolver
{
    public ProjectAvatarImageResolver(final VelocityRequestContextFactory velocityRequestContextFactory, final ApplicationProperties applicationProperties)
    {
        super(Avatar.Type.PROJECT, velocityRequestContextFactory, applicationProperties);
    }
}
