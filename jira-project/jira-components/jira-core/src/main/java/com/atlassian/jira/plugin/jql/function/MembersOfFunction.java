package com.atlassian.jira.plugin.jql.function;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Performs the validation and value generation for the MembersOf function.
 * <p>
 * The function takes exactly one argument which is the name of the group to retrieve the members from.
 * <p>
 * The function is case-insensitive: if there are two groups named <code>jira-users</code> and <code>JIRA-USERS</code>,
 * then a search for <code>membersOf("jira-USERS")</code> will return members from both.
 *
 * @since v4.0
 */
public class MembersOfFunction extends AbstractJqlFunction
{
    public static final String FUNCTION_MEMBERSOF = "membersOf";
    private static final int EXPECTED_ARGS = 1;

    private final UserUtil userUtil;
    private final CrowdService crowdService;

    public MembersOfFunction(UserUtil userUtil, CrowdService crowdService)
    {
        this.userUtil = userUtil;
        this.crowdService = crowdService;
    }

    public MessageSet validate(final User searcher, final FunctionOperand functionOperand, final TerminalClause terminalClause)
    {
        //We don't do any permissions checks here. 3.x allowed users to search on groups they where not members of
        //provided they knew its name. As such, we need to allow this function to return groups that user is not a
        //member of.

        final I18nHelper i18n = getI18n();
        MessageSet messages = validateNumberOfArgs(functionOperand, EXPECTED_ARGS);
        if (!messages.hasAnyErrors())
        {
            final String groupName = functionOperand.getArgs().get(0);
            final Group group = getGroupsIgnoreCase(groupName);
            if (group == null)
            {
                messages.addErrorMessage(i18n.getText("jira.jql.group.no.such.group", functionOperand.getName(), groupName));
            }
        }
        return messages;
    }

    public List<QueryLiteral> getValues(final QueryCreationContext queryCreationContext, final FunctionOperand operand, final TerminalClause terminalClause)
    {
        if (!operand.getArgs().isEmpty())
        {
            final String groupName = operand.getArgs().get(0);
            final Group group = getGroupsIgnoreCase(groupName);

            final Set<QueryLiteral> usernames = new LinkedHashSet<QueryLiteral>();
            if (group != null)
            {
                Collections.singletonList(group);
                final List<Group> groups = Collections.singletonList(group);
                final Set<User> users = userUtil.getAllUsersInGroups(groups);
                if (users != null)
                {
                    for (User user : users)
                    {
                        usernames.add(new QueryLiteral(operand, user.getName()));
                    }
                }
            }

            return new ArrayList<QueryLiteral>(usernames);
        }

        // invalid input will simply return no results
        return Collections.emptyList();
    }

    private Group getGroupsIgnoreCase(final String groupName)
    {
        // Crowd is always case inseneitive for search
        return crowdService.getGroup(groupName);
    }

    public int getMinimumNumberOfExpectedArguments()
    {
        return EXPECTED_ARGS;
    }

    public JiraDataType getDataType()
    {
        return JiraDataTypes.USER;
    }
}
