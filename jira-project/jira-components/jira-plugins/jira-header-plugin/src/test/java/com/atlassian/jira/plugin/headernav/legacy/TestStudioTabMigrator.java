package com.atlassian.jira.plugin.headernav.legacy;

import com.atlassian.jira.plugin.headernav.customcontentlinks.CustomContentLinkServiceFactory;
import com.atlassian.jira.plugin.headernav.navlinks.spi.NavlinksProjectPermissionManager;
import com.atlassian.plugins.navlink.producer.contentlinks.customcontentlink.CustomContentLink;
import com.atlassian.plugins.navlink.producer.contentlinks.customcontentlink.CustomContentLinkService;
import com.atlassian.plugins.navlink.producer.contentlinks.customcontentlink.NoAdminPermissionException;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.project.ProjectManager;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class TestStudioTabMigrator
{
    private CustomContentLinkServiceFactory customContentLinkServiceFactory = mock(CustomContentLinkServiceFactory.class);
    private ReadOnlyStudioTabManager readOnlyStudioTabManager = mock(ReadOnlyStudioTabManager.class);
    private ProjectManager projectManager = mock(ProjectManager.class);
    private CustomContentLinkService customContentLinkService = mock(CustomContentLinkService.class);
    private PluginSettingsFactory pluginSettingsFactory = mock(PluginSettingsFactory.class);
    private PluginSettings pluginSettings = mock(PluginSettings.class);
    private NavlinksProjectPermissionManager navlinksProjectPermissionManager = mock(NavlinksProjectPermissionManager.class);
    private StudioTabMigrator migrator = new StudioTabMigrator(readOnlyStudioTabManager, projectManager, customContentLinkServiceFactory, pluginSettingsFactory, navlinksProjectPermissionManager);

    @Before
    public void setup()
    {
        when(pluginSettingsFactory.createGlobalSettings()).thenReturn(pluginSettings);
    }

    private void setupMigration(DataSet dataSet)
    {
        when(pluginSettings.get(StudioTabMigrator.MIGRATION_COMPLETE_KEY)).thenReturn(dataSet.alreadyMigrated);
        when(customContentLinkServiceFactory.getCustomContentLinkService()).thenReturn(customContentLinkService);
        when(projectManager.getAllProjectKeys()).thenReturn(dataSet.projects);

        for (int i = 0; i < dataSet.projects.size(); ++i)
        {
            List<StudioTab> tabs = dataSet.tabsLists.get(i);
            String projectKey = dataSet.projects.get(i);
            when(readOnlyStudioTabManager.getAllTabs(projectKey)).thenReturn(tabs);
            when(customContentLinkService.getCustomContentLinks(projectKey))
                    .thenReturn(dataSet.existingLinks == null ? Collections.<CustomContentLink>emptyList() : dataSet.existingLinks.get(i));
        }
    }

    public void verifyMigration(DataSet dataSet) throws NoAdminPermissionException
    {
        int migrationCount = 0;
        for (int i = 0; i < dataSet.projects.size(); ++i)
        {
            String projectKey = dataSet.projects.get(i);
            verify(customContentLinkService).getCustomContentLinks(projectKey);
            List<StudioTab> tabs = dataSet.tabsLists.get(i);
            for (StudioTab tab : tabs)
            {
                if (tab.getType().equals(StudioTab.StudioTabType.CUSTOM) && tab.isDisplayed())
                {
                    CustomContentLink newLink = CustomContentLink.builder().key(projectKey).label(tab.getName()).url(tab.getUrl()).build();
                    if (dataSet.existingLinks == null || !dataSet.existingLinks.get(i).contains(newLink))
                    {
                        verify(customContentLinkService).addCustomContentLink(newLink);
                        ++migrationCount;
                    }
                }
            }
        }
        verifyNoMoreInteractions(customContentLinkService);
        verify(pluginSettings).put(StudioTabMigrator.MIGRATION_COMPLETE_KEY, "migrated");
        assertEquals(dataSet.numberExpectedToMigrate, migrationCount);
    }

    @Test
    public void testWithNoCustomContentLinkService()
    {
        when(customContentLinkServiceFactory.getCustomContentLinkService()).thenReturn(null);
        migrator.onStart();
        verifyZeroInteractions(projectManager, readOnlyStudioTabManager);
    }

    @Test
    public void testWithNoProjects()
    {
        DataSet dataSet = new DataSet(Collections.<String>emptyList(), Collections.<List<StudioTab>>emptyList(), 0);
        setupMigration(dataSet);
        migrator.onStart();
        verifyZeroInteractions(customContentLinkService, readOnlyStudioTabManager);
    }

    @Test
    public void testWithNoStudioTabs() throws NoAdminPermissionException
    {
        DataSet dataSet = new DataSet(
                Arrays.asList("FOO", "BAR"),
                Arrays.asList(Collections.<StudioTab>emptyList(), Collections.<StudioTab>emptyList()),
                0);
        setupMigration(dataSet);
        migrator.onStart();
        verifyMigration(dataSet);
    }

    @Test
    public void testWithTabs() throws NoAdminPermissionException
    {
        List<StudioTab> foos = Arrays.asList(
                new StudioTab("1", StudioTab.StudioTabType.CUSTOM, "footab1", "footab1url", true),
                new StudioTab("2", StudioTab.StudioTabType.CUSTOM, "footab2", "footab2url", true)
        );
        List<StudioTab> bars = Arrays.asList(new StudioTab("2", StudioTab.StudioTabType.CUSTOM, "bartab1", "bartab1url", true));
        DataSet dataSet = new DataSet(Arrays.asList("FOO", "BAR"), Arrays.asList(foos, bars), 3);
        setupMigration(dataSet);
        migrator.onStart();
        verifyMigration(dataSet);
    }

    @Test
    public void testWithTabsWhichShouldNotBeMigrated() throws NoAdminPermissionException
    {
        List<StudioTab> foos = Arrays.asList(
                new StudioTab("1", StudioTab.StudioTabType.HOME, "footab1", "footab1url", true),
                new StudioTab("2", StudioTab.StudioTabType.CUSTOM, "footab2", "footab2url", false)
        );
        List<StudioTab> bars = Arrays.asList(new StudioTab("2", StudioTab.StudioTabType.CUSTOM, "bartab1", "bartab1url", true));
        DataSet dataSet = new DataSet(Arrays.asList("FOO", "BAR"), Arrays.asList(foos, bars), 1);
        setupMigration(dataSet);
        migrator.onStart();
        verifyMigration(dataSet);
    }

    @Test
    public void testMigrationIsNotRepeated()
    {
        List<StudioTab> foos = Arrays.asList(
                new StudioTab("1", StudioTab.StudioTabType.CUSTOM, "footab1", "footab1url", true),
                new StudioTab("2", StudioTab.StudioTabType.CUSTOM, "footab2", "footab2url", true)
        );
        List<StudioTab> bars = Arrays.asList(new StudioTab("2", StudioTab.StudioTabType.CUSTOM, "bartab1", "bartab1url", true));
        DataSet dataSet = new DataSet(Arrays.asList("FOO", "BAR"), Arrays.asList(foos, bars), 3);
        dataSet.alreadyMigrated = true;
        setupMigration(dataSet);
        migrator.onStart();
        verifyZeroInteractions(projectManager, readOnlyStudioTabManager, customContentLinkService);
    }

    @Test
    // test that identical existing links (perhaps left over from a previous partial migration) don't get inserted again
    public void testLinksAreNotDuplicated() throws NoAdminPermissionException
    {
        List<StudioTab> foos = Arrays.asList(
                new StudioTab("1", StudioTab.StudioTabType.CUSTOM, "footab1", "footab1url", true),
                new StudioTab("2", StudioTab.StudioTabType.CUSTOM, "footab2", "footab2url", true)
        );
        List<StudioTab> bars = Arrays.asList(new StudioTab("2", StudioTab.StudioTabType.CUSTOM, "bartab1", "bartab1url", true));
        DataSet dataSet = new DataSet(Arrays.asList("FOO", "BAR"), Arrays.asList(foos, bars), 2);
        dataSet.existingLinks = Arrays.asList(
                Arrays.asList(
                        CustomContentLink.builder().key("FOO").label("footab1").url("footab1url").build(), // identical
                        CustomContentLink.builder().key("FOO").label("footab2").url("footab1urlx").build() // different URL
                ),
                Arrays.asList(
                        CustomContentLink.builder().key("BAR").label("bartab1x").url("bartab1url").build() // different label
                )
        );
        setupMigration(dataSet);
        migrator.onStart();
        verifyMigration(dataSet);
    }

    @Ignore
    private static class DataSet
    {

        final List<String> projects;
        final List<List<StudioTab>> tabsLists;
        final int numberExpectedToMigrate;
        public Boolean alreadyMigrated = null;
        public List<List<CustomContentLink>> existingLinks = null;

        public DataSet(List<String> projects, List<List<StudioTab>> tabsLists, int numberExpectedToMigrate)
        {
            this.numberExpectedToMigrate = numberExpectedToMigrate;
            assertTrue(projects.size() == tabsLists.size());

            this.projects = projects;
            this.tabsLists = tabsLists;
        }
    }

}
