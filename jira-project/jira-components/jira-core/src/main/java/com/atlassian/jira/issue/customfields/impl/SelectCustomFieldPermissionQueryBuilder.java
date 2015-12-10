package com.atlassian.jira.issue.customfields.impl;

import java.util.List;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import com.atlassian.jira.issue.customfields.statistics.SelectStatisticsMapper;

/**
 * Builds queries used by select custom field types to filter based on permissions.
 *
 * @see MultiSelectCFType
 * @see SelectCFType
 */
public class SelectCustomFieldPermissionQueryBuilder
{

    /**
     * Builds a query for selected group by given group name.
     *
     * @param fieldID custom field id
     * @param groupName name of the group
     * @param idsFromName resolved select's option identifiers by group name
     * @return query
     */
    public static Query getQueryForGroup(String fieldID, String groupName, List<String> idsFromName)
    {
        BooleanQuery termQueries = new BooleanQuery();
        for (String id : idsFromName)
        {
            termQueries.add(new TermQuery(new Term(fieldID + SelectStatisticsMapper.RAW_VALUE_SUFFIX, id)), BooleanClause.Occur.SHOULD);
        }
        // means that for this groupName there is no such option, but we want to avoid this method to return "empty" query
        if (idsFromName.isEmpty())
        {
            termQueries.add(new TermQuery(new Term(fieldID + SelectStatisticsMapper.RAW_VALUE_SUFFIX, groupName)), BooleanClause.Occur.SHOULD);
        }
        return termQueries;
    }
}
