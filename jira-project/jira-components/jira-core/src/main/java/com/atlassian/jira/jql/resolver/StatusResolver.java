package com.atlassian.jira.jql.resolver;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.status.Status;

import java.util.Collection;

/**
 * Resolves Status objects.
 *
 * @since v4.0
 */
public class StatusResolver extends ConstantsNameResolver<Status>
{
    private ConstantsManager constantsManager;

    public StatusResolver(final ConstantsManager constantsManager)
    {
        super(constantsManager, ConstantsManager.STATUS_CONSTANT_TYPE);
        this.constantsManager = constantsManager;
    }

    public Collection<Status> getAll()
    {
        return constantsManager.getStatusObjects();
    }
}
