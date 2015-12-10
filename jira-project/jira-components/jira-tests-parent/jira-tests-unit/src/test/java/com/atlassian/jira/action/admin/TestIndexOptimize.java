/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.action.admin;

import java.util.Collection;

import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.action.admin.IndexOptimize;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import webwork.action.Action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

public class TestIndexOptimize
{
    @Mock private IssueIndexManager mockIssueIndexManager;
    private IndexOptimize action;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        final MockI18nHelper i18nHelper = new MockI18nHelper();
        action = new IndexOptimize((IssueIndexManager) mockIssueIndexManager)
        {
            public String getRedirect(final String defaultUrl)
            {
                return defaultUrl;
            }

            @Override
            protected <T> T getComponentInstanceOfType(Class<T> clazz)
            {
                return null;
            }

            @Override
            protected I18nHelper getI18nHelper()
            {
                return i18nHelper;
            }
        };
    }

    @Test
    public void testNullInCtorDoesntWork()
    {
        try
        {
            new IndexOptimize(null) {
                @Override
                protected <T> T getComponentInstanceOfType(Class<T> clazz)
                {
                    return null;
                }
            };
            fail("NPE expected");
        }
        catch (NullPointerException yay)
        {
        }
    }

    @Test
    public void testGettersAndSetters()
    {
        assertEquals(0, action.getOptimizeTime());
        action.setOptimizeTime(123);
        assertEquals(123, action.getOptimizeTime());
        action.setOptimizeTime(123098765);
        assertEquals(123098765, action.getOptimizeTime());
    }

    @Test
    public void testValidationFailureIfIndexingDisabled() throws Exception
    {
        when(mockIssueIndexManager.isIndexAvailable()).thenReturn(false);

        assertFalse("Indexing is disabled", action.isIndexing());
        assertEquals(Action.INPUT, action.execute());
        Collection<?> errors = action.getErrorMessages();
        assertEquals(1, errors.size());
        assertEquals("admin.indexing.optimize.index.disabled", errors.iterator().next());
    }

    @Test
    public void testErrorIfIndexingLockFails() throws Exception
    {
        // Set up
        when(mockIssueIndexManager.isIndexAvailable()).thenReturn(true);
        when(mockIssueIndexManager.optimize()).thenReturn(-1L);

        assertTrue("Indexing is enabled", action.isIndexing());
        assertEquals(Action.ERROR, action.execute());
        Collection<?> errors = action.getErrorMessages();
        assertEquals(1, errors.size());
        assertEquals("admin.indexing.optimize.index.nolock", errors.iterator().next());
    }

    @Test
    public void testCorrectResult() throws Exception
    {
        // Set up
        when(mockIssueIndexManager.isIndexAvailable()).thenReturn(true);
        when(mockIssueIndexManager.optimize()).thenReturn(1234L);

        assertTrue("Indexing is enabled", action.isIndexing());
        assertEquals("IndexOptimize!default.jspa?optimizeTime=1234", action.execute());
        Collection<?> errors = action.getErrorMessages();
        assertEquals(0, errors.size());
    }
}
