package com.atlassian.jira.jql.query;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.SearchProviderFactory;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.filters.IssueIdFilter;
import com.atlassian.jira.issue.search.parameters.lucene.CachedWrappedFilterCache;
import com.atlassian.jira.issue.search.parameters.lucene.PermissionsFilterGenerator;
import com.atlassian.jira.issue.search.util.LuceneQueryModifier;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContextImpl;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.RequestCacheKeys;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operator.Operator;
import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.CachingWrapperFilter;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.OpenBitSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Generates a query for the comment system field.
 *
 * @since v4.0
 */
public class CommentClauseQueryFactory implements ClauseQueryFactory
{
    private static final Logger log = Logger.getLogger(CommentClauseQueryFactory.class);

    private final PermissionManager permissionManager;
    private final ProjectRoleManager projectRoleManager;
    private final SearchProviderFactory searchProviderFactory;
    private final LuceneQueryModifier luceneQueryModifier;
    private final PermissionsFilterGenerator permissionsFilterGenerator;
    private final ClauseQueryFactory delegateClauseQueryFactory;


    public CommentClauseQueryFactory(final PermissionManager permissionManager, final ProjectRoleManager projectRoleManager,
            final JqlOperandResolver operandResolver, final SearchProviderFactory searchProviderFactory, final LuceneQueryModifier luceneQueryModifier,
            final PermissionsFilterGenerator permissionsFilterGenerator)
    {
        this.permissionManager = permissionManager;
        this.projectRoleManager = projectRoleManager;
        this.searchProviderFactory = searchProviderFactory;
        this.luceneQueryModifier = luceneQueryModifier;
        this.permissionsFilterGenerator = permissionsFilterGenerator;
        this.delegateClauseQueryFactory = getDelegate(operandResolver);
    }

    public QueryFactoryResult getQuery(final QueryCreationContext queryCreationContext, final TerminalClause terminalClause)
    {
        notNull("queryCreationContext", queryCreationContext);
        if (!isClauseValid(terminalClause))
        {
            return QueryFactoryResult.createFalseResult();
        }

        final boolean overrideSecurity = queryCreationContext.isSecurityOverriden();

        List<Long> projectIds = null;
        if (!overrideSecurity)
        {
            projectIds = getVisibleProjectIds(queryCreationContext.getQueryUser());
            // if we cannot see any projects, then we might as well quit now
            if (projectIds.isEmpty())
            {
                return QueryFactoryResult.createFalseResult();
            }
        }

        BooleanQuery levelQuery = null;
        BooleanQuery projectVisibilityQuery = null;
        // only create the restriction queries if we are not overriding security
        if (!overrideSecurity)
        {
            levelQuery = createLevelRestrictionQueryForComments(projectIds, queryCreationContext.getQueryUser());
            projectVisibilityQuery = createProjectVisibilityQuery(projectIds);
        }

        // note: the delegate query will take care of the negation required if the operator is NOT LIKE or if the terms
        // specified are negating.
        final Query commentBodyQuery = luceneQueryModifier.getModifiedQuery(delegateClauseQueryFactory.getQuery(queryCreationContext, terminalClause).getLuceneQuery());

        final BooleanQuery commentIndexQuery = new BooleanQuery();
        if (levelQuery != null)
        {
            commentIndexQuery.add(levelQuery, BooleanClause.Occur.MUST);
        }
        commentIndexQuery.add(commentBodyQuery, BooleanClause.Occur.MUST);
        if (projectVisibilityQuery != null)
        {
            commentIndexQuery.add(projectVisibilityQuery, BooleanClause.Occur.MUST);
        }

        return generateIssueIdQueryFromCommentQuery(commentIndexQuery, queryCreationContext);
    }

    boolean isClauseValid(final TerminalClause terminalClause)
    {
        final Operator operator = terminalClause.getOperator();
        if (!Operator.LIKE.equals(operator) && !Operator.NOT_LIKE.equals(operator))
        {
            log.debug("Can not generate a comment clause query for a clause with operator '" + operator.getDisplayString() + "'.");
            return false;
        }
        return true;
    }

    // Not testing this method because lucene Hits are a final class that are a pain to mock out
    ///CLOVER:OFF
    QueryFactoryResult generateIssueIdQueryFromCommentQuery(final Query commentIndexQuery, final QueryCreationContext creationContext)
    {
        try
        {
            if (log.isDebugEnabled())
            {
                log.debug("Searching the comment index using the query: " + commentIndexQuery.toString());
            }

            // Now we need to execute the search and build up a new query that enumerates the issue id's found by the comment search
            final IndexSearcher commentSearcher = searchProviderFactory.getSearcher(SearchProviderFactory.COMMENT_INDEX);

            final IssueIdCollector collector = new IssueIdCollector(commentSearcher.getIndexReader());

            commentSearcher.search(commentIndexQuery, collector);
            final Set<String> issueIds = collector.getIssueIds();

            return new QueryFactoryResult(new ConstantScoreQuery(new IssueIdFilter(issueIds)));
        }
        catch (final IOException e)
        {
            log.error("Unable to search the comment index.", e);
            return QueryFactoryResult.createFalseResult();
        }
    }

    ///CLOVER:ON

    TermQuery getTermQuery(final String documentConstant, final String value)
    {
        return new TermQuery(new Term(documentConstant, value));
    }

    ClauseQueryFactory getDelegate(final JqlOperandResolver jqlOperandResolver)
    {
        final List<OperatorSpecificQueryFactory> operatorFactories = new ArrayList<OperatorSpecificQueryFactory>();
        operatorFactories.add(new LikeQueryFactory(false));
        return new GenericClauseQueryFactory(DocumentConstants.COMMENT_BODY, operatorFactories, jqlOperandResolver);
    }

    BooleanQuery createProjectVisibilityQuery(final List<Long> projectIds)
    {
        if (projectIds.isEmpty())
        {
            return null;
        }

        final BooleanQuery visibilityQuery = new BooleanQuery();
        for (final Long projectId : projectIds)
        {
            visibilityQuery.add(getTermQuery(SystemSearchConstants.forProject().getIndexField(), projectId.toString()), BooleanClause.Occur.SHOULD);
        }
        return visibilityQuery;
    }

    /**
     * Creates a lucene query that will restrict the results to only the comments
     * that are visible to the user based on comments' group and role levels for the passed projects.
     *
     * @param projectIds the ids of the projects that we are interested in.
     * @param searcher   user running this search
     * @return lucene query, never null
     */
    BooleanQuery createLevelRestrictionQueryForComments(final List<Long> projectIds, final User searcher)
    {
        final BooleanQuery levelQuery = new BooleanQuery();

        // always add the no group level and no project role level
        final Query noGroupOrLevelConstraints = createNoGroupOrProjectRoleLevelQuery();
        levelQuery.add(noGroupOrLevelConstraints, BooleanClause.Occur.SHOULD);

        // Only create level restriction queries if the user is not null (i.e. not Anonymous/not-logged-in user)
        if (searcher != null)
        {
            // add group level restriction
            final Set<String> groups = getGroups(searcher);
            if (!groups.isEmpty())
            {
                levelQuery.add(createGroupLevelQuery(groups), BooleanClause.Occur.SHOULD);
            }

            // add project role level restriction
            final ProjectRoleManager.ProjectIdToProjectRoleIdsMap projectIdToProjectRolesMap = projectRoleManager.createProjectIdToProjectRolesMap(
                searcher, projectIds);
            if (!projectIdToProjectRolesMap.isEmpty())
            {
                final Query query = createProjectRoleLevelQuery(projectIdToProjectRolesMap);
                levelQuery.add(query, BooleanClause.Occur.SHOULD);
            }
        }
        return levelQuery;
    }

    ///CLOVER:OFF
    Set<String> getGroups(final User searcher)
    {
        UserUtil userUtil = ComponentAccessor.getComponent(UserUtil.class);
        return userUtil.getGroupNamesForUser(searcher.getName());
    }

    ///CLOVER:ON

    Query createGroupLevelQuery(final Set<String> groups)
    {
        final BooleanQuery query = new BooleanQuery();
        if ((groups == null) || groups.isEmpty())
        {
            log.debug("Groups must be specified!");
            return query;
        }

        for (final String group : groups)
        {
            query.add(getTermQuery(DocumentConstants.COMMENT_LEVEL, group), BooleanClause.Occur.SHOULD);
        }
        return query;
    }

    /**
     * Creates new query with the restriction of comment group level set to -1
     * (no group level) AND comment project role level set to -1 (no project
     * role level).
     *
     * @return query
     */
    Query createNoGroupOrProjectRoleLevelQuery()
    {
        final BooleanQuery query = new BooleanQuery();
        query.add(getTermQuery(DocumentConstants.COMMENT_LEVEL, "-1"), BooleanClause.Occur.MUST);
        query.add(getTermQuery(DocumentConstants.COMMENT_LEVEL_ROLE, "-1"), BooleanClause.Occur.MUST);
        return query;
    }

    /**
     * Creates a project role level based restriction query.
     * <p/>
     * NOTE: This method should not be called with null or empty map (i.e if
     * the user is not a member of any roles). If the user is not a member
     * of any roles, the right thing to do is not to call this method.
     *
     * @param projectIdToProjectRolesMap Map[Long,Collection[ProjectRole]]
     * @return query
     */
    Query createProjectRoleLevelQuery(final ProjectRoleManager.ProjectIdToProjectRoleIdsMap projectIdToProjectRolesMap)
    {
        final BooleanQuery query = new BooleanQuery();
        if ((projectIdToProjectRolesMap == null) || projectIdToProjectRolesMap.isEmpty())
        {
            log.debug("Groups must be specified!");
            return query;
        }

        for (final ProjectRoleManager.ProjectIdToProjectRoleIdsMap.Entry entry : projectIdToProjectRolesMap)
        {
            final Long projectId = entry.getProjectId();
            final Collection<Long> projectRoles = entry.getProjectRoleIds();
            for (final Long projectRoleId : projectRoles)
            {
                query.add(createCommentInProjectAndUserInRoleQuery(projectId, projectRoleId), BooleanClause.Occur.SHOULD);
            }
        }
        return query;
    }

    /**
     * Creates a new query that sets the project id must be equal to the given value and the project role visibility
     * of the comment must be equal to the given value.
     *
     * @param projectId     project ID
     * @param projectRoleId project role ID
     * @return new query
     */
    Query createCommentInProjectAndUserInRoleQuery(final Long projectId, final Long projectRoleId)
    {
        final BooleanQuery query = new BooleanQuery();
        if (projectId == null)
        {
            log.debug("projectId must be specified!");
            return query;
        }
        if (projectRoleId == null)
        {
            log.debug("projectRoleId must be specified!");
            return query;
        }

        query.add(getTermQuery(SystemSearchConstants.forProject().getIndexField(), projectId.toString()), BooleanClause.Occur.MUST);
        query.add(getTermQuery(DocumentConstants.COMMENT_LEVEL_ROLE, projectRoleId.toString()), BooleanClause.Occur.MUST);
        return query;
    }

    List<Long> getVisibleProjectIds(final User searcher)
    {
        final Collection<Project> projects = permissionManager.getProjectObjects(Permissions.BROWSE, searcher);
        final List<Long> projectIds = new ArrayList<Long>();
        if (projects != null)
        {
            for (final Project project : projects)
            {
                if (project != null)
                {
                    projectIds.add(project.getId());
                }
            }
        }
        return projectIds;
    }

    Filter getPermissionsFilter(final boolean overRideSecurity, final User searchUser)
    {
        if (!overRideSecurity)
        {
            // JRA-14980: first attempt to retrieve the filter from cache
            final CachedWrappedFilterCache cache = getCachedWrappedFilterCache();

            Filter filter = cache.getFilter(searchUser);
            if (filter != null)
            {
                return filter;
            }

            // if not in cache, construct a query (also using a cache)
            final org.apache.lucene.search.Query permissionQuery = permissionsFilterGenerator.getQuery(searchUser);
            filter = new CachingWrapperFilter(new QueryWrapperFilter(permissionQuery));

            // JRA-14980: store the wrapped filter in the cache
            // this is because the CachingWrapperFilter gives us an extra benefit of precalculating its BitSet, and so
            // we should use this for the duration of the request.
            cache.storeFilter(filter, searchUser);

            return filter;
        }
        else
        {
            return null;
        }
    }

    CachedWrappedFilterCache getCachedWrappedFilterCache()
    {
        CachedWrappedFilterCache cache = (CachedWrappedFilterCache) JiraAuthenticationContextImpl.getRequestCache().get(
                RequestCacheKeys.CACHED_WRAPPED_FILTER_CACHE);

        if (cache == null)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Creating new CachedWrappedFilterCache");
            }
            cache = new CachedWrappedFilterCache();
            JiraAuthenticationContextImpl.getRequestCache().put(RequestCacheKeys.CACHED_WRAPPED_FILTER_CACHE, cache);
        }

        return cache;
    }
}
