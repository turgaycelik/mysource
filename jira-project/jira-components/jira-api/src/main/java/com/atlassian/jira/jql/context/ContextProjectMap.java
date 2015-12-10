package com.atlassian.jira.jql.context;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * Helper class to simplify the job of combining multiple contexts
 *
 * @since v6.1
 */
public class ContextProjectMap
{
    private static enum CombineType
    {
        INTERSECT, UNION
    }

    final Map<ProjectContext, Set<IssueTypeContext>> issueTypeContextsPerProject = Maps.newHashMap();
    final Set<IssueTypeContext> issueTypeContextsContainedInGlobalProjectContexts = Sets.newHashSet();
    final Set<ProjectContext> projectsWithAllIssueTypes = Sets.newHashSet();
    final boolean containsGlobalContext;

    @Nonnull
    public ContextProjectMap(@Nonnull final ClauseContext context)
    {
        final Set<ProjectIssueTypeContext> projectIssueTypeContexts = context.getContexts();
        containsGlobalContext = context.containsGlobalContext();
        if (projectIssueTypeContexts != null)
        {
            for (ProjectIssueTypeContext projectIssueTypeContext : projectIssueTypeContexts)
            {
                final ProjectContext projectContext = projectIssueTypeContext.getProjectContext();
                final IssueTypeContext issueTypeContext = projectIssueTypeContext.getIssueTypeContext();
                if (projectContext.isAll())
                {
                    if (!issueTypeContext.isAll())
                    {
                        issueTypeContextsContainedInGlobalProjectContexts.add(issueTypeContext);
                    }
                }
                else
                {
                    if (issueTypeContext.isAll())
                    {
                        projectsWithAllIssueTypes.add(projectContext);
                    }
                }
                if (issueTypeContextsPerProject.containsKey(projectContext))
                {
                    issueTypeContextsPerProject.get(projectContext).add(issueTypeContext);
                }
                else
                {
                    Set<IssueTypeContext> issueTypeContexts = Sets.newHashSet(issueTypeContext);
                    issueTypeContextsPerProject.put(projectContext, issueTypeContexts);
                }
            }
        }
    }

    @Nonnull
    public ClauseContext intersect(@Nonnull ContextProjectMap contextProjectMap)
    {
        final Set<ProjectIssueTypeContext> intersection = toProjectIssueTypeContextSet(combineContextMaps(issueTypeContextsPerProject, contextProjectMap.issueTypeContextsPerProject, CombineType.INTERSECT));
        final Set<ProjectIssueTypeContext> explicitProjectIssueTypeContexts = handleGlobals(contextProjectMap);
        intersection.addAll(explicitProjectIssueTypeContexts);
        return new ClauseContextImpl(intersection);
    }

    @Nonnull
    public ClauseContext union(@Nonnull ContextProjectMap contextProjectMap)
    {
        final Set<ProjectIssueTypeContext> union = toProjectIssueTypeContextSet(combineContextMaps(issueTypeContextsPerProject, contextProjectMap.issueTypeContextsPerProject, CombineType.UNION));
        return new ClauseContextImpl(union);
    }

    @Nonnull
    private Map<ProjectContext, Set<IssueTypeContext>> combineContextMaps(@Nonnull final Map<ProjectContext, Set<IssueTypeContext>> map1, @Nonnull final Map<ProjectContext, Set<IssueTypeContext>> map2, CombineType combineType)
    {
        final Set<ProjectContext> projectContexts = Sets.newHashSet(map1.keySet());
        projectContexts.addAll(map2.keySet());
        final Map<ProjectContext, Set<IssueTypeContext>> results = Maps.newHashMap();
        for (ProjectContext projectContext : projectContexts)
        {
            final Set<IssueTypeContext> issueTypesContextSet1 = (map1.get(projectContext) == null) ? Sets.<IssueTypeContext>newHashSet() : map1.get(projectContext);
            final Set<IssueTypeContext> issueTypesContextSet2 = (map2.get(projectContext) == null) ? Sets.<IssueTypeContext>newHashSet() : map2.get(projectContext);
            Set<IssueTypeContext> resultSet = Sets.newHashSet();
            if (combineType.equals(CombineType.UNION))
            {
                resultSet = Sets.union(issueTypesContextSet1, issueTypesContextSet2);
            }
            else if (combineType.equals(CombineType.INTERSECT))
            {
                resultSet = Sets.intersection(issueTypesContextSet1, issueTypesContextSet2);
            }
            if (!resultSet.isEmpty())
            {
                results.put(projectContext, resultSet);
            }
        }
        return results;
    }

    /**
     *
     * @param contextProjectMap   The map containing sets of {@link ProjectIssueTypeContext}  keyed by {@link ProjectContext}
     * @return  a set that represents IMPLICIT type contexts that have been replaced by EXPLICIT type contexts.
     *
     * The rules are as follows
     *
     * an ALL.ALL - any combination is replaced by any
     * an ALL.x - y.x - is replaced by y.x
     * an x.ALL - x.y - is replaced by x.y
     * an ALL.x - x.ALL - is replaced by x.x
     *
     * this replacement needs to take place in both directions
     *
     */
    @Nonnull
    private Set<ProjectIssueTypeContext> handleGlobals(@Nonnull final ContextProjectMap contextProjectMap)
    {
        final Set<ProjectIssueTypeContext> projectIssueTypeContexts = Sets.newHashSet();
        if (containsGlobalContext)
        {
            projectIssueTypeContexts.addAll(toProjectIssueTypeContextSet(contextProjectMap.issueTypeContextsPerProject));
        }
        if (contextProjectMap.containsGlobalContext)
        {
            projectIssueTypeContexts.addAll(toProjectIssueTypeContextSet(issueTypeContextsPerProject));
        }
        projectIssueTypeContexts.addAll(getProjectIssueTypeContextsForProjectGlobals(projectsWithAllIssueTypes, contextProjectMap.issueTypeContextsPerProject));
        projectIssueTypeContexts.addAll(getProjectIssueTypeContextsForProjectGlobals(contextProjectMap.projectsWithAllIssueTypes, issueTypeContextsPerProject));
        projectIssueTypeContexts.addAll(getProjectIssueTypeContextsForAllIssueTypes(issueTypeContextsContainedInGlobalProjectContexts, contextProjectMap.issueTypeContextsPerProject));
        projectIssueTypeContexts.addAll(getProjectIssueTypeContextsForAllIssueTypes(contextProjectMap.issueTypeContextsContainedInGlobalProjectContexts, issueTypeContextsPerProject));

        return projectIssueTypeContexts;
    }

    @Nonnull
    private Set<ProjectIssueTypeContext> getProjectIssueTypeContextsForProjectGlobals(@Nonnull final Set<ProjectContext> projectsWithAllIssueTypes, @Nonnull final Map<ProjectContext, Set<IssueTypeContext>> issueTypeContextsMap)
    {
        Set<ProjectIssueTypeContext> projectIssueTypeContexts = Sets.newHashSet();
        for (ProjectContext projectContext : projectsWithAllIssueTypes)
        {
            Set<IssueTypeContext> issueTypeContexts = issueTypeContextsMap.get(projectContext);
            Set<IssueTypeContext> issueTypeContextsinAllProjectContexts = issueTypeContextsMap.get(AllProjectsContext.INSTANCE);
            if (issueTypeContexts != null)
            {
                projectIssueTypeContexts.addAll(toProjectIssueTypeContextSet(projectContext, issueTypeContexts));
            }
            if (issueTypeContextsinAllProjectContexts != null)
            {
                projectIssueTypeContexts.addAll(toProjectIssueTypeContextSet(projectContext, issueTypeContextsinAllProjectContexts));
            }
        }
        return projectIssueTypeContexts;
    }

    @Nonnull
    private Set<ProjectIssueTypeContext>  toProjectIssueTypeContextSet(@Nonnull final ProjectContext projectContext, @Nonnull final Set<IssueTypeContext> issueTypeContexts)
    {
        final Set<ProjectIssueTypeContext> projectIssueTypeContexts = Sets.newHashSet();
        for (IssueTypeContext issueTypeContext : issueTypeContexts)
        {
            if (issueTypeContext != AllIssueTypesContext.INSTANCE)
            {
                projectIssueTypeContexts.add(new ProjectIssueTypeContextImpl(projectContext, issueTypeContext));
            }
        }
        return projectIssueTypeContexts;
    }

    @Nonnull
    private Set<ProjectIssueTypeContext> getProjectIssueTypeContextsForAllIssueTypes(@Nonnull final Set<IssueTypeContext> issueTypeContextsInAllProjects, @Nonnull final Map<ProjectContext, Set<IssueTypeContext>> issueTypeContextsMap)
    {
        Set<ProjectIssueTypeContext> projectIssueTypeContexts = Sets.newHashSet();
        if (issueTypeContextsInAllProjects.size() > 0)
        {
            for (ProjectContext projectContext : issueTypeContextsMap.keySet())
            {
                if (projectContext != AllProjectsContext.INSTANCE)
                {
                    Set<IssueTypeContext> issueTypeContexts = issueTypeContextsMap.get(projectContext);
                    if (issueTypeContexts != null)
                    {
                        for (IssueTypeContext issueTypeContext : issueTypeContexts)
                        {
                            if (issueTypeContextsInAllProjects.contains(issueTypeContext))
                            {
                                projectIssueTypeContexts.add(new ProjectIssueTypeContextImpl(projectContext, issueTypeContext));
                            }
                        }
                    }
                }
            }
        }
        return projectIssueTypeContexts;
    }


    @Nonnull
    private Set<ProjectIssueTypeContext> toProjectIssueTypeContextSet(@Nonnull final Map<ProjectContext, Set<IssueTypeContext>> projectIssueTypeContextMap)
    {
        final Set<ProjectIssueTypeContext> projectIssueTypeContexts = Sets.newHashSet();
        if (!projectIssueTypeContextMap.isEmpty())
        {
            for (Map.Entry<ProjectContext, Set<IssueTypeContext>> entry : projectIssueTypeContextMap.entrySet())
            {
                for (IssueTypeContext issueTypeContext : entry.getValue())
                {
                    projectIssueTypeContexts.add(new ProjectIssueTypeContextImpl(entry.getKey(), issueTypeContext));
                }
            }
        }
        return projectIssueTypeContexts;
    }


}
