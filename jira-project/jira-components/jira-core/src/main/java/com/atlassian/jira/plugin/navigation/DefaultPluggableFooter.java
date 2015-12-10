package com.atlassian.jira.plugin.navigation;

import com.atlassian.core.util.map.EasyMap;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;

/**
 * Default implementation of {@link com.atlassian.jira.plugin.navigation.PluggableFooter}.
 *
 * @since v3.12
 */
public class DefaultPluggableFooter implements PluggableFooter
{
    private FooterModuleDescriptor descriptor;

    public void init(FooterModuleDescriptor descriptor)
    {
        this.descriptor = descriptor;
    }

    public String getFullFooterHtml(HttpServletRequest request)
    {
        return descriptor.getFooterHtml(request, Collections.EMPTY_MAP);
    }

    public String getSmallFooterHtml(HttpServletRequest request)
    {
        Map startingParams = EasyMap.build("smallFooter", Boolean.TRUE);
        return descriptor.getFooterHtml(request, startingParams);
    }
}
