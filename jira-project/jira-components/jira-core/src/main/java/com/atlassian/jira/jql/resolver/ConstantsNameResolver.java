package com.atlassian.jira.jql.resolver;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.IssueConstant;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

/**
 * Common base class for issue constants providing most of the resolution logic.
 *
 * @since v4.0
 */
public abstract class ConstantsNameResolver<T> implements NameResolver<T>
{
    private final ConstantsManager constantsManager;
    private final String constantName;

    public ConstantsNameResolver(ConstantsManager constantsManager, String constantName)
    {
        this.constantsManager = constantsManager;
        this.constantName = constantName;
    }

    public List<String> getIdsFromName(String name)
    {
        IssueConstant constant = constantsManager.getConstantByNameIgnoreCase(constantName, name);

        if (constant != null)
        {
            return Lists.newArrayList(constant.getId());
        }
        return Collections.emptyList();
    }

    public boolean nameExists(final String name)
    {
        return constantsManager.getConstantByNameIgnoreCase(constantName, name) != null;
    }

    public boolean idExists(final Long id)
    {
        return get(id) != null;
    }

    @SuppressWarnings("unchecked")
    public T get(final Long id)
    {
        return (T) constantsManager.getConstantObject(constantName, id.toString());
    }
}
