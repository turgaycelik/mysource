package com.atlassian.jira.util;

import org.junit.Test;
import org.ofbiz.core.entity.GenericEntityException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/**
 * Unit test for JiraVelocityHelper.
 */
public class TestJiraVelocityHelper
{

    private static final JiraVelocityHelper VELOCITY_HELPER = new JiraVelocityHelper(null);

    /**
     * This is a simple test that makes sure that this method will not explode when passed a null
     * GenericValue.
     */
    @Test
    public void testWasDeletedHandlesNull()
    {
        try
        {
            assertFalse(VELOCITY_HELPER.wasDeleted(null, "fake value", null));
        }
        catch (GenericEntityException e)
        {
            fail();
        }
    }

}
