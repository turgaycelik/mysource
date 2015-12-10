package com.atlassian.jira.jql.validator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.search.MockSearchRequest;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.MockOperandHandler;
import com.atlassian.jira.jql.operand.OperandHandler;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.resolver.SavedFilterResolver;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.UserKeyService;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith (MockitoJUnitRunner.class)
public class TestSavedFilterClauseValidator
{
    private MockJqlOperandResolver jqlOperandResolver = MockJqlOperandResolver.createSimpleSupport();

    @Mock
    private SavedFilterResolver filterResolver;

    @Mock
    private SavedFilterCycleDetector savedFilterCycleDetector;

    @AvailableInContainer
    @Mock
    private UserKeyService userKeyService;

    @Rule
    public MockitoContainer mockitoContainer = MockitoMocksInContainer.rule(this);

    @Mock
    private User theUser;

    private boolean overrideSecurity = false;
    private long nextId = 0;

    @Before
    public void setUp() throws Exception
    {
        when(theUser.getName()).thenReturn("admin");
        when(theUser.getDirectoryId()).thenReturn(-1L);
    }

    @Test
    public void testValidateEmpty()
    {
        final Operand operand = EmptyOperand.EMPTY;
        TerminalClause clause = new TerminalClauseImpl("test", Operator.EQUALS, operand);

        final MockI18NValidator validator = new MockI18NValidator(filterResolver, jqlOperandResolver, savedFilterCycleDetector);
        final MessageSet messageSet = validator.validate(theUser, clause);
        assertTrue(messageSet.hasAnyMessages());
        assertEquals("The field 'test' does not support searching for EMPTY values.", messageSet.getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateEmptyFromFunction()
    {
        final Operand operand = new FunctionOperand("generateEmpty");
        jqlOperandResolver.addHandler(operand.getName(), new MockEmptyHandler());

        TerminalClause clause = new TerminalClauseImpl("test", Operator.EQUALS, operand);

        final MockI18NValidator validator = new MockI18NValidator(filterResolver, jqlOperandResolver, savedFilterCycleDetector);
        final MessageSet messageSet = validator.validate(theUser, clause);
        assertTrue(messageSet.hasAnyMessages());
        assertEquals("The field 'test' does not support EMPTY values provided by function 'generateEmpty'.", messageSet.getErrorMessages().iterator().next());
    }


    @Test
    public void testValidateHappyPathMultipleFilters()
    {
        assertHappyPath(null);
    }

    @Test
    public void testValidateHappyPathMultipleFiltersAndFilterId()
    {
        assertHappyPath(28283843876L);
    }

    private void assertHappyPath(Long id)
    {
        final SearchRequest filter1 = getRequest("filter1");
        final SearchRequest filter2 = getRequest("filter2");
        final SearchRequest filter3 = getRequest("filter3");

        final MultiValueOperand operand = new MultiValueOperand(filter1.getName(), filter2.getName(), filter3.getName());
        TerminalClause clause = new TerminalClauseImpl("test", Operator.IN, operand);

        final SavedFilterClauseValidator filterClauseValidator = new MockI18NValidator(filterResolver, jqlOperandResolver, savedFilterCycleDetector);
        final MessageSet messageSet;
        if (id == null)
        {
            messageSet = filterClauseValidator.validate(theUser, clause);
        }
        else
        {
            messageSet = filterClauseValidator.validate(theUser, clause, id);
        }

        assertFalse(messageSet.hasAnyMessages());
        verify(savedFilterCycleDetector).containsSavedFilterReference(theUser, overrideSecurity, filter1, id);
        verify(savedFilterCycleDetector).containsSavedFilterReference(theUser, overrideSecurity, filter2, id);
        verify(savedFilterCycleDetector).containsSavedFilterReference(theUser, overrideSecurity, filter3, id);
    }

    @Test
    public void testValidateMultipleFiltersOneContainsCycle() throws Exception
    {
        final SearchRequest filter1 = getRequest("filter1");
        final SearchRequest filter2 = getRequest("filter2");
        final SearchRequest filter3 = getRequest(1919191L, "filter3");

        final MultiValueOperand operand = new MultiValueOperand(new SingleValueOperand(filter1.getName()),
                new SingleValueOperand(filter2.getName()), new SingleValueOperand(filter3.getId()));
        TerminalClause clause = new TerminalClauseImpl("test", Operator.IN, operand);

        when(savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, filter3, null)).thenReturn(true);
        final SavedFilterClauseValidator filterClauseValidator = new MockI18NValidator(filterResolver, jqlOperandResolver, savedFilterCycleDetector);

        final MessageSet messageSet = filterClauseValidator.validate(theUser, clause);
        assertTrue(messageSet.hasAnyMessages());
        assertEquals("Field 'test' with value '1919191' matches filter 'filter3' and causes a cyclical reference, this query can not be executed and should be edited.", messageSet.getErrorMessages().iterator().next());

        verify(savedFilterCycleDetector).containsSavedFilterReference(theUser, overrideSecurity, filter1, null);
        verify(savedFilterCycleDetector).containsSavedFilterReference(theUser, overrideSecurity, filter2, null);
    }

    @Test
    public void testErrorFindingFilterByName() throws Exception
    {
        final SearchRequest filter1 = getRequest("filter1");
        final SearchRequest filter2 = new MockSearchRequest("admin", getNextId(), "filter2");
        final SearchRequest filter3 = getRequest("filter3");

        final MultiValueOperand operand = new MultiValueOperand(filter1.getName(), filter2.getName(), filter3.getName());
        TerminalClause clause = new TerminalClauseImpl("test", Operator.IN, operand);

        final SavedFilterClauseValidator filterClauseValidator = new MockI18NValidator(filterResolver, jqlOperandResolver, savedFilterCycleDetector);
        final MessageSet messageSet = filterClauseValidator.validate(theUser, clause);

        verify(savedFilterCycleDetector).containsSavedFilterReference(theUser, overrideSecurity, filter1, null);
        verify(savedFilterCycleDetector).containsSavedFilterReference(theUser, overrideSecurity, filter3, null);

        assertTrue(messageSet.hasAnyMessages());
        assertEquals("The value 'filter2' does not exist for the field 'test'.", messageSet.getErrorMessages().iterator().next());
    }

    @Test
    public void testErrorFindingFilterByNameFromFunction() throws Exception
    {
        final Operand function = new FunctionOperand("functionName");
        jqlOperandResolver.addHandler(function.getName(), new MockOperandHandler<Operand>(false, false, true).add(new QueryLiteral(function, "random")));

        final SearchRequest filter1 = getRequest("filter1");
        final SearchRequest filter3 = getRequest("filter3");

        final MultiValueOperand operand = new MultiValueOperand(new SingleValueOperand(filter1.getName()), function, new SingleValueOperand(filter3.getName()));
        TerminalClause clause = new TerminalClauseImpl("test", Operator.IN, operand);

        final SavedFilterClauseValidator filterClauseValidator = new MockI18NValidator(filterResolver, jqlOperandResolver, savedFilterCycleDetector);

        final MessageSet messageSet = filterClauseValidator.validate(theUser, clause);
        assertTrue(messageSet.hasAnyMessages());
        assertEquals("A value provided by the function 'functionName' is invalid for the field 'test'.", messageSet.getErrorMessages().iterator().next());

        verify(savedFilterCycleDetector).containsSavedFilterReference(theUser, overrideSecurity, filter1, null);
        verify(savedFilterCycleDetector).containsSavedFilterReference(theUser, overrideSecurity, filter3, null);
    }

    @Test
    public void testErrorFindingFilterById() throws Exception
    {
        TerminalClause clause = new TerminalClauseImpl("test", Operator.IN, new SingleValueOperand(12992L));

        final SavedFilterClauseValidator filterClauseValidator = new MockI18NValidator(filterResolver, jqlOperandResolver, savedFilterCycleDetector);

        final MessageSet messageSet = filterClauseValidator.validate(theUser, clause);
        assertTrue(messageSet.hasAnyMessages());
        assertEquals("A value with ID '12992' does not exist for the field 'test'.", messageSet.getErrorMessages().iterator().next());
    }

    @Test
    public void testErrorFindingFilterByIdFromFunction() throws Exception
    {
        final Operand function = new FunctionOperand("functionName");
        jqlOperandResolver.addHandler(function.getName(), new MockOperandHandler<Operand>(false, false, true).add(new QueryLiteral(function, 10101L)));

        TerminalClause clause = new TerminalClauseImpl("test", Operator.IN, 28382L);

        final SavedFilterClauseValidator filterClauseValidator = new MockI18NValidator(filterResolver, jqlOperandResolver, savedFilterCycleDetector);

        final MessageSet messageSet = filterClauseValidator.validate(theUser, clause);
        assertTrue(messageSet.hasAnyMessages());
        assertEquals("A value with ID '28382' does not exist for the field 'test'.", messageSet.getErrorMessages().iterator().next());
    }

    @Test
    public void testInvalidOperator() throws Exception
    {
        final TerminalClauseImpl name = new TerminalClauseImpl("name", Operator.EQUALS, 1);
        final MessageSetImpl messageSet = new MessageSetImpl();
        messageSet.addErrorMessage("dude");


        final SupportedOperatorsValidator mock = Mockito.mock(SupportedOperatorsValidator.class);
        when(mock.validate(theUser, name)).thenReturn(messageSet);

        final SavedFilterClauseValidator filterClauseValidator = new MockI18NValidator(filterResolver, jqlOperandResolver, savedFilterCycleDetector)
        {
            @Override
            SupportedOperatorsValidator getSupportedOperatorsValidator()
            {
                return mock;
            }
        };
        final MessageSet foundSet = filterClauseValidator.validate(theUser, name);
        assertTrue(foundSet.hasAnyMessages());
    }

    private long getNextId()
    {
        return nextId++;
    }

    private SearchRequest getRequest(String name)
    {
        final SingleValueOperand operand = new SingleValueOperand(name);
        SearchRequest mockRequest = new MockSearchRequest("admin", getNextId(), name);
        when(filterResolver.getSearchRequest(theUser, Collections.singletonList(new QueryLiteral(operand, name))))
                .thenReturn(Arrays.asList(mockRequest));

        return mockRequest;
    }

    private SearchRequest getRequest(long id, String name)
    {
        final SingleValueOperand operand = new SingleValueOperand(id);
        SearchRequest mockRequest = new MockSearchRequest("admin", id, name);
        when(filterResolver.getSearchRequest(theUser, Collections.singletonList(new QueryLiteral(operand, id))))
                .thenReturn(Arrays.asList(mockRequest));

        return mockRequest;
    }

    private static class MockEmptyHandler implements OperandHandler<FunctionOperand>
    {
        @Override
        public MessageSet validate(final User searcher, final FunctionOperand operand, final TerminalClause terminalClause)
        {
            return new MessageSetImpl();
        }

        @Override
        public List<QueryLiteral> getValues(final QueryCreationContext queryCreationContext, final FunctionOperand operand, final TerminalClause terminalClause)
        {
            return Collections.singletonList(new QueryLiteral(operand));
        }

        @Override
        public boolean isList()
        {
            return true;
        }

        @Override
        public boolean isEmpty()
        {
            return false;
        }

        @Override
        public boolean isFunction()
        {
            return true;
        }
    }

    private static class MockI18NValidator extends SavedFilterClauseValidator
    {
        public MockI18NValidator(final SavedFilterResolver savedFilterResolver,
                final JqlOperandResolver jqlOperandResolver, final SavedFilterCycleDetector savedFilterCycleDetector)
        {
            super(savedFilterResolver, jqlOperandResolver, savedFilterCycleDetector);
        }

        @Override
        I18nHelper getI18n(final User user)
        {
            return new MockI18nBean();
        }
    }
}
