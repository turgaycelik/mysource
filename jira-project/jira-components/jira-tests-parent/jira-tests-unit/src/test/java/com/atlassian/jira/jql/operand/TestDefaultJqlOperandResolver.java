package com.atlassian.jira.jql.operand;

import java.util.Collections;
import java.util.List;

import com.atlassian.core.test.util.DuckTypeProxy;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.QueryCache;
import com.atlassian.jira.jql.operand.registry.JqlFunctionHandlerRegistry;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.validator.MockJqlFunctionHandlerRegistry;
import com.atlassian.jira.plugin.jql.function.ClauseSanitisingJqlFunction;
import com.atlassian.jira.plugin.jql.function.JqlFunction;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.util.NoopI18nFactory;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.OperandVisitor;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Test for {@link DefaultJqlOperandResolver}.
 *
 * @since v4.0
 */
@RunWith (MockitoJUnitRunner.class)
public class TestDefaultJqlOperandResolver 
{
    private String field = "blah";
    private User theUser = null;
    @Mock
    private QueryCreationContext queryCreationContext;
    @Mock
    private QueryCache queryCache;
    @Mock
    private  JqlFunctionHandlerRegistry jqlFunctionHandlerRegistry;
    @Mock
    private I18nHelper i18nHelper;

    @Test
    public void testConstructor() throws Exception
    {
        try
        {
            new DefaultJqlOperandResolver(null, new NoopI18nFactory(), queryCache);
            fail("Expected and exception");
        }
        catch (IllegalArgumentException ignored)
        {
        }

        try
        {
            new DefaultJqlOperandResolver(jqlFunctionHandlerRegistry, null, queryCache);
            fail("Expected exception");
        }
        catch (IllegalArgumentException expected) {}

        try
        {
            new DefaultJqlOperandResolver(jqlFunctionHandlerRegistry, new NoopI18nFactory(), null);
            fail("Expected exception");
        }
        catch (IllegalArgumentException stillExpected) {}
    }

    @Test
    public void testGetValuesEmptyOperand() throws Exception
    {
        final List<QueryLiteral> expectedValues = Collections.emptyList();
        TerminalClause clause = new TerminalClauseImpl(field, Operator.EQUALS, EmptyOperand.EMPTY);
        final OperandHandler<EmptyOperand> emptyHandler = mock(OperandHandler.class);
        when(emptyHandler.getValues(queryCreationContext, EmptyOperand.EMPTY, clause)).thenReturn(expectedValues);

        final DefaultJqlOperandResolver jqlOperandSupport = createResolver(null, emptyHandler, null, null);
        final List<QueryLiteral> list = jqlOperandSupport.getValues(queryCreationContext, EmptyOperand.EMPTY, clause);
        assertEquals(expectedValues, list);
    }

    @Test
    public void testGetValuesEmptyOperandUserInsteadOfContext() throws Exception
    {
        final List<QueryLiteral> expectedValues = Collections.emptyList();
        TerminalClause clause = new TerminalClauseImpl(field, Operator.EQUALS, EmptyOperand.EMPTY);
        final OperandHandler<EmptyOperand> emptyHandler = mock(OperandHandler.class);
        when(emptyHandler.getValues(queryCreationContext, EmptyOperand.EMPTY, clause)).thenReturn(expectedValues);

        final DefaultJqlOperandResolver jqlOperandSupport = createResolver(null, emptyHandler, null, null);
        final List<QueryLiteral> list = jqlOperandSupport.getValues(theUser, EmptyOperand.EMPTY, clause);
        assertEquals(expectedValues, list);
    }

    @Test
    public void testGetValuesSingleValueOperand() throws Exception
    {
        final List<QueryLiteral> expectedValues = Collections.emptyList();
        final SingleValueOperand operand = new SingleValueOperand("");
        TerminalClause clause = new TerminalClauseImpl(field, Operator.EQUALS, operand);

        final OperandHandler<SingleValueOperand> singleHandler = mock(OperandHandler.class);
        when(singleHandler.getValues(queryCreationContext, operand, clause)).thenReturn(expectedValues);

        final DefaultJqlOperandResolver jqlOperandSupport = createResolver(null, null, singleHandler, null);
        final List<QueryLiteral> list = jqlOperandSupport.getValues(queryCreationContext, operand, clause);
        assertEquals(expectedValues, list);
    }

    @Test
    public void testGetValuesMultiValueOperand() throws Exception
    {
        final List<QueryLiteral> expectedValues = Collections.emptyList();
        final MultiValueOperand operand = new MultiValueOperand("");
        TerminalClause clause = new TerminalClauseImpl(field, Operator.EQUALS, operand);

        final OperandHandler<MultiValueOperand> multiHandler = mock(OperandHandler.class);
        when(multiHandler.getValues(queryCreationContext, operand, clause)).thenReturn(expectedValues);

        final DefaultJqlOperandResolver jqlOperandSupport = createResolver(null, null, null, multiHandler);
        final List<QueryLiteral> list = jqlOperandSupport.getValues(queryCreationContext, operand, clause);
        assertEquals(expectedValues, list);
    }

    @Test
    public void testGetValuesRegisteredFunction() throws Exception
    {
        final List<QueryLiteral> expectedValues = Collections.emptyList();
        final FunctionOperand operand = new FunctionOperand("funcName");
        TerminalClause clause = new TerminalClauseImpl(field, Operator.EQUALS, operand);

        final FunctionOperandHandler funcHandler = mock(FunctionOperandHandler.class);
        when(funcHandler.getValues(queryCreationContext, operand, clause)).thenReturn(expectedValues);

        final JqlFunctionHandlerRegistry registry = mock(JqlFunctionHandlerRegistry.class);
        when(registry.getOperandHandler(operand)).thenReturn(funcHandler);

        final DefaultJqlOperandResolver jqlOperandSupport = createResolver(registry, null, null, null);
        final List<QueryLiteral> list = jqlOperandSupport.getValues(queryCreationContext, operand, clause);
        assertEquals(expectedValues, list);
    }

    @Test
    public void testGetValuesNonRegisteredFunction() throws Exception
    {
        final FunctionOperand operand = new FunctionOperand("funcName");
        TerminalClause clause = new TerminalClauseImpl(field, Operator.EQUALS, operand);

        final JqlFunctionHandlerRegistry registry = mock(JqlFunctionHandlerRegistry.class);
        when(registry.getOperandHandler(operand)).thenReturn(null);
        when(queryCache.getValues(queryCreationContext, operand, clause)).thenReturn(null);

        final DefaultJqlOperandResolver jqlOperandSupport = createResolver(registry, null, null, null);
        assertEquals(Collections.emptyList(), jqlOperandSupport.getValues(queryCreationContext, operand, clause));
    }

    @Test
    public void testGetValuesUnknownOperand() throws Exception
    {
        final Operand operand = new UnknownOperand();
        TerminalClause clause = new TerminalClauseImpl(field, Operator.EQUALS, operand);
        when(queryCache.getValues(queryCreationContext, operand, clause)).thenReturn(null);
        final DefaultJqlOperandResolver jqlOperandSupport = createResolver(null, null, null, null);
        assertNull(jqlOperandSupport.getValues(queryCreationContext, operand, clause));
    }

    @Test
    public void testValidateEmptyOperand() throws Exception
    {
        final MessageSet expectedResult = new MessageSetImpl();

        final OperandHandler<EmptyOperand> emptyHandler = mock(OperandHandler.class);
        TerminalClause clause = new TerminalClauseImpl(field, Operator.EQUALS, EmptyOperand.EMPTY);

        when(emptyHandler.validate(null, EmptyOperand.EMPTY, clause)).thenReturn(expectedResult);

        final DefaultJqlOperandResolver jqlOperandSupport = createResolver(null, emptyHandler, null, null);
        final MessageSet result = jqlOperandSupport.validate(null, EmptyOperand.EMPTY, clause);
        assertEquals(expectedResult, result);
    }

    @Test
    public void testValidateSingleValueOperand() throws Exception
    {
        final MessageSet expectedResult = new MessageSetImpl();
        final SingleValueOperand operand = new SingleValueOperand("");
        TerminalClause clause = new TerminalClauseImpl(field, Operator.EQUALS, operand);

        final OperandHandler<SingleValueOperand> singleHandler = mock(OperandHandler.class);
        when(singleHandler.validate(null, operand, clause)).thenReturn(expectedResult);

        final DefaultJqlOperandResolver jqlOperandSupport = createResolver(null, null, singleHandler, null);
        final MessageSet result = jqlOperandSupport.validate(null, operand, clause);
        assertEquals(expectedResult, result);
    }

    @Test
    public void testValidateMultiValueOperand() throws Exception
    {
        final MessageSet expectedResult = new MessageSetImpl();
        final MultiValueOperand operand = new MultiValueOperand("");
        TerminalClause clause = new TerminalClauseImpl(field, Operator.EQUALS, operand);

        final OperandHandler<MultiValueOperand> multiHandler = mock(OperandHandler.class);
        when(multiHandler.validate(null, operand, clause)).thenReturn(expectedResult);

        final DefaultJqlOperandResolver jqlOperandSupport = createResolver(null, null, null, multiHandler);
        final MessageSet result = jqlOperandSupport.validate(null, operand, clause);
        assertEquals(expectedResult, result);
    }

    @Test
    public void testValidateRegisteredFunction() throws Exception
    {
        final MessageSet expectedResult = new MessageSetImpl();
        final FunctionOperand operand = new FunctionOperand("funcName");
        TerminalClause clause = new TerminalClauseImpl(field, Operator.EQUALS, operand);

        final FunctionOperandHandler funcHandler = mock(FunctionOperandHandler.class);
        when(funcHandler.validate(null, operand, clause)).thenReturn(expectedResult);

        final JqlFunctionHandlerRegistry registry = mock(JqlFunctionHandlerRegistry.class);
        when(registry.getOperandHandler(operand)).thenReturn(funcHandler);

        final DefaultJqlOperandResolver jqlOperandSupport = createResolver(registry, null, null, null);
        final MessageSet result = jqlOperandSupport.validate(null, operand, clause);
        assertEquals(expectedResult, result);
    }

    @Test
    public void testValidateNonRegisteredFunction() throws Exception
    {
        final String expectedError = "jira.jql.operand.illegal.function{[funcName()]}";
        final FunctionOperand operand = new FunctionOperand("funcName");
        TerminalClause clause = new TerminalClauseImpl(field, Operator.EQUALS, operand);

        final JqlFunctionHandlerRegistry registry = mock(JqlFunctionHandlerRegistry.class);
        when(registry.getOperandHandler(operand)).thenReturn(null);

        final DefaultJqlOperandResolver jqlOperandSupport = createResolver(registry, null, null, null);
        final MessageSet result = jqlOperandSupport.validate(null, operand, clause);
        assertEquals(1, result.getErrorMessages().size());
        assertEquals(expectedError, result.getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateUnknownOperand() throws Exception
    {
        final String expectedError = "jira.jql.operand.illegal.operand{[UNKNOWN]}";
        final UnknownOperand operand = new UnknownOperand();
        TerminalClause clause = new TerminalClauseImpl(field, Operator.EQUALS, operand);

        final DefaultJqlOperandResolver jqlOperandSupport = createResolver(null, null, null, null);
        final MessageSet result = jqlOperandSupport.validate(null, operand, clause);
        assertEquals(1, result.getErrorMessages().size());
        assertEquals(expectedError, result.getErrorMessages().iterator().next());
    }

    @Test
    public void testGetSingleValueException()
    {
        final JqlFunctionHandlerRegistry functionRegistry = mock(JqlFunctionHandlerRegistry.class);

        final DefaultJqlOperandResolver jqlOperandSupport = createResolver(functionRegistry, null, null, null);
        try
        {
            jqlOperandSupport.getSingleValue(null, null, null);
            fail("Expected and exception");
        }
        catch (IllegalArgumentException expected)
        {
        }

    }

    @Test
    public void testGetSingleValueMultiReturned()
    {
        final FunctionOperand operand = new FunctionOperand("testGetValues");
        TerminalClause clause = new TerminalClauseImpl(field, Operator.EQUALS, operand);

        final JqlFunctionHandlerRegistry functionRegistry = mock(JqlFunctionHandlerRegistry.class);
        when(functionRegistry.getOperandHandler(operand)).thenReturn(new MockFunctionOperandHandler(10L, 20L));
        when(queryCache.getValues(isA(QueryCreationContext.class), eq(operand), eq(clause))).thenReturn(null);
        final DefaultJqlOperandResolver jqlOperandSupport = createResolver(functionRegistry, null, null, null);

        try
        {
            jqlOperandSupport.getSingleValue(null, operand, clause);
            fail("exception expected");
        }
        catch (IllegalArgumentException expected)
        {
        }

    }

    @Test
    public void testGetSingleValueHappy()
    {
        final FunctionOperand operand = new FunctionOperand("testGetValues");
        final long expectedValue = 10;
        TerminalClause clause = new TerminalClauseImpl(field, Operator.EQUALS, operand);

        final JqlFunctionHandlerRegistry functionRegistry = mock(JqlFunctionHandlerRegistry.class);
        when(functionRegistry.getOperandHandler(operand)).thenReturn(new MockFunctionOperandHandler(expectedValue));
        when(queryCache.getValues(isA(QueryCreationContext.class), eq(operand), eq(clause))).thenReturn(null);

        final DefaultJqlOperandResolver jqlOperandSupport = createResolver(functionRegistry, null, null, null);

        final QueryLiteral literal = jqlOperandSupport.getSingleValue(null, operand, clause);
        assertEquals(createLiteral(expectedValue), literal);

    }

    @Test
    public void testIsEmpty() throws Exception
    {
        final DefaultJqlOperandResolver jqlOperandSupport = createResolver(null, null, null, null);
        assertTrue(jqlOperandSupport.isEmptyOperand(new EmptyOperand()));
        assertFalse(jqlOperandSupport.isEmptyOperand(new UnknownOperand()));
    }

    @Test
    public void testIsFunction() throws Exception
    {
        final FunctionOperand knownFunc = new FunctionOperand("funcName");
        final FunctionOperand unknownFunc = new FunctionOperand("unknownFuncName");

        final FunctionOperandHandler funcHandler = mock(FunctionOperandHandler.class);
        when(funcHandler.isFunction()).thenReturn(true);

        final JqlFunctionHandlerRegistry functionRegistry = mock(JqlFunctionHandlerRegistry.class);
        when(functionRegistry.getOperandHandler(knownFunc)).thenReturn(funcHandler);
        when(functionRegistry.getOperandHandler(unknownFunc)).thenReturn(null);


        final DefaultJqlOperandResolver jqlOperandSupport = createResolver(functionRegistry, null, null, null);
        assertTrue(jqlOperandSupport.isFunctionOperand(knownFunc));
        assertFalse(jqlOperandSupport.isFunctionOperand(unknownFunc));
        assertFalse(jqlOperandSupport.isFunctionOperand(new UnknownOperand()));
    }

    @Test
    public void testIsList() throws Exception
    {
        final DefaultJqlOperandResolver jqlOperandSupport = createResolver(null, null, null, null);
        assertTrue(jqlOperandSupport.isListOperand(new MultiValueOperand("blah")));
        assertFalse(jqlOperandSupport.isListOperand(new UnknownOperand()));
    }

    @Test
    public void testIsValidEmptyOperand() throws Exception
    {
        final DefaultJqlOperandResolver jqlOperandSupport = createResolver(null, null, null, null);
        assertTrue(jqlOperandSupport.isValidOperand(EmptyOperand.EMPTY));
    }

    @Test
    public void testIsValidSingleValueOperand() throws Exception
    {
        final DefaultJqlOperandResolver jqlOperandSupport = createResolver(null, null, null, null);
        assertTrue(jqlOperandSupport.isValidOperand(new SingleValueOperand("ba")));
    }

    @Test
    public void testIsValidMultiValueOperand() throws Exception
    {
        final DefaultJqlOperandResolver jqlOperandSupport = createResolver(null, null, null, null);
        assertTrue(jqlOperandSupport.isValidOperand(new MultiValueOperand("ba")));
    }

    @Test
    public void testIsValidRegisteredFunction() throws Exception
    {
        final FunctionOperand operand = new FunctionOperand("funcName");

        final FunctionOperandHandler funcHandler = mock(FunctionOperandHandler.class);

        final JqlFunctionHandlerRegistry registry = mock(JqlFunctionHandlerRegistry.class);
        when(registry.getOperandHandler(operand)).thenReturn(funcHandler);

        final DefaultJqlOperandResolver jqlOperandSupport = createResolver(registry, null, null, null);
        assertTrue(jqlOperandSupport.isValidOperand(operand));
    }

    @Test
    public void testIsValidNonRegisteredFunction() throws Exception
    {
        final FunctionOperand operand = new FunctionOperand("funcName");

        final JqlFunctionHandlerRegistry registry = mock(JqlFunctionHandlerRegistry.class);
        when(registry.getOperandHandler(operand)).thenReturn(null);

        final DefaultJqlOperandResolver jqlOperandSupport = createResolver(registry, null, null, null);
        assertFalse(jqlOperandSupport.isValidOperand(operand));
    }

    @Test
    public void testIsValidUnknownOperand() throws Exception
    {
        final Operand operand = new UnknownOperand();

        final DefaultJqlOperandResolver jqlOperandSupport = createResolver(null, null, null, null);
        assertFalse(jqlOperandSupport.isValidOperand(operand));
    }

    @Test
    public void testSanitiseFuncOperandNotRegistered() throws Exception
    {
        final FunctionOperand operand = new FunctionOperand("funcName");

        final JqlFunctionHandlerRegistry registry = mock(JqlFunctionHandlerRegistry.class);
        when(registry.getOperandHandler(operand)).thenReturn(null);

        final DefaultJqlOperandResolver jqlOperandSupport = createResolver(registry, null, null, null);
        assertSame(operand, jqlOperandSupport.sanitiseFunctionOperand(null, operand));
    }

    @Test
    public void testSanitiseFuncOperandNotSanitiseAware() throws Exception
    {
        final FunctionOperand operand = new FunctionOperand("funcName");

        // this function does not implement ClauseSanitisingJqlFunction, so it will not attempt to sanitise
        final JqlFunction function = mock(JqlFunction.class);
        final FunctionOperandHandler funcHandler = new FunctionOperandHandler(function, i18nHelper);

        final JqlFunctionHandlerRegistry registry = mock(JqlFunctionHandlerRegistry.class);
        when(registry.getOperandHandler(operand)).thenReturn(funcHandler);

        final DefaultJqlOperandResolver jqlOperandSupport = createResolver(registry, null, null, null);
        assertSame(operand, jqlOperandSupport.sanitiseFunctionOperand(null, operand));
    }

    @Test
    public void testSanitiseFuncOperandSanitiseAware() throws Exception
    {
        final FunctionOperand inputOperand = new FunctionOperand("funcName");
        final FunctionOperand outputOperand = new FunctionOperand("output");

        // this function DOES implement ClauseSanitisingJqlFunction (via DuckTypeProxy) and so the sanitiseOperand method below will be used.
        final Object delegate = new Object()
        {
            public FunctionOperand sanitiseOperand(User searcher, FunctionOperand operand)
            {
                return outputOperand;
            }
        };
        final JqlFunction function = (JqlFunction) DuckTypeProxy.getProxy(new Class[] { JqlFunction.class, ClauseSanitisingJqlFunction.class }, Collections.singletonList(delegate));
        final FunctionOperandHandler funcHandler = new FunctionOperandHandler(function, i18nHelper);

        final JqlFunctionHandlerRegistry registry = mock(JqlFunctionHandlerRegistry.class);
        when(registry.getOperandHandler(inputOperand)).thenReturn(funcHandler);

        final DefaultJqlOperandResolver jqlOperandSupport = createResolver(registry, null, null, null);
        assertEquals(outputOperand, jqlOperandSupport.sanitiseFunctionOperand(null, inputOperand));
    }

    @Test
    public void testSanitiseFuncOperandSurvivesPluginThrowingException() throws Exception
    {
        final FunctionOperand operand = new FunctionOperand("funcName");

        final ClauseSanitisingJqlFunction badJqlFunction = mock(ClauseSanitisingJqlFunction.class, withSettings().extraInterfaces(JqlFunction.class));
        when(badJqlFunction.sanitiseOperand(Mockito.<User>any(), Mockito.<FunctionOperand>any())).thenThrow(new RuntimeException("Misbehaving plugin exception"));
        final FunctionOperandHandler funcHandler = new FunctionOperandHandler((JqlFunction)badJqlFunction, i18nHelper);

        final JqlFunctionHandlerRegistry registry = mock(JqlFunctionHandlerRegistry.class);
        when(registry.getOperandHandler(operand)).thenReturn(funcHandler);

        final DefaultJqlOperandResolver jqlOperandResolver = createResolver(registry, null, null, null);

        jqlOperandResolver.sanitiseFunctionOperand(null, operand);
    }


    private DefaultJqlOperandResolver createResolver(JqlFunctionHandlerRegistry functionRegistry, OperandHandler<EmptyOperand> emptyHandler, OperandHandler<SingleValueOperand> singleHandler, OperandHandler<MultiValueOperand> multiHandler)
    {
        if (functionRegistry == null)
        {
            functionRegistry = new MockJqlFunctionHandlerRegistry();
        }
        if (emptyHandler == null)
        {
            emptyHandler = new MockOperandHandler<EmptyOperand>(false, true, false);
        }
        if (singleHandler == null)
        {
            singleHandler = new MockOperandHandler<SingleValueOperand>(false, false, false);
        }
        if (multiHandler == null)
        {
            multiHandler = new MockOperandHandler<MultiValueOperand>(true, false, false);
        }
        return new DefaultJqlOperandResolver(functionRegistry, new NoopI18nFactory(), emptyHandler, singleHandler, multiHandler,queryCache);
    }

    private static class UnknownOperand implements Operand
    {
        public String getName()
        {
            return "UNKNOWN";
        }

        public String getDisplayString()
        {
            return "UNKNOWN";
        }

        public <R> R accept(final OperandVisitor<R> visitor)
        {
            throw new UnsupportedOperationException();
        }
    }
}
