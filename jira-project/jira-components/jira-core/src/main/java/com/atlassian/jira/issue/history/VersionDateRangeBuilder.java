package com.atlassian.jira.issue.history;

import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.changehistory.ChangeHistoryItem;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.project.version.Version;
import com.atlassian.util.concurrent.LazyReference;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.PeekingIterator;
import org.apache.log4j.Logger;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Implementation of DateRangeBuilder - will work for all version  fields.
 *
 * @since v5.0
 */
public class VersionDateRangeBuilder extends AbstractDateRangeBuilder
{
    private static final Logger log = Logger.getLogger(AbstractDateRangeBuilder.class);
    private final String fieldName;
    private final String emptyValue;
    private static final String EMPTY_STRING = "";
    private final static Class<Issue> issueClass = Issue.class;
    @ClusterSafe
    private final LazyReference<Map<String, VersionAccessor>> ref = new LazyReference<Map<String, VersionAccessor>>(){

        @Override
        protected Map<String, VersionAccessor> create() throws Exception
        {
            return ImmutableMap.<String, VersionAccessor>
                of(SystemSearchConstants.FIX_FOR_VERSION,
                    new VersionAccessor()
                    {
                        @Override
                        public Collection<Version> getVersions(Issue issue)
                        {
                            return issue.getFixVersions();
                        }
                    },
                    IssueFieldConstants.AFFECTED_VERSIONS,
                    new VersionAccessor()
                    {

                        @Override
                        public Collection<Version> getVersions(Issue issue)
                        {
                            return issue.getAffectedVersions();
                        }
                    }
                );
        }
    };

    public VersionDateRangeBuilder(String fieldName, String emptyValue)
    {
        super(fieldName, emptyValue);
        this.fieldName = fieldName;
        this.emptyValue = emptyValue;
    }

    @Override
    protected ChangeHistoryItem createInitialChangeItem(Issue issue)
    {
        Collection<Version> currentVersions = ref.get().get(fieldName).getVersions(issue);
        ChangeHistoryItem.Builder builder = changeItemBuilder(issue);
        if (currentVersions.isEmpty())
        {
            builder.to(EMPTY_STRING , emptyValue);
        }
        else
        {
            for (Version version : currentVersions)
            {
                builder.to(version.getName(), version.getId().toString());
            }
        }
        return builder.build();
    }

    /**
     *
     * @param items                      The list of {@link com.atlassian.jira.issue.changehistory.ChangeHistoryItem}'s that represent all chnages in the current issue
     * @param initialChangeItem     The synthetic ChangeHistoryItem previously built
     * @param created                  Timestamp of the first itrem in the list
     * @return                               The  ChangeHistoryItem that accurately reflects the initial state of the issue
     *
     *   The algorithm is pretty straightforward, revers the list and then iterate through reversing each change from the initial list.  What is left is the
     *   original state.
     */
    private ChangeHistoryItem fixInitialChangeItemToValue(List <ChangeHistoryItem> items, ChangeHistoryItem initialChangeItem, Timestamp created)
    {

        Map<String, String> newToValues = Maps.newHashMap(initialChangeItem.getTos());
        newToValues.remove(emptyValue);

        for (ChangeHistoryItem item : Iterables.reverse(items)) {
            newToValues.putAll(item.getFroms());
            for (String key: item.getTos().keySet())
            {
                newToValues.remove(key);
            }
        }
        if (newToValues.isEmpty())
        {
            newToValues.put(emptyValue, EMPTY_STRING);
        }
        return new ChangeHistoryItem.Builder().fromChangeItemWithoutPreservingChanges(initialChangeItem).
                                               withTos(newToValues).nextChangeOn(created).build();
    }

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
                final ChangeHistoryItem fixedInitialItem = fixInitialChangeItemToValue(items, initialChangeItem, items.get(0).getCreated());
                changeItems.add(fixedInitialItem);
                Map<String, String> previousItemToValues = fixedInitialItem.getTos();
                PeekingIterator<ChangeHistoryItem> iterator = Iterators.peekingIterator(items.iterator());

                while (iterator.hasNext())
                {
                    ChangeHistoryItem nextItem = iterator.next();
                    Map<String, String>  removals = nextItem.getFroms();
                    Map <String, String> additions = nextItem.getTos();
                    Map <String, String> effectiveTos = Maps.newHashMap();
                    effectiveTos.putAll(previousItemToValues);
                    effectiveTos.putAll(additions);
                    // to empty is not an addition
                    effectiveTos.remove(emptyValue);
                    for (String key : removals.keySet())
                    {
                        effectiveTos.remove(key);
                    }
                    if (effectiveTos.isEmpty())
                    {
                        effectiveTos.put(emptyValue, EMPTY_STRING);
                    }
                   if (iterator.hasNext())
                    {
                        changeItems.add(new ChangeHistoryItem.Builder().fromChangeItem(nextItem).
                                nextChangeOn(iterator.peek().getCreated()).field(fieldName).
                                withFroms(previousItemToValues).withTos(effectiveTos).build());
                    }
                    else
                    {
                        changeItems.add(new ChangeHistoryItem.Builder().fromChangeItem(nextItem).
                                field(fieldName).withFroms(previousItemToValues).withTos(effectiveTos).build());
                    }
                    previousItemToValues = effectiveTos;
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

    private abstract static class VersionAccessor
    {
        public abstract Collection<Version> getVersions(Issue issue);
    }
}
