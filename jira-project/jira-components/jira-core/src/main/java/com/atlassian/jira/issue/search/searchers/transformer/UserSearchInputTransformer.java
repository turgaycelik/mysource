package com.atlassian.jira.issue.search.searchers.transformer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.UserFieldSearchConstants;
import com.atlassian.jira.issue.search.constants.UserFieldSearchConstantsWithEmpty;
import com.atlassian.jira.issue.search.searchers.impl.NamedTerminalClauseCollectingVisitor;
import com.atlassian.jira.issue.search.searchers.util.UserFitsNavigatorHelper;
import com.atlassian.jira.issue.transport.ActionParams;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.plugin.jql.function.CurrentUserFunction;
import com.atlassian.jira.plugin.jql.function.MembersOfFunction;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.UserHistoryManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.ParameterUtils;
import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * An search input transformer for issue type
 *
 * @since v4.0
 */
public class UserSearchInputTransformer implements SearchInputTransformer
{
    private static final Logger log = Logger.getLogger(UserSearchInputTransformer.class);

    protected final UserHistoryManager userHistoryManager;
    protected final UserFieldSearchConstants searchConstants;
    protected final UserFitsNavigatorHelper userFitsNavigatorHelper;
    protected final GroupManager groupManager;
    protected final UserManager userManager;
    protected final String emptySelectFlag;
    private final CustomField customField;
    private final CustomFieldInputHelper customFieldInputHelper;

    public UserSearchInputTransformer(UserFieldSearchConstantsWithEmpty searchConstants,
            final UserFitsNavigatorHelper userFitsNavigatorHelper, GroupManager groupManager, UserManager userManager,
            UserHistoryManager userHistoryManager)
    {
        this(groupManager, userManager, searchConstants.getEmptySelectFlag(), searchConstants, userFitsNavigatorHelper,
                userHistoryManager, null, null);
    }

    public UserSearchInputTransformer(UserFieldSearchConstants searchConstants,
            final UserFitsNavigatorHelper userFitsNavigatorHelper, GroupManager groupManager, UserManager userManager,
            UserHistoryManager userHistoryManager)
    {
        this(groupManager, userManager, null, searchConstants, userFitsNavigatorHelper,
                userHistoryManager, null, null);
    }

    public UserSearchInputTransformer(UserFieldSearchConstants searchConstants,
            final UserFitsNavigatorHelper userFitsNavigatorHelper, GroupManager groupManager, UserManager userManager,
            UserHistoryManager userHistoryManager, CustomField customField, CustomFieldInputHelper customFieldInputHelper)
    {
        this(groupManager, userManager, null, searchConstants, userFitsNavigatorHelper,
                userHistoryManager, customField, customFieldInputHelper);
    }

    protected UserSearchInputTransformer(GroupManager groupManager, UserManager userManager, String emptySelectFlag,
            UserFieldSearchConstants searchConstants, UserFitsNavigatorHelper userFitsNavigatorHelper,
            UserHistoryManager userHistoryManager, CustomField customField, CustomFieldInputHelper customFieldInputHelper)
    {
        this.groupManager = groupManager;
        this.userManager = userManager;
        this.emptySelectFlag = emptySelectFlag;
        this.searchConstants = searchConstants;
        this.userFitsNavigatorHelper = userFitsNavigatorHelper;
        this.userHistoryManager = userHistoryManager;
        this.customField = customField;
        this.customFieldInputHelper = customFieldInputHelper;
    }

    public void populateFromParams(final User user, final FieldValuesHolder fieldValuesHolder, final ActionParams actionParams)
    {
        fieldValuesHolder.put(searchConstants.getSelectUrlParameter(), actionParams.getFirstValueForKey(searchConstants.getSelectUrlParameter()));

        // If no user select was selected but the user field was, then assume that it's a user search
        if (actionParams.containsKey(searchConstants.getFieldUrlParameter()) && !actionParams.containsKey(searchConstants.getSelectUrlParameter()))
        {
            fieldValuesHolder.put(searchConstants.getSelectUrlParameter(), searchConstants.getSpecificUserSelectFlag());
        }

        fieldValuesHolder.put(searchConstants.getFieldUrlParameter(), actionParams.getFirstValueForKey(searchConstants.getFieldUrlParameter()));
    }

    public void validateParams(final User searcher, final SearchContext searchContext, final FieldValuesHolder fieldValuesHolder, final I18nHelper i18nHelper, final ErrorCollection errors)
    {
        final String user = (String) fieldValuesHolder.get(searchConstants.getFieldUrlParameter());
        if (StringUtils.isNotBlank(user))
        {
            final String userTypeSelectList = (String) fieldValuesHolder.get(searchConstants.getSelectUrlParameter());
            if (searchConstants.getSpecificUserSelectFlag().equals(userTypeSelectList))
            {
                if (!userExists(user))
                {
                    errors.addError(searchConstants.getFieldUrlParameter(), i18nHelper.getText("admin.errors.could.not.find.username", user));
                }
            }
            else if (searchConstants.getSpecificGroupSelectFlag().equals(userTypeSelectList))
            {
                if (!groupExists(user))
                {
                    errors.addError(searchConstants.getFieldUrlParameter(), i18nHelper.getText("admin.errors.abstractusersearcher.could.not.find.group", user));
                }
            }
        }
    }

    public void populateFromQuery(final User searcher, final FieldValuesHolder fieldValuesHolder, final Query query, final SearchContext searchContext)
    {
        if (query == null)
        {
            return;
        }

        List<TerminalClause> clauses = getMatchingClauses(searchConstants.getJqlClauseNames().getJqlFieldNames(), query);

        for (TerminalClause clause : clauses)
        {
            Operand operand = clause.getOperand();
            if (operand instanceof SingleValueOperand)
            {
                SingleValueOperand svop = (SingleValueOperand)operand;
                String stringValue = svop.getStringValue() == null ? svop.getLongValue().toString() : svop.getStringValue();
                String user = userFitsNavigatorHelper.checkUser(stringValue);
                if (user != null)
                {
                    fieldValuesHolder.put(searchConstants.getFieldUrlParameter(), user);
                    fieldValuesHolder.put(searchConstants.getSelectUrlParameter(), searchConstants.getSpecificUserSelectFlag());
                }
            }
            else if (operand instanceof FunctionOperand)
            {
                FunctionOperand fop = (FunctionOperand)operand;
                if (MembersOfFunction.FUNCTION_MEMBERSOF.equalsIgnoreCase(fop.getName()) && fop.getArgs().size() == 1)
                {
                    String group = fop.getArgs().get(0);
                    fieldValuesHolder.put(searchConstants.getFieldUrlParameter(), group);
                    fieldValuesHolder.put(searchConstants.getSelectUrlParameter(), searchConstants.getSpecificGroupSelectFlag());
                }
                else if (CurrentUserFunction.FUNCTION_CURRENT_USER.equalsIgnoreCase(fop.getName()))
                {
                    fieldValuesHolder.put(searchConstants.getSelectUrlParameter(), searchConstants.getCurrentUserSelectFlag());
                }
            }
            else if (operand instanceof EmptyOperand && emptySelectFlag != null)
            {
                fieldValuesHolder.put(searchConstants.getSelectUrlParameter(), emptySelectFlag);
            }
            else
            {
                log.warn("Operand '" + operand + "' cannot be processed in navigator for query '" + query + "'.");
            }
        }
    }

    public boolean doRelevantClausesFitFilterForm(final User searcher, final Query query, final SearchContext searchContext)
    {
        if (query != null && query.getWhereClause() != null)
        {
            SimpleNavigatorCollectorVisitor visitor = createSimpleNavigatorCollectorVisitor();
            Clause whereClause = query.getWhereClause();
            whereClause.accept(visitor);

            final List<TerminalClause> clauses = visitor.getClauses();
            if (clauses.size() == 0)
            {
                return true;
            }
            else if (clauses.size() != 1 || !visitor.isValid())
            {
                return false;
            }

            TerminalClause clause = clauses.get(0);
            Operator operator = clause.getOperator();
            Operand operand = clause.getOperand();
            if (operand instanceof SingleValueOperand)
            {
                if (operator == Operator.EQUALS)
                {
                    SingleValueOperand svop = (SingleValueOperand)operand;
                    String user = svop.getStringValue() == null ? svop.getLongValue().toString() : svop.getStringValue();
                    return userFitsNavigatorHelper.checkUser(user) != null;
                }
                else
                {
                    return false;
                }
            }
            else if (operand instanceof FunctionOperand)
            {
                FunctionOperand fop = (FunctionOperand)operand;
                if (MembersOfFunction.FUNCTION_MEMBERSOF.equalsIgnoreCase(fop.getName()) && fop.getArgs().size() == 1 && operator == Operator.IN)
                {
                    return true;
                }
                else if (CurrentUserFunction.FUNCTION_CURRENT_USER.equalsIgnoreCase(fop.getName()) && operator == Operator.EQUALS && isUserLoggedIn(searcher))
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
            else if (operand instanceof EmptyOperand && emptySelectFlag != null && (operator == Operator.EQUALS || operator == Operator.IS))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return true;
        }
    }

    public Clause getSearchClause(final User searcher, final FieldValuesHolder fieldValuesHolder)
    {
        final String clauseName = getClauseName(searcher);
        if (emptySelectFlag != null && ParameterUtils.paramContains(fieldValuesHolder, searchConstants.getSelectUrlParameter(), emptySelectFlag))
        {
            return new TerminalClauseImpl(clauseName, Operator.IS, new EmptyOperand());
        }
        else if (ParameterUtils.paramContains(fieldValuesHolder, searchConstants.getSelectUrlParameter(), searchConstants.getCurrentUserSelectFlag()))
        {
            return new TerminalClauseImpl(clauseName, Operator.EQUALS, new FunctionOperand(CurrentUserFunction.FUNCTION_CURRENT_USER));
        }
        //We do a specific user search when either the type is specifed or (when there is no search type and there is a value).
        else if (fieldValuesHolder.containsKey(searchConstants.getFieldUrlParameter()) && (!fieldValuesHolder.containsKey(searchConstants.getSelectUrlParameter()) || (ParameterUtils.paramContains(fieldValuesHolder, searchConstants.getSelectUrlParameter(), searchConstants.getSpecificUserSelectFlag()))))
        {
            final String user = ParameterUtils.getStringParam(fieldValuesHolder, searchConstants.getFieldUrlParameter());
            return new TerminalClauseImpl(clauseName, Operator.EQUALS, user);
        }
        else if (ParameterUtils.paramContains(fieldValuesHolder, searchConstants.getSelectUrlParameter(), searchConstants.getSpecificGroupSelectFlag()) && fieldValuesHolder.containsKey(searchConstants.getFieldUrlParameter()))
        {
            final String group = ParameterUtils.getStringParam(fieldValuesHolder, searchConstants.getSelectUrlParameter(), searchConstants.getSpecificUserSelectFlag(), searchConstants.getFieldUrlParameter());
            return new TerminalClauseImpl(clauseName, Operator.IN, new FunctionOperand(MembersOfFunction.FUNCTION_MEMBERSOF, group));
        }
        else
        {
            return null;
        }
    }

    protected String getClauseName(User user)
    {
        if (null == customField)
        {
            return searchConstants.getJqlClauseNames().getPrimaryName();
        }
        else
        {
            return customFieldInputHelper.getUniqueClauseName(user, searchConstants.getJqlClauseNames().getPrimaryName(), customField.getName());
        }
    }

    protected List<TerminalClause> getMatchingClauses(final Set<String> jqlClauseNames, final Query query)
    {
        final NamedTerminalClauseCollectingVisitor clauseVisitor = new NamedTerminalClauseCollectingVisitor(jqlClauseNames);
        if(query.getWhereClause() != null)
        {
            query.getWhereClause().accept(clauseVisitor);
            return clauseVisitor.getNamedClauses();
        }
        return Collections.emptyList();
    }

    ///CLOVER:OFF
    boolean isUserLoggedIn(final User user)
    {
        return user != null;
    }

    boolean groupExists(final String user)
    {
        return groupManager.groupExists(user);
    }

    boolean userExists(final String user)
    {
        return userManager.getUserObject(user) != null;
    }

    SimpleNavigatorCollectorVisitor createSimpleNavigatorCollectorVisitor()
    {
        return new SimpleNavigatorCollectorVisitor(searchConstants.getJqlClauseNames().getJqlFieldNames());
    }
    ///CLOVER:ON
}
