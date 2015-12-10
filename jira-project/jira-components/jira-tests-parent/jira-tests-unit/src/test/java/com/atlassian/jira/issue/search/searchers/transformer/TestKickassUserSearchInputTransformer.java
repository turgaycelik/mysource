package com.atlassian.jira.issue.search.searchers.transformer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.constants.UserFieldSearchConstantsWithEmpty;
import com.atlassian.jira.issue.search.searchers.util.UserFitsNavigatorHelper;
import com.atlassian.jira.issue.transport.ActionParams;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.issue.transport.impl.ActionParamsImpl;
import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.plugin.jql.function.CurrentUserFunction;
import com.atlassian.jira.plugin.jql.function.MembersOfFunction;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import com.google.common.collect.Sets;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link KickassUserSearchInputTransformer}.
 *
 * @since v5.2
 */
@RunWith (MockitoJUnitRunner.class)
public class TestKickassUserSearchInputTransformer
{
    private FieldValuesHolder fieldValuesHolder;
    private UserFieldSearchConstantsWithEmpty searchConstants;
    private KickassUserSearchInputTransformer transformer;

    @Mock
    private CustomField customField;
    @Mock
    private CustomFieldInputHelper customFieldInputHelper;
    @Mock
    private UserFitsNavigatorHelper userFitsNavigatorHelper;
    @Mock
    private UserManager userManager;
    @Mock
    private ApplicationUser appUser;
    private User user;

    @Before
    public void setUp()
    {
        user = new MockUser("admin");
        fieldValuesHolder = new FieldValuesHolderImpl();
        searchConstants = new UserFieldSearchConstantsWithEmpty("indexField",
                new ClauseNames("assignee"), "fieldUrlParameter",
                "selectUrlParameter", "searcherId", "emptySelectFlag",
                "fieldId", "currentUserSelectFlag", "specificUserSelectFlag",
                "specificGroupSelectFlag", "emptyIndexValue",
                OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY);
    }

    @Test
    public void testDoRelevantClausesFitFilterForm()
    {
        Operand currentUserOperand = new FunctionOperand(CurrentUserFunction.FUNCTION_CURRENT_USER);
        Operand groupOperand = new FunctionOperand(MembersOfFunction.FUNCTION_MEMBERSOF, "administrators");
        Operand userOperand = new SingleValueOperand("admin");
        Operand multiValueOperand = new MultiValueOperand(currentUserOperand,
                new EmptyOperand(), groupOperand, userOperand);
        Query query = new QueryImpl(new TerminalClauseImpl("assignee",
                Operator.IN, multiValueOperand));

        when(userFitsNavigatorHelper.checkUser("admin")).thenReturn("admin");
        when(userManager.getUserByKey("admin")).thenReturn(appUser);
        transformer = new KickassUserSearchInputTransformer(searchConstants,
                userFitsNavigatorHelper, null, userManager, null);
        assertTrue(transformer.doRelevantClausesFitFilterForm(user, query, null));
    }

    /**
     * Queries in the form `field = currentUser()` can be represented in basic
     * mode if the user performing the search is authenticated.
     */
    @Test
    public void testDoRelevantClausesFitFilterFormCurrentUser()
    {
        Operand currentUserOperand = new FunctionOperand(CurrentUserFunction.FUNCTION_CURRENT_USER);
        Query query = new QueryImpl(new TerminalClauseImpl("assignee",
                Operator.EQUALS, currentUserOperand));

        transformer = new KickassUserSearchInputTransformer(searchConstants,
                userFitsNavigatorHelper, null, null, null);
        assertTrue(transformer.doRelevantClausesFitFilterForm(user, query, null));
        assertFalse(transformer.doRelevantClausesFitFilterForm(null, query, null));
    }

    /**
     * Queries in the form `field IS EMPTY` can be represented in basic mode.
     */
    @Test
    public void testDoRelevantClausesFitFilterFormEmpty()
    {
        Query query = new QueryImpl(new TerminalClauseImpl("assignee",
                Operator.IS, new EmptyOperand()));

        transformer = new KickassUserSearchInputTransformer(searchConstants,
                userFitsNavigatorHelper, null, null, null);
        assertTrue(transformer.doRelevantClausesFitFilterForm(user, query, null));
    }

    /**
     * Queries in the form `field = value` can be represented in basic mode.
     */
    @Test
    public void testDoRelevantClausesFitFilterFormEquals()
    {
        Query query = new QueryImpl(new TerminalClauseImpl("assignee",
                Operator.EQUALS, "admin"));

        when(userFitsNavigatorHelper.checkUser("admin")).thenReturn("admin");
        when(userManager.getUserByKey("admin")).thenReturn(appUser);
        transformer = new KickassUserSearchInputTransformer(searchConstants,
                userFitsNavigatorHelper, null, userManager, null);
        assertTrue(transformer.doRelevantClausesFitFilterForm(user, query, null));
    }

    /**
     * Queries in the form `field = EMPTY` can be represented in basic mode.
     */
    @Test
    public void testDoRelevantClausesFitFilterFormEqualsEmpty()
    {
        Query query = new QueryImpl(new TerminalClauseImpl("assignee",
                Operator.EQUALS, new EmptyOperand()));

        transformer = new KickassUserSearchInputTransformer(searchConstants,
                userFitsNavigatorHelper, null, null, null);
        assertTrue(transformer.doRelevantClausesFitFilterForm(user, query, null));
    }

    /**
     * Queries in the form `field = membersOf(group)` can't be represented in
     * basic mode.
     */
    @Test
    public void testDoRelevantClausesFitFilterFormEqualsMembersOf()
    {
        Operand membersOfOperand = new FunctionOperand(
                MembersOfFunction.FUNCTION_MEMBERSOF, "administrators");
        Query query = new QueryImpl(new TerminalClauseImpl("assignee",
                Operator.EQUALS, membersOfOperand));

        transformer = new KickassUserSearchInputTransformer(searchConstants,
                userFitsNavigatorHelper, null, null, null);
        assertFalse(transformer.doRelevantClausesFitFilterForm(user, query, null));
    }

    /**
     * Queries in the form `field IN membersOf(group)` can be represented in
     * basic mode.
     */
    @Test
    public void testDoRelevantClausesFitFilterFormInMembersOf()
    {
        Operand membersOfOperand = new FunctionOperand(
                MembersOfFunction.FUNCTION_MEMBERSOF, "administrators");
        Query query = new QueryImpl(new TerminalClauseImpl("assignee",
                Operator.IN, membersOfOperand));

        transformer = new KickassUserSearchInputTransformer(searchConstants,
                userFitsNavigatorHelper, null, null, null);
        assertTrue(transformer.doRelevantClausesFitFilterForm(user, query, null));
    }

    /**
     * If multiple clauses deal with our field, then it can't be represented in
     * basic mode (e.g. `field = a OR field = b`).
     */
    @Test
    public void testDoRelevantClausesFitFilterFormMultipleClauses()
    {
        Clause adminClause = new TerminalClauseImpl("assignee", Operator.EQUALS, "admin");
        Clause santaClause = new TerminalClauseImpl("assignee", Operator.EQUALS, "santa");
        Query query = new QueryImpl(new OrClause(adminClause, santaClause));

        when(userFitsNavigatorHelper.checkUser("admin")).thenReturn("admin");
        when(userFitsNavigatorHelper.checkUser("santa")).thenReturn("santa");
        transformer = new KickassUserSearchInputTransformer(searchConstants,
                userFitsNavigatorHelper, null, null, null);
        assertFalse(transformer.doRelevantClausesFitFilterForm(user, query, null));
    }

    /**
     * Not equals and not in cannot be represented
     */
    @Test
    public void testDoRelevantClausesFitFilterFormNot()
    {
        Query query = new QueryImpl(new TerminalClauseImpl(
                "assignee", Operator.NOT_EQUALS, new SingleValueOperand("admin")));

        when(userFitsNavigatorHelper.checkUser("admin")).thenReturn("admin");
        when(userFitsNavigatorHelper.checkUser("santa")).thenReturn("santa");
        transformer = new KickassUserSearchInputTransformer(searchConstants,
                userFitsNavigatorHelper, null, null, null);
        assertFalse(transformer.doRelevantClausesFitFilterForm(user, query, null));

        query = new QueryImpl(new TerminalClauseImpl(
                "assignee", Operator.NOT_IN, new SingleValueOperand("admin")));
        assertFalse(transformer.doRelevantClausesFitFilterForm(user, query, null));

        query = new QueryImpl(new TerminalClauseImpl(
                "assignee", Operator.NOT_IN, new MultiValueOperand("admin", "santa")));
        assertFalse(transformer.doRelevantClausesFitFilterForm(user, query, null));
    }

    @Test
    public void testGetSearchClause()
    {
        setValues(
                UserSearchInput.currentUser(),
                UserSearchInput.empty(),
                UserSearchInput.group("administrators"),
                UserSearchInput.user("admin")
        );

        when(userFitsNavigatorHelper.checkUser("admin")).thenReturn("admin");
        transformer = new KickassUserSearchInputTransformer(searchConstants,
                userFitsNavigatorHelper, null, null, null);
        validateClause("assignee", transformer.getSearchClause(user, fieldValuesHolder));
    }

    @Test
    public void testGetSearchClauseCustomField()
    {
        setValues(
                UserSearchInput.currentUser(),
                UserSearchInput.empty(),
                UserSearchInput.group("administrators"),
                UserSearchInput.user("admin")
        );

        when(userFitsNavigatorHelper.checkUser("admin")).thenReturn("admin");
        when(customField.getName()).thenReturn("MyUser");
        when(customFieldInputHelper.getUniqueClauseName(user, "assignee", "MyUser")).thenReturn("MyUser");
        transformer = new KickassUserSearchInputTransformer(searchConstants,
                userFitsNavigatorHelper, null, null, null, customField, customFieldInputHelper);
        validateClause("MyUser", transformer.getSearchClause(user, fieldValuesHolder));
    }

    @Test
    public void testGetSearchClauseEmpty()
    {
        transformer = new KickassUserSearchInputTransformer(searchConstants,
                userFitsNavigatorHelper, null, null, null);
        assertNull(transformer.getSearchClause(user, fieldValuesHolder));
    }

    @Test
    public void testPopulateFromParams()
    {
        transformer = new KickassUserSearchInputTransformer(searchConstants,
                userFitsNavigatorHelper, null, null, null);
        transformer.populateFromParams(user, fieldValuesHolder,
                createParams("empty", "group:admins", "issue_current_user",
                        "user:admin", "asdf"));

        checkValues(4, Sets.newHashSet(
                UserSearchInput.currentUser(),
                UserSearchInput.empty(),
                UserSearchInput.group("admins"),
                UserSearchInput.user("admin")
        ));
    }

    @Test
    public void testPopulateFromParamsWithMultipleColons()
    {
        transformer = new KickassUserSearchInputTransformer(searchConstants,
                userFitsNavigatorHelper, null, null, null);
        transformer.populateFromParams(user, fieldValuesHolder,
                createParams("empty", "group:admins", "issue_current_user",
                        "user:<iframe src=\"http://www.google.com\"></iframe>", "asdf"));

        checkValues(4, Sets.newHashSet(
                UserSearchInput.currentUser(),
                UserSearchInput.empty(),
                UserSearchInput.group("admins"),
                UserSearchInput.user("<iframe src=\"http://www.google.com\"></iframe>")
        ));
    }

    @Test
    public void testPopulateFromParamsEmpty()
    {
        transformer = new KickassUserSearchInputTransformer(searchConstants,
                userFitsNavigatorHelper, null, null, null);
        transformer.populateFromParams(user, fieldValuesHolder, createParams());

        checkValues(0, null);
    }

    /**
     * Duplicate values should be ignored.
     */
    @Test
    public void testPopulateFromQueryDuplicates()
    {
        Operand currentUserOperand = new FunctionOperand(
                CurrentUserFunction.FUNCTION_CURRENT_USER);
        Operand groupOperand = new FunctionOperand(
                MembersOfFunction.FUNCTION_MEMBERSOF, "administrators");
        Operand userOperand = new SingleValueOperand("admin");
        Operand multiValueOperand = new MultiValueOperand(currentUserOperand,
                currentUserOperand, new EmptyOperand(), new EmptyOperand(),
                groupOperand, groupOperand, userOperand, userOperand);
        Query query = new QueryImpl(new TerminalClauseImpl(
                "assignee", Operator.IN, multiValueOperand));

        when(userFitsNavigatorHelper.checkUser("admin")).thenReturn("admin");
        transformer = new KickassUserSearchInputTransformer(searchConstants,
                userFitsNavigatorHelper, null, null, null);
        transformer.populateFromQuery(user, fieldValuesHolder, query, null);

        checkValues(4, Sets.newHashSet(
                UserSearchInput.currentUser(),
                UserSearchInput.empty(),
                UserSearchInput.group("administrators"),
                UserSearchInput.user("admin")
        ));
    }

    /**
     * We should be able to parse queries in the form `field = value`.
     */
    @Test
    public void testPopulateFromQueryEquals()
    {
        when(userFitsNavigatorHelper.checkUser("admin")).thenReturn("admin");
        Query query = new QueryImpl(new TerminalClauseImpl(
                "assignee", Operator.EQUALS, "admin"));

        transformer = new KickassUserSearchInputTransformer(searchConstants,
                userFitsNavigatorHelper, null, null, null);
        transformer.populateFromQuery(user, fieldValuesHolder, query, null);

        checkValues(1, Sets.newHashSet(
                UserSearchInput.user("admin")
        ));
    }

    /**
     * We should be able to parse queries in the form `field = currentUser()`.
     */
    @Test
    public void testPopulateFromQueryEqualsCurrentUser()
    {
        Operand currentUserOperand = new FunctionOperand(
                CurrentUserFunction.FUNCTION_CURRENT_USER);
        Query query = new QueryImpl(new TerminalClauseImpl(
                "assignee", Operator.EQUALS, currentUserOperand));

        transformer = new KickassUserSearchInputTransformer(searchConstants,
                userFitsNavigatorHelper, null, null, null);
        transformer.populateFromQuery(user, fieldValuesHolder, query, null);

        checkValues(1, Sets.newHashSet(
                UserSearchInput.currentUser()
        ));
    }

    /**
     * We should be able to parse queries in the form `field = EMPTY`.
     */
    @Test
    public void testPopulateFromQueryEqualsEmpty()
    {
        Query query = new QueryImpl(new TerminalClauseImpl(
                "assignee", Operator.EQUALS, new EmptyOperand()));

        transformer = new KickassUserSearchInputTransformer(searchConstants,
                userFitsNavigatorHelper, null, null, null);
        transformer.populateFromQuery(user, fieldValuesHolder, query, null);

        checkValues(1, Sets.newHashSet(
                UserSearchInput.empty()
        ));
    }

    /**
     * We should be able to parse queries in the form
     * `field IN membersOf(group)`.
     */
    @Test
    public void testPopulateFromQueryInGroup()
    {
        Operand membersOfOperand = new FunctionOperand(
                MembersOfFunction.FUNCTION_MEMBERSOF, "administrators");
        Query query = new QueryImpl(new TerminalClauseImpl(
                "assignee", Operator.IN, membersOfOperand));

        transformer = new KickassUserSearchInputTransformer(searchConstants,
                userFitsNavigatorHelper, null, null, null);
        transformer.populateFromQuery(user, fieldValuesHolder, query, null);

        checkValues(1, Sets.newHashSet(
                UserSearchInput.group("administrators")
        ));
    }

    /**
     * We should be able to parse queries in the form
     * `field IN (currentUser(), EMPTY, membersOf(group), value)`.
     */
    @Test
    public void testPopulateFromQueryInMultiple()
    {
        when(userFitsNavigatorHelper.checkUser("admin")).thenReturn("admin");
        Operand currentUserOperand = new FunctionOperand(
                CurrentUserFunction.FUNCTION_CURRENT_USER);
        Operand membersOfOperand = new FunctionOperand(
                MembersOfFunction.FUNCTION_MEMBERSOF, "administrators");
        Operand multiValueOperand = new MultiValueOperand(
                currentUserOperand, new EmptyOperand(), membersOfOperand,
                new SingleValueOperand("admin"));
        Query query = new QueryImpl(new TerminalClauseImpl(
                "assignee", Operator.IN, multiValueOperand));

        transformer = new KickassUserSearchInputTransformer(searchConstants,
                userFitsNavigatorHelper, null, null, null);
        transformer.populateFromQuery(user, fieldValuesHolder, query, null);

        checkValues(4, Sets.newHashSet(
                UserSearchInput.currentUser(),
                UserSearchInput.empty(),
                UserSearchInput.group("administrators"),
                UserSearchInput.user("admin")
        ));
    }

    /**
     * We should be able to parse queries in the form `field IS EMPTY`.
     */
    @Test
    public void testPopulateFromQueryIsEmpty()
    {
        Query query = new QueryImpl(new TerminalClauseImpl(
                "assignee", Operator.IS, new EmptyOperand()));

        transformer = new KickassUserSearchInputTransformer(searchConstants,
                userFitsNavigatorHelper, null, null, null);
        transformer.populateFromQuery(user, fieldValuesHolder, query, null);

        checkValues(1, Sets.newHashSet(
                UserSearchInput.empty()
        ));
    }

    private ActionParams createParams(String... values)
    {
        return new ActionParamsImpl(EasyMap.build(
                this.searchConstants.getFieldUrlParameter(), values));
    }

    /**
     * Checks that the correct number of values were parsed into
     * {@code fieldValuesHolder} and that the values themselves are correct.
     *
     * @param expectedSize The expected number of parsed values.
     * @param expectedValues The expected parsed values.
     */
    private void checkValues(int expectedSize,
            Set<UserSearchInput> expectedValues)
    {
        Collection<UserSearchInput> values = getValues();

        if (values != null)
        {
            assertEquals(expectedSize, values.size());
            assertEquals(expectedValues, Sets.newHashSet(values));
        }
        else
        {
            assertTrue(expectedSize == 0 && expectedValues == null);
        }
    }

    @SuppressWarnings("unchecked")
    private Collection<UserSearchInput> getValues()
    {
        String key = searchConstants.getFieldUrlParameter();
        return (Collection<UserSearchInput>)fieldValuesHolder.get(key);
    }

    private Set<UserSearchInput> getValuesSet()
    {
        Collection<UserSearchInput> values = getValues();
        if (values != null)
        {
            return new HashSet<UserSearchInput>(values);
        }
        else
        {
            return null;
        }
    }

    private void setValues(UserSearchInput... values)
    {
        fieldValuesHolder.put(searchConstants.getFieldUrlParameter(), values);
    }

    /**
     * Validates a clause against the contents of {@code fieldValuesHolder}.
     *
     * @param clause The clause that is to be validated.
     */
    private void validateClause(Clause clause)
    {
        Set<UserSearchInput> expected = getValuesSet();
        transformer.populateFromQuery(user, fieldValuesHolder,
                new QueryImpl(clause), null);

        checkValues(expected.size(), expected);
    }

    private void validateClause(String expectedClauseName, Clause clause)
    {
        assertEquals(expectedClauseName, clause.getName());
        validateClause(clause);
    }
}
