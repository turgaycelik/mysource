package com.atlassian.jira.security.util;

import com.atlassian.jira.issue.comparator.OfBizComparators;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractGroupMapper implements GroupMapper
{
    private Map groupMapping;

    protected abstract Map init() throws GenericEntityException;

    protected void addEntry(Map map, Object key, Object value)
    {
        final Object currentValue = map.get(key);
        Set set = null;
        if (currentValue == null)
        {
            set = new HashSet();
        }
        else if (currentValue instanceof Set)
        {
            set = (Set) currentValue;
        }
        else
        {
            throw new IllegalStateException("The values in the groupMapping Map must implement Set interface.");
        }

        set.add(value);
        map.put(key, set);
    }

    protected Map getGroupMapping()
    {
        return groupMapping;
    }

    protected void setGroupMapping(Map groupToSchemeMapping)
    {
        this.groupMapping = groupToSchemeMapping;
    }

    public Collection getMappedValues(String groupName)
    {
        final Set schemes = (Set) getGroupMapping().get(groupName);
        if (schemes != null)
        {
            List schemeList = new ArrayList(schemes);
            Collections.sort(schemeList, OfBizComparators.NAME_COMPARATOR);
            return schemeList;
        }
        else
        {
            return Collections.EMPTY_LIST;
        }
    }
}
