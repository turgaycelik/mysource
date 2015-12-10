package com.atlassian.jira.gadgets.system;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.query.Query;

import java.util.ArrayList;
import java.util.Collection;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * REST endpoint to retrieve a list of common usage links.
 *
 * @since v4.0
 */
@Path ("/quicklinks")
@AnonymousAllowed
@Produces ({ MediaType.APPLICATION_JSON })
public class QuickLinksResource
{
    private final JiraAuthenticationContext authenticationContext;
    private final PermissionManager permissionManager;
    private final ApplicationProperties applicationProperties;
    private final SearchService searchService;

    public QuickLinksResource(final JiraAuthenticationContext authenticationContext, final PermissionManager permissionManager,
            final ApplicationProperties applicationProperties, final SearchService searchService)
    {
        this.authenticationContext = authenticationContext;
        this.permissionManager = permissionManager;
        this.applicationProperties = applicationProperties;
        this.searchService = searchService;
    }

    @GET
    public Response getQuickLinks() throws Exception
    {
        final Collection<Link> commonLinks = new ArrayList<Link>();
        final Collection<Link> navigationLinks = new ArrayList<Link>();

        final User user = authenticationContext.getLoggedInUser();
        if (permissionManager.hasProjects(Permissions.BROWSE, user))
        {
            navigationLinks.add(new Link("gadget.quicklinks.browse.projects", "/secure/BrowseProjects.jspa", "gadget.quicklinks.browse.projects.title"));
            navigationLinks.add(new Link("gadget.quicklinks.find.issues", "/secure/IssueNavigator.jspa?mode=show", "gadget.quicklinks.find.issues.title"));
        }

        if (permissionManager.hasProjects(Permissions.CREATE_ISSUE, user))
        {
            navigationLinks.add(new Link("gadget.quicklinks.create.issue", "/secure/CreateIssue!default.jspa", "gadget.quicklinks.create.issue.title"));
        }

        if (permissionManager.hasPermission(Permissions.ADMINISTER, user) &&
                permissionManager.hasProjects(Permissions.PROJECT_ADMIN, user))
        {
            navigationLinks.add(new Link("gadget.quicklinks.adminstration", "/secure/project/ViewProjects.jspa", "gadget.quicklinks.administration.title"));
        }

        if (user != null)
        {
            final Query query = JqlQueryBuilder.newBuilder().where().unresolved().and().reporterIsCurrentUser().buildQuery();
            commonLinks.add(new Link("gadget.quicklinks.reported.issues", "/secure/IssueNavigator.jspa?reset=true&hide=true" + searchService.getQueryString(user, query), "gadget.quicklinks.reported.issues.title"));

            if (applicationProperties.getOption("jira.option.voting"))
            {
                JqlClauseBuilder builder = JqlQueryBuilder.newClauseBuilder().issueInVotedIssues();

                final String jql = searchService.getQueryString(user, builder.buildQuery());
                final String url = "/secure/IssueNavigator.jspa?reset=true" + jql;

                commonLinks.add(new Link("gadget.quicklinks.voted.issues", url, "gadget.quicklinks.voted.issues.title"));
            }
            if (applicationProperties.getOption("jira.option.watching"))
            {
                JqlClauseBuilder builder = JqlQueryBuilder.newClauseBuilder().issueInWatchedIssues();

                final String jql = searchService.getQueryString(user, builder.buildQuery());
                final String url = "/secure/IssueNavigator.jspa?reset=true" + jql;
                commonLinks.add(new Link("gadget.quicklinks.watched.issues", url, "gadget.quicklinks.watched.issues.title"));
            }
        }
        else if (navigationLinks.isEmpty())
        {
            return Response.ok(new Warning()).cacheControl(NO_CACHE).build();
        }

        return Response.ok(new LinkLists(navigationLinks, commonLinks)).cacheControl(NO_CACHE).build();
    }

    ///CLOVER:OFF
    @XmlRootElement
    public static class Warning
    {
        @XmlElement
        private boolean noDataAndNoUser = true;

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        private Warning()
        {}

        public boolean isNoDataNoUser()
        {
            return noDataAndNoUser;
        }
    }

    @XmlRootElement
    public static class LinkLists
    {
        @XmlElement
        private Collection<Link> commonLinks;
        @XmlElement
        private Collection<Link> navigationLinks;

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        private LinkLists()
        {}

        public LinkLists(Collection<Link> navigationLinks, Collection<Link> commonLinks)
        {
            this.navigationLinks = navigationLinks;
            this.commonLinks = commonLinks;
        }

        public Collection<Link> getCommonLinks()
        {
            return commonLinks;
        }

        public Collection<Link> getNavigationLinks()
        {
            return navigationLinks;
        }
    }

    @XmlRootElement
    public static class Link
    {
        @XmlElement
        private String title;
        @XmlElement
        private String url;
        @XmlElement
        private String text;

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        private Link()
        {}

        public Link(final String text, final String url, final String title)
        {
            this.title = title;
            this.url = url;
            this.text = text;
        }

        public String getTitle()
        {
            return title;
        }

        public String getUrl()
        {
            return url;
        }

        public String getText()
        {
            return text;
        }
    }
    ///CLOVER:ON
}
