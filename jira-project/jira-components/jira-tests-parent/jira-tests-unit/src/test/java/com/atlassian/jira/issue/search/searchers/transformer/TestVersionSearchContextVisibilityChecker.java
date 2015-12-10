package com.atlassian.jira.issue.search.searchers.transformer;

import java.util.Set;

import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestVersionSearchContextVisibilityChecker extends MockControllerTestCase
{
    private VersionManager versionManager;
    private SearchContext searchContext;

    @Before
    public void setUp() throws Exception
    {
        versionManager = mockController.getMock(VersionManager.class);
        searchContext = mockController.getMock(SearchContext.class);
    }

    @Test
    public void testVisibleInContextIsVisible() throws Exception
    {
        final Version version1 = mockController.getMock(Version.class);
        final Version version2 = mockController.getMock(Version.class);

        final MockProject project1 = new MockProject(10L);
        final MockProject project2 = new MockProject(20L);

        searchContext.getProjectIds();
        mockController.setReturnValue(CollectionBuilder.newBuilder(10L).asList());

        versionManager.getVersion(5L);
        mockController.setReturnValue(version1);

        version1.getProjectObject();
        mockController.setReturnValue(project1);

        versionManager.getVersion(7L);
        mockController.setReturnValue(version2);

        version2.getProjectObject();
        mockController.setReturnValue(project2);

        mockController.replay();
        final VersionSearchContextVisibilityChecker checker = new VersionSearchContextVisibilityChecker(versionManager);
        final Set<String> result = checker.FilterOutNonVisibleInContext(searchContext, CollectionBuilder.newBuilder("5", "7", "ab").asList());
        assertEquals(CollectionBuilder.newBuilder("5").asSet(), result);
        mockController.verify();
    }

    @Test
    public void testVisibleInContextNoProjects() throws Exception
    {
        searchContext.getProjectIds();
        mockController.setReturnValue(CollectionBuilder.newBuilder().asList());

        mockController.replay();
        final VersionSearchContextVisibilityChecker checker = new VersionSearchContextVisibilityChecker(versionManager);
        final Set<String> result = checker.FilterOutNonVisibleInContext(searchContext, CollectionBuilder.newBuilder("5", "7", "ab").asList());
        assertEquals(CollectionBuilder.newBuilder().asSet(), result);
        mockController.verify();
    }
    
    @Test
    public void testVisibleInContextTwoProjects() throws Exception
    {
        searchContext.getProjectIds();
        mockController.setReturnValue(CollectionBuilder.newBuilder(10L, 20L).asList());

        mockController.replay();
        final VersionSearchContextVisibilityChecker checker = new VersionSearchContextVisibilityChecker(versionManager);
        final Set<String> result = checker.FilterOutNonVisibleInContext(searchContext, CollectionBuilder.newBuilder("5", "7", "ab").asList());
        assertEquals(CollectionBuilder.newBuilder().asSet(), result);
        mockController.verify();
    }

    @Test
    public void testVisibleInContextExcecption() throws Exception
    {
        searchContext.getProjectIds();
        mockController.setReturnValue(CollectionBuilder.newBuilder(10L).asList());

        versionManager.getVersion(5L);
        mockController.setReturnValue(null);

        mockController.replay();
        final VersionSearchContextVisibilityChecker checker = new VersionSearchContextVisibilityChecker(versionManager);
        final Set<String> result = checker.FilterOutNonVisibleInContext(searchContext, CollectionBuilder.newBuilder("5").asList());
        assertTrue(result.isEmpty());
        mockController.verify();
    }
}
