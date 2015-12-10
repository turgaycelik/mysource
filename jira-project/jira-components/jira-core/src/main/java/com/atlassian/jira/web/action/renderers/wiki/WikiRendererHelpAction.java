package com.atlassian.jira.web.action.renderers.wiki;

import com.atlassian.jira.plugin.contentlinkresolver.ContentLinkResolverDescriptor;
import com.atlassian.jira.plugin.renderer.MacroModuleDescriptor;
import com.atlassian.jira.plugin.renderercomponent.RendererComponentFactoryDescriptor;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.PluginAccessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/** A webwork action that controls the display of the wiki renderers help. */
public class WikiRendererHelpAction extends JiraWebActionSupport
{
    private String section;
    private PluginAccessor pluginAccessor;
    private HashMap macrosWithHelpBySection;
    private ArrayList macrosWithHelpNoSection;
    private HashMap contentLinksWithHelpBySection;
    private ArrayList contentLinksWithHelpNoSection;
    private HashMap rendererComponentFactoriesWithHelpBySection;
    private ArrayList rendererComponentFactoriesWithHelpNoSection;

    public WikiRendererHelpAction(PluginAccessor pluginAccessor)
    {
        this.pluginAccessor = pluginAccessor;
    }

    protected String doExecute() throws Exception
    {
        return "success";
    }

    public String getSection()
    {
        if (section == null)
        {
            return "index";
        }
        return section;
    }

    public void setSection(String section)
    {
        this.section = section;
    }

    /**
     * Gets any help text that have been registered from macro plugins for the specified section.
     *
     * @param section the help section you want to render
     * @return matching help modules
     */
    public Collection getMacroHelpForSection(String section)
    {
        if (macrosWithHelpBySection == null)
        {
            loadMacroHelp();
        }

        return (Collection) macrosWithHelpBySection.get(section);
    }

    /**
     * Gets any help text that have been registered from macro plugins without specifying a section.
     *
     * @return matching help modules
     */
    public Collection getMacroHelpNoSection()
    {
        if (macrosWithHelpNoSection == null)
        {
            loadMacroHelp();
        }

        return macrosWithHelpNoSection;
    }

    /**
     * Gets any help text that have been registered from content link plugins for the specified section.
     *
     * @param section the help section you want to render
     * @return matching help modules
     *
     * @since v3.12
     */
    public Collection getContentLinkHelpForSection(String section)
    {
        if (contentLinksWithHelpBySection == null)
        {
            loadContentLinkHelp();
        }

        return (Collection) contentLinksWithHelpBySection.get(section);
    }

    /**
     * Gets any help text that have been registered from content link plugins without specifying a section.
     *
     * @return matching help modules
     *
     * @since v3.12
     */
    public Collection getContentLinkHelpNoSection()
    {
        if (contentLinksWithHelpNoSection == null)
        {
            loadContentLinkHelp();
        }

        return contentLinksWithHelpNoSection;
    }

    /**
     * Gets any help text that have been registered from renderer component plugins for the specified section.
     *
     * @param section the help section you want to render
     * @return matching help modules
     *
     * @since v3.12
     */
    public Collection getRendererComponentFactoriesHelpForSection(String section)
    {
        if (rendererComponentFactoriesWithHelpBySection == null)
        {
            loadRendererComponentFactoriesHelp();
        }

        return (Collection) rendererComponentFactoriesWithHelpBySection.get(section);
    }

    /**
     * Gets any help text that have been registered from renderer component plugins without specifying a section.
     *
     * @return matching help modules
     *
     * @since v3.12
     */
    public Collection getRendererComponentFactoriesHelpNoSection()
    {
        if (rendererComponentFactoriesWithHelpNoSection == null)
        {
            loadRendererComponentFactoriesHelp();
        }

        return rendererComponentFactoriesWithHelpNoSection;
    }

    private void loadMacroHelp()
    {
        macrosWithHelpBySection = new HashMap();
        macrosWithHelpNoSection = new ArrayList();

        List<MacroModuleDescriptor> macroDescriptors = pluginAccessor.getEnabledModuleDescriptorsByClass(MacroModuleDescriptor.class);
        for (final MacroModuleDescriptor descriptor : macroDescriptors)
        {
            if (descriptor.hasHelp())
            {
                if (descriptor.getHelpSection() == null)
                {
                    macrosWithHelpNoSection.add(descriptor);
                }
                else
                {
                    List list = (List) macrosWithHelpBySection.get(descriptor.getHelpSection());
                    if (list == null)
                    {
                        list = new ArrayList();
                    }

                    list.add(descriptor);
                    macrosWithHelpBySection.put(descriptor.getHelpSection(), list);
                }
            }
        }
    }

    private void loadContentLinkHelp()
    {
        contentLinksWithHelpBySection = new HashMap();
        contentLinksWithHelpNoSection = new ArrayList();

        List<ContentLinkResolverDescriptor> contentLinkDescriptors = pluginAccessor.getEnabledModuleDescriptorsByClass(ContentLinkResolverDescriptor.class);
        for (final ContentLinkResolverDescriptor descriptor : contentLinkDescriptors)
        {
            if (descriptor.hasHelp())
            {
                if (descriptor.getHelpSection() == null)
                {
                    contentLinksWithHelpNoSection.add(descriptor);
                }
                else
                {
                    List list = (List) contentLinksWithHelpBySection.get(descriptor.getHelpSection());
                    if (list == null)
                    {
                        list = new ArrayList();
                    }

                    list.add(descriptor);
                    contentLinksWithHelpBySection.put(descriptor.getHelpSection(), list);
                }
            }
        }
    }

    private void loadRendererComponentFactoriesHelp()
    {
        rendererComponentFactoriesWithHelpBySection = new HashMap();
        rendererComponentFactoriesWithHelpNoSection = new ArrayList();

        List<RendererComponentFactoryDescriptor> descriptors = pluginAccessor.getEnabledModuleDescriptorsByClass(RendererComponentFactoryDescriptor.class);
        for (final RendererComponentFactoryDescriptor descriptor : descriptors)
        {
            if (descriptor.hasHelp())
            {
                if (descriptor.getHelpSection() == null)
                {
                    rendererComponentFactoriesWithHelpNoSection.add(descriptor);
                }
                else
                {
                    List list = (List) rendererComponentFactoriesWithHelpBySection.get(descriptor.getHelpSection());
                    if (list == null)
                    {
                        list = new ArrayList();
                    }

                    list.add(descriptor);
                    rendererComponentFactoriesWithHelpBySection.put(descriptor.getHelpSection(), list);
                }
            }
        }
    }
}
