package com.atlassian.jira.entity;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.link.IssueLink;
import com.atlassian.jira.issue.link.IssueLinkImpl;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.ofbiz.FieldMap;
import org.ofbiz.core.entity.GenericValue;

/**
 * IssueLink EntityFactory
 *
 * @since v5.0
 */
public class IssueLinkFactory extends AbstractEntityFactory<IssueLink>
{
    @Override
    public String getEntityName()
    {
        return "IssueLink";
    }

    @Override
    public IssueLink build(final GenericValue genericValue)
    {
        if (genericValue == null)
        {
            return null;
        }

        return new IssueLinkImpl(genericValue, ComponentAccessor.getComponent(IssueLinkTypeManager.class), ComponentAccessor.getIssueManager());
    }

    @Override
    public FieldMap fieldMapFrom(final IssueLink value)
    {
        return new FieldMap("id", value.getId())
                .add("linktype", value.getLinkTypeId())
                .add("source", value.getSourceId())
                .add("destination", value.getDestinationId())
                .add("sequence", value.getSequence());
    }
}
