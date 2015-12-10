package com.atlassian.jira.applinks;

import com.atlassian.applinks.api.EntityLink;
import com.atlassian.applinks.api.EntityLinkService;
import com.atlassian.applinks.api.EntityType;
import com.atlassian.applinks.host.OsgiServiceProxyFactory;
import com.atlassian.jira.extension.Startable;

/**
 * This class delegates to the EntityLinkService provided by UAL.
 * Because this EntityLinkService lives within the bundled applinks plugin, we have to use
 * the OsgiServiceProxyFactory to get a reference to this service, after the plugin system has finished starting up.
 *
 * @since v4.3
 */
public class JiraEntityLinkService implements EntityLinkService, Startable
{
    private EntityLinkService entityLinkService;
    private final OsgiServiceProxyFactory osgiServiceProxyFactory;

    public JiraEntityLinkService(OsgiServiceProxyFactory osgiServiceProxyFactory)
    {
        this.osgiServiceProxyFactory = osgiServiceProxyFactory;
    }

    @Override
    public Iterable<EntityLink> getEntityLinks(Object entity, Class<? extends EntityType> type)
    {
        return entityLinkService.getEntityLinks(entity, type);
    }

    @Override
    public Iterable<EntityLink> getEntityLinks(Object entity)
    {
        return entityLinkService.getEntityLinks(entity);
    }

    @Override
    public EntityLink getPrimaryEntityLink(Object entity, Class<? extends EntityType> type)
    {
        return entityLinkService.getPrimaryEntityLink(entity, type);
    }

    @Override
    public void start() throws Exception
    {
        this.entityLinkService = osgiServiceProxyFactory.createProxy(EntityLinkService.class, JiraAppLinksHostApplication.TIMEOUT);
    }
}
