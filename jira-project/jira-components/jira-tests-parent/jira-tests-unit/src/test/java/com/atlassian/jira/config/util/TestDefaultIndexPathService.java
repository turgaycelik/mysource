package com.atlassian.jira.config.util;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.MockJiraServiceContext;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.mock.MockPermissionManager;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestDefaultIndexPathService
{
    @Rule
    public final RuleChain mockContainer = MockitoMocksInContainer.forTest(this);

    private JiraServiceContext ctx;
    private IndexPathService indexPathService;
    private MockPermissionManager permissionManager;
    private ApplicationProperties properties;
    @Mock @AvailableInContainer
    private PluginAccessor pluginAccessor;
    @Mock @AvailableInContainer
    private PluginEventManager pluginEventManager;

    private static final String INDEXPATH = "/a/crazy/index/path";
    private static final String ERROR_MESSAGE = "Only a user with the System Administrator Global Permission can get and set the Index path.";

    @Before
    public void setUp()
    {
        ctx = new MockJiraServiceContext();
        permissionManager = new MockPermissionManager();
        properties = new MockApplicationProperties();
        IndexPathManager indexPathManager = new IndexPathManager.PropertiesAdaptor(properties, null);
        properties.setOption(APKeys.JIRA_PATH_INDEX_USE_DEFAULT_DIRECTORY, false);
        properties.setString(APKeys.JIRA_PATH_INDEX, INDEXPATH);
        indexPathService = new DefaultIndexPathService(indexPathManager, permissionManager);
    }

    @Test
    public void testGetIndexRootPath()
    {
        permissionManager.setDefaultPermission(true);
        final String indexRootPath = indexPathService.getIndexRootPath(ctx);
        assertEquals(indexRootPath, INDEXPATH);

        permissionManager.setDefaultPermission(false);
        final String bad = indexPathService.getIndexRootPath(ctx);
        assertNull(bad);
        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        assertEquals(1, ctx.getErrorCollection().getErrorMessages().size());
        assertEquals(ERROR_MESSAGE, ctx.getErrorCollection().getErrorMessages().toArray()[0]);
    }

    @Test
    public void testGetIssueIndexPath()
    {
        permissionManager.setDefaultPermission(true);
        final String indexRootPath = indexPathService.getIssueIndexPath(ctx);
        assertEquals(indexRootPath, INDEXPATH + java.io.File.separator + "issues");

        permissionManager.setDefaultPermission(false);
        final String bad = indexPathService.getIssueIndexPath(ctx);
        assertNull(bad);
        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        assertEquals(1, ctx.getErrorCollection().getErrorMessages().size());
        assertEquals(ERROR_MESSAGE, ctx.getErrorCollection().getErrorMessages().toArray()[0]);
    }

    @Test
    public void testGetCommentIndexPath()
    {
        permissionManager.setDefaultPermission(true);
        final String indexRootPath = indexPathService.getCommentIndexPath(ctx);
        assertEquals(indexRootPath, INDEXPATH + java.io.File.separator + "comments");

        permissionManager.setDefaultPermission(false);
        final String bad = indexPathService.getCommentIndexPath(ctx);
        assertNull(bad);
        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        assertEquals(1, ctx.getErrorCollection().getErrorMessages().size());
        assertEquals(ERROR_MESSAGE, ctx.getErrorCollection().getErrorMessages().toArray()[0]);
    }

    @Test
    public void testGetPluginIndexRootPath()
    {
        permissionManager.setDefaultPermission(true);
        final String indexRootPath = indexPathService.getPluginIndexRootPath(ctx);
        assertEquals(indexRootPath, INDEXPATH + java.io.File.separator + "plugins");

        permissionManager.setDefaultPermission(false);
        final String bad = indexPathService.getPluginIndexRootPath(ctx);
        assertNull(bad);
        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        assertEquals(1, ctx.getErrorCollection().getErrorMessages().size());
        assertEquals(ERROR_MESSAGE, ctx.getErrorCollection().getErrorMessages().toArray()[0]);
    }

    @Test
    public void testGetSharedEntityIndexPath()
    {
        permissionManager.setDefaultPermission(true);
        final String indexRootPath = indexPathService.getSharedEntityIndexPath(ctx);
        assertEquals(indexRootPath, INDEXPATH + java.io.File.separator +"entities");

        permissionManager.setDefaultPermission(false);
        final String bad = indexPathService.getSharedEntityIndexPath(ctx);
        assertNull(bad);
        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        assertEquals(1, ctx.getErrorCollection().getErrorMessages().size());
        assertEquals(ERROR_MESSAGE, ctx.getErrorCollection().getErrorMessages().toArray()[0]);
    }

    @Test
    public void testSetIndexRootPath()
    {
        permissionManager.setDefaultPermission(true);
        final String newPath = "/new/string/path";
        indexPathService.setIndexRootPath(ctx, newPath);
        assertFalse(ctx.getErrorCollection().hasAnyErrors());
        final String indexRootPath = indexPathService.getIndexRootPath(ctx);
        assertEquals(indexRootPath, newPath);
        assertFalse(properties.getOption(APKeys.JIRA_PATH_INDEX_USE_DEFAULT_DIRECTORY));

        permissionManager.setDefaultPermission(false);
        indexPathService.setIndexRootPath(ctx, "/even/better/path");
        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        assertEquals(1, ctx.getErrorCollection().getErrorMessages().size());
        assertEquals(ERROR_MESSAGE, ctx.getErrorCollection().getErrorMessages().toArray()[0]);
        // ensure we didn't change the value
        assertEquals(properties.getString(APKeys.JIRA_PATH_INDEX), newPath);
    }

    @Test
    public void testSetUseJiraHomeIndexRoot()
    {
        // assert precondition -- must be false before we can test that the switch to true works
        assertFalse(properties.getOption(APKeys.JIRA_PATH_INDEX_USE_DEFAULT_DIRECTORY));
        permissionManager.setDefaultPermission(true);
        indexPathService.setUseDefaultDirectory(ctx);
        assertFalse(ctx.getErrorCollection().hasAnyErrors());
        assertTrue(properties.getOption(APKeys.JIRA_PATH_INDEX_USE_DEFAULT_DIRECTORY));
    }

    @Test
    public void testSetUseJiraHomeIndexRootFailure()
    {
        // assert precondition -- must be false before we can test that the switch to true works
        assertFalse(properties.getOption(APKeys.JIRA_PATH_INDEX_USE_DEFAULT_DIRECTORY));
        permissionManager.setDefaultPermission(false);
        indexPathService.setUseDefaultDirectory(ctx);
        assertTrue(ctx.getErrorCollection().hasAnyErrors());
        assertEquals(1, ctx.getErrorCollection().getErrorMessages().size());
        assertEquals(ERROR_MESSAGE, ctx.getErrorCollection().getErrorMessages().toArray()[0]);
        assertFalse(properties.getOption(APKeys.JIRA_PATH_INDEX_USE_DEFAULT_DIRECTORY));
    }
}
