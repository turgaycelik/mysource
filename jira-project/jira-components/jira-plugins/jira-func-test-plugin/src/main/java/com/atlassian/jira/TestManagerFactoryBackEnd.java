package com.atlassian.jira;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.atlassian.functest.junit.SpringAwareTestCase;
import com.atlassian.jira.issue.index.BulkOnlyIndexManager;
import com.atlassian.jira.issue.index.DefaultIndexManager;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.util.ImportUtils;

public class TestManagerFactoryBackEnd extends SpringAwareTestCase
{

    private static final Class<DefaultIndexManager> DEFAULT_INDEX_MANAGER = DefaultIndexManager.class;

    private final boolean issueIndexOriginal = ImportUtils.isIndexIssues();

    @Autowired
    private IssueIndexManager issueIndexManager;

    @Before
    public void onTestUp()
    {
        // expected start point
        ImportUtils.setIndexIssues(true);
    }

    @After
    public void onTestDown()
    {
        // recover to original settings
        ImportUtils.setIndexIssues(issueIndexOriginal);
    }

    @Test
    public void testGetCorrectIndexManagerImplementation()
    {
        assertProxyOf(issueIndexManager, DEFAULT_INDEX_MANAGER);

        ImportUtils.setIndexIssues(false);
        assertProxyOf(issueIndexManager, BulkOnlyIndexManager.class);

        ImportUtils.setIndexIssues(true);
        assertProxyOf(issueIndexManager, DEFAULT_INDEX_MANAGER);
    }

    private <T extends IssueIndexManager> void assertProxyOf(final T object, final Class<? extends T> expectedClass)
    {
        // Can't assert it's an instance of the given class because it's actually a proxy,
        // so we instead use the dodgy approach of testing the toString implementation.
        assertTrue("Actual toString = " + object.toString(), object.toString().contains(expectedClass.getSimpleName()));
    }
}
