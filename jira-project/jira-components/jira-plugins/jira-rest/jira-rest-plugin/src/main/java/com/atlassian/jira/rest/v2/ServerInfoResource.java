package com.atlassian.jira.rest.v2;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.rest.v2.healthcheck.ValidationQuery;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.plugins.rest.common.security.CorsAllowed;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.Date;

/**
 * @since v4.2
 */
@Path ("serverInfo")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
@CorsAllowed
public class ServerInfoResource
{
    private final ApplicationProperties properties;
    private final BuildUtilsInfo buildUtils;
    private final JiraAuthenticationContext authContext;
    private final PermissionManager permissionManager;
    private final OfBizDelegator ofBizDelegator;

    public ServerInfoResource(final ApplicationProperties properties, final BuildUtilsInfo buildUtils, final JiraAuthenticationContext authContext, final PermissionManager permissionManager, OfBizDelegator ofBizDelegator)
    {
        this.properties = properties;
        this.buildUtils = buildUtils;
        this.authContext = authContext;
        this.permissionManager = permissionManager;
        this.ofBizDelegator = ofBizDelegator;
    }

    /**
     * Returns general information about the current JIRA server.
     *
     * @return a Response containing a ServerInfoBean
     *
     * @response.representation.200.qname
     *      serverInfo
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.200.example
     *      {@link ServerInfoBean#DOC_EXAMPLE}
     *
     * @response.representation.200.doc
     *      Returns a full representation of the server info in JSON format
     */
    @GET
    public ServerInfoBean getServerInfo(@Deprecated @QueryParam ("doHealthCheck") boolean doHealthCheck)
    {
        ServerInfoBean serverInfo = createServerInfoBean();
        if (doHealthCheck)
        {
            // JRADEV-15745: quick health check that HOPS can use to monitor OnDemand instances
            // deprecated: Utilize jira-healthcheck-plugin instead. See JDEV-23665 for more details. Remove with 7.0
            addHealthCheckInfo(serverInfo);
        }

        return serverInfo;
    }

    private ServerInfoBean createServerInfoBean()
    {
        final boolean canUse = permissionManager.hasPermission(Permissions.USE, authContext.getLoggedInUser());
        return new ServerInfoBean(
                properties.getString(APKeys.JIRA_BASEURL),
                buildUtils.getVersion(),
                buildUtils.getVersionNumbers(),
                Integer.valueOf(buildUtils.getCurrentBuildNumber()),
                buildUtils.getCurrentBuildDate(),
                buildUtils.getCommitId(),
                buildUtils.getBuildPartnerName(),
                properties.getString(APKeys.JIRA_TITLE),
                canUse ? new Date() : null);
    }

    /**
     * Quick and dirty health check so Hosted Operations can check whether JIRA's database is all locked up.
     *
     * @param serverInfo a ServerInfoBean to add the health check info to
     * @deprecated Utilize jira-healthcheck-plugin instead. See JDEV-23665 for more details. Remove with 7.0
     */
    @Deprecated
    private void addHealthCheckInfo(ServerInfoBean serverInfo)
    {
        serverInfo.healthChecks = Collections.singletonList(
                new ValidationQuery(ofBizDelegator).doCheck()
        );
    }
}
