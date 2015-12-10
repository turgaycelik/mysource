/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.issue.index;

import java.io.File;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.config.util.MockJiraHome;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.util.PathUtils;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestIndexPathPropertiesAdaptor
{

    private final String tempIndexDir = System.getProperty("java.io.tmpdir") + "/index/" + getClass().getName();

    private ApplicationProperties mockApplicationProperties = new MockApplicationProperties();

    @Before
    public void setUp() throws Exception
    {
        mockApplicationProperties.setOption(APKeys.JIRA_PATH_INDEX_USE_DEFAULT_DIRECTORY, false);
        mockApplicationProperties.setString(APKeys.JIRA_PATH_INDEX, tempIndexDir);
    }

    @Test
    public void testIndexRootPath()
    {
        final JiraHome mockJiraHome = new MockJiraHome("/jira-local-home/", "jira-shared-home");
        final IndexPathManager.PropertiesAdaptor indexPathManager = new IndexPathManager.PropertiesAdaptor(mockApplicationProperties, mockJiraHome);

        // Test with custom index directory
        assertEquals(tempIndexDir, new IndexPathManager.PropertiesAdaptor(mockApplicationProperties, null).getIndexRootPath());

        // Test with default index directory
        mockApplicationProperties.setOption(APKeys.JIRA_PATH_INDEX_USE_DEFAULT_DIRECTORY, true);
        assertEquals(new File("/jira-local-home/caches/indexes").getAbsolutePath(), indexPathManager.getIndexRootPath());
    }

    @Test
    public void testDefaultIndexRootPath()
    {
        final JiraHome mockJiraHome = new MockJiraHome("/jira-home/");
        final IndexPathManager.PropertiesAdaptor indexPathManager = new IndexPathManager.PropertiesAdaptor(mockApplicationProperties, mockJiraHome);

        assertEquals(new File("/jira-home/caches/indexes").getAbsolutePath(), indexPathManager.getDefaultIndexRootPath());
    }

    @Test
    public void testIssueIndexPath()
    {
        assertEquals(tempIndexDir + File.separator + "issues", new IndexPathManager.PropertiesAdaptor(mockApplicationProperties, null).getIssueIndexPath());
    }

    @Test
    public void testCommentIndexPath()
    {
        assertEquals(tempIndexDir + File.separator + "comments", new IndexPathManager.PropertiesAdaptor(mockApplicationProperties, null).getCommentIndexPath());
    }

    @Test
    public void testPluginIndexRootPath()
    {
        assertEquals(tempIndexDir + File.separator + "plugins", new IndexPathManager.PropertiesAdaptor(mockApplicationProperties, null).getPluginIndexRootPath());
    }

    @Test
    public void testAppendFileSeparatorChar()
    {
        assertEquals("dir" + File.separator, PathUtils.appendFileSeparator("dir"));
        assertEquals("dir" + File.separator, PathUtils.appendFileSeparator("dir" + File.separator));
        assertEquals(File.separator, PathUtils.appendFileSeparator(""));
    }
}
