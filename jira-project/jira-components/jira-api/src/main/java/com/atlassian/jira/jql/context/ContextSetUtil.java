package com.atlassian.jira.jql.context;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import static com.atlassian.jira.util.dbc.Assertions.containsNoNulls;

/**
 * Performs set utilities on {@link com.atlassian.jira.jql.context.ClauseContext}'s
 *
 * @since v4.0
 */
public class ContextSetUtil
{
    private static final ContextSetUtil INSTANCE = new ContextSetUtil();

    private static final ProjectIssueTypeContext GLOBAL_CONTEXT = ProjectIssueTypeContextImpl.createGlobalContext();

    public static ContextSetUtil getInstance()
    {
        return INSTANCE;
    }
    
    /**
     * Performs an itersection of the ClauseContext's passed in.
     *
     * NOTE: When {@link com.atlassian.jira.jql.context.ProjectIssueTypeContext}'s are compared they are considered
     * equivilent if the id values are the same, we do not compare if they are Explicit or Implicit. When combined
     * an Explicit flag will always replace an Implicit flag.
     *
     * @param childClauseContexts the child clause contexts to intersect, must never be null or contain null elements
     * @return the intersection of ClauseContext's that were passed in.
     */
    public ClauseContext intersect(final Set<? extends ClauseContext> childClauseContexts)
    {
        containsNoNulls("childClauseContexts", childClauseContexts);

        if (childClauseContexts.isEmpty())
        {
            return new ClauseContextImpl();
        }
        
        Iterator<? extends ClauseContext> iter = childClauseContexts.iterator();

        // Our initial result set is the first set of ProjectIssueTypeContext's in out childClauseContexts
        ClauseContext intersection =  iter.next();

        if (childClauseContexts.size() == 1)
        {
            return new ClauseContextImpl(intersection.getContexts());
        }
        while(iter.hasNext())
        {
            ClauseContext toIntersect = iter.next();
            intersection = intersect(intersection, toIntersect);
        }
        return intersection;
    }

    /**
     * Performs a union of the ClauseContext's passed in.
     *
     * NOTE: When {@link com.atlassian.jira.jql.context.ProjectIssueTypeContext}'s are compared they are considered
     * equivilent if the id values are the same, we do not compare if they are Explicit or Implicit. When combined
     * an Explicit flag will always replace an Implicit flag.
     *
     * @param childClauseContexts the child clause contexts to union, must never be null or contain null elements
     * @return the union of the ClauseContext's that were passed in.
     */
    public ClauseContext union(final Set<? extends ClauseContext> childClauseContexts)
    {
        containsNoNulls("childClauseContexts", childClauseContexts);

        if (childClauseContexts.isEmpty())
        {
            return new ClauseContextImpl();
        }

        Iterator<? extends ClauseContext> iter = childClauseContexts.iterator();

        // Our initial result set is the first set of ProjectIssueTypeContext's in out childClauseContexts
        ClauseContext union = iter.next();

        while(iter.hasNext())
        {
            ClauseContext toUnion = iter.next();
            union = union(union, toUnion);
        }

        return union;
    }

    @Nonnull
    private  ClauseContext intersect(@Nonnull final  ClauseContext context1, @Nonnull final ClauseContext context2)
    {
        ClauseContext clauseContext = shortCircuitIfBothGlobal(context1, context2);
        if (clauseContext != null)
        {
            return clauseContext;
        }
        final ContextProjectMap contextProjectMap1 = new ContextProjectMap(context1);
        final ContextProjectMap contextProjectMap2 = new ContextProjectMap(context2);
        clauseContext = contextProjectMap1.intersect(contextProjectMap2);
        return clauseContext;
    }

    @Nonnull
    private  ClauseContext  union(@Nonnull final  ClauseContext context1, @Nonnull final ClauseContext context2)
    {
        final ContextProjectMap contextProjectMap1 = new ContextProjectMap(context1);
        final ContextProjectMap contextProjectMap2 = new ContextProjectMap(context2);
        return contextProjectMap1.union(contextProjectMap2);
    }

    @Nonnull
    private Map<ProjectContext, Set<IssueTypeContext>> handleProjectGlobals(@Nullable final Set<IssueTypeContext> issueTypeContexts,
                                                                             @Nonnull final Set<ProjectContext> projectContexts)
    {
        Map<ProjectContext, Set<IssueTypeContext>> resultsMap = Maps.newHashMap();
        if (issueTypeContexts != null )
        {
            for (ProjectContext projectContext : projectContexts)
            {
                resultsMap.put(projectContext, issueTypeContexts);
            }
        }
        return resultsMap;
    }

    @Nullable
    private ClauseContext shortCircuitIfBothGlobal(@Nonnull final ClauseContext context1,
            @Nonnull final ClauseContext context2)
    {
        if (context1.getContexts().contains(GLOBAL_CONTEXT) && context2.getContexts().contains(GLOBAL_CONTEXT))
        {
            return new ClauseContextImpl(Sets.union(context1.getContexts(), context2.getContexts()));
        }
        return null;
    }
}
