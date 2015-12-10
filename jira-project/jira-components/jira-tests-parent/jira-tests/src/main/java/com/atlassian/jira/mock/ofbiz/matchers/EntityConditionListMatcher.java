package com.atlassian.jira.mock.ofbiz.matchers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityConditionList;
import org.ofbiz.core.entity.EntityOperator;

import static com.atlassian.jira.mock.ofbiz.matchers.EntityConditionMatcher.entityCondition;

/**
 * Mockito/hamcrest matcher factory for an {@code EntityConditionList}.
 *
 * @since v6.2
 */

public class EntityConditionListMatcher extends BaseMatcher<EntityConditionList>
{
    private final EntityOperator expectedOperator;
    private final Matcher<?> conditions;

    @SuppressWarnings("unchecked")  // hamcrest's generics are broken :(
    private EntityConditionListMatcher(EntityConditionList conditions)
    {
        this.expectedOperator = conditions.getOperator();
        final int size = conditions.getConditionListSize();
        final List<Matcher<? super EntityCondition>> matchers = new ArrayList<Matcher<? super EntityCondition>>(size);
        for (int i=0; i<size; ++i)
        {
            matchers.add((Matcher<EntityCondition>)entityCondition(conditions.getCondition(i)));
        }
        this.conditions = new IsIterableContainingInAnyOrder<EntityCondition>(matchers);
    }

    public static EntityConditionListMatcher entityConditionList(EntityConditionList expected)
    {
        return new EntityConditionListMatcher(expected);
    }

    @Override
    public boolean matches(final Object item)
    {
        return item instanceof EntityConditionList && matches((EntityConditionList)item);
    }

    private boolean matches(final EntityConditionList other)
    {
        return expectedOperator.equals(other.getOperator()) && conditions.matches(toList(other));
    }

    @Override
    public void describeTo(final Description description)
    {
        description.appendText("Entity condition list joined with '")
                .appendText(expectedOperator.getCode())
                .appendText("' and ")
                .appendDescriptionOf(conditions);
    }

    private static List<EntityCondition> toList(EntityConditionList conditions)
    {
        final List<EntityCondition> list = new ArrayList<EntityCondition>(conditions.getConditionListSize());
        final Iterator<? extends EntityCondition> iter = conditions.getConditionIterator();
        while (iter.hasNext())
        {
            list.add(iter.next());
        }
        return list;
    }
}
