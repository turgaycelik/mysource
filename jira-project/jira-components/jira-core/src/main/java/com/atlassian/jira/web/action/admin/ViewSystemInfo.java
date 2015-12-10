/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.atlassian.core.ofbiz.util.OFBizPropertyUtils;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.LocaleManager;
import com.atlassian.jira.startup.PluginInfo;
import com.atlassian.jira.startup.PluginInfoProvider;
import com.atlassian.jira.startup.PluginInfos;
import com.atlassian.jira.util.system.ExtendedSystemInfoUtils;
import com.atlassian.jira.util.system.ExtendedSystemInfoUtilsImpl;
import com.atlassian.jira.util.system.check.SystemEnvironmentChecklist;
import com.atlassian.jira.util.system.patch.AppliedPatchInfo;
import com.atlassian.jira.util.system.patch.AppliedPatches;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import com.opensymphony.module.propertyset.PropertySet;

import org.ofbiz.core.entity.GenericValue;

@SuppressWarnings ({ "UnusedDeclaration" })
@WebSudoRequired
public class ViewSystemInfo extends JiraWebActionSupport
{
    private final ExtendedSystemInfoUtils extendedSystemInfoUtils;
    private List<String> warningMessages;
    private final LocaleManager localeManager;
    private final PluginInfoProvider pluginInfoProvider;
    private final FeatureManager featureManager;

    public ViewSystemInfo(LocaleManager localeManager, final PluginInfoProvider pluginInfoProvider, FeatureManager featureManager)
    {
        this.localeManager = localeManager;
        this.pluginInfoProvider = pluginInfoProvider;
        this.featureManager = featureManager;
        this.extendedSystemInfoUtils = new ExtendedSystemInfoUtilsImpl(this);
    }

    public String doGarbageCollection()
    {
        System.gc();
        return SUCCESS;
    }

    public ExtendedSystemInfoUtils getExtendedSystemInfoUtils()
    {
        return extendedSystemInfoUtils;
    }

    public LocaleManager getLocaleManager()
    {
        return localeManager;
    }

    public List<String> getWarningMessages()
    {
        if (warningMessages == null)
        {
            warningMessages = SystemEnvironmentChecklist.getWarningMessages(getLocale(), true);
        }
        return warningMessages;
    }

    public String getDisplayNameOfLocale(Locale locale)
    {
        return locale.getDisplayName(getLocale());
    }

    /**
     * For each application properties, display the key and its value. If the value is a path, then break the paths at
     * semi colons ';' (For non-windows operatin systems, breaks on colon ':' as well). The break is accomplished by
     * adding the HTML &lt;br&gt; tag.
     *
     * @return Map of system property keys to its value
     */
    public Map getApplicationPropertiesHTML()
    {
        if (featureManager.isOnDemand())
        {
            return Collections.emptyMap();
        }
        else
        {
            return extendedSystemInfoUtils.getApplicationPropertiesFormatted("<br>");
        }
    }

    /**
     * For each system properties, display the key and its value. If the value is a path, then break the paths at semi
     * colons ';' (For non-windows operatin systems, breaks on colon ':' as well). The break is accomplished by adding
     * the HTML &lt;br&gt; tag.
     *
     * @return Map of system property keys to its value
     */
    public Map getSystemPropertiesHTML()
    {
        if (featureManager.isOnDemand())
        {
            return Collections.emptyMap();
        }
        else
        {
            return extendedSystemInfoUtils.getSystemPropertiesFormatted("<br>");
        }
    }

    public Set<AppliedPatchInfo> getAppliedPatches()
    {
        return AppliedPatches.getAppliedPatches();
    }

    public PluginInfos getUserPlugins()
    {
        return pluginInfoProvider.getUserPlugins();
    }

    public Iterator<PluginInfo> getSystemPlugins()
    {
        return pluginInfoProvider.getSystemPlugins().iterator();
    }

    public PropertySet getPropertySet(GenericValue gv)
    {
        return OFBizPropertyUtils.getPropertySet(gv);
    }
}