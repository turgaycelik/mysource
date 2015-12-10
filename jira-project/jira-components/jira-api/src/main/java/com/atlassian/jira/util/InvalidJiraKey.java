package com.atlassian.jira.util;

/**
* @since v6.1
*/
class InvalidJiraKey extends AbstractJiraKey
{
    InvalidJiraKey()
    {
        super(null, -1);
    }

    @Override
    public boolean isValid()
    {
        return false;
    }
}
