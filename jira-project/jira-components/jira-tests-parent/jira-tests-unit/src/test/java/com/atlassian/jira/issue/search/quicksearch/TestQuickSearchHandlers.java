package com.atlassian.jira.issue.search.quicksearch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.MockPermissionManager;
import com.atlassian.jira.mock.project.MockVersion;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventManager;

import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestQuickSearchHandlers
{
    @Rule
    public final RuleChain mockContainer = MockitoMocksInContainer.forTest(this);
    @org.mockito.Mock
    @AvailableInContainer
    private PluginAccessor pluginAccessor;
    @org.mockito.Mock
    @AvailableInContainer
    private PluginEventManager pluginEventManager;

    @Test
    public void testMyIssuesQuickSearchHandler()
    {
        QuickSearchResult quickSearchResult = new ModifiableQuickSearchResult("abc my def");
        MyIssuesQuickSearchHandler quickSearchHandler = new MyIssuesQuickSearchHandler();
        quickSearchHandler.modifySearchResult(quickSearchResult);
        assertEquals("issue_current_user", quickSearchResult.getSearchParameters("assigneeSelect").iterator().next());
        assertContains("abc", quickSearchResult.getSearchInput());
        assertContains("def", quickSearchResult.getSearchInput());
    }

    @Test
    public void testReporterQuickSearchHandler()
    {
        _testReporterQuickSearchHandler("r:me", "issue_current_user", null);
        _testReporterQuickSearchHandler("r:none", "issue_no_reporter", null);
        _testReporterQuickSearchHandler("r:sfarquhar", "specificuser", "sfarquhar");
        _testReporterQuickSearchHandler("r:username", "specificuser", "username");
        _testReporterQuickSearchHandler("r:userWith:colonInTheirName", "specificuser", "userWith:colonInTheirName");
    }

    private void _testReporterQuickSearchHandler(String searchTerm, String reporterSelectValue, String reporterValue)
    {
        QuickSearchResult quickSearchResult = new ModifiableQuickSearchResult("abc " + searchTerm + " def");
        ReporterQuickSearchHandler quickSearchHandler = new ReporterQuickSearchHandler();
        quickSearchHandler.modifySearchResult(quickSearchResult);
        assertEquals(reporterSelectValue, quickSearchResult.getSearchParameters("reporterSelect").iterator().next());
        if (reporterValue != null)
            assertEquals(reporterValue, quickSearchResult.getSearchParameters("reporter").iterator().next());
        assertContains("abc", quickSearchResult.getSearchInput());
        assertContains("def", quickSearchResult.getSearchInput());
    }

    @Test
    public void testFixForHandler()
    {
        QuickSearchResult quickSearchResult = new ModifiableQuickSearchResult("abc ff:version1 def");
        quickSearchResult.addSearchParameter("pid", "987");
        Version version = getVersion(123, "a version containing version1");

        VersionQuickSearchHandler quickSearchHandler = new FixForQuickSearchHandler(getVersionManager(EasyList.build(version)), getNullProjectManager(), getNullPermissionsManager(), getAuthenticationContext());
        quickSearchHandler.modifySearchResult(quickSearchResult);
        assertEquals("a version containing version1", quickSearchResult.getSearchParameters("fixfor").iterator().next());
        assertContains("abc", quickSearchResult.getSearchInput());
        assertContains("def", quickSearchResult.getSearchInput());
    }

    @Test
    public void testRaisedInHandler()
    {
        QuickSearchResult quickSearchResult = new ModifiableQuickSearchResult("abc v:version1 def");
        quickSearchResult.addSearchParameter("pid", "987");
        Version version = getVersion(123, "a version containing version1");

        VersionQuickSearchHandler quickSearchHandler = new RaisedInVersionQuickSearchHandler(getVersionManager(EasyList.build(version)), getNullProjectManager(), getNullPermissionsManager(), getAuthenticationContext());
        quickSearchHandler.modifySearchResult(quickSearchResult);
        assertEquals("a version containing version1", quickSearchResult.getSearchParameters("version").iterator().next());
        assertContains("abc", quickSearchResult.getSearchInput());
        assertContains("def", quickSearchResult.getSearchInput());
    }

    @Test
    public void testFixForHandlerMatchesOnlyWholeWords()
    {
        _testFixForHandler("abc ff:2.5 def", new String[] { "version 2.5" }, new String[] { "version 2.5.1" });
    }

    @Test
    public void testFixForHandlerMatchesRegexp()
    {
        _testFixForHandler("abc ff:2.5* def", new String[] { "2.5", "2.5.1", "2.5.2" }, new String[] { "2.4", "2.6" });
        _testFixForHandler("abc ff:2.1* def", new String[] { }, new String[] { "2.4", "2.6", "2.5", "2.5.1", "2.5.2" });
        _testFixForHandler("ff:1*", new String[] { "1.0", "1.1", "1" }, new String[] { "2.4", "2.6", "2.5", "2.5.1", "2.5.2" });
        _testFixForHandler("ff:2.?.1", new String[] { "2.4.1", "2.5.1", "2.6.1" }, new String[] { "2.4", "2.6", "2.5", "2.5.3", "2.5.2" });
        _testFixForHandler("ff:2.*", new String[] { "2.4.1", "2.*", "2.* Professional" }, new String[] { "1.0", "1.1", "3.2" });
        _testFixForHandler("ff:2.?", new String[] { "2.1", "2.?", "2.*" }, new String[] { "1.0", "1.1", "3.2", "1.*" });
    }

    private void _testFixForHandler(String searchQuery, String[] matchingVersionNames, String[] notMatchingVersionNames)
    {
        QuickSearchResult quickSearchResult = new ModifiableQuickSearchResult(searchQuery);
        quickSearchResult.addSearchParameter("pid", "987");
        List<Version> allVersions = new ArrayList<Version>();
        for (int i = 0; i < matchingVersionNames.length; i++)
        {
            String matchingVersionName = matchingVersionNames[i];
            allVersions.add(getVersion(i, matchingVersionName));
        }

        for (int i = 0; i < notMatchingVersionNames.length; i++)
        {
            String notMatchingVersionName = notMatchingVersionNames[i];
            allVersions.add(getVersion(i + 10000, notMatchingVersionName));
        }

        VersionQuickSearchHandler quickSearchHandler = new FixForQuickSearchHandler(getVersionManager(allVersions), getNullProjectManager(), getNullPermissionsManager(), getAuthenticationContext());
        quickSearchHandler.modifySearchResult(quickSearchResult);
        Collection searchParams = quickSearchResult.getSearchParameters("fixfor");
        if (searchParams == null)
        {
            searchParams = Collections.EMPTY_LIST;
        }

        assertEquals(matchingVersionNames.length, searchParams.size());
        for (String matchingVersionName : matchingVersionNames)
        {
            assertTrue(searchParams.contains(matchingVersionName));
        }
    }

    private Version getVersion(final long id, final String name)
    {
        Version version = new MockVersion()
        {
            public Long getId()
            {
                return new Long(id);
            }

            public String getName()
            {
                return name;
            }
        };
        return version;
    }

    private VersionManager getVersionManager(Collection versions)
    {
        Mock versionManagerMock = new Mock(VersionManager.class);
        versionManagerMock.expectAndReturn("getVersions", P.ANY_ARGS, versions);
        return (VersionManager) versionManagerMock.proxy();
    }

    private ProjectManager getNullProjectManager()
    {
        Mock projectManagerMock = new Mock(ProjectManager.class);
        projectManagerMock.expectAndReturn("getProjectByKey", P.ANY_ARGS, null);
        projectManagerMock.expectAndReturn("getProjectByName", P.ANY_ARGS, null);

        ProjectManager projectManager = (ProjectManager) projectManagerMock.proxy();
        return projectManager;
    }

    private PermissionManager getNullPermissionsManager()
    {
        PermissionManager permissionManager = new MockPermissionManager();
        return permissionManager;
    }

    private JiraAuthenticationContext getAuthenticationContext()
    {
        return new MockSimpleAuthenticationContext(null);
    }

    private static void assertContains(String match, String container)
    {
        assertTrue(container.indexOf(match) != -1);
    }
}
