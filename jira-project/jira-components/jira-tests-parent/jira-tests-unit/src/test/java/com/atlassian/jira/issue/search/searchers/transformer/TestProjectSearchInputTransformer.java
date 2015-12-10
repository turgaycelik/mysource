package com.atlassian.jira.issue.search.searchers.transformer;

import java.util.Arrays;
import java.util.Collections;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.util.IndexedInputHelper;
import com.atlassian.jira.issue.transport.impl.ActionParamsImpl;
import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.ProjectIndexInfoResolver;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.UserProjectHistoryManager;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.easymock.EasyMock;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @since v4.0
 */
public class TestProjectSearchInputTransformer extends MockControllerTestCase
{
    private static final String PROJECT_KEY = SystemSearchConstants.forProject().getUrlParameter();

    private JiraAuthenticationContext authenticationContext = null;
    private UserProjectHistoryManager projectHistoryManager;
    private ProjectManager projectManager;
    private User theUser = null;
    private SearchContext searchContext;

    @Before
    public void setUp() throws Exception
    {
        theUser = new MockUser("admin");

        authenticationContext = mockController.getMock(JiraAuthenticationContext.class);
        searchContext = mockController.getMock(SearchContext.class);
        projectHistoryManager = mockController.getMock(UserProjectHistoryManager.class);
        projectManager = mockController.getMock(ProjectManager.class);

    }

    @After
    public void tearDown() throws Exception
    {
        authenticationContext = null;
        searchContext = null;
        projectHistoryManager = null;
        projectManager = null;
    }

    @Test
    public void testPopulateFromParams() throws Exception
    {
        final FieldFlagOperandRegistry mockFieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        mockController.replay();

        SearchInputTransformer transformer = new ProjectSearchInputTransformer(null, null, mockFieldFlagOperandRegistry, projectManager, projectHistoryManager, authenticationContext);
        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl();
        final String[] values = { "val1", "val2" };
        final ActionParamsImpl actionParams = new ActionParamsImpl(EasyMap.build(PROJECT_KEY, values));
        transformer.populateFromParams(null, valuesHolder, actionParams);
        assertEquals(Arrays.asList(values), valuesHolder.get(PROJECT_KEY));

        mockController.verify();
    }

    @Test
    public void testPopulateFromParamsDuplicatesValues() throws Exception
    {
        final FieldFlagOperandRegistry mockFieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        mockController.replay();

        SearchInputTransformer transformer = new ProjectSearchInputTransformer(null, null, mockFieldFlagOperandRegistry, projectManager, projectHistoryManager, authenticationContext);
        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl();
        final String[] values = { "val1", "val1" };
        final ActionParamsImpl actionParams = new ActionParamsImpl(EasyMap.build(PROJECT_KEY, values));
        transformer.populateFromParams(null, valuesHolder, actionParams);
        assertEquals(Arrays.asList(new String[]{ "val1", }), valuesHolder.get(PROJECT_KEY));

        mockController.verify();
    }

    @Test
    public void testPopulateFromSearchRequestEmptyMatchingValues() throws Exception
    {
        final MockControl mockProjectIndexInfoResolverControl = MockClassControl.createControl(ProjectIndexInfoResolver.class);
        final ProjectIndexInfoResolver mockProjectIndexInfoResolver = (ProjectIndexInfoResolver) mockProjectIndexInfoResolverControl.getMock();
        mockProjectIndexInfoResolverControl.replay();

        final FieldFlagOperandRegistry mockFieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);

        final IndexedInputHelper indexedInputHelper = mockController.getMock(IndexedInputHelper.class);
        indexedInputHelper.getAllNavigatorValuesForMatchingClauses(theUser, SystemSearchConstants.forProject().getJqlClauseNames(), new QueryImpl());
        mockController.setReturnValue(Collections.emptySet());

        mockController.replay();

        final SearchInputTransformer transformer = new ProjectSearchInputTransformer(mockProjectIndexInfoResolver, MockJqlOperandResolver.createSimpleSupport(), mockFieldFlagOperandRegistry, projectManager, projectHistoryManager, authenticationContext)
        {

            @Override
            IndexedInputHelper createIndexedInputHelper()
            {
                return indexedInputHelper;
            }
        };
        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl();
        transformer.populateFromQuery(theUser, valuesHolder, new QueryImpl(), searchContext);
        assertNull(valuesHolder.get(PROJECT_KEY));
        mockProjectIndexInfoResolverControl.verify();

        mockController.verify();
    }

    @Test
    public void testPopulateFromSearchRequestHappyPath() throws Exception
    {
        final MockControl mockProjectIndexInfoResolverControl = MockClassControl.createControl(ProjectIndexInfoResolver.class);
        final ProjectIndexInfoResolver mockProjectIndexInfoResolver = (ProjectIndexInfoResolver) mockProjectIndexInfoResolverControl.getMock();
        mockProjectIndexInfoResolverControl.replay();

        final FieldFlagOperandRegistry mockFieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);

        final IndexedInputHelper indexedInputHelper = mockController.getMock(IndexedInputHelper.class);
        indexedInputHelper.getAllNavigatorValuesForMatchingClauses(theUser, new ClauseNames(IssueFieldConstants.PROJECT), new QueryImpl());
        mockController.setReturnValue(CollectionBuilder.newBuilder("1", "2").asSortedSet());

        mockController.replay();

        final SearchInputTransformer transformer = new ProjectSearchInputTransformer(mockProjectIndexInfoResolver, MockJqlOperandResolver.createSimpleSupport(), mockFieldFlagOperandRegistry, projectManager, projectHistoryManager, authenticationContext)
        {

            @Override
            IndexedInputHelper createIndexedInputHelper()
            {
                return indexedInputHelper;
            }
        };
        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl();
        transformer.populateFromQuery(theUser, valuesHolder, new QueryImpl(), searchContext);
        assertEquals(EasyList.build("1", "2"), valuesHolder.get(PROJECT_KEY));
        mockProjectIndexInfoResolverControl.verify();

        mockController.verify();
    }

    @Test
    public void testGetSearchClauseHappyPath() throws Exception
    {
        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl();
        valuesHolder.put(PROJECT_KEY, EasyList.build("1"));

        EasyMock.expect(projectManager.getProjectObj(1L))
                .andReturn(new MockProject(1L, "MKY"));

        final FieldFlagOperandRegistry mockFieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        mockController.replay();

        final ProjectSearchInputTransformer transformer = new ProjectSearchInputTransformer(null, null, mockFieldFlagOperandRegistry, projectManager, projectHistoryManager, authenticationContext);
        Clause result = transformer.getSearchClause(null, valuesHolder);
        assertEquals(new TerminalClauseImpl(SystemSearchConstants.forProject().getJqlClauseNames().getPrimaryName(), Operator.EQUALS, "MKY"), result);

        mockController.verify();
    }

    @Test
    public void testGetSearchClauseSomeNumbersSomeNot() throws Exception
    {
        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl();
        valuesHolder.put(PROJECT_KEY, CollectionBuilder.newBuilder("1", "frank").asList());

        EasyMock.expect(projectManager.getProjectObj(1L))
                .andReturn(new MockProject(1L, "MKY"));

        final FieldFlagOperandRegistry mockFieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        mockController.replay();

        final ProjectSearchInputTransformer transformer = new ProjectSearchInputTransformer(null, null, mockFieldFlagOperandRegistry, projectManager, projectHistoryManager, authenticationContext);
        Clause result = transformer.getSearchClause(null, valuesHolder);
        final Clause expectedClause = new TerminalClauseImpl(SystemSearchConstants.forProject().getJqlClauseNames().getPrimaryName(), Operator.IN, new MultiValueOperand("MKY", "frank"));
        assertEquals(expectedClause, result);

        mockController.verify();
    }

    @Test
    public void testGetSearchClauseSomeValidIdsSomeNot() throws Exception
    {
        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl();
        valuesHolder.put(PROJECT_KEY, CollectionBuilder.newBuilder("1", "2").asList());

        EasyMock.expect(projectManager.getProjectObj(1L))
                .andReturn(new MockProject(1L, "MKY"));

        EasyMock.expect(projectManager.getProjectObj(2L))
                .andReturn(null);

        final FieldFlagOperandRegistry mockFieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        mockController.replay();

        final ProjectSearchInputTransformer transformer = new ProjectSearchInputTransformer(null, null, mockFieldFlagOperandRegistry, projectManager, projectHistoryManager, authenticationContext);
        Clause result = transformer.getSearchClause(null, valuesHolder);
        final Clause expectedClause = new TerminalClauseImpl(SystemSearchConstants.forProject().getJqlClauseNames().getPrimaryName(), Operator.IN, new MultiValueOperand(new SingleValueOperand("MKY"), new SingleValueOperand(2L)));
        assertEquals(expectedClause, result);

        mockController.verify();
    }

    @Test
    public void testGetSearchClauseInOperator() throws Exception
    {
        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl();
        valuesHolder.put(PROJECT_KEY, EasyList.build("1", "2"));

        EasyMock.expect(projectManager.getProjectObj(1L))
                .andReturn(new MockProject(1L, "MKY"));

        EasyMock.expect(projectManager.getProjectObj(2L))
                .andReturn(new MockProject(2L, "HSP"));

        final FieldFlagOperandRegistry mockFieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        mockController.replay();

        final ProjectSearchInputTransformer transformer = new ProjectSearchInputTransformer(null, null, mockFieldFlagOperandRegistry, projectManager, projectHistoryManager, authenticationContext);
        Clause result = transformer.getSearchClause(null, valuesHolder);

        MultiValueOperand multiValueOperand = new MultiValueOperand("MKY", "HSP");

        assertEquals(new TerminalClauseImpl(SystemSearchConstants.forProject().getJqlClauseNames().getPrimaryName(), Operator.IN, multiValueOperand), result);

        mockController.verify();
    }
    
    @Test
    public void testGetSearchClauseNoProjects() throws Exception
    {
        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl();
        valuesHolder.put(PROJECT_KEY, EasyList.build());

        final FieldFlagOperandRegistry mockFieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        mockController.replay();

        final ProjectSearchInputTransformer transformer = new ProjectSearchInputTransformer(null, null, mockFieldFlagOperandRegistry, projectManager, projectHistoryManager, authenticationContext);
        Clause result = transformer.getSearchClause(null, valuesHolder);
        assertNull(result);

        mockController.verify();
    }

    @Test
    public void testsetProjectIdInSessionOnlyOne() throws Exception
    {

        final FieldFlagOperandRegistry mockFieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);

        final Project project = new MockProject(1L);
        projectManager.getProjectObj(1L);
        mockController.setReturnValue(project);

        authenticationContext.getLoggedInUser();
        mockController.setReturnValue(theUser);

        projectHistoryManager.addProjectToHistory(theUser, project);

        mockController.replay();

        final ProjectSearchInputTransformer transformer = new ProjectSearchInputTransformer(null, null, mockFieldFlagOperandRegistry, projectManager, projectHistoryManager, authenticationContext);
        transformer.setProjectIdInSession(CollectionBuilder.newBuilder("1").asSet());

        mockController.verify();
    }

    @Test
    public void testsetProjectIdInSessionMoreThanOne() throws Exception
    {

        final FieldFlagOperandRegistry mockFieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        mockController.replay();

        final ProjectSearchInputTransformer transformer = new ProjectSearchInputTransformer(null, null, mockFieldFlagOperandRegistry, projectManager, projectHistoryManager, authenticationContext);
        transformer.setProjectIdInSession(CollectionBuilder.newBuilder("1", "2").asSet());

        mockController.verify();
    }

    @Test
    public void testValidateForNavigatorHappyPath() throws Exception
    {
        AndClause andClause = new AndClause(new TerminalClauseImpl(SystemSearchConstants.forProject().getJqlClauseNames().getPrimaryName(), Operator.EQUALS, "value"),
                                            new TerminalClauseImpl("other", Operator.EQUALS, "valueother"));

        final FieldFlagOperandRegistry fieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        final JqlOperandResolver mockJqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        mockController.replay();

        ProjectSearchInputTransformer transformer = new ProjectSearchInputTransformer(null, null, null, projectManager, projectHistoryManager, authenticationContext)
        {
            @Override
            NavigatorStructureChecker<Project> createNavigatorStructureChecker()
            {
                return new NavigatorStructureChecker<Project>(SystemSearchConstants.forProject().getJqlClauseNames(), false, fieldFlagOperandRegistry, mockJqlOperandResolver)
                {
                    @Override
                    public boolean checkSearchRequest(final Query query)
                    {
                        return true;
                    }
                };
            }
        };

        assertValidate(andClause, transformer, true);

        mockController.verify();
    }

    @Test
    public void testValidateForNavigatorSadPath() throws Exception
    {
        AndClause andClause = new AndClause(new TerminalClauseImpl(SystemSearchConstants.forProject().getJqlClauseNames().getPrimaryName(), Operator.EQUALS, "value"),
                                            new TerminalClauseImpl("other", Operator.EQUALS, "valueother"));

        final FieldFlagOperandRegistry fieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        final JqlOperandResolver mockJqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        mockController.replay();

        ProjectSearchInputTransformer transformer = new ProjectSearchInputTransformer(null, null, null, projectManager, projectHistoryManager, authenticationContext)
        {
            @Override
            NavigatorStructureChecker<Project> createNavigatorStructureChecker()
            {
                return new NavigatorStructureChecker<Project>(SystemSearchConstants.forProject().getJqlClauseNames(), false, fieldFlagOperandRegistry, mockJqlOperandResolver)
                {
                    @Override
                    public boolean checkSearchRequest(final Query query)
                    {
                        return false;
                    }
                };
            }
        };

        assertValidate(andClause, transformer, false);
        mockController.verify();
    }
    
    private void assertValidate(final AndClause andClause, final ProjectSearchInputTransformer transformer, final boolean isClauseValid)
    {
        assertEquals(isClauseValid, transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(andClause), searchContext));
    }
}
