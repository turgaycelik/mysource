package com.atlassian.jira.web.action.admin.index;

import com.atlassian.jira.util.SimpleErrorCollection;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/*
 * @since v3.13
 */

public class TestIndexCommandResult
{
    private static final int REINDEX_TIME = 50;
    private static final String ERRROR_MESSAGE = "ERRROR MESSAGE";

    @Test
    public void testTestIsOK() throws Exception
    {
        IndexCommandResult result = new IndexCommandResult(REINDEX_TIME);
        assertTrue(result.isSuccessful());

        SimpleErrorCollection collection = new SimpleErrorCollection();
        collection.addErrorMessage(ERRROR_MESSAGE);

        result = new IndexCommandResult(collection);
        assertFalse(result.isSuccessful());
    }

    @Test
    public void testConstructor() throws Exception
    {
        IndexCommandResult result = new IndexCommandResult(REINDEX_TIME);
        assertEquals(REINDEX_TIME, result.getReindexTime());
        assertNotNull(result.getErrorCollection());
        assertFalse(result.getErrorCollection().hasAnyErrors());
        assertTrue(result.isSuccessful());


        SimpleErrorCollection collection = new SimpleErrorCollection();
        collection.addErrorMessage(ERRROR_MESSAGE);
        result = new IndexCommandResult(collection);
        assertEquals(0, result.getReindexTime());
        assertFalse(result.isSuccessful());
        assertEquals(collection, result.getErrorCollection());

        try
        {
            new IndexCommandResult(null);
            fail("Should not accept null collection.");
        }
        catch (RuntimeException e)
        {
            //expected
        }

    }
}
