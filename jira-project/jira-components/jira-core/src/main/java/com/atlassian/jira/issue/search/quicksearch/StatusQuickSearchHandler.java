package com.atlassian.jira.issue.search.quicksearch;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.config.ConstantsManager;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Map;

public class StatusQuickSearchHandler extends SingleWordQuickSearchHandler
{
    public final ConstantsManager constantsManager;

       public StatusQuickSearchHandler(ConstantsManager constantsManager)
       {
           this.constantsManager = constantsManager;
       }

    protected Map handleWord(String word, QuickSearchResult searchResult)
    {
        GenericValue statusByName = getStatusByName(word);
        return statusByName != null ? EasyMap.build("status", statusByName.getString("id")) : null;
    }

    private GenericValue getStatusByName(String name)
    {
        Collection statuses = constantsManager.getStatuses();
        return getByName(statuses, name);
    }

}
