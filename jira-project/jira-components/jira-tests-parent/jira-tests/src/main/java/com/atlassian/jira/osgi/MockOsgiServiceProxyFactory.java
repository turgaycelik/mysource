package com.atlassian.jira.osgi;

import com.atlassian.applinks.host.OsgiServiceProxyFactory;
import com.atlassian.plugin.osgi.container.OsgiContainerManager;

/**
 * TODO: Document this class / interface here
 *
 * @since v4.3
 */
public class  MockOsgiServiceProxyFactory extends OsgiServiceProxyFactory
{
    public MockOsgiServiceProxyFactory()
    {
        super((OsgiContainerManager) null);
    }
}
