package com.atlassian.jira.jql.values;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;

import java.util.ArrayList;
import java.util.List;

/**
 * Gets all the users for the specified value.
 *
 * @since v4.0
 */
public class UserClauseValuesGenerator implements ClauseValuesGenerator
{
    private final UserPickerSearchService userPickerSearchService;

    public UserClauseValuesGenerator(final UserPickerSearchService userPickerSearchService)
    {
        this.userPickerSearchService = userPickerSearchService;
    }

    public Results getPossibleValues(final User searcher, final String jqlClauseName, final String valuePrefix, final int maxNumResults)
    {
        List<Result> userValues = new ArrayList<Result>();
        final JiraServiceContextImpl serviceContext = new JiraServiceContextImpl(searcher);
        if (userPickerSearchService.canPerformAjaxSearch(serviceContext))
        {
            final List<User> users = userPickerSearchService.findUsersAllowEmptyQuery(serviceContext, valuePrefix);
            for (User user : users)
            {
                if (userValues.size() == maxNumResults)
                {
                    break;
                }
                final String fullName = user.getDisplayName();
                final String name = user.getName();
                final String email = user.getEmailAddress();
                userValues.add(new Result(name, new String [] {fullName, "- " + email,  " (" + name + ")"}));
            }
        }
        return new Results(userValues);
    }
}
