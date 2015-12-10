package com.atlassian.jira.plugin.myjirahome;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.project.Project;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.plugin.web.model.WebLink;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Resolves the current My JIRA Home location by looking up the plugin and returning the rendered url. If the plugin is
 * not enabled, the {@link #DEFAULT_HOME_NOT_ANON} is returned for logged in users, or {@link #DEFAULT_HOME_OD_ANON} for anons.
 *
 * @since 5.1
 */
public class MyJiraHomeLinkerImpl implements MyJiraHomeLinker
{
    private final PluginAccessor pluginAccessor;
    private final MyJiraHomePreference myJiraHomePreference;
    private final ProjectService projectService;
    private final FeatureManager featureManager;

    public MyJiraHomeLinkerImpl(@Nonnull final PluginAccessor pluginAccessor, @Nonnull final MyJiraHomePreference myJiraHomePreference, @Nonnull final ProjectService projectService, @Nonnull final FeatureManager featureManager)
    {
        this.pluginAccessor = pluginAccessor;
        this.myJiraHomePreference = myJiraHomePreference;
        this.projectService = projectService;
        this.featureManager = featureManager;
    }

    @Nonnull
    @Override
    public String getHomeLink(@Nullable final User user)
    {
        final String completePluginModuleKey = myJiraHomePreference.findHome(user);
        try
        {
            // Avoid going through an IllegalArgumentException for empty plugin module key
            if (StringUtils.isEmpty(completePluginModuleKey))
            {
                return getDefaultHome(user);
            }

            if (!pluginAccessor.isPluginModuleEnabled(completePluginModuleKey))
            {
                return getDefaultHome(user);
            }

            final WebLink link = getWebLinkFromWebItemModuleDescriptor(completePluginModuleKey);
            if (link != null)
            {
                return link.getRenderedUrl(Collections.<String, Object>emptyMap());
            }
            else
            {
                return getDefaultHome(user);
            }
        }
        catch (IllegalArgumentException e)
        {
            return getDefaultHome(user);
        }
    }

    @Nullable
    private WebLink getWebLinkFromWebItemModuleDescriptor(@Nonnull final String completePluginModuleKey)
    {
        final WebItemModuleDescriptor webItemModuleDescriptor = getWebItemModuleDescriptorFromKey(completePluginModuleKey);
        if (webItemModuleDescriptor != null)
        {
            return webItemModuleDescriptor.getLink();
        }
        else
        {
            return null;
        }
    }

    @Nullable
    private WebItemModuleDescriptor getWebItemModuleDescriptorFromKey(@Nonnull final String completePluginModuleKey)
    {
        final ModuleDescriptor<?> pluginModule = pluginAccessor.getPluginModule(completePluginModuleKey);
        if (pluginModule instanceof WebItemModuleDescriptor)
        {
            return (WebItemModuleDescriptor) pluginModule;
        }
        else
        {
            return null;
        }
    }

    private String getDefaultHome(User user) {
        if (user == null)
        {
            // TODO Faster way of finding a single project which is anonymous viewable
            ServiceOutcome<List<Project>> allProjects = projectService.getAllProjects(user);
            if (allProjects.isValid() && allProjects.getReturnedValue().isEmpty() && featureManager.isOnDemand())
            {
                return DEFAULT_HOME_OD_ANON;
            }
        }
        return DEFAULT_HOME_NOT_ANON;
    }
}
