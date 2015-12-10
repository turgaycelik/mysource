package com.atlassian.jira.plugin.jql.function;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.atlassian.crowd.model.user.User;
import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.action.issue.customfields.option.MockOption;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.impl.CascadingSelectCFType;
import com.atlassian.jira.issue.customfields.impl.TextCFType;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryCreationContextImpl;
import com.atlassian.jira.jql.util.JqlCascadingSelectLiteralUtil;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operator.Operator;

import com.google.common.collect.ImmutableList;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static com.atlassian.jira.util.MessageSetAssert.assert1ErrorNoWarnings;
import static com.atlassian.jira.util.MessageSetAssert.assertNoMessages;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
public class TestCascadeOptionFunction
{
    private static final String CLAUSE_NAME = "cascade";
    private static final String FUNCTION_NAME = "CascadeOption";
    private static final String CUSTOM_FIELD_ID = "1000";

    @Mock private CustomFieldManager customFieldManager;
    @Mock private SearchHandlerManager searchHandlerManager;
    @Mock private JqlSelectOptionsUtil jqlSelectOptionsUtil;
    @Mock private CustomField customField;
    @Mock private JqlCascadingSelectLiteralUtil jqlCascadingSelectLiteralUtil;

    private QueryCreationContext queryCreationContext;
    private User theUser = null;

    @Before
    public void setUp() throws Exception
    {
        customFieldManager = mock(CustomFieldManager.class);
        searchHandlerManager = mock(SearchHandlerManager.class);
        jqlSelectOptionsUtil = mock(JqlSelectOptionsUtil.class);
        customField = mock(CustomField.class);
        jqlCascadingSelectLiteralUtil = mock(JqlCascadingSelectLiteralUtil.class);
        queryCreationContext = new QueryCreationContextImpl(theUser);
    }

    @After
    public void tearDown() throws Exception
    {
        customFieldManager = null;
        searchHandlerManager = null;
        jqlSelectOptionsUtil = null;
        customField = null;
        jqlCascadingSelectLiteralUtil = null;
        queryCreationContext = null;
    }

    @Test
    public void testValidateNoFields() throws Exception
    {
        final FunctionOperand functionOperand = new FunctionOperand(FUNCTION_NAME, "parent", "child");
        final TerminalClause clause = createClause(functionOperand);
        when(searchHandlerManager.getFieldIds(theUser, CLAUSE_NAME)).thenReturn(ImmutableList.<String>of());

        final CascadeOptionFunction function = createFunction(null, null);
        final MessageSet result = function.validate(theUser, functionOperand, clause);

        assertNotCascadeField(result);
    }

    @Test
    public void testValidateWrongCustomFieldType() throws Exception
    {
        final FunctionOperand functionOperand = new FunctionOperand(FUNCTION_NAME, "parent", "child");
        final TerminalClause clause = createClause(functionOperand);
        expectClauseResolvesToCustomField(TextCFType.class);

        final CascadeOptionFunction function = createFunction(null, null);
        final MessageSet result = function.validate(theUser, functionOperand, clause);

        assertNotCascadeField(result);
    }

    @Test
    public void testValidateWrongArgsSize() throws Exception
    {
        final FunctionOperand functionOperand = new FunctionOperand(FUNCTION_NAME, "parent", "child", "blarg");
        final TerminalClause clause = createClause(functionOperand);
        expectClauseResolvesToCustomField();

        final CascadeOptionFunction function = createFunction(null, null);
        final MessageSet result = function.validate(theUser, functionOperand, clause);

        assertIncorrectArgs(result);
    }

    @Test
    public void testValidateEmptyArgs() throws Exception
    {
        final FunctionOperand functionOperand = new FunctionOperand(FUNCTION_NAME);
        final TerminalClause clause = createClause(functionOperand);
        expectClauseResolvesToCustomField();

        final CascadeOptionFunction function = createFunction(null, null);
        final MessageSet result = function.validate(theUser, functionOperand, clause);

        assertIncorrectArgs(result);
    }

    @Test
    public void testValidateParentArgIsNotParent() throws Exception
    {
        final FunctionOperand functionOperand = new FunctionOperand(FUNCTION_NAME, "child", "parent");
        final TerminalClause clause = createClause(functionOperand);
        expectClauseResolvesToCustomField();

        final MockOption parentOption = createParentOption(100L);
        final MockOption childOption = createChildOption(parentOption, 200L);
        parentOption.setChildOptions(CollectionBuilder.newBuilder(childOption).asList());
        mockLiteralOptions("child", childOption);

        final CascadeOptionFunction function = createFunction(null, null);
        final MessageSet result = function.validate(theUser, functionOperand, clause);

        assertNotParent(result, "child");
    }

    @Test
    public void testValidateSingleParentAndChildHappyPath() throws Exception
    {
        final FunctionOperand functionOperand = new FunctionOperand(FUNCTION_NAME, "parent", "child");
        final TerminalClause clause = createClause(functionOperand);
        expectClauseResolvesToCustomField();

        final MockOption parentOption = createParentOption(100L);
        final MockOption childOption = createChildOption(parentOption, 200L);
        parentOption.setChildOptions(CollectionBuilder.newBuilder(childOption).asList());
        mockLiteralOptions("parent", parentOption);
        mockLiteralOptions("child", childOption);

        final CascadeOptionFunction function = createFunction(null, null);
        final MessageSet result = function.validate(theUser, functionOperand, clause);

        assertNoMessages(result);
    }

    @Test
    public void testValidateSingleParentAndNoneChild() throws Exception
    {
        FunctionOperand functionOperand = new FunctionOperand(FUNCTION_NAME, "parent", "none");
        TerminalClause clause = createClause(functionOperand);

        expectClauseResolvesToCustomField();

        final MockOption parentOption = createParentOption(100L);
        final MockOption childOption = createChildOption(parentOption, 200L);
        parentOption.setChildOptions(CollectionBuilder.newBuilder(childOption).asList());
        mockLiteralOptions("parent", parentOption);

        final CascadeOptionFunction function = createFunction(null, null);

        final MessageSet result = function.validate(theUser, functionOperand, clause);
        assertNoMessages(result);
    }

    @Test
    public void testValidateNoneAsParentWithChild() throws Exception
    {
        final FunctionOperand functionOperand = new FunctionOperand(FUNCTION_NAME, "none", "child");
        final TerminalClause clause = createClause(functionOperand);
        expectClauseResolvesToCustomField();

        final CascadeOptionFunction function = createFunction(null, null);
        final MessageSet result = function.validate(theUser, functionOperand, clause);
        assertNotParent(result, "none");
    }

    @Test
    public void testValidateNoneAsParentOnItsOwn() throws Exception
    {
        final FunctionOperand functionOperand = new FunctionOperand(FUNCTION_NAME, "none");
        final TerminalClause clause = createClause(functionOperand);
        expectClauseResolvesToCustomField();

        final CascadeOptionFunction function = createFunction(null, null);
        final MessageSet result = function.validate(theUser, functionOperand, clause);
        assertNoMessages(result);
    }

    @Test
    public void testValidateNoneValueParent() throws Exception
    {
        final FunctionOperand functionOperand = new FunctionOperand(FUNCTION_NAME, "\"none\"");
        final TerminalClause clause = createClause(functionOperand);
        expectClauseResolvesToCustomField();

        final MockOption parentOption = createParentOption(100L);
        mockLiteralOptions("none", parentOption);

        final CascadeOptionFunction function = createFunction(null, null);
        final MessageSet result = function.validate(theUser, functionOperand, clause);
        assertNoMessages(result);
    }

    @Test
    public void testValidateSingleParentAndNoneValueChild() throws Exception
    {
        final FunctionOperand functionOperand = new FunctionOperand(FUNCTION_NAME, "parent", "\"none\"");
        final TerminalClause clause = createClause(functionOperand);
        expectClauseResolvesToCustomField();

        final MockOption parentOption = createParentOption(100L);
        final MockOption childOption = createChildOption(parentOption, 200L);
        parentOption.setChildOptions(CollectionBuilder.newBuilder(childOption).asList());
        mockLiteralOptions("parent", parentOption);
        mockLiteralOptions("none", childOption);

        final CascadeOptionFunction function = createFunction(null, null);
        final MessageSet result = function.validate(theUser, functionOperand, clause);
        assertNoMessages(result);
    }

    @Test
    public void testValidateParentAndChildButChildIsNotChildOfParent() throws Exception
    {
        final FunctionOperand functionOperand = new FunctionOperand(FUNCTION_NAME, "parent", "child");
        final TerminalClause clause = createClause(functionOperand);
        expectClauseResolvesToCustomField();

        final MockOption parentOption = createParentOption(100L);
        final MockOption childOption = createChildOption(parentOption, 200L);
        mockLiteralOptions("parent", parentOption);
        mockLiteralOptions("child", childOption);

        final CascadeOptionFunction function = createFunction(null, null);
        final MessageSet result = function.validate(theUser, functionOperand, clause);

        assert1ErrorNoWarnings(result, "jira.jql.function.cascade.option.parent.children.doesnt.match [child] [parent] [" + FUNCTION_NAME + ']');
    }

    @Test
    public void testValidateSingleParentHappyPath() throws Exception
    {
        final FunctionOperand functionOperand = new FunctionOperand(FUNCTION_NAME, "parent");
        final TerminalClause clause = createClause(functionOperand);
        expectClauseResolvesToCustomField();

        final MockOption parentOption = createParentOption(100L);
        mockLiteralOptions("parent", parentOption);

        final CascadeOptionFunction function = createFunction(null, null);
        final MessageSet result = function.validate(theUser, functionOperand, clause);
        assertNoMessages(result);
    }

    @Test
    public void testGetValuesSingleParentAndChildHappyPath() throws Exception
    {
        final FunctionOperand functionOperand = new FunctionOperand(FUNCTION_NAME, "parent", "child");
        final TerminalClause clause = createClause(functionOperand);
        expectClauseResolvesToCustomField();

        final MockOption parentOption = createParentOption(100L);
        final MockOption childOption = createChildOption(parentOption, 200L);
        parentOption.setChildOptions(Collections.<Option>singletonList(childOption));
        mockLiteralOptions("parent", parentOption);
        mockLiteralOptions("child", childOption);

        final CascadeOptionFunction function = createFunction(Collections.<Option>singleton(childOption), Collections.<Option>emptySet());
        final List<QueryLiteral> values = function.getValues(queryCreationContext, functionOperand, clause);
        assertEmptyList(values);
    }

    @Test
    public void testGetValuesSingleParentHappyPath() throws Exception
    {
        final FunctionOperand functionOperand = new FunctionOperand(FUNCTION_NAME, "parent");
        final TerminalClause clause = createClause(functionOperand);
        expectClauseResolvesToCustomField();

        final MockOption parentOption = createParentOption(100L);
        mockLiteralOptions("parent", parentOption);

        final CascadeOptionFunction function = createFunction(Collections.<Option>singleton(parentOption), Collections.<Option>emptySet());
        final List<QueryLiteral> values = function.getValues(queryCreationContext, functionOperand, clause);
        assertEmptyList(values);
    }

    @Test
    public void testGetValuesNoneParentHappyPath() throws Exception
    {
        final FunctionOperand functionOperand = new FunctionOperand(FUNCTION_NAME, "none");
        final TerminalClause clause = createClause(functionOperand);
        expectClauseResolvesToCustomField();

        final CascadeOptionFunction function = createFunction(null, null);
        final List<QueryLiteral> result = function.getValues(queryCreationContext, functionOperand, clause);
        assertThat(result, contains(new QueryLiteral()));
    }

    @Test
    public void testGetValuesNoneParentWithChild() throws Exception
    {
        final FunctionOperand functionOperand = new FunctionOperand(FUNCTION_NAME, "none", "child");
        final TerminalClause clause = createClause(functionOperand);
        expectClauseResolvesToCustomField();

        final CascadeOptionFunction function = createFunction(null, null);
        final List<QueryLiteral> result = function.getValues(queryCreationContext, functionOperand, clause);
        assertEmptyList(result);
    }

    @Test
    public void testGetValuesSingleParentOverrideSecurity() throws Exception
    {
        queryCreationContext = new QueryCreationContextImpl(theUser, true);
        final FunctionOperand functionOperand = new FunctionOperand(FUNCTION_NAME, "parent");
        final TerminalClause clause = createClause(functionOperand);

        final CustomFieldType<Map<String,Option>,Option> selectCFType = mock(CascadingSelectCFType.class);
        when(customField.getCustomFieldType()).thenReturn(selectCFType);
        when(searchHandlerManager.getFieldIds(CLAUSE_NAME)).thenReturn(ImmutableList.of(CUSTOM_FIELD_ID));
        when(customFieldManager.getCustomFieldObject(CUSTOM_FIELD_ID)).thenReturn(customField);

        final MockOption parentOption = createParentOption(100L);
        mockLiteralOptions("parent", parentOption);

        final CascadeOptionFunction function = createFunction(Collections.<Option>singleton(parentOption), Collections.<Option>emptySet());
        final List<QueryLiteral> result = function.getValues(queryCreationContext, functionOperand, clause);
        assertEmptyList(result);
    }

    @Test
    public void testGetValuesBadArgs() throws Exception
    {
        final FunctionOperand functionOperand = new FunctionOperand(FUNCTION_NAME, "parent", "child", "blah");
        final TerminalClause clause = createClause(functionOperand);
        expectClauseResolvesToCustomField();

        final CascadeOptionFunction function = createFunction(null, null);
        final List<QueryLiteral> result = function.getValues(queryCreationContext, functionOperand, clause);
        assertEmptyList(result);
    }

    @Test
    public void testGetValuesNoArgs() throws Exception
    {
        final FunctionOperand functionOperand = new FunctionOperand(FUNCTION_NAME);
        final TerminalClause clause = createClause(functionOperand);
        expectClauseResolvesToCustomField();

        final CascadeOptionFunction function = createFunction(null, null);
        final List<QueryLiteral> result = function.getValues(queryCreationContext, functionOperand, clause);
        assertEmptyList(result);
    }

    @Test
    public void testGetValuesNoFields() throws Exception
    {
        final FunctionOperand functionOperand = new FunctionOperand(FUNCTION_NAME, "parent", "child");
        final TerminalClause clause = createClause(functionOperand);
        expectClauseResolvesToCustomField(TextCFType.class);

        final CascadeOptionFunction function = createFunction(null, null);
        final List<QueryLiteral> result = function.getValues(queryCreationContext, functionOperand, clause);
        assertEmptyList(result);
    }

    @Test
    public void testGetValuesSingleParentAndNoneChild() throws Exception
    {
        final FunctionOperand functionOperand = new FunctionOperand(FUNCTION_NAME, "parent", "none");
        final TerminalClause clause = createClause(functionOperand);
        expectClauseResolvesToCustomField();

        final MockOption parentOption = createParentOption(100L);
        final MockOption childOption = createChildOption(parentOption, 200L);
        parentOption.setChildOptions(Collections.<Option>singletonList(childOption));
        mockLiteralOptions("parent", parentOption);

        final CascadeOptionFunction function = createFunction(Collections.<Option>singleton(parentOption), Collections.<Option>singleton(childOption));
        final List<QueryLiteral> result = function.getValues(queryCreationContext, functionOperand, clause);
        assertEmptyList(result);
    }

    @Test
    public void testGetValuesSingleParentAndNoneValueChild() throws Exception
    {
        final FunctionOperand functionOperand = new FunctionOperand(FUNCTION_NAME, "parent", "\"none\"");
        final TerminalClause clause = createClause(functionOperand);
        expectClauseResolvesToCustomField();

        final MockOption parentOption = createParentOption(100L);
        final MockOption childOption = createChildOption(parentOption, 200L);
        parentOption.setChildOptions(Collections.<Option>singletonList(childOption));
        mockLiteralOptions("parent", parentOption);
        mockLiteralOptions("none", childOption);

        final CascadeOptionFunction function = createFunction(Collections.<Option>singleton(childOption), Collections.<Option>emptySet());
        final List<QueryLiteral> result = function.getValues(queryCreationContext, functionOperand, clause);
        assertEmptyList(result);
    }

    @Test
    public void testGetValuesNoneValueParent() throws Exception
    {
        final FunctionOperand functionOperand = new FunctionOperand(FUNCTION_NAME, "\"none\"");
        final TerminalClause clause = createClause(functionOperand);
        expectClauseResolvesToCustomField();

        final MockOption parentOption = createParentOption(100L);
        mockLiteralOptions("none", parentOption);

        final CascadeOptionFunction function = createFunction(Collections.<Option>singleton(parentOption), Collections.<Option>emptySet());
        final List<QueryLiteral> result = function.getValues(queryCreationContext, functionOperand, clause);
        assertEmptyList(result);
    }

    @Test
    public void testGetMinimumNumberOfExpectedArguments() throws Exception
    {
        final CascadeOptionFunction function = createFunction(null, null);
        assertEquals(1, function.getMinimumNumberOfExpectedArguments());
    }

    @Test
    public void testDataType() throws Exception
    {
        final CascadeOptionFunction function = createFunction(null, null);
        assertEquals(JiraDataTypes.CASCADING_OPTION, function.getDataType());
    }

    private <T,S,R extends CustomFieldType<T,S>> void expectClauseResolvesToCustomField(Class<R> type)
    {
        final CustomFieldType<T,S> selectCFType = mock(type);
        when(customField.getCustomFieldType()).thenReturn(selectCFType);
        when(searchHandlerManager.getFieldIds(theUser, CLAUSE_NAME)).thenReturn(ImmutableList.of(CUSTOM_FIELD_ID));
        when(customFieldManager.getCustomFieldObject(CUSTOM_FIELD_ID)).thenReturn(customField);
    }

    private void expectClauseResolvesToCustomField()
    {
        expectClauseResolvesToCustomField(CascadingSelectCFType.class);
    }

    private CascadeOptionFunction createFunction(final Collection<Option> expectedPositive, final Collection<Option> expectedNegative)
    {
        return new CascadeOptionFunction(jqlSelectOptionsUtil, searchHandlerManager, customFieldManager, jqlCascadingSelectLiteralUtil)
        {
            @Override
            protected I18nHelper getI18n()
            {
                return new MockI18nHelper();
            }

            @Override
            public String getFunctionName()
            {
                return FUNCTION_NAME;
            }

            @Override
            public JiraDataType getDataType()
            {
                return JiraDataTypes.CASCADING_OPTION;
            }

            @Override
            List<QueryLiteral> createLiterals(final Operand operand, final Collection<Option> positiveOptions, final Collection<Option> negativeOptions)
            {
                if (expectedPositive != null)
                {
                    assertEquals(expectedPositive, positiveOptions);
                }

                if (expectedNegative != null)
                {
                    assertEquals(expectedNegative, negativeOptions);
                }

                return Collections.emptyList();
            }
        };
    }

    private static MockOption createParentOption(final long optionId)
    {
        return new MockOption(null, Collections.emptyList(), null, null, null, optionId);
    }

    private static MockOption createChildOption(final MockOption parentOption, final long optionId)
    {
        return new MockOption(parentOption, null, null, null, null, optionId);
    }

    private static TerminalClauseImpl createClause(final FunctionOperand functionOperand)
    {
        return new TerminalClauseImpl(CLAUSE_NAME, Operator.IN, functionOperand);
    }

    private void mockLiteralOptions(String name, Option... options)
    {
       when(jqlSelectOptionsUtil.getOptions(customField, createLiteral(name), true))
            .thenReturn(ImmutableList.copyOf(options));
    }

    private static void assertNotCascadeField(final MessageSet result)
    {
        assert1ErrorNoWarnings(result, "jira.jql.function.cascade.option.not.cascade.field [" + CLAUSE_NAME + "] [" + FUNCTION_NAME + ']');
    }

    private static void assertIncorrectArgs(final MessageSet result)
    {
        assert1ErrorNoWarnings(result, "jira.jql.function.cascade.option.incorrect.args [" + FUNCTION_NAME + ']');
    }

    private static void assertNotParent(final MessageSet result, final String which)
    {
        assert1ErrorNoWarnings(result, "jira.jql.function.cascade.option.not.parent [" + FUNCTION_NAME + "] [" + which + ']');
    }

    private static <T> void assertEmptyList(final List<T> list)
    {
        assertThat(list, Matchers.<T>empty());
    }
}
