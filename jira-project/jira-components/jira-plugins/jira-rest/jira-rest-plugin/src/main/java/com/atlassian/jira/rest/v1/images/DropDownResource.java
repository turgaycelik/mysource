package com.atlassian.jira.rest.v1.images;

import com.atlassian.jira.image.dropdown.DropDownCreatorService;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.plugins.rest.common.security.CorsAllowed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import static com.atlassian.jira.rest.v1.util.CacheControl.CACHE_FOREVER;

/**
 * REST endpoint for retrieving a dropdown arrow of a given colour.
 *
 * @since v4.0
 */
@Path("dropdowns")
@AnonymousAllowed
@Produces({"image/png"})
@CorsAllowed
public class DropDownResource
{
    private final DropDownCreatorService dropDownCreatorService;

    public DropDownResource(DropDownCreatorService dropDownCreatorService)
    {
        this.dropDownCreatorService = dropDownCreatorService;
    }

    /**
     * Retrieve a dropdown arrow for the passed in color.
     * <p/>
     * Input strings can ontain a leading hash (#) and can be a 3 char or 6 char hex string.  See any web tutorial for
     * what colour the string represents.
     * <p/>
     * This is cached effectively forever
     *
     * @param colorHex           The main color of the dropdown
     * @param backgroundColorHex Thebackground colour of the dropdown. This will also be transparent.  Useful for IE6
     * @return An array of bytes that represent the returned png. This is cached effectively forever
     */
    @GET
    public Response getImage(@QueryParam("color") String colorHex, @QueryParam("bgcolor") String backgroundColorHex)
    {

        final byte[] dropdown = dropDownCreatorService.getDropdown(colorHex, backgroundColorHex);
        return Response.ok(dropdown).cacheControl(CACHE_FOREVER).build();
    }

    /**
     * Same as the /dropdowns?color&amp;bgcolor url, but the colours are specified in the path not the query string.
     * The leading (hash) component of the path is ignored.
     */
    @GET
    @Path("/{hash}/{color}/{bgcolor}/img.png")
    public Response getImageFromPath(@PathParam("color") String colorHex, @PathParam("bgcolor") String backgroundColorHex)
    {
        return getImage(colorHex, backgroundColorHex);
    }
}
