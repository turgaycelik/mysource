package com.atlassian.jira.dev.backdoor;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.index.IndexDocumentConfigurationFactory;
import com.atlassian.jira.index.property.PluginIndexConfiguration;
import com.atlassian.jira.index.property.PluginIndexConfigurationManager;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import org.apache.commons.io.IOUtils;

/**
 * Backdoor for retrieving plugin index configurations.
 *
 * @since v6.2
 */
@Path ("plugin-index-configuration")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class PluginIndexConfigurationBackdoor
{
    @GET
    public Response getDocumentsForEntity(@QueryParam("entityKey") final String entityKey)
    {
        return Response.status(Response.Status.OK).entity(Iterables.transform(ComponentAccessor.getComponent(PluginIndexConfigurationManager.class).getDocumentsForEntity(entityKey), new Function<PluginIndexConfiguration, String>()
        {
            @Override
            public String apply(final PluginIndexConfiguration indexConfiguration)
            {
                return indexConfiguration.getPluginKey() + ":" + indexConfiguration.getModuleKey();
            }
        })).build();
    }

    @PUT
    public Response putDocumentConfiguration(@QueryParam("pluginKey") final String pluginKey,
            @QueryParam("moduleKey") final String moduleKey,
            @Context final HttpServletRequest request) throws IOException
    {
        IndexDocumentConfigurationFactory configurationFactory = ComponentAccessor.getComponent(IndexDocumentConfigurationFactory.class);
        InputStream inputStream = null;
        try
        {
            inputStream = request.getInputStream();
            final String entity = new String(IOUtils.toByteArray(inputStream));
            ComponentAccessor.getComponent(PluginIndexConfigurationManager.class)
                    .put(pluginKey, moduleKey, configurationFactory.fromXML(entity));
            return Response.status(Response.Status.CREATED).build();
        }
        catch (IOException e)
        {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        catch (IndexDocumentConfigurationFactory.IndexDocumentConfigurationParseException e)
        {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        finally
        {
            if (inputStream != null)
            {
                inputStream.close();
            }
        }
    }
}
