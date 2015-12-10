package com.atlassian.jira.rest.v1.menus;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.UserProjectHistoryManager;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.plugins.rest.common.security.CorsAllowed;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;

/**
 * REST endpoint to get a list of Sections and links for a menu.  This uses SimpleLinkManager to generate list of links.
 *
 * @since v4.0
 */
@Path("menus")
@AnonymousAllowed
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_FORM_URLENCODED  })
@Produces ({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@CorsAllowed
public class MenuResource
{
    private final SimpleLinkManager simpleLinkManager;
    private final JiraAuthenticationContext authenticationContext;
    private final UserProjectHistoryManager userProjectHistoryManager;

    public MenuResource(SimpleLinkManager simpleLinkManager, JiraAuthenticationContext authenticationContext, UserProjectHistoryManager userProjectHistoryManager)
    {
        this.simpleLinkManager = simpleLinkManager;
        this.authenticationContext = authenticationContext;
        this.userProjectHistoryManager = userProjectHistoryManager;
    }

    /**
     * Gets a list of {@link Section} and sub links {@link Link} for a menu. These are wrapped in a {@link Menu}
     *
     * @param request  The request for the resource
     * @param location The location that the menu is retreiving for.
     * @return a list of {@link Section} and sub links {@link Link} for a menu. These are wrapped in a {@link Menu}
     */
    @GET
    @Path("{location}")
    public Response getMenuList(@Context HttpServletRequest request,
                                @PathParam("location") String location,
                                @QueryParam("inAdminMode") Boolean inAdminMode)
    {
        final User user = authenticationContext.getLoggedInUser();
        final Project selectedProject = userProjectHistoryManager.getCurrentProject(Permissions.BROWSE, user);
        final JiraHelper jiraHelper = new JiraHelper(request, selectedProject);
        final List<SimpleLinkSection> sections = simpleLinkManager.getSectionsForLocation(location, user, jiraHelper);

        if (inAdminMode)
        {
            request.setAttribute("jira.admin.mode", true);
        }

        final List<Section> returnedList = new ArrayList<Section>(sections.size());
        for (SimpleLinkSection section : sections)
        {
            final List<SimpleLink> links = simpleLinkManager.getLinksForSection(location + "/" + section.getId(), user, jiraHelper);

            final List<Link> returnLinks = new ArrayList<Link>(links.size());
            for (SimpleLink link : links)
            {
                returnLinks.add(new Link(link));
            }

            final Section newSection = new Section(section, returnLinks);
            returnedList.add(newSection);
        }

        return Response.ok(new Menu(returnedList)).cacheControl(NO_CACHE).build();
    }

    @XmlRootElement
    public static class Menu
    {
        @XmlElement
        private List<Section> sections;

        @SuppressWarnings({"UnusedDeclaration", "unused"})
        private Menu() {}

        public Menu(List<Section> sections)
        {
            this.sections = sections;
        }
    }

    @XmlRootElement
    public static class Section
    {
        @XmlElement
        private String label;
        @XmlElement
        private String title;
        @XmlElement
        private String iconUrl;
        @XmlElement
        private String style;
        @XmlElement
        private String id;

        @XmlElement
        List<Link> items;

        @SuppressWarnings({"UnusedDeclaration", "unused"})
        private Section() {}

        public Section(SimpleLinkSection section, List<Link> items)
        {
            this.items = items;
            this.label = section.getLabel();
            this.title = section.getTitle();
            this.iconUrl = section.getIconUrl();
            this.style = section.getStyleClass();
            this.id = section.getId();
        }
    }

    @XmlRootElement
    public static class Link
    {
        @XmlElement
        private String label;
        @XmlElement
        private String title;
        @XmlElement
        private String iconUrl;
        @XmlElement
        private String style;
        @XmlElement
        private String id;
        @XmlElement
        private String url;
        @XmlElement
        private Map<String, String> parameters;

        @SuppressWarnings({"UnusedDeclaration", "unused"})
        private Link() {}

        public Link(SimpleLink link)
        {
            this.label = link.getLabel();
            this.title = link.getTitle();
            this.iconUrl = link.getIconUrl();
            this.style = link.getStyleClass();
            this.id = link.getId();
            this.url = link.getUrl();
            this.parameters = link.getParams();
        }
    }
}
