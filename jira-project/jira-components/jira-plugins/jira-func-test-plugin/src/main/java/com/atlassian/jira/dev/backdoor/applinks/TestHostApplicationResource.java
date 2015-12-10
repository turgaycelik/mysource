package com.atlassian.jira.dev.backdoor.applinks;

import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.api.auth.AuthenticationProvider;
import com.atlassian.applinks.host.spi.EntityReference;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.jira.testkit.beans.AppTypeBean;
import com.atlassian.jira.testkit.beans.EntityList;
import com.atlassian.jira.testkit.beans.EntityRefBean;
import com.atlassian.jira.testkit.beans.EntityTypeBean;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import javax.annotation.Nullable;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * This resource is used for exercising JIRA's InternalHostApplication implementation without going through the web UI.
 *
 * @since v4.3
 */
@Path ("applinks")
@AnonymousAllowed
@Produces(MediaType.APPLICATION_JSON)
public class TestHostApplicationResource
{
    private final InternalHostApplication hostApplication;

    public TestHostApplicationResource(InternalHostApplication hostApplication)
    {
        this.hostApplication = hostApplication;
    }

    @GET
    @Path ("getDocumentationBaseUrl")
    public String getDocumentationBaseUrl()
    {
        return hostApplication.getDocumentationBaseUrl().toString();
    }

    @GET
    @Path ("getName")
    public String getName()
    {
        return hostApplication.getName();
    }

    @GET
    @Path ("getType")
    public AppTypeBean getType()
    {
        ApplicationType type = hostApplication.getType();
        return new AppTypeBean(type.getI18nKey(), String.valueOf(type.getIconUrl()));
    }

    public Iterable<Class<? extends AuthenticationProvider>> getSupportedInboundAuthenticationTypes()
    {
        return hostApplication.getSupportedInboundAuthenticationTypes();
    }

    public Iterable<Class<? extends AuthenticationProvider>> getSupportedOutboundAuthenticationTypes()
    {
        return hostApplication.getSupportedOutboundAuthenticationTypes();
    }

    @GET
    @Path ("getLocalEntities")
    public EntityList getLocalEntities()
    {
        ArrayList<EntityRefBean> entities = Lists.newArrayList(Iterables.transform(hostApplication.getLocalEntities(), new Function<EntityReference, EntityRefBean>()
        {
            public EntityRefBean apply(@Nullable EntityReference from)
            {
                return new EntityRefBean(
                        from.getKey(),
                        new EntityTypeBean(from.getType().getApplicationType().getName(), from.getType().getI18nKey(), from.getType().getPluralizedI18nKey(), from.getType().getIconUrl().toString()),
                        from.getName()
                );
            }
        }));

        return new EntityList(entities);
    }

    @GET
    @Path ("hasPublicSignup")
    public String hasPublicSignup()
    {
        return Boolean.valueOf(hostApplication.hasPublicSignup()).toString();
    }
}
