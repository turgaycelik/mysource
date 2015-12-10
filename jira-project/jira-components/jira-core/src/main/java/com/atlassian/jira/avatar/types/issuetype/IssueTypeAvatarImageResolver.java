package com.atlassian.jira.avatar.types.issuetype;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.types.BasicAvatarsImageResolver;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;

// for DI purposes (really needed?)
public class IssueTypeAvatarImageResolver extends BasicAvatarsImageResolver
{
    public IssueTypeAvatarImageResolver(final VelocityRequestContextFactory velocityRequestContextFactory, final ApplicationProperties applicationProperties)
    {
        super(Avatar.Type.ISSUETYPE, velocityRequestContextFactory, applicationProperties);
    }
}
