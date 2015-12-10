package com.atlassian.sal.jira.pluginsettings;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.PropertiesManager;
import com.atlassian.jira.propertyset.JiraPropertySetFactory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.util.concurrent.Supplier;
import com.atlassian.util.concurrent.Suppliers;
import com.opensymphony.module.propertyset.PropertySet;

public class JiraPluginSettingsFactory implements PluginSettingsFactory
{
    private final JiraPropertySetFactory jiraPropertySetFactory;
    private final ProjectManager projectManager;

    public JiraPluginSettingsFactory(JiraPropertySetFactory jiraPropertySetFactory, ProjectManager projectManager)
    {
        this.jiraPropertySetFactory = jiraPropertySetFactory;
        this.projectManager = projectManager;
    }

    public PluginSettings createSettingsForKey(final String key)
    {
        Supplier<? extends PropertySet> propertySet = null;
        if (key != null)
        {
            propertySet = Suppliers.memoize(LazyProjectMigratingPropertySet.create(projectManager, jiraPropertySetFactory,
                            jiraPropertySetFactory.buildCachingDefaultPropertySet(key), key));
        }
        else
        {
            propertySet = ComponentAccessor.getComponent(PropertiesManager.class).getPropertySetReference();
        }
        return new JiraPluginSettings(propertySet);
    }

    public PluginSettings createGlobalSettings()
    {
        return createSettingsForKey(null);
    }
}
