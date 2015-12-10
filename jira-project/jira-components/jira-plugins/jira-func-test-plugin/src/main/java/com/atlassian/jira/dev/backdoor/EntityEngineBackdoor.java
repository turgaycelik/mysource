package com.atlassian.jira.dev.backdoor;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;
import java.util.Map;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * Use this backdoor to manipulate User Profiles as part of setup for tests.
 *
 * This class should only be called by the {@link com.atlassian.jira.functest.framework.backdoor.EntityEngine}.
 *
 * @since v5.2
 */
@Path ("entityEngine")
public class EntityEngineBackdoor
{
    private final OfBizDelegator genericDelegator;

    public EntityEngineBackdoor()
    {
        this.genericDelegator = ComponentAccessor.getOfBizDelegator();
    }

    @POST
    @AnonymousAllowed
    @Path("findByAnd")
    public Response findByAnd(@QueryParam ("entity") String entityname, Map<String, Object> fields)
    {
        List<GenericValue> values = null;
        values = genericDelegator.findByAnd(entityname, fields);

        return Response.ok(values).build();
    }
}
