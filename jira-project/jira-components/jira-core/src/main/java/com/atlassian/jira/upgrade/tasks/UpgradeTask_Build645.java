package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.PluginController;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;

import java.util.Map;

/**
 * Upgrade task hackery to ensure the bundled GreenHopper is enabled if a non-bundled GreenHopper was previously enabled.
 *
 * @since v4.4
 */
public class UpgradeTask_Build645 extends AbstractUpgradeTask
{
    private static final String GREENHOPPER_KEY = "jira.plugin.state-.com.pyxis.greenhopper.jira";

    public UpgradeTask_Build645()
    {
        super(false);
    }

    @Override
    public String getBuildNumber()
    {
        return "645";
    }

    @Override
    public String getShortDescription()
    {
        return "ensure the bundled GreenHopper is enabled if a non-bundled GreenHopper was previously enabled.";
    }

    @Override
    public void doUpgrade(boolean setupMode) throws Exception
    {
        ApplicationProperties applicationProperties = ComponentAccessor.getApplicationProperties();

        // If there is already an explicit setting for GreenHopper's plugin state then we don't want to change it.
        if (applicationProperties.getDefaultBackedString(GREENHOPPER_KEY) == null)
        {
            final Map<Object, Object> parameters = MapBuilder.newBuilder()
                    .add("delegator.name", "default")
                    .add("entityName", "GreenHopper")
                    .add("entityId", 1L).toMap();
            final PropertySet ofbizPs = PropertySetManager.getInstance("ofbiz", parameters);
            final String license = ofbizPs.getText("LICENSE");
            if (license != null)
            {
                final PluginController controller = ComponentAccessor.getComponent(PluginController.class);
                controller.enablePlugins("com.pyxis.greenhopper.jira");
            }
        }
    }
}
