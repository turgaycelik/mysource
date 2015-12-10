package com.atlassian.jira.mock.ofbiz.matchers;

import com.atlassian.jira.ofbiz.FieldMap;

import org.hamcrest.Matcher;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityConditionList;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityFieldMap;

import static com.atlassian.jira.mock.ofbiz.matchers.EntityConditionListMatcher.entityConditionList;
import static com.atlassian.jira.mock.ofbiz.matchers.EntityExprMatcher.entityExpr;
import static com.atlassian.jira.mock.ofbiz.matchers.EntityFieldMapMatcher.entityFieldMap;

/**
 * Mockito/hamcrest matcher factory for an {@code EntityCondition}.
 *
 * @since v6.2
 */
public class EntityConditionMatcher
{
    private EntityConditionMatcher() {}

    public static Matcher<? extends EntityCondition> entityCondition(EntityCondition condition)
    {
        if (condition instanceof EntityExpr)
        {
            return entityExpr((EntityExpr)condition);
        }
        if (condition instanceof EntityFieldMap)
        {
            return entityFieldMap((EntityFieldMap)condition);
        }
        if (condition instanceof EntityConditionList)
        {
            return entityConditionList((EntityConditionList)condition);
        }
        throw new IllegalArgumentException("Unable to match entity condition type " + condition.getClass().getName());
    }

    public static Matcher<? extends EntityCondition> entityCondition(FieldMap fieldMap)
    {
        return entityFieldMap(fieldMap);
    }
}
