package com.atlassian.jira.web.component;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection;
import com.atlassian.plugin.web.descriptors.WebPanelModuleDescriptor;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Renders {@link com.atlassian.plugin.web.model.WebPanel} with the common JIRA module structure.  It
 * also makes the header links and dropdown links.
 *
 * @since v4.4
 */
public interface ModuleWebComponent
{
    /**
     * A convienence method that iterates over of the list of WebPanels and renders each.
     *
     * @param user The user that we are rendering these for
     * @param request The request that these web panels are being rendered in
     * @param webPanelModuleDescriptors The list of WebPanels being rendered
     * @param params The params to pass to the render
     * @return The rendered HTML
     */
    String renderModules(User user, HttpServletRequest request, List<WebPanelModuleDescriptor> webPanelModuleDescriptors, Map<String, Object> params);

    /**
     *
     * @param user The user that we are rendering these for
     * @param request The request that these web panels are being rendered in
     * @param webPanelModuleDescriptor The WebPanel to render
     * @param params The params to pass to the render
     * @return The rendered HTML
     */
    String renderModule(User user, HttpServletRequest request, WebPanelModuleDescriptor webPanelModuleDescriptor, Map<String, Object> params);
}
