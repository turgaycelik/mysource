package com.atlassian.jira.upgrade.tasks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.atlassian.jira.entity.EntityListConsumer;
import com.atlassian.jira.entity.Select;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;

import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Recalculates watches count because of 'JRA-34394: Watch count gets out of sync when cloning issues' bug.
 *
 * @since v6.3.3
 */
public class UpgradeTask_Build6331 extends AbstractUpgradeTask
{

    private static final Logger log = LoggerFactory.getLogger(UpgradeTask_Build6331.class);

    private final OfBizDelegator ofBizDelegator;

    public UpgradeTask_Build6331(OfBizDelegator ofBizDelegator)
    {
        super(true);
        this.ofBizDelegator = ofBizDelegator;
    }

    @Override
    public String getBuildNumber()
    {
        return "6331";
    }

    @Override
    public String getShortDescription()
    {
        return "JRA-34394: Fixing incorrect watch count for cloned issues";
    }

    @Override
    public void doUpgrade(final boolean setupMode) throws Exception
    {
        if (setupMode)
        {
            // there can not exist any broken issue => no update is needed
            return;
        }

        final DateTime startedAt = new DateTime();
        final Map<Long, Long> watchesByIssueId = findWatchesByIssueId();
        final List<BrokenIssue> brokenIssues = findBrokenIssues(watchesByIssueId);
        fixBrokenIssues(brokenIssues);
        log.info(String.format("Upgrade task took %d seconds to fix %d records", Seconds.secondsBetween(startedAt, new DateTime()).getSeconds(), brokenIssues.size()));
    }

    private Map<Long, Long> findWatchesByIssueId()
    {
        return Select.columns("id", "watches").from("Issue").runWith(ofBizDelegator)
                .consumeWith(new EntityListConsumer<GenericValue, Map<Long, Long>>()
                {

                    final Map<Long, Long> result = new HashMap<Long, Long>();

                    @Override
                    public void consume(final GenericValue entity)
                    {
                        result.put(entity.getLong("id"), entity.getLong("watches"));
                    }

                    @Override
                    public Map<Long, Long> result()
                    {
                        return result;
                    }

                });
    }

    private List<BrokenIssue> findBrokenIssues(final Map<Long, Long> watchesByIssueId)
    {
        return Select.columns("sinkNodeId", "count").from("UserAssociationCount")
                .whereEqual("sinkNodeEntity", "Issue").whereEqual("associationType", "WatchIssue").runWith(ofBizDelegator)
                .consumeWith(new EntityListConsumer<GenericValue, List<BrokenIssue>>()
                {

                    final List<BrokenIssue> result = new LinkedList<BrokenIssue>();

                    /* this set is all issues now, but will be pruned to issues without
                     * watchers as the query goes */
                    final Set<Long> issuesWithoutWatchers = new HashSet<Long>(watchesByIssueId.keySet());

                    /*
                     * This adds to the result all issues that are supposed to have nonzero
                     * watch count but are broken and either the number is wrong, or null.
                     */
                    @Override
                    public void consume(final GenericValue entity)
                    {
                        final Long issueId = entity.getLong("sinkNodeId"); // can possibly be null

                        // let's not take any chances.
                        if(issueId != null)
                        {
                            final Long correctWatches = entity.getLong("count"); // this can't be null
                            final Long currentWatches = watchesByIssueId.get(issueId);

                            issuesWithoutWatchers.remove(issueId);
                            if (currentWatches == null || !currentWatches.equals(correctWatches))
                            {
                                result.add(new BrokenIssue(issueId, correctWatches));
                            }
                        }
                    }

                    /*
                     * This adds to the result all issues that are supposed to have zero
                     * watch count but are broken and either the number is wrong, or null.
                     */
                    @Override
                    public List<BrokenIssue> result()
                    {
                        /* At this stage, the set is indeed pruned to contain only
                         * issues without watchers */
                        for (Long issueId : issuesWithoutWatchers)
                        {
                            Long currentWatches = watchesByIssueId.get(issueId);
                            if (currentWatches == null || !currentWatches.equals(0L))
                            {
                                result.add(new BrokenIssue(issueId, 0L));
                            }
                        }
                        return result;
                    }

                });
    }

    private void fixBrokenIssues(List<BrokenIssue> brokenIssues) throws GenericEntityException
    {
        for (BrokenIssue brokenIssue : brokenIssues)
        {
            final GenericValue issue = ofBizDelegator.findById("Issue", brokenIssue.issueId);
            if (issue != null)
            {
                issue.set("watches", brokenIssue.correctWatches);
                issue.store();
            }
        }
    }

    private static final class BrokenIssue
    {

        public final long issueId;
        public final long correctWatches;

        private BrokenIssue(final long issueId, final long correctWatches)
        {
            this.issueId = issueId;
            this.correctWatches = correctWatches;
        }
    }

}
