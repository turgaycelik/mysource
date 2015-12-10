package com.atlassian.jira.plugin.navigation;

import com.atlassian.annotations.PublicSpi;

import javax.servlet.http.HttpServletRequest;

/**
 * A plugin to render a top navigation bar in JIRA.
 *
 * @since v3.12
 */
@PublicSpi
public interface PluggableTopNavigation
{
    /**
     * This is called when the controling module descriptor is initialized and enabled.
     *
     * @param descriptor is the controling module descriptor.
     *
     * @since v3.12
     */
    void init(TopNavigationModuleDescriptor descriptor);

    /**
     * Get the HTML to present on screen.
     *
     * @param request the request that is asking for the html.
     *
     * @return the html to be rendered.
     *
     * @since v3.12
     */
    String getHtml(HttpServletRequest request);

}
