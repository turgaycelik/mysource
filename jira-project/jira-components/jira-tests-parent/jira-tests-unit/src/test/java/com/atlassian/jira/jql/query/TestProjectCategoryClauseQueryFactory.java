package com.atlassian.jira.jql.query;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.resolver.ProjectCategoryResolver;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.util.Comparator;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestProjectCategoryClauseQueryFactory extends MockControllerTestCase
{
    private ProjectCategoryResolver projectCategoryResolver;
    private JqlOperandResolver jqlOperandResolver;
    private User theUser = null;
    private QueryCreationContext queryCreationContext;

    @Before
    public void setUp() throws Exception
    {
        projectCategoryResolver = mockController.getMock(ProjectCategoryResolver.class);
        jqlOperandResolver = MockJqlOperandResolver.createSimpleSupport();
        queryCreationContext = new QueryCreationContextImpl(theUser);
    }

    @Test
    public void testBadOperator() throws Exception
    {
        final TerminalClause terminalClause = new TerminalClauseImpl("category", Operator.LIKE, "test");

        replay();
        final ProjectCategoryClauseQueryFactory factory = new ProjectCategoryClauseQueryFactory(projectCategoryResolver, jqlOperandResolver);

        final QueryFactoryResult result = factory.getQuery(queryCreationContext, terminalClause);
        final Query expectedQuery = new BooleanQuery();
        assertEquals(expectedQuery, result.getLuceneQuery());
        assertFalse(result.mustNotOccur());

        verify();
    }

    @Test
    public void testNullLiterals() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("test");
        final TerminalClause terminalClause = new TerminalClauseImpl("category", Operator.EQUALS, operand);

        jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        EasyMock.expect(jqlOperandResolver.getValues(queryCreationContext, operand, terminalClause))
                .andReturn(null);

        replay();
        final ProjectCategoryClauseQueryFactory factory = new ProjectCategoryClauseQueryFactory(projectCategoryResolver, jqlOperandResolver);

        final QueryFactoryResult result = factory.getQuery(queryCreationContext, terminalClause);
        final Query expectedQuery = new BooleanQuery();
        assertEquals(expectedQuery, result.getLuceneQuery());
        assertFalse(result.mustNotOccur());

        verify();
    }

    @Test
    public void testEqualitySingleValue() throws Exception
    {
        final Operand operand = new SingleValueOperand(createLiteral(1L));
        final TerminalClause terminalClause = new TerminalClauseImpl("category", Operator.EQUALS, operand);

        EasyMock.expect(projectCategoryResolver.getProjectsForCategory(createLiteral(1L)))
                .andReturn(CollectionBuilder.<Project>newBuilder(new MockProject(2L)).asSet());

        replay();
        final ProjectCategoryClauseQueryFactory factory = new ProjectCategoryClauseQueryFactory(projectCategoryResolver, jqlOperandResolver);

        final QueryFactoryResult result = factory.getQuery(queryCreationContext, terminalClause);
        final Query expectedQuery = new TermQuery(new Term("projid", "2"));
        assertEquals(expectedQuery, result.getLuceneQuery());
        assertFalse(result.mustNotOccur());

        verify();
    }

    @Test
    public void testEqualityMultiValue() throws Exception
    {
        final MultiValueOperand operand = new MultiValueOperand(new QueryLiteral(), createLiteral(1L), createLiteral("test"));
        final TerminalClause terminalClause = new TerminalClauseImpl("category", Operator.IN, operand);

        EasyMock.expect(projectCategoryResolver.getProjectsForCategory(new QueryLiteral()))
                .andReturn(CollectionBuilder.<Project>newBuilder(new MockProject(1L)).asSet());
        EasyMock.expect(projectCategoryResolver.getProjectsForCategory(createLiteral(1L)))
                .andReturn(CollectionBuilder.<Project>newBuilder(new MockProject(2L)).asSet());
        EasyMock.expect(projectCategoryResolver.getProjectsForCategory(createLiteral("test")))
                .andReturn(CollectionBuilder.<Project>newBuilder(new MockProject(3L)).asSet());

        replay();
        final ProjectCategoryClauseQueryFactory factory = new ProjectCategoryClauseQueryFactory(projectCategoryResolver, jqlOperandResolver);

        final QueryFactoryResult result = factory.getQuery(queryCreationContext, terminalClause);
        final BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(new TermQuery(new Term("projid", "1")), BooleanClause.Occur.SHOULD);
        expectedQuery.add(new TermQuery(new Term("projid", "2")), BooleanClause.Occur.SHOULD);
        expectedQuery.add(new TermQuery(new Term("projid", "3")), BooleanClause.Occur.SHOULD);
        assertEquals(expectedQuery, ((BooleanQuery) result.getLuceneQuery()));
        assertFalse(result.mustNotOccur());

        verify();
    }

    @Test
    public void testInequalityWithEmpty() throws Exception
    {
        final MultiValueOperand operand = new MultiValueOperand(new QueryLiteral(), createLiteral(1L), createLiteral("test"));
        final TerminalClause terminalClause = new TerminalClauseImpl("category", Operator.NOT_IN, operand);

        EasyMock.expect(projectCategoryResolver.getProjectsForCategory(new QueryLiteral()))
                .andReturn(CollectionBuilder.<Project>newBuilder(new MockProject(1L)).asSet());
        EasyMock.expect(projectCategoryResolver.getProjectsForCategory(createLiteral(1L)))
                .andReturn(CollectionBuilder.<Project>newBuilder(new MockProject(2L)).asSet());
        EasyMock.expect(projectCategoryResolver.getProjectsForCategory(createLiteral("test")))
                .andReturn(CollectionBuilder.<Project>newBuilder(new MockProject(3L)).asSet());

        replay();
        final ProjectCategoryClauseQueryFactory factory = new ProjectCategoryClauseQueryFactory(projectCategoryResolver, jqlOperandResolver);

        final QueryFactoryResult result = factory.getQuery(queryCreationContext, terminalClause);
        final BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(new TermQuery(new Term("projid", "1")), BooleanClause.Occur.SHOULD);
        expectedQuery.add(new TermQuery(new Term("projid", "2")), BooleanClause.Occur.SHOULD);
        expectedQuery.add(new TermQuery(new Term("projid", "3")), BooleanClause.Occur.SHOULD);
        assertEquals(expectedQuery, ((BooleanQuery) result.getLuceneQuery()));
        assertTrue(result.mustNotOccur());

        verify();
    }

    @Test
    public void testInequalityWithoutEmpty() throws Exception
    {
        final MultiValueOperand operand = new MultiValueOperand(createLiteral(1L), createLiteral("test"));
        final TerminalClause terminalClause = new TerminalClauseImpl("category", Operator.NOT_IN, operand);

        EasyMock.expect(projectCategoryResolver.getProjectsForCategory(createLiteral(1L)))
                .andReturn(CollectionBuilder.<Project>newBuilder(new MockProject(2L)).asSet());
        EasyMock.expect(projectCategoryResolver.getProjectsForCategory(createLiteral("test")))
                .andReturn(CollectionBuilder.<Project>newBuilder(new MockProject(3L)).asSet());
        EasyMock.expect(projectCategoryResolver.getProjectsForCategory(new QueryLiteral()))
                .andReturn(CollectionBuilder.<Project>newBuilder(new MockProject(1L)).asSet());
        replay();
        final ProjectCategoryClauseQueryFactory factory = new ProjectCategoryClauseQueryFactory(projectCategoryResolver, jqlOperandResolver);

        final QueryFactoryResult result = factory.getQuery(queryCreationContext, terminalClause);
        final BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(new TermQuery(new Term("projid", "2")), BooleanClause.Occur.SHOULD);
        expectedQuery.add(new TermQuery(new Term("projid", "3")), BooleanClause.Occur.SHOULD);
        expectedQuery.add(new TermQuery(new Term("projid", "1")), BooleanClause.Occur.SHOULD);
        assertEquals(expectedQuery, ((BooleanQuery) result.getLuceneQuery()));
        assertTrue(result.mustNotOccur());

        verify();
    }

}
