package com.atlassian.jira.mock;

import com.atlassian.jira.util.JiraUtilsBean;

public class MockJiraUtilsBean extends JiraUtilsBean
{
    private boolean publicMode;

    public void setPublicMode(boolean publicMode)
    {
        this.publicMode = publicMode;
    }

    public boolean isPublicMode()
    {
        return publicMode;
    }

    public Object loadComponent(String className, Class callingClass) throws ClassNotFoundException
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public Object loadComponent(Class componentClass)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
