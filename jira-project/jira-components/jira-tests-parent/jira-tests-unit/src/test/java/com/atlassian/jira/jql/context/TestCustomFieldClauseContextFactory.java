package com.atlassian.jira.jql.context;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestCustomFieldClauseContextFactory
{
    @Mock private CustomField customField;
    @Mock private FieldConfigSchemeClauseContextUtil fieldConfigSchemeClauseContextUtil;
    @Mock private ContextSetUtil contextSetUtil;

    @After
    public void tearDown() throws Exception
    {
        customField = null;
        fieldConfigSchemeClauseContextUtil = null;
        contextSetUtil = null;
    }

    @Test
    public void testGetClauseContextNoGlobalContextSeen() throws Exception
    {
        final FieldConfigScheme fieldConfigScheme1 = mock(FieldConfigScheme.class);
        final FieldConfigScheme fieldConfigScheme2 = mock(FieldConfigScheme.class);
        final ClauseContext context2 = createForProjects(37);
        final ClauseContext context4 = createForProjects(37383);
        final ClauseContext context5 = createForProjects(37394782, 3483);

        when(customField.getConfigurationSchemes()).thenReturn(asList(fieldConfigScheme1, fieldConfigScheme2));
        when(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(null, fieldConfigScheme1)).thenReturn(context2);
        when(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(null, fieldConfigScheme2)).thenReturn(context4);
        when(contextSetUtil.union(CollectionBuilder.newBuilder(context2, context4).asSet())).thenReturn(context5);

        final CustomFieldClauseContextFactory clauseContextFactory = new CustomFieldClauseContextFactory(customField, fieldConfigSchemeClauseContextUtil, contextSetUtil);
        final ClauseContext result = clauseContextFactory.getClauseContext(null, new TerminalClauseImpl("blah", Operator.LESS_THAN_EQUALS, "blah"));
        assertSame(context5, result);
    }

    @Test
    public void testGetClauseContextGlobalContextSeen() throws Exception
    {
        final FieldConfigScheme fieldConfigScheme1 = mock(FieldConfigScheme.class);
        final FieldConfigScheme fieldConfigScheme2 = mock(FieldConfigScheme.class);
        final ClauseContext context2 = createForProjects(37);
        final ClauseContext context4 = ClauseContextImpl.createGlobalClauseContext();

        when(customField.getConfigurationSchemes()).thenReturn(asList(fieldConfigScheme1, fieldConfigScheme2));
        when(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(null, fieldConfigScheme1)).thenReturn(context2);
        when(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(null, fieldConfigScheme2)).thenReturn(context4);

        final CustomFieldClauseContextFactory clauseContextFactory = new CustomFieldClauseContextFactory(customField, fieldConfigSchemeClauseContextUtil, contextSetUtil);
        final ClauseContext result = clauseContextFactory.getClauseContext(null, new TerminalClauseImpl("blah", Operator.LESS_THAN_EQUALS, "blah"));
        assertSame(ClauseContextImpl.createGlobalClauseContext(), result);
    }

    @Test
    public void testGetClauseContextNoFieldSchemes() throws Exception
    {
        when(customField.getConfigurationSchemes()).thenReturn(Collections.<FieldConfigScheme>emptyList());
        final CustomFieldClauseContextFactory clauseContextFactory = new CustomFieldClauseContextFactory(customField, fieldConfigSchemeClauseContextUtil, contextSetUtil);
        final ClauseContext result = clauseContextFactory.getClauseContext(null, new TerminalClauseImpl("blah", Operator.LESS_THAN_EQUALS, "blah"));
        assertEquals(ClauseContextImpl.createGlobalClauseContext(), result);
    }

    @Test
    public void testGetClauseContextOneContext() throws Exception
    {
        final ClauseContext context2 = createForProjects(373883);
        final FieldConfigScheme fieldConfigScheme1 = mock(FieldConfigScheme.class);

        when(customField.getConfigurationSchemes()).thenReturn(Collections.<FieldConfigScheme>singletonList(fieldConfigScheme1));
        when(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(null, fieldConfigScheme1)).thenReturn(context2);

        final CustomFieldClauseContextFactory clauseContextFactory = new CustomFieldClauseContextFactory(customField, fieldConfigSchemeClauseContextUtil, contextSetUtil);
        final ClauseContext result = clauseContextFactory.getClauseContext(null, new TerminalClauseImpl("blah", Operator.LESS_THAN_EQUALS, "blah"));
        assertEquals(context2, result);
    }

    @Test
    public void testGetClauseContextEmptyContext() throws Exception
    {
        final ClauseContext context2 = new ClauseContextImpl(Collections.<ProjectIssueTypeContext>emptySet());
        final FieldConfigScheme fieldConfigScheme1 = mock(FieldConfigScheme.class);

        when(customField.getConfigurationSchemes()).thenReturn(Collections.<FieldConfigScheme>singletonList(fieldConfigScheme1));
        when(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(null, fieldConfigScheme1)).thenReturn(context2);

        final CustomFieldClauseContextFactory clauseContextFactory = new CustomFieldClauseContextFactory(customField, fieldConfigSchemeClauseContextUtil, contextSetUtil);
        final ClauseContext result = clauseContextFactory.getClauseContext(null, new TerminalClauseImpl("blah", Operator.LESS_THAN_EQUALS, "blah"));
        assertEquals(ClauseContextImpl.createGlobalClauseContext(), result);
    }

    private static ClauseContext createForProjects(long... ids)
    {
        Set<ProjectIssueTypeContext> ctxs = new HashSet<ProjectIssueTypeContext>();
        for (long id : ids)
        {
            ctxs.add(new ProjectIssueTypeContextImpl(new ProjectContextImpl(id), AllIssueTypesContext.getInstance()));
        }
        return new ClauseContextImpl(ctxs);
    }
}
