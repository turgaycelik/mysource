package com.atlassian.jira.issue.security;

import com.atlassian.jira.entity.NamedEntityBuilder;
import org.ofbiz.core.entity.GenericValue;

/**
 * @since v5.2
 */
public class IssueSecurityLevelSchemeBuilder implements NamedEntityBuilder<IssueSecurityLevelScheme>
{
    @Override
    public String getEntityName()
    {
        return "IssueSecurityScheme";
    }

    @Override
    public IssueSecurityLevelScheme build(GenericValue gv)
    {
        return new IssueSecurityLevelScheme(
                gv.getLong("id"),
                gv.getString("name"),
                gv.getString("description"),
                gv.getLong("defaultlevel")
                );
    }
}
