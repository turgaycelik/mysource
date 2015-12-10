/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Aug 19, 2004
 * Time: 7:08:44 PM
 */
package com.atlassian.jira.util;

import java.util.EnumSet;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestSimpleErrorCollection
{
    @Test
    public void testHasAnyErrors()
    {
        ErrorCollection col = new SimpleErrorCollection();
        assertFalse(col.hasAnyErrors());
        col.addError("foo", "bar");
        assertTrue(col.hasAnyErrors());

        col = new SimpleErrorCollection();
        assertFalse(col.hasAnyErrors());
        col.addErrorMessage("message");
        assertTrue(col.hasAnyErrors());

        col = new SimpleErrorCollection();
        assertFalse(col.hasAnyErrors());
        col.addErrorMessage("message");
        col.addError("foo", "bar");
        assertTrue(col.hasAnyErrors());
    }

    @Test
    public void testReasons()
    {
        ErrorCollection col = new SimpleErrorCollection();
        assertFalse(col.hasAnyErrors());
        col.addError("foo", "bar", ErrorCollection.Reason.FORBIDDEN);
        assertTrue(col.hasAnyErrors());
        assertTrue(col.getReasons().contains(ErrorCollection.Reason.FORBIDDEN));

        col = new SimpleErrorCollection();
        assertFalse(col.hasAnyErrors());
        col.addError("foo", "bar");
        col.addReason(ErrorCollection.Reason.FORBIDDEN);
        assertTrue(col.hasAnyErrors());
        assertTrue(col.getReasons().contains(ErrorCollection.Reason.FORBIDDEN));

        col = new SimpleErrorCollection();
        assertFalse(col.hasAnyErrors());
        col.addError("foo", "bar");
        col.setReasons(EnumSet.of(ErrorCollection.Reason.FORBIDDEN));
        assertTrue(col.hasAnyErrors());
        assertTrue(col.getReasons().contains(ErrorCollection.Reason.FORBIDDEN));

        col = new SimpleErrorCollection();
        assertFalse(col.hasAnyErrors());
        col.addErrorMessage("message", ErrorCollection.Reason.FORBIDDEN);
        col.addError("foo", "bar", ErrorCollection.Reason.VALIDATION_FAILED);
        assertTrue(col.hasAnyErrors());
        assertTrue(col.getReasons().contains(ErrorCollection.Reason.FORBIDDEN));
        assertTrue(col.getReasons().contains(ErrorCollection.Reason.VALIDATION_FAILED));

    }
}
