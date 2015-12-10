package com.atlassian.jira.jql.context;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.Collections;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.containsNoNulls;

/**
 * @since v4.0
 */
public class ClauseContextImpl implements ClauseContext
{
    private static final ClauseContextImpl GLOBAL_CONTEXT = new ClauseContextImpl(Collections.singleton(ProjectIssueTypeContextImpl.createGlobalContext()));

    private final Set<ProjectIssueTypeContext> contexts;

    /**
     * @return a {@link com.atlassian.jira.jql.context.ClauseContextImpl} containing a single
     * {@link com.atlassian.jira.jql.context.ProjectIssueTypeContext} which represents the All Projects/All Issue Types context.
     */
    public static ClauseContext createGlobalClauseContext()
    {
        return GLOBAL_CONTEXT;
    }

    public ClauseContextImpl()
    {
        this(Collections.<ProjectIssueTypeContext>emptySet());
    }

    public ClauseContextImpl(final Set<ProjectIssueTypeContext> contexts)
    {
        this.contexts = Collections.unmodifiableSet(containsNoNulls("contexts", contexts));
    }

    public Set<ProjectIssueTypeContext> getContexts()
    {
        return contexts;
    }

    public boolean containsGlobalContext()
    {
        return contexts.contains(ProjectIssueTypeContextImpl.createGlobalContext());
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

        final ClauseContextImpl that = (ClauseContextImpl) o;

        if (!contexts.equals(that.contexts))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return contexts.hashCode();
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).
                append("contexts", contexts).
                toString();
    }
}
