package com.atlassian.jira.jql.resolver;

import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.CollectionUtil;

import java.util.Collection;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Resolves Version objects and ids from their names.
 *
 * @since v4.0
 */
public class VersionResolver implements NameResolver<Version>
{
    private final VersionManager versionManager;

    public VersionResolver(final VersionManager versionManager)
    {
        this.versionManager = versionManager;
    }

    public List<String> getIdsFromName(final String name)
    {
        notNull("name", name);
        Collection<Version> versions = versionManager.getVersionsByName(name);

        Function<Version, String> function = new Function<Version, String>()
        {
            public String get(final Version input)
            {
                return input.getId().toString();
            }
        };

        return CollectionUtil.transform(versions, function);
    }

    public boolean nameExists(final String name)
    {
        notNull("name", name);
        Collection<Version> versions = versionManager.getVersionsByName(name);
        return !versions.isEmpty();
    }

    public boolean idExists(final Long id)
    {
        notNull("id", id);
        return versionManager.getVersion(id) != null;
    }

    public Version get(final Long id)
    {
        return versionManager.getVersion(id);
    }

    ///CLOVER:OFF
    public Collection<Version> getAll()
    {
        return versionManager.getAllVersions();
    }
    ///CLOVER:ON
}
