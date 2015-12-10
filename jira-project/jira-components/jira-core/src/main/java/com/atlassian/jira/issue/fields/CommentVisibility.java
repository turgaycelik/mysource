package com.atlassian.jira.issue.fields;

import java.util.Map;

/**
 * Represents the visibility of the comment to roles or groups of users.
 */
public class CommentVisibility
{
    private String commentLevel;
    private static final String ROLE_PREFIX = "role:";
    private static final String GROUP_PREFIX = "group:";

    /**
     * Gets a commentLevel string out of a map and then will tell you if the
     * provided param is a role or group level visiblity param.
     */
    public CommentVisibility(Map params, String paramName)
    {
        String[] value;
        value = (String[]) params.get(paramName);
        if (value != null && value.length > 0)
        {
            commentLevel = value[0];
            // transform empty string to null
            if (commentLevel != null && "".equals(commentLevel.trim()))
            {
                commentLevel = null;
            }
        }
    }

    /**
     * Digests the passed in commentLevel string, will look something like
     * 'role:ProjectRole' or 'group:jira-users'.
     * @param commentLevel
     */
    public CommentVisibility(String commentLevel)
    {
        this.commentLevel = commentLevel;
    }

    /**
     * Gets the role level from the commentLevel if it is of type role, null
     * otherwise.
     * @return role level
     */
    public String getRoleLevel()
    {
        if (commentLevel != null && commentLevel.startsWith(ROLE_PREFIX)) {
            return commentLevel.substring(ROLE_PREFIX.length());
        }
        return null;
    }

    /**
     * Gets the group level from the commentLevel if it is of type group, null
     * otherwise.
     * @return group level
     */
    public String getGroupLevel()
    {
        if (commentLevel != null && commentLevel.startsWith(GROUP_PREFIX)) {
            return commentLevel.substring(GROUP_PREFIX.length());
        }
        return null;
    }

    public static String getCommentLevelFromLevels(String groupLevel, Long roleLevelId)
    {
        if (groupLevel != null)
        {
            return GROUP_PREFIX + groupLevel;
        }
        else if (roleLevelId != null)
        {
            return ROLE_PREFIX + roleLevelId;
        }
        return null;
    }

    public static String getRoleLevelWithPrefix(String roleLevel)
    {
        return ROLE_PREFIX + roleLevel;
    }
}
