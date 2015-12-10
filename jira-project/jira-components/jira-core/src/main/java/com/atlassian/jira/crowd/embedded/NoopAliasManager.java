package com.atlassian.jira.crowd.embedded;

import com.atlassian.crowd.manager.application.AliasAlreadyInUseException;
import com.atlassian.crowd.manager.application.AliasManager;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.search.query.entity.EntityQuery;

import java.util.List;

/**
 * We don't provide any of this functionality.  Just need to provide this guy because
 * Crowd Rest Plugin needs an implementation.
 *
 * @since v6.0
 */
public class NoopAliasManager implements AliasManager
{
    @Override
    public String findUsernameByAlias(Application application, String authenticatingUsername)
    {
        return null;
    }

    @Override
    public String findAliasByUsername(Application application, String username)
    {
        return null;
    }

    @Override
    public List<String> search(EntityQuery entityQuery)
    {
        return null;
    }

    @Override
    public void storeAlias(Application application, String username, String alias) throws AliasAlreadyInUseException
    {
    }

    @Override
    public void removeAlias(Application application, String username) throws AliasAlreadyInUseException
    {
    }
}
