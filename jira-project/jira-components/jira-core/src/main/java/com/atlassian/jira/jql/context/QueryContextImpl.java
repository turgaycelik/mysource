package com.atlassian.jira.jql.context;

import com.atlassian.jira.util.Supplier;
import com.atlassian.jira.util.collect.MultiMap;
import com.atlassian.jira.util.collect.MultiMaps;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @since v4.0
 */
public class QueryContextImpl implements QueryContext
{
    private final Collection<ProjectIssueTypeContexts> projectIssueTypeContexts;

    public QueryContextImpl(ClauseContext clauseContext)
    {
        this.projectIssueTypeContexts = Collections.unmodifiableList(init(clauseContext));
    }

    public Collection<ProjectIssueTypeContexts> getProjectIssueTypeContexts()
    {
        return projectIssueTypeContexts;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final QueryContextImpl that = (QueryContextImpl) o;
        if (!projectIssueTypeContexts.equals(that.projectIssueTypeContexts))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return projectIssueTypeContexts.hashCode();
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).
                append("projectIssueTypeContexts", projectIssueTypeContexts).
                toString();
    }

    private static List<ProjectIssueTypeContexts> init(final ClauseContext clauseContext)
    {
        MultiMap<ProjectContext, IssueTypeContext, Set<IssueTypeContext>> contextSetMap = MultiMaps.create(new Supplier<Set<IssueTypeContext>>()
        {
            public Set<IssueTypeContext> get()
            {
                return new HashSet<IssueTypeContext>();
            }
        });

        final Set<ProjectIssueTypeContext> contexts = clauseContext.getContexts();
        for (ProjectIssueTypeContext context : contexts)
        {
            contextSetMap.putSingle(context.getProjectContext(), context.getIssueTypeContext());
        }

        List<ProjectIssueTypeContexts> ctxs = new ArrayList<ProjectIssueTypeContexts>(contextSetMap.size());
        for (Map.Entry<ProjectContext, Set<IssueTypeContext>> entry : contextSetMap.entrySet())
        {
            ctxs.add(new ProjectIssueTypeContexts(entry.getKey(), entry.getValue()));
        }
        return ctxs;
    }
}
