package com.atlassian.jira.issue.customfields.impl.rest;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.issue.fields.rest.json.beans.GroupJsonBean;
import com.google.common.base.Function;

import javax.annotation.Nullable;

/**
 * Handy functions for dealing with groups.
 *
 * @since v5.0
 */
class GroupFunctions
{
    /**
     * Extracts the Group's name.
     */
    static final Function<Group, String> GROUP_TO_NAME = new Function<Group, String>()
    {
        @Override
        public String apply(@Nullable Group group)
        {
            return group != null ? group.getName() : null;
        }
    };

    /**
     * Extracts the GroupJsonBean's name.
     */
    public static final Function<GroupJsonBean, String> GROUP_BEAN_TO_NAME = new Function<GroupJsonBean, String>()
    {
        @Override
        public String apply(@Nullable GroupJsonBean group)
        {
            return group != null ? group.getName() : null;
        }
    };
}
