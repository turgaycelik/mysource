package com.atlassian.jira.jql.resolver;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.Transformed;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @since v4.2
 */
public class ResolverManagerImpl implements ResolverManager
{
    private static final Logger LOG = Logger.getLogger(ResolverManagerImpl.class);

    Map<String, NameResolver> map = new HashMap<String, NameResolver>();

    public ResolverManagerImpl(final UserResolver userResolver, final ProjectResolver projectResolver, final VersionResolver versionResolver, final ComponentResolver componentResolver, final IssueTypeResolver issueTypeResolver, final PriorityResolver priorityResolver, final ResolutionResolver resolutionResolver, final StatusResolver statusResolver, final IssueSecurityLevelResolver issueSecurityLevelResolver, final JiraAuthenticationContext jiraAuthenticationContext)
    {
        map.put(IssueFieldConstants.REPORTER, userResolver);
        map.put(IssueFieldConstants.ASSIGNEE, userResolver);

        map.put(IssueFieldConstants.AFFECTED_VERSIONS, versionResolver);
        map.put(IssueFieldConstants.FIX_FOR_VERSIONS, versionResolver);

        map.put(IssueFieldConstants.PROJECT, projectResolver);
        map.put(IssueFieldConstants.COMPONENTS, componentResolver);
        map.put(IssueFieldConstants.ISSUE_TYPE, issueTypeResolver);
        map.put(IssueFieldConstants.PRIORITY, priorityResolver);
        map.put(IssueFieldConstants.RESOLUTION, resolutionResolver);
        map.put(IssueFieldConstants.STATUS, statusResolver);

        // The IssueSecurityLevelResolver doesn't implement NameResolver so we need to put a facade in front of it that
        // works for our purposes.
        map.put(IssueFieldConstants.SECURITY, new IssueSecurityLevelResolverFacade(issueSecurityLevelResolver, jiraAuthenticationContext));

        // These things aren't NameResolvers but probably should be.
        // ProjectCategoryResolver
        // SavedFilterResolver
    }

    public boolean handles(final String field)
    {
        return map.containsKey(field);
    }

    public List<String> getIdsFromName(final String name, final String field)
    {
        if (!map.containsKey(field))
        {
            throw new IllegalArgumentException("Unexpected Field class for ResolverManager of " + field);
        }

        final NameResolver<?> resolver = map.get(field);
        return resolver.getIdsFromName(name);
    }

    public String getSingleIdFromName(final String name, final String field)
    {
        final List<String> ids = getIdsFromName(name, field);

        if (ids.size() == 0)
        {
            throw new IllegalArgumentException(String.format("There is no id for field %s with name %s", field, name));
        }

        if (ids.size() > 1)
        {
            LOG.debug(String.format("Found more than 1 id during REST name resolution for name %s field %s.", name, field));
        }

        return ids.get(0);
    }

    public static class IssueSecurityLevelResolverFacade implements NameResolver
    {
        private final IssueSecurityLevelResolver resolver;
        private final JiraAuthenticationContext authenticationContext;

        public IssueSecurityLevelResolverFacade(final IssueSecurityLevelResolver resolver, final JiraAuthenticationContext authenticationContext)
        {
            this.resolver = resolver;
            this.authenticationContext = authenticationContext;
        }

        public List<String> getIdsFromName(final String name)
        {
            final Collection<IssueSecurityLevel> levels = resolver.getIssueSecurityLevelsByName(authenticationContext.getLoggedInUser(), false, name);
            return new ArrayList<String>(Transformed.collection(levels, new Function<IssueSecurityLevel, String>()
            {
                public String get(final IssueSecurityLevel issueSecurityLevel)
                {
                    return issueSecurityLevel.getId().toString();
                }
            }));
        }

        public boolean nameExists(final String name)
        {
            return getIdsFromName(name).size() != 0;
        }

        public boolean idExists(final Long id)
        {
            throw new UnsupportedOperationException();
        }

        public Object get(final Long id)
        {
            throw new UnsupportedOperationException();
        }

        public Collection getAll()
        {
            throw new UnsupportedOperationException();
        }
    }
}
