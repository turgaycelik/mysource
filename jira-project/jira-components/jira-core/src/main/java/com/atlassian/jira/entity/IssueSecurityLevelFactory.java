package com.atlassian.jira.entity;

import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.atlassian.jira.issue.security.IssueSecurityLevelImpl;
import com.atlassian.jira.ofbiz.FieldMap;
import org.ofbiz.core.entity.GenericValue;

/**
 * EntityFactory for IssueSecurityLevel
 *
 * @since v5.0
 * @see EntityFactory
 */
public class IssueSecurityLevelFactory extends AbstractEntityFactory<IssueSecurityLevel>
{
    @Override
    public String getEntityName()
    {
        return "SchemeIssueSecurityLevels";
    }

    @Override
    public IssueSecurityLevel build(GenericValue genericValue)
    {
        if (genericValue == null)
        {
            return null;
        }
        Builder builder = new Builder();
        builder.id(genericValue.getLong("id"));
        builder.name(genericValue.getString("name"));
        builder.description(genericValue.getString("description"));
        builder.scheme(genericValue.getLong("scheme"));
        return builder.build();
    }

    @Override
    public FieldMap fieldMapFrom(IssueSecurityLevel value)
    {
        return new FieldMap("id", value.getId())
                .add("name", value.getName())
                .add("description", value.getDescription())
                .add("scheme", value.getSchemeId());
    }

    public static class Builder
    {
        private Long id;
        private String name;
        private String description;
        private Long scheme;

        public Builder id(Long id)
        {
            this.id = id;
            return this;
        }

        public Builder name(String name)
        {
            this.name = name;
            return this;
        }

        public Builder description(String description)
        {
            this.description = description;
            return this;
        }

        public Builder scheme(Long scheme)
        {
            this.scheme = scheme;
            return this;
        }

        public IssueSecurityLevel build()
        {
            return new IssueSecurityLevelImpl(id, name, description, scheme);
        }
    }
    
}
