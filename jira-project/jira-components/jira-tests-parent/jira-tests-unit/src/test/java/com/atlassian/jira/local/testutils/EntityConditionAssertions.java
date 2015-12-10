package com.atlassian.jira.local.testutils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.atlassian.jira.ofbiz.CountedCondition;

import com.google.common.collect.Lists;

import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityConditionList;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.EntityWhereString;

import junit.framework.Assert;

/**
 * Some utility assertions for EntityConditions.
 *
 * @since v3.13
 */
public class EntityConditionAssertions
{
    public static void assertEquals(final EntityCondition expected, final EntityCondition actual)
    {
        assertEquals(expected, actual, new Context());
    }

    private static void assertEquals(final EntityCondition expected, final EntityCondition actual, final Context ctx)
    {
        if (expected instanceof CountedCondition)
        {
            if (!(actual instanceof CountedCondition))
            {
                assertEquals(unwrapCondition(expected), actual, ctx);
            }
            else
            {
                assertCountedConditionEquals((CountedCondition)expected, (CountedCondition)actual, ctx);
            }
        }
        else
        {
            final EntityCondition unwrappedCondition = unwrapCondition(actual);

            if (expected instanceof EntityWhereString)
            {
                assertWhereEquals((EntityWhereString)expected, unwrappedCondition, ctx);
            }
            else if (expected instanceof EntityConditionList)
            {
                assertConditionListEquals((EntityConditionList)expected, unwrappedCondition, ctx);
            }
            else if (expected instanceof EntityExpr)
            {
                assertEntityExpressionEquals((EntityExpr)expected, unwrappedCondition, ctx);
            }

            else
            {
                Assert.fail(createMessage("Unable to detect entity type.", ctx));
            }
        }
    }

    private static void assertCountedConditionEquals(CountedCondition expectedCondition, CountedCondition actualCondition, Context context)
    {
        context.push("CountedCondition");
        try
        {
            Assert.assertEquals(createMessage("Size is not correct.", context), expectedCondition.getTermCount(), actualCondition.getTermCount());
            assertEquals(expectedCondition.getCondition(), actualCondition.getCondition(), context);
        }
        finally
        {
            context.pop();
        }
    }

    private static void assertEntityExpressionEquals(final EntityExpr expectedExpr, final EntityCondition actualCondition,
            final Context context)
    {
        Assert.assertTrue(createMessage("Classes do not match.", context), expectedExpr.getClass().isAssignableFrom(actualCondition.getClass()));

        final EntityExpr actualExpr = (EntityExpr) actualCondition;

        context.push("EntityExpr");
        try
        {
            Assert.assertEquals(createMessage("Operator is not same.", context), expectedExpr.getOperator(), actualExpr.getOperator());
        }
        finally
        {
            context.pop();
        }

        context.push("EntityExpr[left]");
        try
        {
            Assert.assertEquals(createMessage("Upper is not same.", context), expectedExpr.isLUpper(), actualExpr.isLUpper());

            final Object left1 = expectedExpr.getLhs();
            final Object left2 = actualExpr.getLhs();

            if (left1 instanceof EntityCondition)
            {
                assertEquals((EntityCondition)left1, (EntityCondition)left2, context);
            }
            else
            {
                Assert.assertEquals(createMessage("Parameter is not the same.", context), left1, left2);
            }
        }
        finally
        {
            context.pop();
        }

        context.push("EntityExpr[right]");
        try
        {
            Assert.assertEquals(createMessage("Upper is not same.", context), expectedExpr.isRUpper(), actualExpr.isRUpper());

            final Object right1 = expectedExpr.getRhs();
            final Object right2 = actualExpr.getRhs();

            if (right1 instanceof EntityCondition)
            {
                assertEquals((EntityCondition)right1, (EntityCondition)right2, context);
            }
            else
            {
                Assert.assertEquals(createMessage("Parameter is not the same.", context), right1, right2);
            }
        }
        finally
        {
            context.pop();
        }
    }

    private static void assertWhereEquals(final EntityWhereString expectedWhereCondition, final EntityCondition actualCondition,
            final Context context)
    {
        Assert.assertEquals(createMessage("Classes do not match.", context), expectedWhereCondition.getClass(), actualCondition.getClass());

        EntityWhereString actualWhereCondition = (EntityWhereString) actualCondition;

        context.push("EntityWhereString");
        try
        {
            Assert.assertEquals(createMessage("Where clause does not match.", context),
                expectedWhereCondition.makeWhereString(null, null),
                actualWhereCondition.makeWhereString(null, null));
        }
        finally
        {
            context.pop();
        }
    }

    private static void assertConditionListEquals(final EntityConditionList expectedList, final EntityCondition actualCondition,
            final Context context)
    {
        EntityConditionList actualList;
        if (actualCondition instanceof EntityConditionList)
        {
            actualList = (EntityConditionList) actualCondition;
        }
        else if (actualCondition instanceof EntityExpr)
        {
            EntityExpr expression = (EntityExpr) actualCondition;
            EntityOperator operator = expression.getOperator();
            if (!expression.isLUpper() && !expression.isRUpper()
                && (operator == EntityOperator.AND || operator == EntityOperator.OR))
            {
                actualList = new EntityConditionList(Lists.newArrayList((EntityCondition)expression.getLhs(), (EntityCondition)expression.getRhs()), operator);
            }
            else
            {
                Assert.fail(createMessage("List and expression do not match.", context));
                actualList = null;
            }
        }
        else
        {
            Assert.assertEquals(createMessage("Classes do not match.", context), expectedList.getClass(), actualCondition.getClass());
            actualList = null;
        }

        context.push("EntityConditionList");
        try
        {
            Assert.assertEquals("'" + context + "' Size does not match.",
                    expectedList.getConditionListSize(),
                    actualList.getConditionListSize());

            Assert.assertEquals("'" + context + "' Operators do not match.",
                    expectedList.getOperator(),
                    actualList.getOperator());
        }
        finally
        {
            context.pop();
        }

        for (int i = 0; i < expectedList.getConditionListSize(); i++)
        {
            context.push("EntityConditionList[" + i + "]");
            try
            {
                assertEquals(expectedList.getCondition(i), actualList.getCondition(i));
            }
            finally
            {
                context.pop();
            }
        }

    }

    private static EntityCondition unwrapCondition(final EntityCondition condition)
    {
        EntityCondition extractedCondition = condition;
        while (extractedCondition instanceof CountedCondition)
        {
            extractedCondition = ((CountedCondition)extractedCondition).getCondition();
        }

        return extractedCondition;
    }

    private static String createMessage(final String msg, final Context context)
    {
        return "'" + context + "': " + msg;
    }

    private static class Context
    {
        private final List<String> context = new ArrayList<String>();

        public void push(final String ctx)
        {
            context.add(ctx);
        }

        public void push(final String ctx, final int index)
        {
            push(ctx + '[' + index + ']');
        }

        public void pop()
        {
            context.remove(context.size() - 1);
        }

        public String toString()
        {
            StringBuilder buffer = new StringBuilder();
            for (Iterator<String> iterator = context.iterator(); iterator.hasNext();)
            {
                String currentContext = iterator.next();
                buffer.append(currentContext);
                if (iterator.hasNext())
                {
                    buffer.append('/');
                }
            }

            return buffer.toString();
        }
    }
}
