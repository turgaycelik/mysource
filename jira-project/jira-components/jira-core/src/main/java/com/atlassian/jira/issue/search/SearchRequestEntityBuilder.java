package com.atlassian.jira.issue.search;

import com.atlassian.jira.entity.NamedEntityBuilder;
import org.ofbiz.core.entity.GenericValue;

/**
 * @since v5.2
 */
public class SearchRequestEntityBuilder implements NamedEntityBuilder<SearchRequestEntity>
{
    @Override
    public String getEntityName()
    {
        return "SearchRequest";
    }

    @Override
    public SearchRequestEntity build(GenericValue gv)
    {
        Long id = gv.getLong("id");
        String name = gv.getString("name");
        String author = gv.getString("author");
        String description = gv.getString("description");
        String user = gv.getString("user");
        String group = gv.getString("group");
        Long project = gv.getLong("project");
        String request = gv.getString("request");
        Long favCount = gv.getLong("favCount");

        return new SearchRequestEntity(id, name, author, description, user, group, project, request, favCount);
    }
}
