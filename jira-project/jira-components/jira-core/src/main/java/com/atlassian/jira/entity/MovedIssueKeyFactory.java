package com.atlassian.jira.entity;

import com.atlassian.jira.issue.MovedIssueKey;
import com.atlassian.jira.ofbiz.FieldMap;
import org.ofbiz.core.entity.GenericValue;

import java.util.Map;

/**
 * @since v6.1
 */
public class MovedIssueKeyFactory extends AbstractEntityFactory<MovedIssueKey>
{
    @Override
    public String getEntityName()
    {
        return "MovedIssueKey";
    }

    @Override
    public MovedIssueKey build(GenericValue gv)
    {
        return new MovedIssueKey(gv.getLong(MovedIssueKey.ID), gv.getString(MovedIssueKey.OLD_ISSUE_KEY), gv.getLong(MovedIssueKey.ISSUE_ID));
    }

    @Override
    public Map<String, Object> fieldMapFrom(MovedIssueKey value)
    {
        return new FieldMap(MovedIssueKey.ID, value.getId())
                .add(MovedIssueKey.OLD_ISSUE_KEY, value.getOldIssueKey())
                .add(MovedIssueKey.ISSUE_ID, value.getIssueId());
    }
}
