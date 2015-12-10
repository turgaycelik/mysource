package com.atlassian.jira.jql.resolver;

import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.util.collect.CollectionBuilder;

import java.util.Collections;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Index resolver that can find the index values for versions.
 *
 * @since v4.0
 */
public class VersionIndexInfoResolver implements IndexInfoResolver<Version>
{
    private final NameResolver<Version> versionResolver;

    public VersionIndexInfoResolver(NameResolver<Version> versionResolver)
    {
        this.versionResolver = versionResolver;
    }

    public List<String> getIndexedValues(final String rawValue)
    {
        notNull("rawValue", rawValue);
        List<String> versions = versionResolver.getIdsFromName(rawValue);
        if (versions.isEmpty())
        {
            final Long versionId = getValueAsLong(rawValue);
            if (versionId != null && versionResolver.idExists(versionId))
            {
                versions = Collections.singletonList(rawValue);
            }
        }
        return versions;
    }

    public List<String> getIndexedValues(final Long rawValue)
    {
        notNull("rawValue", rawValue);
        if (versionResolver.idExists(rawValue))
        {
            return CollectionBuilder.newBuilder(rawValue.toString()).asList();
        }
        else
        {
            return versionResolver.getIdsFromName(rawValue.toString());
        }
    }

    public String getIndexedValue(final Version version)
    {
        notNull("version", version);
        return getIdAsString(version);
    }

    private String getIdAsString(final Version version)
    {
        return version.getId().toString();
    }

    private Long getValueAsLong(final String value)
    {
        try
        {
            return new Long(value);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }
}