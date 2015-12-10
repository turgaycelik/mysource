package com.atlassian.jira.issue.search.searchers.transformer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.UserFieldSearchConstants;
import com.atlassian.jira.issue.search.constants.UserFieldSearchConstantsWithEmpty;
import com.atlassian.jira.issue.search.searchers.util.UserFitsNavigatorHelper;
import com.atlassian.jira.issue.transport.ActionParams;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.plugin.jql.function.CurrentUserFunction;
import com.atlassian.jira.plugin.jql.function.MembersOfFunction;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.UserHistoryItem;
import com.atlassian.jira.user.UserHistoryManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A search input transformer for user/group fields with Kickass modifications.
 *
 * @since v5.2
 */
public class KickassUserSearchInputTransformer extends UserSearchInputTransformer
{
    public KickassUserSearchInputTransformer(
            UserFieldSearchConstantsWithEmpty searchConstants,
            final UserFitsNavigatorHelper userFitsNavigatorHelper,
            GroupManager groupManager, UserManager userManager, UserHistoryManager userHistoryManager)
    {
        super(searchConstants, userFitsNavigatorHelper, groupManager, userManager, userHistoryManager);
    }

    public KickassUserSearchInputTransformer(
            UserFieldSearchConstants searchConstants,
            final UserFitsNavigatorHelper userFitsNavigatorHelper,
            GroupManager groupManager, UserManager userManager, UserHistoryManager userHistoryManager)
    {
        super(searchConstants, userFitsNavigatorHelper, groupManager, userManager, userHistoryManager);
    }

    public KickassUserSearchInputTransformer(
            UserFieldSearchConstants searchConstants,
            final UserFitsNavigatorHelper userFitsNavigatorHelper,
            GroupManager groupManager, UserManager userManager, UserHistoryManager userHistoryManager,
            CustomField customField, CustomFieldInputHelper customFieldInputHelper)
    {
        super(searchConstants, userFitsNavigatorHelper, groupManager, userManager, userHistoryManager,
                customField, customFieldInputHelper);
    }

    /**
     * Populates a {@link FieldValuesHolder} with {@link ActionParams}.
     *
     * Values that start with "user:" refer to users, values that start with
     * "group:" refers to groups, and "empty" refers to empty values.
     */
    @Override
    public void populateFromParams(final User user,
            final FieldValuesHolder fieldValuesHolder,
            final ActionParams actionParams)
    {
        // For backwards compatability with old urls (eg assigneeSelect=issue_current_user), check selectUrlParameter
        String[] selectValues = actionParams.getValuesForKey(searchConstants.getSelectUrlParameter());
        if (null != selectValues && selectValues.length > 0)
        {
            super.populateFromParams(user, fieldValuesHolder, actionParams);
            return;
        }

        String paramName = searchConstants.getFieldUrlParameter();
        Set<UserSearchInput> newValues = getFromParams(actionParams, paramName);
        fieldValuesHolder.put(paramName, newValues);

        if (actionParams.containsKey("check_prev_" + paramName)) { // if there are no prev_ params, it prob wasn't a user-submitted form
            Set<UserSearchInput> prevValues = getFromParams(actionParams, "prev_" + paramName);
            updateUsedUsers(user, newValues, prevValues);
        }
    }

    private Set<UserSearchInput> getFromParams(ActionParams actionParams, String paramName)
    {
        String[] params = actionParams.getValuesForKey(paramName);
        Set<UserSearchInput> values = new HashSet<UserSearchInput>();

        if (params != null)
        {
            for (String param : params)
            {
                String[] parts = param.split(":", 2);
                if (parts[0].equals("empty"))
                {
                    values.add(UserSearchInput.empty());
                }
                else if (parts[0].equals("group"))
                {
                    values.add(UserSearchInput.group(parts[1]));
                }
                else if (parts[0].equals("issue_current_user"))
                {
                    values.add(UserSearchInput.currentUser());
                }
                else if (parts[0].equals("user"))
                {
                    values.add(UserSearchInput.user(parts[1]));
                }
            }
        }
        return values;
    }
    
    private void updateUsedUsers(User remoteUser, Set<UserSearchInput> newValues, Set<UserSearchInput> prevValues)
    {
        for (UserSearchInput input : Sets.difference(newValues, prevValues))
        {
            if (input.isUser())
            {
                final User user = userManager.getUser(input.getValue());
                if (user != null)
                {
                    userHistoryManager.addUserToHistory(UserHistoryItem.USED_USER, remoteUser, user);
                }
            }
        }
    }

    

    @Override
    public void validateParams(final User user,
            final SearchContext searchContext,
            final FieldValuesHolder fieldValuesHolder,
            final I18nHelper i18nHelper, final ErrorCollection errors)
    {
    }

    /**
     * Populates a {@link FieldValuesHolder} from a {@link Query}.
     */
    @Override
    public void populateFromQuery(final User user,
            final FieldValuesHolder fieldValuesHolder,
            final Query query, final SearchContext searchContext)
    {
        if (query == null)
        {
            return;
        }

        List<TerminalClause> clauses = getMatchingClauses(
                searchConstants.getJqlClauseNames().getJqlFieldNames(), query);
        Set<UserSearchInput> values =
                new HashSet<UserSearchInput>();

        for (TerminalClause clause : clauses)
        {
            parseOperand(clause.getOperand(), values);
        }

        fieldValuesHolder.put(searchConstants.getFieldUrlParameter(), values);
    }

    /**
     * Convert the user's input into a JQL clause. Always in the form:
     *
     *    field IN (user1, user2, membersOf(group1), membersOf(group2), ...)
     */
    @Override
    public Clause getSearchClause(final User user,
            final FieldValuesHolder fieldValuesHolder)
    {
        if (fieldValuesHolder.containsKey(searchConstants.getSelectUrlParameter()))
        {
            return super.getSearchClause(user, fieldValuesHolder);
        }
        Collection<Operand> operands = new ArrayList<Operand>();
        Collection<UserSearchInput> values =
                (Collection<UserSearchInput>)fieldValuesHolder.get(
                        searchConstants.getFieldUrlParameter());

        if (values != null)
        {
            for (UserSearchInput value : values)
            {
                if (value.isCurrentUser())
                {
                    String name = CurrentUserFunction.FUNCTION_CURRENT_USER;
                    operands.add(new FunctionOperand(name));
                }
                else if (value.isEmpty())
                {
                    operands.add(EmptyOperand.EMPTY);
                }
                else if (value.isGroup())
                {
                    String name = MembersOfFunction.FUNCTION_MEMBERSOF;
                    operands.add(new FunctionOperand(name, value.getValue()));
                }
                if (value.isUser())
                {
                    operands.add(new SingleValueOperand(value.getValue()));
                }
            }
        }

        if (!operands.isEmpty())
        {
            String clauseName = getClauseName(user);
            return new TerminalClauseImpl(clauseName, Operator.IN,
                    new MultiValueOperand(operands));
        }
        else
        {
            return null;
        }
    }

    /**
     * Determines whether the given query can be represented in basic mode.
     */
    @Override
    public boolean doRelevantClausesFitFilterForm(final User user,
            final Query query, final SearchContext searchContext)
    {
        if (query == null || query.getWhereClause() == null)
        {
            return true;
        }

        SimpleNavigatorCollectorVisitor visitor = createSimpleNavigatorCollectorVisitor();
        Clause whereClause = query.getWhereClause();
        whereClause.accept(visitor);

        // If we have multiple terminal clauses or the visitor determines that
        // this query can't be represented in the old navigator, it's bad!
        List<TerminalClause> clauses = visitor.getClauses();
        if (clauses.size() == 0)
        {
            return true;
        }
        else if (clauses.size() > 1 || !visitor.isValid())
        {
            return false;
        }

        return checkClause(clauses.get(0), user);
    }

    /**
     * Checks whether a {@link TerminalClause} can be represented in basic mode.
     *
     * @param clause The clause.
     * @param user The user executing the search.
     */
    private boolean checkClause(TerminalClause clause, User user)
    {
        Operator operator = clause.getOperator();
        Operand operand = clause.getOperand();

        if (operand instanceof SingleValueOperand)
        {
            return checkSingleValueClause(operator,
                    (SingleValueOperand)operand);
        }
        else if (operand instanceof FunctionOperand)
        {
            return checkFunctionClause(operator, (FunctionOperand)operand,
                    user);
        }
        else if (operand instanceof MultiValueOperand)
        {
            return checkMultiValueClause(operator, (MultiValueOperand)operand,
                    user);
        }
        else if (operand instanceof EmptyOperand && emptySelectFlag != null &&
                (operator == Operator.EQUALS || operator == Operator.IS))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Checks whether a {@link TerminalClause} with a {@link SingleValueOperand}
     * can be represented in basic mode.
     */
    private boolean checkSingleValueClause(Operator operator,
            SingleValueOperand operand)
    {
        String value = operand.getStringValue();
        if (value == null)
        {
            value = operand.getLongValue().toString();
        }

        //For it to fit the operator it must be equals, the user must be a username not a fullname or email and the user
        //must exist.
        return operator == Operator.EQUALS &&
                userFitsNavigatorHelper.checkUser(value) != null &&
                userManager.getUserByKey(value) != null;
    }

    /**
     * Checks whether a {@link TerminalClause} with a {@link FunctionOperand}
     * can be represented in basic mode.
     */
    private boolean checkFunctionClause(Operator operator,
            FunctionOperand operand, User user)
    {
        if (operator == Operator.EQUALS && isCurrentUser(operand) &&
                isUserLoggedIn(user))
        {
            // field = currentUser()
            return true;
        }
        else if (operator == Operator.IN && isMembersOf(operand))
        {
            // field IN membersOf(group)
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Checks whether a {@link TerminalClause} with a {@link MultiValueOperand}
     * can be represented in basic mode.
     */
    private boolean checkMultiValueClause(Operator operator,
            MultiValueOperand operand, User user)
    {
        if (operator != Operator.IN)
        {
            return false;
        }
        for (Operand value : operand.getValues())
        {
            if (value instanceof SingleValueOperand)
            {
                // We just want to validate the user, so pass EQUALS.
                if (!checkSingleValueClause(Operator.EQUALS,
                        (SingleValueOperand)value))
                {
                    return false;
                }
            }
            else if (value instanceof FunctionOperand)
            {
                boolean isCurrentUser = isUserLoggedIn(user) &&
                        isCurrentUser((FunctionOperand)value);

                if (!isCurrentUser && !isMembersOf((FunctionOperand)value))
                {
                    return false;
                }
            }
            else if (!(value instanceof EmptyOperand))
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Extract the values (i.e. group/user) referenced in the operand.
     *
     * @param operand The operand from which values are to be extracted.
     * @param values The collection to add the extracted values to.
     */
    private void parseOperand(Operand operand,
            Collection<UserSearchInput> values)
    {
        if (operand instanceof EmptyOperand)
        {
            values.add(UserSearchInput.empty());
        }
        else if (operand instanceof SingleValueOperand)
        {
            parseSingleValueOperand((SingleValueOperand)operand, values);
        }
        else if (operand instanceof FunctionOperand)
        {
            parseFunctionOperand((FunctionOperand)operand, values);
        }
        else if (operand instanceof MultiValueOperand)
        {
            MultiValueOperand multiValueOperand = (MultiValueOperand)operand;
            for (Operand value : multiValueOperand.getValues())
            {
                parseOperand(value, values);
            }
        }
    }

    /**
     * Extract the value (e.g. user) referenced in a {@link SingleValueOperand}.
     *
     * @param operand The operand from which the value is to be extracted.
     * @param values The collection to add the extracted value to.
     */
    private void parseSingleValueOperand(SingleValueOperand operand,
            Collection<UserSearchInput> values)
    {
        String value = operand.getStringValue();
        if (value == null)
        {
            value = operand.getLongValue().toString();
        }

        value = userFitsNavigatorHelper.checkUser(value);
        if (value != null)
        {
            values.add(UserSearchInput.user(value));
        }
    }

    /**
     * Extract the value (e.g. group/user) referenced in a
     * {@link FunctionOperand}.
     *
     * @param operand The operand from which the value is extracted.
     * @param values The collection to add the extracted value to.
     */
    private void parseFunctionOperand(FunctionOperand operand,
            Collection<UserSearchInput> values)
    {
        if (isCurrentUser(operand))
        {
            values.add(UserSearchInput.currentUser());
        }
        else if (isMembersOf(operand))
        {
            values.add(UserSearchInput.group(operand.getArgs().get(0)));
        }
    }

    /**
     * Determines whether a {@link FunctionOperand} represents a valid
     * "currentUser()" function.
     */
    private boolean isCurrentUser(FunctionOperand operand)
    {
        String name = CurrentUserFunction.FUNCTION_CURRENT_USER;
        return operand.getName().equalsIgnoreCase(name);
    }

    /**
     * Determines whether a {@link FunctionOperand} represents a valid
     * "membersOf(group)" function.
     */
    private boolean isMembersOf(FunctionOperand operand)
    {
        String name = MembersOfFunction.FUNCTION_MEMBERSOF;
        return operand.getName().equalsIgnoreCase(name) &&
                operand.getArgs().size() == 1;
    }
}
