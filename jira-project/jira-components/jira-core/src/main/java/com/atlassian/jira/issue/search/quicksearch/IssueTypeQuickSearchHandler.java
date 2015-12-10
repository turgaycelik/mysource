package com.atlassian.jira.issue.search.quicksearch;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.index.DocumentConstants;
import org.ofbiz.core.entity.GenericValue;

import java.util.Map;

public class IssueTypeQuickSearchHandler extends SingleWordQuickSearchHandler
{
    public final ConstantsManager constantsManager;

    public IssueTypeQuickSearchHandler(ConstantsManager constantsManager)
    {
        this.constantsManager = constantsManager;
    }

    public GenericValue getTypeByName(String name)
    {
        return getByName(constantsManager.getIssueTypes(), name);
    }

    protected Map handleWord(String word, QuickSearchResult searchResult)
    {
        GenericValue typeByName = getTypeByName(word);
        if (typeByName == null && (word.endsWith("S") || word.endsWith("s")))
        {
            typeByName = getTypeByName(word.substring(0, word.length() - 1));
        }
        return typeByName != null ? EasyMap.build(DocumentConstants.ISSUE_TYPE, typeByName.getString("id")) : null;
    }
}
