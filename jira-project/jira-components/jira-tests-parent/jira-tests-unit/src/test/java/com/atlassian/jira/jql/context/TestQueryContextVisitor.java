package com.atlassian.jira.jql.context;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.jql.ClauseHandler;
import com.atlassian.jira.jql.MockClauseHandler;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.jql.MockClauseInformation;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @since v4.0
 */
public class TestQueryContextVisitor extends MockControllerTestCase
{
    @Test
    public void testRootClause() throws Exception
    {
        AndClause andClause = new AndClause(new TerminalClauseImpl("blah", Operator.GREATER_THAN, "blah"));
        NotClause notClause = new NotClause(new TerminalClauseImpl("blah", Operator.GREATER_THAN, "blah"));
        OrClause orClause = new OrClause(new TerminalClauseImpl("blah", Operator.GREATER_THAN, "blah"));
        TerminalClause termClause = new TerminalClauseImpl("blah", Operator.GREATER_THAN, "blah");

        mockController.replay();

        assertRootCalled(andClause, createProject(1));
        assertRootCalled(orClause, createProject(34));
        assertRootCalled(notClause, createProject(333));
        assertRootCalled(termClause, createProject(12));

        mockController.verify();
    }

    private void assertRootCalled(Clause clause, ClauseContext expectedCtx)
    {
        class RootContextVisitor extends QueryContextVisitor
        {
            private Clause calledWith;
            private final ClauseContext result;

            public RootContextVisitor(ClauseContext result)
            {
                super(null, null, null);
                this.result = result;
            }

            @Override
            public ContextResult createContext(final Clause clause)
            {
                calledWith = clause;
                return new ContextResult(result, result);
            }

            public Clause getCalledClause()
            {
                return calledWith;
            }
        }

        final RootContextVisitor rootVisitor = new RootContextVisitor(expectedCtx);

        final QueryContextVisitor.ContextResult contextResult = clause.accept(rootVisitor);
        assertEquals(clause, rootVisitor.getCalledClause());
        assertEquals(expectedCtx, contextResult.getFullContext());
        assertEquals(expectedCtx, contextResult.getSimpleContext());
    }

    @Test
    public void testOr() throws Exception
    {
        TerminalClause termClause1 = new TerminalClauseImpl("blah1", Operator.GREATER_THAN, "val1");
        TerminalClause termClause2 = new TerminalClauseImpl("blah2", Operator.GREATER_THAN, "val2");
        Clause andClause = new OrClause(termClause1, termClause2);

        final ClauseContext clauseContext1 = createProject(78);

        final ClauseContextFactory clauseContextFactory = mockController.getNiceMock(ClauseContextFactory.class);
        expect(clauseContextFactory.getClauseContext(null, termClause1)).andReturn(clauseContext1);
        expect(clauseContextFactory.getClauseContext(null, termClause2)).andReturn(clauseContext1);

        final ClauseHandler clauseHandler = new MockClauseHandler().setContextFactory(clauseContextFactory).setInformation(new MockClauseInformation(new ClauseNames("blah1")));
        final ClauseHandler clauseHandler2 = new MockClauseHandler().setContextFactory(clauseContextFactory).setInformation(new MockClauseInformation(new ClauseNames("blah2")));

        final SearchHandlerManager searchHandlerManager = mockController.getMock(SearchHandlerManager.class);
        expect(searchHandlerManager.getClauseHandler((User) null, "blah1")).andReturn(Collections.singleton(clauseHandler));
        expect(searchHandlerManager.getClauseHandler((User) null, "blah2")).andReturn(Collections.singleton(clauseHandler2));

        final ContextSetUtil contextSetUtil = mockController.getMock(ContextSetUtil.class);

        mockController.replay();

        QueryContextVisitor visitor = new QueryContextVisitor(null, contextSetUtil, searchHandlerManager);
        final QueryContextVisitor.ContextResult result = andClause.accept(visitor);

        QueryContextVisitor.ContextResult expectedResult = new QueryContextVisitor.ContextResult(clauseContext1, ClauseContextImpl.createGlobalClauseContext());

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testOrUnion() throws Exception
    {
        TerminalClause termClause1 = new TerminalClauseImpl("blah1", Operator.GREATER_THAN, "val1");
        TerminalClause termClause2 = new TerminalClauseImpl("blah2", Operator.GREATER_THAN, "val2");
        TerminalClause termClause2negated = new TerminalClauseImpl("blah2", Operator.LESS_THAN_EQUALS, "val2");
        OrClause orClause = new OrClause(termClause1, new NotClause(termClause2));

        final ClauseContext clauseContext1 = createProject(18202);
        final ProjectIssueTypeContext projectIssueTypeContext = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), new IssueTypeContextImpl("10"));
        final ClauseContext clauseContext2 = new ClauseContextImpl(Collections.singleton(projectIssueTypeContext));

        final ClauseContextFactory clauseContextFactory = mockController.getNiceMock(ClauseContextFactory.class);
        expect(clauseContextFactory.getClauseContext(null, termClause1)).andReturn(clauseContext1);
        expect(clauseContextFactory.getClauseContext(null, termClause2negated)).andReturn(clauseContext2);

        final ClauseHandler clauseHandler = new MockClauseHandler().setContextFactory(clauseContextFactory).setInformation(new MockClauseInformation(new ClauseNames("blah1")));
        final ClauseHandler clauseHandler2 = new MockClauseHandler().setContextFactory(clauseContextFactory).setInformation(new MockClauseInformation(new ClauseNames("blah2")));

        final SearchHandlerManager searchHandlerManager = mockController.getMock(SearchHandlerManager.class);
        expect(searchHandlerManager.getClauseHandler((User) null, "blah1")).andReturn(Collections.singleton(clauseHandler));
        expect(searchHandlerManager.getClauseHandler((User) null, "blah2")).andReturn(Collections.singleton(clauseHandler2));

        final ContextSetUtil contextSetUtil = mockController.getMock(ContextSetUtil.class);

        final ClauseContext clauseContext3 = createProject(15,6,0x3993);
        expect(contextSetUtil.union(CollectionBuilder.newBuilder(clauseContext1,  clauseContext2).asSet())).andReturn(clauseContext3);

        mockController.replay();

        QueryContextVisitor visitor = new QueryContextVisitor(null, contextSetUtil, searchHandlerManager);
        final QueryContextVisitor.ContextResult result = orClause.accept(visitor);
        QueryContextVisitor.ContextResult expectedResult = new QueryContextVisitor.ContextResult(clauseContext3, ClauseContextImpl.createGlobalClauseContext());

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testOrUnionSimpleAndComplexDifferent() throws Exception
    {
        TerminalClause termClause1 = new TerminalClauseImpl("blah1", Operator.GREATER_THAN, "val1");
        TerminalClause termClause2 = new TerminalClauseImpl("project", Operator.GREATER_THAN, "val2");
        TerminalClause termClause3 = new TerminalClauseImpl("type", Operator.GREATER_THAN, "val2");
        Clause andClause = new OrClause(termClause1, termClause2, termClause3);

        final ClauseContext clauseContext1 = createProject(78);
        final ProjectIssueTypeContext projectIssueTypeContext = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), new IssueTypeContextImpl("10"));
        final ClauseContext clauseContext2 = new ClauseContextImpl(Collections.singleton(projectIssueTypeContext));
        final ClauseContext clauseContext3 = createProject(678, 292);

        final ClauseContextFactory clauseContextFactory = mockController.getNiceMock(ClauseContextFactory.class);
        expect(clauseContextFactory.getClauseContext(null, termClause1)).andReturn(clauseContext1);
        expect(clauseContextFactory.getClauseContext(null, termClause2)).andReturn(clauseContext2);
        expect(clauseContextFactory.getClauseContext(null, termClause3)).andReturn(clauseContext3);

        final ClauseHandler clauseHandler = new MockClauseHandler().setContextFactory(clauseContextFactory).setInformation(new MockClauseInformation(new ClauseNames("blah1")));
        final ClauseHandler clauseHandler2 = new MockClauseHandler().setContextFactory(clauseContextFactory).setInformation(new MockClauseInformation(SystemSearchConstants.forProject().getJqlClauseNames()));
        final ClauseHandler clauseHandler3 = new MockClauseHandler().setContextFactory(clauseContextFactory).setInformation(new MockClauseInformation(SystemSearchConstants.forIssueType().getJqlClauseNames()));

        final SearchHandlerManager searchHandlerManager = mockController.getMock(SearchHandlerManager.class);
        expect(searchHandlerManager.getClauseHandler((User) null, "blah1")).andReturn(Collections.singleton(clauseHandler));
        expect(searchHandlerManager.getClauseHandler((User) null, "project")).andReturn(Collections.singleton(clauseHandler2));
        expect(searchHandlerManager.getClauseHandler((User) null, "type")).andReturn(Collections.singleton(clauseHandler3));

        final ContextSetUtil contextSetUtil = mockController.getMock(ContextSetUtil.class);

        // the end result of visitation is at a minimum the Global context; never an empty context.
        final ClauseContext clauseContextComplex = createProject(2, 4, 6);
        final ClauseContext clauseContextSimple = createProject(2, 4, 6);
        expect(contextSetUtil.union(CollectionBuilder.newBuilder(clauseContext1, clauseContext2, clauseContext3).asSet())).andReturn(clauseContextComplex);
        expect(contextSetUtil.union(CollectionBuilder.newBuilder(ClauseContextImpl.createGlobalClauseContext(), clauseContext2, clauseContext3).asSet())).andReturn(clauseContextSimple);

        mockController.replay();

        QueryContextVisitor visitor = new QueryContextVisitor(null, contextSetUtil, searchHandlerManager);
        final QueryContextVisitor.ContextResult result = andClause.accept(visitor);

        QueryContextVisitor.ContextResult expectedResult = new QueryContextVisitor.ContextResult(clauseContextComplex, clauseContextSimple);

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testOrUnionSimpleAndComplexSame() throws Exception
    {
        TerminalClause termClause2 = new TerminalClauseImpl("project", Operator.GREATER_THAN, "val2");
        TerminalClause termClause3 = new TerminalClauseImpl("type", Operator.GREATER_THAN, "val2");
        Clause andClause = new OrClause(termClause2, termClause3);

        final ProjectIssueTypeContext projectIssueTypeContext = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), new IssueTypeContextImpl("10"));
        final ClauseContext clauseContext2 = new ClauseContextImpl(Collections.singleton(projectIssueTypeContext));
        final ClauseContext clauseContext3 = createProject(678, 292);

        final ClauseContextFactory clauseContextFactory = mockController.getNiceMock(ClauseContextFactory.class);
        expect(clauseContextFactory.getClauseContext(null, termClause2)).andReturn(clauseContext2);
        expect(clauseContextFactory.getClauseContext(null, termClause3)).andReturn(clauseContext3);

        final ClauseHandler clauseHandler2 = new MockClauseHandler().setContextFactory(clauseContextFactory).setInformation(new MockClauseInformation(SystemSearchConstants.forProject().getJqlClauseNames()));
        final ClauseHandler clauseHandler3 = new MockClauseHandler().setContextFactory(clauseContextFactory).setInformation(new MockClauseInformation(SystemSearchConstants.forIssueType().getJqlClauseNames()));

        final SearchHandlerManager searchHandlerManager = mockController.getMock(SearchHandlerManager.class);
        expect(searchHandlerManager.getClauseHandler((User) null, "project")).andReturn(Collections.singleton(clauseHandler2));
        expect(searchHandlerManager.getClauseHandler((User) null, "type")).andReturn(Collections.singleton(clauseHandler3));

        final ContextSetUtil contextSetUtil = mockController.getMock(ContextSetUtil.class);

        // the end result of visitation is at a minimum the Global context; never an empty context.
        final ClauseContext clauseContextComplex = createProject(2, 4, 6);
        expect(contextSetUtil.union(CollectionBuilder.newBuilder(clauseContext2, clauseContext3).asSet())).andReturn(clauseContextComplex);

        mockController.replay();

        QueryContextVisitor visitor = new QueryContextVisitor(null, contextSetUtil, searchHandlerManager);
        final QueryContextVisitor.ContextResult result = andClause.accept(visitor);

        QueryContextVisitor.ContextResult expectedResult = new QueryContextVisitor.ContextResult(clauseContextComplex, clauseContextComplex);

        assertEquals(expectedResult, result);

        mockController.verify();
    }


    @Test
    public void testAnd() throws Exception
    {
        TerminalClause termClause1 = new TerminalClauseImpl("blah1", Operator.GREATER_THAN, "val1");
        TerminalClause termClause2 = new TerminalClauseImpl("blah2", Operator.GREATER_THAN, "val2");
        Clause andClause = new AndClause(termClause1, termClause2);

        final ClauseContext clauseContext1 = createProject(78);

        final ClauseContextFactory clauseContextFactory = mockController.getNiceMock(ClauseContextFactory.class);
        expect(clauseContextFactory.getClauseContext(null, termClause1)).andReturn(clauseContext1);
        expect(clauseContextFactory.getClauseContext(null, termClause2)).andReturn(clauseContext1);

        final ClauseHandler clauseHandler = new MockClauseHandler().setContextFactory(clauseContextFactory).setInformation(new MockClauseInformation(new ClauseNames("blah1")));
        final ClauseHandler clauseHandler2 = new MockClauseHandler().setContextFactory(clauseContextFactory).setInformation(new MockClauseInformation(new ClauseNames("blah2")));

        final SearchHandlerManager searchHandlerManager = mockController.getMock(SearchHandlerManager.class);
        expect(searchHandlerManager.getClauseHandler((User) null, "blah1")).andReturn(Collections.singleton(clauseHandler));
        expect(searchHandlerManager.getClauseHandler((User) null, "blah2")).andReturn(Collections.singleton(clauseHandler2));

        final ContextSetUtil contextSetUtil = mockController.getMock(ContextSetUtil.class);

        mockController.replay();

        QueryContextVisitor visitor = new QueryContextVisitor(null, contextSetUtil, searchHandlerManager);
        final QueryContextVisitor.ContextResult result = andClause.accept(visitor);

        QueryContextVisitor.ContextResult expectedResult = new QueryContextVisitor.ContextResult(clauseContext1, ClauseContextImpl.createGlobalClauseContext());

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testAndIntersect() throws Exception
    {
        //We are checking the negation here.
        TerminalClause termClause1 = new TerminalClauseImpl("blah1", Operator.GREATER_THAN, "val1");
        TerminalClause termClause1negated = new TerminalClauseImpl("blah1", Operator.LESS_THAN_EQUALS, "val1");
        TerminalClause termClause2 = new TerminalClauseImpl("blah2", Operator.GREATER_THAN, "val2");
        TerminalClause termClause2negated = new TerminalClauseImpl("blah2", Operator.LESS_THAN_EQUALS, "val2");
        Clause andClause = new NotClause(new OrClause(termClause1,termClause2));

        final ClauseContext clauseContext1 = createProject(78);
        final ProjectIssueTypeContext projectIssueTypeContext = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), new IssueTypeContextImpl("10"));
        final ClauseContext clauseContext2 = new ClauseContextImpl(Collections.singleton(projectIssueTypeContext));

        final ClauseContextFactory clauseContextFactory = mockController.getNiceMock(ClauseContextFactory.class);
        expect(clauseContextFactory.getClauseContext(null, termClause1negated)).andReturn(clauseContext1);
        expect(clauseContextFactory.getClauseContext(null, termClause2negated)).andReturn(clauseContext2);

        final ClauseHandler clauseHandler = new MockClauseHandler().setContextFactory(clauseContextFactory).setInformation(new MockClauseInformation(new ClauseNames("blah1")));
        final ClauseHandler clauseHandler2 = new MockClauseHandler().setContextFactory(clauseContextFactory).setInformation(new MockClauseInformation(new ClauseNames("blah2")));

        final SearchHandlerManager searchHandlerManager = mockController.getMock(SearchHandlerManager.class);
        expect(searchHandlerManager.getClauseHandler((User) null, "blah1")).andReturn(Collections.singleton(clauseHandler));
        expect(searchHandlerManager.getClauseHandler((User) null, "blah2")).andReturn(Collections.singleton(clauseHandler2));

        final ContextSetUtil contextSetUtil = mockController.getMock(ContextSetUtil.class);

        // the end result of visitation is at a minimum the Global context; never an empty context.
        final ClauseContext clauseContextComplex = createProject(2, 4, 6);
        expect(contextSetUtil.intersect(CollectionBuilder.newBuilder(clauseContext1, clauseContext2).asSet())).andReturn(clauseContextComplex);

        mockController.replay();

        QueryContextVisitor visitor = new QueryContextVisitor(null, contextSetUtil, searchHandlerManager);
        final QueryContextVisitor.ContextResult result = andClause.accept(visitor);

        QueryContextVisitor.ContextResult expectedResult = new QueryContextVisitor.ContextResult(clauseContextComplex, ClauseContextImpl.createGlobalClauseContext());

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testAndIntersectSimpleAndComplexDifferent() throws Exception
    {
        TerminalClause termClause1 = new TerminalClauseImpl("blah1", Operator.GREATER_THAN, "val1");
        TerminalClause termClause2 = new TerminalClauseImpl("project", Operator.GREATER_THAN, "val2");
        TerminalClause termClause3 = new TerminalClauseImpl("type", Operator.GREATER_THAN, "val2");
        Clause andClause = new AndClause(termClause1, termClause2, termClause3);

        final ClauseContext clauseContext1 = createProject(78);
        final ProjectIssueTypeContext projectIssueTypeContext = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), new IssueTypeContextImpl("10"));
        final ClauseContext clauseContext2 = new ClauseContextImpl(Collections.singleton(projectIssueTypeContext));
        final ClauseContext clauseContext3 = createProject(678, 292);

        final ClauseContextFactory clauseContextFactory = mockController.getNiceMock(ClauseContextFactory.class);
        expect(clauseContextFactory.getClauseContext(null, termClause1)).andReturn(clauseContext1);
        expect(clauseContextFactory.getClauseContext(null, termClause2)).andReturn(clauseContext2);
        expect(clauseContextFactory.getClauseContext(null, termClause3)).andReturn(clauseContext3);

        final ClauseHandler clauseHandler = new MockClauseHandler().setContextFactory(clauseContextFactory).setInformation(new MockClauseInformation(new ClauseNames("blah1")));
        final ClauseHandler clauseHandler2 = new MockClauseHandler().setContextFactory(clauseContextFactory).setInformation(new MockClauseInformation(SystemSearchConstants.forProject().getJqlClauseNames()));
        final ClauseHandler clauseHandler3 = new MockClauseHandler().setContextFactory(clauseContextFactory).setInformation(new MockClauseInformation(SystemSearchConstants.forIssueType().getJqlClauseNames()));

        final SearchHandlerManager searchHandlerManager = mockController.getMock(SearchHandlerManager.class);
        expect(searchHandlerManager.getClauseHandler((User) null, "blah1")).andReturn(Collections.singleton(clauseHandler));
        expect(searchHandlerManager.getClauseHandler((User) null, "project")).andReturn(Collections.singleton(clauseHandler2));
        expect(searchHandlerManager.getClauseHandler((User) null, "type")).andReturn(Collections.singleton(clauseHandler3));

        final ContextSetUtil contextSetUtil = mockController.getMock(ContextSetUtil.class);

        // the end result of visitation is at a minimum the Global context; never an empty context.
        final ClauseContext clauseContextComplex = createProject(2, 4, 6);
        final ClauseContext clauseContextSimple = createProject(2, 4, 6);
        expect(contextSetUtil.intersect(CollectionBuilder.newBuilder(clauseContext1, clauseContext2, clauseContext3).asSet())).andReturn(clauseContextComplex);
        expect(contextSetUtil.intersect(CollectionBuilder.newBuilder(ClauseContextImpl.createGlobalClauseContext(), clauseContext2, clauseContext3).asSet())).andReturn(clauseContextSimple);

        mockController.replay();

        QueryContextVisitor visitor = new QueryContextVisitor(null, contextSetUtil, searchHandlerManager);
        final QueryContextVisitor.ContextResult result = andClause.accept(visitor);

        QueryContextVisitor.ContextResult expectedResult = new QueryContextVisitor.ContextResult(clauseContextComplex, clauseContextSimple);

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testAndIntersectSimpleAndComplexSame() throws Exception
    {
        TerminalClause termClause1 = new TerminalClauseImpl("blah1", Operator.GREATER_THAN, "val1");
        TerminalClause termClause2 = new TerminalClauseImpl("project", Operator.GREATER_THAN, "val2");
        TerminalClause termClause3 = new TerminalClauseImpl("type", Operator.GREATER_THAN, "val2");
        Clause andClause = new AndClause(termClause1, termClause2, termClause3);

        final ClauseContext clauseContext1 = ClauseContextImpl.createGlobalClauseContext();
        final ProjectIssueTypeContext projectIssueTypeContext = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), new IssueTypeContextImpl("10"));
        final ClauseContext clauseContext2 = new ClauseContextImpl(Collections.singleton(projectIssueTypeContext));
        final ClauseContext clauseContext3 = createProject(678, 292);

        final ClauseContextFactory clauseContextFactory = mockController.getNiceMock(ClauseContextFactory.class);
        expect(clauseContextFactory.getClauseContext(null, termClause1)).andReturn(clauseContext1);
        expect(clauseContextFactory.getClauseContext(null, termClause2)).andReturn(clauseContext2);
        expect(clauseContextFactory.getClauseContext(null, termClause3)).andReturn(clauseContext3);

        final ClauseHandler clauseHandler = new MockClauseHandler().setContextFactory(clauseContextFactory).setInformation(new MockClauseInformation(new ClauseNames("blah1")));
        final ClauseHandler clauseHandler2 = new MockClauseHandler().setContextFactory(clauseContextFactory).setInformation(new MockClauseInformation(SystemSearchConstants.forProject().getJqlClauseNames()));
        final ClauseHandler clauseHandler3 = new MockClauseHandler().setContextFactory(clauseContextFactory).setInformation(new MockClauseInformation(SystemSearchConstants.forIssueType().getJqlClauseNames()));

        final SearchHandlerManager searchHandlerManager = mockController.getMock(SearchHandlerManager.class);
        expect(searchHandlerManager.getClauseHandler((User) null, "blah1")).andReturn(Collections.singleton(clauseHandler));
        expect(searchHandlerManager.getClauseHandler((User) null, "project")).andReturn(Collections.singleton(clauseHandler2));
        expect(searchHandlerManager.getClauseHandler((User) null, "type")).andReturn(Collections.singleton(clauseHandler3));

        final ContextSetUtil contextSetUtil = mockController.getMock(ContextSetUtil.class);

        // the end result of visitation is at a minimum the Global context; never an empty context.
        final ClauseContext clauseContextComplex = createProject(2, 4, 6);
        expect(contextSetUtil.intersect(CollectionBuilder.newBuilder(clauseContext1, clauseContext2, clauseContext3).asSet())).andReturn(clauseContextComplex);

        mockController.replay();

        QueryContextVisitor visitor = new QueryContextVisitor(null, contextSetUtil, searchHandlerManager);
        final QueryContextVisitor.ContextResult result = andClause.accept(visitor);

        QueryContextVisitor.ContextResult expectedResult = new QueryContextVisitor.ContextResult(clauseContextComplex, clauseContextComplex);

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testTerminalClause() throws Exception
    {
        TerminalClause termClause1 = new TerminalClauseImpl("blah1", Operator.GREATER_THAN, "val1");

        final ClauseContext clauseContext1 = createProject(394748494);

        final ClauseContextFactory clauseContextFactory = mockController.getNiceMock(ClauseContextFactory.class);
        expect(clauseContextFactory.getClauseContext(null, termClause1)).andReturn(clauseContext1);

        final ClauseHandler clauseHandler = new MockClauseHandler().setContextFactory(clauseContextFactory).setInformation(new MockClauseInformation(new ClauseNames("blah1")));

        final SearchHandlerManager searchHandlerManager = mockController.getMock(SearchHandlerManager.class);
        expect(searchHandlerManager.getClauseHandler((User) null, "blah1")).andReturn(Collections.singleton(clauseHandler));

        final ContextSetUtil contextSetUtil = mockController.getMock(ContextSetUtil.class);

        mockController.replay();

        final QueryContextVisitor.QueryContextVisitorFactory visitorFactory = new QueryContextVisitor.QueryContextVisitorFactory(contextSetUtil, searchHandlerManager);
        QueryContextVisitor visitor = visitorFactory.createVisitor(null);
        final QueryContextVisitor.ContextResult result = termClause1.accept(visitor);

        final QueryContextVisitor.ContextResult expectedResult = new QueryContextVisitor.ContextResult(clauseContext1, ClauseContextImpl.createGlobalClauseContext());
        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testTerminalClauseNoHandlers() throws Exception
    {
        TerminalClause termClause1 = new TerminalClauseImpl("blah1", Operator.GREATER_THAN, "val1");

        final SearchHandlerManager searchHandlerManager = mockController.getMock(SearchHandlerManager.class);
        expect(searchHandlerManager.getClauseHandler((User) null, "blah1")).andReturn(Collections.<ClauseHandler>emptyList());

        final ContextSetUtil contextSetUtil = mockController.getMock(ContextSetUtil.class);

        mockController.replay();

        final QueryContextVisitor.QueryContextVisitorFactory visitorFactory = new QueryContextVisitor.QueryContextVisitorFactory(contextSetUtil, searchHandlerManager);
        QueryContextVisitor visitor = visitorFactory.createVisitor(null);
        final QueryContextVisitor.ContextResult result = termClause1.accept(visitor);

        final QueryContextVisitor.ContextResult expectedResult = new QueryContextVisitor.ContextResult(ClauseContextImpl.createGlobalClauseContext(), ClauseContextImpl.createGlobalClauseContext());
        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testTerminalClauseMultipleHandlers() throws Exception
    {
        final TerminalClause termClause1 = new TerminalClauseImpl("gin", Operator.EQUALS, "1");

        final ClauseContext clauseContext1 = createProject(1);
        final ClauseContext clauseContext2 = createProject(2);
        final ClauseContext clauseContext3 = createProject(3);

        final ClauseContextFactory clauseContextFactory = mockController.getNiceMock(ClauseContextFactory.class);
        expect(clauseContextFactory.getClauseContext(null, termClause1)).andReturn(clauseContext1);

        final ClauseContextFactory clauseContextFactory2 = mockController.getNiceMock(ClauseContextFactory.class);
        expect(clauseContextFactory2.getClauseContext(null, termClause1)).andReturn(clauseContext2);

        final ClauseHandler handler1 = new MockClauseHandler().setContextFactory(clauseContextFactory).setInformation(new MockClauseInformation(new ClauseNames("gin")));
        final ClauseHandler handler2 = new MockClauseHandler().setContextFactory(clauseContextFactory2).setInformation(new MockClauseInformation(new ClauseNames("gin")));

        final SearchHandlerManager searchHandlerManager = mockController.getMock(SearchHandlerManager.class);
        expect(searchHandlerManager.getClauseHandler((User) null, "gin")).andReturn(Arrays.asList(handler1, handler2));

        final ContextSetUtil contextSetUtil = mockController.getMock(ContextSetUtil.class);
        expect(contextSetUtil.union(CollectionBuilder.newBuilder(clauseContext1, clauseContext2).asSet())).andReturn(clauseContext3);
        
        mockController.replay();

        final QueryContextVisitor.QueryContextVisitorFactory visitorFactory = new QueryContextVisitor.QueryContextVisitorFactory(contextSetUtil, searchHandlerManager);
        QueryContextVisitor visitor = visitorFactory.createVisitor(null);
        final QueryContextVisitor.ContextResult result = termClause1.accept(visitor);

        final QueryContextVisitor.ContextResult expectedResult = new QueryContextVisitor.ContextResult(clauseContext3, ClauseContextImpl.createGlobalClauseContext());
        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testTerminalClauseExplicit() throws Exception
    {
        final TerminalClause termClause1 = new TerminalClauseImpl("project", Operator.EQUALS, "1");

        final ClauseContext clauseContext1 = createProject(1);

        final ClauseContextFactory clauseContextFactory = mockController.getNiceMock(ClauseContextFactory.class);
        expect(clauseContextFactory.getClauseContext(null, termClause1)).andReturn(clauseContext1);

        final ClauseHandler handler1 = new MockClauseHandler().setContextFactory(clauseContextFactory).setInformation(new MockClauseInformation(SystemSearchConstants.forProject().getJqlClauseNames()));

        final SearchHandlerManager searchHandlerManager = mockController.getMock(SearchHandlerManager.class);
        expect(searchHandlerManager.getClauseHandler((User) null, "project")).andReturn(Arrays.asList(handler1));

        final ContextSetUtil contextSetUtil = mockController.getMock(ContextSetUtil.class);

        mockController.replay();

        final QueryContextVisitor.QueryContextVisitorFactory visitorFactory = new QueryContextVisitor.QueryContextVisitorFactory(contextSetUtil, searchHandlerManager);
        QueryContextVisitor visitor = visitorFactory.createVisitor(null);
        final QueryContextVisitor.ContextResult result = termClause1.accept(visitor);

        final QueryContextVisitor.ContextResult expectedResult = new QueryContextVisitor.ContextResult(clauseContext1, clauseContext1);
        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testTerminalClauseEmptyContext() throws Exception
    {
        final TerminalClause termClause1 = new TerminalClauseImpl("project", Operator.EQUALS, "1");

        final ClauseContextFactory clauseContextFactory = mockController.getNiceMock(ClauseContextFactory.class);
        expect(clauseContextFactory.getClauseContext(null, termClause1)).andReturn(new ClauseContextImpl());

        final ClauseHandler handler1 = new MockClauseHandler().setContextFactory(clauseContextFactory).setInformation(new MockClauseInformation(SystemSearchConstants.forProject().getJqlClauseNames()));

        final SearchHandlerManager searchHandlerManager = mockController.getMock(SearchHandlerManager.class);
        expect(searchHandlerManager.getClauseHandler((User) null, "project")).andReturn(Arrays.asList(handler1));

        final ContextSetUtil contextSetUtil = mockController.getMock(ContextSetUtil.class);

        mockController.replay();

        final QueryContextVisitor.QueryContextVisitorFactory visitorFactory = new QueryContextVisitor.QueryContextVisitorFactory(contextSetUtil, searchHandlerManager);
        QueryContextVisitor visitor = visitorFactory.createVisitor(null);
        final QueryContextVisitor.ContextResult result = termClause1.accept(visitor);

        final QueryContextVisitor.ContextResult expectedResult = new QueryContextVisitor.ContextResult(ClauseContextImpl.createGlobalClauseContext(), ClauseContextImpl.createGlobalClauseContext());
        assertEquals(expectedResult, result);

        mockController.verify();
    }

    private static ClauseContext createProject(int ... ids)
    {
        Set<ProjectIssueTypeContext> ctxs = new HashSet<ProjectIssueTypeContext>();
        for (int id : ids)
        {
            ctxs.add(new ProjectIssueTypeContextImpl(new ProjectContextImpl((long) id), AllIssueTypesContext.getInstance()));
        }
        return new ClauseContextImpl(ctxs);
    }
}
