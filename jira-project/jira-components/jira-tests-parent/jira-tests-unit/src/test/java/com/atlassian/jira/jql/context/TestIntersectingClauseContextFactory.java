package com.atlassian.jira.jql.context;

import java.util.Collections;

import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import org.easymock.classextension.EasyMock;
import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * @since v4.0
 */
public class TestIntersectingClauseContextFactory extends MockControllerTestCase
{
    @Test
    public void testSingleSub() throws Exception
    {
        final ClauseContextFactory sub1 = EasyMock.createMock(ClauseContextFactory.class);
        final TerminalClause aClause = new TerminalClauseImpl("blarg", Operator.NOT_EQUALS, "blarg");

        final ClauseContext context = EasyMock.createMock(ClauseContext.class);
        EasyMock.expect(sub1.getClauseContext(null, aClause)).andReturn(context);

        final ContextSetUtil contextSetUtil = mockController.getMock(ContextSetUtil.class);
        contextSetUtil.intersect(CollectionBuilder.newBuilder(context).asSet());
        mockController.setReturnValue(context);

        mockController.replay();
        final IntersectingClauseContextFactory factory = new IntersectingClauseContextFactory(contextSetUtil, CollectionBuilder.newBuilder(sub1).asCollection());

        EasyMock.replay(sub1, context);
        
        factory.getClauseContext(null, aClause);

        mockController.verify();
        EasyMock.verify(sub1, context);
    }

    @Test
    public void testTwoSubs() throws Exception
    {
        final ClauseContextFactory sub1 = EasyMock.createMock(ClauseContextFactory.class);
        final ClauseContextFactory sub2 = EasyMock.createMock(ClauseContextFactory.class);
        final TerminalClause aClause = new TerminalClauseImpl("blarg", Operator.NOT_EQUALS, "blarg");

        final ClauseContext context1 = EasyMock.createMock(ClauseContext.class);
        final ClauseContext context2 = EasyMock.createMock(ClauseContext.class);
        EasyMock.expect(sub1.getClauseContext(null, aClause)).andReturn(context1);
        EasyMock.expect(sub2.getClauseContext(null, aClause)).andReturn(context2);

        final ContextSetUtil contextSetUtil = mockController.getMock(ContextSetUtil.class);
        contextSetUtil.intersect(CollectionBuilder.newBuilder(context1, context2).asSet());
        mockController.setReturnValue(context1);

        final IntersectingClauseContextFactory factory = new IntersectingClauseContextFactory(contextSetUtil, CollectionBuilder.newBuilder(sub1, sub2).asCollection());

        EasyMock.replay(sub1, sub2, context1, context2);
        mockController.replay();

        factory.getClauseContext(null, aClause);

        mockController.verify();
    }

    @Test
    public void testNoSubs() throws Exception
    {
        final TerminalClause aClause = new TerminalClauseImpl("blarg", Operator.NOT_EQUALS, "blarg");

        final ContextSetUtil contextSetUtil = mockController.getMock(ContextSetUtil.class);
        contextSetUtil.intersect(CollectionBuilder.<ClauseContext>newBuilder().asSet());
        mockController.setReturnValue(null);

        final IntersectingClauseContextFactory factory = new IntersectingClauseContextFactory(contextSetUtil, Collections.<ClauseContextFactory>emptyList());

        mockController.replay();

        factory.getClauseContext(null, aClause);

        mockController.verify();
    }

    @Test
    public void testConstructorExceptions() throws Exception
    {
        final ContextSetUtil contextSetUtil = mockController.getMock(ContextSetUtil.class);
        mockController.replay();

        try
        {
            new IntersectingClauseContextFactory(null, null);
            fail("IAE expected");
        }
        catch (IllegalArgumentException e) { }

        try
        {
            new IntersectingClauseContextFactory(contextSetUtil, CollectionBuilder.<ClauseContextFactory>newBuilder(null, null).asCollection());
            fail("IAE expected");
        }
        catch (IllegalArgumentException e) { }
    }
}
