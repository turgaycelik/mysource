package com.atlassian.jira.rest.v2.issue.version;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.version.VersionService;
import com.atlassian.jira.bc.project.version.remotelink.RemoteVersionLink;
import com.atlassian.jira.bc.project.version.remotelink.RemoteVersionLinkService;
import com.atlassian.jira.bc.project.version.remotelink.RemoteVersionLinkService.RemoteVersionLinkListResult;
import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.rest.v2.common.SimpleLinkBean;
import com.atlassian.jira.rest.v2.entity.RemoteEntityLinkJsonBean;
import com.atlassian.jira.rest.v2.issue.VersionResource;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.DateFieldFormat;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.web.ExecutingHttpRequest;

/**
 * Implementation of {@link VersionBeanFactory}.
 *
 * @since v4.4
 */
public class VersionBeanFactoryImpl implements VersionBeanFactory
{
    private final VersionService versionService;
    private final RemoteVersionLinkService remoteVersionLinkService;
    private final UriInfo info;
    private final DateFieldFormat dateFieldFormat;
    private final JiraAuthenticationContext authContext;
    private final SimpleLinkManager simpleLinkManager;

    /**
     * This constructor is still present for backwards compatibility with JIRA mobile which unfortunately
     * makes use of what is not public api.
     */
    public VersionBeanFactoryImpl(VersionService versionService, UriInfo info, DateFieldFormat dateFieldFormat,
            JiraAuthenticationContext authContext, SimpleLinkManager simpleLinkManager)
    {
        this.versionService = versionService;
        this.remoteVersionLinkService = null;
        this.authContext = authContext;
        this.simpleLinkManager = simpleLinkManager;

        // These two are proxied to objects from the current request. Be careful we are hunting AOP.
        this.info = info;
        this.dateFieldFormat = dateFieldFormat;
    }

    public VersionBeanFactoryImpl(VersionService versionService, RemoteVersionLinkService remoteVersionLinkService,
            UriInfo info, DateFieldFormat dateFieldFormat, JiraAuthenticationContext authContext,
            SimpleLinkManager simpleLinkManager)
    {
        this.versionService = versionService;
        this.remoteVersionLinkService = remoteVersionLinkService;
        this.authContext = authContext;
        this.simpleLinkManager = simpleLinkManager;

        //these two are proxied to objects from the current request. Be careful we are hunting AOP.
        this.info = info;
        this.dateFieldFormat = dateFieldFormat;
    }

    public VersionBean createVersionBean(Version version)
    {
        return createVersionBean(version, false, false);
    }

    public VersionBean createVersionBean(Version version, boolean expandOps)
    {
        return createVersionBean(version, expandOps, false);
    }

    public VersionBean createVersionBean(final Version version, final boolean expandOps, final boolean expandRemoteLinks)
    {
        final String expand = buildExpandString(expandOps, expandRemoteLinks);
        return createVersionBean(version, expandOps, expandRemoteLinks, expand);
    }



    private VersionBean createVersionBean(final Version version, final boolean expandOps, final boolean expandRemoteLinks, final String expand)
    {
        Assertions.notNull("version", version);

        final Date startDate = version.getStartDate();
        final Date releaseDate = version.getReleaseDate();

        String prettyStartDate = null;
        if (startDate != null)
        {
            prettyStartDate = dateFieldFormat.format(startDate);
        }

        Boolean versionOverDue = null;
        String prettyReleaseDate = null;
        if (releaseDate != null)
        {
            if (!version.isReleased())
            {
                versionOverDue = versionService.isOverdue(version);
            }

            prettyReleaseDate = dateFieldFormat.format(releaseDate);
        }

        final URI versionUri = createSelfURI(version);
        final VersionBean.Builder versionBeanBuilder = new VersionBean.Builder().setVersion(version)
                .setOverdue(versionOverDue)
                .setUserStartDate(prettyStartDate)
                .setUserReleaseDate(prettyReleaseDate)
                .setSelf(versionUri)
                .setProjectId(version.getProjectId())
                .setExpand(expand);
        if (expandOps)
        {
            versionBeanBuilder.setOperations(getOperations(version));
        }
        if (expandRemoteLinks)
        {
            versionBeanBuilder.setRemoteLinks(getRemoteLinks(version.getId(), versionUri));
        }
        return versionBeanBuilder.build();
    }



    private ArrayList<SimpleLinkBean> getOperations(final Version version)
    {
        final HttpServletRequest httpServletRequest = ExecutingHttpRequest.get();

        final User loggedInUser = authContext.getLoggedInUser();
        final Project project = version.getProjectObject();
        final Map<String, Object> params = MapBuilder.build("version", version, "user", loggedInUser, "project", project);
        final List<SimpleLink> links = simpleLinkManager.getLinksForSection(VERSION_OPERATIONS_WEB_LOCATION,
                loggedInUser, new JiraHelper(httpServletRequest, project, params));

        final ArrayList<SimpleLinkBean> linkBeans = new ArrayList<SimpleLinkBean>(links.size());
        for (SimpleLink link : links)
        {
            linkBeans.add(new SimpleLinkBean(link));
        }
        return linkBeans;
    }

    private List<RemoteEntityLinkJsonBean> getRemoteLinks(final Long versionId, final URI versionUri)
    {
        final RemoteVersionLinkListResult result = remoteVersionLinkService.getRemoteVersionLinksByVersionId(authContext.getUser(), versionId);
        if (!result.isValid())
        {
            return null;
        }

        final List<RemoteVersionLink> links = result.getRemoteVersionLinks();
        final List<RemoteEntityLinkJsonBean> beans = new ArrayList<RemoteEntityLinkJsonBean>(links.size());
        for (RemoteVersionLink link : links)
        {
            final URI self = UriBuilder.fromUri(versionUri).path("remotelink").path(link.getGlobalId()).build();
            beans.add(new RemoteEntityLinkJsonBean()
                    .self(self)
                    .name(link.getEntity().getName())
                    .link(link.getJsonString()));
        }
        return beans;
    }



    public List<VersionBean> createVersionBeans(Collection<? extends Version> versions)
    {
        return createVersionBeans(versions, false, false);
    }

    public List<VersionBean> createVersionBeans(Collection<? extends Version> versions, boolean expandOps)
    {
        return createVersionBeans(versions, expandOps, false);
    }

    public List<VersionBean> createVersionBeans(final Collection<? extends Version> versions, final boolean expandOps,
            final boolean expandRemoteLinks)
    {
        Assertions.containsNoNulls("versions", versions);
        final List<VersionBean> beans = new ArrayList<VersionBean>(versions.size());
        final String expand = buildExpandString(expandOps, expandRemoteLinks);
        for (Version version : versions)
        {
            beans.add(createVersionBean(version, expandOps, expandRemoteLinks, expand));
        }
        return beans;
    }

    private URI createSelfURI(Version version)
    {
        return info.getBaseUriBuilder()
                .path(VersionResource.class)
                .path(version.getId().toString())
                .build();
    }

    private String buildExpandString(boolean expandOps, boolean expandRemoteLinks)
    {
        final StringBuilder expand = new StringBuilder();
        if (expandOps)
        {
            expand.append(VersionBean.EXPAND_OPERATIONS).append(',');
        }
        if (expandRemoteLinks)
        {
            expand.append(VersionBean.EXPAND_REMOTE_LINKS).append(',');
        }
        if (expand.length() == 0)
        {
            return null;
        }
        expand.setLength(expand.length() - 1);  // Discard last ','
        return expand.toString();
    }
}
