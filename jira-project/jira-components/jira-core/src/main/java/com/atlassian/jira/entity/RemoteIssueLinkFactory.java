package com.atlassian.jira.entity;

import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.issue.link.RemoteIssueLinkBuilder;
import com.atlassian.jira.ofbiz.FieldMap;
import org.ofbiz.core.entity.GenericValue;

/**
 * RemoteIssueLink EntityFactory
 *
 * @since v5.0
 */
public class RemoteIssueLinkFactory extends AbstractEntityFactory<RemoteIssueLink>
{
    @Override
    public String getEntityName()
    {
        return "RemoteIssueLink";
    }

    @Override
    public RemoteIssueLink build(final GenericValue genericValue)
    {
        if (genericValue == null)
        {
            return null;
        }

        final RemoteIssueLinkBuilder builder = new RemoteIssueLinkBuilder();
        builder.id(genericValue.getLong("id"));
        builder.issueId(genericValue.getLong("issueid"));
        builder.globalId(genericValue.getString("globalid"));
        builder.title(genericValue.getString("title"));
        builder.summary(genericValue.getString("summary"));
        builder.url(genericValue.getString("url"));
        builder.iconUrl(genericValue.getString("iconurl"));
        builder.iconTitle(genericValue.getString("icontitle"));
        builder.relationship(genericValue.getString("relationship"));
        builder.resolved(genericValue.getBoolean("resolved"));
        builder.statusName(genericValue.getString("statusname"));
        builder.statusDescription(genericValue.getString("statusdescription"));
        builder.statusIconUrl(genericValue.getString("statusiconurl"));
        builder.statusIconTitle(genericValue.getString("statusicontitle"));
        builder.statusIconLink(genericValue.getString("statusiconlink"));
        builder.statusCategoryKey(genericValue.getString("statuscategorykey"));
        builder.statusCategoryColorName(genericValue.getString("statuscategorycolorname"));
        builder.applicationType(genericValue.getString("applicationtype"));
        builder.applicationName(genericValue.getString("applicationname"));
        return builder.build();
    }

    @Override
    public FieldMap fieldMapFrom(final RemoteIssueLink value)
    {
        return new FieldMap("id", value.getId())
                .add("issueid", value.getIssueId())
                .add("globalid", value.getGlobalId())
                .add("title", value.getTitle())
                .add("summary", value.getSummary())
                .add("url", value.getUrl())
                .add("iconurl", value.getIconUrl())
                .add("icontitle", value.getIconTitle())
                .add("relationship", value.getRelationship())
                .add("resolved", booleanToString(value.isResolved()))
                .add("statusname", value.getStatusName())
                .add("statusdescription", value.getStatusDescription())
                .add("statusiconurl", value.getStatusIconUrl())
                .add("statusicontitle", value.getStatusIconTitle())
                .add("statusiconlink", value.getStatusIconLink())
                .add("statuscategorykey", value.getStatusCategoryKey())
                .add("statuscategorycolorname", value.getStatusCategoryColorName())
                .add("applicationtype", value.getApplicationType())
                .add("applicationname", value.getApplicationName());
    }

    /**
     * We need to convert Booleans to Strings as there is a bug creating GenericValues with Booleans (see JRADEV-6904).
     *
     * @param b a Boolean
     * @return a String
     */
    private String booleanToString(final Boolean b)
    {
        if (b == null)
        {
            return null;
        }

        if (b.equals(Boolean.TRUE))
        {
            return "Y";
        }

        return "N";
    }
}
