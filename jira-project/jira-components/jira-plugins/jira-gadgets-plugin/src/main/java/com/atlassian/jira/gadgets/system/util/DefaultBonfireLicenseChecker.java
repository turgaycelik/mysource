package com.atlassian.jira.gadgets.system.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.atlassian.jira.config.FeatureManager;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;

public class DefaultBonfireLicenseChecker implements BonfireLicenseChecker
{
    private final PluginAccessor pluginAccessor;
    private final FeatureManager featureManager;

    public DefaultBonfireLicenseChecker(final PluginAccessor pluginAccessor, final FeatureManager featureManager)
    {
        this.pluginAccessor = pluginAccessor;
        this.featureManager = featureManager;
    }

    @Override
    public boolean bonfireIsActiveAndLicensed()
    {
        Plugin bonfire = pluginAccessor.getEnabledPlugin("com.atlassian.bonfire.plugin");
        if (bonfire == null)
        {
            return false;
        }
        // If we're BTF, it's enough that bonfire is enabled
        if (!featureManager.isOnDemand())
        {
            return true;
        }
        ModuleDescriptor<?> moduleDescriptor = bonfire.getModuleDescriptor("bonfire-license-service");
        if (moduleDescriptor == null)
        {
            return false;
        }
        Object bonfireLicenseService = moduleDescriptor.getModule();
        try
        {
            final Method isBonfireActivated = bonfireLicenseService.getClass().getDeclaredMethod("isBonfireActivated");
            return (Boolean) isBonfireActivated.invoke(bonfireLicenseService);
        }
        // If something goes wrong, it's safest for our purposes to assume we're not licensed
        catch (IllegalAccessException e)
        {
            return false;
        }
        catch (InvocationTargetException e)
        {
            return false;
        }
        catch (NoSuchMethodException e)
        {
            return false;
        }
    }
}
