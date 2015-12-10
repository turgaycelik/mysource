package com.atlassian.jira.jql.context;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.resolver.SavedFilterResolver;
import com.atlassian.jira.jql.validator.SavedFilterCycleDetector;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.ClauseVisitor;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operator.Operator;

import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.junit.Assert.assertEquals;

/**
 * @since v4.0
 */
public class TestSavedFilterClauseContextFactory extends MockControllerTestCase
{
    private JqlOperandResolver jqlOperandResolver;
    private SavedFilterResolver savedFilterResolver;
    private ContextSetUtil contextSetUtil;
    private SavedFilterCycleDetector savedFilterCycleDetector;
    private User theUser = null;
    private boolean overrideSecurity = false;

    @Before
    public void setUp() throws Exception
    {
        jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        savedFilterResolver = mockController.getMock(SavedFilterResolver.class);
        contextSetUtil = mockController.getMock(ContextSetUtil.class);
        savedFilterCycleDetector = mockController.getMock(SavedFilterCycleDetector.class);
    }

    @After
    public void tearDown() throws Exception
    {
        jqlOperandResolver = null;
        savedFilterResolver = null;
        contextSetUtil = null;
        savedFilterCycleDetector = null;
    }

    @Test
    public void testGetClauseContext() throws Exception
    {
        final SearchRequest searchRequest1 = mockController.getMock(SearchRequest.class);

        final TerminalClauseImpl anyClause = new TerminalClauseImpl("blah", Operator.EQUALS, "blah");
        Query query1 = new QueryImpl(anyClause);
        Query query2 = new QueryImpl(anyClause);
        Query query3 = new QueryImpl();

        // these values are meaningless as long as they are different
        final ClauseContext clauseContext1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), new IssueTypeContextImpl("10"))).asSet());
        final ClauseContext clauseContext2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(new ProjectIssueTypeContextImpl(new ProjectContextImpl(20L), new IssueTypeContextImpl("20"))).asSet());
        final ClauseContext clauseContext3 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, AllIssueTypesContext.INSTANCE)).asSet());

        final ClauseContext union = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(new ProjectIssueTypeContextImpl(new ProjectContextImpl(40L), new IssueTypeContextImpl("40"))).asSet());

        TerminalClause clause = new TerminalClauseImpl(SystemSearchConstants.forSavedFilter().getJqlClauseNames().getPrimaryName(), Operator.EQUALS, new MultiValueOperand("blarg", "orgle"));

        final List<QueryLiteral> literals = CollectionBuilder.newBuilder(createLiteral("blarg"), createLiteral("orgle")).asList();
        jqlOperandResolver.getValues(theUser, clause.getOperand(), clause);
        mockController.setReturnValue(literals);

        savedFilterResolver.getSearchRequest(theUser, literals);
        mockController.setReturnValue(CollectionBuilder.newBuilder(searchRequest1, searchRequest1, searchRequest1).asList());

        searchRequest1.getQuery();
        mockController.setReturnValue(query1);

        searchRequest1.getQuery();
        mockController.setReturnValue(query2);

        searchRequest1.getQuery();
        mockController.setReturnValue(query3);

        savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, searchRequest1, null);
        mockController.setReturnValue(false);
        savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, searchRequest1, null);
        mockController.setReturnValue(false);
        savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, searchRequest1, null);
        mockController.setReturnValue(false);

        contextSetUtil.union(CollectionBuilder.newBuilder(clauseContext1, clauseContext2).asSet());
        mockController.setReturnValue(union);
        contextSetUtil.union(CollectionBuilder.newBuilder(union, clauseContext3).asSet());
        mockController.setReturnValue(union);

        final SavedFilterClauseContextFactory factory = new SavedFilterClauseContextFactory(savedFilterResolver, jqlOperandResolver, mockController.getMock(QueryContextVisitor.QueryContextVisitorFactory.class), contextSetUtil, savedFilterCycleDetector)
        {
            int called = 0;

            private ClauseContext[] contexts = new ClauseContext[] {clauseContext1, clauseContext2, clauseContext3};
            private Clause[] expected = new Clause [] {anyClause, anyClause, null};

            @Override
            ClauseContext getSavedFilterContext(final User searcher, final Clause clause)
            {
                assertEquals(expected[called], clause);
                return contexts[called++];
            }
        };
        
        mockController.replay();

        assertEquals(union, factory.getClauseContext(theUser, clause));

        mockController.verify();
    }

    @Test
    public void testGetClauseContextClauseContainsCycle() throws Exception
    {
        final SearchRequest searchRequest1 = mockController.getMock(SearchRequest.class);
        final SearchRequest searchRequest2 = mockController.getMock(SearchRequest.class);

        final TerminalClauseImpl anyClause = new TerminalClauseImpl("blah", Operator.EQUALS, "blah");
        Query query1 = new QueryImpl(anyClause);

        // these values are meaningless as long as they are different
        final ClauseContext clauseContext1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), new IssueTypeContextImpl("10"))).asSet());
        final ClauseContext clauseContext2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(new ProjectIssueTypeContextImpl(new ProjectContextImpl(20L), new IssueTypeContextImpl("20"))).asSet());

        TerminalClause clause = new TerminalClauseImpl(SystemSearchConstants.forSavedFilter().getJqlClauseNames().getPrimaryName(), Operator.EQUALS, new MultiValueOperand("blarg", "orgle"));

        final List<QueryLiteral> literals = CollectionBuilder.newBuilder(createLiteral("blarg"), createLiteral("orgle")).asList();
        jqlOperandResolver.getValues(theUser, clause.getOperand(), clause);
        mockController.setReturnValue(literals);

        savedFilterResolver.getSearchRequest(theUser, literals);
        mockController.setReturnValue(CollectionBuilder.newBuilder(searchRequest1, searchRequest2).asList());

        searchRequest1.getQuery();
        mockController.setReturnValue(query1);

        savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, searchRequest1, null);
        mockController.setReturnValue(false);
        savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, searchRequest2, null);
        mockController.setReturnValue(true);

        SavedFilterClauseContextFactory factory = new SavedFilterClauseContextFactory(savedFilterResolver, jqlOperandResolver, mockController.getMock(QueryContextVisitor.QueryContextVisitorFactory.class), contextSetUtil, savedFilterCycleDetector)
        {
            int called = 0;

            @Override
            ClauseContext getSavedFilterContext(final User searcher, final Clause clause)
            {
                called++;
                if (called == 1)
                {
                    return clauseContext1;
                }
                else
                {
                    return clauseContext2;
                }

            }
        };


        mockController.replay();

        final ClauseContext expectedResult = ClauseContextImpl.createGlobalClauseContext();
        assertEquals(expectedResult, factory.getClauseContext(theUser, clause));

        mockController.verify();
    }

    @Test
    public void testGetClauseContextInvalidClause() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl("blah", Operator.IS, EmptyOperand.EMPTY);
        SavedFilterClauseContextFactory factory = new SavedFilterClauseContextFactory(savedFilterResolver, jqlOperandResolver, mockController.getMock(QueryContextVisitor.QueryContextVisitorFactory.class), contextSetUtil, savedFilterCycleDetector);
        mockController.replay();

        final ClauseContext result = factory.getClauseContext(theUser, clause);
        final ClauseContext expectedResult = ClauseContextImpl.createGlobalClauseContext();
        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetClauseContextIneqality() throws Exception
    {
        final SearchRequest searchRequest1 = mockController.getMock(SearchRequest.class);

        final TerminalClauseImpl anyClause = new TerminalClauseImpl("blah", Operator.NOT_EQUALS, "blah");
        final Query query1 = new QueryImpl(anyClause);
        final Query query2 = new QueryImpl(anyClause);
        final Query query3 = new QueryImpl();

        // these values are meaningless as long as they are different
        final ClauseContext clauseContext1 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), new IssueTypeContextImpl("10"))).asSet());
        final ClauseContext clauseContext2 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(new ProjectIssueTypeContextImpl(new ProjectContextImpl(20L), new IssueTypeContextImpl("20"))).asSet());
        final ClauseContext clauseContext3 = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, AllIssueTypesContext.INSTANCE)).asSet());

        final ClauseContext intersection = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(new ProjectIssueTypeContextImpl(new ProjectContextImpl(30L), new IssueTypeContextImpl("30"))).asSet());

        TerminalClause clause = new TerminalClauseImpl(SystemSearchConstants.forSavedFilter().getJqlClauseNames().getPrimaryName(), Operator.NOT_EQUALS, new MultiValueOperand("blarg", "orgle"));

        final List<QueryLiteral> literals = CollectionBuilder.newBuilder(createLiteral("blarg"), createLiteral("orgle")).asList();
        jqlOperandResolver.getValues(theUser, clause.getOperand(), clause);
        mockController.setReturnValue(literals);

        savedFilterResolver.getSearchRequest(theUser, literals);
        mockController.setReturnValue(CollectionBuilder.newBuilder(searchRequest1, searchRequest1, searchRequest1).asList());

        searchRequest1.getQuery();
        mockController.setReturnValue(query1);

        searchRequest1.getQuery();
        mockController.setReturnValue(query2);

        searchRequest1.getQuery();
        mockController.setReturnValue(query3);

        savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, searchRequest1, null);
        mockController.setDefaultReturnValue(false);

        contextSetUtil.intersect(CollectionBuilder.newBuilder(clauseContext1, clauseContext2).asSet());
        mockController.setReturnValue(intersection);

        contextSetUtil.intersect(CollectionBuilder.newBuilder(intersection, clauseContext3).asSet());
        mockController.setReturnValue(intersection);

        SavedFilterClauseContextFactory factory = new SavedFilterClauseContextFactory(savedFilterResolver, jqlOperandResolver, mockController.getMock(QueryContextVisitor.QueryContextVisitorFactory.class), contextSetUtil, savedFilterCycleDetector)
        {
            int called = 0;

            private ClauseContext[] contexts = new ClauseContext[] {clauseContext1, clauseContext2, clauseContext3};
            private Clause[] expected = new Clause [] { new NotClause(anyClause), new NotClause(anyClause), null};

            @Override
            ClauseContext getSavedFilterContext(final User searcher, final Clause clause)
            {
                assertEquals(expected[called], clause);
                return contexts[called++];
            }
        };

        mockController.replay();

        assertEquals(intersection, factory.getClauseContext(theUser, clause));

        mockController.verify();
    }

    @Test
    public void testGetSavedFilterContextNullClause() throws Exception
    {
        final SavedFilterClauseContextFactory factory = instantiate(SavedFilterClauseContextFactory.class);
        final ClauseContext filterContext = factory.getSavedFilterContext(null, null);
        assertEquals(ClauseContextImpl.createGlobalClauseContext(), filterContext);
    }

    @Test
    public void testGetSavedFilterContextNullCalculatedContextContext() throws Exception
    {
        final Clause clause = getMock(Clause.class);
        expect(clause.accept(EasyMock.<ClauseVisitor<QueryContextVisitor.ContextResult>>anyObject())).andReturn(new QueryContextVisitor.ContextResult(null, null));

        mockController.addObjectInstance(createFactory());

        final SavedFilterClauseContextFactory factory = instantiate(SavedFilterClauseContextFactory.class);
        final ClauseContext filterContext = factory.getSavedFilterContext(null, clause);
        assertEquals(ClauseContextImpl.createGlobalClauseContext(), filterContext);
    }

    @Test
    public void testGetSavedFilterContextImplicit() throws Exception
    {
        Set<ProjectIssueTypeContext> ctxs = new HashSet<ProjectIssueTypeContext>();
        ctxs.add(new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), new IssueTypeContextImpl("18")));
        ctxs.add(new ProjectIssueTypeContextImpl(new ProjectContextImpl(17L), AllIssueTypesContext.getInstance()));
        ctxs.add(new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, new IssueTypeContextImpl("3475832")));
        ctxs.add(ProjectIssueTypeContextImpl.createGlobalContext());

        final ClauseContext expectedContext = new ClauseContextImpl(ctxs);

        final Clause clause = getMock(Clause.class);
        expect(clause.accept(EasyMock.<ClauseVisitor<QueryContextVisitor.ContextResult>>anyObject())).andReturn(new QueryContextVisitor.ContextResult(expectedContext, expectedContext));

        mockController.addObjectInstance(createFactory());

        final SavedFilterClauseContextFactory factory = instantiate(SavedFilterClauseContextFactory.class);
        final ClauseContext filterContext = factory.getSavedFilterContext(null, clause);
        assertEquals(expectedContext, filterContext);
    }

    @Test
    public void testGetSavedFilterContextExplicit() throws Exception
    {
        Set<ProjectIssueTypeContext> ctxs = new HashSet<ProjectIssueTypeContext>();
        ctxs.add(new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), new IssueTypeContextImpl("18")));
        ctxs.add(new ProjectIssueTypeContextImpl(new ProjectContextImpl(17L), AllIssueTypesContext.getInstance()));
        ctxs.add(new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, new IssueTypeContextImpl("3475832")));
        ctxs.add(ProjectIssueTypeContextImpl.createGlobalContext());

        final ClauseContext inputContext = new ClauseContextImpl(ctxs);

        ctxs = new HashSet<ProjectIssueTypeContext>();
        ctxs.add(new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), new IssueTypeContextImpl("18")));
        ctxs.add(new ProjectIssueTypeContextImpl(new ProjectContextImpl(17L), AllIssueTypesContext.getInstance()));
        ctxs.add(new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, new IssueTypeContextImpl("3475832")));
        ctxs.add(ProjectIssueTypeContextImpl.createGlobalContext());

        final ClauseContext expectedContext = new ClauseContextImpl(ctxs);

        final Clause clause = getMock(Clause.class);
        expect(clause.accept(EasyMock.<ClauseVisitor<QueryContextVisitor.ContextResult>>anyObject())).andReturn(new QueryContextVisitor.ContextResult(inputContext, inputContext));

        mockController.addObjectInstance(createFactory());

        final SavedFilterClauseContextFactory factory = instantiate(SavedFilterClauseContextFactory.class);
        final ClauseContext filterContext = factory.getSavedFilterContext(null, clause);
        assertEquals(expectedContext, filterContext);
    }

    private QueryContextVisitor.QueryContextVisitorFactory createFactory()
    {
        class DumbVisitor extends QueryContextVisitor
        {
            public DumbVisitor()
            {
                super(null, null, null);
            }

            @Override
            public ContextResult createContext(final Clause clause)
            {
                return new ContextResult(ClauseContextImpl.createGlobalClauseContext(), ClauseContextImpl.createGlobalClauseContext());
            }
        }

        return new QueryContextVisitor.QueryContextVisitorFactory(getMock(ContextSetUtil.class), getMock(SearchHandlerManager.class))
        {
            @Override
            public QueryContextVisitor createVisitor(final com.atlassian.crowd.embedded.api.User searcher)
            {
                return new DumbVisitor();
            }
        };
    }
}
