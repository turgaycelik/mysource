package com.atlassian.jira.issue.search.quicksearch;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.config.ConstantsManager;
import com.google.common.collect.ImmutableMap;
import org.ofbiz.core.entity.GenericValue;

import java.util.Map;

public class ResolutionQuickSearchHandler extends SingleWordQuickSearchHandler
{
    private static final String UNRESOLVED = "unresolved";
    private static final Map<String, String> UNRESOLVED_RESULT = ImmutableMap.of("resolution", "-1");
    public final ConstantsManager constantsManager;

    public ResolutionQuickSearchHandler(ConstantsManager constantsManager)
    {
        this.constantsManager = constantsManager;
    }

    public GenericValue getResolutionsByName(String name)
    {
        return getByName(constantsManager.getResolutions(), name);
    }

    protected Map handleWord(String word, QuickSearchResult searchResult)
    {
        if (UNRESOLVED.equalsIgnoreCase(word))
            return UNRESOLVED_RESULT;
        
        GenericValue resolutionByName = getResolutionsByName(word);
        return resolutionByName != null ? EasyMap.build("resolution", resolutionByName.getString("id")) : null;
    }
}
