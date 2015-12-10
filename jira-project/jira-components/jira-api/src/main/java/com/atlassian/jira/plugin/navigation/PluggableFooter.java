package com.atlassian.jira.plugin.navigation;

import com.atlassian.annotations.PublicSpi;

import javax.servlet.http.HttpServletRequest;

/**
 * A plugin to render the footer in JIRA.
 *
 * @since v3.12
 */
@PublicSpi
public interface PluggableFooter
{

    /**
     * This is called when the controling module descriptor is initialized and enabled.
     *
     * @param descriptor is the controling module descriptor.
     *
     * @since v3.12
     */
    void init(FooterModuleDescriptor descriptor);

    /**
     * Get the footer HTML to present on a page that utilizes 100% of the page width.
     *
     * @param request the request that is asking for the html.
     *
     * @return the html to be rendered.
     *
     * @since v3.12
     */
    String getFullFooterHtml(HttpServletRequest request);

    /**
     * Get the footer HTML to present on a page that does not utilize 100% of the page width.
     *
     * @param request the request that is asking for the html.
     *
     * @return the html to be rendered.
     *
     * @since v3.12
     */
    String getSmallFooterHtml(HttpServletRequest request);

}
