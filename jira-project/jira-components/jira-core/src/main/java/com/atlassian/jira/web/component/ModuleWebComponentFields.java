package com.atlassian.jira.web.component;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection;
import javax.annotation.Nonnull;
import com.atlassian.plugin.web.descriptors.WebPanelModuleDescriptor;

import java.util.List;
import java.util.Map;

/**
 * Utility to extract fields from a {@link com.atlassian.plugin.web.model.WebPanel}.
 * </br>
 * Header links - {@link com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor}
 *     location = {webPanel.completeKey}/header
 * </br>
 * Default DropDown items - {@link com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor}
 *     location = {webPanel.completeKey}/drop/default
 * </br>
 * DropDown sections - {@link com.atlassian.plugin.web.descriptors.WebSectionModuleDescriptor}
 *     location - {webPanel.completeKey}/drop
 * </br>
 *     Items in those sections - {@link com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor}
 *         location = {webpanel.completeKey}/drop/{section.key}
 *
 * @since v5.2
 */
public interface ModuleWebComponentFields
{
    public static final String RENDER_PARAM_HEADLESS = "headless";
    public static final String RENDER_PARAM_CONTAINER_CLASS = "containerClass";
    public static final String RENDER_PARAM_PREFIX = "prefix";

    /**
     * Returns header links for the given module
     * @param key module descriptor key
     * @param user current user
     * @param helper The context under which the list is being generated
     * @return header links for the given module
     */
    public List<SimpleLink> getHeaderItems(@Nonnull String key, User user, @Nonnull JiraHelper helper);

    /**
     * Returns dropdown sections and links for the given module
     * @param key module descriptor key
     * @param user current user
     * @param helper The context under which the list is being generated
     * @return dropdown sections for the given module
     */
    public List<SectionsAndLinks> getDropdownSections(@Nonnull String key, User user, @Nonnull JiraHelper helper);

    /**
     * Returns panel descriptors for given module
     * @param key module descriptor key
     * @param params The params to pass to the render
     * @return panel descriptors for given module
     */
    public List<WebPanelModuleDescriptor> getPanels(@Nonnull String key, @Nonnull Map<String, Object> params);

    public class SectionsAndLinks
    {
        private final SimpleLinkSection section;
        private final List<SimpleLink> links;

        public SectionsAndLinks(SimpleLinkSection section, List<SimpleLink> links)
        {
            this.section = section;
            this.links = links;
        }

        public SimpleLinkSection getSection()
        {
            return section;
        }

        public List<SimpleLink> getLinks()
        {
            return links;
        }
    }
}
