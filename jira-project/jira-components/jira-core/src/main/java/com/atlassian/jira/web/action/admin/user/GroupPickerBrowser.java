package com.atlassian.jira.web.action.admin.user;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.action.AbstractBrowser;
import com.atlassian.jira.web.bean.GroupBrowserFilter;
import com.atlassian.jira.web.bean.PagerFilter;
import webwork.action.ActionContext;
import webwork.util.BeanUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GroupPickerBrowser extends AbstractBrowser
{
    private List<Group> groups;
    private String formName;
    private String element;

    // Multi-select
    private boolean multiSelect = false;
    private List<String> previouslySelectedGroups = new ArrayList<String>();

    public GroupPickerBrowser()
    {
    }

    protected String doExecute() throws Exception
    {
        resetPager();

        BeanUtil.setProperties(params, getFilter());

        // If the user changes either the email filter, name filter or the group,
        // reset the cursor to the start of the filter result set.
        if (params.containsKey("nameFilter"))
        {
            if (!params.containsKey("start"))
            {
                setStart("0");
            }
        }

        // JRA-12989 - Reset the start to 0 if number of items returned is less than the pager start
        if (getBrowsableItems().size() <= getPager().getStart())
        {
            setStart("0");
        }

        return super.doExecute();
    }

    public PagerFilter getPager()
    {
        return getFilter();
    }

    public void resetPager()
    {
        ActionContext.getSession().put(SessionKeys.GROUP_PICKER_FILTER, null);
    }

    public GroupBrowserFilter getFilter()
    {
        GroupBrowserFilter filter = (GroupBrowserFilter) ActionContext.getSession().get(SessionKeys.GROUP_PICKER_FILTER);

        if (filter == null)
        {
            filter = new GroupBrowserFilter();
            ActionContext.getSession().put(SessionKeys.GROUP_PICKER_FILTER, filter);
        }

        return filter;
    }

    /**
     * Return the current 'page' of issues (given max and start) for the current filter
     */
    public List getCurrentPage()
    {
        return getFilter().getCurrentPage(getBrowsableItems());
    }

    public List getBrowsableItems()
    {
        if (groups == null)
        {
            try
            {
                groups = getFilter().getFilteredGroups();
            }
            catch (Exception e)
            {
                log.error("Exception getting groups: " + e, e);
                return null;
            }
        }

        return groups;
    }

    public Collection getGroups()
    {
        return getBrowsableItems();
    }

    /**
     * Get the name of the calling form
     *
     * @return form name
     */
    public String getFormName()
    {
        return formName;
    }

    /**
     * Set the name of the calling form
     *
     * @param formName form name
     */
    public void setFormName(String formName)
    {
        this.formName = formName;
    }

    /**
     * Get the name of the element for the value to be returned to
     *
     * @return the name of the element for the value to be returned to
     */
    public String getElement()
    {
        return element;
    }

    /**
     * Set the name of the element for the value to be returned to
     *
     * @param element the name of the element for the value to be returned to
     */
    public void setElement(String element)
    {
        this.element = element;
    }

    public boolean getPermission()
    {
        return hasPermission(Permissions.USER_PICKER);
    }

    // -----------------    Multi-select    ----------------------

    public String getPreviouslySelected()
    {
        StringBuilder stringBuilder = new StringBuilder();
        if (!previouslySelectedGroups.isEmpty())
        {
            stringBuilder.append(";");
        }

        for (final String groupName : previouslySelectedGroups)
        {
            stringBuilder.append(encode(groupName));
            stringBuilder.append(';');
        }

        return stringBuilder.toString();
    }

    public void setPreviouslySelected(String previouslySelected)
    {
        if (previouslySelected.length() != 0)
        {
            previouslySelected = previouslySelected.substring(1, previouslySelected.length()-1);
            final String[] groups = previouslySelected.split(";");
            for (String group : groups)
            {
                previouslySelectedGroups.add(decode(group));
            }
        }
    }

    public boolean wasPreviouslySelected(Group group)
    {
        return previouslySelectedGroups.contains(group.getName());
    }

    private String decode(final String group)
    {
        return group.replaceAll("%59", ";");
    }

    private String encode(final String group)
    {
        return group.replaceAll(";", "%59");
    }

    public boolean isMultiSelect()
    {
        return multiSelect;
    }

    public void setMultiSelect(boolean isMultiSelect)
    {
        this.multiSelect = isMultiSelect;
    }

}
