/*
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Oct 22, 2002
 * Time: 4:16:21 PM
 * To change this template use Options | File Templates.
 */
package com.atlassian.core.ofbiz;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

import com.atlassian.core.ofbiz.util.EntityUtils;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;

import org.junit.Test;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilDateTime;
import org.ofbiz.core.util.UtilMisc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TestEntityUtils extends AbstractOFBizTestCase
{
    @Test
    public void testGetOperator()
    {
        assertEquals(EntityOperator.EQUALS, EntityUtils.getOperator("="));
        assertEquals(EntityOperator.GREATER_THAN, EntityUtils.getOperator(">"));
        assertEquals(EntityOperator.GREATER_THAN_EQUAL_TO, EntityUtils.getOperator(">="));
        assertEquals(EntityOperator.LESS_THAN, EntityUtils.getOperator("<"));
        assertEquals(EntityOperator.LESS_THAN_EQUAL_TO, EntityUtils.getOperator("<="));
        assertNull(EntityUtils.getOperator("foo"));
    }

    @Test
    public void testCreateValue() throws GenericEntityException
    {
        final GenericValue gv2 = EntityUtils.createValue("Component", UtilMisc.toMap("id", new Long(20), "name", "baz"));
        assertEquals(new Long(20), gv2.getLong("id"));
        assertEquals("Component", gv2.getEntityName());
        assertEquals("baz", gv2.getString("name"));

        final GenericValue gv = EntityUtils.createValue("Component", UtilMisc.toMap("name", "bar"));
        assertEquals(new Long(1), gv.getLong("id"));
        assertEquals("Component", gv.getEntityName());
        assertEquals("bar", gv.getString("name"));
    }

    @Test
    public void testContainsEntity()
    {
        final GenericValue gv1 = new MockGenericValue("Component", UtilMisc.toMap("id", new Long(2),
            "name", "fred"));
        final GenericValue gv2 = new MockGenericValue("Component", UtilMisc.toMap("id", new Long(2),
            "name", "fred"));
        final GenericValue gv3 = new MockGenericValue("Component", UtilMisc.toMap("id", new Long(3),
            "name", "john"));

        assertTrue(EntityUtils.contains(UtilMisc.toList(gv1), gv1));
        assertTrue(EntityUtils.contains(UtilMisc.toList(gv1), gv2));
        assertFalse(EntityUtils.contains(UtilMisc.toList(gv1), gv3));
    }

    // always test identicals both ways around just to make sure
    @Test
    public void testIdentical()
    {
        assertTrue(EntityUtils.identical(null, null));

        final GenericValue entity1 = new MockGenericValue("TestEntity", UtilMisc.toMap("id", new Long(1)));
        assertTrue(!EntityUtils.identical(entity1, null));
        assertTrue(!EntityUtils.identical(null, entity1));
        assertTrue(EntityUtils.identical(entity1, entity1));

        final GenericValue entity2 = new MockGenericValue("TestEntity2", UtilMisc.toMap("id", new Long(1)));
        assertTrue(!EntityUtils.identical(entity1, entity2));
        assertTrue(!EntityUtils.identical(entity2, entity1));

        final GenericValue entity3 = new MockGenericValue("TestEntity", UtilMisc.toMap("id", new Long(2)));
        assertTrue(!EntityUtils.identical(entity1, entity3));
        assertTrue(!EntityUtils.identical(entity3, entity1));

        final GenericValue issue4 = new MockGenericValue("TestEntity", UtilMisc.toMap("id", new Long(1),
            "numvalue", new Long(4)));
        assertTrue(!EntityUtils.identical(entity1, issue4));
        assertTrue(!EntityUtils.identical(issue4, entity1));

        final GenericValue issue5 = new MockGenericValue("TestEntity", UtilMisc.toMap("id", new Long(1),
            "numvalue", null));
        assertTrue(EntityUtils.identical(entity1, issue5));
        assertTrue(EntityUtils.identical(issue5, entity1));

        final Timestamp ts = UtilDateTime.nowTimestamp();

        final GenericValue tsIssue = new MockGenericValue("TestEntity", UtilMisc.toMap("id", new Long(1),
            "created", ts));
        final GenericValue tsIssue2 = new MockGenericValue("TestEntity", UtilMisc.toMap("id", new Long(1),
            "created", ts));
        assertTrue(EntityUtils.identical(tsIssue, tsIssue2));
        assertTrue(EntityUtils.identical(tsIssue2, tsIssue));

        final Timestamp ts2 = new Timestamp(ts.getTime() - 100000);
        final GenericValue tsIssue3 = new MockGenericValue("TestEntity", UtilMisc.toMap("id", new Long(1),
            "created", ts2));
        assertTrue(!EntityUtils.identical(tsIssue, tsIssue3));
        assertTrue(!EntityUtils.identical(tsIssue3, tsIssue));
    }

    @Test
    public void testFilterByAnd()
    {
        assertNull(EntityUtils.filterByAnd(null, null));

        final GenericValue entity1 = new MockGenericValue("TestEntity", UtilMisc.toMap("id", new Long(1), "numvalue", new Long(4)));
        final GenericValue entity2 = new MockGenericValue("TestEntity", UtilMisc.toMap("id", new Long(2),
            "numvalue", new Long(5), "name", "bar"));

        final List issues = UtilMisc.toList(entity1, entity2);

        assertEquals(issues, EntityUtils.filterByAnd(issues, null));
        assertEquals(issues, EntityUtils.filterByAnd(issues, Collections.EMPTY_LIST));

        assertEquals(UtilMisc.toList(entity2), EntityUtils.filterByAnd(issues, UtilMisc.toList(new EntityExpr("name", EntityOperator.EQUALS, "bar"))));
        assertEquals(UtilMisc.toList(entity1), EntityUtils.filterByAnd(issues,
            UtilMisc.toList(new EntityExpr("name", EntityOperator.NOT_EQUAL, "bar"))));
        assertEquals(UtilMisc.toList(entity1), EntityUtils.filterByAnd(issues, UtilMisc.toList(new EntityExpr("numvalue", EntityOperator.LESS_THAN,
            new Long(5)))));
        assertEquals(UtilMisc.toList(entity1, entity2), EntityUtils.filterByAnd(issues, UtilMisc.toList(new EntityExpr("numvalue",
            EntityOperator.LESS_THAN_EQUAL_TO, new Long(5)))));
        assertEquals(UtilMisc.toList(entity2), EntityUtils.filterByAnd(issues, UtilMisc.toList(new EntityExpr("numvalue",
            EntityOperator.GREATER_THAN_EQUAL_TO, new Long(5)))));
        assertEquals(UtilMisc.toList(entity2), EntityUtils.filterByAnd(issues, UtilMisc.toList(new EntityExpr("numvalue",
            EntityOperator.GREATER_THAN, new Long(4)))));

        boolean exceptionThrown = false;

        try
        {
            EntityUtils.filterByAnd(issues, UtilMisc.toList(new EntityExpr("project", EntityOperator.IN, new Long(5))));
        }
        catch (final IllegalArgumentException e)
        {
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown);

        exceptionThrown = false;

        try
        {
            EntityUtils.filterByAnd(issues, UtilMisc.toList(new EntityExpr("summary", EntityOperator.GREATER_THAN, new Long(5))));
        }
        catch (final IllegalArgumentException e)
        {
            exceptionThrown = true;
        }
    }
}
