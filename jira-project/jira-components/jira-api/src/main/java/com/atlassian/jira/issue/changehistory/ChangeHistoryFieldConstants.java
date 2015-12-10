package com.atlassian.jira.issue.changehistory;


import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.util.concurrent.LazyReference;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.atlassian.jira.util.dbc.Assertions.notNull;


/**
 * @since v4.4
 *
 *
 * @deprecated since v6.2 This class is redundant, and not used by JIRA.
 *
 */
public class ChangeHistoryFieldConstants
{
    private static final String UNRESOLVED = "unresolved";
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final List<String> managedFieldConstantTypes = Lists.newArrayList(IssueFieldConstants.STATUS,
            IssueFieldConstants.PRIORITY,
            IssueFieldConstants.RESOLUTION);

    private final LazyReference<Map<String, Multimap<String, String>>> ref = new LazyReference<Map<String, Multimap<String, String>>>()
    {
        @Override
        protected Map<String, Multimap<String, String>> create() throws Exception
        {
            final Map<String, Multimap<String, String>> map = Maps.newHashMap();
            //giant hack for  unresolved
            Multimap<String, String> multiMap = TreeMultimap.create();
            multiMap.put(UNRESOLVED, "-1");
            map.put(IssueFieldConstants.RESOLUTION,
                    multiMap);
            return map;
        }
    };

    public void addChangeHistoryFieldConstant(String field, String value, String id)
    {
        /**
         * This seems NOT VERY MULTI THREADED.  Do we really want to do this?
         */
        lock.readLock().lock();
        try
        {
            field = notNull("field", field).toLowerCase();
            value = notNull("value", value).toLowerCase();
            if (managedFieldConstantTypes.contains(field))
            {
                id = id != null ? id.toLowerCase() : id;
                if (!ref.get().containsKey(field))
                {
                    upgradeToWriteLock();
                    ref.get().put(field, TreeMultimap.<String, String>create());
                    downgradeToReadLock();
                }
                else
                {
                    if (!ref.get().get(field).containsKey(value))
                    {
                        upgradeToWriteLock();
                        ref.get().get(field).put(value, id);
                        downgradeToReadLock();
                    }
                }
            }
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    private void downgradeToReadLock()
    {
        lock.writeLock().unlock();
        lock.readLock().lock();
    }

    private void upgradeToWriteLock()
    {
        lock.readLock().unlock();
        lock.writeLock().lock();
    }

    public Set<String> getIdsForField(String field, QueryLiteral literal)
    {
        field = notNull("field", field).toLowerCase();
        String value;
        if (literal.isEmpty())
        {
            if (IssueFieldConstants.RESOLUTION.equals(field))
            {
                value = UNRESOLVED;
            }
            else
            {
                // ok be resillient but I have not idea about this value.
                return Collections.emptySet();
            }
        }
        else if (literal.getLongValue() != null)
        {
            return ImmutableSet.of(literal.getLongValue().toString());
        }
        else
        {
            value = literal.getStringValue().toLowerCase();
        }
        return ref.get().get(field) != null ? ImmutableSet.copyOf(ref.get().get(field).get(value)) : ImmutableSet.<String>of();
    }


}

