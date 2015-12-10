package com.atlassian.jira.issue.search.quicksearch;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.config.ConstantsManager;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Map;

public class PriorityQuickSearchHandler extends SingleWordQuickSearchHandler
{
    public final ConstantsManager constantsManager;

       public PriorityQuickSearchHandler(ConstantsManager constantsManager)
       {
           this.constantsManager = constantsManager;
       }

    protected Map handleWord(String word, QuickSearchResult searchResult)
    {
        GenericValue statusByName = getPriorityByName(word);
        return statusByName != null ? EasyMap.build("priority", statusByName.getString("id")) : null;
    }

    private GenericValue getPriorityByName(String name)
    {
        Collection statuses = constantsManager.getPriorities();
        return getByName(statuses, name);
    }

}
