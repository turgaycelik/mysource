package com.atlassian.jira.ofbiz;

import com.atlassian.jira.util.dbc.Null;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.GenericModelException;
import org.ofbiz.core.entity.model.ModelEntity;

import java.util.List;

/**
 * This class is used to track how many clauses are in an EntityCondition.  It doesn't inspect the
 * condition but rather is a glorified tuple to allow methods to return a Condition and a count of where
 * clauses. 
 *
 * @since v3.13
 */
public class CountedCondition extends EntityCondition
{
    private final EntityCondition condition;
    private final int terms;

    public CountedCondition(final EntityCondition condition, final int terms)
    {
        Null.not("condition", condition);
        if (terms <= 0)
        {
            throw new IllegalArgumentException("terms <= 0");
        }

        this.condition = condition;
        this.terms = terms;
    }

    public EntityCondition getCondition()
    {
        return condition;
    }

    public int getTermCount()
    {
        return terms;
    }

    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

    public String makeWhereString(final ModelEntity modelEntity, final List entityConditionParams)
    {
        return condition.makeWhereString(modelEntity, entityConditionParams);
    }

    public void checkCondition(final ModelEntity modelEntity) throws GenericModelException
    {
        condition.checkCondition(modelEntity);
    }
}
