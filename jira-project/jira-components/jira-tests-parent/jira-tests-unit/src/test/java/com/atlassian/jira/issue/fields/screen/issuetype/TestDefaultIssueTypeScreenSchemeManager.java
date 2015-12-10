package com.atlassian.jira.issue.fields.screen.issuetype;

import com.atlassian.cache.CacheManager;
import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.jira.association.NodeAssociationStore;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.mock.MockConstantsManager;
import com.atlassian.jira.mock.issue.fields.screen.issuetype.MockIssueTypeScreenScheme;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestDefaultIssueTypeScreenSchemeManager
{
    private IssueTypeScreenSchemeManager manager;

    @Before
    public void setup()
    {
        MockOfBizDelegator delegator = new MockOfBizDelegator();
        MockConstantsManager constantsManager = new MockConstantsManager();
        FieldScreenSchemeManager fieldScreenSchemeManager = null;
        NodeAssociationStore nodeAssociationStore = null;
        CacheManager cacheManager = new MemoryCacheManager();

        manager = new DefaultIssueTypeScreenSchemeManager(delegator, constantsManager, fieldScreenSchemeManager, nodeAssociationStore, cacheManager);
    }

    /**
     * Retrieve by long ID that does not exist should return null.
     */
    @Test
    public void testGetNonExistent()
    {
        IssueTypeScreenScheme result = manager.getIssueTypeScreenScheme(12345L);
        assertNull("Should not find scheme.", result);
    }

    /**
     * A previously saved scheme should be retrieved by get.
     */
    @Test
    public void testSaveAndGet()
    {
        Long schemeId = 12345L;
        IssueTypeScreenScheme scheme = new MockIssueTypeScreenScheme(schemeId, "Galah", "Galah Scheme");
        manager.createIssueTypeScreenScheme(scheme);

        IssueTypeScreenScheme retrieved = manager.getIssueTypeScreenScheme(schemeId);
        assertNotNull("Scheme not found.", retrieved);
        assertEquals("Wrong ID.", schemeId, retrieved.getId());
        assertEquals("Wrong name.", "Galah", retrieved.getName());
    }
}
