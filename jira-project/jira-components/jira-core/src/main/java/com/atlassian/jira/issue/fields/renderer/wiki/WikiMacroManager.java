package com.atlassian.jira.issue.fields.renderer.wiki;

import com.atlassian.jira.plugin.renderer.MacroModuleDescriptor;
import com.atlassian.jira.util.JiraUrlCodec;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.renderer.macro.RadeoxCompatibilityMacro;
import com.atlassian.renderer.v2.macro.Macro;
import com.atlassian.renderer.v2.macro.MacroManager;
import com.atlassian.renderer.v2.macro.ResourceAware;
import com.atlassian.renderer.v2.macro.ResourceAwareMacroDecorator;

import java.util.Collection;

/**
 * Jira implementation of the MacroManager for the wiki renderer plugin.
 */
public class WikiMacroManager implements MacroManager
{
    private final PluginAccessor pluginAccessor;

    public WikiMacroManager(PluginAccessor pluginAccessor)
    {
        this.pluginAccessor = pluginAccessor;
    }

    public Macro getEnabledMacro(String name)
    {
        if (name == null)
        {
            return null;
        }

        for (final Object o : getEnabledMacroDescriptors())
        {
            MacroModuleDescriptor descriptor = (MacroModuleDescriptor) o;
            if (descriptor.getKey().equals(name))
            {
                return initMacro(descriptor.getModule(), descriptor);
            }
        }

        return null;
    }

    private Macro initMacro(Object obj, MacroModuleDescriptor descriptor)
    {
        Macro macro = null;
        if (obj instanceof org.radeox.macro.Macro)
        {
            macro = new RadeoxCompatibilityMacro((com.atlassian.renderer.macro.Macro) obj);
        }
        else if (obj instanceof Macro)
        {
            macro = (Macro) obj;
        }

        if (macro != null && descriptor != null)
        {
            if (!(macro instanceof ResourceAware))
            {
                macro = new ResourceAwareMacroDecorator(macro);
            }
            ((ResourceAware) macro).setResourcePath(JiraUrlCodec.encode(descriptor.getCompleteKey()));
        }

        return macro;
    }

    private Collection getEnabledMacroDescriptors()
    {
        return pluginAccessor.getEnabledModuleDescriptorsByClass(MacroModuleDescriptor.class);
    }
}