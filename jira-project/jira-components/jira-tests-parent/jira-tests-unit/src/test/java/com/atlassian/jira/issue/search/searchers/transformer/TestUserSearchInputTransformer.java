package com.atlassian.jira.issue.search.searchers.transformer;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.UserFieldSearchConstantsWithEmpty;
import com.atlassian.jira.issue.search.searchers.util.UserFitsNavigatorHelper;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.issue.transport.impl.ActionParamsImpl;
import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.plugin.jql.function.MembersOfFunction;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operator.Operator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.util.ErrorCollectionAssert.assert1FieldError;
import static com.atlassian.jira.util.ErrorCollectionAssert.assertNoErrors;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestUserSearchInputTransformer
{
    @Rule
    public MockitoContainer mockitoContainer = MockitoMocksInContainer.rule(this);

    @Mock private UserFitsNavigatorHelper userFitsNavigatorHelper = null;
    @Mock private SearchContext searchContext;

    @Mock
    @AvailableInContainer
    private JiraAuthenticationContext authenticationContext = null;

    private UserFieldSearchConstantsWithEmpty searchConstants = null;

    @Before
    public void setUp()
    {
        searchConstants = new UserFieldSearchConstantsWithEmpty("indexField", new ClauseNames("reporter"),
                "fieldUrlParameter", "selectUrlParameter", "searcherId", "emptySelectFlag", "fieldId",
                "currentUserSelectFlag", "specificUserSelectFlag", "specificGroupSelectFlag", "emptyIndexValue",
                OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY);

    }

    @After
    public void tearDown()
    {
        userFitsNavigatorHelper = null;
        searchContext = null;
        authenticationContext = null;
        searchConstants = null;
    }

    @Test
    public void testPopulateFromParamsCurrentUserNotLoggedIn() throws Exception
    {
        final UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null, null)
        {
            @Override
            boolean isUserLoggedIn(final User user)
            {
                return false;
            }
        };

        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl();
        final String[] values = { searchConstants.getCurrentUserSelectFlag() };
        final ActionParamsImpl actionParams = new ActionParamsImpl(ImmutableMap.of(searchConstants.getSelectUrlParameter(), values));
        transformer.populateFromParams(null, valuesHolder, actionParams);
        assertEquals(searchConstants.getCurrentUserSelectFlag(), valuesHolder.get(searchConstants.getSelectUrlParameter()));
    }

    @Test
    public void testPopulateFromParamsCurrentUserLoggedIn() throws Exception
    {
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null, null)
        {
            @Override
            boolean isUserLoggedIn(final User user)
            {
                return true;
            }
        };

        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl();
        final String[] values = { searchConstants.getCurrentUserSelectFlag() };
        final ActionParamsImpl actionParams = new ActionParamsImpl(ImmutableMap.of(searchConstants.getSelectUrlParameter(), values));
        transformer.populateFromParams(null, valuesHolder, actionParams);
        assertEquals(searchConstants.getCurrentUserSelectFlag(), valuesHolder.get(searchConstants.getSelectUrlParameter()));
    }

    @Test
    public void testPopulateFromParamsCurrentNoUserSelectWithUserField() throws Exception
    {
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null, null)
        {
            @Override
            boolean isUserLoggedIn(final User user)
            {
                return true;
            }
        };

        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl();
        final String[] values = { "monkey" };
        final ActionParamsImpl actionParams = new ActionParamsImpl(ImmutableMap.of(searchConstants.getFieldUrlParameter(), values));
        transformer.populateFromParams(null, valuesHolder, actionParams);
        assertEquals(searchConstants.getSpecificUserSelectFlag(), valuesHolder.get(searchConstants.getSelectUrlParameter()));
        assertEquals("monkey", valuesHolder.get(searchConstants.getFieldUrlParameter()));
    }

    @Test
    public void testPopulateFromParamsCurrentHappyPath() throws Exception
    {
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null, null)
        {
            @Override
            boolean isUserLoggedIn(final User user)
            {
                return true;
            }
        };

        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl();
        final String[] values = { "monkey" };
        final String[] select = { searchConstants.getSpecificGroupSelectFlag() };
        final ActionParamsImpl actionParams = new ActionParamsImpl(ImmutableMap.of(searchConstants.getSelectUrlParameter(), select, searchConstants.getFieldUrlParameter(), values));
        transformer.populateFromParams(null, valuesHolder, actionParams);
        assertEquals(searchConstants.getSpecificGroupSelectFlag(), valuesHolder.get(searchConstants.getSelectUrlParameter()));
        assertEquals("monkey", valuesHolder.get(searchConstants.getFieldUrlParameter()));
    }

    @Test
    public void testValidateParamBlankUser() throws Exception
    {
        final UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null, null);
        final FieldValuesHolder valuesHolder = new FieldValuesHolderImpl(ImmutableMap.of(searchConstants.getFieldUrlParameter(), ""));
        final I18nHelper i18n = new MockI18nHelper();
        final ErrorCollection errors = new SimpleErrorCollection();
        transformer.validateParams(null, null, valuesHolder, i18n, errors);
        assertNoErrors(errors);
    }

    @Test
    public void testValidatSpecificUserExists() throws Exception
    {
        final AtomicBoolean called = new AtomicBoolean(false);
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null, null)
        {
            @Override
            boolean userExists(final String user)
            {
                called.set(true);
                assertEquals("monkey", user);
                return true;
            }
        };

        final FieldValuesHolder valuesHolder = new FieldValuesHolderImpl(ImmutableMap.of(searchConstants.getSelectUrlParameter(), searchConstants.getSpecificUserSelectFlag(), searchConstants.getFieldUrlParameter(), "monkey"));
        final I18nHelper i18n = new MockI18nHelper();
        final ErrorCollection errors = new SimpleErrorCollection();
        transformer.validateParams(null, null, valuesHolder, i18n, errors);
        assertNoErrors(errors);
        assertTrue("called", called.get());
    }

    @Test
    public void testValidatSpecificUserDoesntExist() throws Exception
    {
        final AtomicBoolean called = new AtomicBoolean(false);
        final UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null, null)
        {
            @Override
            boolean userExists(final String user)
            {
                called.set(true);
                assertEquals("monkey", user);
                return false;
            }
        };

        final FieldValuesHolder valuesHolder = new FieldValuesHolderImpl(ImmutableMap.of(searchConstants.getSelectUrlParameter(), searchConstants.getSpecificUserSelectFlag(), searchConstants.getFieldUrlParameter(), "monkey"));
        final ErrorCollection errors = new SimpleErrorCollection();
        transformer.validateParams(null, null, valuesHolder, new MockI18nHelper(), errors);
        assert1FieldError(errors, searchConstants.getFieldUrlParameter(), "admin.errors.could.not.find.username [monkey]");
        assertTrue("called", called.get());
    }

    @Test
    public void testValidatSpecificGroupExists() throws Exception
    {
        final AtomicBoolean called = new AtomicBoolean(false);
        final UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null, null)
        {
            @Override
            boolean groupExists(final String user)
            {
                called.set(true);
                assertEquals("monkey", user);
                return true;
            }
        };

        final FieldValuesHolder valuesHolder = new FieldValuesHolderImpl(ImmutableMap.of(searchConstants.getSelectUrlParameter(), searchConstants.getSpecificGroupSelectFlag(), searchConstants.getFieldUrlParameter(), "monkey"));
        final ErrorCollection errors = new SimpleErrorCollection();
        transformer.validateParams(null, null, valuesHolder, new MockI18nHelper(), errors);
        assertNoErrors(errors);
        assertTrue("called", called.get());
    }

    @Test
    public void testValidatSpecificGroupDoesntExist() throws Exception
    {
        final AtomicBoolean called = new AtomicBoolean(false);
        final UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null, null)
        {
            @Override
            boolean groupExists(final String user)
            {
                called.set(true);
                assertEquals("monkey", user);
                return false;
            }
        };

        final FieldValuesHolder valuesHolder = new FieldValuesHolderImpl(ImmutableMap.of(searchConstants.getSelectUrlParameter(), searchConstants.getSpecificGroupSelectFlag(), searchConstants.getFieldUrlParameter(), "monkey"));
        final ErrorCollection errors = new SimpleErrorCollection();
        transformer.validateParams(null, null, valuesHolder, new MockI18nHelper(), errors);
        assert1FieldError(errors, searchConstants.getFieldUrlParameter(), "admin.errors.abstractusersearcher.could.not.find.group [monkey]");
        assertTrue("called", called.get());
    }

    @Test
    public void testPopulateFromSearchRequestNoWhereClause() throws Exception
    {
        final UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null, null);
        final FieldValuesHolder valuesHolder = new FieldValuesHolderImpl();
        transformer.populateFromQuery(null, valuesHolder, new QueryImpl(), searchContext);
        assertTrue("valuesHolder.isEmpty", valuesHolder.isEmpty());
    }

    @Test
    public void testPopulateFromSearchRequestSingleValueOperand() throws Exception
    {
        when(userFitsNavigatorHelper.checkUser("monkey")).thenReturn("monkey");

        final UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null, null);
        final FieldValuesHolder valuesHolder = new FieldValuesHolderImpl();
        final JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();
        final Clause clause = builder.where().reporterUser("monkey").buildClause();

        transformer.populateFromQuery(null, valuesHolder, new QueryImpl(clause), searchContext);
        assertEquals(searchConstants.getSpecificUserSelectFlag(), valuesHolder.get(searchConstants.getSelectUrlParameter()));
        assertEquals("monkey", valuesHolder.get(searchConstants.getFieldUrlParameter()));
    }

    @Test
    public void testPopulateFromSearchRequestSingleValueOperandUserNotOk() throws Exception
    {
        final UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null, null);
        final FieldValuesHolder valuesHolder = new FieldValuesHolderImpl();
        final JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();
        final Clause clause = builder.where().reporterUser("monkey").buildClause();

        transformer.populateFromQuery(null, valuesHolder, new QueryImpl(clause), searchContext);
        assertTrue("valuesHolder.isEmpty", valuesHolder.isEmpty());
    }

    @Test
    public void testPopulateFromSearchRequestEmpty() throws Exception
    {
        final UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null, null);
        final FieldValuesHolder valuesHolder = new FieldValuesHolderImpl();
        final JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();
        final Clause clause = builder.where().reporterIsEmpty().buildClause();

        transformer.populateFromQuery(null, valuesHolder, new QueryImpl(clause), searchContext);
        assertEquals(searchConstants.getEmptySelectFlag(), valuesHolder.get(searchConstants.getSelectUrlParameter()));
    }

    @Test
    public void testPopulateFromSearchRequestSingleValueOperandLong() throws Exception
    {
        when(userFitsNavigatorHelper.checkUser("10")).thenReturn("10");
        final UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null, null);
        final FieldValuesHolder valuesHolder = new FieldValuesHolderImpl();
        final Clause clause = new TerminalClauseImpl(searchConstants.getJqlClauseNames().getPrimaryName(), Operator.EQUALS, 10L);
        transformer.populateFromQuery(null, valuesHolder, new QueryImpl(clause), searchContext);

        assertEquals(searchConstants.getSpecificUserSelectFlag(), valuesHolder.get(searchConstants.getSelectUrlParameter()));
        assertEquals("10", valuesHolder.get(searchConstants.getFieldUrlParameter()));
    }

    @Test
    public void testPopulateFromSearchRequestGroupFunction() throws Exception
    {
        final UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null, null);
        final FieldValuesHolder valuesHolder = new FieldValuesHolderImpl();
        final JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();
        final Clause clause = builder.where().reporterInGroup("monkey").buildClause();
        transformer.populateFromQuery(null, valuesHolder, new QueryImpl(clause), searchContext);

        assertEquals(searchConstants.getSpecificGroupSelectFlag(), valuesHolder.get(searchConstants.getSelectUrlParameter()));
        assertEquals("monkey", valuesHolder.get(searchConstants.getFieldUrlParameter()));
    }

    @Test
    public void testPopulateFromSearchRequestGroupFunctionMultiArg() throws Exception
    {
        final UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null, null);
        final FieldValuesHolder valuesHolder = new FieldValuesHolderImpl();
        final Clause clause = new TerminalClauseImpl(searchConstants.getJqlClauseNames().getPrimaryName(), Operator.IN, new FunctionOperand(MembersOfFunction.FUNCTION_MEMBERSOF, "monkey1", "monkey2" ));
        transformer.populateFromQuery(null, valuesHolder, new QueryImpl(clause), searchContext);

        assertThat(valuesHolder, not(hasKey(searchConstants.getSelectUrlParameter())));
        assertThat(valuesHolder, not(hasKey(searchConstants.getFieldUrlParameter())));
    }

    @Test
    public void testPopulateFromSearchRequestCurrentUserFunction() throws Exception
    {
        final UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null, null);
        final FieldValuesHolder valuesHolder = new FieldValuesHolderImpl();
        final JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();
        final Clause clause = builder.where().reporterIsCurrentUser().buildClause();
        transformer.populateFromQuery(null, valuesHolder, new QueryImpl(clause), searchContext);

        assertEquals(searchConstants.getCurrentUserSelectFlag(), valuesHolder.get(searchConstants.getSelectUrlParameter()));
    }

    @Test
    public void testPopulateFromSearchRequestNotRelevant() throws Exception
    {
        final UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null, null);
        final FieldValuesHolder valuesHolder = new FieldValuesHolderImpl();
        final Clause clause = new TerminalClauseImpl("blarg", Operator.EQUALS, "blarg");
        transformer.populateFromQuery(null, valuesHolder, new QueryImpl(clause), searchContext);

        assertThat(valuesHolder, not(hasKey(searchConstants.getSelectUrlParameter())));
        assertThat(valuesHolder, not(hasKey(searchConstants.getFieldUrlParameter())));
    }

    @Test
    public void testPopulateFromSearchRequestMultiValueOperand() throws Exception
    {
        final UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null, null);
        final FieldValuesHolder valuesHolder = new FieldValuesHolderImpl();
        final Clause clause = new TerminalClauseImpl(searchConstants.getJqlClauseNames().getPrimaryName(), Operator.EQUALS, new MultiValueOperand("blarg", "blarg"));
        transformer.populateFromQuery(null, valuesHolder, new QueryImpl(clause), searchContext);

        assertThat(valuesHolder, not(hasKey(searchConstants.getSelectUrlParameter())));
        assertThat(valuesHolder, not(hasKey(searchConstants.getFieldUrlParameter())));
    }
    
    @Test
    public void testPopulateFromSearchRequestNoQuery() throws Exception
    {
        final FieldValuesHolder valuesHolder = new FieldValuesHolderImpl();
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null, null);

        transformer.populateFromQuery(null, valuesHolder, null, searchContext);

        assertThat(valuesHolder, not(hasKey(searchConstants.getSelectUrlParameter())));
        assertThat(valuesHolder, not(hasKey(searchConstants.getFieldUrlParameter())));
    }

    @Test
    public void testDoRelevantClausesFitFilterFormNoWhereClause() throws Exception
    {
        final UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null, null);

        assertTrue(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(), searchContext));
    }

    @Test
    public void testDoRelevantClausesFitFilterFormEmpty() throws Exception
    {
        final UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null, null);
        final JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();
        final Clause clause = builder.where().reporterIsEmpty().buildClause();

        assertTrue(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(clause), searchContext));
    }

    @Test
    public void testDoRelevantClausesFitFilterFormNotEmpty() throws Exception
    {
        final UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null, null);
        final Clause clause = new TerminalClauseImpl(searchConstants.getJqlClauseNames().getPrimaryName(), Operator.IS_NOT, EmptyOperand.EMPTY);

        assertFalse(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(clause), searchContext));
    }

    @Test
    public void testDoRelevantClausesFitFilterFormSingleValueOperandUserOk() throws Exception
    {
        when(userFitsNavigatorHelper.checkUser("monkey")).thenReturn("monkey");
        final UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null, null);
        final Clause clause = new TerminalClauseImpl(searchConstants.getJqlClauseNames().getPrimaryName(), Operator.EQUALS, "monkey");

        assertTrue(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(clause), searchContext));
    }

    @Test
    public void testDoRelevantClausesFitFilterFormSingleValueOperandUserNotOk() throws Exception
    {
        final UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null, null);
        final Clause clause = new TerminalClauseImpl(searchConstants.getJqlClauseNames().getPrimaryName(), Operator.EQUALS, "monkey");

        assertFalse(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(clause), searchContext));
    }

    @Test
    public void testDoRelevantClausesFitFilterFormSingleValueOperandLong() throws Exception
    {
        when(userFitsNavigatorHelper.checkUser("10")).thenReturn("10");
        final UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null, null);
        final Clause clause = new TerminalClauseImpl(searchConstants.getJqlClauseNames().getPrimaryName(), Operator.EQUALS, 10L);

        assertTrue(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(clause), searchContext));
    }

    @Test
    public void testDoRelevantClausesFitFilterFormNotSingleValueOperandLong() throws Exception
    {
        final UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null, null);
        final Clause clause = new TerminalClauseImpl(searchConstants.getJqlClauseNames().getPrimaryName(), Operator.NOT_EQUALS, 10L);

        assertFalse(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(clause), searchContext));
    }

    @Test
    public void testDoRelevantClausesFitFilterFormGroupFunction() throws Exception
    {
        final UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null, null);
        final JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();
        final Clause clause = builder.where().reporterInGroup("monkey").buildClause();

        assertTrue(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(clause), searchContext));
    }

    @Test
    public void testDoRelevantClausesFitFilterFormNotGroupFunction() throws Exception
    {
        final UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null, null);
        final Clause clause = new TerminalClauseImpl(searchConstants.getJqlClauseNames().getPrimaryName(), Operator.NOT_IN, new FunctionOperand("membersOf", "jira-users"));

        assertFalse(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(clause), searchContext));
    }

    @Test
    public void testDoRelevantClausesFitFilterFormGroupFunctionMultiArg() throws Exception
    {
        final UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null, null);
        final Clause clause = new TerminalClauseImpl(searchConstants.getJqlClauseNames().getPrimaryName(), Operator.IN, new FunctionOperand(MembersOfFunction.FUNCTION_MEMBERSOF, "monkey1", "monkey2" ));

        assertFalse(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(clause), searchContext));
    }

    @Test
    public void testDoRelevantClausesFitFilterFormCurrentUserFunctionUserLoggedIn() throws Exception
    {
        final UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null, null)
        {
            @Override
            boolean isUserLoggedIn(final User user)
            {
                return true;
            }
        };

        final JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();
        final Clause clause = builder.where().reporterIsCurrentUser().buildClause();

        assertTrue(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(clause), searchContext));
    }

    @Test
    public void testDoRelevantClausesFitFilterFormCurrentUserFunctionUserNotLoggedIn() throws Exception
    {
        final UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null, null)
        {
            @Override
            boolean isUserLoggedIn(final User user)
            {
                return false;
            }
        };

        final JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();
        final Clause clause = builder.where().reporterIsCurrentUser().buildClause();

        assertFalse(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(clause), searchContext));
    }

    @Test
    public void testDoRelevantClausesFitFilterFormNotCurrentUserFunction() throws Exception
    {
        final UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null, null);
        final Clause clause = new TerminalClauseImpl(searchConstants.getJqlClauseNames().getPrimaryName(), Operator.NOT_EQUALS, new FunctionOperand("currentUser"));

        assertFalse(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(clause), searchContext));
    }

    @Test
    public void testDoRelevantClausesFitFilterFormNotRelevant() throws Exception
    {
        final UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null, null);
        final Clause clause = new TerminalClauseImpl("blarg", Operator.EQUALS, "blarg");

        assertTrue(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(clause), searchContext));
    }

    @Test
    public void testDoRelevantClausesFitFilterFormToManyClauses() throws Exception
    {
        final TerminalClause clause = new TerminalClauseImpl(searchConstants.getJqlClauseNames().getPrimaryName(), Operator.EQUALS, "blarg");
        final UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null, null)
        {
            @Override
            SimpleNavigatorCollectorVisitor createSimpleNavigatorCollectorVisitor()
            {
                return new MockSimpleNavigatorCollectorVisitor(true, ImmutableList.of(clause, clause));
            }
        };

        assertFalse(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(clause), searchContext));
    }

    @Test
    public void testDoRelevantClausesFitFilterFormNotValid() throws Exception
    {
        final TerminalClause clause = new TerminalClauseImpl(searchConstants.getJqlClauseNames().getPrimaryName(), Operator.EQUALS, "blarg");
        final UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null, null)
        {
            @Override
            SimpleNavigatorCollectorVisitor createSimpleNavigatorCollectorVisitor()
            {
                return new MockSimpleNavigatorCollectorVisitor(false, ImmutableList.of(clause));
            }
        };

        assertFalse(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(clause), searchContext));
    }

    @Test
    public void testDoRelevantClausesFitFilterFormNoQuery() throws Exception
    {
        final UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null, null);

        assertTrue(transformer.doRelevantClausesFitFilterForm(null, null, searchContext));
    }

    @Test
    public void testDoRelevantClausesFitFilterFormMultiValueOperand() throws Exception
    {
        final UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null, null);
        final Clause clause = new TerminalClauseImpl(searchConstants.getJqlClauseNames().getPrimaryName(), Operator.EQUALS, new MultiValueOperand("blarg", "blarg"));

        assertFalse(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(clause), searchContext));
    }

    @Test
    public void testGetSearchClauseEmpty() throws Exception
    {
        final UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null, null);
        final FieldValuesHolder valuesHolder = new FieldValuesHolderImpl(ImmutableMap.of(searchConstants.getSelectUrlParameter(), searchConstants.getEmptySelectFlag()));
        final JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();

        assertEquals(builder.where().reporterIsEmpty().buildClause(), transformer.getSearchClause(null, valuesHolder));
    }

    @Test
    public void testGetSearchClauseCurrentUser() throws Exception
    {
        final UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null, null);
        final FieldValuesHolder valuesHolder = new FieldValuesHolderImpl(ImmutableMap.of(searchConstants.getSelectUrlParameter(), searchConstants.getCurrentUserSelectFlag()));
        final JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();

        assertEquals(builder.where().reporterIsCurrentUser().buildClause(), transformer.getSearchClause(null, valuesHolder));
    }

    @Test
    public void testGetSearchClauseSpecificUser() throws Exception
    {
        final UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null, null);
        final JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();
        FieldValuesHolder valuesHolder = new FieldValuesHolderImpl(ImmutableMap.of(searchConstants.getSelectUrlParameter(), searchConstants.getSpecificUserSelectFlag(), searchConstants.getFieldUrlParameter(), "monkey"));
        assertEquals(builder.where().reporterUser("monkey").buildClause(), transformer.getSearchClause(null, valuesHolder));

        JqlQueryBuilder builder2 = JqlQueryBuilder.newBuilder();
        valuesHolder = new FieldValuesHolderImpl(ImmutableMap.of(searchConstants.getFieldUrlParameter(), "monkey"));
        assertEquals(builder2.where().reporterUser("monkey").buildClause(), transformer.getSearchClause(null, valuesHolder));
    }

    @Test
    public void testGetSearchClauseSpecificGroup() throws Exception
    {
        final UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null, null);
        final JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();

        final FieldValuesHolder valuesHolder = new FieldValuesHolderImpl(ImmutableMap.of(searchConstants.getSelectUrlParameter(), searchConstants.getSpecificGroupSelectFlag(), searchConstants.getFieldUrlParameter(), "monkey"));
        assertEquals(builder.where().reporterInGroup("monkey").buildClause(), transformer.getSearchClause(null, valuesHolder));
    }

    @Test
    public void testGetSearchClauseNotRelevant() throws Exception
    {
        final UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null, null);

        final FieldValuesHolder valuesHolder = new FieldValuesHolderImpl();
        assertNull(transformer.getSearchClause(null, valuesHolder));
    }

    static class MockSimpleNavigatorCollectorVisitor extends SimpleNavigatorCollectorVisitor
    {
        private final boolean isValid;
        private final List<TerminalClause> clauses;

        @Override
        public List<TerminalClause> getClauses()
        {
            return clauses;
        }

        @Override
        public boolean isValid()
        {
            return isValid;
        }

        public MockSimpleNavigatorCollectorVisitor(final boolean isValid, List<TerminalClause> clauses)
        {
            super("clauseName");
            this.isValid = isValid;
            this.clauses = clauses;
        }


        @Override
        public Void visit(final AndClause andClause)
        {
            return null;
        }

        @Override
        public Void visit(final NotClause notClause)
        {
            return null;
        }

        @Override
        public Void visit(final OrClause orClause)
        {
            return null;
        }

        @Override
        public Void visit(final TerminalClause terminalClause)
        {
            return null;
        }
    }
}
