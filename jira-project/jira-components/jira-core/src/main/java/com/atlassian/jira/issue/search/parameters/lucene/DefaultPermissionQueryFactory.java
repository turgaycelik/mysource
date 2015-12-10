package com.atlassian.jira.issue.search.parameters.lucene;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.security.IssueSecurityLevelPermission;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.permission.PermissionTypeManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.security.JiraAuthenticationContextImpl;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.RequestCacheKeys;
import com.atlassian.jira.security.SecurityTypeManager;
import com.atlassian.jira.security.type.SecurityType;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DefaultPermissionQueryFactory implements PermissionQueryFactory
{
    private static final Logger log = Logger.getLogger(DefaultPermissionQueryFactory.class);

    private final IssueSecurityLevelManager issueSecurityLevelManager;
    private final PermissionManager permissionManager;
    private final PermissionSchemeManager permissionSchemeManager;
    private final PermissionTypeManager permissionTypeManager;
    private final IssueSecuritySchemeManager issueSecuritySchemeManager;
    private final SecurityTypeManager issueSecurityTypeManager;

    public DefaultPermissionQueryFactory(final IssueSecurityLevelManager issueSecurityLevelManager, final PermissionManager permissionManager, final PermissionSchemeManager permissionSchemeManager, final PermissionTypeManager permissionTypeManager, final IssueSecuritySchemeManager issueSecuritySchemeManager, final SecurityTypeManager issueSecurityTypeManager, final ProjectFactory projectFactory)
    {
        this.issueSecurityLevelManager = issueSecurityLevelManager;
        this.permissionManager = permissionManager;
        this.permissionSchemeManager = permissionSchemeManager;
        this.permissionTypeManager = permissionTypeManager;
        this.issueSecuritySchemeManager = issueSecuritySchemeManager;
        this.issueSecurityTypeManager = issueSecurityTypeManager;
    }

    public Query getQuery(final ApplicationUser searcher, final int permissionId)
    {
        try
        {
            final BooleanQuery query = new BooleanQuery();

            // This function loop around all the security types in the current scheme or schemes
            final Collection<Project> projects = permissionManager.getProjects(permissionId, searcher);

            // collect unique project queries
            final Set<Query> projectQueries = new LinkedHashSet<Query>();
            for (final Project project : projects)
            {
                collectProjectTerms(project, searcher, projectQueries, permissionId);
            }

            // add them to the permission query
            final BooleanQuery permissionQuery = new BooleanQuery();
            for (final Query projectQuery : projectQueries)
            {
                permissionQuery.add(projectQuery, BooleanClause.Occur.SHOULD);
            }

            // If you have a project query then add it and look for issue level queries
            if (!permissionQuery.clauses().isEmpty())
            {
                query.add(permissionQuery, BooleanClause.Occur.MUST);

                // collect unique issue level security queries
                final Set<Query> issueLevelSecurityQueries = new LinkedHashSet<Query>();
                issueLevelSecurityQueries.add(new TermQuery(new Term(SystemSearchConstants.forSecurityLevel().getIndexField(), "-1")));

                try
                {
                    //Also loop through the project and return the security levels this user has access
                    for (final Project project : projects)
                    {
                        collectSecurityLevelTerms(project, searcher, issueLevelSecurityQueries);
                    }
                }
                catch (final GenericEntityException e)
                {
                    log.error("Error occurred retrieving security levels for this user");
                }

                final BooleanQuery issueLevelQuery = new BooleanQuery();
                for (final Query issueLevelSecurityQuery : issueLevelSecurityQueries)
                {
                    issueLevelQuery.add(issueLevelSecurityQuery, BooleanClause.Occur.SHOULD);
                }

                query.add(issueLevelQuery, BooleanClause.Occur.MUST);
            }

            return query;
        }
        catch (final GenericEntityException e)
        {
            log.error("Error constructing query: " + e, e);
            return null;
        }
    }

    ///CLOVER:OFF
    PermissionsFilterCache getCache()
    {
        PermissionsFilterCache cache = (PermissionsFilterCache) JiraAuthenticationContextImpl.getRequestCache().get(
            RequestCacheKeys.PERMISSIONS_FILTER_CACHE);

        if (cache == null)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Creating new PermissionsFilterCache");
            }
            cache = new PermissionsFilterCache();
            JiraAuthenticationContextImpl.getRequestCache().put(RequestCacheKeys.PERMISSIONS_FILTER_CACHE, cache);
        }

        return cache;
    }

    ///CLOVER:ON

    /**
     * Loops around the permission schemes for the current project and adds a query for the SecurityType if there is one
     * in scheme.
     *
     * @param project The project for which we need to construct the query
     * @param searcher The user conducting the search
     * @param queries The collection of queries already generated for projects
     * @throws org.ofbiz.core.entity.GenericEntityException If there's a problem retrieving permissions.
     */
    void collectProjectTerms(final Project project, final ApplicationUser searcher, final Set<Query> queries, final int permissionId) throws GenericEntityException
    {
        final User searcherUser = ApplicationUsers.toDirectoryUser(searcher);
        final List<GenericValue> schemes = permissionSchemeManager.getSchemes(project.getGenericValue());
        for (final GenericValue scheme : schemes)
        {
            final List<GenericValue> entities = permissionSchemeManager.getEntities(scheme, (long) permissionId);
            for (final GenericValue entity : entities)
            {
                final SecurityType securityType = permissionTypeManager.getSecurityType(entity.getString("type"));
                if (securityType != null)
                {
                    try
                    {
                        if (userHasPermissionForProjectAndSecurityType(searcher, project, entity.getString("parameter"), securityType))
                        {
                            final Query tempQuery = securityType.getQuery(searcherUser, project, entity.getString("parameter"));
                            if (tempQuery != null)
                            {
                                queries.add(tempQuery);
                            }
                        }
                    }
                    catch (final Exception e)
                    {
                        log.debug("Could not add query for security type:" + securityType.getDisplayName(), e);
                    }
                }
                else
                {
                    log.debug("Could not find security type:" + entity.getString("type"));
                }
            }
        }
    }

    /**
     * Loop through the user security levels for project adding them to the query if they exists
     *
     * @param project The project for which we are constructing a query for the security levels
     * @param queries The collection of queries already generated for security levels
     * @param searcher The user conducting the search
     * @throws org.ofbiz.core.entity.GenericEntityException If there's a problem retrieving security levels.
     */
    void collectSecurityLevelTerms(final Project project, final ApplicationUser searcher, final Set<Query> queries) throws GenericEntityException
    {
        final User searcherUser = ApplicationUsers.toDirectoryUser(searcher);
        final List<IssueSecurityLevel> usersSecurityLevels = issueSecurityLevelManager.getUsersSecurityLevels(project, searcherUser);
        for (final IssueSecurityLevel securityLevel : usersSecurityLevels)
        {
            @SuppressWarnings("unchecked")
            final List<IssueSecurityLevelPermission> securities = issueSecuritySchemeManager.getPermissionsBySecurityLevel(securityLevel.getId());
            for (final IssueSecurityLevelPermission securityLevelPermission : securities)
            {
                final SecurityType securityType = issueSecurityTypeManager.getSecurityType(securityLevelPermission.getType());
                if (securityType != null)
                {
                    if (userHasPermissionForProjectAndSecurityType(searcher, project, securityLevelPermission.getParameter(), securityType))
                    {
                        final Query tempQuery = securityType.getQuery(searcherUser, project, securityLevel, securityLevelPermission.getParameter());
                        if (tempQuery != null)
                        {
                            queries.add(tempQuery);
                        }
                    }
                }
            }
        }
    }

    /**
     * Tests if the specified user has permission for the specified security type in the specified project given the
     * context of the permission scheme entity.
     *
     * @param searcher the user; may be null if user is anonymous
     * @param project the project
     * @param parameter the permission parameter (group name etc)
     * @param securityType the security type
     * @return true if the user has permission; false otherwise
     */
    boolean userHasPermissionForProjectAndSecurityType(final ApplicationUser searcher, final Project project, final String parameter, final SecurityType securityType)
    {
        boolean hasPermission;
        if (searcher == null)
        {
            hasPermission = securityType.hasPermission(project, parameter);
        }
        else
        {
            hasPermission = securityType.hasPermission(project, parameter, searcher.getDirectoryUser(), false);
        }
        return hasPermission;
    }
}
