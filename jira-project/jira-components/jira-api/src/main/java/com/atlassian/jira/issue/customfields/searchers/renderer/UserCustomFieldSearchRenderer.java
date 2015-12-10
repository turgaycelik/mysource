package com.atlassian.jira.issue.customfields.searchers.renderer;

import com.atlassian.annotations.Internal;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.CustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.util.UserSearcherHelper;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.plugin.customfield.CustomFieldSearcherModuleDescriptor;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.web.FieldVisibilityManager;
import webwork.action.Action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @since v5.2
 */
@Internal
public class UserCustomFieldSearchRenderer extends CustomFieldRenderer
{
    private final CustomField field;
    private final UserSearcherHelper userSearcherHelper;
    private final UserUtil userUtil;

    public UserCustomFieldSearchRenderer(ClauseNames clauseNames, CustomFieldSearcherModuleDescriptor customFieldSearcherModuleDescriptor, CustomField field, CustomFieldValueProvider customFieldValueProvider, FieldVisibilityManager fieldVisibilityManager)
    {
        super(clauseNames, customFieldSearcherModuleDescriptor, field, customFieldValueProvider, fieldVisibilityManager);
        this.field = field;
        userSearcherHelper = ComponentAccessor.getComponent(UserSearcherHelper.class);
        userUtil = ComponentAccessor.getComponent(UserUtil.class);
    }

    @Override
    public String getEditHtml(final User user, final SearchContext searchContext, final FieldValuesHolder fieldValuesHolder, final Map<?, ?> displayParameters, final Action action)
    {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("isKickass", true);
        final CustomFieldParams customFieldParams = (CustomFieldParams) fieldValuesHolder.get(field.getId());
        ArrayList<String> usernames = new ArrayList<String>();
        if (customFieldParams != null)
        {
            usernames = new ArrayList<String>(customFieldParams.getValuesForNullKey());
            final List<User> users = transformUserInput(usernames);
            params.put("values", users);
        }
        userSearcherHelper.addUserSuggestionParams(user, usernames, params);
        return super.getEditHtml(searchContext, fieldValuesHolder, displayParameters, action, params);
    }

    public List<User> transformUserInput(Collection<String> values)
    {
        List<User> users = new ArrayList<User>();
        for (String username : values)
        {
            final User user = userUtil.getUser(username);
            if (user != null) {
                users.add(user);
            }
        }
        return  users;
    }
}
