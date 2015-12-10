package com.atlassian.jira.issue.customfields.searchers.transformer;

import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.constants.UserFieldSearchConstants;
import com.atlassian.jira.issue.search.searchers.transformer.KickassUserSearchInputTransformer;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.search.searchers.util.UserFitsNavigatorHelper;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.UserHistoryManager;
import com.atlassian.jira.user.util.UserManager;

/**
 * @since 5.2
 */
public class UserPickerSearcherInputTransformerFactoryImpl implements UserPickerSearcherInputTransformerFactory
{
    private final GroupManager groupManager;
    private final UserHistoryManager userHistoryManager;
    private final UserManager userManager;

    public UserPickerSearcherInputTransformerFactoryImpl(final UserHistoryManager userHistoryManager,
            final GroupManager groupManager,
            final UserManager userManager)
    {
        this.groupManager = groupManager;
        this.userHistoryManager = userHistoryManager;
        this.userManager = userManager;
    }

    @Override
    public SearchInputTransformer create(UserFieldSearchConstants searchConstants, CustomField field,
            UserFitsNavigatorHelper userFitsNavigatorHelper)
    {
        return new KickassUserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, groupManager,
                    userManager, userHistoryManager);
    }
}
