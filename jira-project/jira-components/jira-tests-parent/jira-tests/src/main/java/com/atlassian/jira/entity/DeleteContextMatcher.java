package com.atlassian.jira.entity;

import com.atlassian.jira.mock.ofbiz.matchers.EntityConditionMatcher;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.ofbiz.core.entity.EntityCondition;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DeleteContextMatcher extends TypeSafeDiagnosingMatcher<Delete.DeleteWhereContext>
{
    private String entity;
    private Map<String, Object> entityMap = Maps.newHashMap();
    private List<EntityCondition> conditions = Lists.newArrayList();

    public DeleteContextMatcher entity(final String entity)
    {
        this.entity = entity;
        return this;
    }

    public DeleteContextMatcher and(String field, Object value)
    {
        entityMap.put(field, value);
        return this;
    }

    public DeleteContextMatcher and(EntityCondition condition)
    {
        this.conditions.add(condition);
        return this;
    }

    @Override
    protected boolean matchesSafely(final Delete.DeleteWhereContext item, final Description mismatchDescription)
    {
        boolean matches = Objects.equal(item.getEntityName(), entity)
                && Objects.equal(entityMap, item.getFieldMap())
                && conditionsEqual(item);

        if (!matches)
        {
            mismatchDescription.appendText(description(item));
        }
        return matches;
    }

    private boolean conditionsEqual(final Delete.DeleteWhereContext item)
    {
        if (conditions != null)
        {
            if (item.getConditions() == null)
            {
                return false;
            }
            else
            {
                if (conditions.size() == item.getConditions().size())
                {
                    Iterator<?> itemCondition = item.getConditions().iterator();
                    for (EntityCondition condition : conditions)
                    {
                        if (!EntityConditionMatcher.entityCondition(condition).matches(itemCondition.next()))
                        {
                            return false;
                        }
                    }
                    return true;
                }
                else
                {
                    return true;
                }
            }
        }
        else
        {
            return item.getConditions() == null;
        }
    }

    @Override
    public void describeTo(final Description description)
    {
        description.appendText(description(entity, entityMap, conditions));
    }

    private static String description(final Delete.DeleteWhereContext item)
    {
        return description(item.getEntityName(), item.getFieldMap(), item.getConditions());
    }

    private static String description(String entity, Map<String, ?> ands, Iterable<? extends EntityCondition> condition)
    {
        return String.format("[Entity = %s, Map = %s, condition = %s]", entity, ands, condition);
    }
}
