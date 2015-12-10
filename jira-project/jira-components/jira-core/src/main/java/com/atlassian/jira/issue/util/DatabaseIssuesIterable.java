package com.atlassian.jira.issue.util;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.ofbiz.DatabaseIterable;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.PagedDatabaseIterable;
import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.collect.EnclosedIterable;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityFindOptions;

import javax.annotation.Nullable;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * This {@link EnclosedIterable} is used to iterate over all issues in the database.
 * <p>
 * This implementation is superseded by the more flexible {@link DatabaseIterable} or
 * {@link PagedDatabaseIterable}.
 */
public class DatabaseIssuesIterable implements IssuesIterable
{
    private final OfBizDelegator delegator;
    private final IssueFactory issueFactory;
    private final EntityCondition condition;
    private final EntityFindOptions findOptions;
    @Nullable
    private final List<String> orderBy;

    public DatabaseIssuesIterable(final OfBizDelegator delegator, final IssueFactory issueFactory)
    {
        this(delegator, issueFactory, null, null, null);
    }

    public DatabaseIssuesIterable(final OfBizDelegator delegator, final IssueFactory issueFactory, @Nullable EntityCondition condition)
    {
        this(delegator, issueFactory, condition, null, null);
    }

    public DatabaseIssuesIterable(final OfBizDelegator delegator, final IssueFactory issueFactory, @Nullable EntityCondition condition, @Nullable EntityFindOptions findOptions)
    {
        this(delegator, issueFactory, condition, null, findOptions);
    }

    public DatabaseIssuesIterable(final OfBizDelegator delegator, final IssueFactory issueFactory, @Nullable EntityCondition condition, @Nullable List<String> orderBy, @Nullable EntityFindOptions findOptions)
    {
        this.delegator = notNull(this.getClass().getName() + " needs a not null " + OfBizDelegator.class.getName() + " instance", delegator);
        this.issueFactory = notNull(this.getClass().getName() + " needs a not null " + IssueFactory.class.getName() + " instance", issueFactory);
        this.orderBy = orderBy;
        this.condition = condition;
        this.findOptions = findOptions;
    }

    public void foreach(final Consumer<Issue> sink)
    {
        final DatabaseIssuesIterator iterator = new DatabaseIssuesIterator(delegator, issueFactory, condition, orderBy, findOptions);
        try
        {
            while (iterator.hasNext())
            {
                Issue next = iterator.next();
                spy(next);
                sink.consume(next);
            }
        }
        finally
        {
            iterator.close();
        }
    }

    /**
     * You cannot rely on this size after you have started iterating through the issues
     */
    public int size()
    {
        return (int) delegator.getCount("Issue");
    }

    public boolean isEmpty()
    {
        return size() == 0;
    }

    @Override
    public String toString()
    {
        return getClass().getName() + ": All issues in the database.";
    }

    /**
     * Allows for IssuesIterable implementations that spy on each iterated issue. Implementations of this method must
     * not modify the issue state.
     *
     * @param next an Issue that is about to be iterated through
     */
    protected void spy(Issue next)
    {
    }
}