package com.atlassian.jira.dev.slomo;

import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.google.common.base.Function;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static com.atlassian.jira.dev.backdoor.util.CacheControl.never;
import static com.atlassian.jira.dev.slomo.SlowMotionResource.PatternRepresentation.toRepresentation;
import static com.google.common.collect.Iterables.transform;
import static org.tuckey.web.filters.urlrewrite.utils.StringUtils.isBlank;

/**
 * @since v5.2
 */
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
@Path ("/slomo")
public class SlowMotionResource
{
    private final SlowMotion slowMotion;

    public SlowMotionResource(SlowMotion slowMotion) {this.slowMotion = slowMotion;}

    @GET
    @Path ("/default")
    public Response get()
    {
        return Response.ok(slowMotion.getDefaultDelay()).cacheControl(never()).build();
    }

    @POST
    @Path ("/default")
    public Response get(int delay)
    {
        slowMotion.setDefaultDelay(delay);
        return get();
    }

    @POST
    @Path ("/pattern/{id}")
    public Response update(@PathParam ("id") final int id, final PatternRepresentation representation)
    {
        PatternDelay patternDelay = slowMotion.get(id);
        if (patternDelay == null)
        {
            return fourOhFour();
        }

        Pattern pattern = patternDelay.getPattern();
        if (representation.pattern != null)
        {
            if (isBlank(representation.pattern))
            {
                return patternError("Pattern cannot be empty.");
            }
            try
            {
                pattern = Pattern.compile(representation.pattern);
            }
            catch (PatternSyntaxException e)
            {
                return patternError(e.getDescription());
            }
        }

        int delay = representation.delay == null ? patternDelay.getDelay() : representation.delay;
        boolean enabled = representation.enabled == null ? patternDelay.isEnabled() : representation.enabled;
        PatternDelay update = slowMotion.update(new PatternDelay(id, pattern, delay, enabled));
        return update == null ? fourOhFour() : ok(update);
    }

    private Response fourOhFour() {return Response.status(Response.Status.NOT_FOUND).cacheControl(never()).build();}

    private static Response patternError(String error)
    {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new PatternError(error)).cacheControl(never()).build();
    }

    @DELETE
    @Path ("/pattern/{id}")
    public Response deletePatttern(@PathParam ("id") final int id)
    {
        if (slowMotion.delete(id))
        {
            return Response.ok().cacheControl(never()).build();
        }
        else
        {
            return fourOhFour();
        }
    }

    @PUT
    @Path ("/pattern")
    public Response create(final PatternRepresentation representation)
    {
        if (isBlank(representation.pattern))
        {
            return patternError("Pattern cannot be empty.");
        }
        try
        {
            Pattern pattern = Pattern.compile(representation.pattern);
            int delay = representation.delay == null ? 0 : representation.delay;
            boolean enabled = representation.enabled == null ? true : representation.enabled;

            PatternDelay config = slowMotion.create(new PatternDelay(pattern, delay, enabled));
            return ok(config);
        }
        catch (PatternSyntaxException e)
        {
            return patternError(e.getDescription());
        }
    }

    private static Response ok(PatternDelay delay)
    {
        return Response.ok(new PatternRepresentation(delay)).cacheControl(never()).build();
    }

    @GET
    @Path ("/pattern/{id}")
    public Response getPattern(@PathParam ("id") int id)
    {
        PatternDelay update = slowMotion.get(id);
        Response.ResponseBuilder r;
        if (update == null)
        {
            r = Response.status(Response.Status.NOT_FOUND);
        }
        else
        {
            r = Response.ok(new PatternRepresentation(update));
        }
        return r.cacheControl(never()).build();
    }

    @GET
    @Path ("/pattern")
    public Response getPatterns()
    {
        return Response.ok(transform(slowMotion.getDelays(), toRepresentation))
                .cacheControl(never()).build();
    }

    @GET
    public Response getConfig()
    {
        return Response.ok(new SlomoConfig(slowMotion))
                .cacheControl(never()).build();
    }

    public static class PatternRepresentation
    {
        public static final Function<PatternDelay, PatternRepresentation> toRepresentation = new Function<PatternDelay, PatternRepresentation>()
        {
            @Override
            public PatternRepresentation apply(PatternDelay input)
            {
                return new PatternRepresentation(input);
            }
        };

        @JsonProperty
        public int id;

        @JsonProperty
        public String pattern;

        @JsonProperty
        public Integer delay;

        @JsonProperty
        public Boolean enabled;

        public PatternRepresentation(PatternDelay delay)
        {
            this.id = delay.getId();
            this.pattern = delay.getPattern().pattern();
            this.delay = delay.getDelay();
            this.enabled = delay.isEnabled();
        }

        public PatternRepresentation() {}
    }

    public static class PatternError
    {
        @JsonProperty
        public String patternError;

        @JsonProperty
        public String delayError;

        public PatternError(String patternError)
        {
            this.patternError = patternError;
        }
    }

    public static class SlomoConfig
    {
        @JsonProperty
        public Iterable<PatternRepresentation> patterns;

        @JsonProperty
        public int defaultDelay;

        public SlomoConfig() {}

        public SlomoConfig(SlowMotion slomo)
        {
            defaultDelay = slomo.getDefaultDelay();
            patterns = transform(slomo.getDelays(), toRepresentation);
        }
    }
}
