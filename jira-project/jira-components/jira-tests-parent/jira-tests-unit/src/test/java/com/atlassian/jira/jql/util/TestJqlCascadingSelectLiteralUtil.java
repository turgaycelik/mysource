package com.atlassian.jira.jql.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.atlassian.jira.action.issue.customfields.option.MockOption;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.operand.EmptyOperand;

import org.junit.Before;
import org.junit.Test;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestJqlCascadingSelectLiteralUtil extends MockControllerTestCase
{
    private JqlSelectOptionsUtil jqlSelectOptionsUtil;

    @Before
    public void setUp() throws Exception
    {
        jqlSelectOptionsUtil = mockController.getMock(JqlSelectOptionsUtil.class);
    }

    @Test
    public void testCreateQueryLiteralsFromOptions() throws Exception
    {
        Option posOption = new MockOption(null, null, null, null, null, 10L);
        Option negOption = new MockOption(null, null, null, null, null, 20L);

        mockController.replay();
        final JqlCascadingSelectLiteralUtil util = new JqlCascadingSelectLiteralUtil(jqlSelectOptionsUtil);

        final List<QueryLiteral> result = util.createQueryLiteralsFromOptions(EmptyOperand.EMPTY, Collections.singleton(posOption), Collections.singleton(negOption));

        assertEquals(2, result.size());
        assertTrue(result.contains(createLiteral(10L)));
        assertTrue(result.contains(createLiteral(-20L)));

        mockController.verify();
    }

    @Test
    public void testProcessPositiveNegativeOptionLiteralsNoLiterals() throws Exception
    {
        final List<QueryLiteral> literals = CollectionBuilder.<QueryLiteral>newBuilder().asList();
        final List<QueryLiteral> positiveLiterals = new ArrayList<QueryLiteral>();
        final List<QueryLiteral> negativeLiterals = new ArrayList<QueryLiteral>();

        final JqlCascadingSelectLiteralUtil util = new JqlCascadingSelectLiteralUtil(jqlSelectOptionsUtil);

        mockController.replay();

        util.processPositiveNegativeOptionLiterals(literals, positiveLiterals, negativeLiterals);

        assertTrue(positiveLiterals.isEmpty());
        assertTrue(negativeLiterals.isEmpty());

        mockController.verify();
    }

    @Test
    public void testProcessPositiveNegativeOptionLiterals() throws Exception
    {
        final List<QueryLiteral> literals = CollectionBuilder.<QueryLiteral>newBuilder(new QueryLiteral(), createLiteral("500"), createLiteral(500L), createLiteral(-500L), createLiteral(-600L)).asList();
        final List<QueryLiteral> positiveLiterals = new ArrayList<QueryLiteral>();
        final List<QueryLiteral> negativeLiterals = new ArrayList<QueryLiteral>();

        jqlSelectOptionsUtil.getOptionById(500L);
        mockController.setReturnValue(new MockOption(null, null, null, null, null, 500L));

        jqlSelectOptionsUtil.getOptionById(600L);
        mockController.setReturnValue(null);

        final List<QueryLiteral> expectedPositiveLiterals = CollectionBuilder.<QueryLiteral>newBuilder(new QueryLiteral(), createLiteral("500"), createLiteral(500L), createLiteral(-600L)).asList();
        final List<QueryLiteral> expectedNegativeLiterals = CollectionBuilder.<QueryLiteral>newBuilder(createLiteral(500L)).asList();

        final JqlCascadingSelectLiteralUtil util = new JqlCascadingSelectLiteralUtil(jqlSelectOptionsUtil);

        mockController.replay();

        util.processPositiveNegativeOptionLiterals(literals, positiveLiterals, negativeLiterals);

        assertEquals(expectedPositiveLiterals, positiveLiterals);
        assertEquals(expectedNegativeLiterals, negativeLiterals);

        mockController.verify();
    }

    @Test
    public void testIsNegativeLiteral() throws Exception
    {
        final QueryLiteral positiveLiteral1 = createLiteral(500L);
        final QueryLiteral positiveLiteral2 = createLiteral("abc");
        final QueryLiteral negativeLiteral = createLiteral(-500L);
        final QueryLiteral emptyLiteral = new QueryLiteral();

        jqlSelectOptionsUtil.getOptionById(500L);
        mockController.setReturnValue(new MockOption(null, null, null, null, null, 500L));

        final JqlCascadingSelectLiteralUtil util = new JqlCascadingSelectLiteralUtil(jqlSelectOptionsUtil);

        mockController.replay();

        assertFalse(util.isNegativeLiteral(positiveLiteral1));
        assertFalse(util.isNegativeLiteral(positiveLiteral2));
        assertTrue(util.isNegativeLiteral(negativeLiteral));
        assertFalse(util.isNegativeLiteral(emptyLiteral));

        mockController.verify();
    }
}
