package com.atlassian.jira.issue.index;

import com.atlassian.jira.concurrent.Barrier;
import com.atlassian.jira.concurrent.BarrierFactory;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.util.DatabaseIssuesIterable;
import com.atlassian.jira.issue.util.IssuesIterable;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableList;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityConditionList;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityFindOptions;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericValue;

import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;

import static org.ofbiz.core.entity.EntityFindOptions.findOptions;
import static org.ofbiz.core.entity.EntityOperator.LESS_THAN_EQUAL_TO;

/**
 * Returns up to {@code batchSize} issues per batch.
 */
class IssueIdBatcher implements IssuesBatcher
{
    private final OfBizDelegator delegator;
    private final IssueFactory issueFactory;
    private final int batchSize;
    private final Spy spy;
    private final ImmutableList<String> orderBy = ImmutableList.of("id DESC");

    private EntityCondition condition;


    /**
     * This barrier is used for testing race conditions in background indexing. The barrier should never be raised in
     * production.
     */
    private final Barrier backgroundReindexBarrier;

    /**
     * Contains the max "id" used to get the next batch.
     */
    private long maxIdNextBatch;

    IssueIdBatcher(final OfBizDelegator delegator, final IssueFactory issueFactory, BarrierFactory barrierFactory, final int batchSize, final EntityCondition condition, final Spy spy)
    {
        this.delegator = delegator;
        this.issueFactory = issueFactory;
        this.batchSize = batchSize;
        this.spy = spy;
        this.maxIdNextBatch = selectMaxId();
        this.condition = null;
        this.condition = condition;

        backgroundReindexBarrier = barrierFactory.getBarrier("backgroundReindex");
    }

    @Override
    public Iterator<IssuesIterable> iterator()
    {
        return new IssuesIterator();
    }

    /**
     * SELECTs the max issue id from the Issue table.
     *
     * @return the issue id of the newest issue, or -1 if there are no issues
     */
    private long selectMaxId()
    {
        GenericValue maxGV = EntityUtil.getOnly(delegator.findByCondition("IssueMaxId", null, ImmutableList.of("max")));
        if (maxGV == null)
        {
            return -1;
        }

        // return -1 if there are no issues yet.
        Long max = maxGV.getLong("max");
        return max != null ? max : -1;
    }

    /**
     * Iterates through all issues starting from the issue with {@code maxId} and working backwards.
     */
    private class IssuesIterator extends AbstractIterator<IssuesIterable>
    {
        @Override
        protected IssuesIterable computeNext()
        {
            if (maxIdNextBatch < 0)
            {
                return endOfData();
            }

            // include up to BATCH_SIZE issues in the next batch
            EntityCondition idCondition = new EntityExpr("id", LESS_THAN_EQUAL_TO, maxIdNextBatch);
            EntityCondition where = condition == null ? idCondition : new EntityConditionList(ImmutableList.of(idCondition, condition), EntityOperator.AND);

            maxIdNextBatch -= batchSize;

            return new SpyingIssuesIterable(delegator, issueFactory, where, orderBy, findOptions().maxResults(batchSize), spy);
        }
    }

    /**
     * Spies on the issues that are iterated and records the last "id" that was seen.
     */
    private class SpyingIssuesIterable extends DatabaseIssuesIterable
    {
        private final Spy spy;

        SpyingIssuesIterable(OfBizDelegator delegator, IssueFactory issueFactory, @Nullable EntityCondition condition, @Nullable List<String> orderBy, @Nullable EntityFindOptions findOptions, final Spy spy)
        {
            super(delegator, issueFactory, condition, orderBy, findOptions);
            this.spy = spy;
        }

        @Override
        protected void spy(Issue next)
        {
            if (spy != null)
            {
                spy.spy(next);
            }
            // block before returning the first Issue. this line only exists for simulating race conditions in
            // background indexing.
            backgroundReindexBarrier.await();

            // this is because there may be "holes" in the returned issue ids, so we make sure that we
            // always get a full batch of #batchSize by decreasing the maxIdNextBatch whenever we spy
            maxIdNextBatch = Math.min(maxIdNextBatch, next.getId()-1);
        }
    }

    // Allows us to watch the issues as they get iterated over
    interface Spy
    {
        void spy(Issue next);
    }
}
