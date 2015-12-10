package com.atlassian.jira.plugin.navigation;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;

/**
 * Represents a default pluggable top navigation.
 *
 * @since v3.12
 */
public class DefaultPluggableTopNavigation implements PluggableTopNavigation
{

    private TopNavigationModuleDescriptor descriptor;

    public void init(TopNavigationModuleDescriptor descriptor)
    {
        this.descriptor = descriptor;
    }

    public String getHtml(HttpServletRequest request)
    {
        return descriptor.getTopNavigationHtml(request, Collections.EMPTY_MAP);
    }

}
