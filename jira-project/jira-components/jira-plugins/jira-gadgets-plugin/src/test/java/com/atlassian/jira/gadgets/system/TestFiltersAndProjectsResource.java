package com.atlassian.jira.gadgets.system;

import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.sharing.type.ShareTypeFactory;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.query.QueryImpl;
import junit.framework.TestCase;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

/**
 *
 */
public class TestFiltersAndProjectsResource extends TestCase
{

    public void testFoo()
    {
        JiraAuthenticationContext jac = createMock(JiraAuthenticationContext.class);
        ApplicationUser user = new MockApplicationUser("foo");
        expect(jac.getUser()).andStubReturn(user);
        SearchRequestService srs = createMock(SearchRequestService.class);

        Collection<SearchRequest> sr = new ArrayList<SearchRequest>();
        sr.add(new SearchRequest(new QueryImpl(), new MockApplicationUser("owerName"), "name", "desc", 1L, 1L));
        expect(srs.getFavouriteFilters(user)).andReturn(sr);
        ShareTypeFactory typeFactory = createMock(ShareTypeFactory.class);
        PermissionManager pm = createMock(PermissionManager.class);
        I18nHelper.BeanFactory fac = createMock(I18nHelper.BeanFactory.class);
        FiltersAndProjectsResource resource = new FiltersAndProjectsResource(jac, srs, typeFactory, pm, fac);

        I18nHelper helper = createMock(I18nHelper.class);
        expect(helper.getText("common.concepts.filters")).andReturn("Filter");
        expect(helper.getText("common.concepts.projects")).andReturn("projects");
        replay(helper);
        expect(fac.getInstance(user)).andReturn(helper);

        ArrayList<Project> list = new ArrayList<Project>();
        Project proj = createMock(Project.class);
        expect(proj.getName()).andReturn("proj");
        expect(proj.getId()).andReturn(2L);
        list.add(proj);

        expect(pm.getProjectObjects(Permissions.BROWSE, user.getDirectoryUser())).andReturn(list);

        replay(fac, jac, srs, pm, proj);
        Response response = resource.getFilters(true, true);
        FiltersAndProjectsResource.OptionList ol = (FiltersAndProjectsResource.OptionList) response.getEntity();
        Collection<FiltersAndProjectsResource.GroupWrapper> collection = ol.getOptions();
        FiltersAndProjectsResource.GroupWrapper groupWrapper1 = collection.iterator().next();
        Iterator<FiltersAndProjectsResource.GroupWrapper> wrapperIterator = collection.iterator();
        wrapperIterator.next();

        FiltersAndProjectsResource.GroupWrapper groupWrapper2 = wrapperIterator.next();
        FiltersAndProjectsResource.OptionGroup group = groupWrapper1.getGroup();
        assertEquals("Filter", group.getLabel());
        Collection<FiltersAndProjectsResource.Option> os = group.getOptions();
        FiltersAndProjectsResource.Option opt = os.iterator().next();
        assertEquals("name", opt.getLabel());
        assertEquals("filter-1", opt.getValue());

        FiltersAndProjectsResource.OptionGroup group2 = groupWrapper2.getGroup();
        assertEquals("projects", group2.getLabel());
        Collection<FiltersAndProjectsResource.Option> os2 = group2.getOptions();
        FiltersAndProjectsResource.Option opt2 = os2.iterator().next();
        assertEquals("proj", opt2.getLabel());
        assertEquals("project-2", opt2.getValue());
    }
}

