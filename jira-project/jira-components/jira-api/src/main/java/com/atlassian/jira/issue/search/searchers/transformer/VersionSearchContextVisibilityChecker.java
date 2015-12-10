package com.atlassian.jira.issue.search.searchers.transformer;

import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A {@link com.atlassian.jira.issue.search.searchers.transformer.SearchContextVisibilityChecker} for version fields
 *
 * @since v4.0
 * @deprecated
 */
@Deprecated
public class VersionSearchContextVisibilityChecker implements SearchContextVisibilityChecker
{
    private final VersionManager versionManager;

    public VersionSearchContextVisibilityChecker(VersionManager versionManager)
    {
        this.versionManager = versionManager;
    }

    public Set<String> FilterOutNonVisibleInContext(final SearchContext searchContext, final Collection<String> ids)
    {
        final List<Long> projects = searchContext.getProjectIds();
        if (projects.size() != 1)
        {
            return Collections.emptySet();
        }
        Set<String> visibleIds = new HashSet<String>();
        for (String sid : ids)
        {
            Long lid = parseLong(sid);
            if (lid != null)
            {
                final Version version = versionManager.getVersion(lid);
                if (version != null)
                {
                    Long pid = version.getProjectObject().getId();
                    if (projects.contains(pid))
                    {
                        visibleIds.add(sid);
                    }
                }
            }
        }
        return visibleIds;
    }

    private Long parseLong(String str)
    {
        try
        {
            return Long.valueOf(str);
        }
        catch (NumberFormatException ignored)
        {
            return null;
        }
    }
}
