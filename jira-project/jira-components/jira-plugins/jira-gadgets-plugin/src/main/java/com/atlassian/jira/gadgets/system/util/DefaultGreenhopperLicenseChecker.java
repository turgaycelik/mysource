package com.atlassian.jira.gadgets.system.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.atlassian.extras.common.LicenseException;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;

public class DefaultGreenhopperLicenseChecker implements GreenhopperLicenseChecker
{
    private final PluginAccessor pluginAccessor;
    private final FeatureManager featureManager;

    public DefaultGreenhopperLicenseChecker(final PluginAccessor pluginAccessor, final FeatureManager featureManager)
    {
        this.pluginAccessor = pluginAccessor;
        this.featureManager = featureManager;
    }

    @Override
    public boolean greenhopperIsActiveAndLicensed()
    {
        Plugin greenhopper = pluginAccessor.getEnabledPlugin("com.pyxis.greenhopper.jira");
        if (greenhopper == null)
        {
            return false;
        }
        // If we're BTF, it's enough that greenhopper is enabled
        if (!featureManager.isOnDemand())
        {
            return true;
        }
        ModuleDescriptor<?> moduleDescriptor = greenhopper.getModuleDescriptor("greenhopper-license-manager");
        if (moduleDescriptor == null)
        {
            return false;
        }
        try
        {
            Object licenseManager = moduleDescriptor.getModule();
            Method isLicensedAndActive = licenseManager.getClass().getDeclaredMethod("verify");
            isLicensedAndActive.invoke(licenseManager);
            return true;
        }
        // If something else goes wrong, it's safer for our purposes to assume we're not licensed
        catch (NoSuchMethodException e)
        {
            return false;
        }
        catch (InvocationTargetException e)
        {
            final Throwable cause = e.getCause();
            if (cause instanceof LicenseException)
            {
                // GH Throwing this exception signifies that there is no valid license
                return false;
            }
            throw new RuntimeException(cause);
        }
        catch (IllegalAccessException e)
        {
            return false;
        }
    }
}
