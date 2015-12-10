package com.atlassian.jira.rest.v2.issue.scope;

import com.atlassian.jira.rest.v2.issue.context.ContextUriInfo;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Factory bean for ContextUriInfo.
 * <p/>
 * This factory bean looks up the UriInfo from the MethodInvocation.
 *
 * @since v4.2
 */
public class ContextUriInfoFactoryBean extends AbstractFactoryBean
{
    private final RequestScope scope;

    public ContextUriInfoFactoryBean(RequestScope scope)
    {
        this.scope = scope;
    }

    @Override
    protected ContextUriInfo createInstance() throws Exception
    {
        final UriInfo uriInfo = scope.currentInvocation().getHttpContext().getUriInfo();
        return new UriInfoWrapper(uriInfo);
    }

    @Override
    public Class<ContextUriInfo> getObjectType()
    {
        return ContextUriInfo.class;
    }

    /**
     * Wrapper around a UriInfo instance.
     */
    static class UriInfoWrapper implements ContextUriInfo
    {
        /**
         * The wrapped UriInfo.
         */
        private final UriInfo uriInfo;

        public UriInfoWrapper(UriInfo uriInfo) { this.uriInfo = notNull("uriInfo", uriInfo); }

        public String getPath() {return uriInfo.getPath();}

        public String getPath(boolean decode) {return uriInfo.getPath(decode);}

        public List<PathSegment> getPathSegments() {return uriInfo.getPathSegments();}

        public List<PathSegment> getPathSegments(boolean decode) {return uriInfo.getPathSegments(decode);}

        public URI getRequestUri() {return uriInfo.getRequestUri();}

        public UriBuilder getRequestUriBuilder() {return uriInfo.getRequestUriBuilder();}

        public URI getAbsolutePath() {return uriInfo.getAbsolutePath();}

        public UriBuilder getAbsolutePathBuilder() {return uriInfo.getAbsolutePathBuilder();}

        public URI getBaseUri() {return uriInfo.getBaseUri();}

        public UriBuilder getBaseUriBuilder() {return uriInfo.getBaseUriBuilder();}

        public MultivaluedMap<String, String> getPathParameters() {return uriInfo.getPathParameters();}

        public MultivaluedMap<String, String> getPathParameters(boolean decode) { return uriInfo.getPathParameters(decode); }

        public MultivaluedMap<String, String> getQueryParameters() {return uriInfo.getQueryParameters();}

        public MultivaluedMap<String, String> getQueryParameters(boolean decode) { return uriInfo.getQueryParameters(decode); }

        public List<String> getMatchedURIs() {return uriInfo.getMatchedURIs();}

        public List<String> getMatchedURIs(boolean decode) {return uriInfo.getMatchedURIs(decode);}

        public List<Object> getMatchedResources() {return uriInfo.getMatchedResources();}
    }
}
