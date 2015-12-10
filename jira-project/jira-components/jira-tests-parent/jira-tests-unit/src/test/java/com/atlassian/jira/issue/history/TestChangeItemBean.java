/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.history;

import java.sql.Timestamp;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestChangeItemBean
{
    @Test
    public void testChangeItemBean()
    {
        ChangeItemBean cib = new ChangeItemBean();
        cib.setFieldType(ChangeItemBean.STATIC_FIELD);
        cib.setField("a");
        cib.setFrom("b");
        cib.setFromString("c");
        cib.setTo("d");
        cib.setToString("e");
        final Timestamp created = new Timestamp(1);
        cib.setCreated(created);

        assertEquals("a", cib.getField());
        assertEquals("b", cib.getFrom());
        assertEquals("c", cib.getFromString());
        assertEquals("d", cib.getTo());
        assertEquals("e", cib.getToString());
        assertEquals(created, cib.getCreated());

        ChangeItemBean cib2 = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "a", "b", "c", "d", "e", created);
        assertTrue(cib.equals(cib));
        assertTrue(cib.toString().equals(cib.toString()));
        assertTrue(!cib.equals(this));

        assertTrue(cib.equals(cib2));

        assertEquals(cib.hashCode(), cib2.hashCode());

        ChangeItemBean cib3 = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "b", "c", "d", "e", "f", created);
        assertTrue(!cib.equals(cib3));

        ChangeItemBean cib4 = new ChangeItemBean();
        assertTrue(!cib.equals(cib4));

        ChangeItemBean cib5 = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "a", "b", "c", "d", "f", created);
        assertTrue(!cib.equals(cib5));

        ChangeItemBean cib6 = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "a", "b", "c", "e", "f", created);
        assertTrue(!cib.equals(cib6));

        ChangeItemBean cib7 = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "a", "b", "d", "e", "f", created);
        assertTrue(!cib.equals(cib7));

        ChangeItemBean cib8 = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "a", "c", "d", "e", "f", created);
        assertTrue(!cib.equals(cib8));

        ChangeItemBean cib9 = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "a", "b", "c", "d", "e", new Timestamp(10));
        assertTrue(!cib.equals(cib9));
    }
}
