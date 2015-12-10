package com.atlassian.jira.rest.internal;

import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.plugin.profile.DarkFeatures;
import com.atlassian.jira.rest.exception.ForbiddenWebException;
import com.atlassian.jira.util.dbc.Assertions;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;

import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;

/**
 * REST resource for managing site-wide dark features.
 *
 * @since 5.2
 */
@Path ("darkFeatures")
@Produces ( { MediaType.APPLICATION_JSON })
public class SiteDarkFeaturesResource
{
    private final FeatureManager featureManager;

    public SiteDarkFeaturesResource(final FeatureManager featureManager)
    {
        this.featureManager = Assertions.notNull("featureManager", featureManager);
    }

    /**
     * Gets whether dark feature is enabled or disabled.
     *
     * @response.representation.200.doc
     *      Returned if the currently authenticated user can view dark features.
     *
     * @response.representation.403.doc
     *      Returned if the currently authenticated user does not have permission to view dark features.
     */
    @GET
    @Path("/{key}")
     public Response get(@PathParam ("key") final String featureKey)
    {
        if (!featureManager.hasSiteEditPermission())
        {
            throw new ForbiddenWebException();
        }

        if (StringUtils.isNotBlank(featureKey))
        {
            boolean enabled = featureManager.isEnabled(featureKey);
            return Response.ok(new DarkFeaturePropertyBean(enabled)).cacheControl(NO_CACHE).build();
        }
        return Response.noContent().cacheControl(NO_CACHE).build();
    }

    /**
     * Enable or disable a dark feature via PUT.
     *
     * @request.representation.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returned if the currently authenticated user can enable dark features.
     *
     * @response.representation.403.doc
     *      Returned if the currently authenticated user does not have permission to edit dark features.
     */
    @PUT
    @Path("/{key}")
    @Consumes ({ MediaType.APPLICATION_JSON  })
    public Response put(@PathParam ("key") final String featureKey, final DarkFeaturePropertyBean darkFeaturePropertyBean)
    {
        if (!featureManager.hasSiteEditPermission())
        {
            throw new ForbiddenWebException();
        }

        if (StringUtils.isNotBlank(featureKey))
        {
            setSiteDarkFeature(featureKey, darkFeaturePropertyBean.isEnabled());
        }
        return Response.noContent().cacheControl(NO_CACHE).build();
    }

    /**
     * Updates the value for a set of dark features.
     *
     * @request.representation.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returned if the request is successful.
     *
     * @response.representation.403.doc
     *      Returned if the currently authenticated user does not have permission to edit dark features.
     *
     * @param updates a map of feature names to enabled state (enabled/disabled)
     *
     * @since 6.0
     * @return
     */
    @PUT
    public DarkFeaturesBean putSiteDarkFeatures(Map<String, DarkFeaturePropertyBean> updates)
    {
        if (!featureManager.hasSiteEditPermission()) { throw new ForbiddenWebException(); }

        for (String feature : updates.keySet())
        {
            boolean enabled = updates.get(feature).isEnabled();
            setSiteDarkFeature(feature, enabled);
        }

        return enabledDarkFeatures();
    }

    /**
     * Enabled or disables <code>feature</code> depending on the value of <code>enabled</code>.
     *
     * @param feature a String containing the feature name
     * @param enabled a Boolean indicating whether to enable or disable the feature
     */
    private void setSiteDarkFeature(String feature, boolean enabled)
    {
        if (enabled)
        {
            featureManager.enableSiteDarkFeature(feature.trim());
        }
        else
        {
            featureManager.disableSiteDarkFeature(feature.trim());
        }
    }

    /**
     * Returns a Map containing all enabled site dark features.
     *
     * @return a Map containing all enabled site dark features.
     */
    private DarkFeaturesBean enabledDarkFeatures()
    {
        DarkFeatures darkFeatures = featureManager.getDarkFeatures();

        Map<String, DarkFeaturePropertyBean> result = Maps.newHashMap();
        for (String featureKey : darkFeatures.getAllEnabledFeatures())
        {
            result.put(featureKey, new DarkFeaturePropertyBean(true));
        }

        return new DarkFeaturesBean()
                .systemFeatures(darkFeatures.getSystemEnabledFeatures())
                .siteFeatures(darkFeatures.getSiteEnabledFeatures());
    }
}
