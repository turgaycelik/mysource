package com.atlassian.jira.plugin.projectpanel.impl;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comparator.NullResolutionComparator;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.plugin.componentpanel.BrowseComponentContext;
import com.atlassian.jira.plugin.util.TabPanelUtil;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.bean.PercentageGraphModel;
import com.atlassian.jira.web.bean.StatisticAccessorBean;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.order.SortOrder;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.exception.VelocityException;
import webwork.action.ActionContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.atlassian.jira.template.TemplateSources.file;

/**
 * Renders a roadmap (or changelog) for a given context. This class is expected to run under a web request.  It uses the
 * user's session and queries the web request for passed in parameters.
 *
 * @since v4.0
 */
public class VersionDrillDownRendererImpl implements VersionDrillDownRenderer
{
    private static final Logger log = Logger.getLogger(VersionDrillDownRendererImpl.class);

    /**
     * The number of versions to display on a single screen
     */
    public static final int SUBSET_SIZE = 10;

    /**
     * The web parameter the specifies whether or not to restrict content to only the changed parts.
     */
    private static final String CONTENT_ONLY = "contentOnly";

    public static final String EXPAND_VERSION = "expandVersion";
    public static final String COLLAPSE_VERSION = "collapseVersion";
    public static final String ALL_VERSIONS = "allVersions";

    private final JiraAuthenticationContext authenticationContext;
    private final VersionManager versionManager;
    private final SearchProvider searchProvider;
    private final VelocityTemplatingEngine templatingEngine;
    private final SearchService searchService;
    private final ConstantsManager constantsManager;
    private final FieldVisibilityManager fieldVisibilityManager;
    private final VelocityRequestContextFactory velocityRequestContextFactory;

    public VersionDrillDownRendererImpl(final JiraAuthenticationContext authenticationContext,
            final VersionManager versionManager, final ConstantsManager constantsManager,
            final SearchProvider searchProvider, final VelocityTemplatingEngine templatingEngine,
            final SearchService searchService, final FieldVisibilityManager fieldVisibilityManager,
            final VelocityRequestContextFactory velocityRequestContextFactory)
    {
        this.authenticationContext = authenticationContext;
        this.versionManager = versionManager;
        this.constantsManager = constantsManager;
        this.searchProvider = searchProvider;
        this.templatingEngine = templatingEngine;
        this.searchService = searchService;
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
    }


    /**
     * Retrieves the html for a non-personal roadmap/changelog
     *
     * @param ctx The context under which this is running
     * @param uniqueKey a key that is unique ot this roadmap/changelog
     * @param versions the list of versions for this context
     * @return The escaped html of the roadmap
     */
    @Override
    public String getHtml(final BrowseContext ctx, final String uniqueKey, final Collection<Version> versions)
    {
        final Map<String, Object> params = createVelocityParams(ctx, uniqueKey, versions);
        try
        {
            return templatingEngine.render(file("templates/plugins/jira/projectpanels/roadmap-panel.vm")).applying(params).asHtml();
        }
        catch (VelocityException e)
        {
            log.error("Error occurred while rendering velocity template: 'templates/plugins/jira/projectpanels/roadmap-panel.vm'.", e);
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> createVelocityParams(final BrowseContext ctx, final String uniqueKey, final Collection<Version> versions)
    {
        Map<String, Object> startingParams = ctx.createParameterMap();

        startingParams = JiraVelocityUtils.getDefaultVelocityParams(startingParams, authenticationContext);

        final boolean isExpanding = getPassedParameter(EXPAND_VERSION) != null;
        final boolean isCollapsing = getPassedParameter(COLLAPSE_VERSION) != null;
        final boolean isContentOnly = Boolean.valueOf(getPassedParameter(CONTENT_ONLY));

        startingParams.put("renderer", this);
        startingParams.put("reportKey", uniqueKey);
        startingParams.put("versionHelper", new VersionHelperBean(ctx, searchProvider));
        startingParams.put("versionManager", versionManager);
        startingParams.put("isExpanding", isExpanding);
        startingParams.put("isCollapsing", isCollapsing);
        startingParams.put("isContentOnly", isContentOnly);
        startingParams.put("selectedVersions", getSelectedVersions(ctx, uniqueKey));
        final boolean showAllVersions = showAllVersions(ctx, uniqueKey);

        startingParams.put("versions", trimVersions(versions, showAllVersions));
        if (isContentOnly && isExpanding)
        {
            final Version expandingVersion = versionManager.getVersion(Long.valueOf(getPassedParameter(EXPAND_VERSION)));
            //only expand if it is in the list of potentials.
            if (versions.contains(expandingVersion))
            {
                startingParams.put("expandingVersion", expandingVersion);
            }
        }

        startingParams.put("graphingBean", new RoadMapGraphingBean(ctx, searchProvider, constantsManager, searchService));

        startingParams.put("versionSubsetSize", SUBSET_SIZE);
        startingParams.put("showAllVersions", showAllVersions);
        startingParams.put("isBigList", versions.size() > SUBSET_SIZE);
        startingParams.put("i18n", new I18nBean(ctx.getUser()));
        startingParams.put("fieldVisibility", fieldVisibilityManager);

        return startingParams;
    }

    @Override
    public String getNavigatorUrl(final Project project, final Version version, final ProjectComponent component)
    {
        final JqlClauseBuilder builder = JqlQueryBuilder.newClauseBuilder().defaultAnd();
        if (project != null)
        {
            builder.project(project.getKey());
        }
        if (version != null)
        {
            builder.fixVersion(version.getName());
        }
        if (component != null)
        {
            builder.component(component.getName());
        }
        return searchService.getQueryString(authenticationContext.getLoggedInUser(), builder.buildQuery());
    }

    /**
     * Creates only a subset of versions depending on whether they want to see all, or only the first {@link
     * #SUBSET_SIZE}.
     *
     * @param versions a complete list of versions.
     * @param showAllVersions Whether or not to only display the first {@link #SUBSET_SIZE} versions.
     * @return The first {@link #SUBSET_SIZE} versions.
     */
    private Collection<Version> trimVersions(Collection<Version> versions, final boolean showAllVersions)
    {
        if (!showAllVersions && versions.size() > SUBSET_SIZE)
        {
            List<Version> versionList = new ArrayList<Version>(versions);
            versions = versionList.subList(0, SUBSET_SIZE);
        }
        return versions;
    }

    /**
     * Get a collections of Long that represents what versions have been selected per context basis.
     *
     * @param ctx the context under which this is running.
     * @param uniqueKey a key that is unique to this roadmap/changelog.
     * @return a list of version ids representing which versions should be expanded.
     */
    private Collection<Long> getSelectedVersions(final BrowseContext ctx, final String uniqueKey)
    {
        final Map<String, Object> session = getSession();

        final String key = uniqueKey + "_selected_" + ctx.getContextKey();

        Set<Long> versions = (Set<Long>) session.get(key);
        if (versions == null)
        {
            versions = new HashSet<Long>();
            session.put(key, versions);
        }

        String requestParameter = getPassedParameter(EXPAND_VERSION);
        if (!StringUtils.isEmpty(requestParameter))
        {
            versions.add(Long.valueOf(requestParameter));
        }

        requestParameter = getPassedParameter(COLLAPSE_VERSION);
        if (!StringUtils.isEmpty(requestParameter))
        {
            versions.remove(Long.valueOf(requestParameter));
        }

        return versions;
    }

    /**
     * Get the specified request parameter.
     *
     * @param parameterName The name of the parameter to retrieve
     * @return The parameter that was passed.
     */
    private String getPassedParameter(final String parameterName)
    {
        final VelocityRequestContext context = velocityRequestContextFactory.getJiraVelocityRequestContext();
        return context.getRequestParameter(parameterName);
    }

    /**
     * Get the user's session.
     *
     * @return The user's session
     */
    private Map<String, Object> getSession()
    {
        return ActionContext.getSession();
    }

    /**
     * Determines whether or not to display all the versions or only the first {@link #SUBSET_SIZE}. This is remember on
     * a per Context basis.
     *
     * @param ctx the context under which this is running.
     * @param uniqueKey a key that is unique to this roadmap / changelog.
     * @return whether or not to display all the versions or only the first {@link #SUBSET_SIZE}.
     */
    private Boolean showAllVersions(final BrowseContext ctx, final String uniqueKey)
    {
        final String key = uniqueKey + "_allVersions_" + ctx.getContextKey();

        String requestParameter = getPassedParameter(ALL_VERSIONS);
        if (!StringUtils.isEmpty(requestParameter))
        {
            final Boolean showAllVersions = Boolean.valueOf(requestParameter);
            getSession().put(key, showAllVersions);
            return showAllVersions;
        }

        final Boolean showAllVersions = (Boolean) getSession().get(key);
        return showAllVersions != null && showAllVersions;
    }

    private static JqlClauseBuilder addFixForClause(JqlClauseBuilder builder, final Version version)
    {
        return builder.fixVersion().eq(version.getId());
    }

    /**
     * A helper that performs lucene searches.
     */
    public static class VersionHelperBean
    {
        private final BrowseContext ctx;
        private final SearchProvider searchProvider;

        public VersionHelperBean(final BrowseContext ctx, final SearchProvider searchProvider)
        {
            this.ctx = ctx;
            this.searchProvider = searchProvider;
        }

        /**
         * Retrieves a {@link com.atlassian.jira.issue.search.SearchResults} for all the issues in the current context
         * and restricts them to assigned to current user if it is personal.
         *
         * @param version The version to get the results for
         * @return a results object contains the issues and the total count
         * @throws SearchException if there is an exception thrown during searching
         */
        public SearchResults getSearchResultByFixForVersion(final Version version) throws SearchException
        {
            final Query initialQuery = ctx.createQuery();

            JqlQueryBuilder queryBuilder = JqlQueryBuilder.newBuilder(initialQuery);
            final JqlClauseBuilder whereBuilder = queryBuilder.where().defaultAnd();

            addFixForClause(whereBuilder, version);
            queryBuilder.orderBy().clear().priority(SortOrder.DESC).status(SortOrder.ASC).resolution(SortOrder.ASC).summary(SortOrder.ASC);

            return searchProvider.search(queryBuilder.buildQuery(), ctx.getUser(), TabPanelUtil.PAGER_FILTER);
        }

        /**
         * Extracts the list of issues and orders them appropriately.
         *
         * @param searchResults The result object to extract the issues from.
         * @return a collection of issue objects.
         */
        public Collection<Issue> getIssuesFromSearchResult(final SearchResults searchResults)
        {
            final List<Issue> issues = searchResults.getIssues();

            Collections.sort(issues, new NullResolutionComparator());

            return issues;
        }
    }

    /**
     * Helper class to draw progress charts.
     */
    public static class RoadMapGraphingBean
    {
        private final BrowseContext ctx;
        private final SearchProvider searchProvider;
        private final ConstantsManager constantsManager;
        private final SearchService searchService;
        private final StatisticAccessorBean statsBean;

        public RoadMapGraphingBean(final BrowseContext ctx, final SearchProvider searchProvider,
                final ConstantsManager constantsManager, final SearchService searchService)
        {
            this.ctx = ctx;
            this.searchProvider = searchProvider;
            this.constantsManager = constantsManager;
            this.searchService = searchService;
            statsBean = createStatsBean();

        }

        /**
         * Creates a StatsBean for the given context and restricts it to assigned to current user if it is personal.
         *
         * @return A StatsBean that you can extract relevant stats for.
         */
        private StatisticAccessorBean createStatsBean()
        {
            JqlClauseBuilder builder = JqlQueryBuilder.newBuilder().where().defaultAnd();
            if (ctx instanceof BrowseComponentContext)
            {
                builder.component(((BrowseComponentContext) ctx).getComponent().getId());
            }
            return new StatisticAccessorBean(ctx.getUser(), ctx.getProject().getId(), builder.buildClause(), true);
        }

        /**
         * Get a count of issues for a given fix for version under the current context.  Restricts it to assigned to
         * current user if it is personal.
         *
         * @param version The fix for version to get the issue count for.
         * @return the number of issues in this version under this context.
         * @throws SearchException if an exception is thrown while searching
         */
        public int getIssueCountByFixForVersion(final Version version) throws SearchException
        {
            final Query initialQuery = ctx.createQuery();

            final JqlClauseBuilder builder = JqlQueryBuilder.newClauseBuilder(initialQuery).defaultAnd();
            addFixForClause(builder, version);
            return (int) searchProvider.searchCount(builder.buildQuery(), ctx.getUser());
        }

        /**
         * Get a count of open issues for a given fix for version under the current context.  Restricts it to assigned to
         * current user if it is personal.
         *
         * @param version The fix for version to get the open issue count for.
         * @return the number of open issues in this version under this context.
         * @throws Exception if an exception is thrown while searching
         */
        public int getIssueCountOpenByFixForVersion(final Version version) throws Exception
        {
            return (int) statsBean.getOpenByFixFor(version);
        }

        /**
         * Gets a progress graph model for the current version under the current context.  Though this does not take a version,
         * the underlying macro that generates the percentage chart takes a version.
         *
         * @param openCount The number of open issues for this version under this context
         * @param allCount  The total issues under this context
         * @return a graph model representing the percentage of issues that are resolved.
         */
        public PercentageGraphModel getIssueSummaryByFixForVersion(final Version version,
                final ProjectComponent component, final int openCount, final int allCount)
        {
            if (allCount == 0)
            {
                return new PercentageGraphModel();
            }

            final I18nBean i18nBean = new I18nBean(ctx.getUser());
            PercentageGraphModel model = new PercentageGraphModel();
            model.addRow("#009900", allCount - openCount, i18nBean.getText("common.concepts.resolved.issues"), getNavigatorUrl(ctx.getProject(), version, component, false));
            model.addRow("#cc0000", openCount, i18nBean.getText("common.concepts.unresolved.issues"), getNavigatorUrl(ctx.getProject(), version, component, true));

            return model;
        }

        public String getNavigatorUrl(final Project project, final Version version, final ProjectComponent component, final boolean unresolved)
        {
            final JqlClauseBuilder builder = JqlQueryBuilder.newClauseBuilder().defaultAnd();
            if (project != null)
            {
                builder.project(project.getKey());
            }
            if (version != null)
            {
                builder.fixVersion(version.getName());
            }
            if (component != null)
            {
                builder.component(component.getName());
            }
            if (!unresolved)
            {
                List<String> resolutions = new ArrayList<String>();
                for (Resolution resolution : constantsManager.getResolutionObjects())
                {
                    resolutions.add(resolution.getName());
                }
                if (!resolutions.isEmpty())
                {
                    builder.resolution().in(resolutions.toArray(new String[resolutions.size()])).buildClause();
                }
            }
            else
            {
                builder.unresolved();
            }
            return searchService.getQueryString(ctx.getUser(), builder.buildQuery());
        }

        /**
         * The query string for resolved issues.
         *
         * @return The query string for resolved issues.
         */
        public String getResolvedQueryString()
        {
            List<Long> resolutions = new ArrayList<Long>();
            for (Resolution resolution : constantsManager.getResolutionObjects())
            {
                resolutions.add(new Long(resolution.getId()));
            }
            if (!resolutions.isEmpty())
            {
                final Clause resolutionClause = JqlQueryBuilder.newClauseBuilder().resolution().inNumbers(resolutions).buildClause();
                return getQuerySnipper(resolutionClause);
            }
            return "";
        }

        /**
         * The query string for unresolved issues.
         *
         * @return The query string for unresolved issues.
         */
        public String getUnresolvedQueryString()
        {
            final Clause resolutionClause = JqlQueryBuilder.newClauseBuilder().unresolved().buildClause();
            return getQuerySnipper(resolutionClause);
        }

        private String getQuerySnipper(final Clause clause)
        {
            final Query query = new QueryImpl(clause);
            return searchService.getQueryString(ctx.getUser(), query);
        }
    }
}
