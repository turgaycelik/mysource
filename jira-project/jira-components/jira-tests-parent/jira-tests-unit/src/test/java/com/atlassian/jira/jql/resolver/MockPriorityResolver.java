package com.atlassian.jira.jql.resolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.issue.priority.MockPriority;
import com.atlassian.jira.issue.priority.Priority;

/**
 * A NameResolver for Priority based on a map configuration. Uses {@link com.atlassian.jira.issue.priority.MockPriority}.
 *
 * @since v4.0
 */
public class MockPriorityResolver extends PriorityResolver
{
    ///CLOVER:OFF

    private LinkedHashMap<String, List<Long>> priorityMap;

    private Map<Long, Priority> prioritiesById = new LinkedHashMap<Long, Priority>();

    public MockPriorityResolver(LinkedHashMap<String, List<Long>> namesToIds)
    {
        super(null);
        this.priorityMap = namesToIds;
        long s = 0;
        for (Map.Entry<String, List<Long>> priorityEntry : namesToIds.entrySet())
        {
            for (Long id : priorityEntry.getValue())
            {
                final String name = priorityEntry.getKey();
                final MockPriority mockPriority = new MockPriority(id.toString(), name);
                mockPriority.setSequence(s++);
                prioritiesById.put(id, mockPriority);
            }
        }
    }

    public List<String> getIdsFromName(final String name)
    {
        final List<Long> longs = priorityMap.get(name);
        if (longs == null)
        {
            return Collections.emptyList();
        }
        else
        {
            final List<String> list = new ArrayList<String>();
            for (Iterator<Long> longIterator = longs.iterator(); longIterator.hasNext();)
            {
                Long aLong = longIterator.next();
                list.add(aLong.toString());
            }

            return list;
        }
    }

    public boolean nameExists(final String name)
    {
        return priorityMap.containsKey(name);
    }

    public boolean idExists(final Long id)
    {
        return prioritiesById.containsKey(id);
    }

    public Priority get(final Long id)
    {
        return prioritiesById.get(id);
    }

    public Collection<Priority> getAll()
    {
        return new ArrayList<Priority>(prioritiesById.values());
    }

    ///CLOVER:ON

}
