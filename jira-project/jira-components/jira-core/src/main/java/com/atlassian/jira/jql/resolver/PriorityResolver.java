package com.atlassian.jira.jql.resolver;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.priority.Priority;

import java.util.Collection;

/**
 * Resolves Priority objects.
 *
 * @since v4.0
 */
public class PriorityResolver extends ConstantsNameResolver<Priority>
{
    private ConstantsManager constantsManager;

    public PriorityResolver(final ConstantsManager constantsManager)
    {
        super(constantsManager, ConstantsManager.PRIORITY_CONSTANT_TYPE);
        this.constantsManager = constantsManager;
    }

    public Collection<Priority> getAll()
    {
        return constantsManager.getPriorityObjects();
    }

}
