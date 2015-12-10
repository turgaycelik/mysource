package com.atlassian.jira.upgrade.tasks;

import java.util.Iterator;
import java.util.List;

import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityConditionList;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;

class MockEntityConditionList extends EntityConditionList
{
    MockEntityConditionList(final List list, final EntityOperator entityOperator)
    {
        super(list, entityOperator);
    }

    public boolean equals(final Object o)
    {
        if (!(o instanceof EntityConditionList))
        {
            return false;
        }
        final EntityConditionList that = (EntityConditionList) o;
        if (getConditionListSize() != that.getConditionListSize())
        {
            return false;
        }
        final Iterator thisIterator = getConditionIterator();
        final Iterator thatIterator = that.getConditionIterator();
        while (thisIterator.hasNext())
        {
            final Object thisO = thisIterator.next();
            if (thisO instanceof EntityExpr)
            {
                final EntityExpr thisEntityExpr = (EntityExpr) thisO;
                final EntityExpr thatEntityExpr = (EntityExpr) thatIterator.next();
                if (!thisEntityExpr.equals(thatEntityExpr))
                {
                    return false;
                }
            }
            else if (thisO instanceof EntityCondition)
            {
                final EntityCondition thisEntityCondition = (EntityCondition) thisO;
                final EntityCondition thatEntityCondition = (EntityCondition) thatIterator.next();
                if (!thisEntityCondition.equals(thatEntityCondition))
                {
                    return false;
                }
            }

        }
        return true;
    }
}
