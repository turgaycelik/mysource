package com.atlassian.jira.issue.customfields.searchers.transformer;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.transformer.FieldFlagOperandRegistry;
import com.atlassian.jira.issue.search.searchers.transformer.NavigatorStructureChecker;
import com.atlassian.jira.issue.search.searchers.util.DefaultIndexedInputHelper;
import com.atlassian.jira.issue.search.searchers.util.IndexedInputHelper;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.IndexInfoResolver;
import com.atlassian.jira.jql.resolver.NameResolver;
import com.atlassian.jira.jql.resolver.VersionIndexInfoResolver;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.project.MockVersion;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import org.easymock.EasyMock;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @since v4.0
 */
public class TestVersionCustomFieldSearchInputTransformer extends MockControllerTestCase
{
    private NameResolver<Version> versionResolver;
    private SearchContext searchContext;
    private User theUser = null;
    private CustomFieldInputHelper customFieldInputHelper;
    private String urlParameterName = "customfield_10000";
    private VersionCustomFieldSearchInputTransformer transformer;
    private String primaryClauseName = "cf[10000]";
    private ClauseNames clauseNames = new ClauseNames(primaryClauseName);
    private CustomField customField = null;
    private IndexInfoResolver<Version> indexInfoResolver;
    private JqlOperandResolver operandResolver;
    private FieldFlagOperandRegistry fieldFlagOperandRegistry;
    private VersionManager versionManager;

    @Before
    public void setUp() throws Exception
    {
        versionResolver = mockController.getMock(NameResolver.class);
        searchContext = mockController.getMock(SearchContext.class);
        customFieldInputHelper = getMock(CustomFieldInputHelper.class);
        indexInfoResolver = getMock(IndexInfoResolver.class);
        operandResolver = getMock(JqlOperandResolver.class);
        fieldFlagOperandRegistry = getMock(FieldFlagOperandRegistry.class);
        versionManager = getMock(VersionManager.class);
    }

    @Test
    public void testGetParamsFromSearchRequestNullSearchRequest() throws Exception
    {
        VersionCustomFieldSearchInputTransformer transformer = createTransformer("cf[1000]");
        assertNull(transformer.getParamsFromSearchRequest(null, null, searchContext));
        mockController.verify();
    }

    @Test
    public void testGetParamsFromSearchRequest() throws Exception
    {
        final String name = "clauseName";
        final User theUser = null;
        final CustomField field = mockController.getMock(CustomField.class);

        final QueryImpl query = new QueryImpl();

        final IndexedInputHelper indexInputHelper = mockController.getMock(IndexedInputHelper.class);
        indexInputHelper.getAllNavigatorValuesForMatchingClauses(theUser, new ClauseNames(name), query);
        final Set<String> valuesAsStrings = Collections.singleton("Hi");
        mockController.setReturnValue(valuesAsStrings);

        final CustomFieldParams expectedParams = new CustomFieldParamsImpl(field, valuesAsStrings);

        final VersionCustomFieldSearchInputTransformer transformer = new VersionCustomFieldSearchInputTransformer(name, new ClauseNames(name), field, mockController.getMock(VersionIndexInfoResolver.class), mockController.getMock(JqlOperandResolver.class), mockController.getMock(FieldFlagOperandRegistry.class), versionResolver, customFieldInputHelper, versionManager)
        {
            @Override
            IndexedInputHelper getIndexedInputHelper()
            {
                return indexInputHelper;
            }
        };

        mockController.replay();

        assertEquals(expectedParams, transformer.getParamsFromSearchRequest(null, query, searchContext));

        mockController.verify();
    }

    @Test
    public void testDoRelevantClausesFitDelegates() throws Exception
    {
        final boolean theResult = false;
        final Query query = null;

        final MockControl mockNavigatorStructureCheckerControl = MockClassControl.createControl(NavigatorStructureChecker.class);
        final NavigatorStructureChecker mockNavigatorStructureChecker = (NavigatorStructureChecker) mockNavigatorStructureCheckerControl.getMock();
        mockNavigatorStructureChecker.checkSearchRequest(query);
        mockNavigatorStructureCheckerControl.setReturnValue(theResult);
        mockNavigatorStructureCheckerControl.replay();

        @SuppressWarnings ({ "unchecked" }) final VersionCustomFieldSearchInputTransformer transformer = new VersionCustomFieldSearchInputTransformer("clauseName", new ClauseNames("clauseName"), mockController.getMock(CustomField.class), mockController.getMock(VersionIndexInfoResolver.class), mockController.getMock(JqlOperandResolver.class), mockController.getMock(FieldFlagOperandRegistry.class), versionResolver, customFieldInputHelper, versionManager)
        {
            @Override
            NavigatorStructureChecker createNavigatorStructureChecker()
            {
                return mockNavigatorStructureChecker;
            }
        };
        
        mockController.replay();

        assertEquals(theResult, transformer.doRelevantClausesFitFilterForm(null, query, searchContext));
        mockNavigatorStructureCheckerControl.verify();
        mockController.verify();
    }

    @Test
    public void testGetClauseFromParamsEmptyValues() throws Exception
    {
        final CustomFieldParams customFieldParams = mockController.getMock(CustomFieldParams.class);
        customFieldParams.getValuesForNullKey();
        mockController.setReturnValue(Collections.EMPTY_LIST);

        final VersionCustomFieldSearchInputTransformer inputTransformer = createTransformer("cf[12345]");

        assertNull(inputTransformer.getClauseFromParams(theUser, customFieldParams, null));

        mockController.verify();
    }

    @Test
    public void testGetClauseFromParamsFilteredValuesEmpty() throws Exception
    {
        final CustomFieldParams customFieldParams = mockController.getMock(CustomFieldParams.class);
        customFieldParams.getValuesForNullKey();
        mockController.setReturnValue(CollectionBuilder.newBuilder("").asMutableSet());

        final VersionCustomFieldSearchInputTransformer inputTransformer = createTransformer("cf[12345]");

        assertNull(inputTransformer.getClauseFromParams(theUser, customFieldParams, null));

        mockController.verify();
    }

    @Test
    public void testGetClauseFromParamsValuesGetFiltered() throws Exception
    {
        final String clauseName = "clauseName";

        final CustomField customField = mockController.getMock(CustomField.class);
        EasyMock.expect(customField.getUntranslatedName()).andStubReturn("ABC");
        EasyMock.expect(customFieldInputHelper.getUniqueClauseName(theUser, clauseName, "ABC")).andStubReturn(clauseName);

        final CustomFieldParamsImpl customFieldParams = new CustomFieldParamsImpl();
        customFieldParams.addValue(CollectionBuilder.newBuilder("55", "-1", "").asMutableSet());

        final Set<String> expectedFilteredValues = CollectionBuilder.newBuilder("55").asMutableSet();

        final Clause expectedClause = mockController.getMock(Clause.class);

        final IndexedInputHelper indexedInputHelper = mockController.getMock(IndexedInputHelper.class);
        indexedInputHelper.getClauseForNavigatorValues(clauseName, expectedFilteredValues);
        mockController.setReturnValue(expectedClause);

        transformer = new VersionCustomFieldSearchInputTransformer(clauseName, new ClauseNames(clauseName), customField, indexInfoResolver, operandResolver, fieldFlagOperandRegistry, versionResolver, customFieldInputHelper, versionManager)
        {
            @Override
            IndexedInputHelper getIndexedInputHelper()
            {
                return indexedInputHelper;
            }

            @Override
            boolean isVersionsNotRelatedToProjects(final Set<String> versionIdsFromHolder, final FieldValuesHolder fieldValuesHolder)
            {
                return false;
            }
        };

        mockController.replay();

        FieldValuesHolder holderValues = new FieldValuesHolderImpl(MapBuilder.singletonMap(urlParameterName, customFieldParams));
        assertEquals(expectedClause, transformer.getClauseFromParams(theUser, customFieldParams, holderValues));

        mockController.verify();
    }

    @Test
    public void testGetSearchClauseValuesNotPresent() throws Exception
    {
        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl();

        VersionCustomFieldSearchInputTransformer transformer = createTransformer("cf[1000]");

        assertNull(transformer.getSearchClause(null, valuesHolder));

        mockController.verify();
    }

    @Test
    public void testGetSearchClauseValuesPresentParamsEmpty() throws Exception
    {
        final String searcherId = "searcherId";

        final CustomFieldParams customFieldParams = new CustomFieldParamsImpl();

        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl();
        valuesHolder.put(searcherId, customFieldParams);

        final VersionCustomFieldSearchInputTransformer transformer = new VersionCustomFieldSearchInputTransformer(searcherId, new ClauseNames("clauseName"), mockController.getMock(CustomField.class), mockController.getMock(VersionIndexInfoResolver.class), mockController.getMock(JqlOperandResolver.class), mockController.getMock(FieldFlagOperandRegistry.class), versionResolver, customFieldInputHelper, versionManager)
        {
            @Override
            protected Clause getClauseFromParams(final User user, final CustomFieldParams params, final FieldValuesHolder fieldValuesHolder)
            {
                fail("Should not have been called");
                return null;
            }
        };

        mockController.replay();

        assertNull(transformer.getSearchClause(null, valuesHolder));

        mockController.verify();
    }

    @Test
    public void testGetSearchClauseValuesPresentParamsNull() throws Exception
    {
        final String searcherId = "searcherId";

        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl();
        valuesHolder.put(searcherId, null);

        final VersionCustomFieldSearchInputTransformer transformer = new VersionCustomFieldSearchInputTransformer(searcherId, new ClauseNames("clauseName"), mockController.getMock(CustomField.class), mockController.getMock(VersionIndexInfoResolver.class), mockController.getMock(JqlOperandResolver.class), mockController.getMock(FieldFlagOperandRegistry.class), versionResolver, customFieldInputHelper, versionManager)
        {
            @Override
            protected Clause getClauseFromParams(final User user, final CustomFieldParams params, final FieldValuesHolder fieldValuesHolder)
            {
                fail("Should not have been called");
                return null;
            }
        };
        
        mockController.replay();

        assertNull(transformer.getSearchClause(null, valuesHolder));

        mockController.verify();
    }

    @Test
    public void testGetSearchClauseValuesPresentParamsNotEmpty() throws Exception
    {
        final String searcherId = "searcherId";
        final String clauseName = "clauseName";

        final CustomFieldParamsImpl customFieldParams = new CustomFieldParamsImpl();
        customFieldParams.addValue(Collections.singleton("dontcare"));

        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl();
        valuesHolder.put(searcherId, customFieldParams);

        final AtomicBoolean wasCalled = new AtomicBoolean(false);
        final Clause expectedClause = new TerminalClauseImpl(clauseName, Operator.EQUALS, "blah");

        final VersionCustomFieldSearchInputTransformer transformer = new VersionCustomFieldSearchInputTransformer(searcherId, new ClauseNames(clauseName), mockController.getMock(CustomField.class), mockController.getMock(VersionIndexInfoResolver.class), mockController.getMock(JqlOperandResolver.class), mockController.getMock(FieldFlagOperandRegistry.class), versionResolver, customFieldInputHelper, versionManager)
        {
            @Override
            protected Clause getClauseFromParams(final User user, final CustomFieldParams params, final FieldValuesHolder fieldValuesHolder)
            {
                wasCalled.set(true);
                return expectedClause;
            }
        };

        mockController.replay();

        assertEquals(expectedClause, transformer.getSearchClause(null, valuesHolder));
        assertTrue(wasCalled.get());

        mockController.verify();
    }

    @Test
    public void testGetSearchClauseNoNavigatorValues() throws Exception
    {
        FieldValuesHolder values = new FieldValuesHolderImpl();
        transformer = new VersionCustomFieldSearchInputTransformer(urlParameterName, clauseNames, customField, indexInfoResolver, operandResolver, fieldFlagOperandRegistry, versionResolver, customFieldInputHelper, versionManager);
        replay();

        final Clause result = transformer.getSearchClause(theUser, values);

        assertNull(result);
    }

    @Test
    public void testGetSearchClauseNoProjects() throws Exception
    {
        final CustomFieldParamsImpl customFieldParams = new CustomFieldParamsImpl();
        customFieldParams.addValue(Collections.singleton("123"));
        FieldValuesHolder values = new FieldValuesHolderImpl(MapBuilder.singletonMap(urlParameterName, customFieldParams));
        final AtomicBoolean specialCalled = new AtomicBoolean(false);
        final DefaultIndexedInputHelper helper = new DefaultIndexedInputHelper<Version>(indexInfoResolver, operandResolver, fieldFlagOperandRegistry)
        {
            @Override
            public Clause getClauseForNavigatorValues(final String clauseName, final Set values)
            {
                return null;
            }
        };

        transformer = new VersionCustomFieldSearchInputTransformer(urlParameterName, clauseNames, customField, indexInfoResolver, operandResolver, fieldFlagOperandRegistry, versionResolver, customFieldInputHelper, versionManager)
        {
            @Override
            protected DefaultIndexedInputHelper getDefaultIndexedInputHelper()
            {
                throw new UnsupportedOperationException("Should not have called through to the special indexed input helper");
            }

            @Override
            protected IndexedInputHelper getIndexedInputHelper()
            {
                specialCalled.set(true);
                return helper;
            }

            @Override
            protected String getClauseName(final User user, final ClauseNames clauseNames)
            {
                return primaryClauseName;
            }
        };
        replay();

        transformer.getSearchClause(theUser, values);
        assertTrue(specialCalled.get());
    }

    @Test
    public void testGetSearchClauseTwoProjects() throws Exception
    {
        final CustomFieldParamsImpl customFieldParams = new CustomFieldParamsImpl();
        customFieldParams.addValue(Collections.singleton("123"));
        final Map<String, Object> map = MapBuilder.<String, Object>newBuilder()
                .add(urlParameterName, customFieldParams)
                .add(SystemSearchConstants.forProject().getUrlParameter(), CollectionBuilder.list("1", "2"))
                .toMap();
        FieldValuesHolder values = new FieldValuesHolderImpl(map);
        final AtomicBoolean defaultCalled = new AtomicBoolean(false);
        final DefaultIndexedInputHelper helper = new DefaultIndexedInputHelper<Version>(indexInfoResolver, operandResolver, fieldFlagOperandRegistry)
        {
            @Override
            public Clause getClauseForNavigatorValues(final String clauseName, final Set values)
            {
                return null;
            }
        };

        transformer = new VersionCustomFieldSearchInputTransformer(urlParameterName, clauseNames, customField, indexInfoResolver, operandResolver, fieldFlagOperandRegistry, versionResolver, customFieldInputHelper, versionManager)
        {
            @Override
            protected DefaultIndexedInputHelper getDefaultIndexedInputHelper()
            {
                defaultCalled.set(true);
                return helper;
            }

            @Override
            protected IndexedInputHelper getIndexedInputHelper()
            {
                throw new UnsupportedOperationException("Should not have called through to the special indexed input helper");
            }

            @Override
            protected String getClauseName(final User user, final ClauseNames clauseNames)
            {
                return primaryClauseName;
            }
        };
        replay();

        transformer.getSearchClause(theUser, values);
        assertTrue(defaultCalled.get());
    }

    @Test
    public void testGetSearchClauseOneProjectIsFlag() throws Exception
    {
        final CustomFieldParamsImpl customFieldParams = new CustomFieldParamsImpl();
        customFieldParams.addValue(Collections.singleton("123"));
        final Map<String, Object> map = MapBuilder.<String, Object>newBuilder()
                .add(urlParameterName, customFieldParams)
                .add(SystemSearchConstants.forProject().getUrlParameter(), Collections.singletonList("-1"))
                .toMap();
        FieldValuesHolder values = new FieldValuesHolderImpl(map);
        final AtomicBoolean specialCalled = new AtomicBoolean(false);

        final IndexedInputHelper helper = new DefaultIndexedInputHelper<Version>(indexInfoResolver, operandResolver, fieldFlagOperandRegistry)
        {
            @Override
            public Clause getClauseForNavigatorValues(final String clauseName, final Set values)
            {
                return null;
            }
        };

        transformer = new VersionCustomFieldSearchInputTransformer(urlParameterName, clauseNames, customField, indexInfoResolver, operandResolver, fieldFlagOperandRegistry, versionResolver, customFieldInputHelper, versionManager)
        {
            @Override
            protected DefaultIndexedInputHelper getDefaultIndexedInputHelper()
            {
                throw new UnsupportedOperationException("Should not have called through to the special indexed input helper");
            }

            @Override
            protected IndexedInputHelper getIndexedInputHelper()
            {
                specialCalled.set(true);
                return helper;
            }

            @Override
            protected String getClauseName(final User user, final ClauseNames clauseNames)
            {
                return primaryClauseName;
            }
        };
        replay();

        transformer.getSearchClause(theUser, values);
        assertTrue(specialCalled.get());
    }

    @Test
    public void testGetSearchClauseOneProjectOneVersionMatch() throws Exception
    {
        final CustomFieldParamsImpl customFieldParams = new CustomFieldParamsImpl();
        customFieldParams.addValue(Collections.singleton("123"));
        final Map<String, Object> map = MapBuilder.<String, Object>newBuilder()
                .add(urlParameterName, customFieldParams)
                .add(SystemSearchConstants.forProject().getUrlParameter(), Collections.singletonList("1"))
                .toMap();
        FieldValuesHolder values = new FieldValuesHolderImpl(map);
        final AtomicBoolean specialCalled = new AtomicBoolean(false);

        EasyMock.expect(versionResolver.get(123L))
                .andReturn(new MockVersion(123L, "V1", new MockProject(1L)));

        final IndexedInputHelper helper = new DefaultIndexedInputHelper<Version>(indexInfoResolver, operandResolver, fieldFlagOperandRegistry)
        {
            @Override
            public Clause getClauseForNavigatorValues(final String clauseName, final Set values)
            {
                return null;
            }
        };

        transformer = new VersionCustomFieldSearchInputTransformer(urlParameterName, clauseNames, customField, indexInfoResolver, operandResolver, fieldFlagOperandRegistry, versionResolver, customFieldInputHelper, versionManager)
        {
            @Override
            protected DefaultIndexedInputHelper getDefaultIndexedInputHelper()
            {
                throw new UnsupportedOperationException("Should not have called through to the special indexed input helper");
            }

            @Override
            protected IndexedInputHelper getIndexedInputHelper()
            {
                specialCalled.set(true);
                return helper;
            }

            @Override
            protected String getClauseName(final User user, final ClauseNames clauseNames)
            {
                return primaryClauseName;
            }
        };
        replay();

        transformer.getSearchClause(theUser, values);
        assertTrue(specialCalled.get());
    }

    @Test
    public void testGetSearchClauseOneProjectTwoVersionsOneDoesntMatch() throws Exception
    {
        final CustomFieldParamsImpl customFieldParams = new CustomFieldParamsImpl();
        customFieldParams.addValue(CollectionBuilder.list("123", "456"));
        final Map<String, Object> map = MapBuilder.<String, Object>newBuilder()
                .add(urlParameterName, customFieldParams)
                .add(SystemSearchConstants.forProject().getUrlParameter(), Collections.singletonList("1"))
                .toMap();
        FieldValuesHolder values = new FieldValuesHolderImpl(map);
        final AtomicBoolean defaultCalled = new AtomicBoolean(false);

        EasyMock.expect(versionResolver.get(123L))
                .andReturn(new MockVersion(123L, "V1", new MockProject(1L)));

        EasyMock.expect(versionResolver.get(456L))
                .andReturn(new MockVersion(456L, "V2", new MockProject(2L)));

        final DefaultIndexedInputHelper helper = new DefaultIndexedInputHelper<Version>(indexInfoResolver, operandResolver, fieldFlagOperandRegistry)
        {
            @Override
            public Clause getClauseForNavigatorValues(final String clauseName, final Set values)
            {
                return null;
            }
        };

        transformer = new VersionCustomFieldSearchInputTransformer(urlParameterName, clauseNames, customField, indexInfoResolver, operandResolver, fieldFlagOperandRegistry, versionResolver, customFieldInputHelper, versionManager)
        {
            @Override
            protected DefaultIndexedInputHelper getDefaultIndexedInputHelper()
            {
                defaultCalled.set(true);
                return helper;
            }

            @Override
            protected IndexedInputHelper getIndexedInputHelper()
            {
                throw new UnsupportedOperationException("Should not have called through to the special indexed input helper");
            }

            @Override
            protected String getClauseName(final User user, final ClauseNames clauseNames)
            {
                return primaryClauseName;
            }
        };
        replay();

        transformer.getSearchClause(theUser, values);
        assertTrue(defaultCalled.get());
    }

    @Test
    public void testGetSearchClauseOneProjectTwoVersionsOneDoesntResolveOneNotANumber() throws Exception
    {
        final CustomFieldParamsImpl customFieldParams = new CustomFieldParamsImpl();
        customFieldParams.addValue(CollectionBuilder.list("ABC", "456"));
        final Map<String, Object> map = MapBuilder.<String, Object>newBuilder()
                .add(urlParameterName, customFieldParams)
                .add(SystemSearchConstants.forProject().getUrlParameter(), Collections.singletonList("1"))
                .toMap();
        FieldValuesHolder values = new FieldValuesHolderImpl(map);
        final AtomicBoolean specialCalled = new AtomicBoolean(false);

        EasyMock.expect(versionResolver.get(456L))
                .andReturn(null);

        final DefaultIndexedInputHelper helper = new DefaultIndexedInputHelper<Version>(indexInfoResolver, operandResolver, fieldFlagOperandRegistry)
        {
            @Override
            public Clause getClauseForNavigatorValues(final String clauseName, final Set values)
            {
                return null;
            }
        };

        transformer = new VersionCustomFieldSearchInputTransformer(urlParameterName, clauseNames, customField, indexInfoResolver, operandResolver, fieldFlagOperandRegistry, versionResolver, customFieldInputHelper, versionManager)
        {
            @Override
            protected DefaultIndexedInputHelper getDefaultIndexedInputHelper()
            {
                throw new UnsupportedOperationException("Should not have called through to the special indexed input helper");
            }

            @Override
            protected IndexedInputHelper getIndexedInputHelper()
            {
                specialCalled.set(true);
                return helper;
            }

            @Override
            protected String getClauseName(final User user, final ClauseNames clauseNames)
            {
                return primaryClauseName;
            }
        };
        replay();

        transformer.getSearchClause(theUser, values);
        assertTrue(specialCalled.get());
    }

    private VersionCustomFieldSearchInputTransformer createTransformer(String clauseName)
    {
        mockController.addObjectInstance(clauseName);
        mockController.addObjectInstance(new ClauseNames("primaryName"));
        return mockController.instantiate(VersionCustomFieldSearchInputTransformer.class);
    }
}
