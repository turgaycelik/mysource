package com.atlassian.jira.issue.transport.impl;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.SearchContextImpl;
import com.atlassian.jira.util.ParameterUtils;
import com.atlassian.jira.web.action.util.navigator.IssueNavigatorType;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

public class IssueNavigatorActionParams extends ActionParamsImpl
{
    private static final String PAGER_SLASH_START = "pager/start";
    private static final String NAVIGATOR_TYPE_PARAMETER = "navType";
    private static final String NAVIGATOR_TOGGLE_AUTOCOMPLETE = "autocomplete";

    public IssueNavigatorActionParams()
    {
    }

    public IssueNavigatorActionParams(Map params)
    {
        super(params);
    }

    public boolean isUserCreated()
    {
        return "true".equals(getFirstValueForKey("usercreated"));        
    }

    public boolean isUpdateParamsRequired()
    {
        return containsKey("reset");
    }

    public boolean isClearOldFilter()
    {
        return containsKey("clear");
    }

    public boolean isUpdateExistingFilter()
    {
        return "update".equals(getFirstValueForKey("reset"));
    }

    /**
     * This parameter is defined in the issue type searcher. It is used to denote that the user changed the
     * project/issue type context, and wants to refresh their options for the Simple filter form.
     * 
     * @return true if the user submitted this request; false otherwise.
     */
    public boolean isRefreshOnly()
    {
        return ("true".equals(getFirstValueForKey("refreshFilter")));
    }

    public boolean isCreateNewFilter()
    {
        return containsKey("createNew");
    }

    public boolean isLoadSavedFilter()
    {
        return containsKey("requestId");
    }

    public SearchContext getSearchContext()
    {
        String[] projectIdsArray = getValuesForKey("pid");
        final List<Long> projectIds;
        if (projectIdsArray != null)
        {
            projectIds = ParameterUtils.getLongListFromStringArray(projectIdsArray);
            // remove the all projects flag
            projectIds.remove(-1L);
        }
        else
        {
            projectIds = null;
        }

        String[] issueTypeIdsArray = getValuesForKey(DocumentConstants.ISSUE_TYPE);
        final List issueTypeIds = issueTypeIdsArray != null ? EasyList.buildNonNull(issueTypeIdsArray) : null;


        return new SearchContextImpl(null, projectIds, issueTypeIds);
    }

    public boolean isAddParamsRequired()
    {
        return containsKey("addParams");
    }

    public boolean isTooComplex()
    {
        return containsKey("tooComplex");
    }

    public final boolean isNavigatorTypeSpecified()
    {
        return containsKey(NAVIGATOR_TYPE_PARAMETER);
    }

    public final IssueNavigatorType getNavigatorType()
    {
        String str = getFirstValueForKey(NAVIGATOR_TYPE_PARAMETER);
        if (StringUtils.isNotBlank(str))
        {
            return IssueNavigatorType.getTypeFromString(str);
        }
        else
        {
            return null;
        }
    }

    public final boolean isAutocompletePreferenceSpecified()
    {
        return containsKey(NAVIGATOR_TOGGLE_AUTOCOMPLETE);
    }

    public final Boolean getNewAutocompletePreference()
    {
        String str = getFirstValueForKey(NAVIGATOR_TOGGLE_AUTOCOMPLETE);
        if (StringUtils.isNotBlank(str))
        {
            return Boolean.valueOf(str);
        }
        else
        {
            return null;
        }
    }

    public boolean isPagerStartSpecified()
    {
        return containsKey(PAGER_SLASH_START);
    }

    public int getPagerStart()
    {
        try
        {
            final String pageStart = getFirstValueForKey(PAGER_SLASH_START);
            return Integer.valueOf(pageStart);
        }
        catch (NumberFormatException e)
        {
            return 0;
        }
    }
}
