package com.atlassian.jira.issue.search.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.atlassian.cache.CacheManager;
import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.core.test.util.DuckTypeProxy;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.QueryCache;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.SearchableField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.SearchHandler;
import com.atlassian.jira.issue.search.SystemClauseHandlerFactory;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.search.searchers.MockCustomFieldSearcher;
import com.atlassian.jira.issue.search.searchers.SearcherGroup;
import com.atlassian.jira.issue.search.searchers.SearcherGroupType;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.jql.ClauseHandler;
import com.atlassian.jira.jql.ClauseInformation;
import com.atlassian.jira.jql.DefaultClauseHandler;
import com.atlassian.jira.jql.permission.ClausePermissionHandler;
import com.atlassian.jira.jql.query.ClauseQueryFactory;
import com.atlassian.jira.jql.validator.ClauseValidator;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.issue.fields.MockSearchableField;
import com.atlassian.jira.mock.issue.search.searchers.information.MockSearcherInformation;
import com.atlassian.jira.mock.jql.MockClauseInformation;
import com.atlassian.jira.mock.jql.context.MockClauseContextFactory;
import com.atlassian.jira.mock.jql.query.MockClauseQueryFactory;
import com.atlassian.jira.mock.jql.validator.MockClausePermissionHandler;
import com.atlassian.jira.mock.jql.validator.MockClauseValidator;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.easymock.classextension.EasyMock;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test for {@link com.atlassian.jira.issue.search.managers.DefaultSearchHandlerManager}.
 *
 * @since v4.0
 */
public class TestDefaultSearchHandlerManager extends MockControllerTestCase
{
    @Test
    public void testConstructor() throws Exception
    {
        final FieldManager fieldManager = mockController.getMock(FieldManager.class);
        final CustomFieldManager customFieldManager = mockController.getMock(CustomFieldManager.class);
        final SystemClauseHandlerFactory systemClauseHandlerFactory = mockController.getMock(SystemClauseHandlerFactory.class);
        final CacheManager cacheManager = new MemoryCacheManager();

        mockController.replay();
        try
        {
            new DefaultSearchHandlerManager(fieldManager, null, systemClauseHandlerFactory, null, cacheManager);
            fail("Should not accept null custom field manager.");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            new DefaultSearchHandlerManager(null, customFieldManager, systemClauseHandlerFactory, null, cacheManager);
            fail("Should not accept null field manager.");
        }
        catch (IllegalArgumentException iae)
        {
            //expected.
        }

        try
        {
            new DefaultSearchHandlerManager(fieldManager, customFieldManager, null, null, cacheManager);
            fail("Should not accept null field manager.");
        }
        catch (IllegalArgumentException iae)
        {
            //expected.
        }

        mockController.verify();
    }

    /*
     * Test to make sure that system fields are registered correctly.
     */
    @Test
    public void testSystemFields() throws Exception
    {
        final SearchableField field1 = createSystemField("system1", new TestSearcherRego("searcher1", SearcherGroupType.TEXT, new TestClauseRego(asSet("sys1", "sys1aLias"), "system1")));
        final SearchableField field2 = createSystemField("sYstem2", new TestSearcherRego("searcher2", SearcherGroupType.TEXT, new TestClauseRego(asSet("sys2"), "sYstem2"), new TestClauseRego(asSet("sys2other"), "BLAH")));
        final SearchableField field3 = createSystemField("System3", null, new TestClauseRego(asSet("Sys3other"), "System3"));
        final SearchableField field4 = createSystemField("SYSTEM4", new TestSearcherRego("searcher4", SearcherGroupType.DATE, new TestClauseRego(asSet("SYS4"), "SYSTEM4")));

        createFieldManager(field1, field2, field3, field4);
        createCustomFieldManager();
        createSystemClauseHandlerFactory();
        createCacheManager();

        /*
         Clause Handler Map:

            sys1, sys1alias -> ClauseHandler1
            sys2 -> ClauseHandelr2
            sys2Other -> ClauseHandler3
            Sys3other -> ClauseHandler4
            sys4 -> ClauseHandelr5

         Searcher Map:
            searcher1 -> searcher1
            searcher2 -> searcher2
            SYSTEM4 -> searcher3

         Searcher Group Map:
            TEXT -> searcher1, searcher2
            DATE -> searcher4

         Searcher By Clause:
            sys1, sys1alias -> searcher1
            sys2 -> searcher2
            sys4 -> searcher4
         */
        createQueryCacheMock();

        final DefaultSearchHandlerManager defaultSearchHandlerManager = mockController.instantiate(DefaultSearchHandlerManager.class);

        assertSearchers(defaultSearchHandlerManager, field1, field2, field3, field4);
        assertClauseHandlers(defaultSearchHandlerManager, false, field1, field2, field3, field4);
        assertSearchersByClauseName(defaultSearchHandlerManager, field1, field2, field3, field4);
        assertClauseNamesByField(defaultSearchHandlerManager, field1, field2, field3, field4);
        assertFieldByClauseNames(defaultSearchHandlerManager, false, field1, field2, field3, field4);

        assertTrue(defaultSearchHandlerManager.getClauseHandler(null, "noid").isEmpty());
        assertTrue(defaultSearchHandlerManager.getSearchersByClauseName(null, "noid").isEmpty());
        assertNull(defaultSearchHandlerManager.getSearcher("noid"));
        assertNull(defaultSearchHandlerManager.getSearcher("system4"));

        mockController.verify();
    }

    /*
     * Test to ensure that handlers we don't have permission to use are not returned.
     */
    @Test
    public void testSystemFieldsNoPermissionOverrideSecurity() throws Exception
    {
        final SearchRenderer mock = EasyMock.createMock(SearchRenderer.class);
        EasyMock.expect(mock.isShown(null, null)).andReturn(false);
        EasyMock.replay(mock);

        final SearchableField field1 = createSystemField("system1", new TestSearcherRego("searcher1", SearcherGroupType.TEXT, new TestClauseRego(asSet("sys1", "sys1alias"), "system1")));
        final SearchableField field2 = createSystemField("sYstem2", new TestSearcherRego("searcher2", SearcherGroupType.TEXT, mock, new TestClauseRego(asSet("sYs2"), new MockClausePermissionHandler(false), "sYstem2")));
        final SearchableField field3 = createSystemField("System3", null, new TestClauseRego(asSet("Sys3other"), "System3"));
        final SearchableField field4 = createSystemField("SYSTEM4", new TestSearcherRego("searcher4", SearcherGroupType.DATE, new TestClauseRego(asSet("SYS4"), "SYSTEM4")));
        createCacheManager();

        /*
         Clause Handler Map:

             sys1, sys1alias -> ClauseHandler1
             sys2 -> ClauseHandelr2 {This one is not visible because we don't have permission}.
             sys2Other -> ClauseHandler3
             Sys3other -> ClauseHandler4
             sys4 -> ClauseHandelr5

         Searcher Map:

             searcher1 -> searcher1
             searcher2 -> searcher2
             SYSTEM4 -> searcher3

         Searcher Group Map:
            TEXT -> searcher1, searcher2
            DATE -> searcher4

         Searcher By Clause:
            sys1, sys1alias -> searcher1
            sys2 -> searcher2 {This one is not visible because we don't have permission}.
            sys4 -> searcher4
         */


        createFieldManager(field1, field2, field3, field4);
        createCustomFieldManager();
        createSystemClauseHandlerFactory();

        final DefaultSearchHandlerManager defaultSearchHandlerManager = mockController.instantiate(DefaultSearchHandlerManager.class);

        //All searchers should be visible.
        assertSearchers(defaultSearchHandlerManager, field1, field2, field3, field4);

        //Note that we don't pass in feidl2 as we don't have permission to see it.
        assertClauseHandlers(defaultSearchHandlerManager, true, field1, field2, field3, field4);
        assertFieldByClauseNames(defaultSearchHandlerManager, true, field1, field2, field3, field4);

        //Make sure we can't see sys2 directly.
        assertFalse(defaultSearchHandlerManager.getClauseHandler("sys2").isEmpty());

        mockController.verify();
        EasyMock.verify();
    }

    /*
     * Test to make sure system and custom fields play nice together. We are also testing that a clause without a field
     * will also work.
     */
    @Test
    public void testSystemAndCustomFields() throws Exception
    {
        final SearchableField field1 = createSystemField("system1", new TestSearcherRego("searCher1", SearcherGroupType.DATE, new TestClauseRego(asSet("sys1", "sys1alias"), "system1")));
        final CustomField customfield1 = createCustomField("CUSTOM1", new TestSearcherRego("custom1", SearcherGroupType.CUSTOM, new TestClauseRego(asSet("cf[101]", "dylan"), "CUSTOM1")));

        //Note
        // - That this custom field is trying to register over a system field. This request should be silently ignore.
        // - This custom field uses the same alias as customfield1. This request should succeed.
        // - This custom field is trying to register in the TEXT group. This request should be ignored.
        final CustomField customfield2 = createCustomField("cuStom2", new TestSearcherRego("custom2", SearcherGroupType.TEXT, new TestClauseRego(asSet("cf[1013]", "dylan", "sys1"), "cuStom2")), new TestClauseRego(asSet("sys1alias"), "BLAH"));

        //Here is a JQL clause without a field.
        final SearchHandler systemHandler = createSearchHandler(new TestClauseRego(asSet("system2"), "BLEE"));
        createCacheManager();

        /*
           Clause Handler Map:

               sys1, sys1alias -> ClauseHandler1
               dylan -> {ClauseHandler2, ClauseHandler 3}
               cf[101] -> ClauseHandler2
               cf[1013] -> ClauseHandler3
               system2 -> ClauseHandler4

           Searcher Map:

               searCher1 -> searcher1
               custom1 -> searcher2
               custom2 -> searcher3

           Searcher Group:
               CUSTOM -> {searcher2, searcher3}
               DATE -> {searcher1}

           Searcher By Clause:
              sys1, sys1alias -> searcher1
              dylan -> searcher2, searcher3
              cf[101] -> searcher2
              cf[1013] -> searcher3
        */
        createQueryCacheMock();

        createFieldManager(field1);
        createCustomFieldManager(customfield1, customfield2);
        createSystemClauseHandlerFactory(systemHandler);

        final DefaultSearchHandlerManager defaultSearchHandlerManager = mockController.instantiate(DefaultSearchHandlerManager.class);

        assertSearchers(defaultSearchHandlerManager, field1, customfield1, customfield2);
        assertClauseHandlers(defaultSearchHandlerManager, false, asList(systemHandler), asList(field1, customfield1, customfield2));
        assertSearchersByClauseName(defaultSearchHandlerManager, field1, customfield1, customfield2);
        assertClauseNamesByField(defaultSearchHandlerManager, field1, customfield1, customfield2);
        assertFieldByClauseNames(defaultSearchHandlerManager, false, field1, customfield1, customfield2);
        assertNotNull(defaultSearchHandlerManager.getClauseHandler(null, "sillyTest"));
    }

    @Test
    public void testEmpty()
    {
        createFieldManager();
        createCustomFieldManager();
        createSystemClauseHandlerFactory();
        createCacheManager();

        final QueryCache mockQueryCache = mockController.getMock(QueryCache.class);
        mockQueryCache.getClauseHandlers(this.<User>anyObject(), anyString());
        expectLastCall().andReturn(null).anyTimes() ;
        mockQueryCache.setClauseHandlers(this.<User>anyObject(), anyString(), this.<Collection<ClauseHandler>>anyObject());
        expectLastCall().anyTimes();

        final DefaultSearchHandlerManager defaultSearchHandlerManager = mockController.instantiate(DefaultSearchHandlerManager.class);

        assertTrue(defaultSearchHandlerManager.getAllSearchers().isEmpty());
        assertTrue(defaultSearchHandlerManager.getClauseHandler(null, "noid").isEmpty());
        assertTrue(defaultSearchHandlerManager.getSearchersByClauseName(null, "noid").isEmpty());
        assertNull(defaultSearchHandlerManager.getSearcher("noid"));

        //Even though there are no searchers, the searcher groups still exists.
        assertSearchers(defaultSearchHandlerManager);

        mockController.verify();
    }

    /*
     * Make sure the manager throws an exception when trying to register a field of the same name.
     */
    @Test
    public void testSameSystemClauseName() throws Exception
    {
        final SearchableField field1 = createSystemField("system1", new TestSearcherRego("searcher1", SearcherGroupType.TEXT, new TestClauseRego(asSet("sys1", "sys1alias"), "system1")));
        final SearchableField field2 = createSystemField("sysTem2", new TestSearcherRego("searcher2", SearcherGroupType.TEXT, new TestClauseRego(asSet("sys1", "sys1alias"), "sysTem2")));
        createCacheManager();

        createFieldManager(field1, field2);
        final DefaultSearchHandlerManager defaultSearchHandlerManager = mockController.instantiate(DefaultSearchHandlerManager.class);

        try
        {
            defaultSearchHandlerManager.getAllSearchers();
            fail("Should not be able to register the same JQL clause name twice for system fields.");
        }
        catch (RuntimeException e)
        {
            //expected
        }

        mockController.verify();
    }

    /*
     * Make sure the manager is correctly refreshed.
     */
    @Test
    public void testRefresh() throws Exception
    {
        final SearchableField field1 = createSystemField("SYStem1", new TestSearcherRego("searcher1", SearcherGroupType.TEXT, new TestClauseRego(asSet("sys1", "S1alias"), "SYStem1")));
        final SearchableField field2 = createSystemField("sysTEm2", new TestSearcherRego("searcher2", SearcherGroupType.PROJECT, new TestClauseRego(asSet("sys2"), "sysTEm2"), new TestClauseRego(asSet("sys2other"), "BLAH")));
        final SearchableField field3 = createSystemField("system3", null, new TestClauseRego(asSet("sys3other"), "system3"));
        final SearchableField field4 = createSystemField("system4", new TestSearcherRego("searcher4", SearcherGroupType.DATE, new TestClauseRego(asSet("sys4"), "system4")));
        final SearchHandler systemHandler = createSearchHandler(new TestClauseRego(asSet("system6"), "BLEE"));
        createCacheManager();

        final CustomFieldManager customFieldManager = mockController.getMock(CustomFieldManager.class);
        customFieldManager.getCustomFieldObjects();
        mockController.setReturnValue(Collections.emptyList());
        mockController.setReturnValue(Collections.emptyList());

        final FieldManager fieldManager = mockController.getMock(FieldManager.class);
        fieldManager.getSystemSearchableFields();
        mockController.setReturnValue(asSet(field1, field2));
        mockController.setReturnValue(asSet(field1, field2, field3, field4));

        final SystemClauseHandlerFactory systemClauseHandlerFactory = mockController.getMock(SystemClauseHandlerFactory.class);
        systemClauseHandlerFactory.getSystemClauseSearchHandlers();
        mockController.setReturnValue(Collections.singletonList(systemHandler));
        mockController.setReturnValue(Collections.emptyList());

        /*
         Clause Handler Map:

             sys1, S1alias -> ClauseHandler1
             sys2 -> ClauseHandeler2
             sys2other -> ClauseHandle3
             system6 -> ClauseHandler6

         Searcher Map:

             searcher1 -> searcher1
             searcher2 -> searcher2

         Searcher Group Map:
            TEXT -> searcher1
            PROJECT_ID -> searcher2

         Searcher By Clause:
            sys1, sys1alias -> searcher1
            sys2 -> searcher2
         */
        createQueryCacheMock();

        final DefaultSearchHandlerManager defaultSearchHandlerManager = mockController.instantiate(DefaultSearchHandlerManager.class);

        assertClauseHandlers(defaultSearchHandlerManager, false, asList(systemHandler), asList(field1, field2));
        assertSearchers(defaultSearchHandlerManager, field1, field2);
        assertSearchersByClauseName(defaultSearchHandlerManager, field1, field2);
        assertClauseNamesByField(defaultSearchHandlerManager, field1, field2);
        assertFieldByClauseNames(defaultSearchHandlerManager, false, field1, field2);

        defaultSearchHandlerManager.refresh();

        /*
         Clause Handler Map:

             sys1, S1alias -> ClauseHandler1
             sys2 -> ClauseHandler2
             sys2other -> ClauseHandler3
             sys3 -> ClauseHandler4
             sys4 -> ClauseHandler5
             system6 -> ClauseHandler6

         Searcher Map:

             searcher1 -> searcher1
             searcher2 -> searcher2
             searcher4 -> searcher4

         Searcher Group Map:
            TEXT -> searcher1
            PROJECT_ID_2 -> searcher2
            DATE -> searcher4

         Searcher By Clause:
            sys1, sys1alias -> searcher1
            sys2 -> searcher2
            sys4 -> searcher4
         */

        assertClauseHandlers(defaultSearchHandlerManager, false, field1, field2, field3, field4);
        assertSearchers(defaultSearchHandlerManager, field1, field2, field3, field4);
        assertSearchersByClauseName(defaultSearchHandlerManager, field1, field2, field3, field4);
        assertClauseNamesByField(defaultSearchHandlerManager, field1, field2, field3, field4);
        assertFieldByClauseNames(defaultSearchHandlerManager, false, field1, field2, field3, field4);

        mockController.verify();
    }

    private void createQueryCacheMock()
    {
        final QueryCache mockQueryCache = mockController.getNiceMock(QueryCache.class);
        mockQueryCache.getClauseHandlers(this.<User>anyObject(), anyString());
        expectLastCall().andReturn(null).anyTimes() ;
        mockQueryCache.getClauseHandlers(this.<User>isNull(), anyString());
        expectLastCall().andReturn(null).anyTimes() ;
        mockQueryCache.setClauseHandlers(this.<User>anyObject(), anyString(), this.<Collection<ClauseHandler>>anyObject());
        expectLastCall().anyTimes();
        mockQueryCache.setClauseHandlers(this.<User>isNull(), anyString(), this.<Collection<ClauseHandler>>anyObject());
        expectLastCall().anyTimes();
    }

    /*
     * Test to see what happens if two searchers with the same id registered.
     */
    @Test
    public void testSameSearcherId() throws Exception
    {
        final SearchableField field1 = createSystemField("system1", new TestSearcherRego("searcher1", SearcherGroupType.TEXT, new TestClauseRego(asSet("sys1", "sys1alias"), "system1")));

        final TestClauseRego sys2rego = new TestClauseRego(asSet("sys2"), "BLAH");
        final TestClauseRego sys2other = new TestClauseRego(asSet("sys2other"), "BLEE");
        final SearchableField field2 = createSystemField("system2", new TestSearcherRego("searcher1", SearcherGroupType.TEXT, sys2rego, sys2other));

        //With a duplicate searcher id, the searcher is ignored. In this case this is actually what the manager will register (i.e. the searcher will be ignored, but its clauses will be registered).
        final SearchableField field2actual = createSystemField("system2", null, sys2rego, sys2other);
        createCacheManager();

        /*
         Clause Handler Map:

             sys1, sys1alias -> ClauseHandler1
             sys2 -> ClauseHandler2
             sys2other -> ClauseHandler3

         Searcher Map:

             searcher1 -> searcher1

         Searcher Group Map:
            TEXT -> searcher1

         Searcher By Clause:
            sys1, sys1alias -> searcher1
            sys2 -> searcher2
            sys2other ->searcher2
         */
        createQueryCacheMock();

        createCustomFieldManager();
        createFieldManager(field1, field2);
        createSystemClauseHandlerFactory();

        final DefaultSearchHandlerManager defaultSearchHandlerManager = mockController.instantiate(DefaultSearchHandlerManager.class);
        assertSearchers(defaultSearchHandlerManager, field1, field2actual);
        assertClauseHandlers(defaultSearchHandlerManager, false, field1, field2);
        assertSearchersByClauseName(defaultSearchHandlerManager, field1, field2);
        assertClauseNamesByField(defaultSearchHandlerManager, field1, field2);
        assertFieldByClauseNames(defaultSearchHandlerManager, false, field1, field2);

        mockController.verify();
    }

    @Test
    public void testBadArguments() throws Exception
    {
        createQueryCacheMock();
        createCacheManager();

        final DefaultSearchHandlerManager defaultSearchHandlerManager = mockController.instantiate(DefaultSearchHandlerManager.class);
        try
        {
            defaultSearchHandlerManager.getClauseHandler(null, null);
            fail("Should not accept illegal argument.");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }

        try
        {
            defaultSearchHandlerManager.getClauseHandler(null, "");
            fail("Should not accept illegal argument.");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }

        try
        {
            defaultSearchHandlerManager.getSearcher(null);
            fail("Should not accept illegal argument.");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }

        try
        {
            defaultSearchHandlerManager.getSearcher("");
            fail("Should not accept illegal argument.");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }

        try
        {
            defaultSearchHandlerManager.getSearchers(null, null);
            fail("Should not accept illegal argument.");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }

        try
        {
            defaultSearchHandlerManager.getSearchersByClauseName(null, null);
            fail("Should not accept illegal argument.");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }

        try
        {
            defaultSearchHandlerManager.getSearchersByClauseName(null, "");
            fail("Should not accept illegal argument.");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }
    }

    @Test
    public void testGetSearchers()
    {
        createCacheManager();
        final SearchContext context = mockController.getMock(SearchContext.class);
        context.verify();

        final SearchRenderer mock = mockController.getMock(SearchRenderer.class);
        mock.isShown(null, context);
        mockController.setReturnValue(false);
        mock.isShown(null, context);
        mockController.setReturnValue(true);

        final SearchableField field2 = createSystemField("system2", new TestSearcherRego("searcher2", SearcherGroupType.CONTEXT, mock), new TestClauseRego(asSet("another", "random", "set", "of", "string"), "system2"));
        final TestSearcherRego searcherRego = new TestSearcherRego("custom1", SearcherGroupType.CUSTOM, mock, new TestClauseRego(asSet("cf[101]", "brenden"), "custom1"));
        final CustomField customfield1 = createCustomField("custom1", searcherRego);

        createFieldManager(field2);
        createCustomFieldManager(customfield1);
        createSystemClauseHandlerFactory();

        final DefaultSearchHandlerManager defaultSearchHandlerManager = mockController.instantiate(DefaultSearchHandlerManager.class);

        assertCollectionEqualsIgnoreOrder(Collections.singletonList(searcherRego.getSearcher()), defaultSearchHandlerManager.getSearchers(null, context));

        mockController.verify();
    }

    /**
     * Make sure the passed DefaultSearchHandlerManager is returning the correct lookups for ClauseHandlers if JIRA were
     * initialised with the passed SearchableFields and system SearchHandlers.
     *
     * @param defaultSearchHandlerManager the manager to check.
     * @param overrideSecurity
     * @param systemHandlers the system SearchHandlers
     * @param fields the system's SearchableFields.
     */
    private static void assertClauseHandlers(final DefaultSearchHandlerManager defaultSearchHandlerManager,
            final boolean overrideSecurity, Collection<SearchHandler> systemHandlers,
            Collection<SearchableField> fields)
    {
        //create a map of clause handlers that we expect the passed manager to generate.
        ClauseHandlerIndexer indexer = new ClauseHandlerIndexer();
        for (SearchableField field : fields)
        {
            indexer.indexField(field);
        }
        indexer.indexSystemHandlers(systemHandlers);

        //Loop through the expected map and make sure the passed defaultSearchHandlerManager meets our expectations.
        for (Map.Entry<String, List<ClauseHandler>> entry : indexer.getNameMap().entrySet())
        {
            if (overrideSecurity)
            {
                assertCollectionEqualsIgnoreOrder(entry.getValue(), defaultSearchHandlerManager.getClauseHandler(entry.getKey()));
            }
            else
            {
                assertCollectionEqualsIgnoreOrder(entry.getValue(), defaultSearchHandlerManager.getClauseHandler(null, entry.getKey()));
            }
        }
    }

    private static void assertClauseHandlers(final DefaultSearchHandlerManager defaultSearchHandlerManager, final boolean overrideSecurity, final SearchableField... fields)
    {
        assertClauseHandlers(defaultSearchHandlerManager, overrideSecurity, Collections.<SearchHandler>emptyList(), Arrays.asList(fields));
    }

    /**
     * Make sure the passed DefaultSearchHandlerManager is returning the correct lookups for Searchers by their id if
     * JIRA were initialised with the passed SearchableFields. It also checks that the managers correctly returns the
     * correct SearcherGroup information.
     *
     * @param defaultSearchHandlerManager the manager to test.
     * @param fields the fields the searchers.
     */
    private static void assertSearchers(final DefaultSearchHandlerManager defaultSearchHandlerManager, final SearchableField... fields)
    {
        List<IssueSearcher<?>> searchers = new ArrayList<IssueSearcher<?>>(fields.length);
        Map<String, IssueSearcher> searcherMap = new HashMap<String, IssueSearcher>();
        Map<SearcherGroupType, List<IssueSearcher<?>>> groups = new EnumMap<SearcherGroupType, List<IssueSearcher<?>>>(SearcherGroupType.class);
        for (SearcherGroupType type : SearcherGroupType.values())
        {
            groups.put(type, new ArrayList<IssueSearcher<?>>());
        }

        for (SearchableField field : fields)
        {
            final SearchHandler searchHandler = field.createAssociatedSearchHandler();
            final List<IssueSearcher<?>> currentSearchers = new ArrayList<IssueSearcher<?>>();

            //Find all searchers for the current field.
            if (searchHandler != null)
            {
                //New Field.
                final SearchHandler.SearcherRegistration registration = searchHandler.getSearcherRegistration();
                if (registration != null)
                {
                    currentSearchers.add(registration.getIssueSearcher());
                }
            }

            //Create the expected searcherId -> searcher and SearcherGroup -> Searcher maps.

            for (IssueSearcher<?> currentSearcher : currentSearchers)
            {
                final String searcherId = currentSearcher.getSearchInformation().getId();

                //Only the first searcher with the id is registered.
                if (!searcherMap.containsKey(searcherId))
                {
                    searcherMap.put(searcherId, currentSearcher);
                    searchers.add(currentSearcher);
                    SearcherGroupType group = currentSearcher.getSearchInformation().getSearcherGroupType();
                    if (!isCustomField(field))
                    {
                        //System fields without a group get placed in custom because there is not better place.
                        if (group == null)
                        {
                            group = SearcherGroupType.CUSTOM;
                        }
                    }
                    else
                    {
                        //Custom fields must always be in the custom group.
                        group = SearcherGroupType.CUSTOM;
                    }
                    groups.get(group).add(currentSearcher);
                }
            }
        }

        //Make sure we can list all searchers.
        assertCollectionEqualsIgnoreOrder(searchers, defaultSearchHandlerManager.getAllSearchers());

        //Make sure each searcher is mapped to its id correctly.
        for (Map.Entry<String, IssueSearcher> entry : searcherMap.entrySet())
        {
            assertSame(entry.getValue(), defaultSearchHandlerManager.getSearcher(entry.getKey()));
        }

        //Make sure each searcher group is as expected.
        Set<SearcherGroupType> types = EnumSet.allOf(SearcherGroupType.class);
        for (SearcherGroup group : defaultSearchHandlerManager.getSearcherGroups())
        {
            assertEquals(groups.get(group.getType()), group.getSearchers());
            types.remove(group.getType());
        }
        //Make sure there not no spurious searcher groups.
        assertTrue(types.isEmpty());
    }

    /**
     * Make sure that the passed manager will return the correct searchers when asked for by JQL name.
     *
     * @param defaultSearchHandlerManager the manager to check.
     * @param fields the fields with searchers.
     */
    private static void assertSearchersByClauseName(final DefaultSearchHandlerManager defaultSearchHandlerManager, final SearchableField... fields)
    {
        Set<String> systemClauses = new HashSet<String>();
        final Map<String, List<IssueSearcher<?>>> testMap = new HashMap<String, List<IssueSearcher<?>>>();

        //Create the expected map of JQL clause name -> searcher.
        for (SearchableField field : fields)
        {
            final SearchHandler handler = field.createAssociatedSearchHandler();
            if (handler != null)
            {
                final SearchHandler.SearcherRegistration registration = handler.getSearcherRegistration();
                if (registration != null)
                {
                    final List<SearchHandler.ClauseRegistration> clauseRegistrations = registration.getClauseHandlers();
                    for (SearchHandler.ClauseRegistration clauseRegistration : clauseRegistrations)
                    {
                        final Set<String> names = clauseRegistration.getHandler().getInformation().getJqlClauseNames().getJqlFieldNames();
                        for (String name : names)
                        {
                            List<IssueSearcher<?>> list = testMap.get(name);
                            if (list == null)
                            {
                                list = new ArrayList<IssueSearcher<?>>();
                                testMap.put(name, list);
                            }

                            if (!isCustomField(field) || !systemClauses.contains(name))
                            {
                                list.add(registration.getIssueSearcher());
                            }

                            if (!isCustomField(field))
                            {
                                systemClauses.add(name);
                            }
                        }
                    }
                }
            }
        }

        //Make sure the manager correctly returns for each lookup.
        for (Map.Entry<String, List<IssueSearcher<?>>> entry : testMap.entrySet())
        {
            final Collection<IssueSearcher<?>> searchersByClauseName = defaultSearchHandlerManager.getSearchersByClauseName(null, entry.getKey());
            assertCollectionEqualsIgnoreOrder(entry.getValue(), searchersByClauseName);
        }
    }

    /**
     * Make sure that the passed manager will return the correct clause names when asked for by field name.
     *
     * @param defaultSearchHandlerManager the manager to check.
     * @param fields the fields with searchers.
     */
    private static void assertClauseNamesByField(final DefaultSearchHandlerManager defaultSearchHandlerManager, final SearchableField... fields)
    {
        ClauseHandlerIndexer indexer = new ClauseHandlerIndexer();
        for (SearchableField field : fields)
        {
            indexer.indexField(field);
        }

        // JQLClauseName -> ClauseHandlers
        final Map<String, List<ClauseHandler>> listMap = indexer.getNameMap();
        final Map<String, List<ClauseNames>> testMap = new HashMap<String, List<ClauseNames>>();


        for (List<ClauseHandler> handlers : listMap.values())
        {
            for (ClauseHandler handler : handlers)
            {
                final ClauseInformation information = handler.getInformation();
                if (information.getFieldId() != null)
                {
                    List<ClauseNames> names = testMap.get(information.getFieldId());
                    if (names == null)
                    {
                        names = new ArrayList<ClauseNames>();
                        testMap.put(information.getFieldId(), names);
                    }

                    names.add(information.getJqlClauseNames());
                }
            }
        }

        //Make sure the manager correctly returns for each lookup.
        for (Map.Entry<String, List<ClauseNames>> entry : testMap.entrySet())
        {
            final Collection<ClauseNames> foundClauseNames = defaultSearchHandlerManager.getJqlClauseNames(entry.getKey());
            assertCollectionEqualsIgnoreOrder(entry.getValue(), foundClauseNames);
        }
    }

    /**
     * Make sure that the passed manager will return the correct clause names when asked for by field name.
     *
     * @param defaultSearchHandlerManager the manager to check.
     * @param overrideSecurity
     * @param fields the fields with searchers.
     */
    private static void assertFieldByClauseNames(final DefaultSearchHandlerManager defaultSearchHandlerManager, final boolean overrideSecurity, final SearchableField... fields)
    {
        ClauseHandlerIndexer indexer = new ClauseHandlerIndexer();
        for (SearchableField field : fields)
        {
            indexer.indexField(field);
        }

        // JQLClauseName -> ClauseHandlers
        final Map<String, List<ClauseHandler>> listMap = indexer.getNameMap();

        for (String clauseName : listMap.keySet())
        {
            final Collection<ClauseHandler> handlers = listMap.get(clauseName);

            Collection<String> ids = new ArrayList<String>(handlers.size());
            for (ClauseHandler handler : handlers)
            {
                final String fieldId = handler.getInformation().getFieldId();
                if (fieldId != null)
                {
                    ids.add(fieldId);
                }
            }

            if (overrideSecurity)
            {
                assertCollectionEqualsIgnoreOrder(ids, defaultSearchHandlerManager.getFieldIds(clauseName));
            }
            else
            {
                assertCollectionEqualsIgnoreOrder(ids, defaultSearchHandlerManager.getFieldIds(null, clauseName));
            }
        }
    }

    private static boolean isCustomField(final SearchableField field)
    {
        return field instanceof CustomField;
    }

    private static <T> void assertCollectionEqualsIgnoreOrder(Collection<T> left, Collection<?> right)
    {
        assertEquals(left.size(), right.size());
        assertTrue(left.containsAll(right));
    }

    private CustomFieldManager createCustomFieldManager(final CustomField... fields)
    {
        final CustomFieldManager customFieldManager = mockController.getMock(CustomFieldManager.class);
        customFieldManager.getCustomFieldObjects();
        mockController.setReturnValue(asList(fields));

        return customFieldManager;
    }

    private CacheManager createCacheManager()
    {
        final MemoryCacheManager cacheManager = new MemoryCacheManager();
        mockController.addObjectInstance(cacheManager);

        return cacheManager;
    }

    private SystemClauseHandlerFactory createSystemClauseHandlerFactory(SearchHandler... handlers)
    {
        final SystemClauseHandlerFactory clauseHandlerFactory = mockController.getMock(SystemClauseHandlerFactory.class);
        clauseHandlerFactory.getSystemClauseSearchHandlers();
        mockController.setReturnValue(Arrays.asList(handlers));

        return clauseHandlerFactory;
    }

    private FieldManager createFieldManager(final SearchableField... fields)
    {
        final FieldManager fieldManager = mockController.getMock(FieldManager.class);
        fieldManager.getSystemSearchableFields();
        mockController.setReturnValue(asSet(fields));

        return fieldManager;
    }

    /**
     * Helper class used to create a map from JQL clause name -> ClauseHandler.
     */
    private static class ClauseHandlerIndexer
    {
        private final Map<String, List<ClauseHandler>> nameMap = new HashMap<String, List<ClauseHandler>>();
        private final Set<String> systemClauses = new HashSet<String>();

        public Map<String, List<ClauseHandler>> getNameMap()
        {
            return nameMap;
        }

        public void indexField(SearchableField field)
        {
            if (isCustomField(field))
            {
                indexField(field, false);
            }
            else
            {
                indexField(field, true);
            }
        }

        public void indexSystemHandlers(Collection<SearchHandler> clauseRegistration)
        {
            for (SearchHandler handler : clauseRegistration)
            {
                indexClauseRegistrations(handler.getClauseRegistrations(), true);
            }
        }

        private void indexClauseRegistrations(Collection<SearchHandler.ClauseRegistration> clauseRegistrations, boolean system)
        {
            for (SearchHandler.ClauseRegistration clauseRegistration : clauseRegistrations)
            {
                for (String name : clauseRegistration.getHandler().getInformation().getJqlClauseNames().getJqlFieldNames())
                {
                    List<ClauseHandler> handlers = nameMap.get(name);
                    if (handlers == null)
                    {
                        nameMap.put(name, new ArrayList<ClauseHandler>(Collections.singletonList(clauseRegistration.getHandler())));
                    }
                    else
                    {
                        if (system || !systemClauses.contains(name))
                        {
                            handlers.add(clauseRegistration.getHandler());
                        }
                    }
                    if (system)
                    {
                        systemClauses.add(name);
                    }
                }
            }
        }

        private void indexField(SearchableField field, boolean system)
        {
            final SearchHandler searchHandler = field.createAssociatedSearchHandler();
            if (searchHandler != null)
            {
                List<SearchHandler.ClauseRegistration> clauseRegistrations = new ArrayList<SearchHandler.ClauseRegistration>();

                final SearchHandler.SearcherRegistration searcherRegistration = searchHandler.getSearcherRegistration();
                if (searcherRegistration != null)
                {
                    clauseRegistrations.addAll(searcherRegistration.getClauseHandlers());
                }

                if (searchHandler.getClauseRegistrations() != null)
                {
                    clauseRegistrations.addAll(searchHandler.getClauseRegistrations());
                }

                indexClauseRegistrations(clauseRegistrations, system);
            }
        }

    }

    private static class TestClauseRego
    {
        private final ClauseNames names;
        private SearchHandler.ClauseRegistration rego = null;
        private String fieldId = null;
        private final ClausePermissionHandler clausePermissionHandler;

        public TestClauseRego(Set<String> names, ClausePermissionHandler clausePermissionHandler, String fieldId)
        {
            this.clausePermissionHandler = clausePermissionHandler;
            this.names = new ClauseNames(names.iterator().next(), names);
            this.fieldId = fieldId;
        }

        public TestClauseRego(Set<String> names, String fieldId)
        {
            this(names, new MockClausePermissionHandler(), fieldId);
        }

        public ClauseNames getNames()
        {
            return names;
        }

        public SearchHandler.ClauseRegistration getClauseRegistration()
        {
            if (rego == null)
            {
                rego = new SearchHandler.ClauseRegistration(new DefaultClauseHandler(new MockClauseInformation(names, null, fieldId), new MockClauseQueryFactory(), new MockClauseValidator(), clausePermissionHandler, new MockClauseContextFactory()));
            }
            return rego;
        }

        public ClauseQueryFactory getClauseQueryFactory()
        {
            return getClauseRegistration().getHandler().getFactory();
        }

        public ClauseValidator getClauseValidator()
        {
            return getClauseRegistration().getHandler().getValidator();
        }

        public ClauseHandler getClauseHandler()
        {
            return getClauseRegistration().getHandler();
        }
    }

    private class TestSearcherRego
    {
        private final String id;
        private final List<TestClauseRego> clauses;
        private final SearcherGroupType groupType;

        private SearchHandler.SearcherRegistration rego = null;
        private SearchRenderer renderer = null;

        public TestSearcherRego(String id, SearcherGroupType groupType, TestClauseRego... repos)
        {
            final SearchRenderer mock = mockController.getMock(SearchRenderer.class);
            mock.isShown(null, null);
            mockController.setDefaultReturnValue(true);

            this.renderer = mock;
            this.id = id;
            this.groupType = groupType;
            this.clauses = new ArrayList<TestClauseRego>(Arrays.asList(repos));
        }

        public TestSearcherRego(String id, SearcherGroupType groupType, SearchRenderer renderer, TestClauseRego... repos)
        {
            this.id = id;
            this.groupType = groupType;
            this.clauses = new ArrayList<TestClauseRego>(Arrays.asList(repos));
            this.renderer = renderer;
        }

        public String getSearcherId()
        {
            return id;
        }

        public List<TestClauseRego> getClauseRegos()
        {
            return clauses;
        }

        public SearcherGroupType getGroupType()
        {
            return groupType;
        }

        public SearchHandler.SearcherRegistration getSearcherRegistration()
        {
            if (rego == null)
            {
                MockSearcherInformation<CustomField> information = new MockSearcherInformation<CustomField>(getSearcherId());
                MockCustomFieldSearcher searcher = new MockCustomFieldSearcher(getSearcherId());
                information.setSearcherGroupType(groupType);
                searcher.setInformation(information);
                searcher.setRenderer(renderer);

                List<SearchHandler.ClauseRegistration> registration = new ArrayList<SearchHandler.ClauseRegistration>(clauses.size());
                for (TestClauseRego clause : clauses)
                {
                    registration.add(clause.getClauseRegistration());
                }

                rego = new SearchHandler.SearcherRegistration(searcher, registration);
            }
            return rego;
        }

        public IssueSearcher<?> getSearcher()
        {
            return getSearcherRegistration().getIssueSearcher();
        }

        public Collection<SearchHandler.ClauseRegistration> getClauseRegistrations()
        {
            return getSearcherRegistration().getClauseHandlers();
        }
    }

    public CustomField createCustomField(String name, TestSearcherRego searcherRego, TestClauseRego... clauseRegos)
    {
        return makeCustomField(createSystemField(name, searcherRego, clauseRegos));
    }

    private CustomField makeCustomField(final SearchableField systemField)
    {
        Object extraMethods = new Object()
        {
            public CustomFieldType getCustomFieldType()
            {
                return null;
            }
        };

        return (CustomField) DuckTypeProxy.getProxy(CustomField.class, asList(systemField, extraMethods));
    }

    public SearchHandler createSearchHandler(TestClauseRego... clauseRegos)
    {
        List<SearchHandler.ClauseRegistration> registration = new ArrayList<SearchHandler.ClauseRegistration>(clauseRegos.length);
        for (TestClauseRego clause : clauseRegos)
        {
            registration.add(clause.getClauseRegistration());
        }

        return new SearchHandler(Collections.<FieldIndexer>emptyList(), null, registration);
    }

    public SearchableField createSystemField(String name, TestSearcherRego searchRego, TestClauseRego... clauseRegos)
    {
        final MockSearchableField field = new MockSearchableField(name);

        List<SearchHandler.ClauseRegistration> registration = new ArrayList<SearchHandler.ClauseRegistration>(clauseRegos.length);
        for (TestClauseRego clause : clauseRegos)
        {
            registration.add(clause.getClauseRegistration());
        }

        final SearchHandler.SearcherRegistration searcherRegistration = (searchRego == null) ? null : searchRego.getSearcherRegistration();
        field.setSearchHandler(new SearchHandler(Collections.<FieldIndexer>emptyList(), searcherRegistration, registration));
        return field;
    }


    private static <T> Set<T> asSet(T... elements)
    {
        return CollectionBuilder.newBuilder(elements).asListOrderedSet();
    }

    private static <T> List<T> asList(T... elements)
    {
        return CollectionBuilder.newBuilder(elements).asList();
    }

}
