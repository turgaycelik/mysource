package com.atlassian.jira.web.component;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSectionImpl;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.WebPanelModuleDescriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModuleWebComponentFieldsImpl implements ModuleWebComponentFields
{
    private final SimpleLinkManager simpleLinkManager;
    private final WebInterfaceManager webInterfaceManager;

    public ModuleWebComponentFieldsImpl(SimpleLinkManager simpleLinkManager, WebInterfaceManager webInterfaceManager)
    {
        this.simpleLinkManager = simpleLinkManager;
        this.webInterfaceManager = webInterfaceManager;
    }

    @Override
    public List<SimpleLink> getHeaderItems(String key, User user, JiraHelper helper)
    {
        return simpleLinkManager.getLinksForSection(key + "/header", user, helper);
    }

    @Override
    public List<WebPanelModuleDescriptor> getPanels(String key, Map<String, Object> params)
    {
        return webInterfaceManager.getDisplayableWebPanelDescriptors(key + "/panels", params);
    }

    @Override
    public List<SectionsAndLinks> getDropdownSections(String key, User user, JiraHelper helper)
    {
        return getSections(key + "/drop", user, helper);
    }

    /*
    * Get the sections and links for the dropdown
    */
    private List<SectionsAndLinks> getSections(String key, User user, JiraHelper helper)
    {
        final List<SectionsAndLinks> sections = new ArrayList<SectionsAndLinks>();

        final List<SimpleLink> defaultLinks = simpleLinkManager.getLinksForSection(key + "/default", user, helper);

        if (!defaultLinks.isEmpty())
        {
            sections.add(new SectionsAndLinks(new SimpleLinkSectionImpl("", null, null, null, "module-drop-default-section", null),
                    defaultLinks));
        }

        final List<SimpleLinkSection> sectionsForLocation = simpleLinkManager.getSectionsForLocation(key, user, helper);
        for (SimpleLinkSection simpleLinkSection : sectionsForLocation)
        {
            final List<SimpleLink> linksForSection = simpleLinkManager.getLinksForSection(key + "/" + simpleLinkSection.getId(), user, helper);
            if (!linksForSection.isEmpty())
            {
                sections.add(new SectionsAndLinks(simpleLinkSection, linksForSection));
            }
        }

        return sections;
    }
}
