package com.atlassian.jira.upgrade.tasks;

import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;

class MockEntityExpr extends EntityExpr
{
    MockEntityExpr(final String lhs, final EntityOperator operator, final Object rhs)
    {
        super(lhs, operator, rhs);
    }

    public boolean equals(final Object o)
    {
        if (!(o instanceof EntityExpr))
        {
            return false;
        }
        final EntityExpr that = (EntityExpr) o;
        final boolean equals = that.getLhs().equals(getLhs()) && that.getOperator().equals(getOperator());
        if (that.getRhs() == null)
        {
            return equals && (getRhs() == null);
        }
        else
        {
            return equals && that.getRhs().equals(getRhs());
        }
    }
}
