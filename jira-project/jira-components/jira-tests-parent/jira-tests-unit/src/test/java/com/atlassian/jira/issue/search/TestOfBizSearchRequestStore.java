package com.atlassian.jira.issue.search;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.jql.parser.JqlQueryParser;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.util.MockUserManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.Visitor;

import junit.framework.TestCase;

import static org.mockito.Mockito.mock;

/**
 * @since v5.2
 */
public class TestOfBizSearchRequestStore extends TestCase
{
    private MockUserManager userManager;

    @Override
    public void setUp() throws Exception
    {
        userManager = new MockUserManager();
        new MockComponentWorker().addMock(UserManager.class, userManager).init();
    }

    public void testVisitAll() throws Exception
    {
        OfBizDelegator mockOfBizDelegator = new MockOfBizDelegator();
        SearchService searchService = mock(SearchService.class);

        final JqlQueryParser jqlQueryParser = mock(JqlQueryParser.class);
        OfBizSearchRequestStore ofBizSearchRequestStore = new OfBizSearchRequestStore(mockOfBizDelegator, jqlQueryParser, searchService, userManager);
        ofBizSearchRequestStore.create(searchRequest("My Search Request"));
        ofBizSearchRequestStore.create(searchRequest("Another Search Request"));

        final Set<String> requestNames = new HashSet<String>();
        ofBizSearchRequestStore.visitAll(new Visitor<SearchRequestEntity>()
        {
            @Override
            public void visit(SearchRequestEntity element)
            {
                requestNames.add(element.getName());
            }
        });

        assertEquals(2, requestNames.size());
        assertTrue(requestNames.contains("My Search Request"));
        assertTrue(requestNames.contains("Another Search Request"));
    }

    public void test_findByNameIgnoreCase() throws Exception
    {
        OfBizDelegator mockOfBizDelegator = new MockOfBizDelegator();
        SearchService searchService = mock(SearchService.class);

        final JqlQueryParser jqlQueryParser = mock(JqlQueryParser.class);
        OfBizSearchRequestStore ofBizSearchRequestStore = new OfBizSearchRequestStore(mockOfBizDelegator, jqlQueryParser, searchService, userManager);
        ofBizSearchRequestStore.create(searchRequest("My Search Request"));
        ofBizSearchRequestStore.create(searchRequest("Another Search Request"));

        List<SearchRequest> results = ofBizSearchRequestStore.findByNameIgnoreCase("my search");
        assertEquals(0, results.size());

        results = ofBizSearchRequestStore.findByNameIgnoreCase("my search Request");
        assertEquals(1, results.size());
        assertEquals("My Search Request", results.get(0).getName());
    }

    public void test_update_nameLower() throws Exception
    {
        OfBizDelegator mockOfBizDelegator = new MockOfBizDelegator();
        SearchService searchService = mock(SearchService.class);

        final JqlQueryParser jqlQueryParser = mock(JqlQueryParser.class);
        OfBizSearchRequestStore ofBizSearchRequestStore = new OfBizSearchRequestStore(mockOfBizDelegator, jqlQueryParser, searchService, userManager);
        SearchRequest searchRequest = ofBizSearchRequestStore.create(searchRequest("My Search Request"));
        searchRequest.setName("Blah Blah");
        ofBizSearchRequestStore.update(searchRequest);

        List<SearchRequest> results = ofBizSearchRequestStore.findByNameIgnoreCase("my search");
        assertEquals(0, ofBizSearchRequestStore.findByNameIgnoreCase("My Search Request").size());
        assertEquals(1, ofBizSearchRequestStore.findByNameIgnoreCase("blah BLAH").size());
    }

    private SearchRequest searchRequest(String name)
    {
        return new SearchRequest(null, new MockApplicationUser("fred"), name, "SearchRequest for " + name);
    }
}
