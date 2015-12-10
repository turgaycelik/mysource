package com.atlassian.jira.issue.history;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.changehistory.ChangeHistoryItem;
import com.atlassian.jira.issue.index.DocumentConstants;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.PeekingIterator;
import org.apache.log4j.Logger;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * Abstract  implementation of DateRangeBuilder - gprovides a buildDateRanges that will work for all single value system fields.
 *
 * @since v4.4
 */
public abstract class AbstractDateRangeBuilder implements DateRangeBuilder
{
    /** Convenient constant for date range builders to use as the initial value
     * provided to {@link ChangeHistoryItem.Builder#withId(Long)} and
     * {@link ChangeHistoryItem.Builder#inChangeGroup(Long)}, since the
     * {@code long} (non-object) argument forms have been deprecated.
     *
     * @since 6.0
     */
    protected static final Long MINUS_1 = -1L;

    private static final Logger log = Logger.getLogger(AbstractDateRangeBuilder.class);
    private final String field;
    private final String emptyValue;

    public AbstractDateRangeBuilder(String field, String emptyValue)
    {
        this.field = field;
        this.emptyValue = emptyValue;
    }

    @Override
    public List<ChangeHistoryItem> buildDateRanges(Issue issue, List<ChangeHistoryItem> items)
    {
        final List<ChangeHistoryItem> changeItems = Lists.newArrayList();
        try
        {
            final ChangeHistoryItem initialChangeItem =  createInitialChangeItem(issue);
            if (items.isEmpty())
            {
                changeItems.add(initialChangeItem);
            }
            else
            {
                changeItems.add(fixInitialChangeItem(initialChangeItem, items.get(0).getFroms(), items.get(0).getCreated()));
                PeekingIterator<ChangeHistoryItem> iterator = Iterators.peekingIterator(items.iterator());
                while (iterator.hasNext())
                {
                    ChangeHistoryItem nextItem = iterator.next();
                    if (iterator.hasNext())
                    {
                        changeItems.add(createChangeItem(nextItem,iterator.peek().getCreated()));
                    }
                    else
                    {
                        changeItems.add(createChangeItem(nextItem, nextItem.getNextChangeCreated()));
                    }
                }
            }
        }
        catch (NullPointerException npe)
        {
            log.warn(String.format("The issue %s has serious data integrity issues", issue.getKey()), npe);
            return ImmutableList.of();
        }
        return changeItems;
    }

    private ChangeHistoryItem fixInitialChangeItem(ChangeHistoryItem initialChangeItem, Map<String, String> values, Timestamp created)
    {
        if (values.isEmpty())
        {
            values = Maps.newHashMap();
            values.put(emptyValue, "");
        }
        return new ChangeHistoryItem.Builder().fromChangeItemWithoutPreservingChanges(initialChangeItem).
                                                   withTos(values).nextChangeOn(created).build();
    }

    protected ChangeHistoryItem createChangeItem(ChangeHistoryItem changeItem, Timestamp nextChange)
    {
        ChangeHistoryItem.Builder builder = new ChangeHistoryItem.Builder().fromChangeItem(changeItem).
                          nextChangeOn(nextChange);
        if (changeItem.getTos().isEmpty())
        {
             builder.to("", emptyValue);
        }
        if (changeItem.getFroms().isEmpty())
        {
            builder.changedFrom("", emptyValue);
        }
        return builder.build();
    }

    protected ChangeHistoryItem.Builder changeItemBuilder(Issue issue)
    {
        return new ChangeHistoryItem.Builder()
                .withId(MINUS_1)
                .inChangeGroup(MINUS_1)
                .forIssue(issue.getId(), issue.getKey())
                .inProject(issue.getProjectObject().getId())
                .field(getField())
                .on(issue.getCreated())
                .byUser(issue.getReporterId() == null ? DocumentConstants.ISSUE_NO_AUTHOR : issue.getReporterId());
    }

    protected abstract ChangeHistoryItem createInitialChangeItem(Issue issue);

    protected String getField()
    {
        return field;
    }

    public String getEmptyValue()
    {
        return emptyValue;
    }
}
