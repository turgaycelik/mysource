package com.atlassian.jira.issue.search.searchers.transformer;

import java.util.Arrays;
import java.util.Set;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.transport.ActionParams;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.issue.transport.impl.ActionParamsImpl;
import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import com.google.common.collect.ImmutableMap;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TestLabelsSearchInputTransformer
{
    @Test
    public void testPopulateFromParams()
    {
        final User user = new MockUser("admin");
        final FieldValuesHolder fieldValuesHolder = new FieldValuesHolderImpl();
        final String[] labels = { "label1", "label2", "label3" };
        final ActionParams params = new ActionParamsImpl(ImmutableMap.of("labels", labels));
        final LabelsSearchInputTransformer transformer = new LabelsSearchInputTransformer(null, null, null);
        transformer.populateFromParams(user, fieldValuesHolder, params);
        assertEquals(Arrays.asList(labels), fieldValuesHolder.get("labels"));
    }

    @Test
    public void testPopulateFromParamsEmpty()
    {
        final User user = new MockUser("admin");
        final FieldValuesHolder fieldValuesHolder = new FieldValuesHolderImpl();
        final ActionParams params = new ActionParamsImpl();
        final LabelsSearchInputTransformer transformer = new LabelsSearchInputTransformer(null, null, null);
        transformer.populateFromParams(user, fieldValuesHolder, params);
        assertNull(fieldValuesHolder.get("labels"));
    }

    @Test
    public void testValidateParams()
    {
        final User user = new MockUser("admin");

        final LabelsSearchInputTransformer transformer = new LabelsSearchInputTransformer(null, null, null);

        final FieldValuesHolder fieldValuesHolder = new FieldValuesHolderImpl();
        fieldValuesHolder.put("labels", CollectionBuilder.newBuilder("label1", "label2", "label3").asList());
        final ErrorCollection errors = new SimpleErrorCollection();
        transformer.validateParams(user, null, fieldValuesHolder, new MockI18nBean(), errors);

        assertFalse(errors.hasAnyErrors());
    }

    @Test
    public void testValidateParamsLabelTooLong()
    {
        final User user = new MockUser("admin");

        final LabelsSearchInputTransformer transformer = new LabelsSearchInputTransformer(null, null, null);

        final FieldValuesHolder fieldValuesHolder = new FieldValuesHolderImpl();
        fieldValuesHolder.put("labels", CollectionBuilder.newBuilder("label1", "reallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelreallylonglabelr", "label3").asList());
        final ErrorCollection errors = new SimpleErrorCollection();
        transformer.validateParams(user, null, fieldValuesHolder, new MockI18nBean(), errors);

        assertTrue(errors.hasAnyErrors());
    }  

    @Test
    public void testGetSearchClauseEmpty()
    {
        final User user = new MockUser("admin");

        final LabelsSearchInputTransformer transformer = new LabelsSearchInputTransformer(null, null, null);

        final FieldValuesHolder fieldValuesHolder = new FieldValuesHolderImpl();
        final Clause searchClause = transformer.getSearchClause(user, fieldValuesHolder);
        assertNull(searchClause);
    }

    @Test
    public void testGetSearchClauseMulti()
    {
        final User user = new MockUser("admin");

        final LabelsSearchInputTransformer transformer = new LabelsSearchInputTransformer(null, null, null);

        final FieldValuesHolder fieldValuesHolder = new FieldValuesHolderImpl();
        fieldValuesHolder.put("labels", CollectionBuilder.newBuilder("label1", "label2", "label3").asList());
        final TerminalClause searchClause = (TerminalClause) transformer.getSearchClause(user, fieldValuesHolder);
        assertEquals(Operator.IN, searchClause.getOperator());
        assertEquals(new MultiValueOperand("label1", "label2", "label3"), searchClause.getOperand());
    }

    @Test
    public void testGetSearchClauseSingle()
    {
        final User user = new MockUser("admin");

        final LabelsSearchInputTransformer transformer = new LabelsSearchInputTransformer(null, null, null);

        final FieldValuesHolder fieldValuesHolder = new FieldValuesHolderImpl();
        fieldValuesHolder.put("labels", CollectionBuilder.newBuilder("label1").asList());
        final TerminalClause searchClause = (TerminalClause) transformer.getSearchClause(user, fieldValuesHolder);
        assertEquals(Operator.EQUALS, searchClause.getOperator());
        assertEquals(new SingleValueOperand("label1"), searchClause.getOperand());
    }

    @Test
    public void testPopulateFromQuery()
    {
        ComponentAccessor.initialiseWorker(new MockComponentWorker());
        final User user = new MockUser("admin");

        final LabelsSearchInputTransformer transformer = new LabelsSearchInputTransformer(null, null, null)
        {
            @Override
            Set<String> getNavigatorValuesAsStrings(final User searcher, final Query query, final SearchContext searchContext)
            {
                return CollectionBuilder.newBuilder("label1", "label2", "label3").asListOrderedSet();
            }
        };

        final FieldValuesHolder fieldValuesHolder = new FieldValuesHolderImpl();
        final Query query = JqlQueryBuilder.newClauseBuilder().labels("label1", "label2", "label3").buildQuery();
        transformer.populateFromQuery(user, fieldValuesHolder, query, null);

        assertEquals(CollectionBuilder.<String>newBuilder("label1", "label2", "label3").asList(), fieldValuesHolder.get("labels"));
    }
}
