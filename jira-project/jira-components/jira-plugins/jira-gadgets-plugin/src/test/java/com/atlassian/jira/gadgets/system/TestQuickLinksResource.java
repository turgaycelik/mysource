package com.atlassian.jira.gadgets.system;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.user.MockUser;
import junit.framework.TestCase;
import org.easymock.classextension.EasyMock;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.createNiceMock;

/**
 * Tests QuickLinksResource
 *
 * @since v4.0
 */
public class TestQuickLinksResource extends TestCase
{
    private JiraAuthenticationContext ctx;
    private User mockUser;
    private PermissionManager prms;
    private ApplicationProperties props;
    private SearchService search;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        mockUser = new MockUser("admin");
        ctx = createNiceMock(JiraAuthenticationContext.class);
        prms = createNiceMock(PermissionManager.class);
        props = createNiceMock(ApplicationProperties.class);
        search = createNiceMock(SearchService.class);
        TimeZoneManager timeZoneManager = createMock(TimeZoneManager.class);
        MockComponentWorker componentWorker = new MockComponentWorker();
        componentWorker.registerMock(TimeZoneManager.class, timeZoneManager);
        ComponentAccessor.initialiseWorker(componentWorker);
    }

    public void testAdmin() throws Exception
    {
        expect(ctx.getLoggedInUser()).andReturn(mockUser);
        expect(prms.hasProjects(Permissions.BROWSE, mockUser)).andReturn(true);
        expect(prms.hasProjects(Permissions.CREATE_ISSUE, mockUser)).andReturn(true);
        expect(prms.hasProjects(Permissions.PROJECT_ADMIN, mockUser)).andReturn(true);
        expect(prms.hasPermission(Permissions.ADMINISTER, mockUser)).andReturn(true);

        expect(props.getOption("jira.option.voting")).andReturn(true);
        expect(props.getOption("jira.option.watching")).andReturn(true);

        EasyMock.replay(ctx, prms, props, search);
        QuickLinksResource resource = new QuickLinksResource(ctx, prms, props, search);
        Response res = resource.getQuickLinks();
        QuickLinksResource.LinkLists lists = (QuickLinksResource.LinkLists) res.getEntity();
        assertNotNull(lists);

        assertLinksExistsWithTitles(lists.getCommonLinks(),
                "gadget.quicklinks.reported.issues",
                "gadget.quicklinks.voted.issues",
                "gadget.quicklinks.watched.issues");

        assertLinksExistsWithTitles(lists.getNavigationLinks(),
                "gadget.quicklinks.browse.projects",
                "gadget.quicklinks.find.issues",
                "gadget.quicklinks.create.issue",
                "gadget.quicklinks.adminstration");
    }

    public void testAdminNoVotingOrWatching() throws Exception
    {
        expect(ctx.getLoggedInUser()).andReturn(mockUser);
        expect(prms.hasProjects(Permissions.BROWSE, mockUser)).andReturn(true);
        expect(prms.hasProjects(Permissions.CREATE_ISSUE, mockUser)).andReturn(true);
        expect(prms.hasProjects(Permissions.PROJECT_ADMIN, mockUser)).andReturn(true);
        expect(prms.hasPermission(Permissions.ADMINISTER, mockUser)).andReturn(true);

        expect(props.getOption("jira.option.voting")).andReturn(false);
        expect(props.getOption("jira.option.watching")).andReturn(false);

        EasyMock.replay(ctx, prms, props, search);
        QuickLinksResource resource = new QuickLinksResource(ctx, prms, props, search);
        Response res = resource.getQuickLinks();
        QuickLinksResource.LinkLists lists = (QuickLinksResource.LinkLists) res.getEntity();
        assertNotNull(lists);

        assertLinksExistsWithTitles(lists.getCommonLinks(),
                "gadget.quicklinks.reported.issues");

        assertLinksExistsWithTitles(lists.getNavigationLinks(),
                "gadget.quicklinks.browse.projects",
                "gadget.quicklinks.find.issues",
                "gadget.quicklinks.create.issue",
                "gadget.quicklinks.adminstration");
    }

    public void testUserLinks() throws Exception
    {
        expect(ctx.getLoggedInUser()).andReturn(mockUser);
        expect(prms.hasProjects(Permissions.BROWSE, mockUser)).andReturn(true);
        expect(prms.hasProjects(Permissions.CREATE_ISSUE, mockUser)).andReturn(true);
        expect(prms.hasProjects(Permissions.PROJECT_ADMIN, mockUser)).andReturn(false);
        expect(prms.hasPermission(Permissions.ADMINISTER, mockUser)).andReturn(false);

        expect(props.getOption("jira.option.voting")).andReturn(true);
        expect(props.getOption("jira.option.watching")).andReturn(true);

        EasyMock.replay(ctx, prms, props, search);
        QuickLinksResource resource = new QuickLinksResource(ctx, prms, props, search);
        Response res = resource.getQuickLinks();
        QuickLinksResource.LinkLists lists = (QuickLinksResource.LinkLists) res.getEntity();
        assertNotNull(lists);

        assertLinksExistsWithTitles(lists.getCommonLinks(),
                "gadget.quicklinks.reported.issues",
                "gadget.quicklinks.voted.issues",
                "gadget.quicklinks.watched.issues");

        assertLinksExistsWithTitles(lists.getNavigationLinks(),
                "gadget.quicklinks.browse.projects",
                "gadget.quicklinks.find.issues",
                "gadget.quicklinks.create.issue");
    }

    public void testProjectAdmin() throws Exception
    {
        expect(ctx.getLoggedInUser()).andReturn(mockUser);
        expect(prms.hasProjects(Permissions.BROWSE, mockUser)).andReturn(true);
        expect(prms.hasProjects(Permissions.CREATE_ISSUE, mockUser)).andReturn(true);
        expect(prms.hasProjects(Permissions.PROJECT_ADMIN, mockUser)).andReturn(true);
        expect(prms.hasPermission(Permissions.ADMINISTER, mockUser)).andReturn(false);

        expect(props.getOption("jira.option.voting")).andReturn(true);
        expect(props.getOption("jira.option.watching")).andReturn(true);

        EasyMock.replay(ctx, prms, props, search);
        QuickLinksResource resource = new QuickLinksResource(ctx, prms, props, search);
        Response res = resource.getQuickLinks();
        QuickLinksResource.LinkLists lists = (QuickLinksResource.LinkLists) res.getEntity();
        assertNotNull(lists);

        assertLinksExistsWithTitles(lists.getCommonLinks(),
                "gadget.quicklinks.reported.issues",
                "gadget.quicklinks.voted.issues",
                "gadget.quicklinks.watched.issues");

        assertLinksExistsWithTitles(lists.getNavigationLinks(),
                "gadget.quicklinks.browse.projects",
                "gadget.quicklinks.find.issues",
                "gadget.quicklinks.create.issue");
    }

    public void testAdminNoProjects() throws Exception
    {
        expect(ctx.getLoggedInUser()).andReturn(mockUser);
        expect(prms.hasProjects(Permissions.BROWSE, mockUser)).andReturn(true);
        expect(prms.hasProjects(Permissions.CREATE_ISSUE, mockUser)).andReturn(true);
        expect(prms.hasProjects(Permissions.PROJECT_ADMIN, mockUser)).andReturn(false);
        expect(prms.hasPermission(Permissions.ADMINISTER, mockUser)).andReturn(true);

        expect(props.getOption("jira.option.voting")).andReturn(true);
        expect(props.getOption("jira.option.watching")).andReturn(true);

        EasyMock.replay(ctx, prms, props, search);
        QuickLinksResource resource = new QuickLinksResource(ctx, prms, props, search);
        Response res = resource.getQuickLinks();
        QuickLinksResource.LinkLists lists = (QuickLinksResource.LinkLists) res.getEntity();
        assertNotNull(lists);

        assertLinksExistsWithTitles(lists.getCommonLinks(),
                "gadget.quicklinks.reported.issues",
                "gadget.quicklinks.voted.issues",
                "gadget.quicklinks.watched.issues");

        assertLinksExistsWithTitles(lists.getNavigationLinks(),
                "gadget.quicklinks.browse.projects",
                "gadget.quicklinks.find.issues",
                "gadget.quicklinks.create.issue");
    }

    public void testAnonymous() throws Exception
    {
        expect(ctx.getLoggedInUser()).andReturn(null);
        expect(prms.hasProjects(Permissions.BROWSE, mockUser)).andReturn(true);
        expect(prms.hasProjects(Permissions.CREATE_ISSUE, mockUser)).andReturn(false);
        expect(prms.hasProjects(Permissions.PROJECT_ADMIN, mockUser)).andReturn(false);
        expect(prms.hasPermission(Permissions.ADMINISTER, mockUser)).andReturn(false);

        expect(props.getOption("jira.option.voting")).andReturn(true);
        expect(props.getOption("jira.option.watching")).andReturn(true);

        EasyMock.replay(ctx, prms, props, search);
        QuickLinksResource resource = new QuickLinksResource(ctx, prms, props, search);
        Response res = resource.getQuickLinks();
        assertTrue(res.getEntity() instanceof QuickLinksResource.Warning);
    }

    void assertLinksExistsWithTitles(Collection<QuickLinksResource.Link> links, String... titles)
    {
        assertEquals("Should be the same number of links as expected", titles.length, links.size());
        Set<String> expectedTitles = new HashSet<String>(Arrays.asList(titles));
        for (QuickLinksResource.Link link : links)
        {
            expectedTitles.remove(link.getText());
        }
        assertEquals("Expected titles not found: " + expectedTitles, 0, expectedTitles.size());
    }
}
