package com.atlassian.jira.issue.fields.option;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates all options and option groups for rendering an Assignee field.
 *
 * @since v5.0
 */
public class AssigneeOptions
{
    private final List<SelectChild> options = new ArrayList<SelectChild>();
    private boolean isLoggedInUserAssignable = false;
    private boolean isInvalidAssigneeSelected = false;

    public void add(SelectChild selectChild)
    {
        options.add(selectChild);
    }

    public void setLoggedInUserIsAssignable(boolean loggedInUserIsAssignable)
    {
        this.isLoggedInUserAssignable = loggedInUserIsAssignable;
    }

    public boolean isLoggedInUserAssignable()
    {
        return isLoggedInUserAssignable;
    }

    public void setInvalidAssigneeSelected(boolean invalidAssigneeSelected)
    {
        this.isInvalidAssigneeSelected = invalidAssigneeSelected;
    }

    public boolean isInvalidAssigneeSelected()
    {
        return isInvalidAssigneeSelected;
    }

    public List<SelectChild> getOptions()
    {
        return options;
    }
}
