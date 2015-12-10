package com.atlassian.jira.functest.framework.navigator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a navigator search. A search is simply made up from a number
 * of NavigatorConditions.
 *
 * @since v3.13
 */
public class NavigatorSearch
{
    private final Set<NavigatorCondition> conditions;

    public NavigatorSearch(Collection <? extends NavigatorCondition> conditions)
    {
        this.conditions = new LinkedHashSet<NavigatorCondition>(conditions);
    }

    public NavigatorSearch(NavigatorCondition... conditions)
    {
        this(Arrays.asList(conditions));
    }

    public NavigatorSearch(NavigatorCondition condition)
    {
        this(Collections.singleton(condition));
    }

    public Collection<NavigatorCondition> getConditions()
    {
        return Collections.unmodifiableCollection(conditions);
    }

    public Collection<NavigatorCondition> createConditionsCopy()
    {
        List <NavigatorCondition> arrayList = new ArrayList<NavigatorCondition>();
        for (NavigatorCondition navigatorCondition : conditions)
        {
            arrayList.add(navigatorCondition.copyCondition());
        }
        return Collections.unmodifiableList(arrayList);
    }

    public Collection<NavigatorCondition> createConditionsForParse()
    {
        List<NavigatorCondition> arrayList = new ArrayList<NavigatorCondition>();
        for (NavigatorCondition navigatorCondition : conditions)
        {
            arrayList.add(navigatorCondition.copyConditionForParse());
        }
        return Collections.unmodifiableList(arrayList);
    }

    public String toString()
    {
        return "NavigatorSearch: [Conditions : " + conditions + ']';
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        NavigatorSearch that = (NavigatorSearch) o;

        if (!conditions.equals(that.conditions))
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        return conditions.hashCode();
    }
}
