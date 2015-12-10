package com.atlassian.jira.jql.validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.jql.ClauseHandler;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import com.google.common.collect.ImmutableList;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestDefaultValidatorRegistry
{
    private static final User ANONYMOUS = null;

    @Mock SearchHandlerManager searchHandlerManager;
    @Mock WasClauseValidator wasClauseValidator;
    @Mock ChangedClauseValidator changedClauseValidator;

    @After
    public void tearDown()
    {
        searchHandlerManager = null;
        wasClauseValidator = null;
        changedClauseValidator = null;
    }

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @Test(expected = IllegalArgumentException.class)
    public void testConstrcutor()
    {
        new DefaultValidatorRegistry(null, null, null);
    }

    @Test
    public void testGetClauseQueryFactory() throws Exception
    {
        final ClauseValidator clauseValidator = mock(ClauseValidator.class);
        final ClauseHandler clauseHandler = mock(ClauseHandler.class);
        when(clauseHandler.getValidator()).thenReturn(clauseValidator);

        final List<ClauseHandler> expectedHandlers = ImmutableList.of(clauseHandler);

        final String clauseName = "name";
        when(searchHandlerManager.getClauseHandler(ANONYMOUS, clauseName)).thenReturn(expectedHandlers);

        final DefaultValidatorRegistry validatorRegistry = fixture();
        assertEquals(Collections.singletonList(clauseValidator), new ArrayList<ClauseValidator>(validatorRegistry.getClauseValidator(null,
                new TerminalClauseImpl(clauseName, Operator.IN, "value"))));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetClauseQueryFactoryBadArgs()
    {
        final DefaultValidatorRegistry validatorRegistry = fixture();
        validatorRegistry.getClauseValidator(ANONYMOUS, (TerminalClause)null);
    }

    DefaultValidatorRegistry fixture()
    {
        return new DefaultValidatorRegistry(searchHandlerManager, wasClauseValidator, changedClauseValidator);
    }
}
