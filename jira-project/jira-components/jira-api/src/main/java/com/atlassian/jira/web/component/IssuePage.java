package com.atlassian.jira.web.component;

public interface IssuePage
{
    public boolean isCurrentPage();
    public int getPageNumber();
    public int getStart();
}
