package com.atlassian.jira.issue.search.searchers.transformer;

import java.util.List;
import java.util.TimeZone;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.jql.builder.JqlClauseBuilderFactory;
import com.atlassian.jira.jql.builder.JqlClauseBuilderFactoryImpl;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.jql.util.JqlDateSupportImpl;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.Query;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;



public class TestCustomFieldLabelsSearchInputTransformer
{
    private User user = new MockUser("admin");


    @Before
    public void setUp() throws Exception
    {

        final TimeZoneManager timeZoneManager = mock(TimeZoneManager.class);
        when(timeZoneManager.getLoggedInUserTimeZone()).thenReturn(TimeZone.getDefault());
        MockComponentWorker componentWorker = new MockComponentWorker();
        componentWorker.registerMock(TimeZoneManager.class, timeZoneManager);
        componentWorker.registerMock(JqlClauseBuilderFactory.class, new JqlClauseBuilderFactoryImpl(new JqlDateSupportImpl(timeZoneManager)));
        ComponentAccessor.initialiseWorker(componentWorker);
    }


    @Test
    public void testGetClauseFromParams()
    {
        final String clauseName = "testName";
        final CustomFieldInputHelper mockCustomFieldInputHelper = mock(CustomFieldInputHelper.class);
        ClauseNames clauseNames = new ClauseNames("cf[10000]");

        CustomFieldLabelsSearchInputTransformer inputTransformer = new CustomFieldLabelsSearchInputTransformer(null, null, mockCustomFieldInputHelper, clauseNames)
        {
            @Override
            protected String getClauseName(final User user, final ClauseNames clauseNames)
            {
                return clauseName;
            }
        };

        CustomFieldParams params = new CustomFieldParamsImpl(null, null);

        TerminalClause clause = (TerminalClause) inputTransformer.getClauseFromParams(user, params);
        assertNull(clause);

        params.put(null, CollectionBuilder.newBuilder("dUde").asList());
        clause = (TerminalClause) inputTransformer.getClauseFromParams(user, params);
        assertNotNull(clause);
        assertEquals("testName", clause.getName());
        assertEquals(Operator.EQUALS, clause.getOperator());
        assertEquals("dUde", ((SingleValueOperand)clause.getOperand()).getStringValue());

        params.put(null, CollectionBuilder.newBuilder("dUde multi VALUE").asList());
        clause = (TerminalClause) inputTransformer.getClauseFromParams(user, params);
        assertNotNull(clause);
        assertEquals("testName", clause.getName());
        assertEquals(Operator.IN, clause.getOperator());
        final List<Operand> operators = ((MultiValueOperand) clause.getOperand()).getValues();
        assertTrue(operators.contains(new SingleValueOperand("dUde")));
        assertTrue(operators.contains(new SingleValueOperand("multi")));
        assertTrue(operators.contains(new SingleValueOperand("VALUE")));
        assertEquals(3, operators.size());

    }

    @Test
    public void testGetParamsFromSR()
    {
        final CustomFieldInputHelper mockCustomFieldInputHelper = mock(CustomFieldInputHelper.class);
        ClauseNames clauseNames = new ClauseNames("cf[10000]");

        CustomFieldLabelsSearchInputTransformer inputTransformer = new CustomFieldLabelsSearchInputTransformer(null, null, mockCustomFieldInputHelper, clauseNames);

        Query query = JqlQueryBuilder.newClauseBuilder().customField(10000L).eq("boO").buildQuery();
        CustomFieldParams params = inputTransformer.getParamsFromSearchRequest(user, query, null);
        assertEquals(CollectionBuilder.list("boO"), params.getValuesForNullKey());

        query = JqlQueryBuilder.newClauseBuilder().customField(10000L).is().empty().buildQuery();
        params = inputTransformer.getParamsFromSearchRequest(user, query, null);
        assertNull(params);

        query = JqlQueryBuilder.newClauseBuilder().customField(10000L).in().strings("DUDE", "boO", "Moo").buildQuery();
        params = inputTransformer.getParamsFromSearchRequest(user, query, null);
        assertEquals(CollectionBuilder.list("DUDE", "boO", "Moo"), params.getValuesForNullKey());

        // Numeric values can be given with or without quotes.
        query = JqlQueryBuilder.newClauseBuilder().customField(10000L).in().operands(new SingleValueOperand("123"), new SingleValueOperand(456L)).buildQuery();
        params = inputTransformer.getParamsFromSearchRequest(user, query, null);
        assertEquals(CollectionBuilder.list("123", "456"), params.getValuesForNullKey());

    }
}
