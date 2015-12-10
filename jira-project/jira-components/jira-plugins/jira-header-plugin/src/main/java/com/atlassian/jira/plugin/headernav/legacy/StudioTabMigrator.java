package com.atlassian.jira.plugin.headernav.legacy;

import com.atlassian.jira.plugin.headernav.customcontentlinks.CustomContentLinkServiceFactory;
import com.atlassian.jira.plugin.headernav.navlinks.spi.NavlinksProjectPermissionManager;
import com.atlassian.plugins.navlink.producer.contentlinks.customcontentlink.CustomContentLink;
import com.atlassian.plugins.navlink.producer.contentlinks.customcontentlink.CustomContentLinkService;
import com.atlassian.plugins.navlink.producer.contentlinks.customcontentlink.NoAdminPermissionException;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.project.ProjectManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * This component migrates project tabs from the StudioTabManager in the jira-ondemand-theme-plugin to the
 * CustomContentLinkService in the atlassian-nav-links-plugin.
 *
 * It exists only to see a startup event and perform the migration
 */
@Component
public class StudioTabMigrator implements LifecycleAware
{
    static final String MIGRATION_COMPLETE_KEY = "jira-header-plugin.studio-tab-migration-complete";
    private static final Logger log = LoggerFactory.getLogger(StudioTabMigrator.class);

    private final ReadOnlyStudioTabManager studioTabManager;
    private final ProjectManager projectManager;
    private final CustomContentLinkServiceFactory customContentLinkServiceFactory;
    private final PluginSettingsFactory pluginSettingsFactory;
    private final NavlinksProjectPermissionManager navlinksProjectPermissionManager;

    @Autowired
    public StudioTabMigrator(
            ReadOnlyStudioTabManager studioTabManager,
            ProjectManager projectManager,
            CustomContentLinkServiceFactory customContentLinkServiceFactory,
            PluginSettingsFactory pluginSettingsFactory,
            NavlinksProjectPermissionManager navlinksProjectPermissionManager
    )
    {
        this.studioTabManager = studioTabManager;
        this.projectManager = projectManager;
        this.customContentLinkServiceFactory = customContentLinkServiceFactory;
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.navlinksProjectPermissionManager = navlinksProjectPermissionManager;
    }

    @Override
    /**
     * Migrates custom studio tabs to the CustomContentLinkService in the atlassian-nav-links plugin
     */
    public void onStart()
    {
        PluginSettings globalSettings = pluginSettingsFactory.createGlobalSettings();
        if (globalSettings.get(MIGRATION_COMPLETE_KEY) == null) {
            CustomContentLinkService customContentLinkService = customContentLinkServiceFactory.getCustomContentLinkService();
            if (customContentLinkService != null) {
                try {
                    navlinksProjectPermissionManager.setSysAdmin(true);
                    log.info("Migrating Studio tabs to Content Links");
                    for (String projectKey : projectManager.getAllProjectKeys()) {
                        log.info("Migrating Studio tabs for project '" + projectKey + "'");
                        List<CustomContentLink> existingLinks = customContentLinkService.getCustomContentLinks(projectKey);
                        for (StudioTab tab : studioTabManager.getAllTabs(projectKey)) {
                            if (tab.getType().equals(StudioTab.StudioTabType.CUSTOM) && tab.isDisplayed()) {
                                CustomContentLink newLink = CustomContentLink.builder()
                                        .key(projectKey)
                                        .label(tab.getName())
                                        .url(tab.getUrl())
                                        .build();
                                if (!existingLinks.contains(newLink)) {
                                    try
                                    {
                                        customContentLinkService.addCustomContentLink(newLink);
                                    }
                                    catch (NoAdminPermissionException e)
                                    {
                                        log.error("Permission error migrating Studio Tabs", e);
                                    }
                                }
                            }
                        }
                    }
                } finally {
                    navlinksProjectPermissionManager.setSysAdmin(false);
                }
                globalSettings.put(MIGRATION_COMPLETE_KEY, "migrated"); // value is irrelevant, we just check nullness
            } else {
                log.warn("CustomContentLinkService not supplied by factory, not migrating tabs");
            }
        }
    }
}
