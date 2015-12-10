package com.atlassian.jira.sharing.search;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.sharing.type.ShareType;
import com.atlassian.jira.util.dbc.Assertions;

/**
 * Object that represents a set of parameters when searching for all the SharePermissions of a particular ShareType.
 * 
 * @since v3.13
 */
@Internal
public abstract class AbstractShareTypeSearchParameter implements ShareTypeSearchParameter
{
    private final ShareType.Name type;

    protected AbstractShareTypeSearchParameter(final ShareType.Name type)
    {
        Assertions.notNull("type", type);
        this.type = type;
    }

    public ShareType.Name getType()
    {
        return type;
    }
}
