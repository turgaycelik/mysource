package com.atlassian.jira.web.component;

import java.util.List;

/**
 * This class is used in conjunction with issuetable.vm, and {@link IssueTableLayoutBean} and {@link IssueTableWebComponent}
 * to display a table of issues. 
 */
public interface IssuePager
{
    public int getStart();
    public int getEnd();
    public int getTotal();
    public int getPreviousStart();
    public int getNextStart();
    public List getPages();
}
