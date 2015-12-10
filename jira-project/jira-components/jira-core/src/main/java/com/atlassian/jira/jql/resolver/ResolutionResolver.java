package com.atlassian.jira.jql.resolver;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.resolution.Resolution;

import java.util.Collection;

/**
 * Resolves Resolution objects.
 *
 * @since v4.0
 */
public class ResolutionResolver extends ConstantsNameResolver<Resolution>
{
    private final ConstantsManager constantsManager;

    public ResolutionResolver(ConstantsManager constantsManager)
    {
        super(constantsManager, ConstantsManager.RESOLUTION_CONSTANT_TYPE);
        this.constantsManager = constantsManager;
    }

    public Collection<Resolution> getAll()
    {
        return constantsManager.getResolutionObjects();
    }

    // This needs to be a special case for resolution since -1 is a valid value
    @Override
    public boolean idExists(final Long id)
    {
        return super.idExists(id);
    }
}
