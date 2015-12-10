package com.atlassian.jira.entity;

import com.atlassian.jira.issue.security.IssueSecurityLevelPermission;
import com.atlassian.jira.ofbiz.FieldMap;
import org.ofbiz.core.entity.GenericValue;

/**
 * EntityFactory for IssueSecurityLevelPermission
 *
 * @since v5.2
 */
public class IssueSecurityLevelPermissionFactory extends AbstractEntityFactory<IssueSecurityLevelPermission>
{
    @Override
    public String getEntityName()
    {
        return "SchemeIssueSecurities";
    }

    @Override
    public IssueSecurityLevelPermission build(GenericValue genericValue)
    {
        if (genericValue == null)
        {
            return null;
        }
        Builder builder = new Builder();
        builder.id(genericValue.getLong("id"));
        builder.schemeId(genericValue.getLong("scheme"));
        builder.securityLevelId(genericValue.getLong("security"));
        builder.type(genericValue.getString("type"));
        builder.parameter(genericValue.getString("parameter"));
        return builder.build();
    }

    @Override
    public FieldMap fieldMapFrom(IssueSecurityLevelPermission value)
    {
        return new FieldMap("id", value.getId())
                .add("scheme", value.getSchemeId())
                .add("security", value.getSecurityLevelId())
                .add("type", value.getType())
                .add("parameter", value.getParameter());
    }

    public static class Builder
    {
        private Long id;
        private Long schemeId;
        private Long securityLevelId;
        private String type;
        private String parameter;

        public Builder id(Long id)
        {
            this.id = id;
            return this;
        }

        public Builder schemeId(Long schemeId)
        {
            this.schemeId = schemeId;
            return this;
        }

        public Builder securityLevelId(Long securityLevelId)
        {
            this.securityLevelId = securityLevelId;
            return this;
        }

        public Builder type(String type)
        {
            this.type = type;
            return this;
        }

        public Builder parameter(String parameter)
        {
            this.parameter = parameter;
            return this;
        }

        public IssueSecurityLevelPermission build()
        {
            return new IssueSecurityLevelPermission(id, schemeId, securityLevelId, type, parameter);
        }
    }
    
}
