package com.atlassian.jira.applinks;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.api.TypeNotInstalledException;
import com.atlassian.applinks.host.OsgiServiceProxyFactory;
import com.atlassian.jira.extension.Startable;

/**
 * This class delegates to the ApplicationLinkService provided by UAL.
 * Because this ApplicationLinkService lives within the bundled applinks plugin, we have to use
 * the OsgiServiceProxyFactory to get a reference to this service, after the plugin system has finished starting up.
 *
 * @since v4.3
 */
public class JiraApplicationLinkService implements ApplicationLinkService, Startable
{
    private ApplicationLinkService applicationLinkService;
    private final OsgiServiceProxyFactory osgiServiceProxyFactory;

    public JiraApplicationLinkService(OsgiServiceProxyFactory osgiServiceProxyFactory)
    {
        this.osgiServiceProxyFactory = osgiServiceProxyFactory;
    }

    @Override
    public ApplicationLink getApplicationLink(ApplicationId id) throws TypeNotInstalledException
    {
        return applicationLinkService.getApplicationLink(id);
    }

    @Override
    public Iterable<ApplicationLink> getApplicationLinks()
    {
        return applicationLinkService.getApplicationLinks();
    }

    @Override
    public Iterable<ApplicationLink> getApplicationLinks(Class<? extends ApplicationType> type)
    {
        return applicationLinkService.getApplicationLinks(type);
    }

    @Override
    public ApplicationLink getPrimaryApplicationLink(Class<? extends ApplicationType> type)
    {
        return applicationLinkService.getPrimaryApplicationLink(type);
    }

    @Override
    public void start() throws Exception
    {
        applicationLinkService = osgiServiceProxyFactory.createProxy(ApplicationLinkService.class, JiraAppLinksHostApplication.TIMEOUT);
    }
}
